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

package com.ibm.zurich.idmx.buildingBlock.structural.attributeSource;

import com.ibm.zurich.idmx.annotations.Nullable;
import com.ibm.zurich.idmx.buildingBlock.GeneralBuildingBlock;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProver;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleVerifier;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ResidueClass;
import com.ibm.zurich.idmx.util.UriUtils;

/**
 * Like a constant building block, but the attribute value is not revealed.
 */
public class AttributeSourceBuildingBlock extends GeneralBuildingBlock {

  public AttributeSourceBuildingBlock() {
    // TODO Auto-generated constructor stub
  }

  @Override
  protected String getBuildingBlockIdSuffix() {
    return "s-source";
  }

  @Override
  protected String getImplementationIdSuffix() {
    return "s-source";
  }

  private String getModuleIdentifier(final String attributeId) {
    return UriUtils.concat(getBuildingBlockId(), attributeId).toString();
  }

  public ZkModuleProver getZkModuleProver(String attributeId, BigInt value, ResidueClass rc) {
    String moduleId = getModuleIdentifier(attributeId);
    return new ProverModule(this, moduleId, attributeId, value, rc);
  }

  public ZkModuleVerifier getZkModuleVerifier(final String attributeId, final @Nullable BigInt value,
                                              final ResidueClass rc) {
    String moduleId = getModuleIdentifier(attributeId);
    return new VerifierModule(this, moduleId, attributeId, value, rc);
  }

}
