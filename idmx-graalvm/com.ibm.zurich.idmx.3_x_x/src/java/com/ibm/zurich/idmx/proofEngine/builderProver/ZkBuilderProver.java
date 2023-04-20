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
package com.ibm.zurich.idmx.proofEngine.builderProver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.ibm.zurich.idmx.buildingBlock.systemParameters.EcryptSystemParametersWrapper;
import com.ibm.zurich.idmx.configuration.Configuration;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.ProofException;
import com.ibm.zurich.idmx.exception.TopologicalSortFailedException;
import com.ibm.zurich.idmx.interfaces.device.DeviceProofCommitment;
import com.ibm.zurich.idmx.interfaces.device.DeviceProofResponse;
import com.ibm.zurich.idmx.interfaces.device.DeviceProofSpecification;
import com.ibm.zurich.idmx.interfaces.device.ExternalSecretsManager;
import com.ibm.zurich.idmx.interfaces.proofEngine.ZkBuilder;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.RandomGeneration;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateCollect;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateFirstRound;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateInitialize;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateSecondRound;
import com.ibm.zurich.idmx.proofEngine.HashComputationForChallenge;

import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.ZkProof;

public class ZkBuilderProver implements ZkBuilder {

  private final Map<String, StatePerModule> statePerModule;
  private final StateForProver state;
  private List<String> sortedListOfModules;
  private final ExternalSecretsManager secretsManager;
  private DeviceProofSpecification deviceSpec;
  private DeviceProofCommitment deviceCom;
  private DeviceProofResponse deviceResp;
  @SuppressWarnings("unused")
  private final EcryptSystemParametersWrapper sp;

  public ZkBuilderProver(final String username, final SystemParameters systemParameters,
		  final BigIntFactory bigIntFactory, final RandomGeneration rg,
		  final ExternalSecretsManager secretsManager, final HashComputationForChallenge hcc) {
    this.state = new StateForProver(systemParameters, bigIntFactory, rg, hcc);
    this.statePerModule = new LinkedHashMap<String, StatePerModule>();
    this.secretsManager = secretsManager;
    this.deviceSpec = secretsManager.newProofSpec(username);
    this.sp = new EcryptSystemParametersWrapper(systemParameters);
  }

  /*private void sanityCheckDeviceManager() {
    try {
      int expectedHashSize = sp.getHashLength();
      int actualHashSize = secretsManager.getChallengeSizeBytes() * 8;
      if (expectedHashSize != actualHashSize) {
        throw new RuntimeException(
            "Secrets manager not compatible with system parameters. Wrong challenge size. SP: "
                + expectedHashSize + " SM: " + actualHashSize);
      }

      int maxAttributeSize = sp.getAttributeLength();
      int actualAttributeSize = secretsManager.getAttributeSizeBytes() * 8;
      if (actualAttributeSize > maxAttributeSize) {
        throw new RuntimeException(
            "Secrets manager not compatible with system parameters. Secret size too large. Max (SP): "
                + maxAttributeSize + " Actual (SM): " + actualAttributeSize);
      }

      int maxRSize = sp.getSizeOfRValue(-1);
      int actualRSize = secretsManager.getRandomizerSizeBytes();
      if (actualRSize > maxRSize) {
        throw new RuntimeException(
            "Secrets manager not compatible with system parameters. R-size too large. Max (SP): "
                + maxRSize + " Actual (SM): " + actualRSize);
      }
    } catch (ConfigurationException e) {
      throw new RuntimeException(e);
    }
  }*/

  @Override
  public ZkProofStateInitialize getStateForInitialize(final String moduleId) {
    final StatePerModule moduleState = new StatePerModule(moduleId);
    statePerModule.put(moduleId, moduleState);

    return new ZkBuilderProverInitialize(moduleState, state, deviceSpec);
  }

  @Override
  public List<String> topologicallySortModules() throws TopologicalSortFailedException {
    if (state.hasExternalSecrets()) {
      //sanityCheckDeviceManager();
    }
    sortedListOfModules = state.sortModules(statePerModule.keySet());
    return Collections.unmodifiableList(sortedListOfModules);
  }

  @Override
  public ZkProofStateCollect getStateForCollect(final String moduleId) {
    final StatePerModule moduleState = statePerModule.get(moduleId);
    return new ZkBuilderProverCollect(moduleState, state, deviceSpec);
  }

  @Override
  public void assignRValues() throws ConfigurationException, ProofException {
    state.checkLinearCombinations();
    state.assignRValues();
    deviceCom = secretsManager.getPresentationCommitment(deviceSpec);
  }

  @Override
  public ZkProofStateFirstRound getStateForFirst(final String moduleId) {
    final StatePerModule moduleState = statePerModule.get(moduleId);
    return new ZkBuilderProverFirst(moduleState, state, deviceCom);
  }

  @Override
  public void computeChallenge() throws ConfigurationException {
    int positionInHash = 0;
    final List<byte[]> hashContributions = new ArrayList<byte[]>();
    for (final String moduleName : sortedListOfModules) {
      final StatePerModule moduleState = statePerModule.get(moduleName);
      moduleState.computeHashContribution(state);
      final byte[] hashContribution = moduleState.getHashContribution();
      if (hashContribution != null) {
        hashContributions.add(moduleState.getHashContribution());
        moduleState.positionInHash = positionInHash;
        positionInHash++;
      }
    }
    state.computeChallenge(hashContributions);
    deviceResp = secretsManager.getPresentationResponse(deviceCom, state.getChallenge().getValue());
  }

  @Override
public List<String> sortModulesForChallengeComputation() {
    final List<String> topSorted = sortedListOfModules;
    final Set<String> alphaSorted = new TreeSet<String>(topSorted);
    final List<String> challengeSorted = new ArrayList<String>();
    
    // Sort module names alphabetically, but move first signature (if exists) to front
    for (final String moduleName : topSorted) {
      final StatePerModule moduleState = statePerModule.get(moduleName);
      if(moduleState.isMovedToFront()) {
        challengeSorted.add(moduleName);
        alphaSorted.remove(moduleName);
        break;
      }
    }
    challengeSorted.addAll(alphaSorted);
    
    sortedListOfModules = challengeSorted;
    return Collections.unmodifiableList(challengeSorted);
  }

  @Override
  public ZkProofStateSecondRound getStateForSecond(final String moduleId) {
    final StatePerModule moduleState = statePerModule.get(moduleId);
    return new ZkBuilderProverSecond(moduleState, state, deviceResp);
  }

  @Override
  public ZkProof assembleProof() {
    final Collection<StatePerModule> moduleList = new ArrayList<StatePerModule>();
    for (final String moduleName : sortedListOfModules) {
      moduleList.add(statePerModule.get(moduleName));
    }
    return state.serialize(moduleList, Configuration.verboseProofXml());
  }

}
