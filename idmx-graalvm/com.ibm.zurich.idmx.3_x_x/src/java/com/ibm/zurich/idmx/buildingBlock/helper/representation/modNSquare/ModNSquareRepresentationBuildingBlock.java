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
import com.ibm.zurich.idmx.buildingBlock.helper.representation.RepresentationBuildingBlock;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.ProofException;
import com.ibm.zurich.idmx.interfaces.device.ExternalSecretsManager;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.RandomGeneration;
import com.ibm.zurich.idmx.interfaces.util.group.PaillierGroup;
import com.ibm.zurich.idmx.interfaces.util.group.PaillierGroupElement;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProverCommitment;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleVerifier;

import eu.abc4trust.xml.SystemParameters;

import javax.inject.Inject;

public class ModNSquareRepresentationBuildingBlock
    extends RepresentationBuildingBlock<PaillierGroup, PaillierGroupElement> {
  @Inject
  public ModNSquareRepresentationBuildingBlock(final RandomGeneration rg, final BuildingBlockFactory bbFactory,
                                               final ExternalSecretsManager esManager, final Logger logger, final BigIntFactory bigIntFactory) {
    super(rg, bbFactory, esManager, logger, bigIntFactory);
  }

  @Override
  protected String getBuildingBlockIdSuffix() {
    return "h-rep-paillier";
  }

  @Override
  protected String getImplementationIdSuffix() {
    return "h-rep-paillier";
  }

  @Override
  public ZkModuleProverCommitment<PaillierGroupElement> getZkModuleProver(final SystemParameters systemParameters,
    final String identifierOfModule, final @Nullable URI identifierOfCredentialForSecret,
      final List<BaseForRepresentation> bases, final PaillierGroup group,
      final @Nullable PaillierGroupElement commitment, final @Nullable URI deviceUid, final String username, final @Nullable URI scope)
      throws ConfigurationException {
    final ProverModule zkModule =
        new ProverModule(this, identifierOfModule, systemParameters, bases, group, deviceUid,
            username, identifierOfCredentialForSecret, scope, commitment, bbFactory, esManager,
            randomGeneration, logger, bigIntFactory);
    return zkModule;
  }

  @Override
  public ZkModuleVerifier getZkModuleVerifier(final SystemParameters systemParameters,
                                              final String identifierOfModule, final List<BaseForRepresentation> bases,
      final @Nullable PaillierGroupElement commitment, final @Nullable String commitmentAsDValue,
      final @Nullable PaillierGroup group) throws ProofException {
    final BuildingBlockFactory bbFactory = null;
    final ZkModuleVerifier zkModule =
        new VerifierModule(this, identifierOfModule, systemParameters, bases, group, commitment,
            commitmentAsDValue, bbFactory);
    return zkModule;
  }

}
