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

package com.ibm.zurich.idmx.buildingBlock.structural.constant;

import com.ibm.zurich.idmx.annotations.Nullable;
import com.ibm.zurich.idmx.buildingBlock.GeneralBuildingBlock;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProver;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleVerifier;
import com.ibm.zurich.idmx.util.UriUtils;

public class ConstantBuildingBlock extends GeneralBuildingBlock {

  public ConstantBuildingBlock() {
    // TODO Auto-generated constructor stub
  }

  @Override
  protected String getBuildingBlockIdSuffix() {
    return "s-constant";
  }

  @Override
  protected String getImplementationIdSuffix() {
    return "s-constant";
  }

  private String getModuleIdentifier(String attributeId) {
    return UriUtils.concat(getBuildingBlockId(), attributeId).toString();
  }

  public ZkModuleProver getZkModuleProver(String attributeId, BigInt value) {
    String moduleId = getModuleIdentifier(attributeId);
    return new ProverModule(this, moduleId, attributeId, value);
  }

  public ZkModuleVerifier getZkModuleVerifier(String attributeId, @Nullable BigInt value) {
    String moduleId = getModuleIdentifier(attributeId);
    return new VerifierModule(this, moduleId, attributeId, value);
  }

}
