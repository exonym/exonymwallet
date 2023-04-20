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

public interface MultOpSequence<G extends Group<G, GE, M>, GE extends GroupElement<G, GE, M>, M extends MultOpSequence<G, GE, M>> {

  /**
   * Adds a multi-operation to the multi-operation sequence that has previously been initiated with
   * .
   * 
   * @return
   */
  void putMultOp(final GE ge, final BigInt iterations);

  /**
   * Finalizes a multi-operation sequence and returns its results.
   * 
   * For performance reasons, computations of the multi-operation sequence may only happen once this
   * method is invoked. For groups with group operation being modular multiplication, a multi-base
   * exponentiation can be computed in a substantially faster way than the naive product of
   * single-base exponentiations.
   * 
   * @return
   */
  GE finalizeSequence();

}
