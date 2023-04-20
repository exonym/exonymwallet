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
package com.ibm.zurich.idmx.buildingBlock.pseudonym.scopeExclusive;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.ibm.zurich.idmx.annotations.Nullable;
import com.ibm.zurich.idmx.buildingBlock.factory.BuildingBlockFactory;
import com.ibm.zurich.idmx.buildingBlock.helper.BaseForRepresentation;
import com.ibm.zurich.idmx.buildingBlock.helper.representation.pedersen.PedersenRepresentationBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.pseudonym.PseudonymBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.systemParameters.EcryptSystemParametersWrapper;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.ProofException;
import com.ibm.zurich.idmx.interfaces.device.ExternalSecretsManager;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.RandomGeneration;
import com.ibm.zurich.idmx.interfaces.util.group.KnownOrderGroup;
import com.ibm.zurich.idmx.interfaces.util.group.KnownOrderGroupElement;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProver;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateCollect;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateFirstRound;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateInitialize;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateSecondRound;
import com.ibm.zurich.idmx.util.group.GroupFactoryImpl;
import com.ibm.zurich.idmx.zkModule.ZkModuleImpl;

import eu.abc4trust.xml.AbstractPseudonym;
import eu.abc4trust.xml.ScopeExclusivePseudonym;
import eu.abc4trust.xml.SystemParameters;

public class ProverModule extends ZkModuleImpl implements ZkModuleProver {
  ZkModuleProver delegatee;

  @SuppressWarnings("unused")
  private final String identifierOfModule;
  private final KnownOrderGroupElement comm;

  public ProverModule(final PseudonymBuildingBlock parent, final String identifierOfModule,
                      final SystemParameters systemParameters, final @Nullable URI deviceUid,  final String username, final URI scope,
      final AbstractPseudonym pseudonym, final BuildingBlockFactory bbFactory,
      final ExternalSecretsManager esManager, final RandomGeneration randomGeneration, final Logger logger,
      final BigIntFactory bigIntFactory) throws ProofException, ConfigurationException {

    super(parent, identifierOfModule);

    final ScopeExclusivePseudonym scopeExclusivePseudonym = (ScopeExclusivePseudonym) pseudonym;
    this.identifierOfModule = identifierOfModule;
    final EcryptSystemParametersWrapper syspar = new EcryptSystemParametersWrapper(systemParameters);

    final KnownOrderGroup group =
        new GroupFactoryImpl().createPrimeOrderGroup(syspar.getDHModulus(),
            syspar.getDHSubgroupOrder());
    final KnownOrderGroupElement base1 =
        group.valueOf(bigIntFactory.valueOf(esManager.getBaseForScopeExclusivePseudonym(username, scope,
            syspar.getDHModulus().getValue(), syspar.getDHSubgroupOrder().getValue())));
    if (!pseudonym.getScope().equals(scope)) {
      throw new ConfigurationException(
          "Scope mismatch: Scopes of pseudonym and the prove building block do not match when generating prover protocol.");
    }

    final List<BaseForRepresentation> basesForRep = new ArrayList<BaseForRepresentation>();
    final BaseForRepresentation baseForRep1 = BaseForRepresentation.deviceSecret(base1);
    basesForRep.add(baseForRep1);

    // TODO maybe replace with valueOfNoCheck later once code is running
    // reusing a pseudonym that has been created through a local protocol
    // should be well-formed
    this.comm = group.valueOf(bigIntFactory.valueOf(scopeExclusivePseudonym.getValue()));

    final PedersenRepresentationBuildingBlock pedersenBB =
        bbFactory.getBuildingBlockByClass(PedersenRepresentationBuildingBlock.class);

    delegatee =
        pedersenBB.getZkModuleProver(systemParameters, identifierOfModule + ":rep", null,
            basesForRep, group, comm, deviceUid, username, scope);

  }

  @Override
  public void initializeModule(final ZkProofStateInitialize zkBuilder) throws ConfigurationException {
    delegatee.initializeModule(zkBuilder);

    zkBuilder.registerAttribute(identifierOfSecretAttribute(), true);

    zkBuilder.attributesAreEqual(identifierOfSecretAttribute(), delegatee.identifierOfAttribute(0));
  }

  @Override
  public void collectAttributesForProof(final ZkProofStateCollect zkBuilder) throws ConfigurationException {
    delegatee.collectAttributesForProof(zkBuilder);
  }

  @Override
  public void firstRound(final ZkProofStateFirstRound zkBuilder) throws ConfigurationException,
      ProofException {
    delegatee.firstRound(zkBuilder);
  }

  @Override
  public void secondRound(final ZkProofStateSecondRound zkBuilder) throws ConfigurationException {
    delegatee.secondRound(zkBuilder);
  }

}
