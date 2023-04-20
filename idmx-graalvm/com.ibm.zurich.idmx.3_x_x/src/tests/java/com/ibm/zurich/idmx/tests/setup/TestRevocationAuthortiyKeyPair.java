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

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.xml.sax.SAXException;

import com.ibm.zurich.idmx.buildingBlock.systemParameters.EcryptSystemParametersGenerator;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.SerializationException;
import com.ibm.zurich.idmx.keypair.KeyPairWrapper;
import com.ibm.zurich.idmx.keypair.ra.RevocationAuthorityKeyPairWrapper;
import com.ibm.zurich.idmx.parameters.ra.RevocationAuthorityPublicKeyTemplateWrapper;
import com.ibm.zurich.idmx.parameters.system.SystemParametersWrapper;
import com.ibm.zurich.idmx.tests.TestInitialisation;
import com.ibm.zurich.idmx.tests.TestUtils;

import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.xml.KeyPair;
import eu.abc4trust.xml.RevocationAuthorityPublicKeyTemplate;
import eu.abc4trust.xml.SystemParameters;

/**
 * 
 */
public class TestRevocationAuthortiyKeyPair extends TestInitialisation {

  private static final URI BASE_LOCATION_REVOCATION_AUTHORTIY = TestSystemParameters.BASE_LOCATION
      .resolve("private/");

  private static final String REVOCATION_AUTHORTIY_PUBLIC_KEY_TEMPLATE_BASE_FILENAME =
      BASE_LOCATION_REVOCATION_AUTHORTIY.resolve("raTemplate_").toString();
  private static final String REVOCATION_AUTHORTIY_KEY_PAIR_BASE_FILENAME =
      BASE_LOCATION_REVOCATION_AUTHORTIY.resolve("ra_").toString();
  private static final String DEFAULT_REVOCATION_AUTHORTIY_KEY_PAIR_FILENAME =
      REVOCATION_AUTHORTIY_KEY_PAIR_BASE_FILENAME + "cl.xml";

  // Revocation authority template configuration values
  static final URI REVOCATION_AUTHORITY_PUBLIC_KEY_PREFIX = URI
      .create("urn:ch.admin:revocation:idmx:3.0.0:publicKey");
  static final int MODULUS_LENGTH = 512;
  static final URI NON_REVOCATION_EVIDENCE_REFERENCE = URI
      .create("non:revocation:evidence:reference");
  static final URI NON_REVOCATION_EVIDENCE_UPDATE_REFERENCE = URI
      .create("non:revocation:evidence:update:reference");
  static final URI REVOCATION_INFORMATION_REFERENCE = URI
      .create("revocation:information:reference");


  private RevocationAuthorityPublicKeyTemplateWrapper initDefaultRevocationAuthorityPublicKeyTemplate()
      throws ConfigurationException, KeyManagerException {

    // Create the template
    RevocationAuthorityPublicKeyTemplate rapkTemplate =
        cryptoEngineRevocationAuthority.createRevocationAuthorityPublicKeyTemplate();
    RevocationAuthorityPublicKeyTemplateWrapper templateWrapper =
        new RevocationAuthorityPublicKeyTemplateWrapper(rapkTemplate);

    // Configure the template
    templateWrapper.setModulusLength(MODULUS_LENGTH);
    templateWrapper.setPublicKeyPrefix(REVOCATION_AUTHORITY_PUBLIC_KEY_PREFIX);
    templateWrapper.setNonRevocationEvidenceReference(NON_REVOCATION_EVIDENCE_REFERENCE);
    templateWrapper
        .setNonRevocationEvidenceUpdateReference(NON_REVOCATION_EVIDENCE_UPDATE_REFERENCE);
    templateWrapper.setRevocationInformationReference(REVOCATION_INFORMATION_REFERENCE);

    return templateWrapper;
  }

