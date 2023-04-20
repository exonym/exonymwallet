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
package com.ibm.zurich.idmx.keypair.inspector;

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

public class InspectorKeyPairWrapper extends KeyPairWrapper {
  public InspectorKeyPairWrapper() {
    super(new ObjectFactory().createPrivateKey(), new ObjectFactory().createPublicKey());
  }

  public InspectorKeyPairWrapper(final JAXBElement<?> keyPair) throws SerializationException,
      ConfigurationException {
    super(keyPair);
  }

  public InspectorKeyPairWrapper(final KeyPair inspectorKeyPair) throws ConfigurationException {
    initKeyPair(inspectorKeyPair);
  }

  public static KeyPairWrapper deserialize(final String keyPair) throws SerializationException,
      ConfigurationException {
    return new InspectorKeyPairWrapper(JaxbHelperClass.deserialize(keyPair));
  }

  public static KeyPairWrapper deserialize(final InputStream inputStream) throws SerializationException,
      ConfigurationException {
    return new InspectorKeyPairWrapper(JaxbHelperClass.deserialize(inputStream));
  }

  public InspectorSecretKeyWrapper getIssuerPrivateKeyFacade() {
    return (InspectorSecretKeyWrapper) this.getSecretKeyWrapper();
  }

  public InspectorPublicKeyWrapper getIssuerPublicKeyWrapper() {
    return (InspectorPublicKeyWrapper) this.getPublicKeyWrapper();
  }

  @Override
  public SecretKeyWrapper getSecretKeyWrapper() {
    return new InspectorSecretKeyWrapper(getSecretKey());
  }

  @Override
  public PublicKeyWrapper getPublicKeyWrapper() {
    return new InspectorPublicKeyWrapper(getPublicKey());
  }

  public InspectorSecretKeyWrapper getInspectorPrivateKeyWrapper() {
    return (InspectorSecretKeyWrapper) this.getSecretKeyWrapper();
  }

  public InspectorPublicKeyWrapper getInspectorPublicKeyWrapper() {
    return (InspectorPublicKeyWrapper) this.getPublicKeyWrapper();
  }
}
