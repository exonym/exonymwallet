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

import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBIntrospector;

import com.ibm.zurich.idmix.abc4trust.XmlUtils;
import com.ibm.zurich.idmx.annotations.Nullable;
import com.ibm.zurich.idmx.configuration.Configuration;
import com.ibm.zurich.idmx.configuration.ErrorMessages;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.NotEnoughTokensException;
import com.ibm.zurich.idmx.exception.SerializationException;
import com.ibm.zurich.idmx.interfaces.configuration.Constants;
import com.ibm.zurich.idmx.interfaces.signature.ListOfSignaturesAndAttributes;
import com.ibm.zurich.idmx.interfaces.state.CarryOverStateRecipientWithAttributes;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.jaxb.JaxbHelperClass;

import eu.abc4trust.abce.internal.user.credentialManager.CredentialManager;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.util.AttributeConverter;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.AttributeDescription;
import eu.abc4trust.xml.Credential;
import eu.abc4trust.xml.CredentialDescription;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.CredentialTemplate;
import eu.abc4trust.xml.CryptoParams;
import eu.abc4trust.xml.FriendlyDescription;
import eu.abc4trust.xml.NonRevocationEvidence;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.Signature;
import eu.abc4trust.xml.SignatureToken;

/**
 * 
 */
public class CredentialFacade implements Constants {

  // Delegatee
  private final JAXBElement<Credential> delegatee;



  public CredentialFacade() {
    this(new ObjectFactory().createCredential());
  }

  public CredentialFacade(final Credential credential) {
    this.delegatee = new ObjectFactory().createCredential(credential);
    // addCryptoParams();
  }

  private CredentialFacade(final JAXBElement<?> jaxbElement) throws SerializationException {
    this.delegatee = verifyTypeOfJaxbElement(jaxbElement);
    // addCryptoParams();
  }

  // private void addCryptoParams() {
  // if (delegatee.getValue().getCryptoParams() == null) {
  // delegatee.getValue().setCryptoParams(new ObjectFactory().createCryptoParams());
  // }
  // }


  public CredentialFacade(final CredentialSpecification cs, final AttributeConverter ac, final CredentialTemplate ct,
                          final @Nullable CarryOverStateRecipientWithAttributes coState, final List<Attribute> issuerAttributes,
      final NonRevocationEvidence nre, final ListOfSignaturesAndAttributes sig) {
    this();
    addSignature(sig.signature);
    updateAttributes(cs, issuerAttributes);
    setCredentialSpecificationUID(cs.getSpecificationUID());
    setIssuerUID(ct.getIssuerParametersUID());
    setRevoked(false);
    setImageReference(cs.getDefaultImageReference());
    setFriendlyName(cs.getFriendlyCredentialName());
    if (coState != null) {
      setSecretReference(coState.deviceUid);
      setCredentialUID(coState.credentialUid);
      updateAttributes(cs, coState.attributes);
      sanityCheck(ac, sig.attributes);
    }
    if (nre != null) {
      setNonRevocationEvidence(nre);
    }
  }

  public URI getCredentialUID() {
    return getCredentialDescription().getCredentialUID();
  }

  public URI getCredentialSpecificicationUID() {
    return getCredentialDescription().getCredentialSpecificationUID();
  }

  public URI getIssuerParametersUID() {
    return getCredentialDescription().getIssuerParametersUID();
  }

  public URI getRevocationInformationUID() {
    // TODO
    return null;
  }

