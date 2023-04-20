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

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBIntrospector;

import com.ibm.zurich.idmix.abc4trust.XmlUtils;
import com.ibm.zurich.idmix.abc4trust.util.EncodeDecode;
import com.ibm.zurich.idmx.configuration.ErrorMessages;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.SerializationException;
import com.ibm.zurich.idmx.interfaces.configuration.Constants;
import com.ibm.zurich.idmx.jaxb.JaxbHelperClass;
import com.ibm.zurich.idmx.keypair.ra.RevocationAuthorityPublicKeyWrapper;

import eu.abc4trust.xml.CryptoParams;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PublicKey;
import eu.abc4trust.xml.Reference;
import eu.abc4trust.xml.RevocationAuthorityParameters;

/**
 * 
 */
public class RevocationAuthorityParametersFacade implements Constants {

  // Delegatee
  private final JAXBElement<RevocationAuthorityParameters> raParameters;



  public RevocationAuthorityParametersFacade() {
    this(new ObjectFactory().createRevocationAuthorityParameters());
  }

  public RevocationAuthorityParametersFacade(final RevocationAuthorityParameters raParameters) {
    this.raParameters = new ObjectFactory().createRevocationAuthorityParameters(raParameters);
  }

  private RevocationAuthorityParametersFacade(final JAXBElement<?> jaxbElement)
      throws SerializationException {
    this.raParameters = verifyTypeOfJaxbElement(jaxbElement);
  }


  @Deprecated
  public String getImplementationVersion() {
    return getRevocationAuthorityParameters().getVersion();
  }

  @Deprecated
  public void setImplementationVersion(final String version) {
    getRevocationAuthorityParameters().setVersion(version);
  }

  public URI getRevocationAuthorityParametersId() {
    return getRevocationAuthorityParameters().getParametersUID();
  }

  public void setRevocationAuthorityParametersId(final URI issuerParametersId) {
    getRevocationAuthorityParameters().setParametersUID(issuerParametersId);
  }

  public URI getRevocationMechanism() {
    return getRevocationAuthorityParameters().getRevocationMechanism();
  }

  public void setRevocationMechanism(final URI revocationMechanism) {
    getRevocationAuthorityParameters().setRevocationMechanism(revocationMechanism);
  }

  public Reference getRevocationInfoReference() {
    return getRevocationAuthorityParameters().getRevocationInfoReference();
  }

  public void setRevocationInfoReference(final Reference revocationInfoReference) {
    getRevocationAuthorityParameters().setRevocationInfoReference(revocationInfoReference);
  }

  public Reference getNonRevocationEvidenceReference() {
    return getRevocationAuthorityParameters().getNonRevocationEvidenceReference();
  }

  public void setNonRevocationEvidenceReference(final Reference nonRevocationEvidenceReference) {
    getRevocationAuthorityParameters().setNonRevocationEvidenceReference(
        nonRevocationEvidenceReference);
  }

  public Reference getNonRevocationEvidenceUpdateReference() {
    return getRevocationAuthorityParameters().getNonRevocationEvidenceUpdateReference();
  }

  public void setNonRevocationEvidenceUpdateReference(final Reference nonRevocationEvidenceUpdateReference) {
    getRevocationAuthorityParameters().setNonRevocationEvidenceUpdateReference(
        nonRevocationEvidenceUpdateReference);
  }

  public Reference getRevocationInformationReference() {
    return getRevocationAuthorityParameters().getNonRevocationEvidenceUpdateReference();
  }

  public void setRevocationInformationReference(final Reference revocationInformationReference) {
    getRevocationAuthorityParameters().setRevocationInfoReference(revocationInformationReference);
  }


  public void setPublicKey(final PublicKey publicKey) {

    final ObjectFactory objectFactory = new ObjectFactory();
    final CryptoParams cryptoParameters = objectFactory.createCryptoParams();
    cryptoParameters.getContent().add(objectFactory.createPublicKey(publicKey));
    getRevocationAuthorityParameters().setCryptoParams(cryptoParameters);
  }

  public PublicKey getPublicKey() {
//	    Object publicKeyObject =
//	            JAXBIntrospector.getValue(getRevocationAuthorityParameters().getCryptoParams().getContent()
//	                .get(0));
	  XmlUtils.fixNestedContent(getRevocationAuthorityParameters().getCryptoParams());
	  final Object publicKeyObject =
	            getRevocationAuthorityParameters().getCryptoParams().getContent()
	                .get(0);
	    return (PublicKey) publicKeyObject;
//    if (PublicKey.class.isAssignableFrom(publicKeyObject.getClass())) {
//      return (PublicKey) publicKeyObject;
//    } else {
//      return null;
//    }
  }


