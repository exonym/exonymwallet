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

package com.ibm.zurich.idmx.orchestration.issuance;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.ibm.zurich.idmix.abc4trust.facades.CredentialFacade;
import com.ibm.zurich.idmix.abc4trust.facades.IssuanceMessageFacade;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.IssuanceOrchestrationException;
import com.ibm.zurich.idmx.exception.ProofException;
import com.ibm.zurich.idmx.exception.SerializationException;
import com.ibm.zurich.idmx.interfaces.state.CarryOverStateIssuer;
import com.ibm.zurich.idmx.interfaces.state.CarryOverStateRecipient;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.jaxb.JaxbHelperClass;
import com.ibm.zurich.idmx.tests.TestInitialisation;
import com.ibm.zurich.idmx.tests.TestUtils;

import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.returnTypes.IssuMsgOrCredDesc;
import eu.abc4trust.xml.CredentialDescription;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.IssuanceMessage;
import eu.abc4trust.xml.IssuanceMessageAndBoolean;
import eu.abc4trust.xml.IssuancePolicy;
import eu.abc4trust.xml.KeyPair;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.ZkProof;

/**
 * 
 */
public class IssuanceOrchestrationTest extends TestInitialisation {

  SystemParameters systemParameters;
  KeyPair keyPair_cl;
  KeyPair keyPair_brands;
  CredentialSpecification credSpec;

  private CarryOverStateIssuer carryOverStateIssuer;
  private CarryOverStateRecipient carryOverStateRecipient;
  private static final String USERNAME = "user";


  @Before
  public void setUp() throws SerializationException, ConfigurationException,
      UnsupportedEncodingException, JAXBException, SAXException {
    systemParameters = TestUtils.getResource("../sp_default.xml", SystemParameters.class, this);
    keyPair_cl = TestUtils.getResource("../keyPair_cl.xml", KeyPair.class, this);
    keyPair_brands = TestUtils.getResource("../keyPair_brands.xml", KeyPair.class, this);
    credSpec =
        TestUtils.getResource("../credSpec_simpleID.xml", CredentialSpecification.class, this);
    // (CredentialSpecification) XmlUtils.getObjectFromXML(
    // this.getClass().getResourceAsStream(CREDENTIAL_SPECIFICATION_ID_CARD), true);
  }

  @Ignore
  @Test
  public void simpleIssuance_CL() throws SerializationException, ConfigurationException,
      KeyManagerException, IOException, CredentialManagerException, CryptoEngineException,
      IssuanceOrchestrationException, JAXBException, SAXException, ProofException {

    // Initialise system parameters, issuer parameters, and credential specification
    // (such that they are available from keyManager and credentialManager)
    initSystemParameters(systemParameters, keyManager);
    initIssuerKeyPair(keyPair_cl, credentialManagerIssuer, keyManager);
    initCredentialSpecification(credSpec, keyManager);

    // Load issuance policy for simple issuance
    IssuancePolicy issuancePolicy =
        TestUtils.getResource("issuancePolicy_simpleID_cl.xml", IssuancePolicy.class, this);

    // Initialise the list of attributes
    List<BigInt> issuerProvidedAttributes = new ArrayList<BigInt>();
    issuerProvidedAttributes.add(bigIntFactory.valueOf(12456415744L));
    issuerProvidedAttributes.add(bigIntFactory.valueOf(123));
    issuerProvidedAttributes.add(bigIntFactory.valueOf(1245621453466415744L));

    IssuanceMessage issuanceMessage = new ObjectFactory().createIssuanceMessage();
    IssuanceMessageFacade issuanceMessageFacade =
        new IssuanceMessageFacade(issuanceMessage, bigIntFactory);
    issuanceMessageFacade.setContext(null);
    issuanceMessageFacade.setIssuancePolicy(issuancePolicy);
    issuanceMessageFacade.setVerifierParameters(null);
    issuanceMessageFacade.setIssuerProvidedAttributes(null/*issuerProvidedAttributes*/);

    boolean lastMessage = false;
    lastMessage = simpleIssuanceProtocol(issuanceMessage);

    assertEquals(true, lastMessage);

  }

