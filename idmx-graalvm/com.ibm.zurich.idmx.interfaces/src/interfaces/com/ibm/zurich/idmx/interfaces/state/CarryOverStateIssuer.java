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

package com.ibm.zurich.idmx.interfaces.state;

import java.util.Collections;
import java.util.List;

import com.ibm.zurich.idmx.interfaces.util.group.Group;
import com.ibm.zurich.idmx.interfaces.util.group.GroupElement;

public class CarryOverStateIssuer implements CarryOverState {

  private static final long serialVersionUID = -3625265871629453014L;

  private final byte[] commitment;

  private final List<Boolean> carryOverAttribute;

  public CarryOverStateIssuer(GroupElement<?, ?, ?> commitment, List<Boolean> carryOverAttribute) {
    this.commitment = commitment.serialize();
    this.carryOverAttribute = Collections.unmodifiableList(carryOverAttribute);
  }

  public <G extends Group<G, GE, ?>, GE extends GroupElement<G, GE, ?>>
      GroupElement<G, GE, ?> getCommitment(Group<G, GE, ?> g) {
    return g.valueOfNoCheck(commitment);
  }

  public int getNumberOfAttributes() {
    return carryOverAttribute.size();
  }

  public boolean isIssuerSetAttribute(final int i) {
    return (!carryOverAttribute.get(i));
  }
}
