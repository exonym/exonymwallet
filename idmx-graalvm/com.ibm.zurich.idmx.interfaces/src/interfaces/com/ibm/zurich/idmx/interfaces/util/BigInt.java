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

package com.ibm.zurich.idmx.interfaces.util;

import java.math.BigInteger;

/**
 * This class provides an interface for time consuming computations. Its implementation may use the
 * standard java implementation or it may delegate the computations to a highly optimized library to
 * improve performance of the overall system.
 */
public interface BigInt {

  public BigInt add(BigInt val);

  public BigInt subtract(BigInt val);

  public BigInt multiply(BigInt val);

  public BigInt divide(BigInt val);

  public BigInt[] divideAndRemainder(BigInt val);

  public BigInt remainder(BigInt val);

  public BigInt pow(int exponent);

  public BigInt gcd(BigInt val);

  public BigInt abs();

  public BigInt negate();

  public int compareTo(BigInt val);

  public BigInt mod(BigInt m);

  public BigInt modPow(BigInt exponent, BigInt m);

  public BigInt modInverse(BigInt m);

  public int getLowestSetBit();

  public int bitLength();

  public int bitCount();

  public BigInt shiftLeft(int n);

  public BigInt shiftRight(int n);

  public boolean testBit(int n);

  public BigInt setBit(int val);

  public BigInt and(BigInt val);

  public BigInt or(BigInt val);

  public BigInt xor(BigInt val);

  public BigInt not();

  public BigInt andNot(BigInt val);

  public boolean isProbablePrime(int primeProbability);

  public BigInt nextProbablePrime();

  public BigInt min(BigInt val);

  public BigInt max(BigInt val);

  public byte[] toByteArray();

  public String toHumanReadableString();

  public String toString();

  public String toString(int radix);

  public int intValue();

  public long longValue();


  /**
   * Returns the big endian representation of this integer WITHOUT the sign bit, assuming that this
   * integer is non-negative.
   * 
   * @return
   */
  public byte[] toByteArrayUnsigned();

  /**
   * @return BigInteger used to delegate all computations to.
   */
  public BigInteger getValue();

  public BigIntFactory getFactory();

}
