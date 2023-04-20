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

package com.ibm.zurich.idmx.buildingBlock.signature.uprove;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.List;
import java.util.logging.Logger;

import com.ibm.zurich.idmx.annotations.Nullable;
import com.ibm.zurich.idmx.buildingBlock.factory.BuildingBlockFactory;
import com.ibm.zurich.idmx.buildingBlock.signature.SignatureBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.issuerKey.IssuerPublicKeyBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.systemParameters.SystemParametersBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.systemParameters.EcryptSystemParametersWrapper;
import com.ibm.zurich.idmx.configuration.Configuration;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.ProofException;
import com.ibm.zurich.idmx.interfaces.device.ExternalSecretsManager;
import com.ibm.zurich.idmx.interfaces.signature.ListOfSignaturesAndAttributes;
import com.ibm.zurich.idmx.interfaces.state.CarryOverStateIssuer;
import com.ibm.zurich.idmx.interfaces.state.CarryOverStateRecipient;
import com.ibm.zurich.idmx.interfaces.state.IssuanceStateIssuer;
import com.ibm.zurich.idmx.interfaces.state.IssuanceStateRecipient;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.RandomGeneration;
import com.ibm.zurich.idmx.interfaces.util.TestVectorHelper;
import com.ibm.zurich.idmx.interfaces.util.group.GroupFactory;
import com.ibm.zurich.idmx.interfaces.util.group.KnownOrderGroup;
import com.ibm.zurich.idmx.interfaces.util.group.KnownOrderGroupElement;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProver;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProverCarryOver;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProverIssuance;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleVerifier;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleVerifierCarryOver;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleVerifierIssuance;

import eu.abc4trust.xml.IssuanceExtraMessage;
import eu.abc4trust.xml.IssuerPublicKeyTemplate;
import eu.abc4trust.xml.KeyPair;
import eu.abc4trust.xml.PrivateKey;
import eu.abc4trust.xml.PublicKey;
import eu.abc4trust.xml.SignatureToken;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.SystemParametersTemplate;
import eu.abc4trust.xml.VerifierParameters;

import javax.inject.Inject;

public class BrandsSignatureBuildingBlock extends SignatureBuildingBlock {

  private final RandomGeneration randomGeneration;
  private final GroupFactory groupFactory;
  private final ExternalSecretsManager externalSecretManager;
  private final BuildingBlockFactory buildingBlockFactory;
  private final BigIntFactory bigIntFactory;
  private final Logger logger;
  private final TestVectorHelper testVector;

  @Inject
  public BrandsSignatureBuildingBlock(final RandomGeneration randomGeneration, final GroupFactory groupFactory,
                                      final BigIntFactory bigIntFactory, final ExternalSecretsManager externalSecretManager,
      final BuildingBlockFactory buildingBlockFactory, final Logger logger, final TestVectorHelper testVector) {
    super(randomGeneration);
    this.randomGeneration = randomGeneration;
    this.groupFactory = groupFactory;
    this.bigIntFactory = bigIntFactory;
    this.externalSecretManager = externalSecretManager;
    this.buildingBlockFactory = buildingBlockFactory;
    this.logger = logger;
    this.testVector = testVector;
  }

  @Override
  protected String getBuildingBlockIdSuffix() {
    return super.getBuildingBlockIdSuffix().concat(":uprove");
  }

  @Override
  protected String getImplementationIdSuffix() {
    return super.getBuildingBlockIdSuffix().concat(":uprove");
  }

  @Override
  public void addBuildingBlockSystemParametersTemplate(final SystemParametersTemplate template) {
    // This building block doesn't contribute to system parameters
  }

  @Override
  public void generateBuildingBlockSystemParameters(final SystemParametersTemplate template,
                                                    final SystemParameters systemParameters) throws ConfigurationException {
    // This building block doesn't contribute to system parameters
  }

