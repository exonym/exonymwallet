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
package com.ibm.zurich.idmx.buildingBlock.pseudonym.standard;

import java.math.BigInteger;
import java.net.URI;
import java.util.logging.Logger;

import com.ibm.zurich.idmx.annotations.Nullable;
import com.ibm.zurich.idmx.buildingBlock.factory.BuildingBlockFactory;
import com.ibm.zurich.idmx.buildingBlock.pseudonym.PseudonymBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.systemParameters.EcryptSystemParametersWrapper;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.ProofException;
import com.ibm.zurich.idmx.interfaces.device.ExternalSecretsHelper;
import com.ibm.zurich.idmx.interfaces.device.ExternalSecretsManager;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.RandomGeneration;
//import com.ibm.zurich.idmx.interfaces.util.group.GroupElement;
import com.ibm.zurich.idmx.interfaces.util.group.KnownOrderGroup;
import com.ibm.zurich.idmx.interfaces.util.group.KnownOrderGroupElement;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProver;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleVerifier;
import com.ibm.zurich.idmx.util.group.GroupFactoryImpl;

import eu.abc4trust.xml.AbstractPseudonym;
import eu.abc4trust.xml.StandardPseudonym;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.VerifierParameters;

import javax.inject.Inject;

public class StandardPseudonymBuildingBlock extends PseudonymBuildingBlock {

  @Inject
  public StandardPseudonymBuildingBlock(final RandomGeneration rg, final BuildingBlockFactory bbFactory,
                                        final ExternalSecretsManager esManager, final ExternalSecretsHelper esHelper, final Logger logger,
      final BigIntFactory bigIntFactory) {
    super(rg, bbFactory, esManager, esHelper, logger, bigIntFactory);
  }

  
  @Override
  protected String getBuildingBlockIdSuffix() {
    return super.getBuildingBlockIdSuffix().concat(":standard");
  }
  
  @Override
  protected String getImplementationIdSuffix() {
    return "standard_pseudonym";
    // should be different for standard and domain-exclusive pseudonyms
  }

  @Override
public ZkModuleProver getZkModuleProver(final SystemParameters systemParameters,
                                        final VerifierParameters verifierParameters, final String identifierOfModule,
                                        final AbstractPseudonym pseudonym, final @Nullable URI deviceUid, final String username, final @Nullable URI scope)
      throws ProofException, ConfigurationException {

    // could have scope as well

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
            logger, bigIntFactory);
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
  public StandardPseudonym createPseudonym(final SystemParameters systemParameters,
                                           final VerifierParameters verifierParameters, final @Nullable URI deviceUid, final String username, final @Nullable URI scope)
      throws ConfigurationException {

    final StandardPseudonym pseudonym;

    final EcryptSystemParametersWrapper syspar = new EcryptSystemParametersWrapper(systemParameters);

    final KnownOrderGroup group =
        new GroupFactoryImpl().createPrimeOrderGroup(syspar.getDHModulus(),
            syspar.getDHSubgroupOrder());
    //KnownOrderGroupElement base1 = group.valueOf(syspar.getDHGenerator1());
    final KnownOrderGroupElement base2 = group.valueOf(syspar.getDHGenerator2());

    final BigInt devicePKasBigInt = bigIntFactory.valueOf(esManager.getDevicePublicKey(username, deviceUid));
    KnownOrderGroupElement devicePK = group.valueOf(devicePKasBigInt);

    final BigInt randomizer = randomGeneration.generateRandomNumber(group.getOrder());

    final KnownOrderGroupElement comm = devicePK.opMultOp(base2, randomizer);

    // TODO Why does the pseudonym class not contain the actual pseudonym value also?
    // can add the pseudonym value to save computation time, particularly for the computations
    // related to card

    // TODO
    pseudonym = new StandardPseudonym();
    pseudonym.setValue(comm.toBigInt().getValue());
    pseudonym.setDeviceUid(deviceUid);
    pseudonym.setScope(scope);
    pseudonym.setOpeningInformation(new BigInteger(randomizer.toByteArray()));

    return pseudonym;
  }

  @Override
  public boolean isScopeExclusive() {
    return false;
  }

  // different blocks for the standard and domain-exclusive pseudonyms
  // exactly same interface
  // abstract superclass; different subclasses for the kinds of pseudonyms

}
