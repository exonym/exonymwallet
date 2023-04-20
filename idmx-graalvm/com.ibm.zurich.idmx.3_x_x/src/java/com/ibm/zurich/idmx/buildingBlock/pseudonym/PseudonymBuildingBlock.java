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
package com.ibm.zurich.idmx.buildingBlock.pseudonym;

import java.net.URI;
import java.util.logging.Logger;

import com.ibm.zurich.idmx.annotations.Nullable;
import com.ibm.zurich.idmx.buildingBlock.GeneralBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.factory.BuildingBlockFactory;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.ProofException;
import com.ibm.zurich.idmx.interfaces.device.ExternalSecretsHelper;
import com.ibm.zurich.idmx.interfaces.device.ExternalSecretsManager;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.RandomGeneration;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProver;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleVerifier;

import eu.abc4trust.xml.AbstractPseudonym;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.VerifierParameters;

public abstract class PseudonymBuildingBlock extends GeneralBuildingBlock {
  protected final RandomGeneration randomGeneration;
  protected final ExternalSecretsManager esManager;
  protected final ExternalSecretsHelper esHelper;
  protected final Logger logger;
  protected final BuildingBlockFactory bbFactory;
  protected final BigIntFactory bigIntFactory;

  public PseudonymBuildingBlock(
          final RandomGeneration rg, final BuildingBlockFactory bbFactory,
          final ExternalSecretsManager esManager, final ExternalSecretsHelper esHelper,
          final Logger logger,
          final BigIntFactory bigIntFactory) {
    this.randomGeneration = rg;
    this.esHelper = esHelper;
    this.esManager = esManager;
    this.logger = logger;
    this.bbFactory = bbFactory;
    this.bigIntFactory = bigIntFactory;
  }

  @Override
  protected String getBuildingBlockIdSuffix() {
    return "pseudonym";
    // same for both standard and domain-exclusive pseudonyms
  }

  @Override
  protected abstract String getImplementationIdSuffix();

  public abstract AbstractPseudonym createPseudonym(final SystemParameters systemParameters,
                                                    final VerifierParameters verifierParameters, final @Nullable URI deviceUid, final String username, final @Nullable URI scope)
      throws ConfigurationException;
  
  public byte[] getPseudonymAsBytes(final AbstractPseudonym pseudonym) {
    return bigIntFactory.valueOf(pseudonym.getValue()).toByteArray();
  }
  
  public BigInt getPseudonymValueFromBytes(final byte[] pseudonymValue) {
    return bigIntFactory.signedValueOf(pseudonymValue);
  }

  public abstract ZkModuleProver getZkModuleProver(final SystemParameters systemParameters,
                                                   final VerifierParameters verifierParameters, final String identifierOfModule,
      final AbstractPseudonym pseudonym, final @Nullable URI deviceUid, final String username, final @Nullable URI scope)
      throws ProofException, ConfigurationException;

  public abstract ZkModuleVerifier getZkModuleVerifier(final SystemParameters systemParameters,
                                                       final VerifierParameters verifierParameters, final String identifierOfModule, final @Nullable URI scope,
      final byte[] pseudonym) throws ProofException, ConfigurationException;
  
  public abstract boolean isScopeExclusive();

}
