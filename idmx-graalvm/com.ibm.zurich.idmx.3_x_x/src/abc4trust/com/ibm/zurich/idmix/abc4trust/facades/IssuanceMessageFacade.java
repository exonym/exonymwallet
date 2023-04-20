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

package com.ibm.zurich.idmix.abc4trust.facades;

import java.net.URI;
import java.util.List;

import javax.xml.bind.JAXBElement;

import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;

import eu.abc4trust.xml.AttributeList;
import eu.abc4trust.xml.CredentialDescription;
import eu.abc4trust.xml.CredentialTemplate;
import eu.abc4trust.xml.IssuanceExtraMessage;
import eu.abc4trust.xml.IssuanceMessage;
import eu.abc4trust.xml.IssuanceMessageAndBoolean;
import eu.abc4trust.xml.IssuancePolicy;
import eu.abc4trust.xml.IssuanceToken;
import eu.abc4trust.xml.MechanismSpecification;
import eu.abc4trust.xml.NonRevocationEvidence;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.VerifierParameters;
import eu.abc4trust.xml.ZkProof;

/**
 * 
 */
//TODO(ksa) why not immutable (for complete PACKAGE)
public class IssuanceMessageFacade {

  private IssuanceMessage issuanceMessage;
  private URI issuanceLogEntry;
  private boolean isLastMessage;

  @SuppressWarnings("unused")
  private final BigIntFactory bigIntFactory;

  /**
   * Constructor.
   */
  public IssuanceMessageFacade(final IssuanceMessageAndBoolean issuanceMessage,
                               final BigIntFactory bigIntFactory) {
    this.issuanceMessage = issuanceMessage.getIssuanceMessage();
    this.issuanceLogEntry = issuanceMessage.getIssuanceLogEntryURI();
    this.isLastMessage = issuanceMessage.isLastMessage();

    this.bigIntFactory = bigIntFactory;
  }

  /**
   * Constructor.
   */
  public IssuanceMessageFacade() {
    this(new ObjectFactory().createIssuanceMessageAndBoolean(), null);
    setIssuanceMessage(new ObjectFactory().createIssuanceMessage());
  }

  /**
   * Convenience constructor.
   */
  public IssuanceMessageFacade(final URI context, final IssuancePolicy issuancePolicy,
                               final VerifierParameters verifierParameters) {
    this();
    setContext(context);
    setIssuancePolicy(issuancePolicy);
    setVerifierParameters(verifierParameters);
    setIssuanceLog(null);
    setLastMessage(false);
  }

  /**
   * Constructor.
   */
  public IssuanceMessageFacade(final IssuanceMessage issuanceMessage, final BigIntFactory bigIntFactory) {
    this.issuanceMessage = issuanceMessage;
    setIssuanceLog(null);
    setLastMessage(false);

    this.bigIntFactory = bigIntFactory;
  }

  /**
   * Convenience constructor.
   */
  public IssuanceMessageFacade(final URI context, final IssuancePolicy issuancePolicy,
                               final VerifierParameters verifierParameters, final AttributeList issuerProvidedAttributes) {
    this();
    setContext(context);
    setIssuancePolicy(issuancePolicy);
    setVerifierParameters(verifierParameters);
    setIssuerProvidedAttributes(issuerProvidedAttributes);
    setIssuanceLog(null);
    setLastMessage(false);
  }

  private void setIssuanceMessage(final IssuanceMessage issuanceMessage) {
    this.issuanceMessage = issuanceMessage;
  }

  /**
   * Returns the issuance session identifier (context) associated with this issuance message.
   */
  public URI getContext() {
    return issuanceMessage.getContext();
  }

  /**
   * Sets the issuance session identifier (context) to the given value.
   */
  public void setContext(final URI context) {
    issuanceMessage.setContext(context);
  }

  public URI getIssuanceLogEntry() {
    return issuanceLogEntry;
  }

  public void setIssuanceLog(final URI issuanceLogEntry) {
    this.issuanceLogEntry = issuanceLogEntry;
  }

  public boolean isLastMessage() {
    return isLastMessage;
  }


  public void setLastMessage(final boolean isLastMessage) {
    this.isLastMessage = isLastMessage;
  }



  /**
   * Returns the issuance policy stored in the issuance message and null if there is no issuance
   * policy present.
   */
  public IssuancePolicy getIssuancePolicy() {
    return getElementFromIssuanceMessage(IssuancePolicy.class);
  }

  /**
   * Removes an issuance policy if it exists and adds the given issuance policy to the issuance
   * message.
   */
  public void setIssuancePolicy(final IssuancePolicy issuancePolicy) {
    addElementToIssuanceMessage(IssuancePolicy.class,
        new ObjectFactory().createIssuancePolicy(issuancePolicy));
  }

  public VerifierParameters getVerifierParameters() {
    return getElementFromIssuanceMessage(VerifierParameters.class);
  }

  public void setVerifierParameters(final VerifierParameters verifierParameters) {
    addElementToIssuanceMessage(VerifierParameters.class,
        new ObjectFactory().createVerifierParameters(verifierParameters));
  }

  public ZkProof getZkProof() {
    return getElementFromIssuanceMessage(ZkProof.class);
  }

  public void setZkProof(final ZkProof zkProof) {
    addElementToIssuanceMessage(ZkProof.class, new ObjectFactory().createZkProof(zkProof));
  }