  @Test
  public void testRevocationAuthorityPublicKeyTemplateCreation() throws SerializationException,
      ConfigurationException, KeyManagerException, IOException, JAXBException, SAXException {

    // Initialise system parameters
    SystemParametersWrapper spWrapper = initSystemParameters();

    // Create the template
    RevocationAuthorityPublicKeyTemplateWrapper templateWrapper =
        initDefaultRevocationAuthorityPublicKeyTemplate();

    // TODO Use an independent RSA modulus length as in the system parameters
    int rsaModulusBitlength =
        (Integer) spWrapper.getParameter(EcryptSystemParametersGenerator.RSA_MODULUS_LENGTH_NAME);
    templateWrapper.setModulusLength(rsaModulusBitlength);

    assertTrue(templateWrapper.getSystemParametersId().equals(spWrapper.getSystemParametersId()));
    assertTrue(templateWrapper.getModulusLength() == rsaModulusBitlength);
    assertTrue(templateWrapper.getNonRevocationEvidenceReference().equals(NON_REVOCATION_EVIDENCE_REFERENCE));
    assertTrue(templateWrapper.getNonRevocationEvidenceUpdateReference()
        .equals(NON_REVOCATION_EVIDENCE_UPDATE_REFERENCE));
  }

  @Test
  public void testRevocationAuthorityPublicKeyTemplateSerialization()
      throws SerializationException, ConfigurationException, KeyManagerException, IOException {

    String ipTemplateFilename =
        REVOCATION_AUTHORTIY_PUBLIC_KEY_TEMPLATE_BASE_FILENAME + "default.xml";

    // Initialise system parameters
    SystemParametersWrapper systemParametersFacade = initSystemParameters();

    // Create the template
    RevocationAuthorityPublicKeyTemplateWrapper templateWrapper =
        initDefaultRevocationAuthorityPublicKeyTemplate();

    // Serialize the template
    TestUtils.saveToFile(templateWrapper.serialize(), ipTemplateFilename);
    TestUtils.print(ipTemplateFilename);

    assertTrue(templateWrapper.getSystemParametersId().equals(
        systemParametersFacade.getSystemParametersId()));
  }

  @Test
  public void testRevocationAuthorityPublicKeyTemplateDeserialization()
      throws SerializationException, ConfigurationException, KeyManagerException, IOException {

    String ipTemplateFilename =
        REVOCATION_AUTHORTIY_PUBLIC_KEY_TEMPLATE_BASE_FILENAME + "default.xml";

    // Initialise system parameters
    SystemParametersWrapper systemParametersFacade = initSystemParameters();

    // Create and serialize the template
    RevocationAuthorityPublicKeyTemplateWrapper templateWrapper =
        initDefaultRevocationAuthorityPublicKeyTemplate();
    TestUtils.saveToFile(templateWrapper.serialize(), ipTemplateFilename);
    TestUtils.print(ipTemplateFilename);

    // De-serialize the template
    String rapk = TestUtils.loadFromFile(ipTemplateFilename);
    RevocationAuthorityPublicKeyTemplateWrapper rapkWrapper =
        RevocationAuthorityPublicKeyTemplateWrapper.deserialize(rapk);

    assertTrue(rapkWrapper.getPublicKeyPrefix() != null);
    assertTrue(rapkWrapper.getSystemParametersId().equals(
        systemParametersFacade.getSystemParametersId()));
    assertTrue(rapkWrapper.getPublicKeyPrefix().equals(templateWrapper.getPublicKeyPrefix()));
  }

