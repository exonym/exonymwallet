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

package com.ibm.zurich.idmx.interfaces.util.group;

import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.RandomGeneration;

public interface Group<G extends Group<G, GE, M>, GE extends GroupElement<G, GE, M>, M extends MultOpSequence<G, GE, M>> {

  /**
   * Returns the neutral element of the group.
   */
  GE neutralElement();

  /**
   * Returns a random element of the group. This function never returns the neutral element of the
   * group. The return element may be a generator of the group.
   */
  GE createRandomElement(final RandomGeneration rg);

  /**
   * Returns a random generator of the group. This function never returns the neutral element of the
   * group.
   */
  GE createRandomGenerator(final RandomGeneration rg);

  /**
   * Returns a random number, so that if you apply the group operation repetitively on a generator
   * of the group with the return value of this function, you get a value that is close to being
   * uniformly distributed in the group. This function never returns a number that will map any
   * generator to the neutral element.
   * 
   * @return
   */
  BigInt createRandomIterationcounter(final RandomGeneration rg, final int statisticalInd);

  GE unserializeElement(final byte[] b);

  public byte[] getGroupDescription();

  /**
   * Creates a new group element of the given group from the BigInt argument passed to the method,
   * where group membership is checked.
   * 
   * @param val
   * @return
   */
  GE valueOf(final BigInt val);

  GE valueOf(final byte[] val);

  /**
   * Creates a new group element of the given group from the BigInt argument passed to the method.
   * 
   * @param val
   * @return
   */
  GE valueOfNoCheck(final BigInt val);

  GE valueOfNoCheck(final byte[] val);

  /**
   * Instructs the group element that a sequence of multi operations, the results of each element of
   * the are to be connected with the group operation, is to be initiated.
   * 
   * This method corresponds to a builder pattern realizing the computation of a multi-operation
   * sequence. K multi-operations can be added to the sequence, the result is computed with
   * finalizing the sequence.
   * 
   * A special case for groups where the operation is multiplication of a multi-operation sequence
   * is a multi-exponentiation.
   * 
   * @return
   */
  M initializeSequence();

}
