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

public interface PaillierGroup
    extends
      Group<PaillierGroup, PaillierGroupElement, PaillierGroupMultOpSequence> {

  // TODO maybe rename to be specific about the subgroup that this is concerned
  public BigInt getOrder();

  // TODO maybe be more specific
  BigInt invertIterationcounter(final BigInt iterationcounter);

  public int getRandomIterationcounterLength(final int statisticalInd);

  public BigInt createRandomIterationcounterForSubgroupOfOrderPhiN(final RandomGeneration rg, final int statisticalInd);

  public int getRandomIterationcounterLengthForSubgroupOfOrderPhiN(final int statisticalInd);

}
