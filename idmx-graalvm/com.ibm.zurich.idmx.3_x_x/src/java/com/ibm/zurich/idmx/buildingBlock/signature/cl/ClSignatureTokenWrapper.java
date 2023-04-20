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
import com.ibm.zurich.idmx.jaxb.wrapper.SignatureTokenWrapper;

import eu.abc4trust.xml.SignatureToken;

class ClSignatureTokenWrapper extends SignatureTokenWrapper {

  private static final String CAP_A_NAME = "A";
  private static final String V_NAME = "v";
  private static final String E_NAME = "e";


  public ClSignatureTokenWrapper() {
    super();
  }

  public ClSignatureTokenWrapper(SignatureToken signatureToken) {
    super(signatureToken);
  }


  public BigInt getA() throws ConfigurationException {
    return (BigInt) getParameter(CAP_A_NAME);
  }

  public void setA(BigInt capA) {
    setParameter(CAP_A_NAME, capA);
  }

  public BigInt getE() throws ConfigurationException {
    return (BigInt) getParameter(E_NAME);
  }

  public void setE(BigInt e) {
    setParameter(E_NAME, e);
  }

  public BigInt getV() throws ConfigurationException {
    return (BigInt) getParameter(V_NAME);
  }

  public void setV(BigInt v) {
    setParameter(V_NAME, v);
  }

}
