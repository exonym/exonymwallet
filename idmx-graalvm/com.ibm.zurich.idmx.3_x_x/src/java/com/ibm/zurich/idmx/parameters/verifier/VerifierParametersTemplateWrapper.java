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

package com.ibm.zurich.idmx.parameters.verifier;

import java.net.URI;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBIntrospector;

import com.ibm.zurich.idmx.configuration.ParameterBaseName;
import com.ibm.zurich.idmx.exception.SerializationException;
import com.ibm.zurich.idmx.jaxb.JaxbHelperClass;
import com.ibm.zurich.idmx.jaxb.ParameterListHelper;
import com.ibm.zurich.idmx.jaxb.ParametersHelper;

import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.VerifierParametersTemplate;

/**
 * 
 */
public class VerifierParametersTemplateWrapper extends ParametersHelper {

  // Verifier parameter template element names

  // Delegatee
  private final JAXBElement<VerifierParametersTemplate> template;

  public VerifierParametersTemplateWrapper() {
    this(new ObjectFactory().createVerifierParametersTemplate());
  }

  public VerifierParametersTemplateWrapper(final VerifierParametersTemplate verifierParametersTemplate) {
    this.template =
        new ObjectFactory().createVerifierParametersTemplate(verifierParametersTemplate);
    this.parameterListHelper =
        new ParameterListHelper(getVerifierParametersTemplate().getParameter());
  }

  @SuppressWarnings("unchecked")
  private VerifierParametersTemplateWrapper(JAXBElement<?> jaxbElement)
      throws SerializationException {

    final Class<?> delegateeClass = VerifierParametersTemplate.class;
    if (delegateeClass.isAssignableFrom(jaxbElement.getDeclaredType())) {
      this.template = (JAXBElement<VerifierParametersTemplate>) jaxbElement;
      this.parameterListHelper =
          new ParameterListHelper(getVerifierParametersTemplate().getParameter());
    } else {
      throw new SerializationException("Idmx: " + delegateeClass.getSimpleName() + " is malformed.");
    }
  }

  /**
   * Returns the verifier parameters template (JAXB object).
   * 
   * @return
   */
  public VerifierParametersTemplate getVerifierParametersTemplate() {
    return (VerifierParametersTemplate) JAXBIntrospector.getValue(template);
  }


  @Override
  protected String createParameterUriBasedOnParameterName(final String parameterName) {
    return ParameterBaseName.systemParametersParameterName(parameterName);
  }


  /**
   * @return
   */
  public URI getSystemParametersId() {
    return getVerifierParametersTemplate().getSystemParametersId();
  }

  /**
   * @param systemParametersId
   */
  public void setSystemParametersId(final URI systemParametersId) {
    getVerifierParametersTemplate().setSystemParametersId(systemParametersId);
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
   * De-serializes a JAXB verifier parameters template.
   * 
   * @throws SerializationException
   */
  public static VerifierParametersTemplateWrapper deserialize(final String verifierParametersTemplate)
      throws SerializationException {
    return new VerifierParametersTemplateWrapper(
        JaxbHelperClass.deserialize(verifierParametersTemplate));
  }
}
