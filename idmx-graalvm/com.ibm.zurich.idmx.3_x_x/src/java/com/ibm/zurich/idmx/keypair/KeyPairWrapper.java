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

package com.ibm.zurich.idmx.keypair;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBIntrospector;

import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.SerializationException;
import com.ibm.zurich.idmx.interfaces.keypair.KeyPairWrapperInterface;
import com.ibm.zurich.idmx.jaxb.JaxbHelperClass;

import eu.abc4trust.xml.KeyPair;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PrivateKey;
import eu.abc4trust.xml.PublicKey;

/**
 * 
 */
public abstract class KeyPairWrapper implements KeyPairWrapperInterface {

  protected JAXBElement<KeyPair> keyPair;

  public KeyPairWrapper(final PrivateKey privateKey, final PublicKey publicKey) {
    final ObjectFactory objectFactory = new ObjectFactory();
    keyPair = objectFactory.createKeyPair(objectFactory.createKeyPair());
    getKeyPair().setPrivateKey(privateKey);
    getKeyPair().setPublicKey(publicKey);
  }

  public KeyPairWrapper() {
    this(new ObjectFactory().createKeyPair());
  }

  public KeyPairWrapper(final KeyPair keyPair) {
    this.keyPair = new ObjectFactory().createKeyPair(keyPair);
  }

  @SuppressWarnings("unchecked")
  protected KeyPairWrapper(final JAXBElement<?> jaxbElement) throws SerializationException {

    final Class<?> delegateeClass = KeyPair.class;
    if (delegateeClass.isAssignableFrom(jaxbElement.getDeclaredType())) {
      this.keyPair = (JAXBElement<KeyPair>) jaxbElement;
    } else {
      throw new SerializationException("Idmx: " + delegateeClass.getSimpleName() + " is malformed.");
    }
  }

  protected void initKeyPair(KeyPair keyPair) throws ConfigurationException {
    this.keyPair = new ObjectFactory().createKeyPair(keyPair);
  }

  public KeyPair getKeyPair() {
    return (KeyPair) JAXBIntrospector.getValue(keyPair);
  }

  protected PublicKey getPublicKey() {
    return getKeyPair().getPublicKey();
  }

  protected PrivateKey getSecretKey() {
    return getKeyPair().getPrivateKey();
  }

  @Override
public abstract SecretKeyWrapper getSecretKeyWrapper();

  @Override
public abstract PublicKeyWrapper getPublicKeyWrapper();

  @Override
  public String serialize() throws SerializationException {
    return JaxbHelperClass.serialize(keyPair);
  }
}
