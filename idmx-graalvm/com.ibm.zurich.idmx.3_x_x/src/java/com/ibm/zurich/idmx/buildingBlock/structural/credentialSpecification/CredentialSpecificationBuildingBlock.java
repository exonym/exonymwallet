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

package com.ibm.zurich.idmx.buildingBlock.structural.credentialSpecification;

import com.ibm.zurich.idmx.buildingBlock.GeneralBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.systemParameters.EcryptSystemParametersWrapper;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProver;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleVerifier;
import com.ibm.zurich.idmx.jaxb.wrapper.CredentialSpecificationWrapper;

import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.PublicKey;
import eu.abc4trust.xml.SystemParameters;

public class CredentialSpecificationBuildingBlock extends GeneralBuildingBlock {

  public CredentialSpecificationBuildingBlock() {
    // TODO Auto-generated constructor stub
  }

  @Override
  protected String getBuildingBlockIdSuffix() {
    return "s-abc4trust-credspec";
  }

  @Override
  protected String getImplementationIdSuffix() {
    return "s-abc4trust-credspec";
  }

  public ZkModuleProver getZkModuleProver(final String identifierOfModule, final SystemParameters sp,
                                          final CredentialSpecification cs, final BigIntFactory bigIntFactory) {
    return new ProverModule(this, identifierOfModule, sp, cs, bigIntFactory);
  }

  public ZkModuleVerifier getZkModuleVerifier(final String identifierOfModule, final SystemParameters sp,
                                              final CredentialSpecification cs, final BigIntFactory bigIntFactory) {
    return new VerifierModule(this, identifierOfModule, sp, cs, bigIntFactory);
  }

  // TODO move this to credential specification wrapper?!
  public BigInt getCredSpecIdentifier(final SystemParameters systemParameters, final PublicKey issuerPublicKey,
                                      final CredentialSpecification credSpec) throws ConfigurationException {
    final EcryptSystemParametersWrapper spWrapper = new EcryptSystemParametersWrapper(systemParameters);
    final BigIntFactory bigIntFactory = spWrapper.getDHModulus().getFactory();
    final CredentialSpecificationWrapper credSpecWrapper =
        new CredentialSpecificationWrapper(credSpec, bigIntFactory);
    return credSpecWrapper.getCredSpecId(spWrapper.getHashFunction());
  }
}
