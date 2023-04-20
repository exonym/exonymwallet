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

package com.ibm.zurich.idmx.orchestration.presentation;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.ibm.zurich.idmix.abc4trust.facades.CredentialFacade;
import com.ibm.zurich.idmx.annotations.Nullable;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.IssuanceOrchestrationException;
import com.ibm.zurich.idmx.exception.ProofException;
import com.ibm.zurich.idmx.exception.SerializationException;
import com.ibm.zurich.idmx.interfaces.util.Pair;
import com.ibm.zurich.idmx.jaxb.JaxbHelperClass;
import com.ibm.zurich.idmx.tests.TestInitialisation;
import com.ibm.zurich.idmx.tests.TestUtils;

import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.xml.ApplicationData;
import eu.abc4trust.xml.AttributeInToken;
import eu.abc4trust.xml.AttributePredicate;
import eu.abc4trust.xml.Credential;
import eu.abc4trust.xml.CredentialInToken;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.KeyPair;
import eu.abc4trust.xml.Message;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.PresentationTokenDescription;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.VerifierParameters;

/**
 * 
 */
@Ignore("TODO(enr): Cannot fix the XML file credential_cl.xml")
public class PresentationOrchestrationTest_Cl extends TestInitialisation {

  SystemParameters systemParameters;
  KeyPair keyPair_cl;
  CredentialSpecification credSpec;
  private static final String USERNAME = "user";

  @Before
  public void setUp() throws SerializationException, ConfigurationException,
      UnsupportedEncodingException, JAXBException, SAXException {
    systemParameters = TestUtils.getResource("../sp_default.xml", SystemParameters.class, this);
    keyPair_cl = TestUtils.getResource("../keyPair_cl.xml", KeyPair.class, this);
    credSpec =
        TestUtils.getResource("../credSpec_simpleID.xml", CredentialSpecification.class, this);

  }

  @Test
  public void presentation_Cl() throws SerializationException, ConfigurationException,
      KeyManagerException, IOException, CredentialManagerException, CryptoEngineException,
      IssuanceOrchestrationException, JAXBException, SAXException, ProofException {

    // Initialise system parameters, issuer parameters, and credential specification
    // (such that they are available from keyManager and credentialManager)
    initSystemParameters(systemParameters, keyManager);
    initIssuerKeyPair(keyPair_cl, credentialManagerIssuer, keyManager);
    initCredentialSpecification(credSpec, keyManager);


    // Initialise the list of credentials and pseudonyms
    List<URI> credentialUris = new ArrayList<URI>();
    List<URI> pseudonymUris = new ArrayList<URI>();

    // Create credential in token
    CredentialFacade credentialFacade =
        initCredential(TestUtils.getResource("credential_cl.xml", Credential.class, this),
            credentialManagerUser);

    PresentationTokenDescription ptd =
        initCredentialInToken(null, null, credentialFacade, credentialUris);

    boolean result = presentationProtocol(ptd, null, credentialUris, pseudonymUris);

    assertEquals(true, result);
  }


  @Test
  public void presentation_ClReveal() throws SerializationException, ConfigurationException,
      KeyManagerException, IOException, CredentialManagerException, CryptoEngineException,
      IssuanceOrchestrationException, JAXBException, SAXException, ProofException {

    // Initialise system parameters, issuer parameters, and credential specification
    // (such that they are available from keyManager and credentialManager)
    initSystemParameters(systemParameters, keyManager);
    initIssuerKeyPair(keyPair_cl, credentialManagerIssuer, keyManager);
    initCredentialSpecification(credSpec, keyManager);

    // Initialise the list of credentials and pseudonyms
    List<URI> credentialUris = new ArrayList<URI>();
    List<URI> pseudonymUris = new ArrayList<URI>();

    // Create credential in token
    CredentialFacade credentialFacade =
        initCredential(TestUtils.getResource("credential_cl.xml", Credential.class, this),
            credentialManagerUser);

    List<Pair<URI, BigInteger>> revealedAttributes = new ArrayList<Pair<URI, BigInteger>>();
    revealedAttributes.add(new Pair<URI, BigInteger>(URI.create("FirstName"), BigInteger
        .valueOf(1337L)));
    revealedAttributes
        .add(new Pair<URI, BigInteger>(URI.create("LastName"), BigInteger.valueOf(1L)));

    PresentationTokenDescription ptd =
        initCredentialInToken(null, revealedAttributes, credentialFacade, credentialUris);

    boolean result = presentationProtocol(ptd, null, credentialUris, pseudonymUris);

    assertEquals(true, result);
  }


  @Test(expected = AssertionError.class)
  public void presentation_ClReveal_fail() throws SerializationException, ConfigurationException,
      KeyManagerException, IOException, CredentialManagerException, CryptoEngineException,
      IssuanceOrchestrationException, JAXBException, SAXException, ProofException {

    // Initialise system parameters, issuer parameters, and credential specification
    // (such that they are available from keyManager and credentialManager)
    initSystemParameters(systemParameters, keyManager);
    initIssuerKeyPair(keyPair_cl, credentialManagerIssuer, keyManager);
    initCredentialSpecification(credSpec, keyManager);

    // Initialise the list of credentials and pseudonyms
    List<URI> credentialUris = new ArrayList<URI>();
    List<URI> pseudonymUris = new ArrayList<URI>();

    // Create credential in token
    CredentialFacade credentialFacade =
        initCredential(TestUtils.getResource("credential_cl.xml", Credential.class, this),
            credentialManagerUser);

    // NOTE: This is not the right attribute value!!!
    List<Pair<URI, BigInteger>> revealedAttributes = new ArrayList<Pair<URI, BigInteger>>();
    revealedAttributes.add(new Pair<URI, BigInteger>(URI.create("LastName"), BigInteger
        .valueOf(13L)));

    PresentationTokenDescription ptd =
        initCredentialInToken(null, revealedAttributes, credentialFacade, credentialUris);

    boolean result = presentationProtocol(ptd, null, credentialUris, pseudonymUris);

    assertEquals(true, result);
  }