  @Override
  public KeyPair generateBuildingBlockIssuerKeyPair(final SystemParameters systemParameters,
                                                    final IssuerPublicKeyTemplate ipkTemplate) throws ConfigurationException {

    final KeyPair xmlKeyPair = super.generateBuildingBlockIssuerKeyPair(systemParameters, ipkTemplate);
    final BrandsKeyPairWrapper keyPair = new BrandsKeyPairWrapper(xmlKeyPair);

    final BrandsKeyPairTemplateWrapper tw = new BrandsKeyPairTemplateWrapper(ipkTemplate);
    final EcryptSystemParametersWrapper sp = new EcryptSystemParametersWrapper(systemParameters);

    final BigInt modulus = sp.getDHModulus();
    final BigInt subgroupOrder = sp.getDHSubgroupOrder();
    final KnownOrderGroup group = groupFactory.createPrimeOrderGroup(modulus, subgroupOrder);

    final BrandsSecretKeyWrapper sk = keyPair.getUProvePrivateKeyWrapper();
    final BigInt y0 = group.createRandomIterationcounter(randomGeneration, sp.getStatisticalInd());
    sk.setY0(y0);

    final BrandsPublicKeyWrapper pk = keyPair.getUProvePublicKeyWrapper();
    final KnownOrderGroupElement g = group.valueOfNoCheck(sp.getDHGenerator1());
    final KnownOrderGroupElement g0 = g.multOp(y0);
    pk.setG0(g0.toBigInt());
    final int maximalNumberOfAttributes = tw.getMaximalNumberOfAttributes();
    for (int i = 1; i <= maximalNumberOfAttributes; ++i) {
      final KnownOrderGroupElement gi = group.createRandomElement(randomGeneration);
      pk.setGI(gi.toBigInt(), i);
    }
    final KnownOrderGroupElement gExt = group.createRandomElement(randomGeneration);
    pk.setGExt(gExt.toBigInt());
    final KnownOrderGroupElement gT = group.createRandomElement(randomGeneration);
    pk.setGT(gT.toBigInt());
    pk.setNumberOfUProveTokens(tw.getNumberOfUProveTokens());
    
    try {
      pk.setUidP(bigIntFactory.unsignedValueOf(pk.getPublicKeyId().toString().getBytes("UTF-8")));
      pk.setS(bigIntFactory.unsignedValueOf(pk.getSystemParametersId().toString().getBytes("UTF-8")));
    } catch (final UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }

    return xmlKeyPair;
  }

  @Override
  public int getNumberOfAdditionalIssuanceRoundtrips() {
    return 1;
  }

  @Override
  public void addToIssuerParametersTemplate(final IssuerPublicKeyTemplate issuerPublicKeyTemplate) {
    final BrandsKeyPairTemplateWrapper tw = new BrandsKeyPairTemplateWrapper(issuerPublicKeyTemplate);
    tw.setNumberOfUProveTokens(Configuration.defaultNumberOfSignatureTokens());
  }

  @Override
  public ZkModuleProver getZkModuleProverPresentation(final SystemParameters systemParameters,
                                                      final VerifierParameters verifierParameters, final PublicKey issuerPublicKey, final String identifierOfModule,
      final SignatureToken signatureToken, final List<BigInt> encodedAttributes,
      final BigInt credentialSpecificationId, final @Nullable URI identifierOfSecret, final String username,
      final @Nullable URI identifierOfSignatureForSecret) {
    return new ProverModulePresentation(this, systemParameters, issuerPublicKey,
        identifierOfModule, signatureToken, encodedAttributes, credentialSpecificationId,
        identifierOfSecret, username, identifierOfSignatureForSecret, groupFactory, bigIntFactory,
        randomGeneration, externalSecretManager, testVector);
  }

  @Override
  public ZkModuleVerifier getZkModuleVerifierPresentation(final SystemParameters systemParameters,
                                                          final VerifierParameters verifierParameters, final PublicKey issuerPublicKey, final String identifierOfModule,
      final BigInt credentialSpecificationId, final int numberOfAttributes, final boolean externalDevice) {
    return new VerifierModulePresentation(this, systemParameters, issuerPublicKey,
        identifierOfModule, credentialSpecificationId, numberOfAttributes, externalDevice,
        groupFactory, logger, testVector);
  }

  @Override
  public ZkModuleProverCarryOver getZkModuleProverCarryOver(final SystemParameters systemParameters,
                                                            final VerifierParameters verifierParameters, final PublicKey issuerPublicKey, final String identifierOfModule,
      final @Nullable URI identifierOfSecret,  final String username, final @Nullable URI identifierOfSignatureForSecret,
      final BigInt credentialSpecificationId, List<Boolean> includeAttributeInCommitment,
      final List</* Nullable */BigInt> encodedAttributes) throws ConfigurationException, ProofException {
    return new ProverModuleCarryOver(this, identifierOfModule, systemParameters,
        verifierParameters, issuerPublicKey, identifierOfSecret, username, identifierOfSignatureForSecret,
        includeAttributeInCommitment, encodedAttributes, credentialSpecificationId,
        externalSecretManager, buildingBlockFactory, groupFactory, bigIntFactory, randomGeneration);
  }

