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
import javax.xml.bind.JAXBIntrospector;

import com.ibm.zurich.idmix.abc4trust.XmlUtils;
import com.ibm.zurich.idmx.configuration.ErrorMessages;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.SerializationException;
import com.ibm.zurich.idmx.interfaces.configuration.Constants;
import com.ibm.zurich.idmx.jaxb.JaxbHelperClass;
import com.ibm.zurich.idmx.keypair.issuer.IssuerPublicKeyWrapper;
import com.ibm.zurich.idmx.parameters.system.SystemParametersWrapper;

import eu.abc4trust.xml.CryptoParams;
import eu.abc4trust.xml.FriendlyDescription;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PublicKey;
import eu.abc4trust.xml.SystemParameters;

/**
 * 
 */
public class IssuerParametersFacade implements Constants {

  // Delegatee
  private final JAXBElement<IssuerParameters> issuerParameters;



  public IssuerParametersFacade() {
    this(new ObjectFactory().createIssuerParameters());
  }

  public IssuerParametersFacade(final IssuerParameters issuerParameters) {
    this.issuerParameters = new ObjectFactory().createIssuerParameters(issuerParameters);
  }

  private IssuerParametersFacade(final JAXBElement<?> jaxbElement) throws SerializationException {
    this.issuerParameters = verifyTypeOfJaxbElement(jaxbElement);
  }

  public String getImplementationVersion() {
    return getIssuerParameters().getVersion();
  }

  public void setImplementationVersion(final String version) {
    getIssuerParameters().setVersion(version);
  }
  
  public String getHashAlgorithm() {
    return getIssuerParameters().getHashAlgorithm().toString();
  }

  public void setHashAlgorithm(final String hashAlgorithm) {
    getIssuerParameters().setHashAlgorithm(URI.create(hashAlgorithm));
  }

  public URI getIssuerParametersId() throws ConfigurationException {
    return getIssuerParameters().getParametersUID();
  }

  public void setIssuerParametersId(final URI issuerParametersId) {
    getIssuerParameters().setParametersUID(issuerParametersId);
  }

  public URI getBuildingBlockId() {
    return getIssuerParameters().getAlgorithmID();
  }

  public void setBuildingBlockId(final URI publicKeyTechnology) {
    getIssuerParameters().setAlgorithmID(publicKeyTechnology);
  }

  public void setSystemParametersId(final URI systemParametersId) {
    getIssuerParameters().setSystemParametersUID(systemParametersId);
  }

  public URI getSystemParametersId() {
    return getIssuerParameters().getSystemParametersUID();
  }

  public void setRevocationAuthorityId(final URI revocationAuthorityId) {
    getIssuerParameters().setRevocationParametersUID(revocationAuthorityId);
  }

  public URI getRevocationAuthorityId() {
    return getIssuerParameters().getRevocationParametersUID();
  }


  public void setPublicKey(final PublicKey publicKey) {
    final ObjectFactory objectFactory = new ObjectFactory();
    final CryptoParams cryptoParameters = objectFactory.createCryptoParams();
    cryptoParameters.getContent().add(objectFactory.createPublicKey(publicKey));
    getIssuerParameters().setCryptoParams(cryptoParameters);
  }

  public PublicKey getPublicKey() {
//	    Object publicKeyObject =
//	            JAXBIntrospector.getValue(getIssuerParameters().getCryptoParams().getContent().get(0));
	  XmlUtils.fixNestedContent(getIssuerParameters().getCryptoParams());
	  final Object publicKeyObject =
	            getIssuerParameters().getCryptoParams().getContent().get(0);
    if (PublicKey.class.isAssignableFrom(publicKeyObject.getClass())) {
      return (PublicKey) publicKeyObject;
    } else {
      return null;
    }
  }


  public IssuerParameters getIssuerParameters() {
    return (IssuerParameters) JAXBIntrospector.getValue(issuerParameters);
  }

