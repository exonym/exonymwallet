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

import com.ibm.zurich.idmx.interfaces.util.BigInt;

public class NumberComparison {

  // Non-instantiable Class
  private NumberComparison() {
    throw new AssertionError("Idemix: This class MUST NOT be instantiated.");
  }

  /**
   * Tests if <tt>n</tt> is odd.
   * 
   * @param n Parameter to be tested.
   * @return True if <tt>n</tt> is odd.
   */
  public static boolean isOdd(final BigInt n) {
    final BigInt one = n.getFactory().one();
    final BigInt two = n.getFactory().two();
    return n.mod(two).equals(one);
  }

  /**
   * Tests if <tt>n</tt> is in given interval <tt>[lower..upper]</tt>.
   * 
   * @param n Parameter to be tested.
   * @param lower Lower bound.
   * @param upper Upper bound.
   * @return True if <tt>lower <= n <= upper</tt>.
   */
  public static boolean isInInterval(final BigInt n, final BigInt lower, final BigInt upper) {
    return (lower.compareTo(n) <= 0 && n.compareTo(upper) <= 0);
  }

  /**
   * 
   * Tests if a ApInteger value lies in a given range, indicated by binary exponents, i.e.,
   * <tt>arg</tt> in <tt>[ 2^powerLower..2^powerUpper]</tt>.
   * 
   * @param arg ApInteger to have its bound checked.
   * @param powerLower Bit length for inclusive lower bound.
   * @param powerUpper Bit length for inclusive upper bound.
   * 
   * @return True if <tt>arg</tt> is within specified range, i.e.,
   *         <tt>2^powerLower <= arg <= 2^powerUpper</tt>.
   */
  public static boolean isInInterval(final BigInt arg, final int powerLower, final int powerUpper) {

    final BigInt lowerBound;
    final BigInt one = arg.getFactory().one();

    if (powerLower == 0) {
      lowerBound = one;
    } else {
      lowerBound = (one.shiftLeft(powerLower));
    }
    final BigInt upperBound = one.shiftLeft(powerUpper);

    return isInInterval(arg, lowerBound, upperBound);
  }
}
