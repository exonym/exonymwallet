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
package com.ibm.zurich.idmx.interfaces.proofEngine;

import java.util.List;

import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.ProofException;
import com.ibm.zurich.idmx.exception.TopologicalSortFailedException;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateCollect;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateFirstRound;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateInitialize;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateSecondRound;

import eu.abc4trust.xml.ZkProof;

public interface ZkBuilder {

  ZkProofStateInitialize getStateForInitialize(final String moduleId);

  /**
   * Returns a list of identifiers of top-level ZkModules in an order that satisfies the following
   * condition: If a module A requires an attribute B, and a module C provides that attribute B,
   * then C comes before A.
   * 
   * @return Such an ordering.
   * @throws TopologicalSortFailedException if no such ordering exists
   */
  List<String> topologicallySortModules() throws TopologicalSortFailedException;
  
  /**
   * Returns a list of identifiers of top-level ZkModules that are sorted alphabetically
   * (according to their module names), except that the first signature building block (if present)
   * is moved to the front. This exception is needed for compatibility with software that wishes
   * to check only the first signature building block for correctness.
   * @return
   */
  List<String> sortModulesForChallengeComputation();

  ZkProofStateCollect getStateForCollect(final String moduleId);

  /**
   * When this function is called, the zkBuilder assigns R-values to all unrevealed attributes for
   * which it knows the value. The R-value of all attributes in an equivalence class must be
   * compatible (i.e., if two attributes are equal, their R-value must be the same; if there exists
   * a linear relationship between attributes, then their R-value must also satisfy this linear
   * relationship). The R-value for a given equivalence class are chosen independently uniformly at
   * random within its domain from R-values in other equivalence classes.
   * @throws ConfigurationException 
   * @throws ProofException 
   */
  void assignRValues() throws ConfigurationException, ProofException;

  ZkProofStateFirstRound getStateForFirst(final String moduleId);

  /**
   * When this function is called, the zkBuilder computes the value of the challenge from the list
   * of hash contributions, in the order in which they appear in the proof.
   * @throws ConfigurationException 
   */
  void computeChallenge() throws ConfigurationException;

  ZkProofStateSecondRound getStateForSecond(final String moduleId);

  /**
   * @return The completed zero-knowledge proof object.
   */
  ZkProof assembleProof();
}
