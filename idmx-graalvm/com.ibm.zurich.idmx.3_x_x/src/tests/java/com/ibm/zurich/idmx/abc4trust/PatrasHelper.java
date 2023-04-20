//* Licensed Materials - Property of IBM                                     *
//* com.ibm.zurich.idmx.3_x_x                                                *
//* (C) Copyright IBM Corp. 2015. All Rights Reserved.                       *
//* US Government Users Restricted Rights - Use, duplication or              *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp.        *
//*                                                                          *
//* The contents of this file are subject to the terms of either the         *
//* International License Agreement for Identity Mixer Version 1.2 or the    *
//* Apache License Version 2.0.                                              *
//*                                                                          *
//* The license terms can be found in the file LICENSE.txt that is provided  *
//* together with this software.                                             *
//*/**/***********************************************************************

package com.ibm.zurich.idmx.abc4trust;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.net.URI;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.ibm.zurich.idmx.configuration.Configuration;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.SerializationException;
import com.ibm.zurich.idmx.jaxb.JaxbHelperClass;
import com.ibm.zurich.idmx.tests.TestUtils;
import com.ibm.zurich.idmx.tests.setup.TestSystemParameters;

import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.exceptions.TokenVerificationException;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.returnTypes.IssuMsgOrCredDesc;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.AttributeDescription;
import eu.abc4trust.xml.Credential;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.IssuanceMessageAndBoolean;
import eu.abc4trust.xml.IssuancePolicy;
import eu.abc4trust.xml.IssuanceTokenAndIssuancePolicy;
import eu.abc4trust.xml.IssuanceTokenDescription;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.PresentationTokenDescription;
import eu.abc4trust.xml.PseudonymWithMetadata;

/**
 * 
 */
public class PatrasHelper extends GeneralHelper {

  private Random random = new Random();
  private static final String USERNAME = "user";


  public PatrasHelper() {
    super();
  }


  @Override
  public void loadDefaultResources() throws SerializationException, KeyManagerException,
      CredentialManagerException, ConfigurationException, CryptoEngineException {
    // Load resources
    loadSystemParamters("parameters/sp.xml");

    loadIssuerParamters("parameters/ip-cl.xml");
    loadIssuerParamters("parameters/ip-brands.xml");

    loadCredentialSpecification("credSpecs/credentialSpecificationPatrasCourse.xml");
    loadCredentialSpecification("credSpecs/credentialSpecificationPatrasUniversity.xml");
    loadCredentialSpecification("credSpecs/credentialSpecificationPatrasCourse_revocable.xml");
    loadCredentialSpecification("credSpecs/credentialSpecificationPatrasUniversity_revocable.xml");
    loadCredentialSpecification("credSpecs/credentialSpecificationPatrasTombola.xml");
    
    verifierParameters = verifier_ce.createVerifierParameters(keyManagers.get(0).getSystemParameters());
  }


  @Override
  public String resolveDefaultPath(String filename) {
    return "patras/" + filename;
  }


  public List<Attribute> populateIssuerAttributes(Map<String, Object> issuerAttsMap, URI credSpecURI)
      throws Exception {
    List<Attribute> issuerAtts = new LinkedList<Attribute>();
    ObjectFactory of = new ObjectFactory();

    // TODO get the user keyManager
    CredentialSpecification credentialSpecification =
        keyManagers.get(0).getCredentialSpecification(credSpecURI);

    for (AttributeDescription attdesc : credentialSpecification.getAttributeDescriptions()
        .getAttributeDescription()) {
      Attribute att = of.createAttribute();
      att.setAttributeUID(URI.create("" + this.random.nextInt()));
      URI type = attdesc.getType();
      AttributeDescription attd = of.createAttributeDescription();
      attd.setDataType(attdesc.getDataType());
      attd.setEncoding(attdesc.getEncoding());
      attd.setType(type);
      att.setAttributeDescription(attd);
      Object value = issuerAttsMap.get(type.toString());
      if (value != null) {
        att.setAttributeValue(value);
      }
      // TODO: in case of carry over attributes this is not good enough / there needs to be another
      // list...
      issuerAtts.add(att);
    }
    return issuerAtts;
  }


