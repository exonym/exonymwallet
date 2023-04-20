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

import com.ibm.zurich.idmix.abc4trust.facades.NonRevocationEvidenceFacade;
import com.ibm.zurich.idmix.abc4trust.facades.RevocationInformationFacade;
import com.ibm.zurich.idmx.buildingBlock.factory.BuildingBlockFactory;
import com.ibm.zurich.idmx.buildingBlock.revocation.RevocationBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.signature.cl.ClHelper;
import com.ibm.zurich.idmx.buildingBlock.structural.revocationAuthorityKey.RevocationAuthorityPublicKeyBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.systemParameters.EcryptSystemParametersGenerator;
import com.ibm.zurich.idmx.buildingBlock.systemParameters.EcryptSystemParametersWrapper;
import com.ibm.zurich.idmx.configuration.Configuration;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.ProofException;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.RandomGeneration;
import com.ibm.zurich.idmx.interfaces.util.Timing;
import com.ibm.zurich.idmx.interfaces.util.group.GroupFactory;
import com.ibm.zurich.idmx.interfaces.util.group.HiddenOrderGroup;
import com.ibm.zurich.idmx.interfaces.util.group.HiddenOrderGroupElement;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProver;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProverRevocation;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleVerifier;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleVerifierRevocation;
import com.ibm.zurich.idmx.parameters.ra.RevocationAuthorityPublicKeyTemplateWrapper;

import eu.abc4trust.revocationProxy.RevocationProxy;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.KeyPair;
import eu.abc4trust.xml.NonRevocationEvidence;
import eu.abc4trust.xml.PublicKey;
import eu.abc4trust.xml.RevocationAuthorityPublicKeyTemplate;
import eu.abc4trust.xml.RevocationInformation;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.VerifierParameters;

import javax.inject.Inject;

/**
 * 
 */
public class ClRevocationBuildingBlock extends RevocationBuildingBlock {

  private final RandomGeneration randomGeneration;
  private final BigIntFactory bigIntFactory;
  private final GroupFactory groupFactory;
  private final Timing timing;

  @Inject
  public ClRevocationBuildingBlock(final RandomGeneration randomGeneration, final BigIntFactory bigIntFactory,
                                   final GroupFactory groupFactory, final Timing timing) {
    super(randomGeneration);

    this.randomGeneration = randomGeneration;
    this.bigIntFactory = bigIntFactory;
    this.groupFactory = groupFactory;

    this.timing = timing;
  }


  @Override
  protected String getImplementationIdSuffix() {
    return "cl";
  }



  @Override
  public void addToRevocationParametersTemplate(
      final RevocationAuthorityPublicKeyTemplate revocationAuthorityPublicKeyTemplate) {
    final RevocationAuthorityPublicKeyTemplateWrapper rakpWrapper =
        new RevocationAuthorityPublicKeyTemplateWrapper(revocationAuthorityPublicKeyTemplate);

    // Set default values
    final int defaultRsaModulusLength =
        EcryptSystemParametersGenerator.securityLevelEquivaltentRsaModulusBitlength(Configuration
            .defaultSecurityLevel());
    rakpWrapper.setModulusLength(defaultRsaModulusLength);
    rakpWrapper.setPublicKeyPrefix(Configuration.defaultRevocationAuthorityPublicKeyPrefix());
  }

  @Override
  public KeyPair generateBuildingBlockRevocationAuthorityKeyPair(final SystemParameters systemParameters,
                                                                 final RevocationAuthorityPublicKeyTemplate template) throws ConfigurationException {

    final KeyPair keyPair =
        super.generateBuildingBlockRevocationAuthorityKeyPair(systemParameters, template);
    final ClRevocationKeyPairWrapper kpWrapper = new ClRevocationKeyPairWrapper(keyPair);
    final RevocationAuthorityPublicKeyTemplateWrapper rapkTemplateWrapper =
        new RevocationAuthorityPublicKeyTemplateWrapper(template);

    final EcryptSystemParametersWrapper spWrapper = new EcryptSystemParametersWrapper(systemParameters);

    final int rsaModulusLength = rapkTemplateWrapper.getModulusLength();
    final int primeProbability = spWrapper.getPrimeProbability();

    timing.startTiming();

    final ClRevocationSecretKeyWrapper skWrapper = kpWrapper.getCLSecretKeyWrapper();
    ClHelper.generateSecretKey(randomGeneration, bigIntFactory, skWrapper, rsaModulusLength,
        primeProbability);

    timing.endTiming("Finished generating an RSA modulus of " + rsaModulusLength + " bits.");

    final ClRevocationAuthorityPublicKeyWrapper pkWrapper = kpWrapper.getCLPublicKeyWrapper();
    generatePublicKey(pkWrapper, skWrapper);

    return keyPair;
  }

