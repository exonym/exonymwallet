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

package com.ibm.zurich.idmx.buildingBlock.revocation.cl;

import java.util.ArrayList;

import com.ibm.zurich.idmx.buildingBlock.structural.revocationAuthorityKey.RevocationAuthorityPublicKeyBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.systemParameters.EcryptSystemParametersWrapper;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.ProofException;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleVerifier;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleVerifierRevocation;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ResidueClass;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkVerifierStateCollect;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkVerifierStateVerify;
import com.ibm.zurich.idmx.parameters.verifier.VerifierParametersWrapper;
import com.ibm.zurich.idmx.zkModule.ZkModuleImpl;

import eu.abc4trust.xml.NonRevocationEvidence;
import eu.abc4trust.xml.PublicKey;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.VerifierParameters;

public class VerifierModuleIssuance extends ZkModuleImpl implements ZkModuleVerifierRevocation {

  @SuppressWarnings("unused")
  private final EcryptSystemParametersWrapper spWrapper;
  @SuppressWarnings("unused")
  private final VerifierParametersWrapper vpWrapper;
  @SuppressWarnings("unused")
  private final ClRevocationAuthorityPublicKeyWrapper pkWrapper;
  // private final CredentialSpecificationWrapper csWrapper;

  private ArrayList<ZkModuleVerifier> childZkModules;

  //TODO(ksa) why global?
  @SuppressWarnings("unused")
  private BigInt revocationHandleValue;

  public VerifierModuleIssuance(final ClRevocationBuildingBlock parent,
                                final SystemParameters systemParameters, final VerifierParameters verifierParameters,
      final PublicKey raPublicKey, final String identifierOfModule,
      final RevocationAuthorityPublicKeyBuildingBlock raPkBB) {
    super(parent, identifierOfModule);

    this.spWrapper = new EcryptSystemParametersWrapper(systemParameters);
    this.vpWrapper = new VerifierParametersWrapper(verifierParameters);
    this.pkWrapper = new ClRevocationAuthorityPublicKeyWrapper(raPublicKey);
    // this.csWrapper = new CredentialSpecificationWrapper(credSpec, bigIntFactory);

    this.childZkModules = new ArrayList<ZkModuleVerifier>();
    final ZkModuleVerifier raZkp =
        raPkBB.getZkModuleVerifier(identifierOfModule + ":raPk", systemParameters, raPublicKey);
    childZkModules.add(raZkp);
  }


  @Override
  public void collectAttributesForVerify(final ZkVerifierStateCollect zkVerifier) throws ProofException,
      ConfigurationException {
    zkVerifier.registerAttribute(identifierOfAttribute(0), false);
    zkVerifier.setResidueClass(identifierOfAttribute(0), ResidueClass.INTEGER_IN_RANGE);

    for (final ZkModuleVerifier zkm : childZkModules) {
      zkm.collectAttributesForVerify(zkVerifier);
    }
  }

  @Override
  public boolean verify(final ZkVerifierStateVerify zkVerifier) throws ConfigurationException,
      ProofException {
    revocationHandleValue = zkVerifier.getValueOfRevealedAttribute(identifierOfAttribute(0));
    for (final ZkModuleVerifier zkm : childZkModules) {
      zkm.verify(zkVerifier);
    }
    return true;
  }


  @Override
  public NonRevocationEvidence getNonRevocationEvidence() throws ConfigurationException {


    // NonRevocationEvidenceFacade nreFacade = new NonRevocationEvidenceFacade(revocationState,
    // nonRevocationEvidenceValue, raPublicKey);
    //
    //
    // // TODO get the attribute from the credential template
    //
    // // TODO transfer the values via D values
    //
    // RevocationAuthorityParametersFacade rapf =
    // RevocationAuthorityParametersFacade.initRevocationAuthorityParameters(pkWrapper
    // .getPublicKey());
    //
    // // nre.setNonRevocationEvidenceUID(URI.create("non:revocation:evidence:uid"));
    // // nre.setRevocationAuthorityParametersUID(rapf.getRevocationAuthorityParametersId());
    // // nre.setCredentialUID(URI.create("credential:uid"));
    // // nre.setCreated(new GregorianCalendar());
    // // nre.setExpires(new GregorianCalendar());
    // // nre.getAttribute().add(revocationHandle);
    //
    // // return nre;
    return null;
  }
}
