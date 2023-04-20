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

import com.ibm.zurich.idmx.interfaces.util.BigInt;

/**
 * 
 */
public interface RandomGeneration {

  public BigInt generateRandomPrime(int bitLength, int primeProbability);

  public BigInt generateRandomSafePrime(int bitLength, final int primeCertainty);

  /**
   * Returns a random number in the range of <tt>[0..(2^bitlength)-1]</tt>. (math notation:
   * <tt>\{0,1\}^{bitlength}</tt> (MSB always 0 to stay >= 0)).
   * 
   * @param bitlength Bit length.
   * @return Positive random number <tt>[0..(2^bitlength)-1]</tt>.
   * 
   */
  public BigInt generateRandomNumber(int bitlength);

  /**
   * Returns a statistically uniformly distributed random number from the interval
   * <tt>[lower..upper]</tt>.
   * 
   * @param lower Lower bound.
   * @param upper Upper bound.
   * @param sp System parameters.
   * @return Random number in the given range.
   */
  public BigInt generateRandomNumber(BigInt lower, BigInt upper, int statisticalZK);

  /**
   * Returns a statistically uniformly distributed random number from the interval
   * <tt>[0..upper-1]</tt>.
   * 
   * @param upper Upper bound.
   * @param statisticalZK Bit length to attain statistical zero-knowledge.
   * @return Random number in the given range.
   */
  public BigInt generateRandomNumber(BigInt upper, int statisticalZK);

  /**
   * Returns a random number from the interval <tt>[0..upper-1]</tt>.
   * 
   * @param upper Upper bound.
   * @return Random number in the given range.
   */
  public BigInt generateRandomNumber(BigInt upper);

  public BigInt generateRandomOddNumber(int bitLength);

  public String generateRandomUid();
}
