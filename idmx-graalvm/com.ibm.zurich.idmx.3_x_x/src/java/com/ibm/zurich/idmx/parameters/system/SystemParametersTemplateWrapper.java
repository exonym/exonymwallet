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

package com.ibm.zurich.idmx.parameters.system;

import java.net.URI;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBIntrospector;

import com.ibm.zurich.idmix.abc4trust.AbcUriConfigurator;
import com.ibm.zurich.idmx.configuration.ParameterBaseName;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.SerializationException;
import com.ibm.zurich.idmx.jaxb.JaxbHelperClass;
import com.ibm.zurich.idmx.jaxb.ParameterListHelper;
import com.ibm.zurich.idmx.jaxb.ParametersHelper;

import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.SystemParametersTemplate;

/**
 * 
 */
public class SystemParametersTemplateWrapper extends ParametersHelper {

  // System parameter template element names
  public static final String HASH_FUNCTION_NAME = "hashFunction";

  // Delegatee
  private final JAXBElement<SystemParametersTemplate> template;

  public SystemParametersTemplateWrapper() {
    this(new ObjectFactory().createSystemParametersTemplate());
  }

  public SystemParametersTemplateWrapper(final SystemParametersTemplate systemParametersTemplate) {
    this.template = new ObjectFactory().createSystemParametersTemplate(systemParametersTemplate);
    this.parameterListHelper =
        new ParameterListHelper(getSystemParametersTemplate().getParameter());
  }

  @SuppressWarnings("unchecked")
  private SystemParametersTemplateWrapper(final JAXBElement<?> jaxbElement) throws SerializationException {

    final Class<?> delegateeClass = SystemParametersTemplate.class;
    if (delegateeClass.isAssignableFrom(jaxbElement.getDeclaredType())) {
      this.template = (JAXBElement<SystemParametersTemplate>) jaxbElement;
      this.parameterListHelper =
          new ParameterListHelper(getSystemParametersTemplate().getParameter());
    } else {
      throw new SerializationException("Idmx: " + delegateeClass.getSimpleName() + " is malformed.");
    }
  }

  /**
   * Returns the system parameters template (JAXB object).
   * 
   * @return
   */
  public SystemParametersTemplate getSystemParametersTemplate() {
    return (SystemParametersTemplate) JAXBIntrospector.getValue(template);
  }


  @Override
  protected String createParameterUriBasedOnParameterName(final String parameterName) {
    return ParameterBaseName.systemParametersParameterName(parameterName);
  }


  /**
   * @return
   */
  public URI getSystemParametersId() {
    return getSystemParametersTemplate().getSystemParametersId();
  }

  /**
   * @param systemParametersId
   */
  public void setSystemParametersId(final URI systemParametersId) {
    getSystemParametersTemplate().setSystemParametersId(systemParametersId);
  }

  /**
   * @param hashFunctionName
   */
  public void setHashFunction(final String hashFunctionName) {
    final String hashFunctionUri = AbcUriConfigurator.prependBasicUri(hashFunctionName);
    setParameter(HASH_FUNCTION_NAME, hashFunctionUri);
  }

  /**
   * @return
   * @throws ConfigurationException
   */
  public String getHashFunction() throws ConfigurationException {
    final String hashFunctionUri = (String) getParameter(HASH_FUNCTION_NAME);
    return AbcUriConfigurator.removeBasicUri(hashFunctionUri);
  }

  /**
   * Serializes the stored template usign JAXB.
   * 
   * @param filename
   * @throws SerializationException
   */
  public String serialize() throws SerializationException {
    return JaxbHelperClass.serialize(template);
  }

  /**
   * De-serializes a JAXB system parameters template.
   * 
   * @param systemParametersTemplate
   * @return
   * @throws SerializationException
   */
  public static SystemParametersTemplateWrapper deserialize(final String systemParametersTemplate)
      throws SerializationException {
    return new SystemParametersTemplateWrapper(
        JaxbHelperClass.deserialize(systemParametersTemplate));
  }
}
