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
package com.ibm.zurich.idmx.proofEngine.builderVerifier;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.ProofException;
import com.ibm.zurich.idmx.interfaces.proofEngine.ZkVerifier;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkVerifierStateCollect;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkVerifierStateVerify;
import com.ibm.zurich.idmx.proofEngine.HashComputationForChallenge;

import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.ZkProof;

public class ZkBuilderVerifier implements ZkVerifier {
  
  private final StateForVerifier state;
  private final Map<String, StatePerModule> moduleStates;
  private final Set<String> visitedModules;
  
  public ZkBuilderVerifier(final BigIntFactory bigIntFactory, final SystemParameters systemParameters,
		  final HashComputationForChallenge hcc) {
    this.state = new StateForVerifier(systemParameters, bigIntFactory, hcc);
    this.moduleStates = new HashMap<String, StatePerModule>();
    this.visitedModules = new HashSet<String>();
  }
  
  @Override
  public void loadProof(final ZkProof proof) throws ProofException, ConfigurationException {
    moduleStates.putAll(state.loadProof(proof));
  }

  @Override
  public ZkVerifierStateCollect getStateForCollect(final String moduleId) throws ProofException {
    if( !moduleStates.containsKey(moduleId)) {
      throw new ProofException("Unknwon module " + moduleId);
    }
    StatePerModule moduleState = moduleStates.get(moduleId);
    return new ZkBuilderVerifierCollect(moduleState, state);
  }

  @Override
  public void integrityCheckOfSValues() throws ConfigurationException, ProofException {
    state.assignSAndAttributeValues();
  }

  @Override
  public ZkVerifierStateVerify getStateForVerify(final String moduleId) throws ProofException {
    if( !moduleStates.containsKey(moduleId)) {
      throw new ProofException("Unknwon module " + moduleId);
    }
    visitedModules.add(moduleId);
    StatePerModule moduleState = moduleStates.get(moduleId);
    return new ZkBuilderVerifierVerify(moduleState, state);
  }

  @Override
  public void finalizeVerification() throws ProofException, ConfigurationException {
    for(final StatePerModule module: moduleStates.values()) {
      if( !visitedModules.contains(module.getModuleName())) {
        throw new ProofException("Module was never processed by verifier: " + module.getModuleName());
      }
      module.finalCheck(state);
    }
  }

}
