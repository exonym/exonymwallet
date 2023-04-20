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

package com.ibm.zurich.idmx.tests.setup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.xml.sax.SAXException;

import com.ibm.zurich.idmix.abc4trust.facades.IssuerParametersFacade;
import com.ibm.zurich.idmx.buildingBlock.signature.uprove.BrandsSignatureBuildingBlock;
import com.ibm.zurich.idmx.configuration.Configuration;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.SerializationException;
import com.ibm.zurich.idmx.jaxb.FriendlyDescriptionHelper;
import com.ibm.zurich.idmx.keypair.KeyPairWrapper;
import com.ibm.zurich.idmx.keypair.issuer.IssuerKeyPairWrapper;
import com.ibm.zurich.idmx.parameters.issuer.IssuerPublicKeyTemplateWrapper;
import com.ibm.zurich.idmx.parameters.system.SystemParametersWrapper;
import com.ibm.zurich.idmx.tests.TestInitialisation;
import com.ibm.zurich.idmx.tests.TestUtils;

import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.xml.IssuerPublicKeyTemplate;
import eu.abc4trust.xml.KeyPair;

/**
 * 
 */
public class TestIssuerKeyPair extends TestInitialisation {

  private static final URI BASE_LOCATION_ISSUER = TestSystemParameters.BASE_LOCATION
      .resolve("private/");

  private static final String ISSUER_PUBLIC_KEY_TEMPLATE_BASE_FILENAME = BASE_LOCATION_ISSUER
      .resolve("ipTemplate_").toString();
  private static final String ISSUER_PARAMETERS_BASE_FILENAME = BASE_LOCATION_ISSUER.resolve("ip_")
      .toString();
  private static final String ISSUER_KEY_PAIR_BASE_FILENAME = BASE_LOCATION_ISSUER.resolve(
      "keyPair_").toString();
  static final String DEFAULT_ISSUER_KEY_PAIR_FILENAME = ISSUER_KEY_PAIR_BASE_FILENAME + "cl.xml";
  static final String DEFAULT_ISSUER_PARAMETERS_FILENAME = ISSUER_PARAMETERS_BASE_FILENAME
      + "cl.xml";

  // Issuer template configuration values
  static final int NUMBER_OF_ATTRIBUTES = 12;
  static final URI PUBLIC_KEY_PREFIX = URI
      .create("urn:com.ibm.zurich:issuance:idmx:3.0.0:publicKey");
  static final URI REVOCATION_AUTHORITY = URI
      .create("urn:ch.admin:revocation:idmx:3.0.0:publicKey");



  private IssuerPublicKeyTemplateWrapper initDefaultIssuerPublicKeyTemplate()
      throws ConfigurationException, KeyManagerException {

    // Create the template
    IssuerPublicKeyTemplate issuerPublicKeyTemplate =
        cryptoEngineIssuer.createIssuerKeyPairTemplate();
    IssuerPublicKeyTemplateWrapper templateFacade =
        new IssuerPublicKeyTemplateWrapper(issuerPublicKeyTemplate);

    // Configure the template
    templateFacade.setMaximalNumberOfAttributes(NUMBER_OF_ATTRIBUTES);
    templateFacade.setPublicKeyPrefix(PUBLIC_KEY_PREFIX);
    templateFacade.setRevocationAuthority(REVOCATION_AUTHORITY);

    return templateFacade;
  }

  @Test
  public void testIssuerPublicKeyTemplateCreation() throws SerializationException,
      ConfigurationException, KeyManagerException, IOException, JAXBException, SAXException {

    // Initialise system parameters
    SystemParametersWrapper systemParametersFacade = initSystemParameters();

    // Create the template
    IssuerPublicKeyTemplateWrapper templateFacade = initDefaultIssuerPublicKeyTemplate();

    assertTrue(templateFacade.getSystemParametersId().equals(
        systemParametersFacade.getSystemParametersId()));
  }

  @Test
  public void testIssuerPublicKeyTemplateSerialization() throws SerializationException,
      ConfigurationException, KeyManagerException, IOException {

    String ipTemplateFilename = ISSUER_PUBLIC_KEY_TEMPLATE_BASE_FILENAME + "default.xml";

    // Initialise system parameters
    SystemParametersWrapper systemParametersFacade = initSystemParameters();

    // Create the template
    IssuerPublicKeyTemplateWrapper templateFacade = initDefaultIssuerPublicKeyTemplate();

    // Serialize the template
    TestUtils.saveToFile(templateFacade.serialize(), ipTemplateFilename);
    TestUtils.print(ipTemplateFilename);

    assertTrue(templateFacade.getSystemParametersId().equals(
        systemParametersFacade.getSystemParametersId()));
  }