  @Test
  public void presentation_Cl_message() throws SerializationException, ConfigurationException,
      KeyManagerException, IOException, CredentialManagerException, CryptoEngineException,
      IssuanceOrchestrationException, JAXBException, SAXException, ProofException {

    // Initialise system parameters, issuer parameters, and credential specification
    // (such that they are available from keyManager and credentialManager)
    initSystemParameters(systemParameters, keyManager);
    initIssuerKeyPair(keyPair_cl, credentialManagerIssuer, keyManager);
    initCredentialSpecification(credSpec, keyManager);


    // Initialise the list of credentials and pseudonyms
    List<URI> credentialUris = new ArrayList<URI>();
    List<URI> pseudonymUris = new ArrayList<URI>();

    // Create credential in token
    CredentialFacade credentialFacade =
        initCredential(TestUtils.getResource("credential_cl.xml", Credential.class, this),
            credentialManagerUser);

    PresentationTokenDescription ptd =
        initCredentialInToken(null, null, credentialFacade, credentialUris);

    ptd = initMessage(ptd, "Message to be signed.");

    boolean result = presentationProtocol(ptd, null, credentialUris, pseudonymUris);

    assertEquals(true, result);
  }


  @Test
  public void presentation_Cl_equality() throws SerializationException, ConfigurationException,
      KeyManagerException, IOException, CredentialManagerException, CryptoEngineException,
      IssuanceOrchestrationException, JAXBException, SAXException, ProofException {

    // Initialise system parameters, issuer parameters, and credential specification
    // (such that they are available from keyManager and credentialManager)
    initSystemParameters(systemParameters, keyManager);
    initIssuerKeyPair(keyPair_cl, credentialManagerIssuer, keyManager);
    initCredentialSpecification(credSpec, keyManager);

    // Initialise the list of credentials and pseudonyms
    List<URI> credentialUris = new ArrayList<URI>();
    List<URI> pseudonymUris = new ArrayList<URI>();

    // Create credential in token
    CredentialFacade credentialFacade =
        initCredential(TestUtils.getResource("credential_cl.xml", Credential.class, this),
            credentialManagerUser);

    PresentationTokenDescription ptd =
        initCredentialInToken(null, null, credentialFacade, credentialUris);

    // Add attribute predicate
    AttributePredicate attributePredicate =
        TestUtils.getResource("attributePredicate_equal.xml", AttributePredicate.class, this);
    ptd.getAttributePredicate().add(attributePredicate);

    boolean result = presentationProtocol(ptd, null, credentialUris, pseudonymUris);

    assertEquals(true, result);
  }



  private PresentationTokenDescription initMessage(@Nullable PresentationTokenDescription ptd,
      String messageString) {
    ApplicationData applicationData = new ObjectFactory().createApplicationData();
    applicationData.getContent().add(messageString);
    Message message = new ObjectFactory().createMessage();
    message.setApplicationData(applicationData);

    // Initialise PresentationTokenDescription
    if (ptd == null) {
      ptd = new ObjectFactory().createPresentationTokenDescription();
    }
    ptd.setMessage(message);
    return ptd;
  }

  private PresentationTokenDescription initCredentialInToken(
      @Nullable PresentationTokenDescription ptd,
      @Nullable List<Pair<URI, BigInteger>> revealedAttributes, CredentialFacade credentialFacade,
      List<URI> credentialUris) {

    CredentialInToken cit = new ObjectFactory().createCredentialInToken();

    // Initialise credential in token
    cit.setAlias(URI.create("#credentialName"));
    cit.setCredentialSpecUID(credentialFacade.getCredentialSpecificicationUID());
    cit.setIssuerParametersUID(credentialFacade.getIssuerParametersUID());
    cit.setRevocationInformationUID(credentialFacade.getRevocationInformationUID());
    cit.setSameKeyBindingAs(null);

    if (revealedAttributes == null) {
      revealedAttributes = Collections.<Pair<URI, BigInteger>>emptyList();
    }
    for (Pair<URI, BigInteger> attRevealed : revealedAttributes) {
      AttributeInToken attributeInToken = new ObjectFactory().createAttributeInToken();
      attributeInToken.setAttributeType(attRevealed.first);
      attributeInToken.setAttributeValue(attRevealed.second);
      cit.getDisclosedAttribute().add(attributeInToken);
    }

    // Initialise PresentationTokenDescription
    if (ptd == null) {
      ptd = new ObjectFactory().createPresentationTokenDescription();
    }
    ptd.getCredential().add(cit);
    credentialUris.add(credentialFacade.getCredentialUID());
    return ptd;
  }

  private boolean presentationProtocol(PresentationTokenDescription ptd,
      VerifierParameters verifierParameters, List<URI> credentialUris, List<URI> pseudonymUris)
      throws CryptoEngineException, SerializationException {

    PresentationToken pt =
        cryptoEngineProver.createPresentationToken(USERNAME, ptd, null, credentialUris, pseudonymUris);

    pt.setPresentationTokenDescription(ptd);

    // Serialization
    String xmlPresentationToken =
        JaxbHelperClass.serialize((new ObjectFactory()).createPresentationToken(pt));
    System.out.println(xmlPresentationToken);

    boolean result = cryptoEngineVerifier.verifyToken(pt, verifierParameters);

    return result;
  }
}