  private void updateAttributes(final CredentialSpecification cs, final List<Attribute> attributes) {
    final CredentialDescription cd = getCredentialDescription();
    final Map<URI, Attribute> merge = new LinkedHashMap<URI, Attribute>();
    // First go through the cred spec to enforce the ordering of attributes in the map
    for (final AttributeDescription ad : cs.getAttributeDescriptions().getAttributeDescription()) {
      final Attribute a = new ObjectFactory().createAttribute();
      a.setAttributeDescription(ad);
      merge.put(ad.getType(), a);
    }
    // Put in the attributes we already have (preserving the order in the map)
    for (final Attribute a : cd.getAttribute()) {
      final URI type = a.getAttributeDescription().getType();
      final Attribute old = merge.get(type);
      if (old == null) {
        throw new RuntimeException("Unknown attribute type " + type);
      }
      a.setAttributeDescription(old.getAttributeDescription());
      merge.put(type, a);
    }
    // Put in new attributes (preserving the order of the map)
    for (final Attribute a : attributes) {
      if (a == null) {
        continue;
      }
      final AttributeDescription ad = a.getAttributeDescription();
      final URI type = ad.getType();
      final Attribute old = merge.get(type);
      if (old == null) {
        throw new RuntimeException("Unknown attribute type " + type);
      }
      a.setAttributeDescription(old.getAttributeDescription());
      merge.put(type, a);
    }
    cd.getAttribute().clear();
    cd.getAttribute().addAll(merge.values());
  }

  private void sanityCheck(final AttributeConverter ac, final List<BigInt> attributeValues) {
    final CredentialDescription cd = getCredentialDescription();
    final Iterator<BigInt> avIterator = attributeValues.iterator();
    for (final Attribute a : cd.getAttribute()) {
      if (a == null) {
        throw new RuntimeException("Missing attribute in new credential");
      }
      final BigInt av = avIterator.next();

      final BigInteger actualValue = ac.getIntegerValueOrNull(a);
      if (!av.getValue().equals(actualValue)) {
        throw new RuntimeException("Attribute value is incorrect: "
            + a.getAttributeDescription().getType() + " in attribute list: " + actualValue
            + " in proof: " + av);
      }

    }
  }

  private void setFriendlyName(final List<FriendlyDescription> friendlyCredentialName) {
    getCredentialDescription().getFriendlyCredentialName().addAll(friendlyCredentialName);
  }

  public void setSecretReference(final URI secretUid) {
    getCredentialDescription().setSecretReference(secretUid);
  }

  public void setCredentialUID(final URI credentialUID) {
    getCredentialDescription().setCredentialUID(credentialUID);
  }

  public void setCredentialSpecificationUID(final URI credentialSpecUID) {
    getCredentialDescription().setCredentialSpecificationUID(credentialSpecUID);
  }

  public void setIssuerUID(final URI issuerUID) {
    getCredentialDescription().setIssuerParametersUID(issuerUID);
  }

  public void setImageReference(final URI image) {
    getCredentialDescription().setImageReference(image);
  }

  public void setRevoked(final boolean revoked) {
    getCredentialDescription().setRevokedByIssuer(revoked);
  }

  public boolean isRevoked() {
    return getCredentialDescription().isRevokedByIssuer();
  }

  public void setSignaturesAndAttributes(final ListOfSignaturesAndAttributes signaturesAndAttributes) {
    getCryptoParameterList().add(
        new ObjectFactory().createSignature(signaturesAndAttributes.signature));
  }


  public ArrayList<Credential> getCredentialTokens() {
    final ArrayList<Credential> credentialTokens = new ArrayList<Credential>();
    credentialTokens.add(getDelegateeValue());
    return credentialTokens;
  }

  public Credential getDelegateeValue() {
    return (Credential) JAXBIntrospector.getValue(delegatee);
  }

  public CredentialDescription getCredentialDescription() {
    CredentialDescription credentialDescription = getDelegateeValue().getCredentialDescription();
    if (credentialDescription == null) {
      ObjectFactory objectFactory = new ObjectFactory();
      credentialDescription = objectFactory.createCredentialDescription();
      getDelegateeValue().setCredentialDescription(credentialDescription);
    }

    return credentialDescription;
  }


  // TODO remove duplicate code (this is the same as the system parameters use)
  private List<Object> getCryptoParameterList() {
    CryptoParams cryptoParams = getDelegateeValue().getCryptoParams();
    if (cryptoParams == null) {
      cryptoParams = new ObjectFactory().createCryptoParams();
      getDelegateeValue().setCryptoParams(cryptoParams);
    }
    XmlUtils.fixNestedContent(cryptoParams);
    return cryptoParams.getContent();
  }


