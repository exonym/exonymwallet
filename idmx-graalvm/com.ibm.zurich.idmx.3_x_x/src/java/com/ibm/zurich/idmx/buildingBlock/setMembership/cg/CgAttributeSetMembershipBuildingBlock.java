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

package com.ibm.zurich.idmx.buildingBlock.setMembership.cg;

import java.util.List;

import com.ibm.zurich.idmx.buildingBlock.factory.BuildingBlockFactory;
import com.ibm.zurich.idmx.buildingBlock.setMembership.AttributeSetMembershipBuildingBlock;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProver;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleVerifier;

import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.VerifierParameters;

public class CgAttributeSetMembershipBuildingBlock extends AttributeSetMembershipBuildingBlock {

  public CgAttributeSetMembershipBuildingBlock() {
    // TODO Auto-generated constructor stub
  }

  @Override
  protected String getBuildingBlockIdSuffix() {
    return super.getBuildingBlockIdSuffix().concat(":cg");
  }

  @Override
  protected String getImplementationIdSuffix() {
    return super.getImplementationIdSuffix().concat(":cg");
  }

  @Override
public ZkModuleProver getZkModuleProver(final SystemParameters systemParameters,
                                        final VerifierParameters verifierParameters, final String moduleId, final String attributeId,
      final List<BigInt> allowedValues, final BuildingBlockFactory buildingBlockFactory) {
    return new ProverModule(this, moduleId, attributeId, allowedValues, buildingBlockFactory);
  }

  @Override
public ZkModuleVerifier getZkModuleVerifier(final SystemParameters systemParameters,
                                            final VerifierParameters verifierParameters, final String moduleId, final String attributeId,
      final List<BigInt> allowedValues, final BuildingBlockFactory buildingBlockFactory) {
    return new VerifierModule(this, moduleId, attributeId, allowedValues, buildingBlockFactory);
  }

}