  public RevocationAuthorityParameters getRevocationAuthorityParameters() {
    return (RevocationAuthorityParameters) JAXBIntrospector.getValue(raParameters);
  }

  /**
   * @param jaxbElement
   * @throws SerializationException
   */
  @SuppressWarnings("unchecked")
  private static JAXBElement<RevocationAuthorityParameters> verifyTypeOfJaxbElement(
    final JAXBElement<?> jaxbElement) throws SerializationException {
    final Class<?> delegateeClass = RevocationAuthorityParameters.class;
    if (delegateeClass.isAssignableFrom(jaxbElement.getDeclaredType())) {
      return (JAXBElement<RevocationAuthorityParameters>) jaxbElement;
    } else {
      throw new SerializationException(ErrorMessages.malformedClass(delegateeClass.getSimpleName()));
    }
  }

  /**
   * @param jaxbElement
   * @throws SerializationException
   * @throws ConfigurationException
   */
  private static void verifyParametersConsistency(final JAXBElement<?> jaxbElement)
      throws SerializationException, ConfigurationException {
    final JAXBElement<RevocationAuthorityParameters> jaxbRaParameters =
        verifyTypeOfJaxbElement(jaxbElement);

    final RevocationAuthorityParameters raParameters =
        (RevocationAuthorityParameters) JAXBIntrospector.getValue(jaxbRaParameters);

    // Verify that a public key is wrapped within the crypto parameters
    final PublicKey publicKey;
    try {
      publicKey = (PublicKey) raParameters.getCryptoParams().getContent().get(0);
    } catch (final ClassCastException e) {
      throw new ConfigurationException(
          "Idmx: PublicKey must be wrapped in the CryptoParams element.");
    }

    final RevocationAuthorityPublicKeyWrapper rapkWrapper =
        new RevocationAuthorityPublicKeyWrapper(publicKey);

    final URI publicKeyId = rapkWrapper.getPublicKeyId();
    if (!raParameters.getParametersUID().equals(publicKeyId)) {
      throw new ConfigurationException(ErrorMessages.elementIdsMismatch("PublicKeyId"));
    }
  }

  public static RevocationAuthorityParametersFacade initRevocationAuthorityParameters(
      final PublicKey revocationAuthorityPublicKey) throws ConfigurationException {
    final RevocationAuthorityParametersFacade raParametersFacade =
        new RevocationAuthorityParametersFacade();
    final RevocationAuthorityPublicKeyWrapper rapkWrapper =
        new RevocationAuthorityPublicKeyWrapper(revocationAuthorityPublicKey);

    // Wrap the public key within the crypto parameters
    raParametersFacade.setPublicKey(rapkWrapper.getPublicKey());

    // Set the rest of the parameters using the public key wrapper
    raParametersFacade
        .setRevocationAuthorityParametersId(getRevocationAuthorityParametersUID(rapkWrapper
            .getPublicKeyId()));
    raParametersFacade.setRevocationMechanism(rapkWrapper.getPublicKeyTechnology());
    raParametersFacade.setNonRevocationEvidenceReference(rapkWrapper
        .getNonRevocationEvidenceReference());
    raParametersFacade.setNonRevocationEvidenceUpdateReference(rapkWrapper
        .getNonRevocationEvidenceUpdateReference());
    raParametersFacade.setRevocationInformationReference(rapkWrapper
        .getRevocationInformationReference());
    raParametersFacade.setImplementationVersion(rapkWrapper.getImplementationVersion());

    return raParametersFacade;
  }


  public static RevocationAuthorityParametersFacade deserialize(final String issuerParameters)
      throws SerializationException, ConfigurationException {

    final JAXBElement<?> jaxbElement = JaxbHelperClass.deserialize(issuerParameters);

    // Verify that the issuer parameters are consistent (e.g., the public key id within the key
    // and the surrounding issuer parameters or the maximal number of attributes match).
    verifyParametersConsistency(jaxbElement);

    return new RevocationAuthorityParametersFacade(jaxbElement);
  }

  public String serialize() throws SerializationException {
    return JaxbHelperClass.serialize(raParameters);
  }

  /**
   * Converts an Idmx compliant public key id to an ABC4Trust compliant parameters uid.
   */
  public static URI getRevocationAuthorityParametersUID(final URI publicKeyId) {
    return EncodeDecode.removeRandomSuffix(publicKeyId);
  }
}
