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
 * Groups with hidden order or relevant subgroups thereof, e.g., the group of signed quadratic
 * residues.
 */
public interface HiddenOrderGroup
    extends
      Group<HiddenOrderGroup, HiddenOrderGroupElement, HiddenOrderGroupMultOpSequence> {

  // TODO additional methods for hidden-order groups to be discussed and added

  /**
   * returns an upper bound on the order of the group. This bound is usually easy to derive from the
   * group description and often required in cryptographic constructions.
   * 
   * @return
   */
  public BigInt getBoundOnOrder();
  
  /**
   * Returns the length of the RandomInterationcounter computed by createRandomIterationCounter
   * 
   * @param statisticalInd: statistical indistinguishability parameter
   * @return
   */
  Integer getRandomIterationcounterLength(final int statisticalInd);
}