  public CredentialDescription getCredentialDescription() {
    return getElementFromIssuanceMessage(CredentialDescription.class);
  }

  public void setAdditionalMessage(final IssuanceExtraMessage additionalIssuanceMessage) {
    addElementToIssuanceMessage(IssuanceExtraMessage.class,
        new ObjectFactory().createIssuanceExtraMessage(additionalIssuanceMessage));
  }

  public IssuanceExtraMessage getAdditionalMessage() {
    return getElementFromIssuanceMessage(IssuanceExtraMessage.class);
  }



  /**
   * Returns the issuer parameter identifier (assumes that an issuance policy containing a
   * credential template is present).
   */
  public URI getIssuerParametersUID() {
    return getIssuancePolicy().getCredentialTemplate().getIssuerParametersUID();
  }

  /**
   * Returns the ID of the credential specification.
   */
  public URI getCredentialSpecificationUID() {
    URI credSpecId = null;
    // use the issuance policy if available
    if (getIssuancePolicy() != null && getIssuancePolicy().getCredentialTemplate() != null) {
      credSpecId = getIssuancePolicy().getCredentialTemplate().getCredentialSpecUID();
    }
    // try the credential description (given the issuance policy or credential template has not been
    // available)
    if (credSpecId == null && getCredentialDescription() != null) {
      credSpecId = getCredentialDescription().getCredentialSpecificationUID();
    }
    return credSpecId;
  }

  public boolean isAdditionalRoundMessage() {
    return (getElementFromIssuanceMessage(IssuanceExtraMessage.class) != null);
  }

  public boolean jointRandomValuesPresent() {
    // TODO: check the issuance policy for joint random values to be computed
    return false;
  }

  public boolean issuanceTokenPresent() {
    // TODO: check the issuance policy for issuance token
    return false;
  }



  // TODO remove duplicate method - (duplicate method also used in other facades with an ANY
  // element)
  private <T> void addElementToIssuanceMessage(final Class<T> elementClass, final JAXBElement<T> element) {
    final T object = getElementFromIssuanceMessage(elementClass);
    // remove previously stored object of the same type
    if (object != null) {
      getExtensionList().remove(object);
    }
    // add new object to the list of elements
    getExtensionList().add(element);
  }

  // TODO remove duplicate method - (duplicate method also used in other facades with an ANY
  // element)
  @SuppressWarnings("unchecked")
  private <T> T getElementFromIssuanceMessage(final Class<T> elementClass) {
    for (final Object object : getExtensionList()) {
      if (object == null || !(object instanceof JAXBElement<?>)) {

    	  if (elementClass.isInstance(object)) {
    		  return (T) object;
    	  }
        continue;
      }
      final JAXBElement<?> el = (JAXBElement<?>) object;
      if (el.getValue().getClass().isAssignableFrom(elementClass)) {
        return (T) el.getValue();
      }
    }
    return null;
  }

  private List<Object> getExtensionList() {
    return issuanceMessage.getContent();
  }



  public IssuanceMessageAndBoolean getExtendedIssuanceMessage() {
    IssuanceMessageAndBoolean issuanceMessageAndBoolean =
        new ObjectFactory().createIssuanceMessageAndBoolean();
    issuanceMessageAndBoolean.setIssuanceLogEntryURI(issuanceLogEntry);
    issuanceMessageAndBoolean.setIssuanceMessage(issuanceMessage);
    issuanceMessageAndBoolean.setLastMessage(isLastMessage);

    return issuanceMessageAndBoolean;
  }

  public IssuanceMessage getIssuanceMessage() {
    return issuanceMessage;
  }

  public void setIssuanceToken(final IssuanceToken it) {
    addElementToIssuanceMessage(IssuanceToken.class, new ObjectFactory().createIssuanceToken(it));
  }

  public IssuanceToken getIssuanceToken() {
    return getElementFromIssuanceMessage(IssuanceToken.class);
  }

  public CredentialTemplate getCredentialTemplate() {
    return getElementFromIssuanceMessage(CredentialTemplate.class);
  }

  public void setCredentialTemplate(final CredentialTemplate ct) {
    addElementToIssuanceMessage(CredentialTemplate.class,
        new ObjectFactory().createCredentialTemplate(ct));
  }

  public void setIssuerProvidedAttributes(final AttributeList al) {
    addElementToIssuanceMessage(AttributeList.class, new ObjectFactory().createAttributeList(al));
  }

  public AttributeList getIssuerProvidedAttributes() {
    return getElementFromIssuanceMessage(AttributeList.class);
  }

  public MechanismSpecification getMechanismSpecification() {
    return getElementFromIssuanceMessage(MechanismSpecification.class);
  }

  public void setMechanismSpecification(final MechanismSpecification ms) {
    addElementToIssuanceMessage(MechanismSpecification.class,
        new ObjectFactory().createMechanismSpecification(ms));
  }

  public NonRevocationEvidence getNonRevocationEvidence() {
    return getElementFromIssuanceMessage(NonRevocationEvidence.class);
  }

  public void setNonRevocationEvidence(final NonRevocationEvidence nonRevocationEvidence) {
    addElementToIssuanceMessage(NonRevocationEvidence.class,
        new ObjectFactory().createNonRevocationEvidence(nonRevocationEvidence));
  }
}
