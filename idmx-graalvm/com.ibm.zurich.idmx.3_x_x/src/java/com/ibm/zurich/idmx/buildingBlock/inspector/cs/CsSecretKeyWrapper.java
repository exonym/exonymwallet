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
package com.ibm.zurich.idmx.buildingBlock.inspector.cs;

import com.ibm.zurich.idmix.abc4trust.AbcUriConfigurator;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.keypair.inspector.InspectorSecretKeyWrapper;

import eu.abc4trust.xml.PrivateKey;

public class CsSecretKeyWrapper extends InspectorSecretKeyWrapper {
  // Public key element names
  private static final String MODULUS_NAME = "RsaModulus";
  private static final String BASE_G = "base:G";
  private static final String Y_NAME = "base:Y";
  private static final String HASH_KEY = "hk";

  private static final String AUXILIARY_MODULUS = "AuxRsaModulus";
  private static final String AUXILIARY_BASE_G = "AuxG";
  private static final String AUXILIARY_BASE_H = "AuxH";

  private static final String X_NAME = "X";

  private static final String HASH_FUNCTION_NAME = "hashFunction";

  public CsSecretKeyWrapper(final PrivateKey secretKey) {
    super(secretKey);
  }

  public String getHashFunction() throws ConfigurationException {
    final String hashFunctionUri = (String) getParameter(HASH_FUNCTION_NAME);
    return AbcUriConfigurator.removeBasicUri(hashFunctionUri);
  }

  public void setHashFunction(final String hashFunctionName) {
    final String hashFunctionUri = AbcUriConfigurator.prependBasicUri(hashFunctionName);
    setParameter(HASH_FUNCTION_NAME, hashFunctionUri);
  }
  
  public void setModulus(final BigInt parameterValue) {
    setParameter(MODULUS_NAME, parameterValue);
  }

  public BigInt getModulus() throws ConfigurationException {
    return (BigInt) getParameter(MODULUS_NAME);
  }

  private void setY(final BigInt parameterValue, final int i) {
    setParameter(Y_NAME + i, parameterValue);
  }

  private BigInt getY(int i) throws ConfigurationException {
    return (BigInt) getParameter(Y_NAME + i);
  }

  public void setY1(final BigInt parameterValue) {
    setY(parameterValue, 1);
  }

  public void setY2(final BigInt parameterValue) {
    setY(parameterValue, 2);
  }

  public void setY3(final BigInt parameterValue) {
    setY(parameterValue, 3);
  }

  public BigInt getY1() throws ConfigurationException {
    return getY(1);
  }

  public BigInt getY2() throws ConfigurationException {
    return getY(2);
  }

  public BigInt getY3() throws ConfigurationException {
    return getY(3);
  }

  public void setHashKey(final BigInt parameterValue) {
    setParameter(HASH_KEY, parameterValue);
  }

  public BigInt getHashKey() throws ConfigurationException {
    return (BigInt) getParameter(HASH_KEY);
  }

  public void setG(final BigInt parameterValue) {
    setParameter(BASE_G, parameterValue);
  }

  public BigInt getG() throws ConfigurationException {
    return (BigInt) getParameter(BASE_G);
  }

  public BigInt getH() throws ConfigurationException {
    final BigInt n = getModulus();
    final BigIntFactory bigIntFactory = n.getFactory();
    return n.add(bigIntFactory.one());
  }

  public BigInt getN2() throws ConfigurationException {
    final BigInt n = getModulus();
    return n.pow(2);
  }

  public void setAuxiliaryModulus(final BigInt parameterValue) {
    setParameter(AUXILIARY_MODULUS, parameterValue);
  }

  public BigInt getAuxiliaryModulus() throws ConfigurationException {
    return (BigInt) getParameter(AUXILIARY_MODULUS);
  }

  public void setAuxiliaryG(final BigInt parameterValue) {
    setParameter(AUXILIARY_BASE_G, parameterValue);
  }

  public BigInt getAuxiliaryG() throws ConfigurationException {
    return (BigInt) getParameter(AUXILIARY_BASE_G);
  }

  public void setAuxiliaryH(final BigInt parameterValue) {
    setParameter(AUXILIARY_BASE_H, parameterValue);
  }

  public BigInt getAuxiliaryH() throws ConfigurationException {
    return (BigInt) getParameter(AUXILIARY_BASE_H);
  }

  private void setX(final BigInt parameterValue, final int i) {
    setParameter(X_NAME + i, parameterValue);
  }

  private BigInt getX(final int i) throws ConfigurationException {
    return (BigInt) getParameter(X_NAME + i);
  }

  public void setX1(final BigInt parameterValue) {
    setX(parameterValue, 1);
  }

  public void setX2(final BigInt parameterValue) {
    setX(parameterValue, 2);
  }

  public void setX3(final BigInt parameterValue) {
    setX(parameterValue, 3);
  }

  public BigInt getX1() throws ConfigurationException {
    return getX(1);
  }

  public BigInt getX2() throws ConfigurationException {
    return getX(2);
  }

  public BigInt getX3() throws ConfigurationException {
    return getX(3);
  }

}
