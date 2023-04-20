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

package com.ibm.zurich.idmx.buildingBlock.structural.verifierParameters;

import com.ibm.zurich.idmx.buildingBlock.GeneralBuildingBlock;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProver;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleVerifier;

import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.VerifierParameters;

public class VerifierParametersBuildingBlock extends GeneralBuildingBlock {

  public VerifierParametersBuildingBlock() {
    // TODO Auto-generated constructor stub
  }

  @Override
  protected String getBuildingBlockIdSuffix() {
    return "s-param-v";
  }

  @Override
  protected String getImplementationIdSuffix() {
    return "s-param-v";
  }

  public ZkModuleProver getZkModuleProver(final String identifierOfModule, final SystemParameters sp,
                                          final VerifierParameters vp) {
    return new ProverModule(this, identifierOfModule, sp, vp);
  }

  public ZkModuleVerifier getZkModuleVerifier(final String identifierOfModule, final SystemParameters sp,
                                              final VerifierParameters vp) {
    return new VerifierModule(this, identifierOfModule, sp, vp);
  }

}
