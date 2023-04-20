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

package com.ibm.zurich.idmx.buildingBlock.structural.issuerKey;

import com.ibm.zurich.idmx.buildingBlock.systemParameters.EcryptSystemParametersWrapper;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProver;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateCollect;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateFirstRound;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateInitialize;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateSecondRound;
import com.ibm.zurich.idmx.keypair.issuer.IssuerPublicKeyWrapper;
import com.ibm.zurich.idmx.zkModule.ZkModuleImpl;

import eu.abc4trust.xml.PublicKey;
import eu.abc4trust.xml.SystemParameters;

public class ProverModule extends ZkModuleImpl implements ZkModuleProver {

  private final EcryptSystemParametersWrapper sp;
  private final IssuerPublicKeyWrapper pk;

  public ProverModule(final IssuerPublicKeyBuildingBlock parent, final String identifierOfModule,
                      final SystemParameters systemParameters, final PublicKey publicKey) {

    super(parent, identifierOfModule);

    this.sp = new EcryptSystemParametersWrapper(systemParameters);
    this.pk = new IssuerPublicKeyWrapper(publicKey);
  }


  @Override
  public void initializeModule(final ZkProofStateInitialize zkBuilder) {
    // Nothing needs to be done
  }

  @Override
  public void collectAttributesForProof(final ZkProofStateCollect zkBuilder) {
    // Nothing needs to be done
  }

  @Override
  public void firstRound(final ZkProofStateFirstRound zkBuilder) throws ConfigurationException {
    zkBuilder.addNValue(getIdentifier() + ":publicKey:c14n", pk.getHash(sp.getHashFunction()));
  }

  @Override
  public void secondRound(final ZkProofStateSecondRound zkBuilder) {
    // Nothing needs to be done
  }

}
