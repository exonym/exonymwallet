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
package com.ibm.zurich.idmx.util.group;

import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.group.KnownOrderGroup;
import com.ibm.zurich.idmx.interfaces.util.group.KnownOrderGroupElement;

public class ResiduesModpqGroupElement implements KnownOrderGroupElement {
  private ResiduesModpqGroup group;
  private BigInt val;
  private BigInt mod;

  public ResiduesModpqGroupElement(final ResiduesModpqGroup group, final BigInt val) {
    this.group = group;
    this.val = val;
    this.mod = group.getModulus();
  }

  @Override
  public KnownOrderGroupElement op(final KnownOrderGroupElement ge) {
    if (ge instanceof ResiduesModpqGroupElement) {
      final ResiduesModpqGroupElement ge1 = (ResiduesModpqGroupElement) ge;
      BigInt result = val.multiply(ge1.val).mod(mod);
      return new ResiduesModpqGroupElement(group, result);
    } else {
      throw new ArithmeticException("Cannot multiply incompatible group elements.");
    }
  }

  @Override
  public KnownOrderGroupElement multOp(final BigInt iterationCounter) {
    BigInt result;
    result = val.modPow(iterationCounter, mod);
    return new ResiduesModpqGroupElement(group, result);
  }

  @Override
  public KnownOrderGroupElement opMultOp(final KnownOrderGroupElement base, final BigInt exponent) {
    return this.op(base.multOp(exponent));
  }

  @Override
  public KnownOrderGroup getGroup() {
    return group;
  }

  @Override
  public KnownOrderGroupElement invert() {
	final BigInt inv = val.modInverse(mod);
    return new ResiduesModpqGroupElement(group, inv);
  }

  @Override
  public BigInt getDiscreteLog(final KnownOrderGroupElement base) {
    throw new RuntimeException("Not implemented.");
  }

  @Override
  public BigInt toBigInt() {
    return val;
  }

  @Override
  public byte[] serialize() {
    throw new RuntimeException("Not implemented.");
  }

}
