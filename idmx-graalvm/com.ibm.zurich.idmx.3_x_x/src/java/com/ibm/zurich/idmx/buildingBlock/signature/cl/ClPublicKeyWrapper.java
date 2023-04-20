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
import com.ibm.zurich.idmx.keypair.issuer.IssuerPublicKeyWrapper;

import eu.abc4trust.xml.PublicKey;

public class ClPublicKeyWrapper extends IssuerPublicKeyWrapper {

  // Public key element names
  private static final String MODULUS_NAME = "rsaModulus";
  private static final String BASE_Z_NAME = "base:Z";
  private static final String BASE_S_NAME = "base:S";
  private static final String BASE_CRED_SPEC = "base:t";
  private static final String BASE_DEVICE = "base:d";

  public ClPublicKeyWrapper(PublicKey publicKey) {
    super(publicKey);
  }


  public void setModulus(BigInt parameterValue) {
    setParameter(MODULUS_NAME, parameterValue);
  }

  public BigInt getModulus() throws ConfigurationException {
    return (BigInt) getParameter(MODULUS_NAME);
  }

  public void setZ(BigInt parameterValue) {
    setParameter(BASE_Z_NAME, parameterValue);
  }

  public BigInt getZ() throws ConfigurationException {
    return (BigInt) getParameter(BASE_Z_NAME);
  }

  public void setS(BigInt parameterValue) {
    setParameter(BASE_S_NAME, parameterValue);
  }

  public BigInt getS() throws ConfigurationException {
    return (BigInt) getParameter(BASE_S_NAME);
  }

  public BigInt getRt() throws ConfigurationException {
    return (BigInt) getParameter(BASE_CRED_SPEC);
  }

  public void setRt(BigInt parameterValue) throws ConfigurationException {
    setParameter(BASE_CRED_SPEC, parameterValue);
  }

  public BigInt getRd() throws ConfigurationException {
    return (BigInt) getParameter(BASE_DEVICE);
  }

  public void setRd(BigInt parameterValue) throws ConfigurationException {
    setParameter(BASE_DEVICE, parameterValue);
  }

}
