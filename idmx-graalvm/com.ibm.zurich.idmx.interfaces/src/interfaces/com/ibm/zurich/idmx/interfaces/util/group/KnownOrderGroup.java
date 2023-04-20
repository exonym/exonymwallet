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

/**
 * Prime-order groups as accessed through this interface are subgroups with the order being prime of
 * Z_n^*.
 */
public interface KnownOrderGroup
    extends
      Group<KnownOrderGroup, KnownOrderGroupElement, KnownOrderGroupMultOpSequence> {

  /**
   * Returns group order. This is always possible for this kind of groups as the order is known.
   * 
   * @return
   */
  public BigInt getOrder();

  // Only available for groups with known order
  /**
   * Computes the inverse of the exponent.
   * 
   * @param exponent
   * @return
   */
  BigInt invertIterationcounter(final BigInt iterationcounter);

}
