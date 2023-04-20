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
package com.ibm.zurich.idmx.interfaces.buildingBlock.structural.linearCombination;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.ibm.zurich.idmx.interfaces.util.BigInt;

public class LinearCombination {
  public final String lhsAttribute;
  public final BigInt constant;
  public final List<Term> terms;
  
  public LinearCombination(final String lhsAttribute, final BigInt constant, final List<Term> terms) {
    this.lhsAttribute = lhsAttribute;
    this.constant = constant;
    this.terms = Collections.unmodifiableList(new ArrayList<Term>(terms));
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append(lhsAttribute + " = " + constant);
    for(final Term term: terms) {
      sb.append(" + " + term);
    }
    return sb.toString();
  }
}
