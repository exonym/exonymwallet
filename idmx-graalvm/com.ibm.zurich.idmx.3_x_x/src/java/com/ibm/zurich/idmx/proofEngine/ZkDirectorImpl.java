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
package com.ibm.zurich.idmx.proofEngine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.ibm.zurich.idmx.configuration.Configuration;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.ProofException;
import com.ibm.zurich.idmx.exception.TopologicalSortFailedException;
import com.ibm.zurich.idmx.interfaces.device.ExternalSecretsManager;
import com.ibm.zurich.idmx.interfaces.proofEngine.ZkBuilder;
import com.ibm.zurich.idmx.interfaces.proofEngine.ZkDirector;
import com.ibm.zurich.idmx.interfaces.proofEngine.ZkVerifier;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.RandomGeneration;
import com.ibm.zurich.idmx.interfaces.util.group.GroupFactory;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProver;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleVerifier;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateCollect;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateFirstRound;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateInitialize;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateSecondRound;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkVerifierStateCollect;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkVerifierStateVerify;
import com.ibm.zurich.idmx.proofEngine.builderProver.ZkBuilderProver;
import com.ibm.zurich.idmx.proofEngine.builderVerifier.ZkBuilderVerifier;

import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.ZkProof;

import javax.inject.Inject;

/**
 * Builds and verifies zero-knowledge proofs using the Builder pattern.
 */
public class ZkDirectorImpl implements ZkDirector {

  private final Logger logger;
  private final BigIntFactory bigIntFactory;
  private final RandomGeneration randomGeneration;
  private final ExternalSecretsManager secretsManager;
  private final HashComputationForChallenge hcc;

  @Inject
  public ZkDirectorImpl(final Logger logger, final BigIntFactory bigIntFactory,
		  final GroupFactory groupFactory, final RandomGeneration rg,
		  final ExternalSecretsManager secretsManager, final HashComputationForChallenge hcc) {
    this.logger = logger;
    this.bigIntFactory = bigIntFactory;
    this.randomGeneration = rg;
    this.secretsManager = secretsManager;
    this.hcc = hcc;
  }

  @Override
  public ZkProof buildProof(final String username, List<? extends ZkModuleProver> modules,
		  final SystemParameters sp)
      throws ConfigurationException, ProofException {
    final ZkBuilder zkBuilder =
        new ZkBuilderProver(username, sp, bigIntFactory, randomGeneration, secretsManager, hcc);

    for (final ZkModuleProver module : modules) {
      final String name = module.getIdentifier();
      final ZkProofStateInitialize zkBuilderInit = zkBuilder.getStateForInitialize(name);
      module.initializeModule(zkBuilderInit);
    }

    try {
      modules = sortModules(modules, zkBuilder.topologicallySortModules());
    } catch (TopologicalSortFailedException e) {
      throw new ProofException(e);
    }

    for (final ZkModuleProver module : modules) {
      final String name = module.getIdentifier();
      final ZkProofStateCollect zkBuilderCollect = zkBuilder.getStateForCollect(name);
      module.collectAttributesForProof(zkBuilderCollect);
    }

    zkBuilder.assignRValues();

    for (final ZkModuleProver module : modules) {
      final String name = module.getIdentifier();
      final ZkProofStateFirstRound zkBuilderFirst = zkBuilder.getStateForFirst(name);
      module.firstRound(zkBuilderFirst);
    }

    modules = sortModules(modules, zkBuilder.sortModulesForChallengeComputation());
    zkBuilder.computeChallenge();

    for (final ZkModuleProver module : modules) {
      final String name = module.getIdentifier();
      final ZkProofStateSecondRound zkBuilderSecond = zkBuilder.getStateForSecond(name);
      module.secondRound(zkBuilderSecond);
    }

    return zkBuilder.assembleProof();
  }


  private List<ZkModuleProver> sortModules(final List<? extends ZkModuleProver> unsortedModules,
		  final List<String> sortedModuleNames) {
    if (unsortedModules.size() != sortedModuleNames.size()) {
      throw new RuntimeException("Incompatible size");
    }

    final Map<String, ZkModuleProver> moduleMap = new HashMap<String, ZkModuleProver>();
    for (final ZkModuleProver module : unsortedModules) {
      moduleMap.put(module.getIdentifier(), module);
    }

    final List<ZkModuleProver> sortedModules = new ArrayList<ZkModuleProver>();
    for (final String name : sortedModuleNames) {
      final ZkModuleProver module = moduleMap.remove(name);
      if (module == null) {
        throw new RuntimeException("Could not find module with ID: " + name);
      }
      sortedModules.add(module);
    }

    if (moduleMap.size() != 0) {
      throw new RuntimeException("Not all modules were used in sortModules().");
    }

    return sortedModules;
  }


  @Override
  public boolean verifyProof(final ZkProof proof, List<? extends ZkModuleVerifier> modules,
		  final SystemParameters sp) throws ConfigurationException {
    final ZkVerifier zkVerifier = new ZkBuilderVerifier(bigIntFactory, sp, hcc);

    // Load proof
    try {
      zkVerifier.loadProof(proof);
    } catch (ProofException e) {
      logError(e);
      return false;
    }

    // Collect phase
    boolean ok = true;
    for (final ZkModuleVerifier module : modules) {
      try {
        final String name = module.getIdentifier();
        final ZkVerifierStateCollect zkVerifierCollect = zkVerifier.getStateForCollect(name);
        module.collectAttributesForVerify(zkVerifierCollect);
      } catch (ProofException e) {
        logError(e);
        ok = false;
      }
    }
    if (!ok) {
      return false;
    }

    // Re-compute SValues
    try {
      zkVerifier.integrityCheckOfSValues();
    } catch (ProofException e) {
      logError(e);
      return false;
    }

    // Verification phase
    for (final ZkModuleVerifier module : modules) {
      final String name = module.getIdentifier();
      try {
        final ZkVerifierStateVerify zkVerifierVerify = zkVerifier.getStateForVerify(name);
        final boolean resultForModule = module.verify(zkVerifierVerify);
        if (!resultForModule) {
          logger.warning("Verification failed for module: " + name);
          ok = false;
        }
      } catch (ProofException e) {
        logger.warning("Verification failed for module: " + name);
        logError(e);
        ok = false;
      }
    }
    if (!ok) {
      return false;
    }

    // Final check
    try {
      zkVerifier.finalizeVerification();
    } catch (ProofException e) {
      logError(e);
      return false;
    }

    return true;
  }

  private void logError(ProofException e) {
    logger.warning(e.getMessage());
    if (Configuration.printStackTraces()) {
      e.printStackTrace();
    }
  }
}
