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


public class BigIntFactoryImpl implements BigIntFactory {

  @Override
  public BigInt zero() {
    return BigIntImpl.ZERO;
  }

  @Override
  public BigInt one() {
    return BigIntImpl.ONE;
  }


  @Override
  public BigInt two() {
    return BigIntImpl.TWO;
  }

  @Override
  public BigInt valueOf(final int a) {
    return valueOf(BigInteger.valueOf(a));
  }

  @Override
  public BigInt valueOf(final long a) {
    return valueOf(BigInteger.valueOf(a));
  }

  @Override
  public BigInt valueOf(final BigInteger a) {
    return new BigIntImpl(a);
  }

  @Override
  public BigInt random(final int bitLength, final Random r) {
    return new BigIntImpl(bitLength, r);
  }

  @Override
  public BigInt randomPrime(final int bitLength, final int certainty, final Random r) {
    return new BigIntImpl(bitLength, certainty, r);
  }

  /**
   * Creates a BigInt from the byte array assuming the array representing a positive integer using
   * big-endian byte order. The method ensures that the generated BigInt has a 0 as its first byte to
   * represent the positive number in two's complement representation.
   */
  @Override
  public BigInt unsignedValueOf(final byte[] bytes) {
    final byte[] signedHash = new byte[bytes.length + 1];
    // Add sign bit
    signedHash[0] = 0;
    System.arraycopy(bytes, 0, signedHash, 1, bytes.length);
    final BigInt result = signedValueOf(signedHash);
    if(result.compareTo(this.zero())<0) {
      throw new RuntimeException("Negative value");
    }
    return result;
  }

  /**
   * Creates a BigInt from the byte array assuming the array being in big-endian two's complement
   * representation.
   */
  @Override
  public BigInt signedValueOf(final byte[] bytes) {
    return new BigIntImpl(bytes);
  }
}
