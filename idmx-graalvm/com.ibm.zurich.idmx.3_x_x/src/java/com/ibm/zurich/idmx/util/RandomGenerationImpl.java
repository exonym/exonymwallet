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

package com.ibm.zurich.idmx.util;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;

//import com.google.inject.Inject;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.RandomGeneration;

import javax.inject.Inject;

/**
 * 
 */
public class RandomGenerationImpl implements RandomGeneration {

  private final static int MAX_SMALL_PRIME = 16384;
  @SuppressWarnings("unused")
  private final static int SIZE_OF_RANDOM_NUMBER_IN_BITS = 80;

  private final static List<BigInteger> listOfSmallPrimes = generateSmallPrimes(MAX_SMALL_PRIME, 3);

  private final SecureRandom secureRandom;
  private final BigIntFactory bigIntFactory;

  private final BigInt zero;
  private final BigInt one;
  private final BigInt two;

  @Inject
  public RandomGenerationImpl(final BigIntFactory bif) {
    this.secureRandom = new SecureRandom();
    this.bigIntFactory = bif;
    this.zero = bif.zero();
    this.one = bif.one();
    this.two = bif.two();
  }

  /**
   * Generate a prime with a given length, based on the prime probability of the system parameters
   */
  @Override
  public final BigInt generateRandomPrime(final int bitLength, final int primeProbability) {

    BigInt rho;
    do {
      rho = bigIntFactory.randomPrime(bitLength, primeProbability, secureRandom);
    } while (!NumberComparison.isInInterval(rho, bitLength - 1, bitLength));
    return rho;
  }

  /**
   * This method generates small prime numbers up to a specified bounds using the Sieve of
   * Eratosthenes algorithm.
   * 
   * @param primeBound The upper bound for the primes to be generated
   * @param start The first prime in the list of primes that is returned
   * @return List of primes up to the specified bound. Each prime is a ApInteger object.
   */
  private static ArrayList<BigInteger> generateSmallPrimes(final int primeBound,
		  final int start) {
	int startingPrime = start;
    final ArrayList<BigInteger> res = new ArrayList<BigInteger>();
    if ((primeBound <= 1) || (startingPrime > primeBound)) return res;
    if (startingPrime <= 2) {
      startingPrime = 2;
      res.add(BigInteger.valueOf(2));
    }
    boolean[] primes = new boolean[(primeBound - 1) / 2];
    int i, k, prime;
    for (i = 0; i < primes.length; i++)
      primes[i] = true;
    for (i = 0; i < primes.length; i++) {
      if (primes[i]) {
        prime = 2 * i + 3;
        for (k = i + prime; k < primes.length; k += prime)
          primes[k] = false;
        if (prime >= startingPrime) res.add(BigInteger.valueOf(prime));
      }
    }
    return res;
  }

  private BigInt getPrimeBound(final int bitLength) {
    // some heuristic checks to limit the number of small primes to check against and the number
    // of Miller-Rabin primality tests at the end
    if (bitLength <= 256) {
      return bigIntFactory.valueOf(768);
    } else if (bitLength <= 512) {
      return bigIntFactory.valueOf(3072);
    } else if (bitLength <= 768) {
      return bigIntFactory.valueOf(6144);
    } else if (bitLength <= 1024) {
      return bigIntFactory.valueOf(10240);
    } else {
      return bigIntFactory.valueOf(MAX_SMALL_PRIME + 1);
    }
  }

  /**
   * Tests if A is a Miller-Rabin witness for N
   * 
   * @param A Number which is supposed to be the witness
   * @param N Number to be tested against
   * @return true if A is Miller-Rabin witness for N, false otherwise
   */
  private boolean isMillerRabinWitness(final BigInt A, final BigInt N) {
	final BigInt N_1 = N.subtract(one);
    int t = 0;

    while (N_1.divide(two.pow(t)).mod(two).compareTo(zero) == 0)
      t++;
    final BigInt U = N_1.divide(two.pow(t));

    BigInt x0;
    BigInt x1 = A.modPow(U, N);

    for (int i = 0; i < t; i++) {
      x0 = x1;
      x1 = x0.modPow(two, N);
      if (x1.compareTo(one) == 0 && x0.compareTo(one) != 0 && x0.compareTo(N_1) != 0) return true;
    }
    if (x1.compareTo(one) != 0)
      return true;
    else
      return false;
  }

  /**
   * Test whether the provided pDash or p = 2*pDash + 1 are divisible by any of the small primes
   * saved in the listOfSmallPrimes. A limit for the largest prime to be tested against can be
   * specified, but it will be ignored if it exceeds the number of pre-computed primes.
   * 
   * @param pDash The number to be tested (pDash)
   * @param primeBound The limit for the small primes to be tested against.
   */
  private boolean testSmallPrimeFactors(final BigInt pDash, final BigInt primeBound) {
	final ListIterator<BigInteger> primes = listOfSmallPrimes.listIterator();
    BigInt smallPrime = one;

    while (primes.hasNext() && (smallPrime.compareTo(primeBound) < 0)) {
      smallPrime = bigIntFactory.valueOf(primes.next());

      // r = pDash % smallPrime
      final BigInt r = pDash.remainder(smallPrime);

      // test if pDash = 0 (mod smallPrime) if (r.compareTo(ApInteger.ZERO) == 0) {
      if (r.equals(zero)) {
        return false;
      }
      // test if p == 0 (mod smallPrime) (or, equivalently, r == smallPrime - r - 1)
      if (r.compareTo(smallPrime.subtract(r).subtract(one)) == 0) {
        return false;
      }
    }
    return true;
  }

