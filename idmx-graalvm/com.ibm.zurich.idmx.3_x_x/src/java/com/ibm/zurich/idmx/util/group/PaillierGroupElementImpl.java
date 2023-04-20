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
import com.ibm.zurich.idmx.interfaces.util.group.PaillierGroupElement;

// TODO package

public class PaillierGroupElementImpl implements PaillierGroupElement {

  private final BigInt val;
  private final PaillierGroupImpl group;
  private final BigInt mod;

  public PaillierGroupElementImpl(final PaillierGroupImpl group, final BigInt value) {
    this.group = group;
    this.val = value;
    this.mod = group.getModulus();
  }

  @Override
  public String toString() {
    return "PaillierGroupElement [" + val + " mod " + mod + "]";
  }

  @Override
  public PaillierGroupElement op(final PaillierGroupElement lhs) {
    if (lhs instanceof PaillierGroupElementImpl) {
      final PaillierGroupElementImpl lhsp = (PaillierGroupElementImpl) lhs;
      final BigInt result = val.multiply(lhsp.val).mod(mod);
      return new PaillierGroupElementImpl(group, result);
    } else {
      throw new ArithmeticException("Cannot multiply incompatible group elements.");
    }
  }

  @Override
  public PaillierGroupElement multOp(final BigInt exponent) {
    final BigInt result = val.modPow(exponent, mod);
    return new PaillierGroupElementImpl(group, result);
  }

  @Override
  public PaillierGroupElement opMultOp(final PaillierGroupElement base, final BigInt exponent) {
    return this.op(base.multOp(exponent));
  }

  @Override
  public PaillierGroupImpl getGroup() {
    return group;
  }

  /**
   * Computes the discrete logarithm of elements of the form h^a = (a + an mod n^2) for 0<=a<n.
   * 
   * @param base
   * @return
   */
  @Override
  public BigInt getDiscreteLog(final PaillierGroupElement base) {
    // TODO check algorithm
    final BigInt temp = this.val.subtract(group.getBigIntFactory().one()).mod(mod);
    final BigInt nInv = group.getN().modInverse(mod);
    final BigInt a = temp.multiply(nInv).mod(mod);

    boolean rangeOK;
    if (group.getBigIntFactory().zero().compareTo(a) <= 0 && a.compareTo(group.getN()) < 0) {
      rangeOK = true;
    } else {
      rangeOK = false;
    }
    if (!rangeOK) {
      throw new ArithmeticException(
          "The discrete logarithm a is out of bounds, that is, the logarithm is not 0 <= a < n.");
    }

    return a;
  }

  @Override
  public BigInt toBigInt() {
    return val;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((mod == null) ? 0 : mod.hashCode());
    result = prime * result + ((val == null) ? 0 : val.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    final PaillierGroupElementImpl other = (PaillierGroupElementImpl) obj;
    if (mod == null) {
      if (other.mod != null) return false;
    } else if (!mod.equals(other.mod)) return false;
    if (val == null) {
      if (other.val != null) return false;
    } else if (!val.equals(other.val)) return false;
    return true;
  }

  @Override
  public PaillierGroupElement invert() {
    final BigInt inv = val.modInverse(mod);
    return new PaillierGroupElementImpl(group, inv);
  }

  @Override
  public byte[] serialize() {
    return this.toBigInt().toByteArray();
  }
}
