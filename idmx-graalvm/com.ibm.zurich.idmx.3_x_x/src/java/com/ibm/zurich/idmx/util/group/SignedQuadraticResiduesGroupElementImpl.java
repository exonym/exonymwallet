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
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.group.HiddenOrderGroupElement;

public class SignedQuadraticResiduesGroupElementImpl implements HiddenOrderGroupElement {

  private final SignedQuadraticResiduesGroupImpl group;
  private final BigInt val;
  private final BigInt modulus;
  @SuppressWarnings("unused")
  private final BigIntFactory bigIntFactory;

  /**
   * Creates a new group element from its BigInt representation, assuming that val is an element of
   * the employed representation system [0..(n-1)/2] with Jacobi symbol 1. Membership is not
   * checked.
   * 
   * @param group
   * @param val
   */
  public SignedQuadraticResiduesGroupElementImpl(final SignedQuadraticResiduesGroupImpl group,
		  final BigInt val) {
    this.group = group;
    this.val = val;
    this.modulus = group.getModulus();
    this.bigIntFactory = this.val.getFactory();
  }

  @Override
  public String toString() {
    return "QuadResGroupElement [" + val + " mod " + modulus + "]";
  }

  @Override
  public HiddenOrderGroupElement op(final HiddenOrderGroupElement lhs) {
    if (lhs instanceof SignedQuadraticResiduesGroupElementImpl) {
      final SignedQuadraticResiduesGroupElementImpl lhsq = (SignedQuadraticResiduesGroupElementImpl) lhs;
      BigInt result = val.multiply(lhsq.val).mod(modulus);
      result = group.mapIntoGroup(result);
      return new SignedQuadraticResiduesGroupElementImpl(group, result);
    } else {
      throw new ArithmeticException("Cannot multiply incompatible group elements.");
    }
  }

  @Override
  public HiddenOrderGroupElement multOp(final BigInt exponent) {
    BigInt result;

    result = val.modPow(exponent, modulus);
    result = group.mapIntoGroup(result);

    // TODO proof/check that mapping back after k group operations yields proper result

    return new SignedQuadraticResiduesGroupElementImpl(group, result);
  }

  /**
   * Multiply this group element with a power of another group element and an exponent.
   */
  @Override
  public HiddenOrderGroupElement opMultOp(final HiddenOrderGroupElement base,
		  final BigInt exponent) {
    return this.op(base.multOp(exponent));
  }

  @Override
  public SignedQuadraticResiduesGroupImpl getGroup() {
    return group;
  }

  @Override
  public BigInt getDiscreteLog(final HiddenOrderGroupElement base) {
    throw new ArithmeticException("Not applicable for groups of this type.");
  }


  @Override
  public BigInt toBigInt() {
    return val;
  }

  @Override
  public SignedQuadraticResiduesGroupElementImpl invert() {
    BigInt inv = val.modInverse(modulus);
    inv = group.mapIntoGroup(inv);
    return new SignedQuadraticResiduesGroupElementImpl(group, inv);
  }

  @Override
  public byte[] serialize() {
    return this.toBigInt().toByteArray();
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
    if (getClass() != obj.getClass()) return false;
    SignedQuadraticResiduesGroupElementImpl other = (SignedQuadraticResiduesGroupElementImpl) obj;
    if (group == null) {
      if (other.group != null) return false;
    } else if (!group.equals(other.group)) return false;
    if (val == null) {
      if (other.val != null) return false;
    } else if (!val.equals(other.val)) return false;
    return true;
  }



}
