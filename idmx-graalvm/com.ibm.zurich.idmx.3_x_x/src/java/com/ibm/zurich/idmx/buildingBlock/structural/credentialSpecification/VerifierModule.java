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

import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.ProofException;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleVerifier;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkVerifierStateCollect;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkVerifierStateVerify;
import com.ibm.zurich.idmx.jaxb.wrapper.CredentialSpecificationWrapper;
import com.ibm.zurich.idmx.parameters.system.SystemParametersWrapper;
import com.ibm.zurich.idmx.zkModule.ZkModuleImpl;

import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.SystemParameters;

public class VerifierModule extends ZkModuleImpl implements ZkModuleVerifier {

  private final SystemParametersWrapper spWrapper;
  private final CredentialSpecificationWrapper credSpecWrapper;

  public VerifierModule(final CredentialSpecificationBuildingBlock parent, final String identifierOfModule,
                        final SystemParameters systemParameters, final CredentialSpecification credSpec,
      final BigIntFactory bigIntFactory) {

    super(parent, identifierOfModule);

    this.spWrapper = new SystemParametersWrapper(systemParameters);
    this.credSpecWrapper = new CredentialSpecificationWrapper(credSpec, bigIntFactory);
  }


  @Override
  public void collectAttributesForVerify(final ZkVerifierStateCollect zkVerifier) {
    // Nothing needs to be done
  }

  @Override
  public boolean verify(final ZkVerifierStateVerify zkVerifier) throws ConfigurationException,
      ProofException {
    zkVerifier.checkNValue(getIdentifier() + ":credSpec:c14n",
        credSpecWrapper.getHash(spWrapper.getHashFunction()));
    return true;
  }

}