  @Test
  public void testIssuerPublicKeyTemplateDeserialization() throws SerializationException,
      ConfigurationException, KeyManagerException, IOException {

    String ipTemplateFilename = ISSUER_PUBLIC_KEY_TEMPLATE_BASE_FILENAME + "default.xml";

    // Initialise system parameters
    SystemParametersWrapper systemParametersFacade = initSystemParameters();

    // Create and serialize the template
    IssuerPublicKeyTemplateWrapper templateFacade = initDefaultIssuerPublicKeyTemplate();
    TestUtils.saveToFile(templateFacade.serialize(), ipTemplateFilename);
    TestUtils.print(ipTemplateFilename);

    // De-serialize the template
    String issuerPublicKey = TestUtils.loadFromFile(ipTemplateFilename);
    IssuerPublicKeyTemplateWrapper issuerPublicKeyTemplateFacade =
        IssuerPublicKeyTemplateWrapper.deserialize(issuerPublicKey);

    assertTrue(issuerPublicKeyTemplateFacade.getIssuerPublicKeyTemplate() != null);
    assertTrue(issuerPublicKeyTemplateFacade.getSystemParametersId().equals(
        systemParametersFacade.getSystemParametersId()));
    assertTrue(issuerPublicKeyTemplateFacade.getIssuerPublicKeyTemplate().getSystemParametersId()
        .equals(templateFacade.getIssuerPublicKeyTemplate().getSystemParametersId()));
    assertTrue(issuerPublicKeyTemplateFacade.getPublicKeyPrefix().equals(
        templateFacade.getPublicKeyPrefix()));
    assertTrue(issuerPublicKeyTemplateFacade.getRevocationAuthority().equals(
        templateFacade.getRevocationAuthority()));
    assertTrue(issuerPublicKeyTemplateFacade.getMaximalNumberOfAttributes() == templateFacade
        .getMaximalNumberOfAttributes());
  }

  @Test(timeout=Configuration.TEST_TIMEOUT)
  public void testIssuerKeyPairGeneration_Cl() throws SerializationException,
      ConfigurationException, KeyManagerException, CredentialManagerException, IOException {

    String keyPairFilename = DEFAULT_ISSUER_KEY_PAIR_FILENAME;

    // Initialise system parameters
    SystemParametersWrapper systemParametersFacade = initSystemParameters();

    // Create issuer public key template
    IssuerPublicKeyTemplateWrapper templateFacade = initDefaultIssuerPublicKeyTemplate();

    // Setup issuer key pair and serialize it
    KeyPairWrapper issuerKeyPairFacade = null;
    if (!new File(keyPairFilename).exists()) {
      KeyPair issuerKeyPair =
          cryptoEngineIssuer.setupIssuerKeyPair(systemParametersFacade.getSystemParameters(),
              templateFacade.getIssuerPublicKeyTemplate());
      issuerKeyPairFacade = new IssuerKeyPairWrapper(issuerKeyPair);
      TestUtils.saveToFile(issuerKeyPairFacade.serialize(), keyPairFilename);
    } else {
      String issuerKeyPair = TestUtils.loadFromFile(keyPairFilename);
      issuerKeyPairFacade = IssuerKeyPairWrapper.deserialize(issuerKeyPair);
    }
    TestUtils.print(keyPairFilename);

    // verify that the parameters have been set properly
    assertTrue(templateFacade.getMaximalNumberOfAttributes() == (NUMBER_OF_ATTRIBUTES));
    assertTrue(templateFacade.getPublicKeyPrefix().equals(PUBLIC_KEY_PREFIX));
    assertTrue(templateFacade.getRevocationAuthority().equals(REVOCATION_AUTHORITY));

    assertTrue(templateFacade.getSystemParametersId().equals(
        systemParametersFacade.getSystemParametersId()));
    assertTrue(issuerKeyPairFacade != null);
    assertTrue(issuerKeyPairFacade.getPublicKeyWrapper().getSystemParametersId()
        .equals(systemParametersFacade.getSystemParametersId()));
    assertTrue(issuerKeyPairFacade.getPublicKeyWrapper().getPublicKeyId().toString()
        .startsWith(templateFacade.getPublicKeyPrefix().toString()));
  }