  /**
   * @param jaxbElement
   * @throws SerializationException
   */
  @SuppressWarnings("unchecked")
  private static JAXBElement<Credential> verifyTypeOfJaxbElement(final JAXBElement<?> jaxbElement)
      throws SerializationException {
    final Class<?> delegateeClass = Credential.class;
    if (delegateeClass.isAssignableFrom(jaxbElement.getDeclaredType())) {
      return (JAXBElement<Credential>) jaxbElement;
    } else {
      throw new SerializationException(ErrorMessages.malformedClass(delegateeClass.getSimpleName()));
    }
  }


  public static CredentialFacade deserialize(final String credential) throws SerializationException,
      ConfigurationException {

    final JAXBElement<?> jaxbElement = JaxbHelperClass.deserialize(credential);

    return new CredentialFacade(jaxbElement);
  }

  public String serialize() throws SerializationException {
    return JaxbHelperClass.serialize(delegatee);
  }

  /**
   * Retrieve a signature token from the credential. If the signature token is not re-usable, it is
   * removed from the credential, and the credential is updated in the credential manager.
   * 
   * @param credentialManager
   * @return
   * @throws NotEnoughTokensException
   * @throws CredentialManagerException
   */
  public SignatureToken consumeToken(final String username, final CredentialManager credentialManager)
      throws NotEnoughTokensException, CredentialManagerException {
    final Signature sig = getSignature();

    if (sig.isCanReuseToken()) {
      return sig.getSignatureToken().get(0);
    } else {
      int numRemainingTokens = sig.getSignatureToken().size();
      int minimumNumberOfRemainingTokens = 1;
      if (Configuration.saveLastSignatureTokenForReIssuance()) {
        // If we want to save one token for the re-issuance protocol
        minimumNumberOfRemainingTokens = 2;
      }
      if (numRemainingTokens < minimumNumberOfRemainingTokens) {
        throw new NotEnoughTokensException(getCredentialDescription().getCredentialUID());
      }
      final int indexOfLastToken = numRemainingTokens - 1;
      final SignatureToken tok = sig.getSignatureToken().get(indexOfLastToken);
      // Remove consumed token from credential and store it in the credential manager
      sig.getSignatureToken().remove(indexOfLastToken);
      credentialManager.storeCredential(username, getDelegateeValue());
      return tok;
    }
  }

  /**
   * Returns the first token in the signature. Use this method only for re-issuance.
   * 
   * @return
   */
  public SignatureToken getTokenWithoutConsumingIt() {
    final Signature sig = getSignature();
    return sig.getSignatureToken().get(0);
  }

  private Signature getSignature() {
    return getContainedObject(Signature.class);
  }

  private void addSignature(Signature sig) {
    if (getSignature() != null) {
      throw new RuntimeException("Cannot add signature twice");
    }
    getCryptoParameterList().add(new ObjectFactory().createSignature(sig));
  }


  // TODO used in RevocationMessageFacade and CredentialFacade (should be used in a general facade)
  @SuppressWarnings("unchecked")
  private <T> T getContainedObject(final Class<T> type) {
    final List<Object> cryptoParameterList = getCryptoParameterList();
    for (final Object object : cryptoParameterList) {
      final Object containedObject = JAXBIntrospector.getValue(object);

      if (type.isAssignableFrom(containedObject.getClass())) {
        return (T) containedObject;
      }
    }
    return null;
  }

  private <T> void replaceContainedObject(final Class<T> type, final JAXBElement<T> object) {
    final List<Object> cryptoParameterList = getCryptoParameterList();
    for (final Object jaxbObject : cryptoParameterList) {
      final Object containedObject = JAXBIntrospector.getValue(jaxbObject);

      if (type.isAssignableFrom(containedObject.getClass())) {
        cryptoParameterList.remove(jaxbObject);
        break;
      }
    }
    cryptoParameterList.add(object);
  }

  public NonRevocationEvidence getNonRevocationEvidence() {
    return getContainedObject(NonRevocationEvidence.class);
  }

  public void setNonRevocationEvidence(final NonRevocationEvidence nre) {

    replaceContainedObject(NonRevocationEvidence.class,
        new ObjectFactory().createNonRevocationEvidence(nre));
  }



}
