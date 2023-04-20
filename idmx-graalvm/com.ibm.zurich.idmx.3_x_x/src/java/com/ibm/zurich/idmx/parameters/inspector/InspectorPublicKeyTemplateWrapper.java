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
package com.ibm.zurich.idmx.parameters.inspector;

import java.net.URI;
//import java.util.List;

import javax.xml.bind.JAXBElement;

import com.ibm.zurich.idmx.configuration.ParameterBaseName;
import com.ibm.zurich.idmx.exception.ConfigurationException;
//import com.ibm.zurich.idmx.exception.SerializationException;
import com.ibm.zurich.idmx.jaxb.ParameterListHelper;
import com.ibm.zurich.idmx.jaxb.ParametersHelper;

//import eu.abc4trust.xml.FriendlyDescription;
import eu.abc4trust.xml.InspectorPublicKeyTemplate;
//import eu.abc4trust.xml.IssuerPublicKeyTemplate;
import eu.abc4trust.xml.ObjectFactory;

public class InspectorPublicKeyTemplateWrapper extends ParametersHelper {

  private static final String PUBLIC_KEY_PREFIX_NAME = "inspectorPublicKeyPrefix";
  // Delegatee
  private final JAXBElement<InspectorPublicKeyTemplate> template;

  public InspectorPublicKeyTemplateWrapper() {
    this(new ObjectFactory().createInspectorPublicKeyTemplate());
  }

  public InspectorPublicKeyTemplateWrapper(final InspectorPublicKeyTemplate inspectorPublicKeyTemplate) {
    this.template =
        new ObjectFactory().createInspectorPublicKeyTemplate(inspectorPublicKeyTemplate);
    this.parameterListHelper =
        new ParameterListHelper(getInspectorPublicKeyTemplate().getParameter());
  }

  public InspectorPublicKeyTemplate getInspectorPublicKeyTemplate() {
    return template.getValue();
  }

//  @SuppressWarnings("unchecked")
//  private InspectorPublicKeyTemplateWrapper(final JAXBElement<?> jaxbElement)
//      throws SerializationException {
//
//    final Class<?> delegateeClass = IssuerPublicKeyTemplate.class;
//    if (delegateeClass.isAssignableFrom(jaxbElement.getDeclaredType())) {
//      this.template = (JAXBElement<InspectorPublicKeyTemplate>) jaxbElement;
//      this.parameterListHelper =
//          new ParameterListHelper(getInspectorPublicKeyTemplate().getParameter());
//    } else {
//      throw new SerializationException("Idmx: " + delegateeClass.getSimpleName() + " is malformed."); // TODO
//    }
//  }

  @Override
  protected String createParameterUriBasedOnParameterName(final String parameterName) {
    return ParameterBaseName.inspectorPublicKeyParameterName(parameterName);
  }

  public URI getSystemParametersId() {
    return getInspectorPublicKeyTemplate().getSystemParametersId();
  }

  /**
   * @param systemParametersId
   */
  public void setSystemParametersId(final URI systemParametersId) {
    getInspectorPublicKeyTemplate().setSystemParametersId(systemParametersId);
  }

  /**
   * 
   */
  public URI getTechnology() {
    return getInspectorPublicKeyTemplate().getTechnology();
  }

  /**
   * @param technology
   */
  public void setTechnology(final URI technology) {
    getInspectorPublicKeyTemplate().setTechnology(technology);
  }

  /**
   * @throws ConfigurationException
   * 
   */
  public URI getPublicKeyPrefix() throws ConfigurationException {
    return (URI) getParameter(PUBLIC_KEY_PREFIX_NAME);
  }

  /**
   * @param publicKeyPrefix
   */
  public void setPublicKeyPrefix(final URI publicKeyPrefix) {
    setParameter(PUBLIC_KEY_PREFIX_NAME, publicKeyPrefix);
  }
}
