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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import com.ibm.zurich.idmx.interfaces.state.IssuanceStateIssuer;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;

class BrandsIssuanceStateIssuer implements IssuanceStateIssuer {
  private static final long serialVersionUID = -3382415531426554633L;
  
  private final List<BigInteger> listOfW;
  private final BigInteger q;
  private final BigInteger y0;
  private boolean destroyed;

  public BrandsIssuanceStateIssuer(final List<BigInt> listOfW, final BigInt q, final BigInt y0) {
    this.listOfW = new ArrayList<>();
    for(final BigInt w: listOfW) {
      this.listOfW.add(w.getValue());
    }
    this.q = q.getValue();
    this.y0 = y0.getValue();
    this.destroyed = false;
  }

  public List<BigInt> computeSigmaR(List<BigInt> sigmaC, BigIntFactory bf) {

    if (this.destroyed) {
      throw new RuntimeException("Cannot call computeSigmaR() on UProverIssuanceStateIssuer twice");
    }
    if (sigmaC.size() != listOfW.size()) {
      throw new RuntimeException("Incorrect size of sigmaC in Issuance State Uprove");
    }

    final List<BigInt> listOfSigmaR = new ArrayList<BigInt>();
    for (int i = 0; i < sigmaC.size(); ++i) {
      BigInt sigmaR = computeSigmaR(sigmaC.get(i), i, bf);
      listOfSigmaR.add(sigmaR);
    }

    this.destroyed = true;
    return listOfSigmaR;
  }

  private BigInt computeSigmaR(final BigInt sigmaC, final int i, final BigIntFactory bf) {
    final BigInt w = bf.valueOf(listOfW.get(i));
    final BigInt y0 = bf.valueOf(this.y0);
    final BigInt q = bf.valueOf(this.q);
    // sigmaR = sigmaC * y0 + w (mod q)
    return sigmaC.multiply(y0).add(w).mod(q);
  }
}
