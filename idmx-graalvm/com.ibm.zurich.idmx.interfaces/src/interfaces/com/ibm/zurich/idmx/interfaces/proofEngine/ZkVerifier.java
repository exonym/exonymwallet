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

import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.ProofException;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkVerifierStateCollect;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkVerifierStateVerify;

import eu.abc4trust.xml.ZkProof;

public interface ZkVerifier {
  
  void loadProof(ZkProof proof) throws ProofException, ConfigurationException;
  
  ZkVerifierStateCollect getStateForCollect(final String moduleId) throws ProofException;

  /**
   * When this method is called, the zkVerifier checks that all S-values are in their expected
   * interval, and that all S-values within an equivalence class are compatible (i.e., S-values
   * corresponding to attributes that are equal, must be equal).
   * 
   * @throws ConfigurationException 
   * @throws ProofException In case any check fails.
   */
  void integrityCheckOfSValues() throws ConfigurationException, ProofException;
  
  ZkVerifierStateVerify getStateForVerify(final String moduleId) throws ProofException;

  /**
   * When this method is called, check that all modules have been processed.
   * 
   * @throws ProofException If any check fails
   * @throws ConfigurationException 
   */
  void finalizeVerification() throws ProofException, ConfigurationException;
}
