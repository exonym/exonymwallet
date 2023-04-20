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

import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.SerializationException;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.jaxb.JaxbHelperClass;
import com.ibm.zurich.idmx.jaxb.ParameterListHelper;

import eu.abc4trust.xml.FriendlyDescription;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PublicKey;

/**
 * 
 */
public abstract class PublicKeyWrapper extends KeyWrapper {

  protected final PublicKey publicKey;

  public PublicKeyWrapper(final PublicKey publicKey) {
    super();
    this.publicKey = publicKey;
    this.listOfParameters = getPublicKey().getParameter();
    this.parameterListHelper = new ParameterListHelper(getPublicKey().getParameter());
  }

  public String getImplementationVersion() {
    return getPublicKey().getVersion();
  }

  public void setImplementationVersion(final String implementationVersion) {
    getPublicKey().setVersion(implementationVersion);
  }

  public URI getSystemParametersId() {
    return getPublicKey().getSystemParametersId();
  }

  public void setSystemParametersId(final URI systemParametersId) {
    getPublicKey().setSystemParametersId(systemParametersId);
  }

  @Override
  public URI getPublicKeyId() {
    return getPublicKey().getPublicKeyId();
  }

  @Override
  public void setPublicKeyId(final URI publicKeyId) {
    getPublicKey().setPublicKeyId(publicKeyId);
  }

  public URI getPublicKeyTechnology() {
    return getPublicKey().getTechnology();
  }

  public void setPublicKeyTechnology(final URI technology) {
    getPublicKey().setTechnology(technology);
  }

  public void setFriendlyDescriptions(final List<FriendlyDescription> listOfFriendlyDescriptions) {
    getPublicKey().getFriendlyDescription().addAll(listOfFriendlyDescriptions);
  }

  public PublicKey getPublicKey() {
    return publicKey;
  }

  public abstract String getAttributeBaseIdentifier(final int i);

  public BigInt getBase(final int i) throws ConfigurationException {
    return (BigInt) getParameter(getAttributeBaseIdentifier(i));
  }

  public void setBase(final int i, final BigInt baseValue) throws ConfigurationException {
    setParameter(getAttributeBaseIdentifier(i), baseValue);
  }

  public String serialize() throws SerializationException {
    return JaxbHelperClass.serialize(new ObjectFactory().createPublicKey(publicKey));
  }
  
  /**
   * Computes a cryptographic hash of the public key
   * 
   * @throws ConfigurationException
   */
  public final byte[] getHash(final String hashAlgorithm) throws ConfigurationException {
    try {
      final byte[] toHash = JaxbHelperClass.canonicalXml(new ObjectFactory().createPublicKey(publicKey));
      final MessageDigest md = MessageDigest.getInstance(hashAlgorithm);
      md.update(toHash);
      return md.digest();
    } catch (final SerializationException e) {
      throw new RuntimeException(e);
    } catch (final NoSuchAlgorithmException e) {
      throw new ConfigurationException(e);
    }
  }
}
