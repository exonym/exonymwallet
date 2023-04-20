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
/**
 * Copyright IBM Corporation 2008-2014.
 */

package com.ibm.zurich.idmx.buildingBlock.rangeProof.fourSq;

import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.RandomGeneration;
import com.ibm.zurich.idmx.util.Arithmetic;
import com.ibm.zurich.idmx.util.RandomGenerationImpl;
//import com.ibm.zurich.idmx.util.bigInt.BigIntFactoryImpl;

//TODO(ksa) Random injection?
public class LipmaaDecomposition {

  private final BigIntFactory bigIntFactory;// = new BigIntFactoryImpl();
  private final RandomGeneration rg;// = new RandomGenerationImpl(bigIntFactory);

  /** Convenience: Constant 0. */
  private final BigInt ZERO;// = bigIntFactory.zero();

  /** Convenience: Constant 1. */
  private final BigInt ONE;// = bigIntFactory.one();

  /** Convenience: Constant 2. */
  private final BigInt TWO;// = bigIntFactory.two();

  /** Convenience: Constant 4. */
  private final BigInt FOUR;// = bigIntFactory.valueOf(4);


  /**
   * Constructor.
   */
  public LipmaaDecomposition(BigIntFactory bg) {
    this.bigIntFactory = bg;
    ZERO = bigIntFactory.zero();
    ONE = bigIntFactory.one();
    TWO = bigIntFactory.two();
    FOUR = bigIntFactory.valueOf(4L);
    rg = new RandomGenerationImpl(bigIntFactory);
  }

  private boolean simpleRepresentation(BigInt numToDecompose, BigInt[] result) {

    // case p is negative
    if (numToDecompose.compareTo(ZERO) < 0) {
      throw new IllegalArgumentException("Lipmaa called with a negative number.");
    }
    // case p==0
    if (numToDecompose.equals(ZERO)) {
      return true;
      // result is [0, 0, 0, 0]
    } else if (numToDecompose.equals(ONE)) {
      // result is [1, 0, 0, 0]
      result[0] = ONE;
      return true;
    } else if (numToDecompose.equals(TWO)) {
      // result is [1, 1, 0, 0]
      result[0] = ONE;
      result[1] = ONE;
      return true;
    }
    return false;
  }

  /**
   * Lipmaa decomposition of integers.
   * 
   * @param primeProbability 2^-primeProbability is the acceptable probability that the prime number
   *        generation algorithm outputs a value that is not a prime.
   * @param numToDecompose The integer that needs to be expressed in at most four squares (for
   *        convenience called p). must be >= 0.
   * 
   * @return The roots of the numbers which - when squared - add up to the value of numToDecompose.
   *         If the decomposition can be done using fewer than four values the remaining values are
   *         set to 0.
   */
  public BigInt[] decomposeInteger(final BigInt numToDecompose, final int primeProbability) {
    int t = 0;

    BigInt[] result = {ZERO, ZERO, ZERO, ZERO};
    if (simpleRepresentation(numToDecompose, result)) {
      return result;
    }


    // get t
    while (!numToDecompose.testBit(t)) {
      t++;
    }
    BigInt rightSide = numToDecompose.shiftRight(t + 1);
    if (t == 1) {
      result = internalDecomp(numToDecompose, primeProbability);
    } else if (t % 2 == 1) {
      result = decomposeInteger(numToDecompose.shiftRight(t - 1), primeProbability);
      result[0] = result[0].shiftLeft((t - 1) / 2);
      result[1] = result[1].shiftLeft((t - 1) / 2);
      result[2] = result[2].shiftLeft((t - 1) / 2);
      result[3] = result[3].shiftLeft((t - 1) / 2);
    } else {
      BigInt toDecompose = rightSide.multiply(FOUR).add(TWO);
      BigInt[] tempDecomp = decomposeInteger(toDecompose, primeProbability);

      if (tempDecomp[0].testBit(0) == tempDecomp[1].testBit(0)) {
        result = tempDecomp;
      }

      if (tempDecomp[0].testBit(0) == tempDecomp[2].testBit(0)) {
        result = tempDecomp;
        BigInt temp = tempDecomp[2];
        tempDecomp[2] = tempDecomp[1];
        tempDecomp[1] = temp;
      }

      if (tempDecomp[0].testBit(0) == tempDecomp[3].testBit(0)) {
        result = tempDecomp;
        BigInt temp = tempDecomp[3];
        tempDecomp[3] = tempDecomp[1];
        tempDecomp[1] = temp;
      }
      BigInt[] copy = {ZERO, ZERO, ZERO, ZERO};

      // BigInt does not support multiplication by reals/rationals
      if (t / 2 - 1 >= 0) {
        BigInt s = TWO.pow(t / 2 - 1);
        copy[0] = s.multiply(result[0].add(result[1]));
        copy[1] = s.multiply(result[0].subtract(result[1]));
        copy[2] = s.multiply(result[2].add(result[3]));
        copy[3] = s.multiply(result[2].subtract(result[3]));
      } else {
        copy[0] = result[0].add(result[1]).divide(TWO);
        copy[1] = result[0].subtract(result[1]).divide(TWO);
        copy[2] = result[2].add(result[3]).divide(TWO);
        copy[3] = result[2].subtract(result[3]).divide(TWO);
      }
      result = copy;
    }
    for (int i = 0; i < 4; i++) {
      result[i] = result[i].abs();
    }
    return result;
  }

