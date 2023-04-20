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

import org.junit.BeforeClass;
import org.junit.Test;

import com.ibm.zurich.idmx.buildingBlock.systemParameters.EcryptSystemParametersTemplateWrapper;
import com.ibm.zurich.idmx.buildingBlock.systemParameters.EcryptSystemParametersWrapper;
import com.ibm.zurich.idmx.configuration.Configuration;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.SerializationException;
import com.ibm.zurich.idmx.parameters.system.SystemParametersTemplateWrapper;
import com.ibm.zurich.idmx.parameters.system.SystemParametersWrapper;
import com.ibm.zurich.idmx.tests.TestInitialisation;
import com.ibm.zurich.idmx.tests.TestUtils;

import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.SystemParametersTemplate;

/**
 * 
 */
public class TestSystemParameters extends TestInitialisation {

  public static final URI BASE_LOCATION = new File(System.getProperty("user.dir")).toURI().resolve(
      "files/");
  private static final URI BASE_LOCATION_PARAMETERS = BASE_LOCATION.resolve("parameters/");

  private static final String SYSTEM_PARAMETERS_TEMPLATE_BASE_FILENAME = BASE_LOCATION_PARAMETERS
      .resolve("spTemplate_").toString();
  private static final String SYSTEM_PARAMETERS_BASE_FILENAME = BASE_LOCATION_PARAMETERS.resolve(
      "sp_").toString();
  public static final String DEFAULT_SYSTEM_PARAMETERS_FILENAME = BASE_LOCATION_PARAMETERS.resolve(
      SYSTEM_PARAMETERS_BASE_FILENAME + "default.xml").toString();

  @BeforeClass
  public static void cleanUpTemporaryFiles() {
    final URI BASE_LOCATION = new File(System.getProperty("user.dir")).toURI();
    final URI PARAMETER = BASE_LOCATION.resolve("files/parameters/");
    TestUtils.deleteFilesInFolder(new File(PARAMETER), null);
  }

  // /**
  // * Initializes the system parameters such that they are available for the key manager.
  // *
  // * @throws SerializationException
  // * @throws KeyManagerException
  // * @throws IOException
  // */
  // public static void initSystemParameters() throws SerializationException, KeyManagerException,
  // IOException {
  // // Init the system parameters
  // String systemParametes = TestUtils.loadFromFile(DEFAULT_SYSTEM_PARAMETERS_FILENAME);
  // SystemParametersWrapper systemParametersFacade = SystemParametersWrapper
  // .deserialize(systemParametes);
  //
  // // Load the parameters to the key manager
  // keyManager.storeSystemParameters(systemParametersFacade.getSystemParameters());
  // }

  private EcryptSystemParametersTemplateWrapper initSystemParametersTemplate()
      throws ConfigurationException {
    SystemParametersTemplate template = cryptoEngineIssuer.createSystemParametersTemplate();
    EcryptSystemParametersTemplateWrapper spt = new EcryptSystemParametersTemplateWrapper(template);
    spt.setSecurityLevel(Configuration.defaultSecurityLevel());
    return spt;
  }

  @Test
  public void testSystemParametersTemplateCreation() throws SerializationException,
      ConfigurationException, KeyManagerException {

    // Create a template
    SystemParametersTemplateWrapper templateFacade = initSystemParametersTemplate();

    assertTrue(templateFacade != null);
    assertTrue(templateFacade.getSystemParametersTemplate() != null);
  }

  @Test
  public void testSystemParametersTemplateSerialization() throws SerializationException,
      ConfigurationException, IOException {

    String spTemplateFilename = SYSTEM_PARAMETERS_TEMPLATE_BASE_FILENAME + "default.xml";

    SystemParametersTemplateWrapper templateFacade = initSystemParametersTemplate();
    TestUtils.saveToFile(templateFacade.serialize(), spTemplateFilename);
    TestUtils.print(spTemplateFilename);

    assertTrue(templateFacade != null);
    assertTrue(templateFacade.getSystemParametersTemplate() != null);
  }

  @Test
  public void testSystemParametersGeneration() throws SerializationException,
      ConfigurationException, IOException {

    String spFilename = DEFAULT_SYSTEM_PARAMETERS_FILENAME;

    // Create a template
    EcryptSystemParametersTemplateWrapper spt = initSystemParametersTemplate();

    // Only re-create system parameters if they are not already existing
    if (!new File(spFilename).exists()) {
      SystemParameters systemParameters =
          cryptoEngineIssuer.setupSystemParameters(spt.getSystemParametersTemplate());
      SystemParametersWrapper systemParametersFacade =
          new SystemParametersWrapper(systemParameters);
      TestUtils.saveToFile(systemParametersFacade.serialize(), spFilename);
    }
    TestUtils.print(spFilename);

    // verify that the parameters have been set properly
    assertEquals(spt.getSecurityLevel(), Configuration.defaultSecurityLevel());
  }

  @Test
  public void testSystemParametersInitialisation() throws SerializationException,
      ConfigurationException, KeyManagerException, IOException {

    // FIXME This is failing because the file isn't there.
    //  If you comment out the clean up and run it again in isolation it will pass.
    // Strangely it passes using Java8.
    initSystemParameters();

    SystemParametersWrapper systemParametersFacade =
        new SystemParametersWrapper(keyManager.getSystemParameters());

    // Verify that the parameters have been set properly
    assertTrue(systemParametersFacade.getSystemParametersId() != null);
    // assertTrue(systemParametersFacade.getImplementationVersion() != null);
    assertTrue(systemParametersFacade.getHashFunction() != null);
  }

  @Test
  public void testSystemParametersTemplateConfiguration() throws SerializationException,
      ConfigurationException, IOException {

    String spTemplateFilename = SYSTEM_PARAMETERS_TEMPLATE_BASE_FILENAME + "templateChange.xml";
    String spFilename = SYSTEM_PARAMETERS_BASE_FILENAME + "templateChange.xml";

    EcryptSystemParametersTemplateWrapper spt = initSystemParametersTemplate();

    // Change the general configuration parameters
    String hashFunction = "MD5";
    //String version = "1.0.0";
    // Change the idmix-specific configuration parameters
    int securityLevel = 64;
    int statisticalZK = 80;
    int attributeLength = 250;

    // Set/change the default values in the system parameters template
    spt.setHashFunction(hashFunction);
    // spt.setVersion(version);
    spt.setSecurityLevel(securityLevel);
    spt.setStatisticalInd(statisticalZK);
    spt.setAttributeLength(attributeLength);

    // verify that the parameters have been set properly
    assertEquals(spt.getSecurityLevel(), securityLevel);
    assertEquals(spt.getStatisticalZeroKnowledge(), statisticalZK);

    TestUtils.saveToFile(spt.serialize(), spTemplateFilename);
    TestUtils.print(spTemplateFilename);

    // Generate system parameters with the changed template
    SystemParameters systemParameters =
        cryptoEngineIssuer.setupSystemParameters(spt.getSystemParametersTemplate());
    EcryptSystemParametersWrapper sp = new EcryptSystemParametersWrapper(systemParameters);
    TestUtils.saveToFile(sp.serialize(), spFilename);
    TestUtils.print(spFilename);

    // Verify that the generated values are used in the system parameters
    // assertEquals(sp.getImplementationVersion(), version);
    assertEquals(sp.getHashFunction(), hashFunction);
    assertEquals(sp.getSecurityLevel(), securityLevel);
    assertEquals(sp.getStatisticalInd(), statisticalZK);
    assertEquals(sp.getAttributeLength(), attributeLength);
  }
}
