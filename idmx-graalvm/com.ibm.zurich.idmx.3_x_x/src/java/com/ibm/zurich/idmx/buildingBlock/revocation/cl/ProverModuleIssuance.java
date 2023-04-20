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

import com.ibm.zurich.idmix.abc4trust.facades.NonRevocationEvidenceFacade;
import com.ibm.zurich.idmix.abc4trust.facades.RevocationAuthorityParametersFacade;
import com.ibm.zurich.idmix.abc4trust.facades.RevocationMessageFacade;
import com.ibm.zurich.idmx.buildingBlock.structural.revocationAuthorityKey.RevocationAuthorityPublicKeyBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.systemParameters.EcryptSystemParametersWrapper;
import com.ibm.zurich.idmx.configuration.Configuration;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.ProofException;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProver;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProverRevocation;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ResidueClass;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateCollect;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateFirstRound;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateInitialize;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateSecondRound;
import com.ibm.zurich.idmx.jaxb.wrapper.CredentialSpecificationWrapper;
import com.ibm.zurich.idmx.parameters.verifier.VerifierParametersWrapper;
import com.ibm.zurich.idmx.zkModule.ZkModuleImpl;

import eu.abc4trust.revocationProxy.RevocationProxy;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.CryptoParams;
import eu.abc4trust.xml.NonRevocationEvidence;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PublicKey;
import eu.abc4trust.xml.RevocationMessage;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.VerifierParameters;

public class ProverModuleIssuance extends ZkModuleImpl implements ZkModuleProverRevocation {

  @SuppressWarnings("unused")
  private final EcryptSystemParametersWrapper spWrapper;
  @SuppressWarnings("unused")
  private final VerifierParametersWrapper vpWrapper;
  private final ClRevocationAuthorityPublicKeyWrapper pkWrapper;
  private final CredentialSpecificationWrapper credSpecWrapper;
  private NonRevocationEvidenceFacade nreFacade;

  private final RevocationProxy revocationProxy;

  private final ArrayList<ZkModuleProver> childZkModules;

  public ProverModuleIssuance(final ClRevocationBuildingBlock parent, final SystemParameters systemParameters,
                              final VerifierParameters verifierParameters, final PublicKey raPublicKey,
      final CredentialSpecification credentialSpecification, final String identifierOfModule,
      final RevocationProxy revocationProxy, final RevocationAuthorityPublicKeyBuildingBlock raPkBB)
      throws ConfigurationException {

    super(parent, identifierOfModule);

    this.spWrapper = new EcryptSystemParametersWrapper(systemParameters);
    this.vpWrapper = new VerifierParametersWrapper(verifierParameters);
    this.pkWrapper = new ClRevocationAuthorityPublicKeyWrapper(raPublicKey);
    this.credSpecWrapper =
        new CredentialSpecificationWrapper(credentialSpecification, pkWrapper.getModulus()
            .getFactory());

    this.revocationProxy = revocationProxy;

    this.childZkModules = new ArrayList<ZkModuleProver>();
    final ZkModuleProver raZkp =
        raPkBB.getZkModuleProver(identifierOfModule + ":raPk", systemParameters, raPublicKey);
    childZkModules.add(raZkp);
  }


  @Override
  public void initializeModule(final ZkProofStateInitialize zkBuilder) throws ConfigurationException {

    try {
      final RevocationAuthorityParametersFacade rapf =
          RevocationAuthorityParametersFacade.initRevocationAuthorityParameters(pkWrapper
              .getPublicKey());

      final RevocationMessageFacade revocationMessageFacade = new RevocationMessageFacade();
      revocationMessageFacade.setRequestRevocationHandle();
      revocationMessageFacade.setRevocationAuthorityParametersUID(rapf
          .getRevocationAuthorityParametersId());
      // revocationMessageFacade.setContext()

      // Create Attribute as expected by the revocation authority
      final ObjectFactory objectFactory = new ObjectFactory();
      final Attribute att = objectFactory.createAttribute();
      att.setAttributeDescription(credSpecWrapper.getRevocationHandleAttributeDescription());
      final CryptoParams cryptoParams = objectFactory.createCryptoParams();
      cryptoParams.getContent().add(objectFactory.createAttribute(att));
      revocationMessageFacade.setCryptoParams(cryptoParams);

      final RevocationMessage message =
          revocationProxy.processRevocationMessage(revocationMessageFacade.getDelegateeValue(),
              rapf.getRevocationAuthorityParameters());
      final RevocationMessageFacade revocationMessageResponseFacade =
          new RevocationMessageFacade(message);

      // Create non revocation evidence object
      nreFacade =
          new NonRevocationEvidenceFacade(
              revocationMessageResponseFacade.getNonRevocationEvidence());

      // TODO store the NRE (this is the issuer - he should not have the NRE, let alone use it)
    } catch (final ConfigurationException e) {
      if (Configuration.printStackTraces()) e.printStackTrace();
      throw new RuntimeException("revocation authority parameters are misconfigured.");
    } catch (final Exception e) {
      if (Configuration.printStackTraces()) e.printStackTrace();
      throw new RuntimeException("getting revocation handle from revocation proxy failed.");
    }

    zkBuilder.registerAttribute(identifierOfAttribute(0), false);
    zkBuilder.setValueOfAttribute(identifierOfAttribute(0), nreFacade.getRevocationHandleValue(), ResidueClass.INTEGER_IN_RANGE);

    for (final ZkModuleProver zkm : childZkModules) {
      zkm.initializeModule(zkBuilder);
    }
  }

  @Override
  public void collectAttributesForProof(final ZkProofStateCollect zkBuilder) throws ConfigurationException {
    for (final ZkModuleProver zkm : childZkModules) {
      zkm.collectAttributesForProof(zkBuilder);
    }
  }

  @Override
  public void firstRound(final ZkProofStateFirstRound zkBuilder) throws ConfigurationException,
      ProofException {
    for (final ZkModuleProver zkm : childZkModules) {
      zkm.firstRound(zkBuilder);
    }
  }

  @Override
  public void secondRound(final ZkProofStateSecondRound zkBuilder) throws ConfigurationException {
    for (final ZkModuleProver zkm : childZkModules) {
      zkm.secondRound(zkBuilder);
    }
  }

  @Override
  public NonRevocationEvidence recoverNonRevocationEvidence() {
    return nreFacade.getDelegateeElement();
  }
}
