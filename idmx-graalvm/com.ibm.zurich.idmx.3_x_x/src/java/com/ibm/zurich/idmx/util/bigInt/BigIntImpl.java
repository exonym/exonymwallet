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

package com.ibm.zurich.idmx.util.bigInt;

import java.math.BigInteger;
import java.util.Random;

import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;

/**
 * 
 */
// Package private
class BigIntImpl implements BigInt {

  private final BigInteger delegatee;

  // Constructors

  public BigIntImpl(final BigInteger delegatee) {
    this.delegatee = delegatee;
    if (delegatee == null) {
      throw new NullPointerException("Cannot instantiate a bigInt with null");
    }
  }

  public BigIntImpl(final byte[] val) {
    delegatee = new BigInteger(val);
  }

  public BigIntImpl(final int signum, final byte[] magnitude) {
    delegatee = new BigInteger(signum, magnitude);
  }

  public BigIntImpl(final String val, final int radix) {
    delegatee = new BigInteger(val, radix);
  }

  public BigIntImpl(final String val) {
    delegatee = new BigInteger(val);
  }

  public BigIntImpl(final int numBits, final Random rnd) {
    delegatee = new BigInteger(numBits, rnd);
  }

  public BigIntImpl(final int bitLength, final int certainty, final Random rnd) {
    delegatee = new BigInteger(bitLength, certainty, rnd);
  }

  public static BigIntImpl probablePrime(final int bitLength, final Random rnd) {
    return new BigIntImpl(BigInteger.probablePrime(bitLength, rnd));
  }

  public static BigIntImpl valueOf(final long val) {
    return new BigIntImpl(BigInteger.valueOf(val));
  }

  // Constants
  public static final BigIntImpl ZERO = valueOf(0);
  public static final BigIntImpl ONE = valueOf(1);
  public static final BigIntImpl TWO = valueOf(2);
  public static final BigIntImpl TEN = valueOf(10);

  @Override
  public BigInt add(final BigInt val) {
    return new BigIntImpl(delegatee.add(val.getValue()));
  }

  @Override
  public BigInt subtract(final BigInt val) {
    return new BigIntImpl(delegatee.subtract(val.getValue()));
  }

  @Override
  public BigInt multiply(final BigInt val) {
    return new BigIntImpl(delegatee.multiply(val.getValue()));
  }

  @Override
  public BigInt divide(final BigInt val) {
    return new BigIntImpl(delegatee.divide(val.getValue()));

  }

  @Override
  public BigInt[] divideAndRemainder(final BigInt val) {
    final BigInteger[] calculationResult = delegatee.divideAndRemainder(val.getValue());
    final BigInt[] result = new BigIntImpl[2];
    result[0] = new BigIntImpl(calculationResult[0]);
    result[1] = new BigIntImpl(calculationResult[1]);
    return result;
  }

  @Override
  public BigInt remainder(final BigInt val) {
    return new BigIntImpl(delegatee.remainder(val.getValue()));
  }

  @Override
  public BigIntImpl pow(final int exponent) {
    return new BigIntImpl(delegatee.pow(exponent));
  }

  @Override
  public BigInt gcd(final BigInt val) {
    return new BigIntImpl(delegatee.gcd(val.getValue()));
  }

  @Override
  public BigIntImpl abs() {
    return new BigIntImpl(delegatee.abs());
  }

  @Override
  public BigIntImpl negate() {
    return new BigIntImpl(delegatee.negate());
  }

  @Override
  public int compareTo(final BigInt val) {
    return delegatee.compareTo(val.getValue());
  }

  @Override
  public BigInt mod(final BigInt m) {
    return new BigIntImpl(delegatee.mod(m.getValue()));
  }

  @Override
  public BigInt modPow(final BigInt exponent, final BigInt m) {
    return new BigIntImpl(delegatee.modPow(exponent.getValue(), m.getValue()));
  }