  @Test(timeout=Configuration.TEST_TIMEOUT)
  public void testIssuerKeyPairGeneration_noRevocation() throws SerializationException,
      ConfigurationException, KeyManagerException, CredentialManagerException, IOException {

    String ipTemplateFilename = ISSUER_PUBLIC_KEY_TEMPLATE_BASE_FILENAME + "noRevocation.xml";
    String keyPairFilename = ISSUER_KEY_PAIR_BASE_FILENAME + "noRevocation.xml";
    String ipFilename = ISSUER_PARAMETERS_BASE_FILENAME + "noRevocation.xml";

    // Initialise system parameters
    SystemParametersWrapper systemParametersFacade = initSystemParameters();

    IssuerPublicKeyTemplateWrapper templateFacade = initDefaultIssuerPublicKeyTemplate();
    TestUtils.saveToFile(templateFacade.serialize(), ipTemplateFilename);
    TestUtils.print(ipTemplateFilename);

    // Configure the template (no revocation handle)
    templateFacade.setMaximalNumberOfAttributes(NUMBER_OF_ATTRIBUTES);
    templateFacade.setPublicKeyPrefix(PUBLIC_KEY_PREFIX);

    // Generate issuer key pair based on the template
    KeyPair issuerKeyPair =
        cryptoEngineIssuer.setupIssuerKeyPair(systemParametersFacade.getSystemParameters(),
            templateFacade.getIssuerPublicKeyTemplate());
    KeyPairWrapper issuerKeyPairFacade = new IssuerKeyPairWrapper(issuerKeyPair);
    TestUtils.saveToFile(issuerKeyPairFacade.serialize(), keyPairFilename);
    TestUtils.print(keyPairFilename);

    // Create ABC issuer parameters based on the issuer public key
    IssuerParametersFacade issuerParametersFacade =
        IssuerParametersFacade.initIssuerParameters(
            issuerKeyPairFacade.getKeyPair().getPublicKey(),
            systemParametersFacade.getSystemParameters());
    TestUtils.saveToFile(issuerParametersFacade.serialize(), ipFilename);
    TestUtils.print(ipFilename);

    // Verify that the parameters have been set properly
    assertTrue(templateFacade.getPublicKeyPrefix().equals(PUBLIC_KEY_PREFIX));
    assertTrue(issuerParametersFacade.getIssuerParametersId().toString()
        .startsWith(PUBLIC_KEY_PREFIX.toString()));
    assertTrue(issuerKeyPairFacade.getPublicKeyWrapper().getPublicKeyId().toString()
        .startsWith(PUBLIC_KEY_PREFIX.toString()));
    assertTrue(templateFacade.getMaximalNumberOfAttributes() == (NUMBER_OF_ATTRIBUTES));

    assertTrue(issuerKeyPairFacade.getPublicKeyWrapper().getSystemParametersId()
        .equals(systemParametersFacade.getSystemParametersId()));
    assertTrue(issuerParametersFacade.getSystemParametersId().equals(
        systemParametersFacade.getSystemParametersId()));

    assertTrue(issuerKeyPairFacade.getPublicKeyWrapper().getPublicKeyId()
        .equals(issuerParametersFacade.getIssuerParametersId()));
  }

