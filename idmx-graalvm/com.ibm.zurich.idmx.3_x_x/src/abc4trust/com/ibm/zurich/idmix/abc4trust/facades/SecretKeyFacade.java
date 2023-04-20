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

import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.util.UriUtils;

import eu.abc4trust.xml.CryptoParams;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PrivateKey;
import eu.abc4trust.xml.SecretKey;
//import javax.xml.bind.JAXBIntrospector;
import com.ibm.zurich.idmix.abc4trust.XmlUtils;

/**
 * 
 */
public class SecretKeyFacade {

  // Delegatee
  private SecretKey secretKey;



  public SecretKeyFacade() {
    secretKey = new ObjectFactory().createSecretKey();
  }

  public SecretKeyFacade(final SecretKey secretKey) {
    this.secretKey = secretKey;
  }

  public static SecretKeyFacade initSecretKey(final URI issuerPublicKeyId, final PrivateKey issuerPrivateKey)
      throws ConfigurationException {
    final SecretKeyFacade secretKeyFacade = new SecretKeyFacade();

    secretKeyFacade.setSecretKeyUID(UriUtils.concat(issuerPublicKeyId, "secretKey"));
    secretKeyFacade.setPrivateKey(issuerPrivateKey);
    return secretKeyFacade;
  }

  private void setPrivateKey(final PrivateKey issuerPrivateKey) {
    final CryptoParams cryptoParams = new ObjectFactory().createCryptoParams();
    cryptoParams.getContent().add(issuerPrivateKey);
    secretKey.setCryptoParams(cryptoParams);
  }

  public PrivateKey getPrivateKey() {
//    Object privateKeyObject =
//        JAXBIntrospector.getValue(getSecretKey().getCryptoParams().getContent().get(0));
	  XmlUtils.fixNestedContent(getSecretKey().getCryptoParams());
	  final Object privateKeyObject =
      getSecretKey().getCryptoParams().getContent().get(0);
    if (PrivateKey.class.isAssignableFrom(privateKeyObject.getClass())) {
      return (PrivateKey) privateKeyObject;
    } else {
      return null;
      // throw new SerializationException(
      // ErrorMessages.malformedClass(SecretKey.class.getSimpleName()));
    }
  }

  private void setSecretKeyUID(URI secretKeyUID) {
    getSecretKey().setSecretKeyUID(secretKeyUID);
  }

  public SecretKey getSecretKey() {
    return secretKey;
  }
}