  @Override
  public ZkModuleVerifierCarryOver getZkModuleVerifierCarryOver(final SystemParameters systemParameters,
                                                                final VerifierParameters verifierParameters, final PublicKey issuerPublicKey, final String identifierOfModule,
      final BigInt credentialSpecificationId, final List<Boolean> attributeSetByIssuer, final boolean hasDevice)
      throws ConfigurationException, ProofException {
    // TODO: attributeSetByIssuer or complement???
    return new VerifierModuleCarryOver(this, identifierOfModule, systemParameters,
        verifierParameters, issuerPublicKey, attributeSetByIssuer, credentialSpecificationId,
        hasDevice, externalSecretManager, logger, buildingBlockFactory, groupFactory,
        bigIntFactory, randomGeneration);

  }

  @Override
  public ZkModuleProverIssuance getZkModuleProverIssuance(final SystemParameters systemParameters,
                                                          final VerifierParameters verifierParameters, final PublicKey issuerPublicKey, final PrivateKey issuerSecretKey,
      final String identifierOfModule, final BigInt credentialSpecificationId, final boolean externalSecret,
      final List</* Nullable */BigInt> issuerSpecifiedAttributes,
      final @Nullable CarryOverStateIssuer carryOverState) throws ConfigurationException {
    final IssuerPublicKeyBuildingBlock issuerParamBB =
        buildingBlockFactory.getBuildingBlockByClass(IssuerPublicKeyBuildingBlock.class);
    final SystemParametersBuildingBlock systemParamBB =
        buildingBlockFactory.getBuildingBlockByClass(SystemParametersBuildingBlock.class);

    return new ProverModuleIssuance(this, systemParameters, issuerPublicKey, issuerSecretKey,
        identifierOfModule, credentialSpecificationId, externalSecret, issuerSpecifiedAttributes,
        carryOverState, systemParamBB, issuerParamBB, groupFactory, randomGeneration, testVector);
  }

  @Override
  public ZkModuleVerifierIssuance getZkModuleVerifierIssuance(final SystemParameters systemParameters,
                                                              final VerifierParameters verifierParameters, final PublicKey issuerPublicKey, final String identifierOfModule,
      final BigInt credentialSpecificationId, final boolean externalDevice, final int numberOfAttributes,
      final @Nullable CarryOverStateRecipient carryOverState) throws ConfigurationException {
    final IssuerPublicKeyBuildingBlock issuerParamBB =
        buildingBlockFactory.getBuildingBlockByClass(IssuerPublicKeyBuildingBlock.class);
    final SystemParametersBuildingBlock systemParamBB =
        buildingBlockFactory.getBuildingBlockByClass(SystemParametersBuildingBlock.class);

    return new VerifierModuleIssuance(this, systemParameters, issuerPublicKey, identifierOfModule,
        carryOverState, credentialSpecificationId, numberOfAttributes, externalDevice,
        systemParamBB, issuerParamBB, groupFactory, bigIntFactory, randomGeneration, testVector);
  }

  @Override
  public @Nullable
  IssuanceExtraMessage extraIssuanceRoundRecipient(
      final @Nullable IssuanceExtraMessage messageFromIssuer, final IssuanceStateRecipient stateRecipient) {
    return VerifierModuleIssuance.extraRound(messageFromIssuer,
        (BrandsIssuanceStateRecipient) stateRecipient, bigIntFactory);
  }

  @Override
  public IssuanceExtraMessage extraIssuanceRoundIssuer(final IssuanceExtraMessage messageFromRecipient,
                                                       final IssuanceStateIssuer stateIssuer) {
    return ProverModuleIssuance.extraRound(messageFromRecipient,
        (BrandsIssuanceStateIssuer) stateIssuer, bigIntFactory);
  }

  @Override
  public ListOfSignaturesAndAttributes extractSignature(
      final @Nullable IssuanceExtraMessage messageFromIssuer, final IssuanceStateRecipient stateRecipient) {
    return VerifierModuleIssuance.extractSignature(messageFromIssuer,
        (BrandsIssuanceStateRecipient) stateRecipient, testVector, groupFactory, bigIntFactory);
  }
}
