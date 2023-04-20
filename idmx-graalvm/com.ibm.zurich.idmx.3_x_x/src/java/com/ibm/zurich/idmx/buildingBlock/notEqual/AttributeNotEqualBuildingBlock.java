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

package com.ibm.zurich.idmx.buildingBlock.notEqual;

import com.ibm.zurich.idmx.buildingBlock.GeneralBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.factory.BuildingBlockFactory;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProver;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleVerifier;
import com.ibm.zurich.idmx.util.UriUtils;

import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.VerifierParameters;

public abstract class AttributeNotEqualBuildingBlock extends GeneralBuildingBlock {

  public AttributeNotEqualBuildingBlock() {
    // TODO Auto-generated constructor stub
  }
//TODO(ksa) why is this stuff not externalized?
  @Override
  protected String getBuildingBlockIdSuffix() {
    return "s-nEq";
  }

  @Override
  protected String getImplementationIdSuffix() {
    return "s-nEq";
  }

  protected String getModuleIdentifier(final String lhsAttribute, final String rhsAttribute) {
    return UriUtils.concat(getBuildingBlockId(), UriUtils.concat(lhsAttribute, rhsAttribute))
        .toString();
  }

  public abstract ZkModuleProver getZkModuleProver(final SystemParameters systemParameters,
                                                   final VerifierParameters verifierParameters, final String lhsAttribute, final String rhsAttribute,
                                                   final BuildingBlockFactory buildingBlockFactory);

  public abstract ZkModuleVerifier getZkModuleVerifier(final SystemParameters systemParameters,
                                                       final VerifierParameters verifierParameters, final String lhsAttribute, final String rhsAttribute,
                                                       final BuildingBlockFactory buildingBlockFactory);

}
