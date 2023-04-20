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

// Package-private
class PrimeOrderGroupElementImpl implements KnownOrderGroupElement {

  private final PrimeOrderGroupImpl group;
  private final BigInt val;
  private final BigInt p;
  private final BigInt q;

  @Override
  public String toString() {
    return "PrimeOrderGroupElement [" + val + " mod " + p + "]";
  }

  public PrimeOrderGroupElementImpl(final PrimeOrderGroupImpl group, final BigInt value) {
    this.group = group;
    this.p = group.getModulus();
    this.q = group.getOrder();
    this.val = value.mod(p);
  }

  @Override
  public KnownOrderGroupElement op(final KnownOrderGroupElement lhs) {
    if (lhs instanceof PrimeOrderGroupElementImpl) {
      final PrimeOrderGroupElementImpl lhsp = (PrimeOrderGroupElementImpl) lhs;
      final BigInt result = val.multiply(lhsp.val).mod(p);
      return new PrimeOrderGroupElementImpl(group, result);
    } else {
      throw new ArithmeticException("Cannot multiply incompatible group elements.");
    }
  }

  @Override
  public KnownOrderGroupElement multOp(final BigInt exponent) {
	final BigInt _exponent;
    if (exponent.compareTo(q) >= 0 || exponent.compareTo(q.negate()) <= 0) {
    	_exponent = exponent.mod(q);
    }
    else {
    	_exponent = exponent;
    }
    BigInt result = val.modPow(_exponent, p);
    return new PrimeOrderGroupElementImpl(group, result);
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
  public BigInt toBigInt() {
    return val;
  }

  @Override
  public byte[] serialize() {
    return this.toBigInt().toByteArray();
  }

  @Override
  public BigInt getDiscreteLog(final KnownOrderGroupElement base) {
    throw new RuntimeException("Discrete logarithm not implemented for prime order groups.");
  }


  @Override
  public KnownOrderGroupElement invert() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((group == null) ? 0 : group.hashCode());
    result = prime * result + ((val == null) ? 0 : val.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (this.getClass() != obj.getClass()) return false;
    final PrimeOrderGroupElementImpl other = (PrimeOrderGroupElementImpl) obj;
    if (group == null) {
      if (other.group != null) return false;
    } else if (!group.equals(other.group)) return false;
    if (val == null) {
      if (other.val != null) return false;
    } else if (!val.equals(other.val)) return false;
    return true;
  }



}