  @Override
  public BigInt modInverse(final BigInt m) {
    BigInteger result = delegatee.modInverse(m.getValue());
    // Bug in IBM JVM, we can get around by converting to string and back.
    result = new BigInteger(result.toString());
    return new BigIntImpl(result);
  }

  @Override
  public int getLowestSetBit() {
    return delegatee.getLowestSetBit();
  }

  @Override
  public int bitLength() {
    return delegatee.bitLength();
  }

  @Override
public int bitCount() {
    return delegatee.bitCount();
  }

  @Override
  public BigIntImpl shiftLeft(final int n) {
    return new BigIntImpl(delegatee.shiftLeft(n));
  }

  @Override
  public BigIntImpl shiftRight(final int n) {
    return new BigIntImpl(delegatee.shiftRight(n));
  }

  @Override
  public boolean testBit(final int n) {
    return delegatee.testBit(n);
  }

  @Override
  public BigIntImpl setBit(final int bitNumber) {
    return new BigIntImpl(delegatee.setBit(bitNumber));
  }

  @Override
  public BigInt and(final BigInt val) {
    return new BigIntImpl(delegatee.and(val.getValue()));
  }

  @Override
  public BigInt or(final BigInt val) {
    return new BigIntImpl(delegatee.or(val.getValue()));
  }

  @Override
  public BigInt xor(final BigInt val) {
    return new BigIntImpl(delegatee.xor(val.getValue()));
  }

  @Override
  public BigIntImpl not() {
    return new BigIntImpl(delegatee.not());
  }

  @Override
  public BigInt andNot(final BigInt val) {
    return new BigIntImpl(delegatee.andNot(val.getValue()));
  }

  @Override
  public boolean isProbablePrime(final int primeProbability) {
    return delegatee.isProbablePrime(primeProbability);
  }

  @Override
  public BigIntImpl nextProbablePrime() {
    return new BigIntImpl(delegatee.nextProbablePrime());
  }

  @Override
  public BigInt min(final BigInt val) {
    return new BigIntImpl(delegatee.min(val.getValue()));
  }

  @Override
  public BigInt max(final BigInt val) {
    return new BigIntImpl(delegatee.max(val.getValue()));
  }


  @Override
  public byte[] toByteArray() {
    return delegatee.toByteArray();
  }

  @Override
  public String toHumanReadableString() {
    final String str = delegatee.toString();
    if (str.length() > 14) {
      return str.substring(0, 4) + ".." + str.substring(str.length() - 4) + "["
          + delegatee.bitLength() + "]";
    } else {
      return str;
    }
  }

  @Override
  public String toString() {
    return delegatee.toString();
  }

  @Override
  public String toString(final int radix) {
    return delegatee.toString(radix);
  }

  @Override
  public int intValue() {
    return delegatee.intValue();
  }

  @Override
  public long longValue() {
    return delegatee.longValue();
  }

  @Override
  public byte[] toByteArrayUnsigned() {
    if (delegatee.compareTo(BigInteger.ZERO) < 0) {
      throw new ArithmeticException("Cannot call toByteArrayUnsigned() on negative number");
    } else if (delegatee.equals(BigInteger.ZERO)) {
      final byte[] ret = new byte[1];
      ret[0] = 0;
      return ret;
    } else {
      final byte[] bytes = toByteArray();
      if (bytes[0] == 0) {
        final byte[] ret = new byte[bytes.length - 1];
        System.arraycopy(bytes, 1, ret, 0, ret.length);
        return ret;
      } else {
        return bytes;
      }
    }
  }

  @Override
  public BigInteger getValue() {
    return delegatee;
  }

  @Override
  public BigIntFactory getFactory() {
    return new BigIntFactoryImpl();
  }


  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((delegatee == null) ? 0 : delegatee.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    final BigIntImpl other = (BigIntImpl) obj;
    if (delegatee == null) {
      if (other.delegatee != null) return false;
    } 
    return delegatee.equals(other.delegatee);
  }

}
