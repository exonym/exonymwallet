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

package com.ibm.zurich.idmx.buildingBlock.structural.linearCombination;

import java.util.List;

import com.ibm.zurich.idmx.buildingBlock.GeneralBuildingBlock;
import com.ibm.zurich.idmx.interfaces.buildingBlock.structural.linearCombination.Term;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProver;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleVerifier;

/**
 * This building block declares a linear combination among several attributes. This block takes care
 * of computing the value of the left-hand-side attribute based on the right hand side. The
 * following constraints must be respected: (1) It is acceptable for the left-hand-side attribute to
 * be outside of the normal range for attributes (i.e., it may be negative). (2) If the
 * left-hand-side (lhs) attribute is revealed, then ALL attributes in the linear combination must be
 * revealed (this is not done automatically). (3) It is acceptable to chain multiple linear
 * combination building blocks (i.e., the lhs in one block appears in the right hand side of another
 * block), but there must be no circular dependencies. (4) It is not acceptable for an attribute to
 * appear as the left-hand-side of two distinct blocks. (5) It is not permissible for an attribute
 * to appear both in the left-hand-side and the right-hand-side (after considering which attributes
 * are declared equal).
 */
public class LinearCombinationLightBuildingBlock extends GeneralBuildingBlock {

  public LinearCombinationLightBuildingBlock() {
    // TODO Auto-generated constructor stub
  }

  @Override
  protected String getBuildingBlockIdSuffix() {
    return "s-linear";
  }

  @Override
  protected String getImplementationIdSuffix() {
    return "s-linear";
  }

  public ZkModuleProver getZkModuleProver(final String identifierOfModule, final String lhsAttribute,
                                          final BigInt constant, final List<Term> terms) {
    return new ProverModule(this, identifierOfModule, lhsAttribute, constant, terms);
  }

  public ZkModuleVerifier getZkModuleVerifier(final String identifierOfModule, final String lhsAttribute,
                                              final BigInt constant, final List<Term> terms) {
    return new VerifierModule(this, identifierOfModule, lhsAttribute, constant, terms);
  }

}
