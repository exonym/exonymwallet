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

import com.ibm.zurich.idmx.interfaces.util.BigInt;

public class Term {
  public final String attribute;
  public final BigInt constant;

  public Term(final String attribute, final BigInt constant) {
    this.attribute = attribute;
    this.constant = constant;
  }

  @Override
  public String toString() {
    return "(" + constant + " * " + attribute + ")";
  }
}
