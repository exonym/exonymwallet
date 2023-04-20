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
package com.ibm.zurich.idmx.buildingBlock.helper.representation.pedersen;

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
import com.ibm.zurich.idmx.interfaces.util.group.KnownOrderGroup;
import com.ibm.zurich.idmx.interfaces.util.group.KnownOrderGroupElement;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProverCommitment;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleVerifier;

import eu.abc4trust.xml.SystemParameters;

import javax.inject.Inject;

/**
 * This block is used for generating a Pedersen commitment of a list of attributes (including
 * secrets), and proving knowledge of the representation. This block is intended to be used as a
 * sub-block by other building blocks. This block is very complex, as it has to satisfy many
 * use-cases.
 */
public class PedersenRepresentationBuildingBlock
    extends RepresentationBuildingBlock<KnownOrderGroup, KnownOrderGroupElement> {
  @Inject
  public PedersenRepresentationBuildingBlock(final RandomGeneration rg, final BuildingBlockFactory bbFactory,
                                             final ExternalSecretsManager esManager, final Logger logger, final BigIntFactory bigIntFactory) {
    super(rg, bbFactory, esManager, logger, bigIntFactory);
  }

  @Override
  protected String getBuildingBlockIdSuffix() {
    return "h-rep-pedersen";
  }

  @Override
  protected String getImplementationIdSuffix() {
    return "h-rep-pedersen";
  }

  /**
   * For all bases where chooseExponentRandomly is true, this method chooses a random value between
   * 0 and subgroupOrder - 1 for the corresponding attribute, and sets that attribute value during
   * the initialization phase. If a secret is used, exactly one base must have hasExternalSecret
   * set, and at most one base may be a credential secret key, which further may have non-null
   * externalRandomizerOffset. This Block will call the External Secret Manager to delegate part of
   * the proof to the external device. It then generates a ZkModuleProver that does the following:
   * (1) It then generates the Pedersen commitment C= PROD base^exponent (mod modulus) , possibly
   * with the help of the external device (though the External Secret Manager); (2) it proves
   * knowledge of the attribute values contained in the commitment possibly with the help of the
   * external device (through the External Secret Manager). It is the caller's responsibility to
   * provide or set attribute values that are available and require attribute values that must be
   * copied from other blocks.
   * 
   * @throws ConfigurationException
   */
  @Override
  public ZkModuleProverCommitment<KnownOrderGroupElement> getZkModuleProver(final SystemParameters systemParameters,
      final String identifierOfModule, final @Nullable URI identifierOfCredentialForSecret,
      final List<BaseForRepresentation> bases, final KnownOrderGroup group,
      final @Nullable KnownOrderGroupElement commitment, final @Nullable URI deviceUid, final String username, final @Nullable URI scope)
      throws ConfigurationException {

    final ProverModule zkModule =
        new ProverModule(this, identifierOfModule, systemParameters, bases, group, deviceUid,
            username, identifierOfCredentialForSecret, scope, commitment, bbFactory, esManager,
            randomGeneration, logger, bigIntFactory);
    return zkModule;
  }

  /**
   * This method creates a new ZkModuleVerifier object that will know how to verify a proof of
   * knowledge of the representation generated by getZkModuleProver.
   */
  @Override
  public ZkModuleVerifier getZkModuleVerifier(final SystemParameters systemParameters,
                                              final String identifierOfModule, final List<BaseForRepresentation> bases,
      final @Nullable KnownOrderGroupElement commitment, final @Nullable String commitmentAsDValue,
      final @Nullable KnownOrderGroup group) throws ProofException {
    final BuildingBlockFactory bbFactory = null;
    final ZkModuleVerifier zkModule =
        new VerifierModule(this, identifierOfModule, systemParameters, bases, group, commitment,
            commitmentAsDValue, bbFactory);
    return zkModule;
  }
}
