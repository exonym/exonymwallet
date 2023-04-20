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

/**
 * ZkModuleProver, ZkModuleVerifier, and ZkModuleDescriber are abstract classes that expose a
 * unified interface to the proof engine for conducting Fiat-Shamir zero-knowledge proofs, verifying
 * them, and describing them. The concrete instantiation of these modules encapsulate implementation
 * specific state.
 */
public interface ZkModule {
  /**
   * Returns the unique identifier of this instance. This identifier can be freely chosen at
   * runtime, must be unique throughout the proof, and it must be guaranteed that the prover and the
   * verifier agree on the same identifier.
   */
  public String getIdentifier();

  /**
   * Returns the attribute name for the i-th attribute of this building block. This should be the
   * composition of the return value of getIdentifier() concatenated with the string ":i".
   */
  public String identifierOfAttribute(final int i);

  /**
   * Returns the name of the building block that generated this instance.
   */
  public String getBuildingBlockId();

  /**
   * Returns the name of the specific implementation of the building block that generated this
   * instance. For building blocks which admit only one implementation, the return value must be the
   * same as for getBuildingBlockId().
   * 
   * @throws ConfigurationException
   */
  public String getImplementationId() throws ConfigurationException;

  /**
   * Returns the attribute name for the secret on device used by this building block. This should be the
   * composition of the return value of getIdentifier() concatenated with the string ":secret".
   */
  public String identifierOfSecretAttribute();
  
  /**
   * Returns the name of the DValue that holds the commitment used by this building block
   * (only relevant for building blocks that hold a commitment)
   * @return
   */
  public String identifierOfCommitment();
}
