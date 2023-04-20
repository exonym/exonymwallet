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

package com.ibm.zurich.idmx.parameters.ra;

import java.net.URI;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBIntrospector;

import com.ibm.zurich.idmx.configuration.ErrorMessages;
import com.ibm.zurich.idmx.configuration.ParameterBaseName;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.SerializationException;
import com.ibm.zurich.idmx.jaxb.JaxbHelperClass;
import com.ibm.zurich.idmx.jaxb.ParameterListHelper;
import com.ibm.zurich.idmx.jaxb.ParametersHelper;
import com.ibm.zurich.idmx.keypair.ra.RevocationAuthorityPublicKeyWrapper;

import eu.abc4trust.xml.FriendlyDescription;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.Reference;
import eu.abc4trust.xml.RevocationAuthorityPublicKeyTemplate;

/**
 * 
 */
public class RevocationAuthorityPublicKeyTemplateWrapper extends ParametersHelper {

  // Template element names
  private static final String MODULUS_LENGTH_NAME = "modulusLength";
  private static final String NON_REVOCATION_EVIDENCE_REFERENCE_NAME =
      "nonRevocationEvidenceReference";
  private static final String NRE_UPDATE_REFERENCE_NAME = "nreUpdateReference";
  private static final String REVOCATION_INFORMATION_REFERENCE_NAME =
      "revocationInformationReference";


  // Delegatee
  private JAXBElement<RevocationAuthorityPublicKeyTemplate> delegatee;


  public RevocationAuthorityPublicKeyTemplateWrapper() {
    this(new ObjectFactory().createRevocationAuthorityPublicKeyTemplate());
  }

  public RevocationAuthorityPublicKeyTemplateWrapper(
      RevocationAuthorityPublicKeyTemplate revocationAuthorityPublicKeyTemplate) {
    this.delegatee =
        new ObjectFactory()
            .createRevocationAuthorityPublicKeyTemplate(revocationAuthorityPublicKeyTemplate);
    this.parameterListHelper =
        new ParameterListHelper(getRevocationAuthorityPublicKeyTemplate().getParameter());
  }


  /**
   * 
   */
  public URI getTechnology() {
    return getRevocationAuthorityPublicKeyTemplate().getTechnology();
  }

  /**
   * @param technology
   */
  public void setTechnology(final URI technology) {
    getRevocationAuthorityPublicKeyTemplate().setTechnology(technology);
  }


  @SuppressWarnings("unchecked")
  private RevocationAuthorityPublicKeyTemplateWrapper(final JAXBElement<?> jaxbElement)
      throws SerializationException {

    final Class<?> delegateeClass = RevocationAuthorityPublicKeyTemplate.class;
    if (delegateeClass.isAssignableFrom(jaxbElement.getDeclaredType())) {
      this.delegatee = (JAXBElement<RevocationAuthorityPublicKeyTemplate>) jaxbElement;
      this.parameterListHelper =
          new ParameterListHelper(getRevocationAuthorityPublicKeyTemplate().getParameter());
    } else {
      throw new SerializationException(ErrorMessages.nonAssignableErrorMessage(delegateeClass));
    }
  }

  protected String createParameterUriBasedOnParameterName(final String parameterName) {
    return ParameterBaseName.revocationAuthorityPublicKeyTemplateParameterName(parameterName);
  }


  /**
   * Returns the revocation authority public key template (JAXB object).
   */
  public RevocationAuthorityPublicKeyTemplate getRevocationAuthorityPublicKeyTemplate() {
    return (RevocationAuthorityPublicKeyTemplate) JAXBIntrospector.getValue(delegatee);
  }


  public URI getSystemParametersId() {
    return getRevocationAuthorityPublicKeyTemplate().getSystemParametersId();
  }

  public void setSystemParametersId(final URI systemParametersId) {
    getRevocationAuthorityPublicKeyTemplate().setSystemParametersId(systemParametersId);
  }

  public int getModulusLength() throws ConfigurationException {
    return (Integer) getParameter(MODULUS_LENGTH_NAME);
  }

  public void setModulusLength(final int modulusLength) {
    setParameter(MODULUS_LENGTH_NAME, modulusLength);
  }

  public URI getNonRevocationEvidenceReference() throws ConfigurationException {
    return (URI) getParameter(NON_REVOCATION_EVIDENCE_REFERENCE_NAME);
  }

  public void setNonRevocationEvidenceReference(final Reference nonRevocationEvidenceReference) {
    setParameter(NON_REVOCATION_EVIDENCE_REFERENCE_NAME,
        RevocationAuthorityPublicKeyWrapper.referenceToUri(nonRevocationEvidenceReference));
  }

  public void setNonRevocationEvidenceReference(final URI nonRevocationEvidenceReference) {
    setParameter(NON_REVOCATION_EVIDENCE_REFERENCE_NAME, nonRevocationEvidenceReference);
  }

  public URI getNonRevocationEvidenceUpdateReference() throws ConfigurationException {
    return (URI) getParameter(NRE_UPDATE_REFERENCE_NAME);
  }

  public void setNonRevocationEvidenceUpdateReference(final URI nonRevocationEvidenceUpdateReference) {
    setParameter(NRE_UPDATE_REFERENCE_NAME, nonRevocationEvidenceUpdateReference);
  }

  public void setNonRevocationEvidenceUpdateReference(final Reference nonRevocationEvidenceUpdateReference) {
    setParameter(NRE_UPDATE_REFERENCE_NAME,
        RevocationAuthorityPublicKeyWrapper.referenceToUri(nonRevocationEvidenceUpdateReference));
  }

  public Reference getRevocationInformationReference() throws ConfigurationException {
    return RevocationAuthorityPublicKeyWrapper
        .uriToReference((URI) getParameter(REVOCATION_INFORMATION_REFERENCE_NAME));
  }

  public void setRevocationInformationReference(final URI revocationInformationReference) {
    setParameter(REVOCATION_INFORMATION_REFERENCE_NAME, revocationInformationReference);
  }

  public void setRevocationInformationReference(final Reference revocationInformationReference) {
    setParameter(REVOCATION_INFORMATION_REFERENCE_NAME,
        RevocationAuthorityPublicKeyWrapper.referenceToUri(revocationInformationReference));
  }

  public URI getPublicKeyPrefix() throws ConfigurationException {
    return getRevocationAuthorityPublicKeyTemplate().getPublicKeyPrefix();
  }

  public void setPublicKeyPrefix(final URI publicKeyPrefix) {
    getRevocationAuthorityPublicKeyTemplate().setPublicKeyPrefix(publicKeyPrefix);
  }

  public List<FriendlyDescription> getFriendlyDescription() {
    return getRevocationAuthorityPublicKeyTemplate().getFriendlyDescription();
  }

  public void addFriendlyDescription(final FriendlyDescription friendlyDescription) {
    getRevocationAuthorityPublicKeyTemplate().getFriendlyDescription().add(friendlyDescription);
  }

  /**
   * Serializes the stored template using JAXB.
   * 
   * @throws SerializationException
   */
  public String serialize() throws SerializationException {
    return JaxbHelperClass.serialize(delegatee);
  }

  /**
   * De-serializes an delegatee element formatted as a string into a JAXB object.
   * 
   * @throws SerializationException
   */
  public static RevocationAuthorityPublicKeyTemplateWrapper deserialize(final String delegatee)
      throws SerializationException {
    return new RevocationAuthorityPublicKeyTemplateWrapper(JaxbHelperClass.deserialize(delegatee));
  }
}
