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

package com.ibm.zurich.idmx.keypair.ra;

import java.io.InputStream;

import javax.xml.bind.JAXBElement;

import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.SerializationException;
import com.ibm.zurich.idmx.jaxb.JaxbHelperClass;
import com.ibm.zurich.idmx.keypair.KeyPairWrapper;
import com.ibm.zurich.idmx.keypair.PublicKeyWrapper;
import com.ibm.zurich.idmx.keypair.SecretKeyWrapper;

import eu.abc4trust.xml.KeyPair;
import eu.abc4trust.xml.ObjectFactory;

/**
 * 
 */
public class RevocationAuthorityKeyPairWrapper extends KeyPairWrapper {

  public RevocationAuthorityKeyPairWrapper() {
    super(new ObjectFactory().createPrivateKey(), new ObjectFactory().createPublicKey());
  }

  public RevocationAuthorityKeyPairWrapper(final JAXBElement<?> keyPair) throws SerializationException,
      ConfigurationException {
    super(keyPair);
  }

  public RevocationAuthorityKeyPairWrapper(final KeyPair raKeyPair) throws ConfigurationException {
    initKeyPair(raKeyPair);
  }

  @Override
  protected void initKeyPair(final KeyPair keyPair) throws ConfigurationException {
    super.initKeyPair(keyPair);
  }

  public static KeyPairWrapper deserialize(final String keyPair) throws SerializationException,
      ConfigurationException {
    return new RevocationAuthorityKeyPairWrapper(JaxbHelperClass.deserialize(keyPair));
  }

  public static KeyPairWrapper deserialize(final InputStream inputStream) throws SerializationException,
      ConfigurationException {
    return new RevocationAuthorityKeyPairWrapper(JaxbHelperClass.deserialize(inputStream));
  }

  @Override
  public SecretKeyWrapper getSecretKeyWrapper() {
    return new RevocationAuthoritySecretKeyWrapper(getSecretKey());
  }

  @Override
  public PublicKeyWrapper getPublicKeyWrapper() {
    return new RevocationAuthorityPublicKeyWrapper(getPublicKey());
  }

  public RevocationAuthoritySecretKeyWrapper getRevocationAuthorityPrivateKeyFacade() {
    return (RevocationAuthoritySecretKeyWrapper) this.getSecretKeyWrapper();
  }

  public RevocationAuthorityPublicKeyWrapper getRevocationAuthorityPublicKeyWrapper() {
    return (RevocationAuthorityPublicKeyWrapper) this.getPublicKeyWrapper();
  }
}
