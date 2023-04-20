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
import com.ibm.zurich.idmx.configuration.ErrorMessages;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.SerializationException;
import com.ibm.zurich.idmx.interfaces.configuration.Constants;
import com.ibm.zurich.idmx.jaxb.JaxbHelperClass;

import eu.abc4trust.xml.CryptoParams;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PrivateKey;
import eu.abc4trust.xml.SecretKey;

/**
 * 
 */
public class Abc4TrustSecretKeyFacade implements Constants {

  // Delegatee
  private JAXBElement<SecretKey> secretKey;



  public Abc4TrustSecretKeyFacade() {
    this(new ObjectFactory().createSecretKey());
  }

  public Abc4TrustSecretKeyFacade(final SecretKey sk) {
    this.secretKey = new ObjectFactory().createIssuerSecretKey(sk);
  }

  private Abc4TrustSecretKeyFacade(final JAXBElement<?> jaxbElement) throws SerializationException {
    this.secretKey = verifyTypeOfJaxbElement(jaxbElement);
  }

  public URI getKeyId() throws ConfigurationException {
    return getSecretKey().getSecretKeyUID();
  }

  public void setKeyId(final URI keyId) {
    getSecretKey().setSecretKeyUID(keyId);
  }

  public void setPrivateKey(final PrivateKey privateKey) throws ConfigurationException {
    final ObjectFactory objectFactory = new ObjectFactory();
    final CryptoParams cryptoParameters = objectFactory.createCryptoParams();
    cryptoParameters.getContent().add(objectFactory.createPrivateKey(privateKey));
    getSecretKey().setCryptoParams(cryptoParameters);
  }

  public PrivateKey getPrivateKey() {
//    Object publicKeyObject =
//    JAXBIntrospector.getValue(getSecretKey().getCryptoParams().getContent().get(0));
	  XmlUtils.fixNestedContent(getSecretKey().getCryptoParams());
	  final Object publicKeyObject =
      getSecretKey().getCryptoParams().getContent().get(0);
    if (PrivateKey.class.isAssignableFrom(publicKeyObject.getClass())) {
      return (PrivateKey) publicKeyObject;
    } else {
      return null;
    }
  }


  public SecretKey getSecretKey() {
    return (SecretKey) JAXBIntrospector.getValue(secretKey);
  }

  /**
   * @param jaxbElement
   * @throws SerializationException
   */
  @SuppressWarnings("unchecked")
  private static JAXBElement<SecretKey> verifyTypeOfJaxbElement(final JAXBElement<?> jaxbElement)
      throws SerializationException {
    final Class<?> delegateeClass = IssuerParameters.class;
    if (delegateeClass.isAssignableFrom(jaxbElement.getDeclaredType())) {
      return (JAXBElement<SecretKey>) jaxbElement;
    } else {
      throw new SerializationException(ErrorMessages.malformedClass(delegateeClass.getSimpleName()));
    }
  }
 
  public static Abc4TrustSecretKeyFacade deserialize(final String xml)
      throws SerializationException, ConfigurationException {
    final JAXBElement<?> jaxbElement = JaxbHelperClass.deserialize(xml);
    return new Abc4TrustSecretKeyFacade(jaxbElement);
  }

  public String serialize() throws SerializationException {
    return JaxbHelperClass.serialize(secretKey);
  }

}
