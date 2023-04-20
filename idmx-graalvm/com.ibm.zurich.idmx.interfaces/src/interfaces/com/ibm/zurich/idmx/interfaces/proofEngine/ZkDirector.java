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
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProver;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleVerifier;

import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.ZkProof;

public interface ZkDirector {

  /**
   * Builds a zero knowledge proof based on the list of provided ZkModules.
   * 
   * @param modules The list of modules that the proof should be constructed from.
   * @param sp The system parameters.
   * @return The constructed proof.
   * 
   * @throws ConfigurationException
   * @throws ProofException
   */
  ZkProof buildProof(final String username, final List<? extends ZkModuleProver> modules, final SystemParameters sp)
      throws ConfigurationException, ProofException;

  /**
   * Verifies a zero knowledge proof.
   * 
   * @param proof The proof to be verified.
   * @param modules The list of modules needed for proof verification.
   * @param sp The system parameters.
   * @return Verification result.
   * 
   * @throws ConfigurationException
   */
  boolean verifyProof(final ZkProof proof, final List<? extends ZkModuleVerifier> modules, final SystemParameters sp)
      throws ConfigurationException;
}
