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
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkVerifierStateCollect;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkVerifierStateVerify;


/**
 * When a proof is being verified, each of the following methods will be called (in the order given
 * below) on each of the ZkModuleVerifier participating in the proof verification. In all of these
 * method calls, the ZkModuleVerifier is given a reference to a ZkVerifier with which it is supposed
 * to interact. The ZkVerifier is responsible for centralizing the actions of all ZkModules. (We
 * note here that there is exactly one ZkVerifier that is active during the proof, and that the
 * ZkModuleVerifier is given a reference to a "firewalled" version of the former, which restricts
 * the methods that can be called).
 */
public interface ZkModuleVerifier extends ZkModule {
  /**
   * Notify the zkVerifier of all attributes (including temporary variables) that will be used
   * during the proof. The method may declare that an attribute is to be revealed, that two
   * attributes are equal, or enforce a particular value of an attribute.
   * 
   * @throws ProofException
   * @throws ConfigurationException 
   */
  public void collectAttributesForVerify(final ZkVerifierStateCollect zkVerifier) throws ProofException, ConfigurationException;

  /**
   * Ask the zkVerifier which attributes are revealed (and recover their value) and which attributes
   * are unrevealed (and recover their S-value), recover D-values, recover the challenge. Re-compute
   * the T-values for each equation, check the hash contribution of the D-values, and perform
   * implementation specific-checks. Return false if any of the implementation-specific checks fail.
   * 
   * @throws ConfigurationException
   * @throws ProofException If there is any problem during verification (may also return false)
   * @return True if all checks pass; false if there is any problem during verification (may also
   *         throw a ProofException)
   */
  public boolean verify(final ZkVerifierStateVerify zkVerifier) throws ConfigurationException,
      ProofException;

  public String identifierOfCommitment();
}
