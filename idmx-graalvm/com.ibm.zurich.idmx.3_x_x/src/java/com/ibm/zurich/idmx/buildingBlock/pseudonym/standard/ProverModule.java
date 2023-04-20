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
package com.ibm.zurich.idmx.buildingBlock.pseudonym.standard;

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
import com.ibm.zurich.idmx.interfaces.util.BigInt;
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
import eu.abc4trust.xml.StandardPseudonym;
import eu.abc4trust.xml.SystemParameters;

public class ProverModule extends ZkModuleImpl implements ZkModuleProver {
  private final ZkModuleProver delegatee;

  // private final String identifierOfModule;
  private final BigInt openingInformation;
  private final KnownOrderGroupElement comm;

  public ProverModule(final PseudonymBuildingBlock parent, final String identifierOfModule,
                      final SystemParameters systemParameters, final @Nullable URI deviceUid, final String username, final @Nullable URI scope,
      final AbstractPseudonym pseudonym, final BuildingBlockFactory bbFactory,
      final ExternalSecretsManager esManager, final RandomGeneration randomGeneration, final Logger logger,
      final BigIntFactory bigIntFactory) throws ProofException, ConfigurationException {

    // scope of std. pseudonym not used cryptographically; crypto engine does not use them
    // List<BaseForRepresentation> bases: need to create; ask SecretManager for them; should
    // be same as in syspars
    // group: use DH pars from sysparameters
    // first base of DH is one with secret; check that this is the same as on card
    // card computes g^x; card public key; fixed for card; get through esManager

    // baseforrep: tell that first is on card
    // register both attr; 1st external; 2nd: not
    // equality between my attribute and attribute of Pedersen block; smart card one; other also
    // set second attribute to opening information of the pseudonym
    // UProveIssuanceTest

    // do not use Pseudonym class here as comprises some ABC4Trust things we do not need

    super(parent, identifierOfModule);

    final StandardPseudonym standardPseudonym = (StandardPseudonym) pseudonym;
    // this.identifierOfModule = identifierOfModule;
    final EcryptSystemParametersWrapper syspar = new EcryptSystemParametersWrapper(systemParameters);

    final KnownOrderGroup group =
        new GroupFactoryImpl().createPrimeOrderGroup(syspar.getDHModulus(),
            syspar.getDHSubgroupOrder());
    final KnownOrderGroupElement base1 = group.valueOf(syspar.getDHGenerator1());
    final KnownOrderGroupElement base2 = group.valueOf(syspar.getDHGenerator2());

    if (!esManager.getPublicKeyBase(username, deviceUid).equals(base1.toBigInt().getValue())) {
      throw new ConfigurationException(
          "Base for scope-exclusive pseudonym was set incorrectly (different values found on smartcard and given, or NULL)!");
    }

    if (!equals(pseudonym.getScope(), scope)) {
      throw new ConfigurationException(
          "Scope mismatch: Scopes of pseudonym and the prove building block do not match when generating prover protocol.");
    }

    final List<BaseForRepresentation> basesForRep = new ArrayList<BaseForRepresentation>();
    final BaseForRepresentation baseForRep1 = BaseForRepresentation.deviceSecret(base1);
    final BaseForRepresentation baseForRep2 = BaseForRepresentation.managedAttribute(base2);
    basesForRep.add(baseForRep1);
    basesForRep.add(baseForRep2);

    // use valueOfNoCheck as we are reusing a pseudonym that has been created through a local
    // protocol, it should be well-formed
    this.comm = group.valueOfNoCheck(bigIntFactory.valueOf(standardPseudonym.getValue()));

    this.openingInformation = bigIntFactory.valueOf(standardPseudonym.getOpeningInformation());

    final PedersenRepresentationBuildingBlock pedersenBB =
        bbFactory.getBuildingBlockByClass(PedersenRepresentationBuildingBlock.class);
    // attributes are passed through the ZK builder and not explicitly
    delegatee =
        pedersenBB.getZkModuleProver(systemParameters, identifierOfModule + ":rep", null,
            basesForRep, group, comm, deviceUid, username, null);

  }

  private boolean equals(final URI lhs, final URI rhs) {
    if (lhs == null && rhs == null) {
      return true;
    } else if (lhs == null || rhs == null) {
      return false;
    }
    return lhs.equals(rhs);
  }

  @Override
  public void initializeModule(final ZkProofStateInitialize zkBuilder) throws ConfigurationException {
    delegatee.initializeModule(zkBuilder);
    zkBuilder.setValueOfAttribute(delegatee.identifierOfAttribute(1), openingInformation, null);
    // Make secret available externally
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
