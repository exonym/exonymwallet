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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.group.Group;
import com.ibm.zurich.idmx.interfaces.util.group.GroupElement;

public class CarryOverStateRecipient implements CarryOverState {

  private static final long serialVersionUID = 1294513081109158590L;
  
  private final byte[] commitment;
  private final List</* Nullable */BigInteger> attributeValues;
  private final BigInteger opening;

  public CarryOverStateRecipient(GroupElement<?, ?, ?> commitment, BigInt opening,
      List</* Nullable */BigInt> attributeValues) {
    this.commitment = commitment.serialize();
    this.attributeValues = new ArrayList<>();
    for(BigInt v: attributeValues) {
      if(v != null) {
        this.attributeValues.add(v.getValue());
      } else {
        this.attributeValues.add(null);
      }
    }
    this.opening = opening.getValue();
  }

  public <G extends Group<G, GE, ?>, GE extends GroupElement<G, GE, ?>>
  GroupElement<G, GE, ?> getCommitment(Group<G, GE, ?> g) {
    return g.valueOfNoCheck(commitment);
  }

  public List</* Nullable */BigInt> getAttributeValues(BigIntFactory bf) {
    List<BigInt> ret = new ArrayList<>();
    for(BigInteger v: attributeValues) {
      if(v == null) {
        ret.add(null);
      } else {
        ret.add(bf.valueOf(v));
      }
    }
    return ret;
  }

  public BigInt getOpening(BigIntFactory bf) {
    return bf.valueOf(opening);
  }

}
