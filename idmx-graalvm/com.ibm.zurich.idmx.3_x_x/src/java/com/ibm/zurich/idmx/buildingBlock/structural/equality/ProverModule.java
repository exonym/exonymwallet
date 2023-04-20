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

import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProver;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateCollect;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateFirstRound;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateInitialize;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateSecondRound;
import com.ibm.zurich.idmx.zkModule.ZkModuleImpl;

public class ProverModule extends ZkModuleImpl implements ZkModuleProver {

  private final String lhsAttribute;
  private final String rhsAttribute;
  private final boolean external;

  public ProverModule(final AttributeEqualityBuildingBlock parent, final String identifierOfModule,
                      final String lhsAttribute, final String rhsAttribute, final boolean external) {

    super(parent, identifierOfModule);

    this.lhsAttribute = lhsAttribute;
    this.rhsAttribute = rhsAttribute;
    this.external = external;
  }


  @Override
  public void initializeModule(final ZkProofStateInitialize zkBuilder) {
    zkBuilder.registerAttribute(lhsAttribute, external);
    zkBuilder.registerAttribute(rhsAttribute, external);
    zkBuilder.attributesAreEqual(lhsAttribute, rhsAttribute);
  }

  @Override
  public void collectAttributesForProof(final ZkProofStateCollect zkBuilder) {
    // Nothing to do
  }

  @Override
  public void firstRound(final ZkProofStateFirstRound zkBuilder) throws ConfigurationException {
    // Suppress the hash contribution we would get from revealing the attribute
    zkBuilder.setHashContributionOfBuildingBlock(null);
  }

  @Override
  public void secondRound(final ZkProofStateSecondRound zkBuilder) {
    // Nothing to do
  }

}