  @Test
  public void testRevocationAuthorityKeyPairGeneration_Cl() throws SerializationException,
      ConfigurationException, KeyManagerException, CredentialManagerException, IOException {

    String keyPairFilename = DEFAULT_REVOCATION_AUTHORTIY_KEY_PAIR_FILENAME;

    // Initialise system parameters
    SystemParametersWrapper spWrapper = initSystemParameters();

    // Create revocation authority public key template
    RevocationAuthorityPublicKeyTemplateWrapper templateWrapper =
        initDefaultRevocationAuthorityPublicKeyTemplate();

    // Setup revocation authority key pair and serialize it
    KeyPairWrapper raKeyPairWrapper = null;
    if (!new File(keyPairFilename).exists()) {
      KeyPair raKeyPair =
          cryptoEngineRevocationAuthority.setupRevocationAuthorityKeyPair(
              spWrapper.getSystemParameters(),
              templateWrapper.getRevocationAuthorityPublicKeyTemplate());
      raKeyPairWrapper = new RevocationAuthorityKeyPairWrapper(raKeyPair);
      TestUtils.saveToFile(raKeyPairWrapper.serialize(), keyPairFilename);
    } else {
      String raKeyPair = TestUtils.loadFromFile(keyPairFilename);
      raKeyPairWrapper = RevocationAuthorityKeyPairWrapper.deserialize(raKeyPair);
    }
    TestUtils.print(keyPairFilename);

    // verify that the parameters have been set properly
    assertTrue(templateWrapper.getPublicKeyPrefix().equals(REVOCATION_AUTHORITY_PUBLIC_KEY_PREFIX));
    assertTrue(templateWrapper.getSystemParametersId().equals(spWrapper.getSystemParametersId()));
    assertTrue(raKeyPairWrapper != null);
    assertTrue(raKeyPairWrapper.getPublicKeyWrapper().getSystemParametersId()
        .equals(spWrapper.getSystemParametersId()));
    assertTrue(raKeyPairWrapper.getPublicKeyWrapper().getPublicKeyId().toString()
        .startsWith(templateWrapper.getPublicKeyPrefix().toString()));
  }

  @Test
  public void testABCERevocationAuthorityKeyPairGeneration_Cl() throws SerializationException,
      ConfigurationException, KeyManagerException, CredentialManagerException, IOException {

    String keyPairFilename = REVOCATION_AUTHORTIY_KEY_PAIR_BASE_FILENAME + "abce.xml";

    // Initialise system parameters
    SystemParameters systemParameters =
        TestUtils.getResource("sp.xml", SystemParameters.class, this);
    SystemParametersWrapper spWrapper = new SystemParametersWrapper(systemParameters);
    keyManager.storeSystemParameters(spWrapper.getSystemParameters());


    // Create revocation authority public key template
    RevocationAuthorityPublicKeyTemplateWrapper templateWrapper =
        new RevocationAuthorityPublicKeyTemplateWrapper(
            cryptoEngineRevocationAuthority.createRevocationAuthorityPublicKeyTemplate());

    templateWrapper.setModulusLength((Integer) spWrapper
        .getParameter(EcryptSystemParametersGenerator.RSA_MODULUS_LENGTH_NAME));
    templateWrapper.setPublicKeyPrefix(URI.create("urn:patras:revocation:pk"));
    templateWrapper.setNonRevocationEvidenceReference(URI
        .create("non:revocation:evidence:reference"));
    templateWrapper.setNonRevocationEvidenceUpdateReference(URI
        .create("non:revocation:evidence:update:reference"));
    templateWrapper.setRevocationInformationReference(URI
        .create("revocation:information:reference"));


    // Setup revocation authority key pair and serialize it
    KeyPairWrapper raKeyPairWrapper = null;
    if (!new File(keyPairFilename).exists()) {
      KeyPair raKeyPair =
          cryptoEngineRevocationAuthority.setupRevocationAuthorityKeyPair(
              spWrapper.getSystemParameters(),
              templateWrapper.getRevocationAuthorityPublicKeyTemplate());
      raKeyPairWrapper = new RevocationAuthorityKeyPairWrapper(raKeyPair);
      TestUtils.saveToFile(raKeyPairWrapper.serialize(), keyPairFilename);
    } else {
      String raKeyPair = TestUtils.loadFromFile(keyPairFilename);
      raKeyPairWrapper = RevocationAuthorityKeyPairWrapper.deserialize(raKeyPair);
    }
    TestUtils.print(keyPairFilename);

    // verify that the parameters have been set properly
    assertTrue(templateWrapper.getPublicKeyPrefix().equals(URI.create("urn:patras:revocation:pk")));
    assertTrue(templateWrapper.getSystemParametersId().equals(spWrapper.getSystemParametersId()));
    assertTrue(raKeyPairWrapper != null);
    assertTrue(raKeyPairWrapper.getPublicKeyWrapper().getSystemParametersId()
        .equals(spWrapper.getSystemParametersId()));
    assertTrue(raKeyPairWrapper.getPublicKeyWrapper().getPublicKeyId().toString()
        .startsWith(templateWrapper.getPublicKeyPrefix().toString()));
  }
}
