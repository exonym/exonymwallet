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

package com.ibm.zurich.idmx.buildingBlock.signature.uprove;

import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.jaxb.wrapper.SignatureTokenWrapper;

import eu.abc4trust.xml.SignatureToken;

class BrandsSignatureTokenWrapper extends SignatureTokenWrapper {

  public BrandsSignatureTokenWrapper() {
    super();
  }

  public BrandsSignatureTokenWrapper(final SignatureToken signatureToken) {
    super(signatureToken);
  }

  private static final String LABEL_SIGMA_Z_PRIME = "sigmaZPrime";

  public void setSigmaZPrime(final BigInt signaZPrime) {
    setParameter(LABEL_SIGMA_Z_PRIME, signaZPrime);
  }

  public BigInt getSigmaZPrime() throws ConfigurationException {
    return (BigInt) getParameter(LABEL_SIGMA_Z_PRIME);
  }

  private static final String LABEL_SIGMA_C_PRIME = "sigmaCPrime";

  public void setSigmaCPrime(final BigInt signaCPrime) {
    setParameter(LABEL_SIGMA_C_PRIME, signaCPrime);
  }

  public BigInt getSigmaCPrime() throws ConfigurationException {
    return (BigInt) getParameter(LABEL_SIGMA_C_PRIME);
  }

  private static final String LABEL_SIGMA_R_PRIME = "sigmaRPrime";

  public void setSigmaRPrime(final BigInt signaRPrime) {
    setParameter(LABEL_SIGMA_R_PRIME, signaRPrime);
  }

  public BigInt getSigmaRPrime() throws ConfigurationException {
    return (BigInt) getParameter(LABEL_SIGMA_R_PRIME);
  }

  private static final String LABEL_H = "h";

  public void setH(final BigInt h) {
    setParameter(LABEL_H, h);
  }

  public BigInt getH() throws ConfigurationException {
    return (BigInt) getParameter(LABEL_H);
  }

  private static final String LABEL_ALPHA_INVERSE = "alphaInverse";

  public void setAlphaInverse(final BigInt alphaInverse) {
    setParameter(LABEL_ALPHA_INVERSE, alphaInverse);
  }

  public BigInt getAlphaInverse() throws ConfigurationException {
    return (BigInt) getParameter(LABEL_ALPHA_INVERSE);
  }
  
  private static final String LABEL_PROVER_INFORMATION = "PI";

  public void setProverInformation(BigInt pi) {
    setParameter(LABEL_PROVER_INFORMATION, pi);
  }
  
  public boolean hasProverInformation() {
    return hasParameter(LABEL_PROVER_INFORMATION);
  }

  public BigInt getProverInformation() throws ConfigurationException {
    return (BigInt) getParameter(LABEL_PROVER_INFORMATION);
  }

}
