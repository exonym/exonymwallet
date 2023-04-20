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

import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProver;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ResidueClass;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateCollect;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateFirstRound;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateInitialize;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateSecondRound;
import com.ibm.zurich.idmx.zkModule.ZkModuleImpl;

public class ProverModule extends ZkModuleImpl implements ZkModuleProver {

  private final String attributeId;
  private final BigInt value;

  public ProverModule(ConstantBuildingBlock parent, String identifierOfModule, String attributeId,
      BigInt value) {

    super(parent, identifierOfModule);

    this.attributeId = attributeId;
    this.value = value;
  }


  @Override
  public void initializeModule(ZkProofStateInitialize zkBuilder) {
    zkBuilder.registerAttribute(attributeId, false);
    zkBuilder.attributeIsRevealed(attributeId);
    zkBuilder.setValueOfAttribute(attributeId, value, ResidueClass.INTEGER_IN_RANGE);
  }

  @Override
  public void collectAttributesForProof(ZkProofStateCollect zkBuilder) {
    // Nothing to do
  }

  @Override
  public void firstRound(ZkProofStateFirstRound zkBuilder) throws ConfigurationException {
    // Suppress the hash contribution we would get from revealing the attribute
    zkBuilder.setHashContributionOfBuildingBlock(null);
  }

  @Override
  public void secondRound(ZkProofStateSecondRound zkBuilder) {
    // Nothing to do
  }

}
