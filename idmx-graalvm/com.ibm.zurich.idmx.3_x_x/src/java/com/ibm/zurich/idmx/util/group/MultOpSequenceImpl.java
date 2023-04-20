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

import java.util.ArrayList;
import java.util.List;

import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.group.Group;
import com.ibm.zurich.idmx.interfaces.util.group.GroupElement;
import com.ibm.zurich.idmx.interfaces.util.group.MultOpSequence;

public abstract class MultOpSequenceImpl<G extends Group<G, GE, M>, GE extends GroupElement<G, GE, M>, M extends MultOpSequence<G, GE, M>>
    implements
      MultOpSequence<G, GE, M> {

  protected final List<GE> groupElements;
  protected final List<BigInt> iterationCounters;

  public MultOpSequenceImpl() {
    groupElements = new ArrayList<GE>();
    iterationCounters = new ArrayList<BigInt>();
  }

  @Override
public void putMultOp(final GE ge, final BigInt iterations) {
    groupElements.add(ge);
    iterationCounters.add(iterations);
  }

  @Override
public abstract GE finalizeSequence();

}
