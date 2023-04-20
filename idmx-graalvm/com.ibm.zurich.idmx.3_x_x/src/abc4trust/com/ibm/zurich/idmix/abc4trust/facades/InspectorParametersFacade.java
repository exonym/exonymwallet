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
import com.ibm.zurich.idmx.keypair.inspector.InspectorPublicKeyWrapper;
import com.ibm.zurich.idmx.parameters.system.SystemParametersWrapper;

import eu.abc4trust.xml.CryptoParams;
import eu.abc4trust.xml.FriendlyDescription;
import eu.abc4trust.xml.InspectorPublicKey;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PublicKey;
import eu.abc4trust.xml.SystemParameters;

/**
 * 
 */
public class InspectorParametersFacade implements Constants {

  // Delegatee
  private final JAXBElement<InspectorPublicKey> issuerParameters;



  public InspectorParametersFacade() {
    this(new ObjectFactory().createInspectorPublicKey());
  }

  public InspectorParametersFacade(final InspectorPublicKey issuerParameters) {
    this.issuerParameters = new ObjectFactory().createInspectorPublicKey(issuerParameters);
  }

  private InspectorParametersFacade(final JAXBElement<?> jaxbElement) throws SerializationException {
    this.issuerParameters = verifyTypeOfJaxbElement(jaxbElement);
  }

  public String getImplementationVersion() {
    return getInspectorParameters().getVersion();
  }

  public void setImplementationVersion(final String version) {
    getInspectorParameters().setVersion(version);
  }
  
  public String getHashAlgorithm() {
    return getInspectorParameters().getHashAlgorithm().toString();
  }

  public void setHashAlgorithm(final String hashAlgorithm) {
    getInspectorParameters().setHashAlgorithm(URI.create(hashAlgorithm));
  }

  public URI getInspectorId() throws ConfigurationException {
    return getInspectorParameters().getPublicKeyUID();
  }

  public void setInspectorId(final URI issuerParametersId) {
    getInspectorParameters().setPublicKeyUID(issuerParametersId);
  }

  public URI getBuildingBlockId() {
    return getInspectorParameters().getAlgorithmID();
  }

  public void setBuildingBlockId(final URI publicKeyTechnology) {
    getInspectorParameters().setAlgorithmID(publicKeyTechnology);
  }

  public void setPublicKey(PublicKey publicKey) {
    final ObjectFactory objectFactory = new ObjectFactory();
    final CryptoParams cryptoParameters = objectFactory.createCryptoParams();
    cryptoParameters.getContent().add(objectFactory.createPublicKey(publicKey));
    getInspectorParameters().setCryptoParams(cryptoParameters);
  }

  public PublicKey getPublicKey() {
//	    Object publicKeyObject =
//	            JAXBIntrospector.getValue(getInspectorParameters().getCryptoParams().getContent().get(0));
	  XmlUtils.fixNestedContent(getInspectorParameters().getCryptoParams());
	  final Object publicKeyObject =
	            getInspectorParameters().getCryptoParams().getContent().get(0);
    if (PublicKey.class.isAssignableFrom(publicKeyObject.getClass())) {
      return (PublicKey) publicKeyObject;
    } else {
      return null;
    }
  }


  public InspectorPublicKey getInspectorParameters() {
    return (InspectorPublicKey) JAXBIntrospector.getValue(issuerParameters);
  }

  /**
   * @param jaxbElement
   * @throws SerializationException
   */
  @SuppressWarnings("unchecked")
  private static JAXBElement<InspectorPublicKey> verifyTypeOfJaxbElement(final JAXBElement<?> jaxbElement)
      throws SerializationException {
    final Class<?> delegateeClass = InspectorPublicKey.class;
    if (delegateeClass.isAssignableFrom(jaxbElement.getDeclaredType())) {
      return (JAXBElement<InspectorPublicKey>) jaxbElement;
    } else {
      throw new SerializationException(ErrorMessages.malformedClass(delegateeClass.getSimpleName()));
    }
  }

  public static InspectorParametersFacade initInspectorParameters(final PublicKey inspectorKey, final SystemParameters syspars)
      throws ConfigurationException {
    final InspectorParametersFacade inspectorParametersFacade = new InspectorParametersFacade();
    final InspectorPublicKeyWrapper ipkw = new InspectorPublicKeyWrapper(inspectorKey);
    final SystemParametersWrapper spw = new SystemParametersWrapper(syspars);
    
    inspectorParametersFacade.setPublicKey(inspectorKey);
    inspectorParametersFacade.setBuildingBlockId(ipkw.getPublicKeyTechnology());
    inspectorParametersFacade.setImplementationVersion(ipkw.getImplementationVersion());
    inspectorParametersFacade.setHashAlgorithm(spw.getHashFunction());
    inspectorParametersFacade.setImplementationVersion(Constants.IMPLEMENTATION_VERSION);
    if(! spw.getSystemParametersId().equals(ipkw.getSystemParametersId())) {
      throw new ConfigurationException("Incompatible system parameters and issuer public key.");
    }
    // issuer parameters ID may be overwritten
    inspectorParametersFacade.setInspectorId(ipkw.getPublicKeyId());

    return inspectorParametersFacade;
  }

  public static InspectorParametersFacade deserialize(final String issuerParameters)
      throws SerializationException, ConfigurationException {

    final JAXBElement<?> jaxbElement = JaxbHelperClass.deserialize(issuerParameters);

    return new InspectorParametersFacade(jaxbElement);
  }

  public String serialize() throws SerializationException {
    return JaxbHelperClass.serialize(issuerParameters);
  }

  public void setFriendlyDescription(final List<FriendlyDescription> friendlyIssuerDescription) {
    final InspectorPublicKey ip = getInspectorParameters();
    ip.getFriendlyInspectorDescription().clear();
    if(friendlyIssuerDescription != null) {
      ip.getFriendlyInspectorDescription().addAll(friendlyIssuerDescription);
    }
  }
  
  public List<FriendlyDescription> getFriendlyDescription() {
    return getInspectorParameters().getFriendlyInspectorDescription();
  }
}
