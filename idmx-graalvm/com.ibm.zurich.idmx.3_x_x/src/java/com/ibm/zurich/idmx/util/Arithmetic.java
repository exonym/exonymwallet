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

import com.ibm.zurich.idmx.configuration.ErrorMessages;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.Pair;

/**
 * 
 */
public class Arithmetic {

  // Non-instantiable Class
  private Arithmetic() {
    throw new AssertionError(ErrorMessages.nonInstantiationErrorMessage());
  }
  
  /**
   * Calculates the next value of the Newton iteration.
   * 
   * @param square value for which the root is wanted.
   * @param root best estimation for the root so far.
   * @return next estimation calculated as <tt>(root+ square/root)/2</tt>.
   */
  private static BigInt newtonIteration(final BigInt square, final BigInt root) {
    final BigIntFactory bigIntFactory = square.getFactory();
    return root.add(square.divide(root)).divide(bigIntFactory.two());
  }

  /**
   * Returns the squareroot of the input. The root is calculated using Newton iteration where the
   * estimation is carried out as long as the difference of the magnitude of the next step of the
   * Newton iteration is greater than one or the root is not found.
   * 
   * @param square the number whose root is to be found.
   * @return an approximation of the root of <tt>square</tt>.
   */
  public static BigInt squareRoot(final BigInt square) {
    final BigIntFactory bigIntFactory = square.getFactory();
    BigInt root = bigIntFactory.one();
    BigInt oldRoot = root;
    root = newtonIteration(square, root);

    while ((root.subtract(oldRoot).abs().compareTo(bigIntFactory.one()) == 1)
        || (root.pow(2).compareTo(square) > 0)) {
      oldRoot = root;
      root = newtonIteration(square, root);
    }
    return root;
  }

  /**
   * Computes <tt>s</tt> and <tt>t</tt> such that <tt>a*s + b*t = gcd(a, b)</tt>. Using the extended
   * Euclid algorithm as described in "A Computational Introduction to Number Theory and Algebra" by
   * Victor Shoup the values of <tt>s</tt> and <tt>t</tt> are computed and returned as an array.
   * 
   * @param a first argument of the gcd method.
   * @param b second argument of the gcd method.
   * @return array [s,t] such that <tt>s*a + t*b = gcd(a,b)</tt>.
   */
  public static Pair<BigInt, BigInt> extendedEuclid(final BigInt a, final BigInt b) {

    final BigIntFactory bigIntFactory = a.getFactory();
    final Pair<BigInt, BigInt> result;

    BigInt r, r0, r00, q;
    BigInt s = bigIntFactory.one();
    BigInt s0 = bigIntFactory.zero();
    BigInt t = bigIntFactory.zero();
    BigInt t0 = bigIntFactory.one();

    if (a.compareTo(b) < 0) {
      r = b;
      r0 = a;
    } else {
      r = a;
      r0 = b;
    }

    while (r0.compareTo(bigIntFactory.zero()) != 0) {
      q = r.divide(r0);
      r00 = r.mod(r0);
      r = r0;
      final BigInt ss = s0;
      final BigInt tt = t0;
      r0 = r00;
      t0 = t.subtract(t0.multiply(q));
      s0 = s.subtract(s0.multiply(q));
      s = ss;
      t = tt;
    }
    if (a.compareTo(b) < 0) {
      result = new Pair<BigInt, BigInt>(t, s);
    } else {
      result = new Pair<BigInt, BigInt>(s, t);
    }

    return result;
  }


}
