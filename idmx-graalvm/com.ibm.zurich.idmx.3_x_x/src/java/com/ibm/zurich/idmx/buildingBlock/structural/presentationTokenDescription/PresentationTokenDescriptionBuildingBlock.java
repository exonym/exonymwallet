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

package com.ibm.zurich.idmx.buildingBlock.structural.presentationTokenDescription;

import com.ibm.zurich.idmx.buildingBlock.GeneralBuildingBlock;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProver;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleVerifier;

import eu.abc4trust.xml.PresentationTokenDescription;
import eu.abc4trust.xml.SystemParameters;

public class PresentationTokenDescriptionBuildingBlock extends GeneralBuildingBlock {

  public PresentationTokenDescriptionBuildingBlock() {
    // TODO Auto-generated constructor stub
  }

  @Override
  protected String getBuildingBlockIdSuffix() {
    return "s-abc4trust-ptd";
  }

  @Override
  protected String getImplementationIdSuffix() {
    return "s-abc4trust-ptd";
  }

  public ZkModuleProver getZkModuleProver(final String identifierOfModule, final SystemParameters sp,
                                          final PresentationTokenDescription ptd) {
    return new ProverModule(this, identifierOfModule, sp, ptd);
  }

  public ZkModuleVerifier getZkModuleVerifier(final String identifierOfModule, final SystemParameters sp,
                                              final PresentationTokenDescription ptd) {
    return new VerifierModule(this, identifierOfModule, sp, ptd);
  }

}
