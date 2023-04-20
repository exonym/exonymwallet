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

package com.ibm.zurich.idmx.buildingBlock.signature.cl;

import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.keypair.issuer.IssuerSecretKeyWrapper;

import eu.abc4trust.xml.PrivateKey;

public class ClSecretKeyWrapper extends IssuerSecretKeyWrapper {

  // Private key elements
  private static final String MODULUS_NAME = "rsaModulus";
  private static final String SAFE_PRIME_P_NAME = "p";
  private static final String SAFE_PRIME_Q_NAME = "q";
  private static final String SOPHIE_GERMAIN_PRMIE_P_NAME = "pPrime";
  private static final String SOPHIE_GERMAIN_PRMIE_Q_NAME = "qPrime";

  public ClSecretKeyWrapper(PrivateKey secretKey) {
    super(secretKey);
  }

  public void setModulus(BigInt parameterValue) {
    setParameter(MODULUS_NAME, parameterValue);
  }

  public BigInt getModulus() throws ConfigurationException {
    return (BigInt) getParameter(MODULUS_NAME);
  }

  public void setSafePrimeP(BigInt parameterValue) {
    setParameter(SAFE_PRIME_P_NAME, parameterValue);
  }

  public BigInt getSafePrimeP() throws ConfigurationException {
    return (BigInt) getParameter(SAFE_PRIME_P_NAME);
  }

  public void setSafePrimeQ(BigInt parameterValue) {
    setParameter(SAFE_PRIME_Q_NAME, parameterValue);
  }

  public BigInt getSafePrimeQ() throws ConfigurationException {
    return (BigInt) getParameter(SAFE_PRIME_Q_NAME);
  }

  public void setSophieGermainPrimeP(BigInt parameterValue) {
    setParameter(SOPHIE_GERMAIN_PRMIE_P_NAME, parameterValue);
  }

  public BigInt getSophieGermainPrimeP() throws ConfigurationException {
    return (BigInt) getParameter(SOPHIE_GERMAIN_PRMIE_P_NAME);
  }

  public void setSophieGermainPrimeQ(BigInt parameterValue) {
    setParameter(SOPHIE_GERMAIN_PRMIE_Q_NAME, parameterValue);
  }

  public BigInt getSophieGermainPrimeQ() throws ConfigurationException {
    return (BigInt) getParameter(SOPHIE_GERMAIN_PRMIE_Q_NAME);
  }
}