  /**
   * @param jaxbElement
   * @throws SerializationException
   */
  @SuppressWarnings("unchecked")
  private static JAXBElement<IssuerParameters> verifyTypeOfJaxbElement(final JAXBElement<?> jaxbElement)
      throws SerializationException {
    final Class<?> delegateeClass = IssuerParameters.class;
    if (delegateeClass.isAssignableFrom(jaxbElement.getDeclaredType())) {
      return (JAXBElement<IssuerParameters>) jaxbElement;
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
    final JAXBElement<IssuerParameters> jaxbIssuerParameters = verifyTypeOfJaxbElement(jaxbElement);

    final IssuerParameters issuerParameters =
        (IssuerParameters) JAXBIntrospector.getValue(jaxbIssuerParameters);

	  
    // Verify that a public key is wrapped within the crypto parameters
    final PublicKey publicKey;
    try {
      publicKey = (PublicKey) issuerParameters.getCryptoParams().getContent().get(0);
    } catch (final ClassCastException e) {
      throw new ConfigurationException(
          "Idmx: PublicKey must be wrapped in the CryptoParams element.");
    }

    final IssuerPublicKeyWrapper issuerPublicKeyFacade = new IssuerPublicKeyWrapper(publicKey);

    final URI publicKeyId = issuerPublicKeyFacade.getPublicKeyId();
    if (!issuerParameters.getParametersUID().equals(publicKeyId)) {
      throw new ConfigurationException(ErrorMessages.elementIdsMismatch("PublicKeyId"));
    }
  }

  public static IssuerParametersFacade initIssuerParameters(final PublicKey issuerPublicKey, final SystemParameters syspars)
      throws ConfigurationException {
    final IssuerParametersFacade issuerParametersFacade = new IssuerParametersFacade();
    final IssuerPublicKeyWrapper ipkw = new IssuerPublicKeyWrapper(issuerPublicKey);
    final SystemParametersWrapper spw = new SystemParametersWrapper(syspars);
    
    issuerParametersFacade.setPublicKey(issuerPublicKey);
    issuerParametersFacade.setBuildingBlockId(ipkw.getPublicKeyTechnology());
    issuerParametersFacade.setImplementationVersion(ipkw.getImplementationVersion());
    issuerParametersFacade.setMaximalNumberOfAttributes(ipkw.getMaximalNumberOfAttributes());
    issuerParametersFacade.setRevocationAuthorityId(ipkw.getRevocationAuthorityId());
    issuerParametersFacade.setSystemParametersId(ipkw.getSystemParametersId());
    issuerParametersFacade.setHashAlgorithm(spw.getHashFunction());
    issuerParametersFacade.setImplementationVersion(Constants.IMPLEMENTATION_VERSION);
    if(!spw.getSystemParametersId().equals(ipkw.getSystemParametersId())) {
      throw new ConfigurationException("Incompatible system parameters and issuer public key.");
    }
    // issuer parameters ID may be overwritten
    issuerParametersFacade.setIssuerParametersId(ipkw.getPublicKeyId());

    return issuerParametersFacade;
  }

  public static IssuerParametersFacade deserialize(final String issuerParameters)
      throws SerializationException, ConfigurationException {

    final JAXBElement<?> jaxbElement = JaxbHelperClass.deserialize(issuerParameters);

    // Verify that the issuer parameters are consistent (e.g., the public key id within the key
    // and the surrounding issuer parameters or the maximal number of attributes match).
    verifyParametersConsistency(jaxbElement);

    return new IssuerParametersFacade(jaxbElement);
  }

  public String serialize() throws SerializationException {
    return JaxbHelperClass.serialize(issuerParameters);
  }

  public void setMaximalNumberOfAttributes(final int maximalNumberOfAttributes) {
    getIssuerParameters().setMaximalNumberOfAttributes(maximalNumberOfAttributes);
  }
  
  public int getMaximalNumberOfAttributes() {
    return getIssuerParameters().getMaximalNumberOfAttributes();
  }

  public void setFriendlyDescription(final List<FriendlyDescription> friendlyIssuerDescription) {
    final IssuerParameters ip = getIssuerParameters();
    ip.getFriendlyIssuerDescription().clear();
    if(friendlyIssuerDescription != null) {
      ip.getFriendlyIssuerDescription().addAll(friendlyIssuerDescription);
    }
  }
  
  public List<FriendlyDescription> getFriendlyDescription() {
    return getIssuerParameters().getFriendlyIssuerDescription();
  }
}
