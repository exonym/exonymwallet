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

import java.math.BigInteger;

import com.ibm.zurich.idmx.configuration.Configuration;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.RandomGeneration;
import com.ibm.zurich.idmx.interfaces.util.group.KnownOrderGroup;
import com.ibm.zurich.idmx.interfaces.util.group.KnownOrderGroupElement;
import com.ibm.zurich.idmx.interfaces.util.group.KnownOrderGroupMultOpSequence;

// Package-private
/**
 * Prime-order subgroup of order q of Z_p^* with p prime.
 */
class PrimeOrderGroupImpl implements KnownOrderGroup {

  private final BigInt modulus;
  private final BigInt subgroupOrder;
  private final BigInt cofactor;
  private final BigIntFactory bigIntFactory;


  @Override
  public String toString() {
    return "PrimeOrderGroup [modulus=" + modulus + ", subgroupOrder=" + subgroupOrder + "]";
  }


  public PrimeOrderGroupImpl(final BigInt modulus, final BigInt subgroupOrder) {
    this.modulus = modulus;
    this.subgroupOrder = subgroupOrder;
    this.bigIntFactory = modulus.getFactory();
    this.cofactor = computeCofactor();
    if (Configuration.checkPrimalityOfModuli()) {
      checkPrimalityOfParameters();
    }
  }

  private void checkPrimalityOfParameters() {
	//TODO This should be externalized
	final int certainty = 80;
    if (!modulus.isProbablePrime(certainty)) {
      throw new ArithmeticException("Modulus of group is not a prime");
    }
    if (!subgroupOrder.isProbablePrime(certainty)) {
      throw new ArithmeticException("Subgroup order is not a prime");
    }
  }

  private BigInt computeCofactor() {
    final BigInt groupOrder = modulus.subtract(bigIntFactory.one());
    final BigInt[] res = groupOrder.divideAndRemainder(subgroupOrder);
    if (!res[1].equals(bigIntFactory.zero())) {
      throw new ArithmeticException("Subgroup order does not divide group order.");
    }
    return res[0];
  }

  @Override
  public KnownOrderGroupElement neutralElement() {
    return new PrimeOrderGroupElementImpl(this, bigIntFactory.one());
  }

  @Override
  public KnownOrderGroupElement createRandomElement(final RandomGeneration rg) {
    return createRandomGenerator(rg);
  }


  @Override
  public KnownOrderGroupElement createRandomGenerator(final RandomGeneration rg) {
    KnownOrderGroupElement ret;
    do {
	  final BigInt r = rg.generateRandomNumber(subgroupOrder);
	  final BigInt exponent = r.multiply(cofactor);
	  final BigInt value = bigIntFactory.two().modPow(exponent, modulus);
      ret = new PrimeOrderGroupElementImpl(this, value);
    } while (ret.equals(neutralElement()));
    return ret;
  }

  @Override
  public BigInt createRandomIterationcounter(final RandomGeneration rg, final int statisticalInd) {
    return rg.generateRandomNumber(bigIntFactory.one(),
        subgroupOrder.subtract(bigIntFactory.one()), statisticalInd);
  }

  private boolean isInSubgroup(final BigInt a) {
    BigInt littleFermat = a.modPow(subgroupOrder, modulus);
    return littleFermat.equals(bigIntFactory.one());
  }

  @Override
  public KnownOrderGroupElement unserializeElement(final byte[] b) {
    return this.valueOf(bigIntFactory.valueOf(new BigInteger(b)));
  }

  @Override
  public byte[] getGroupDescription() {
    return this.toString().getBytes();
  }

  @Override
  public BigInt invertIterationcounter(final BigInt iterationcounter) {
    return iterationcounter.modInverse(subgroupOrder);
  }

  public BigInt getModulus() {
    return modulus;
  }

  /**
   * Returns the order of the prime order group which is a subgroup of Z_n^*.
   */
  @Override
public BigInt getOrder() {
    return subgroupOrder;
  }

  // TODO to be tested
  @Override
  public KnownOrderGroupElement valueOf(final BigInt val) {
    final PrimeOrderGroupElementImpl ge = new PrimeOrderGroupElementImpl(this, val);
    if (isInSubgroup(val)) {
      return ge;
    } else {
      // TODO discuss handling
      throw new ArithmeticException("Parameter is not an element of the given group");
    }
  }

  // TODO to be tested
  @Override
  public KnownOrderGroupElement valueOf(final byte[] val) {
    final BigInt valBI = bigIntFactory.unsignedValueOf(val);
    return valueOf(valBI);
  }

  // TODO to be tested
  @Override
  public KnownOrderGroupElement valueOfNoCheck(final BigInt val) {
    final PrimeOrderGroupElementImpl ge = new PrimeOrderGroupElementImpl(this, val);
    return ge;
  }


  // TODO to be tested
  @Override
  public KnownOrderGroupElement valueOfNoCheck(final byte[] val) {
    final BigInt valBI = bigIntFactory.unsignedValueOf(val);
    return valueOfNoCheck(valBI);
  }

  @Override
  public KnownOrderGroupMultOpSequence initializeSequence() {
    return new PrimeOrderGroupMOS(this, bigIntFactory);
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    final PrimeOrderGroupImpl other = (PrimeOrderGroupImpl) obj;
    if (modulus == null) {
      if (other.modulus != null) return false;
    } else if (!modulus.equals(other.modulus)) return false;
    if (subgroupOrder == null) {
      if (other.subgroupOrder != null) return false;
    } else if (!subgroupOrder.equals(other.subgroupOrder)) return false;
    return true;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((modulus == null) ? 0 : modulus.hashCode());
    result = prime * result + ((subgroupOrder == null) ? 0 : subgroupOrder.hashCode());
    return result;
  }

}
