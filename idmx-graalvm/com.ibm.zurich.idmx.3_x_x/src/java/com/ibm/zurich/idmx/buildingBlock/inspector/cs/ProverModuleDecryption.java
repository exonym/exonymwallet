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
package com.ibm.zurich.idmx.buildingBlock.inspector.cs;

import java.math.BigInteger;

import com.ibm.zurich.idmx.buildingBlock.GeneralBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.factory.BuildingBlockFactory;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.ProofException;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProver;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateCollect;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateFirstRound;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateInitialize;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateSecondRound;
import com.ibm.zurich.idmx.zkModule.ZkModuleImpl;

import eu.abc4trust.xml.InspectorPublicKey;
import eu.abc4trust.xml.SecretKey;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.VerifierParameters;

public class ProverModuleDecryption extends ZkModuleImpl implements ZkModuleProver {

  public ProverModuleDecryption(final GeneralBuildingBlock parent, final String identifierOfModule,
                                final SystemParameters systemParameters, final VerifierParameters verifierParameters,
      final InspectorPublicKey inspectorPublicKey, /* Inspector */final SecretKey inspectorSecretKey,
      final byte[] ciphertext, final BigInt label, final BuildingBlockFactory bbFactory) {
    super(parent, identifierOfModule);
    throw new RuntimeException("Not yet implemented!");
  }

  @Override
  public void initializeModule(final ZkProofStateInitialize zkBuilder) throws ConfigurationException {
    // TODO Auto-generated method stub

  }

  @Override
  public void collectAttributesForProof(final ZkProofStateCollect zkBuilder) {
    // TODO Auto-generated method stub

  }

  @Override
  public void firstRound(final ZkProofStateFirstRound zkBuilder) throws ConfigurationException,
      ProofException {
    // TODO Auto-generated method stub

  }

  @Override
  public void secondRound(final ZkProofStateSecondRound zkBuilder) throws ConfigurationException {
    // TODO Auto-generated method stub

  }

  public BigInteger getPlaintext() {
    return null;
  }
}
