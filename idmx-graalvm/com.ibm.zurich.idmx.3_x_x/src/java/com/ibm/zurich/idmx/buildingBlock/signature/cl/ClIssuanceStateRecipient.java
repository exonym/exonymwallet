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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import com.ibm.zurich.idmx.interfaces.state.IssuanceStateRecipient;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.group.GroupElement;
import com.ibm.zurich.idmx.interfaces.util.group.HiddenOrderGroupElement;

class ClIssuanceStateRecipient implements IssuanceStateRecipient {

  private static final long serialVersionUID = -8093423932880151558L;

  private BigInteger capA;
  private BigInteger e;
  private BigInteger v;
  private List<BigInteger> attributes;


  public ClIssuanceStateRecipient() {}

  public BigInt getA(BigIntFactory bf) {
    return bf.valueOf(capA);
  }

  public void setA(GroupElement<?,?,?> capA) {
    this.capA = capA.toBigInt().getValue();
  }

  public BigInt getE(BigIntFactory bf) {
    return bf.valueOf(e);
  }

  public void setE(BigInt e) {
    this.e = e.getValue();
  }

  public BigInt getV(BigIntFactory bf) {
    return bf.valueOf(v);
  }

  public void setV(BigInt v) {
    this.v = v.getValue();
  }

  public List<BigInt> getAttributes(BigIntFactory bf) {
    List<BigInt> ret = new ArrayList<>();
    for (BigInteger a : attributes) {
      ret.add(bf.valueOf(a));
    }
    return ret;
  }

  public void setAttributes(List<BigInt> attributes) {
    this.attributes = new ArrayList<>();
    for (BigInt a : attributes) {
      this.attributes.add(a.getValue());
    }
  }

}
