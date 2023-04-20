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

package com.ibm.zurich.idmx.parameters.issuer;

import java.net.URI;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBIntrospector;

import com.ibm.zurich.idmx.configuration.ParameterBaseName;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.SerializationException;
import com.ibm.zurich.idmx.jaxb.JaxbHelperClass;
import com.ibm.zurich.idmx.jaxb.ParameterListHelper;
import com.ibm.zurich.idmx.jaxb.ParametersHelper;

import eu.abc4trust.xml.FriendlyDescription;
import eu.abc4trust.xml.IssuerPublicKeyTemplate;
import eu.abc4trust.xml.ObjectFactory;

/**
 * 
 */
public class IssuerPublicKeyTemplateWrapper extends ParametersHelper {

  // Template element names
  private static final String PUBLIC_KEY_PREFIX_NAME = "publicKeyPrefix";
  private static final String MAX_ATTRIBUTES_NAME = "maximalNumberOfAttributes";
  private static final String REVOCATION_AUTHORITY_NAME = "revocationAuthority";

  // Delegatee
  private final JAXBElement<IssuerPublicKeyTemplate> template;

  public IssuerPublicKeyTemplateWrapper() {
    this(new ObjectFactory().createIssuerPublicKeyTemplate());
  }

  public IssuerPublicKeyTemplateWrapper(final IssuerPublicKeyTemplate issuerPublicKeyTemplate) {
    this.template = new ObjectFactory().createIssuerPublicKeyTemplate(issuerPublicKeyTemplate);
    this.parameterListHelper = new ParameterListHelper(getIssuerPublicKeyTemplate().getParameter());
  }

  @SuppressWarnings("unchecked")
  private IssuerPublicKeyTemplateWrapper(JAXBElement<?> jaxbElement) throws SerializationException {

    final Class<?> delegateeClass = IssuerPublicKeyTemplate.class;
    if (delegateeClass.isAssignableFrom(jaxbElement.getDeclaredType())) {
      this.template = (JAXBElement<IssuerPublicKeyTemplate>) jaxbElement;
      this.parameterListHelper =
          new ParameterListHelper(getIssuerPublicKeyTemplate().getParameter());
    } else {
      throw new SerializationException("Idmx: " + delegateeClass.getSimpleName() + " is malformed.");
    }
  }

  /**
   * Returns the issuer parameters template (JAXB object).
   * 
   * @return
   */
  public IssuerPublicKeyTemplate getIssuerPublicKeyTemplate() {
    return (IssuerPublicKeyTemplate) JAXBIntrospector.getValue(template);
  }


  @Override
  protected String createParameterUriBasedOnParameterName(final String parameterName) {
    return ParameterBaseName.issuerPublicKeyTemplateParameterName(parameterName);
  }


//  /**
//   * Returns true if a parameter is present in the template.
//   * 
//   * @param parameterName
//   * @throws ConfigurationException
//   */
//  protected boolean hasParameter(String parameterName) {
//    return super.hasParameter(ParameterBaseName.issuerPublicKeyTemplateParameterName(parameterName));
//  }

  public URI getSystemParametersId() {
    return getIssuerPublicKeyTemplate().getSystemParametersId();
  }

  /**
   * @param systemParametersId
   */
  public void setSystemParametersId(final URI systemParametersId) {
    getIssuerPublicKeyTemplate().setSystemParametersId(systemParametersId);
  }

  /**
	 * 
	 */
  public URI getTechnology() {
    return getIssuerPublicKeyTemplate().getTechnology();
  }

  /**
   * @param technology
   */
  public void setTechnology(final URI technology) {
    getIssuerPublicKeyTemplate().setTechnology(technology);
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

  /**
   * @throws ConfigurationException
   * 
   */
  public int getMaximalNumberOfAttributes() throws ConfigurationException {
    return (Integer) getParameter(MAX_ATTRIBUTES_NAME);
  }

  /**
   * @param maximalNumberOfAttributes
   */
  public void setMaximalNumberOfAttributes(int maximalNumberOfAttributes) {
    setParameter(MAX_ATTRIBUTES_NAME, maximalNumberOfAttributes);
  }

  /**
   * @throws ConfigurationException
   * 
   */
  public URI getRevocationAuthority() throws ConfigurationException {
    return (URI) getParameter(REVOCATION_AUTHORITY_NAME);
  }

  public boolean hasRevocationAuthority() {
    return hasParameter(REVOCATION_AUTHORITY_NAME);
  }

  /**
   * @param revocationAuthority
   */
  public void setRevocationAuthority(final URI revocationAuthority) {
    setParameter(REVOCATION_AUTHORITY_NAME, revocationAuthority);
  }

  /**
	 * 
	 */
  public List<FriendlyDescription> getFriendlyDescription() {
    return getIssuerPublicKeyTemplate().getFriendlyDescription();
  }

  /**
   * @param friendlyDescription
   */
  public void addFriendlyDescription(final FriendlyDescription friendlyDescription) {
    getIssuerPublicKeyTemplate().getFriendlyDescription().add(friendlyDescription);
  }

  /**
   * Serializes the stored template using JAXB.
   * 
   * @param filename
   * @throws SerializationException
   */
  public String serialize() throws SerializationException {
    return JaxbHelperClass.serialize(template);
  }

  /**
   * De-serializes an issuer public key template formatted as a string into a JAXB object.
   * 
   * @param issuerPublicKeyTemplate
   * @return
   * @throws SerializationException
   */
  public static IssuerPublicKeyTemplateWrapper deserialize(final String issuerPublicKeyTemplate)
      throws SerializationException {
    return new IssuerPublicKeyTemplateWrapper(JaxbHelperClass.deserialize(issuerPublicKeyTemplate));
  }
}
