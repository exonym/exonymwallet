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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.SerializationException;
import com.ibm.zurich.idmx.parameters.system.SystemParametersTemplateWrapper;
import com.ibm.zurich.idmx.parameters.system.SystemParametersWrapper;
import com.ibm.zurich.idmx.tests.TestInitialisation;
import com.ibm.zurich.idmx.tests.TestUtils;

/**
 * 
 */
public class TestSystemParametersDeserialization extends TestInitialisation {

  public static final URI BASE_LOCATION = new File(System.getProperty("user.dir")).toURI().resolve(
      "files/");
  private static final URI BASE_LOCATION_PARAMETERS = BASE_LOCATION.resolve("parameters/");

  private static final String SYSTEM_PARAMETERS_TEMPLATE_BASE_FILENAME = BASE_LOCATION_PARAMETERS
      .resolve("spTemplate_").toString();
  private static final String SYSTEM_PARAMETERS_BASE_FILENAME = BASE_LOCATION_PARAMETERS.resolve(
      "sp_").toString();
  private static final String DEFAULT_SYSTEM_PARAMETERS_FILENAME = BASE_LOCATION_PARAMETERS
      .resolve(SYSTEM_PARAMETERS_BASE_FILENAME + "default.xml").toString();

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void testSystemParametersTemplateDeserialization() throws SerializationException,
      ConfigurationException, IOException {

    String spTemplateFilename = SYSTEM_PARAMETERS_TEMPLATE_BASE_FILENAME + "default.xml";

    // De-serialise the template
    SystemParametersTemplateWrapper templateFacade = null;
    String systemParametersTemplate = TestUtils.loadFromFile(spTemplateFilename);
    templateFacade = SystemParametersTemplateWrapper.deserialize(systemParametersTemplate);

    assertTrue(templateFacade != null);
    assertTrue(templateFacade.getSystemParametersTemplate() != null);
  }

  @Test
  public void testExceptionWhenDeserializingSystemParametersAsSystemParameterTemplate()
      throws SerializationException, IOException {
    thrown.expect(SerializationException.class);
    thrown.expectMessage("Idmx: SystemParametersTemplate is malformed.");

    String systemParametersTemplate = TestUtils.loadFromFile(DEFAULT_SYSTEM_PARAMETERS_FILENAME);
    SystemParametersTemplateWrapper.deserialize(systemParametersTemplate);
  }

  @Test
  public void testExceptionWhenDeserializingSystemParametersTemplateAsSystemParameter()
      throws SerializationException, IOException {
    thrown.expect(SerializationException.class);
    thrown.expectMessage("Idmx: Class SystemParameters is malformed.");

    String systemParameters =
        TestUtils.loadFromFile(SYSTEM_PARAMETERS_TEMPLATE_BASE_FILENAME + "default.xml");
    SystemParametersWrapper.deserialize(systemParameters);
  }

  @Test
  public void testSystemParametersDeserialization() throws SerializationException,
      ConfigurationException, IOException {

    String spFilename = DEFAULT_SYSTEM_PARAMETERS_FILENAME;

    // De-serialise the system parameters
    String systemParameters = TestUtils.loadFromFile(spFilename);
    SystemParametersWrapper systemParametersFacade =
        SystemParametersWrapper.deserialize(systemParameters);
    TestUtils.print(spFilename);

    // verify that the parameters have been set properly
    assertTrue(systemParametersFacade.getSystemParametersId() != null);
    // assertTrue(systemParametersFacade.getImplementationVersion() != null);
    assertTrue(systemParametersFacade.getHashFunction() != null);
  }

}
