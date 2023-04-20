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

package com.ibm.zurich.idmx.buildingBlock.rangeProof.fourSq;

import com.ibm.zurich.idmx.buildingBlock.factory.BuildingBlockFactory;
import com.ibm.zurich.idmx.buildingBlock.rangeProof.RangeProofBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.rangeProof.SafeRSAGroupInVerifierParameters;
import com.ibm.zurich.idmx.buildingBlock.systemParameters.EcryptSystemParametersWrapper;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.group.GroupFactory;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProver;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleVerifier;

import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.VerifierParameters;

import javax.inject.Inject;

public class FourSquaresRangeProofBuildingBlock extends RangeProofBuildingBlock {

  private final BigIntFactory bif;
  private final BuildingBlockFactory bbFactory;
  private final GroupFactory gf;
  private final SafeRSAGroupInVerifierParameters rsaVp;

  @Inject
  public FourSquaresRangeProofBuildingBlock(final SafeRSAGroupInVerifierParameters rsaVp,
                                            final BigIntFactory bif, final BuildingBlockFactory bbFactory, final GroupFactory gf) {
    super(rsaVp);
    this.bif = bif;
    this.bbFactory = bbFactory;
    this.gf = gf;
    this.rsaVp = rsaVp;
  }

  @Override
  protected String getImplementationIdSuffix() {
    return ":rabinshallit";
  }


  @Override
  public ZkModuleProver getZkModuleProver(final SystemParameters systemParameters,
                                          final VerifierParameters verifierParameters, final String lhsAttribute, final String rhsAttribute,
      final boolean strict, final int predicateNumber) throws ConfigurationException {
    EcryptSystemParametersWrapper esw = new EcryptSystemParametersWrapper(systemParameters);
    String moduleId = getModuleIdentifier(lhsAttribute, rhsAttribute, predicateNumber);
    return new ProverModule(this, moduleId, lhsAttribute, rhsAttribute, strict, bif, esw,
        verifierParameters, bbFactory, gf, rsaVp);
  }


  @Override
  public ZkModuleVerifier getZkModuleVerifier(final SystemParameters systemParameters,
                                              final VerifierParameters verifierParameters, final String lhsAttribute, final String rhsAttribute,
      final boolean strict, final int predicateNumber) throws ConfigurationException {
    final EcryptSystemParametersWrapper esw = new EcryptSystemParametersWrapper(systemParameters);
    final String moduleId = getModuleIdentifier(lhsAttribute, rhsAttribute, predicateNumber);
    return new VerifierModule(this, moduleId, lhsAttribute, rhsAttribute, strict, bif, esw,
        verifierParameters, bbFactory, gf, rsaVp);
  }

  @Override
  public ZkModuleProver getZkModuleProverRangeCheck(SystemParameters systemParameters,
      VerifierParameters verifierParameters, String attributeName) throws ConfigurationException {
    throw new RuntimeException("Not yet implemented");
  }

  @Override
  public ZkModuleVerifier getZkModuleVerifierRangeCheck(SystemParameters systemParameters,
      VerifierParameters verifierParameters, String attributeName) throws ConfigurationException {
    throw new RuntimeException("Not yet implemented");
  }

}
