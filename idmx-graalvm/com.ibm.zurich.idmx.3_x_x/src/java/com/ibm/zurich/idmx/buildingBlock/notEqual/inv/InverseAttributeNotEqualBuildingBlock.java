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

package com.ibm.zurich.idmx.buildingBlock.notEqual.inv;

import com.ibm.zurich.idmx.buildingBlock.factory.BuildingBlockFactory;
import com.ibm.zurich.idmx.buildingBlock.notEqual.AttributeNotEqualBuildingBlock;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProver;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleVerifier;

import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.VerifierParameters;

public class InverseAttributeNotEqualBuildingBlock extends AttributeNotEqualBuildingBlock {

  public InverseAttributeNotEqualBuildingBlock() {
    // TODO Auto-generated constructor stub
  }
  //TODO(ksa) externalize?
  @Override
  protected String getBuildingBlockIdSuffix() {
    return "s-nEq";
  }

  @Override
  protected String getImplementationIdSuffix() {
    return "s-nEq";
  }

  @Override
  public ZkModuleProver getZkModuleProver(final SystemParameters systemParameters,
                                          final VerifierParameters verifierParameters, final String lhsAttribute, final String rhsAttribute,
      final BuildingBlockFactory buildingBlockFactory) {
    String moduleId = getModuleIdentifier(lhsAttribute, rhsAttribute);
    return new ProverModule(this, moduleId, lhsAttribute, rhsAttribute, buildingBlockFactory);
  }

  @Override
  public ZkModuleVerifier getZkModuleVerifier(final SystemParameters systemParameters,
                                              final VerifierParameters verifierParameters, final String lhsAttribute, final String rhsAttribute,
      final BuildingBlockFactory buildingBlockFactory) {
    final String moduleId = getModuleIdentifier(lhsAttribute, rhsAttribute);
    return new VerifierModule(this, moduleId, lhsAttribute, rhsAttribute, buildingBlockFactory);
  }

}
