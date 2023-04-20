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
package com.ibm.zurich.idmx.buildingBlock.helper.representation.modNSquare;

import java.net.URI;
import java.util.List;
import java.util.logging.Logger;

import com.ibm.zurich.idmx.annotations.Nullable;
import com.ibm.zurich.idmx.buildingBlock.factory.BuildingBlockFactory;
import com.ibm.zurich.idmx.buildingBlock.helper.BaseForRepresentation;
import com.ibm.zurich.idmx.buildingBlock.helper.representation.ProverModulePresentation;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.ProofException;
import com.ibm.zurich.idmx.interfaces.device.ExternalSecretsManager;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.RandomGeneration;
import com.ibm.zurich.idmx.interfaces.util.group.PaillierGroup;
import com.ibm.zurich.idmx.interfaces.util.group.PaillierGroupElement;
import com.ibm.zurich.idmx.interfaces.util.group.PaillierGroupMultOpSequence;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateFirstRound;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateInitialize;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateSecondRound;

import eu.abc4trust.xml.SystemParameters;

public class ProverModule
    extends ProverModulePresentation<ModNSquareRepresentationBuildingBlock, PaillierGroup, PaillierGroupElement, PaillierGroupMultOpSequence> {

  public ProverModule(final ModNSquareRepresentationBuildingBlock parent, final String identifierOfModule,
                      final SystemParameters systemParameters, final List<BaseForRepresentation> bases, final PaillierGroup group,
      final @Nullable URI deviceUid, final String username, final @Nullable URI identifierOfCredentialForSecret, final @Nullable URI scope,
      final @Nullable PaillierGroupElement commitment, final BuildingBlockFactory bbFactory,
      final ExternalSecretsManager esManager, final RandomGeneration randomGeneration, final Logger logger,
      final BigIntFactory bigIntFactory) {

    super(parent, identifierOfModule, systemParameters, bases, group, deviceUid,
        username, identifierOfCredentialForSecret, scope, commitment, false, bbFactory, esManager,
        randomGeneration, logger, bigIntFactory);
  }

  @Override
  public void initializeModule(final ZkProofStateInitialize zkBuilder) throws ConfigurationException {
    findProofTypeAndIndicesOfExternalVariables(zkBuilder);
    /*
     * register all attributes that are to be used in the building block; tell the proof engine
     * which attributes will be provided, and choose random attributes
     */
    for (int i = 0; i < bases.size(); i++) {
      if (bases.get(i).chooseExponentRandomly == false) {
        zkBuilder.registerAttribute(identifierOfAttribute(i), false);
      } else {
        zkBuilder.registerAttribute(identifierOfAttribute(i), false, group
            .getRandomIterationcounterLengthForSubgroupOfOrderPhiN(spWrapper.getStatisticalInd()));
      }

      if (indicesOfRandomlyChosenAttributes.indexOf(i) != -1) {
        zkBuilder.providesAttribute(identifierOfAttribute(i));
        // choose and assign random attributes
        final BigInt r =
            group.createRandomIterationcounterForSubgroupOfOrderPhiN(rg,
                spWrapper.getStatisticalInd());
        zkBuilder.setValueOfAttribute(identifierOfAttribute(i), r, null);
        valuesOfRandomlyChosenAttributes.add(r);
      } else {
        zkBuilder.requiresAttributeValue(identifierOfAttribute(i));
      }
    }
  }

  @Override
  public void secondRound(final ZkProofStateSecondRound zkBuilder) throws ConfigurationException {
    // nothing to do here
  }

  @Override
  protected PaillierGroupElement getDeviceTValuePortion(final ZkProofStateFirstRound zkBuilder)
      throws ConfigurationException, ProofException {
    return group.neutralElement();
  }
}