  /**
   * Runs the issuance protocol using the entities as initialised.
   */
  public void runDeviceIssuance(IssuancePolicy policy, IssuanceTokenDescription itd,
      Credential sourceCred, List<Attribute> issuerAttributes, String credName) throws Exception,
      CryptoEngineException {
    
    if(policy.getVerifierParameters() == null) {
      policy.setVerifierParameters(verifierParameters);
    }

    List<URI> creds = Collections.emptyList();
    List<URI> nyms = Collections.emptyList();

    if (sourceCred != null) {
      user_cm.storeCredential(USERNAME, sourceCred);
      URI sourceUri = sourceCred.getCredentialDescription().getCredentialUID();
      creds = Collections.singletonList(sourceUri);
    }

    if (!policy.getPresentationPolicy().getPseudonym().isEmpty()) {

      String scope = policy.getPresentationPolicy().getPseudonym().get(0).getScope();
      boolean exclusive = policy.getPresentationPolicy().getPseudonym().get(0).isExclusive();
      URI secret = URI.create("secret");
      URI pseudonymUri = URI.create("nym");
      PseudonymWithMetadata pwm = user_ce.createPseudonym(USERNAME, pseudonymUri, scope, exclusive, secret);
      if (Configuration.debug()) {
        String nym_xml =
            JaxbHelperClass.serialize((new ObjectFactory()).createPseudonymWithMetadata(pwm));
        System.out.println(nym_xml);
      }
      user_cm.storePseudonym(USERNAME, pwm);
      itd.getPresentationTokenDescription().getPseudonym().get(0)
          .setPseudonymValue(pwm.getPseudonym().getPseudonymValue());
      nyms = Collections.singletonList(pseudonymUri);
    }


    IssuanceMessageAndBoolean imab;
    IssuMsgOrCredDesc imcd = new IssuMsgOrCredDesc();
    imab = issuer_ce.initIssuanceProtocol(policy, issuerAttributes, null);

    // Complex issuance requires pre-issuance presentation

    List<Attribute> atts = Collections.emptyList();
    imcd.im = user_ce.createIssuanceToken(USERNAME, imab.getIssuanceMessage(), itd, creds, nyms, atts);

    // Issuer needs to check if token satisfies policy
    IssuanceTokenAndIssuancePolicy itip = issuer_ce.extractIssuanceTokenAndPolicy(imcd.im);
    assertEquals(
      JaxbHelperClass.serialize(new ObjectFactory().createIssuancePolicy(policy)),
      JaxbHelperClass.serialize(new ObjectFactory().createIssuancePolicy(itip.getIssuancePolicy())));
    assertEquals(JaxbHelperClass.serialize(new ObjectFactory().createIssuanceTokenDescription(itd),
        true), JaxbHelperClass.serialize(new ObjectFactory().createIssuanceTokenDescription(itip
        .getIssuanceToken().getIssuanceTokenDescription()), true));

    while (!imab.isLastMessage()) {
      imab = issuer_ce.issuanceProtocolStep(imcd.im);
      imcd = user_ce.issuanceProtocolStep(USERNAME, imab.getIssuanceMessage());
    }

    assertNotNull(imcd.cd);
    assertNull(imcd.im);

    Credential cNew = user_cm.getCredential(USERNAME, imcd.cd.getCredentialUID());

    String c_xml = JaxbHelperClass.serialize((new ObjectFactory()).createCredential(cNew), true);
    if (Configuration.debug()) {
      System.out.println(c_xml);
    }
    TestUtils.saveToFile(
        c_xml,
        TestSystemParameters.BASE_LOCATION.resolve(
            "abc4trust/patras/credentials/credential" + credName + ".xml").toString());

  }


  /**
   * Runs the verification protocol using the entities as initialised.
   */
  @Override
public PresentationToken runPresentationProtocol(PresentationTokenDescription ptd, List<URI> creds)
      throws Exception {

    PresentationToken pt = runPresentationProtocolProver(ptd, creds);
    runPresentationProtocolVerifier(pt);

    return pt;
  }


  void runPresentationProtocolVerifier(PresentationToken pt)
      throws TokenVerificationException, CryptoEngineException {
    assertEquals(true, verifier_ce.verifyToken(pt, verifierParameters));
  }


  PresentationToken runPresentationProtocolProver(PresentationTokenDescription ptd,
      List<URI> creds) throws CryptoEngineException, SerializationException,
      CredentialManagerException {
    List<URI> nyms = Collections.emptyList();

    if (!ptd.getPseudonym().isEmpty()) {
      String scope = ptd.getPseudonym().get(0).getScope();
      boolean exclusive = ptd.getPseudonym().get(0).isExclusive();
      URI secret = URI.create("secret");
      URI pseudonymUri = URI.create("nym");
      PseudonymWithMetadata pwm = user_ce.createPseudonym(USERNAME, pseudonymUri, scope, exclusive, secret);
      if (Configuration.debug()) {
        String nym_xml =
            JaxbHelperClass.serialize((new ObjectFactory()).createPseudonymWithMetadata(pwm));
        System.out.println(nym_xml);
      }
      user_cm.storePseudonym(USERNAME, pwm);

      ptd.getPseudonym().get(0).setPseudonymValue(pwm.getPseudonym().getPseudonymValue());

      nyms = Collections.singletonList(pseudonymUri);
    }

    PresentationToken pt = user_ce.createPresentationToken(USERNAME, ptd, verifierParameters, creds, nyms);

    if (Configuration.debug()) {
      String pt_xml = JaxbHelperClass.serialize((new ObjectFactory()).createPresentationToken(pt));
      System.out.println(pt_xml);
    }
    return pt;
  }

}