  @Ignore
  @Test
  public void simpleIssuance_Brands() throws SerializationException, ConfigurationException,
      KeyManagerException, IOException, CredentialManagerException, CryptoEngineException,
      IssuanceOrchestrationException, JAXBException, SAXException, ProofException {

    // Initialise system parameters, issuer parameters, and credential specification
    // (such that they are available from keyManager and credentialManager)
    initSystemParameters(systemParameters, keyManager);
    initIssuerKeyPair(keyPair_brands, credentialManagerIssuer, keyManager);
    initCredentialSpecification(credSpec, keyManager);

    // Load issuance policy for simple issuance
    IssuancePolicy issuancePolicy =
        TestUtils.getResource("issuancePolicy_simpleID_brands.xml", IssuancePolicy.class, this);

    // Initialise the list of attributes
    List<BigInt> issuerProvidedAttributes = new ArrayList<BigInt>();
    issuerProvidedAttributes.add(bigIntFactory.valueOf(12456415744L));
    issuerProvidedAttributes.add(bigIntFactory.valueOf(123));
    issuerProvidedAttributes.add(bigIntFactory.valueOf(1245621453466415744L));

    IssuanceMessage issuanceMessage = new ObjectFactory().createIssuanceMessage();
    IssuanceMessageFacade issuanceMessageFacade =
        new IssuanceMessageFacade(issuanceMessage, bigIntFactory);
    issuanceMessageFacade.setContext(null);
    issuanceMessageFacade.setIssuancePolicy(issuancePolicy);
    issuanceMessageFacade.setVerifierParameters(null);
    issuanceMessageFacade.setIssuerProvidedAttributes(null/*issuerProvidedAttributes*/);

    boolean lastMessage = false;
    lastMessage = simpleIssuanceProtocol(issuanceMessage);

    assertEquals(true, lastMessage);
  }

  @Ignore
  @Test
  public void advancedIssuance_CL() throws SerializationException, ConfigurationException,
      KeyManagerException, IOException, CredentialManagerException, CryptoEngineException,
      IssuanceOrchestrationException, JAXBException, SAXException, ProofException {

    // Initialise system parameters, issuer parameters, and credential specification
    // (such that they are available from keyManager and credentialManager)
    initSystemParameters(systemParameters, keyManager);
    initIssuerKeyPair(keyPair_cl, credentialManagerIssuer, keyManager);
    initCredentialSpecification(credSpec, keyManager);

    // Load issuance policy for simple issuance
    IssuancePolicy issuancePolicy =
        TestUtils.getResource("issuancePolicy_simpleID_cl.xml", IssuancePolicy.class, this);

    // Initialise the list of attributes
    List<BigInt> issuerProvidedAttributes = new ArrayList<BigInt>();
    issuerProvidedAttributes.add(bigIntFactory.valueOf(12456415744L));
    issuerProvidedAttributes.add(bigIntFactory.valueOf(123));
    issuerProvidedAttributes.add(bigIntFactory.valueOf(1245621453466415744L));

    IssuanceMessage issuanceMessage = new ObjectFactory().createIssuanceMessage();
    IssuanceMessageFacade issuanceMessageFacade =
        new IssuanceMessageFacade(issuanceMessage, bigIntFactory);
    issuanceMessageFacade.setContext(null);
    issuanceMessageFacade.setIssuancePolicy(issuancePolicy);
    issuanceMessageFacade.setVerifierParameters(null);
    issuanceMessageFacade.setIssuerProvidedAttributes(null/*issuerProvidedAttributes*/);

    boolean lastMessage = false;
    lastMessage = simpleIssuanceProtocol(issuanceMessage);

    assertEquals(true, lastMessage);

  }

  private boolean simpleIssuanceProtocol(IssuanceMessage issuanceMessage)
      throws KeyManagerException, ConfigurationException, CredentialManagerException,
      ProofException, IssuanceOrchestrationException, SerializationException, CryptoEngineException {
    IssuanceMessageFacade issuanceMessageFacade;

    boolean lastMessage = false;
    CredentialDescription credDesc = null;
    do {
      IssuanceMessageAndBoolean issuanceMessageIssuer =
          cryptoEngineIssuer.issuanceStep(issuanceMessage);
      lastMessage = issuanceMessageIssuer.isLastMessage();

      // Serialisation of the proof
      issuanceMessageFacade = new IssuanceMessageFacade(issuanceMessageIssuer, bigIntFactory);
      ZkProof proof = issuanceMessageFacade.getZkProof();
      if (proof != null) {
        String xmlProof = JaxbHelperClass.serialize((new ObjectFactory()).createZkProof(proof));
        System.out.println(xmlProof);
      }

      IssuMsgOrCredDesc issuanceMessageRecipient =
          cryptoEngineRecipient.issuanceStep(USERNAME, issuanceMessageIssuer.getIssuanceMessage());
      credDesc = issuanceMessageRecipient.cd;
      issuanceMessage = issuanceMessageRecipient.im;
    } while (credDesc == null);

    // // Serialise issuer log
    // URI issuanceLogEntry = issuanceMessageFacade.getIssuanceLogEntry();
    // System.out.println(issuanceLogEntry.toString());

    // Serialise credential
    URI credentialId = credDesc.getCredentialUID();
    CredentialFacade credentialFacade =
        new CredentialFacade(credentialManagerUser.getCredential(USERNAME, credentialId));
    System.out.println(credentialFacade.serialize());

    return lastMessage;
  }
}
