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

package com.ibm.zurich.idmx.interfaces.zkModule;

import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.ProofException;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateCollect;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateFirstRound;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateInitialize;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateSecondRound;

/**
 * When a proof is being conducted, each of the following methods will be called (in the order given
 * below) on each of the ZkModuleProver participating in the proof. In all of these method calls,
 * the ZkModuleProver is given a reference to ZkBuilder with which it is supposed to interact. The
 * ZkBuilder is responsible for centralizing the actions of all ZkModules. (We note here that there
 * is exactly one ZkBuilder that is active during the proof, and that the ZkModuleProver is given a
 * reference to a "firewalled" version of the former, which restricts the methods that can be
 * called).
 */
public interface ZkModuleProver extends ZkModule {

  /**
   * Notify the zkBuilder of all attributes (including temporary variables) that it will use during
   * the proof. Notify the zkBuilder which attribute values this ZkModule requires from other
   * ZkModules, and which attribute values this ZkModule will provide for other ZkModules (the
   * modules will be topologically sorted after this function call). The method may declare that an
   * attribute is to be revealed, or that two attributes are equal.
   * @throws ConfigurationException 
   */
  public void initializeModule(final ZkProofStateInitialize zkBuilder) throws ConfigurationException;

  /**
   * For all attributes for which this module called providesAttributeValue(), this method must
   * provide the value of that attribute to the ZkBuilder. This module may query the attribute value
   * for all attributes for which this method called requiresAttribute- Value(). (The modules were
   * topologically sorted prior to this function being called.)
   * @throws ConfigurationException 
   */
  public void collectAttributesForProof(final ZkProofStateCollect zkBuilder) throws ConfigurationException;

  /**
   * Ask the builder which attributes are revealed (and recover their value) and which attributes
   * are unrevealed (and recover the R-value (randomizer) associated with the attribute). Generate
   * T-values (first message of a Sigma-protocol) for each equations, generate D-values (values
   * delivered to the verifier), register N-values (context values that both the prover and verifier
   * know). Send these to the builder.
   * 
   * @throws ConfigurationException
   */
  public void firstRound(final ZkProofStateFirstRound zkBuilder) throws ConfigurationException, ProofException;

  /**
   * Recover the value of the nonce commitments, and the challenge from the zkBuilder. Generate the
   * S-value (third message of a Sigma-protocol) for all external attributes, and send them to the
   * builder.
   * 
   * @throws ConfigurationException
   */
  public void secondRound(final ZkProofStateSecondRound zkBuilder) throws ConfigurationException;

}
