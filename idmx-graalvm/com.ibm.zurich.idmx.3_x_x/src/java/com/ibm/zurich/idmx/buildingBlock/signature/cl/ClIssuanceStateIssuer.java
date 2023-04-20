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

import java.util.List;

import com.ibm.zurich.idmx.interfaces.state.IssuanceStateIssuer;
import com.ibm.zurich.idmx.interfaces.util.BigInt;

class ClIssuanceStateIssuer implements IssuanceStateIssuer {
  /**
   * 
   */
  private static final long serialVersionUID = 2304730733408041742L;
  @SuppressWarnings("unused")
  private final List<BigInt> privateAttributeValues;
  private final BigInt vPrime;
  @SuppressWarnings("unused")
  private boolean destroyed;

  public ClIssuanceStateIssuer(List<BigInt> privateAttributeValues, BigInt vPrime) {
    this.privateAttributeValues = privateAttributeValues;
    this.vPrime = vPrime;
    this.destroyed = false;
  }

  public BigInt getVPrime() {
    return vPrime;
  }

  // public List<BigInt> computeU() {
  // if (this.destroyed) {
  // throw new RuntimeException("Cannot call computeSigmaR() on ClIssuanceStateIssuer twice");
  // }
  //
  //
  // List<BigInt> listOfSigmaR = new ArrayList<BigInt>();
  // for (int i = 0; i < sigmaC.size(); ++i) {
  // BigInt sigmaR = computeSigmaR(sigmaC.get(i), i);
  // listOfSigmaR.add(sigmaR);
  // }
  //
  // this.destroyed = true;
  // return listOfSigmaR;
  // }
  //
  // private BigInt computeSigmaR(BigInt sigmaC, int i) {
  // BigInt w = listOfW.get(i);
  // // sigmaR = sigmaC * y0 + w (mod q)
  // return sigmaC.multiply(y0).add(w).mod(q);
  // }
}
