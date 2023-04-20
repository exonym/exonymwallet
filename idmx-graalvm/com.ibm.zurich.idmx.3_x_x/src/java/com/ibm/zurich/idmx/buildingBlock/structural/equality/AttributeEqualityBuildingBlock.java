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

package com.ibm.zurich.idmx.buildingBlock.structural.equality;

import com.ibm.zurich.idmx.buildingBlock.GeneralBuildingBlock;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProver;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleVerifier;
import com.ibm.zurich.idmx.util.UriUtils;

public class AttributeEqualityBuildingBlock extends GeneralBuildingBlock {

  public AttributeEqualityBuildingBlock() {
    // TODO Auto-generated constructor stub
  }

  @Override
  protected String getBuildingBlockIdSuffix() {
    return "s-eq";
  }

  @Override
  protected String getImplementationIdSuffix() {
    return "s-eq";
  }

  private String getModuleIdentifier(final String lhsAttribute, final String rhsAttribute) {
    return UriUtils.concat(getBuildingBlockId(), UriUtils.concat(lhsAttribute, rhsAttribute))
        .toString();
  }

  public ZkModuleProver getZkModuleProver(final String lhsAttribute, final String rhsAttribute, final boolean external) {
    final String moduleId = getModuleIdentifier(lhsAttribute, rhsAttribute);
    return new ProverModule(this, moduleId, lhsAttribute, rhsAttribute, external);
  }

  public ZkModuleVerifier getZkModuleVerifier(final String lhsAttribute, final String rhsAttribute,
                                              final boolean external) {
    final String moduleId = getModuleIdentifier(lhsAttribute, rhsAttribute);
    return new VerifierModule(this, moduleId, lhsAttribute, rhsAttribute, external);
  }

}