  private BigInt tryGenerateSafePrime(final int bitLength, final BigInt primeBound,
      final int primeCertainty) {

    // generate random, odd pDash
    final BigInt pDash = generateRandomOddNumber(bitLength - 1);

    // calculate p = 2*pDash+1
    final BigInt p = pDash.shiftLeft(1).add(one);

    // test if pDash or p are divisible by some small primes
    if (!testSmallPrimeFactors(pDash, primeBound)) {
      return null;
    }
    // test if 2 is a compositness witness for pDash or p
    if (isMillerRabinWitness(two, pDash)) {
      return null;
    }

    // test if 2^(pDash) == +1/-1 (mod p)
    final BigInt tempP = two.modPow(pDash, p);
    if ((tempP.compareTo(one) != 0) && (tempP.compareTo(p.subtract(one)) != 0)) {
      return null;
    }

    // use the ApInteger primality check, implements MillerRabin and LucasLehmer
    if (pDash.isProbablePrime(primeCertainty)) {
      // we found a prime!
      // and return p = 2*p' + 1
      return p;
    }
    return null;
  }

  /**
   * The main method to compute a random safe prime of the specified bit length. IMPORTANT: The
   * computed prime will have two first bits and the last bit set to 1 !! i.e. >
   * (2^(bitLength-1)+2^(bitLength-2)+1). This is done to be sure that if two primes of bitLength n
   * are multiplied, the result will have the bitLength of 2*n exactly.
   * 
   * This implementation uses the algorithm proposed by Ronald Cramer and Victor Shoup in
   * "Signature Schemes Based on the strong RSA Assumption" May 9, 2000.
   * 
   * @param bitLength The bit length of the safe prime to be computed.
   * @param primeCertainty The error probability that the computed number is not prime is
   *        (2^(-primeCertainty))
   * @return A prime number p which is considered to be safe with the prime certainty specified
   *         above. It has the property of p = 2p'+ 1 with both, p and p' being prime.
   */
  @Override
  public final BigInt generateRandomSafePrime(final int bitLength, final int primeCertainty) {

    final BigInt primeBound = getPrimeBound(bitLength);

    BigInt p = null;

    do {
      p = tryGenerateSafePrime(bitLength, primeBound, primeCertainty);
    } while (p == null);
    return p;
  }

  /**
   * Returns a random number in the range of <tt>[0..(2^bitlength)-1]</tt>. (math notation:
   * <tt>\{0,1\}^{bitlength}</tt> (MSB always 0 to stay >= 0)).
   * 
   * @param bitlength Bit length.
   * @return Positive random number <tt>[0..(2^bitlength)-1]</tt>.
   * 
   */
  @Override
  public final BigInt generateRandomNumber(final int bitlength) {
    return bigIntFactory.random(bitlength, secureRandom);
  }

  /**
   * Returns a statistically uniformly distributed random number from the interval
   * <tt>[lower..upper]</tt>.
   * 
   * @param lower Lower bound.
   * @param upper Upper bound.
   * @param statisticalInd Additional bits to achieve statistical indistinguishability.
   * @return Random number in the given range.
   */
  @Override
  public final BigInt generateRandomNumber(final BigInt lower, final BigInt upper,
      final int statisticalInd) {
    final BigInt delta = upper.subtract(lower).add(one);
    final BigInt temp = generateRandomNumber(delta, statisticalInd);
    return temp.add(lower);
  }

  
  //TODO(ksa) with rejection sampling, we may have a faster implementation...
  /**
   * Returns a statistically uniformly distributed random number from the interval
   * <tt>[0..upper-1]</tt>.
   * 
   * @param upper Upper bound.
   * @param statisticalInd Bit length to attain statistical zero-knowledge.
   * @return Random number in the given range.
   */
  @Override
public final BigInt generateRandomNumber(final BigInt upper, final int statisticalInd) {
    return bigIntFactory.random((upper.bitLength() + statisticalInd), secureRandom).mod(upper);
  }

  /**
   * Returns a random number from the interval <tt>[0..upper-1]</tt>.
   * 
   * @param upper Upper bound.
   * @return Random number in the given range.
   */
  @Override
public final BigInt generateRandomNumber(final BigInt upper) {
    // TODO(enr): This is a temporary fix
    // TODO(ksa) externalize
    int statisticalZk = 80;
    return generateRandomNumber(upper, statisticalZk);
  }

  /**
   * Generates a random number of bitLength bit length. The first two bits and the last bit of this
   * number are always set, therefore the number is odd and
   * <tt>>= (2^(bitLength-1)+2^(bitLength-2)+1)</tt>.
   * 
   * @param bitLength Length of the number to be generated, in bits.
   * @return A random number of bitLength bit length with first and last bits set.
   */
  @Override
  public final BigInt generateRandomOddNumber(final int bitLength) {
    if (bitLength <= 0) {
      throw new IllegalArgumentException("Idmix: Bitlenght must be > 0");
    }
    final BigInt temp = bigIntFactory.random(bitLength, secureRandom);
    final BigInt oddNumber = temp.setBit(0).setBit(bitLength - 1);
    if (!NumberComparison.isOdd(oddNumber)) {
      throw new RuntimeException("Idmix: Can not generate odd number");
    }
    return oddNumber;
  }

  @Override
  public String generateRandomUid() {
    return UUID.randomUUID().toString();
  }

}