  @Test(timeout=Configuration.TEST_TIMEOUT)
  public void testIssuerKeyPairGeneration_withFriendlyDescriptions() throws SerializationException,
      ConfigurationException, KeyManagerException, CredentialManagerException, IOException {

    String ipTemplateFilename =
        ISSUER_PUBLIC_KEY_TEMPLATE_BASE_FILENAME + "withFriendlyDescription.xml";
    String keyPairFilename = ISSUER_KEY_PAIR_BASE_FILENAME + "withFriendlyDescription.xml";
    String ipFilename = ISSUER_PARAMETERS_BASE_FILENAME + "withFriendlyDescription.xml";

    // Initialise system parameters
    SystemParametersWrapper systemParametersFacade = initSystemParameters();

    IssuerPublicKeyTemplateWrapper templateFacade = initDefaultIssuerPublicKeyTemplate();
    TestUtils.saveToFile(templateFacade.serialize(), ipTemplateFilename);
    TestUtils.print(ipTemplateFilename);

    // Configure the template
    templateFacade.setMaximalNumberOfAttributes(NUMBER_OF_ATTRIBUTES);
    templateFacade.setPublicKeyPrefix(PUBLIC_KEY_PREFIX);
    templateFacade.setRevocationAuthority(REVOCATION_AUTHORITY);
    templateFacade.addFriendlyDescription(FriendlyDescriptionHelper.getFriendlyDescription("en",
        "This is a test issuer."));
    templateFacade.addFriendlyDescription(FriendlyDescriptionHelper.getFriendlyDescription("de",
        "Dies ist ein Test Issuer."));

    // Generate issuer key pair based on the template
    KeyPair issuerKeyPair =
        cryptoEngineIssuer.setupIssuerKeyPair(systemParametersFacade.getSystemParameters(),
            templateFacade.getIssuerPublicKeyTemplate());
    KeyPairWrapper issuerKeyPairFacade = new IssuerKeyPairWrapper(issuerKeyPair);
    TestUtils.saveToFile(issuerKeyPairFacade.serialize(), keyPairFilename);
    TestUtils.print(keyPairFilename);

    // Create ABC issuer parameters based on the issuer public key
    IssuerParametersFacade issuerParametersFacade =
        IssuerParametersFacade.initIssuerParameters(
            issuerKeyPairFacade.getKeyPair().getPublicKey(),
            systemParametersFacade.getSystemParameters());
    TestUtils.saveToFile(issuerParametersFacade.serialize(), ipFilename);
    TestUtils.print(ipFilename);

    // Verify that the parameters have been set properly
    assertTrue(templateFacade.getPublicKeyPrefix().equals(PUBLIC_KEY_PREFIX));
    assertTrue(issuerParametersFacade.getIssuerParametersId().toString()
        .startsWith(PUBLIC_KEY_PREFIX.toString()));
    assertTrue(issuerKeyPairFacade.getPublicKeyWrapper().getPublicKeyId().toString()
        .startsWith(PUBLIC_KEY_PREFIX.toString()));
    assertTrue(templateFacade.getMaximalNumberOfAttributes() == (NUMBER_OF_ATTRIBUTES));
    assertTrue(templateFacade.getRevocationAuthority().equals(REVOCATION_AUTHORITY));
    assertTrue(issuerParametersFacade.getRevocationAuthorityId().equals(REVOCATION_AUTHORITY));

    assertTrue(issuerKeyPairFacade.getPublicKeyWrapper().getSystemParametersId()
        .equals(systemParametersFacade.getSystemParametersId()));
    assertTrue(issuerParametersFacade.getSystemParametersId().equals(
        systemParametersFacade.getSystemParametersId()));

    assertTrue(issuerKeyPairFacade.getPublicKeyWrapper().getPublicKeyId()
        .equals(issuerParametersFacade.getIssuerParametersId()));
  }

  static final String BRANDS_ISSUER_KEY_PAIR_FILENAME = ISSUER_KEY_PAIR_BASE_FILENAME
      + "brands.xml";

  @Test
  public void testIssuerKeyPairGeneration_Uprove() throws SerializationException,
      ConfigurationException, KeyManagerException, CredentialManagerException, IOException {

    String keyPairFilename = BRANDS_ISSUER_KEY_PAIR_FILENAME;

    // Initialise system parameters
    SystemParametersWrapper systemParametersFacade = initSystemParameters();

    // Create issuer public key template
    IssuerPublicKeyTemplateWrapper templateFacade = initDefaultIssuerPublicKeyTemplate();
    URI brandsBuildingBlockId =
        new BrandsSignatureBuildingBlock(null, null, null, null, null, null, null)
            .getBuildingBlockId();
    templateFacade.setTechnology(brandsBuildingBlockId);

    // Setup issuer key pair and serialize it
    KeyPair issuerKeyPair =
        cryptoEngineIssuer.setupIssuerKeyPair(systemParametersFacade.getSystemParameters(),
            templateFacade.getIssuerPublicKeyTemplate());
    KeyPairWrapper issuerKeyPairFacade = new IssuerKeyPairWrapper(issuerKeyPair);
    TestUtils.saveToFile(issuerKeyPairFacade.serialize(), keyPairFilename);
    TestUtils.print(keyPairFilename);

    // verify that the parameters have been set properly
    assertTrue(templateFacade.getMaximalNumberOfAttributes() == (NUMBER_OF_ATTRIBUTES));
    assertTrue(templateFacade.getPublicKeyPrefix().equals(PUBLIC_KEY_PREFIX));
    assertTrue(templateFacade.getRevocationAuthority().equals(REVOCATION_AUTHORITY));

    assertTrue(templateFacade.getSystemParametersId().equals(
        systemParametersFacade.getSystemParametersId()));
    assertTrue(issuerKeyPairFacade != null);
    assertTrue(issuerKeyPairFacade.getPublicKeyWrapper().getSystemParametersId()
        .equals(systemParametersFacade.getSystemParametersId()));
    assertTrue(issuerKeyPairFacade.getPublicKeyWrapper().getPublicKeyId().toString()
        .startsWith(templateFacade.getPublicKeyPrefix().toString()));
    assertEquals(issuerKeyPairFacade.getPublicKeyWrapper().getPublicKeyTechnology(),
        brandsBuildingBlockId);
  }

}
