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

public interface GroupElement <G extends Group<G, GE, M>, GE extends GroupElement<G, GE, M>, M extends MultOpSequence<G, GE, M>> {

  /**
   * Perform group operation with this as the first and ge as the second group element, both of
   * which need to be from the same group.
   * 
   * @param ge
   * @return
   */
  GE op(final GE lhs);

  GE multOp(final BigInt exponent);

  GE opMultOp(final GE base, final BigInt exponent);

  G getGroup();

  GE invert();
  
  // Only available for some groups
  BigInt getDiscreteLog(final GE base);

  // TODO(enr): toBigInt might not make sense for Elliptic curves.
  BigInt toBigInt();

  byte[] serialize();
}
