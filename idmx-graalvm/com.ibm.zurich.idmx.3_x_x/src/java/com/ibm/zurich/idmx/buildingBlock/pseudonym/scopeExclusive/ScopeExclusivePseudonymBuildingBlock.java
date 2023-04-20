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
package com.ibm.zurich.idmx.buildingBlock.pseudonym.scopeExclusive;

import java.net.URI;
import java.util.logging.Logger;

import com.ibm.zurich.idmx.annotations.Nullable;
import com.ibm.zurich.idmx.buildingBlock.factory.BuildingBlockFactory;
import com.ibm.zurich.idmx.buildingBlock.pseudonym.PseudonymBuildingBlock;
//import com.ibm.zurich.idmx.buildingBlock.systemParameters.EcryptSystemParametersWrapper;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.ProofException;
import com.ibm.zurich.idmx.interfaces.device.ExternalSecretsHelper;
import com.ibm.zurich.idmx.interfaces.device.ExternalSecretsManager;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.RandomGeneration;
//import com.ibm.zurich.idmx.interfaces.util.group.KnownOrderGroup;
//import com.ibm.zurich.idmx.interfaces.util.group.KnownOrderGroupElement;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProver;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleVerifier;
//import com.ibm.zurich.idmx.util.group.GroupFactoryImpl;

import eu.abc4trust.xml.AbstractPseudonym;
import eu.abc4trust.xml.ScopeExclusivePseudonym;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.VerifierParameters;

import javax.inject.Inject;

public class ScopeExclusivePseudonymBuildingBlock extends PseudonymBuildingBlock {

  @Inject
  public ScopeExclusivePseudonymBuildingBlock(final RandomGeneration rg, final BuildingBlockFactory bbFactory,
                                              final ExternalSecretsManager esManager, final ExternalSecretsHelper esHelper, final Logger logger,
      final BigIntFactory bigIntFactory) {
    super(rg, bbFactory, esManager, esHelper, logger, bigIntFactory);
  }

  @Override
  protected String getBuildingBlockIdSuffix() {
    return super.getBuildingBlockIdSuffix().concat(":scopeExclusive");
  }

  @Override
  protected String getImplementationIdSuffix() {
    return "scope_exclusive_pseudonym";
    // should be different for standard and domain-exclusive pseudonyms
  }

  @Override
  public ZkModuleProver getZkModuleProver(final SystemParameters systemParameters,
                                          final VerifierParameters verifierParameters, final String identifierOfModule,
      final AbstractPseudonym pseudonym, final @Nullable URI deviceUid, final String username, final @Nullable URI scope)
      throws ProofException, ConfigurationException {

    final ZkModuleProver zkModuleProver =
        new ProverModule(this, identifierOfModule, systemParameters, deviceUid, username, scope, pseudonym,
            bbFactory, esManager, randomGeneration, logger, bigIntFactory);
    return zkModuleProver;
  }

  @Override
public ZkModuleVerifier getZkModuleVerifier(final SystemParameters systemParameters,
                                            final VerifierParameters verifierParameters, final String identifierOfModule, final @Nullable URI scope,
      final byte[] pseudonym) throws ProofException, ConfigurationException {

    final ZkModuleVerifier zkModuleVerifier =
        new VerifierModule(this, identifierOfModule, systemParameters, scope, pseudonym, bbFactory,
            esHelper, logger, bigIntFactory);
    return zkModuleVerifier;
  }

  /*
   * Pseudonym createPseudonym (
   * 
   * @in Map<Field, Value> systemParameters,
   * 
   * @in Map<Field, Value> verifierParameters,
   * 
   * @in URI secretLocation ,
   * 
   * @in URI scope )
   * 
   * This method creates a new pseudonym with the given scope and with the given secret.
   */
  @Override
  public ScopeExclusivePseudonym createPseudonym(final SystemParameters systemParameters,
                                                 final VerifierParameters verifierParameters, final @Nullable URI deviceUid, final String username, final @Nullable URI scope)
      throws ConfigurationException {

    final ScopeExclusivePseudonym pseudonym = new ScopeExclusivePseudonym();

//    EcryptSystemParametersWrapper syspar = new EcryptSystemParametersWrapper(systemParameters);

//    KnownOrderGroup group =
//        new GroupFactoryImpl().createPrimeOrderGroup(syspar.getDHModulus(),
//            syspar.getDHSubgroupOrder());
//    KnownOrderGroupElement base1 =
//        group.valueOf(bigIntFactory.valueOf(esManager.getBaseForScopeExclusivePseudonym(username, scope,
//            syspar.getDHModulus().getValue(), syspar.getDHSubgroupOrder().getValue())));

    pseudonym.setValue(esManager.getScopeExclusivePseudonym(username, deviceUid, scope));
    pseudonym.setDeviceUid(deviceUid);
    pseudonym.setScope(scope);

    return pseudonym;
  }

  @Override
  public boolean isScopeExclusive() {
    return true;
  }
}