  private BigInt[] internalDecomp(final BigInt numToDecompose, final int primeProbability) {
    BigInt[] result = {ZERO, ZERO, ZERO, ZERO};

    if (simpleRepresentation(numToDecompose, result)) {
      return result;
    }

    BigInt p = null;
    BigInt omega1;
    BigInt omega2;


    BigInt upperLimit1 = Arithmetic.squareRoot(numToDecompose).subtract(ONE);
    BigInt upperLimit2;
    // BigInt rootP = upperLimit1;
    do {
      // BigInt toTest = generaterandomMax(upperLimit1);
      omega1 = rg.generateRandomNumber(upperLimit1);

      upperLimit2 = Arithmetic.squareRoot(numToDecompose.subtract(omega1.multiply(omega1)));
      // biased - however this is not important for the application
      omega2 = rg.generateRandomNumber(upperLimit2);

      if (omega1.testBit(0) == omega2.testBit(0)) {
        omega1 = omega1.subtract(ONE);
        if (omega1.compareTo(ZERO) <= 0) {
          continue;
        }
      }

      p = numToDecompose.subtract(omega1.multiply(omega1)).subtract(omega2.multiply(omega2));
    } while (p == null || !p.isProbablePrime(primeProbability));


    BigInt[] twosquares = gcdLipmaa(findRootMinus1(p), p, Arithmetic.squareRoot(p));
    result[0] = omega1;
    result[1] = omega2;
    result[2] = twosquares[0];
    result[3] = twosquares[1];
    return result;
  }

  /**
   * Calculates the gcd - however, it returns the two remainder < sqrt(p) and stops. This is a
   * special implementation for Lipmaa's algorithm!! Do not use outside!
   * 
   * @param m the first number
   * @param n the second number
   * @param limit the square root of p - this determines the end of the loop
   * @return an approximation of the root of <tt>square</tt>.
   */
  private BigInt[] gcdLipmaa(final BigInt m, final BigInt n, final BigInt limit) {
    BigInt[] result = new BigInt[2];
    BigInt a, b, rest;
    b = n;
    a = m;
    rest = a.mod(b);
    result[0] = a;
    result[1] = rest;
    while (result[0].compareTo(limit) > 0 || result[0].equals(result[1])) {
      result[0] = a;
      result[1] = rest;
      rest = a.mod(b);
      a = b;
      b = rest;
    }
    return result;
  }

  /**
   * Finds a square of minus 1. Requires that p = 1 mod 4. This implementation does only this and
   * nothing else! It is derived from the Tonelli Shanks algorithm
   * 
   * @param p the prime
   * @return t, s.t. t^2 = p-1 mod p
   */
  private BigInt findRootMinus1(final BigInt p) {
    BigInt v = null;
    BigInt u = p.subtract(ONE);

    if (u.equals(ZERO)) {
      return ZERO;
    }

    if (p.equals(TWO)) {
      return u;
    }

    int t = 0;

    // initialization
    // compute k and s, where p = 2^s (2k+1) +1

    BigInt k = u;
    int s = 0;
    while (!k.testBit(s)) { // get factor 2^k
      s++;
    }
    k = k.shiftRight(s);

    k = k.subtract(ONE); // k = k - 1
    k = k.shiftRight(1); // k = k/2

    // initial values
    BigInt r = u.modPow(k, p); // r = a^k mod p

    BigInt n = r.pow(2).remainder(p); // n = r^2 % p
    n = n.multiply(u).remainder(p); // n = n * a % p
    r = r.multiply(u).remainder(p); // r = r * a %p

    if (n.equals(ONE)) {
      return r;
    }

    // non-quadratic residue
    BigInt z = TWO;
    while (z.modPow(p.subtract(ONE).divide(TWO), p).equals(ONE)) { // legendre
      // while z quadratic residue
      z = z.add(ONE); // z = z + 1
    }

    v = k;
    v = v.shiftLeft(1); // v = 2k
    v = v.add(ONE); // v = 2k + 1
    BigInt c = z.modPow(v, p); // c = z^v mod p

    // iteration
    while (n.compareTo(ONE) == 1) { // n > 1
      k = n; // k = n
      t = s; // t = s
      s = 0;

      while (!k.equals(ONE)) { // k != 1
        k = k.pow(2).mod(p); // k = k^2 % p
        s++; // s = s + 1
      }

      t -= s; // t = t - s

      v = ONE;
      v = v.shiftLeft(t - 1); // v = 2^(t - 1)
      c = c.modPow(v, p); // c = c^vmodd p
      r = r.multiply(c).remainder(p); // r = r * c % p
      c = c.multiply(c).remainder(p); // c = c^2 % p
      n = n.multiply(c).mod(p); // n = n * c % p
    }
    return r;
  }
}
