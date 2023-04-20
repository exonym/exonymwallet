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
package com.ibm.zurich.idmx.util.group;

import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.group.PaillierGroup;
import com.ibm.zurich.idmx.interfaces.util.group.PaillierGroupElement;
import com.ibm.zurich.idmx.interfaces.util.group.PaillierGroupMultOpSequence;

public class PaillierGroupMOS
    extends MultOpSequenceImpl<PaillierGroup, PaillierGroupElement, PaillierGroupMultOpSequence>
    implements
      PaillierGroupMultOpSequence {

  private final PaillierGroup group;
  @SuppressWarnings("unused")
  private final BigIntFactory bigIntFactory;

  public PaillierGroupMOS(final PaillierGroup group, final BigIntFactory bigIntFactory) {
    this.group = group;
    this.bigIntFactory = bigIntFactory;
  }

  @Override
  public PaillierGroupElement finalizeSequence() {
    PaillierGroupElement result = group.neutralElement();
    PaillierGroupElement temp;
    assert (groupElements.size() == iterationCounters.size());
    // naive implementation -- to be replaced by more elaborate one when there is time for this
    for (int i = 0; i < groupElements.size(); i++) {
      temp = groupElements.get(i).multOp(iterationCounters.get(i));
      result = result.op(temp);
    }
    return result;
  }

}