  private void generatePublicKey(final ClRevocationAuthorityPublicKeyWrapper pkWrapper,
                                 final ClRevocationSecretKeyWrapper skWrapper) throws ConfigurationException {

    timing.startTiming();

    final BigInt modulus = skWrapper.getModulus();

    pkWrapper.setModulus(modulus);
    final HiddenOrderGroup group = pkWrapper.getGroup(groupFactory);

    final HiddenOrderGroupElement g = group.createRandomGenerator(randomGeneration);
    final HiddenOrderGroupElement h = group.createRandomGenerator(randomGeneration);
    pkWrapper.setBase(0, g.toBigInt());
    pkWrapper.setBase(1, h.toBigInt());

    timing.endTiming("Generation of CL revocation authority public key.");
  }


  @Override
  public ZkModuleProverRevocation getZkModuleProverIssuance(final SystemParameters systemParameters,
                                                            final VerifierParameters verifierParameters, final PublicKey raPublicKey,
      final CredentialSpecification credentialSpecification, final String moduleId,
      final RevocationProxy revocationProxy, final BuildingBlockFactory buildingBlockFactory)
      throws ConfigurationException {

    final RevocationAuthorityPublicKeyBuildingBlock raPkBB =
        buildingBlockFactory
            .getBuildingBlockByClass(RevocationAuthorityPublicKeyBuildingBlock.class);

    return new ProverModuleIssuance(this, systemParameters, verifierParameters, raPublicKey,
        credentialSpecification, moduleId, revocationProxy, raPkBB);
  }

  @Override
  public ZkModuleVerifierRevocation getZkModuleVerifierIssuance(final SystemParameters systemParameters,
                                                                final VerifierParameters verifierParameters, final PublicKey raPublicKey, final String moduleId,
      final BuildingBlockFactory buildingBlockFactory) throws ConfigurationException {

    final RevocationAuthorityPublicKeyBuildingBlock raPkBB =
        buildingBlockFactory
            .getBuildingBlockByClass(RevocationAuthorityPublicKeyBuildingBlock.class);

    return new VerifierModuleIssuance(this, systemParameters, verifierParameters, raPublicKey,
        moduleId, raPkBB);
  }


  @Override
  public ZkModuleProver getZkModuleProverPresentation(final String moduleId, final String attributeId,
                                                      final SystemParameters systemParameters, final VerifierParameters verifierParameters,
      final PublicKey raPublicKey, final RevocationInformation revocationInformation,
      final NonRevocationEvidence nonRevocationEvidence, final BuildingBlockFactory buildingBlockFactory)
      throws ConfigurationException, ProofException {
    final NonRevocationEvidenceFacade nreFacade = new NonRevocationEvidenceFacade(nonRevocationEvidence);
    final RevocationInformationFacade riFacade = new RevocationInformationFacade(revocationInformation);
    final ClRevocationStateWrapper revState = new ClRevocationStateWrapper(riFacade.getRevocationState());

    return new ProverModulePresentation(this, moduleId, attributeId, systemParameters, raPublicKey,
        nreFacade, revState, bigIntFactory, groupFactory, randomGeneration, buildingBlockFactory);
  }


  @Override
  public ZkModuleVerifier getZkModuleVerifierPresentation(final String moduleId, final String attributeId,
                                                          final SystemParameters systemParameters, final VerifierParameters verifierParameters,
      final PublicKey raPublicKey, final RevocationInformation revocationInformation,
      final BuildingBlockFactory buildingBlockFactory) throws ConfigurationException, ProofException {
    final RevocationInformationFacade riFacade = new RevocationInformationFacade(revocationInformation);
    final ClRevocationStateWrapper revState = new ClRevocationStateWrapper(riFacade.getRevocationState());

    return new VerifierModulePresentation(this, moduleId, attributeId, systemParameters,
        raPublicKey, revState, bigIntFactory, groupFactory, randomGeneration, buildingBlockFactory);
  }


}
