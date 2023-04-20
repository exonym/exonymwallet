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

package com.ibm.zurich.idmx.buildingBlock.signature.cl;

import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import com.ibm.zurich.idmx.annotations.Nullable;
import com.ibm.zurich.idmx.buildingBlock.factory.BuildingBlockFactory;
import com.ibm.zurich.idmx.buildingBlock.signature.SignatureBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.issuerKey.IssuerPublicKeyBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.systemParameters.SystemParametersBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.systemParameters.EcryptSystemParametersGenerator;
import com.ibm.zurich.idmx.buildingBlock.systemParameters.EcryptSystemParametersTemplateWrapper;
import com.ibm.zurich.idmx.buildingBlock.systemParameters.EcryptSystemParametersWrapper;
import com.ibm.zurich.idmx.configuration.Configuration;
import com.ibm.zurich.idmx.configuration.ErrorMessages;
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
import com.ibm.zurich.idmx.interfaces.util.Timing;
//import com.ibm.zurich.idmx.interfaces.util.group.Group;
import com.ibm.zurich.idmx.interfaces.util.group.GroupElement;
import com.ibm.zurich.idmx.interfaces.util.group.GroupFactory;
import com.ibm.zurich.idmx.interfaces.util.group.HiddenOrderGroup;
import com.ibm.zurich.idmx.interfaces.util.group.HiddenOrderGroupElement;
import com.ibm.zurich.idmx.interfaces.util.group.KnownOrderGroup;
import com.ibm.zurich.idmx.interfaces.util.group.KnownOrderGroupElement;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProver;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProverCarryOver;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProverIssuance;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleVerifier;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleVerifierCarryOver;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleVerifierIssuance;
import com.ibm.zurich.idmx.parameters.issuer.IssuerPublicKeyTemplateWrapper;
import com.ibm.zurich.idmx.util.NumberComparison;

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

/**
 * 
 */
public class ClSignatureBuildingBlock extends SignatureBuildingBlock {

  private final RandomGeneration randomGeneration;
  private final BigIntFactory bigIntFactory;
  private final GroupFactory groupFactory;
  private final BuildingBlockFactory buildingBlockFactory;
  private final Timing timing;
  private final ExternalSecretsManager esManager;

  @Inject
  public ClSignatureBuildingBlock(RandomGeneration randomGeneration, BigIntFactory bigIntFactory,
      GroupFactory groupFactory, BuildingBlockFactory buildingBlockFactory, Timing timing,
      ExternalSecretsManager esManager) {
    super(randomGeneration);
    this.randomGeneration = randomGeneration;
    this.bigIntFactory = bigIntFactory;
    this.groupFactory = groupFactory;
    this.buildingBlockFactory = buildingBlockFactory;
    this.timing = timing;
    this.esManager = esManager;
  }

  @Override
  public final String getImplementationIdSuffix() {
    return "cl";
  }

  @Override
  public void addBuildingBlockSystemParametersTemplate(SystemParametersTemplate template) {

    EcryptSystemParametersTemplateWrapper sptWrapper =
        new EcryptSystemParametersTemplateWrapper(template);

    sptWrapper.setStatisticalInd(Configuration.defaultStatisticalInd());
    sptWrapper.setAttributeLength(Configuration.defaultAttributeLength());
  }

  @Override
  public void generateBuildingBlockSystemParameters(SystemParametersTemplate template,
      SystemParameters systemParameters) throws ConfigurationException {

    EcryptSystemParametersTemplateWrapper sptWrapper =
        new EcryptSystemParametersTemplateWrapper(template);
    EcryptSystemParametersWrapper spWrapper = new EcryptSystemParametersWrapper(systemParameters);

    // calculate the bit length values based on the configuration values
    calculateBitLengthSystemParameters(sptWrapper, spWrapper);

    // generate the DH-Group parameters
    generateGroupElementSystemParamters(spWrapper);
  }

  /**
   * 
   * @return System parameters calculated according to the conditions as in Camenisch et al. (2010).
   *         http://domino.research.ibm.com/library/cyberdig
   *         .nsf/papers/EEB54FF3B91C1D648525759B004FBBB1 /$File/rz3730_revised.pdf
   * @throws ConfigurationException
   * @throws javax.naming.ConfigurationException
   */
  private void calculateBitLengthSystemParameters(EcryptSystemParametersTemplateWrapper sptWrapper,
      EcryptSystemParametersWrapper spWrapper) throws ConfigurationException {

    // Get own parameters form the template
    int statisticalInd = sptWrapper.getStatisticalZeroKnowledge();
    int attributeLength = sptWrapper.getAttributeLength();

    // Get general parameters from the system parameters
    int rsaModulusBitlength =
        (Integer) spWrapper.getParameter(EcryptSystemParametersGenerator.RSA_MODULUS_LENGTH_NAME);
    String hashFunction = spWrapper.getHashFunction();

    int symmetricKey =
        ((int) Math.round(EcryptSystemParametersGenerator
            .rsaEquivalentSecurityLevel(rsaModulusBitlength) / 8)) * 8;
    int asymmetricKey = 2 * symmetricKey;

    if (statisticalInd == 0) {
      statisticalInd = symmetricKey;
    }

    int hashBitLength = getHashFunctionBitLength(hashFunction);
    // int attributeLength = getHashFunctionBitLength(hashFunction);
    int dhModulusLength = rsaModulusBitlength;
    // Subgroup size should be a multiple of 8 (because of broken arithmetic on smartcards)
    // and must be strictly larger than the attribute length.
    int dhSubgroupLength = Math.max(attributeLength+8, asymmetricKey);
    
    //FIXME(ksa) something is VERY odd here
    //assert(dhSubgroupLength % 8 == 0);
    
    
    // Verify that the parameters adhere to the set of constraints
    verifyConstraints(hashBitLength, symmetricKey, statisticalInd, attributeLength,
        rsaModulusBitlength, dhSubgroupLength);

    // Set the parameters in the system parameters
    spWrapper.setAttributeLength(attributeLength);
    spWrapper.setDHModulusLength(dhModulusLength);
    spWrapper.setDHSubgroupLength(dhSubgroupLength);
    spWrapper.setStatisticalZeroKnowledge(statisticalInd);
    spWrapper.setPrimeProbability(statisticalInd);
  }

  private int getHashFunctionBitLength(String hashFunction) throws ConfigurationException {
    int l_H = -1;
    try {
      l_H = MessageDigest.getInstance(hashFunction).getDigestLength() * 8;
    } catch (NoSuchAlgorithmException e) {
      throw new ConfigurationException(e);
    }
    return l_H;
  }

  /**
   * Verifies that all security relevant constraints are respected.
   * 
   * @throws ConfigurationException
   */
  private void verifyConstraints(int hashFunctionBitLength, int symmetricKey, int statisticalInd,
      int attributeLength, int rsaModulusLength, int dhSubgroupLength)
      throws ConfigurationException {

    int l_H = hashFunctionBitLength;

    // supplemental parameters for verification
    int l_k = 2 * symmetricKey;
    int l_e = statisticalInd + l_H + attributeLength + 5;
    int l_prime_e = attributeLength + 1;
    int l_v = rsaModulusLength + 2 * statisticalInd + l_H + 3;

    if (l_e <= (statisticalInd + l_H + Math.max(attributeLength + 4, l_prime_e + 2))) {
      throw new ConfigurationException(
          "Constraint 1 violated: l_e <= (l_Phi + l_H + Math.max(l_m + 4, l_prime_e + 2)).");
    }
    if (l_v <= (rsaModulusLength + statisticalInd + l_H + statisticalInd + 2)) {
      throw new ConfigurationException(
          "Constraint 2 violated: l_v <= (l_n + l_Phi + l_H + l_Phi + 2).");
    }
    if (l_H < l_k) {
      throw new ConfigurationException("Constraint 3 violated: l_H < l_k.");
    }
    if (l_H >= l_e) {
      throw new ConfigurationException("Constraint 4 violated: l_H >= l_e.");
    }
    if (l_prime_e >= l_e - statisticalInd - l_H - 3) {
      throw new ConfigurationException("Constraint 5 violated: l_prime_e >= l_e - l_Phi - l_H - 3.");
    }
    /**
     * TODO(enr), issue #187. The subgroup order needs to be big enough so that the full range of
     * attribute values fits inside. We need to consider exactly what the security implications of
     * violating this constraint are.
     */
    /*
     * if (dhSubgroupLength > attributeLength) { throw new
     * ConfigurationException("Constraint 6 violated: l_rho > l_m."); }
     */
  }

  /**
   * Generates group parameters according to section 4.1 in math doc.
   * 
   * @throws ConfigurationException
   */
  private void generateGroupElementSystemParamters(EcryptSystemParametersWrapper spWrapper)
      throws ConfigurationException {

    int dhSubgroupLength = spWrapper.getDHSubgroupLength();
    int primeProbability = spWrapper.getPrimeProbability();
    int dhModulusLength = spWrapper.getDHModulusLength();
    int statisticalZk = spWrapper.getStatisticalInd();

    timing.startTiming();

    // select rho of given length with prime probability.
    BigInt dhSubgroupOrder =
        randomGeneration.generateRandomPrime(dhSubgroupLength, primeProbability);

    // find the group order Gamma.
    BigInt dhModulus = generateGroupModulus(dhSubgroupOrder, dhModulusLength, primeProbability);

    // get generator 1. see math doc for details.
    KnownOrderGroup primeOrderGroup = groupFactory.createPrimeOrderGroup(dhModulus, dhSubgroupOrder);
    // BigInt dhGenerator1 = newGenerator(dhSubgroupOrder, dhModulus, statisticalZk);
    KnownOrderGroupElement dhGenerator1 = primeOrderGroup.createRandomGenerator(randomGeneration);

    // compute second generator h = g^random, where random lies in the interval [0..rho].
    //
    // final BigInt rh =
    // randomGeneration.generateRandomNumber(bigIntFactory.zero(), dhSubgroupOrder, statisticalZk);
    // BigInt dhGenerator2 = dhGenerator1.modPow(rh, dhModulus);
    BigInt r_h = primeOrderGroup.createRandomIterationcounter(randomGeneration, statisticalZk);
   KnownOrderGroupElement dhGenerator2 = dhGenerator1.multOp(r_h);

    // Set the parameters in the system parameters
    spWrapper.setDHModulus(dhModulus);
    spWrapper.setDHSubgroupOrder(dhSubgroupOrder);
    spWrapper.setDHGenerator1(dhGenerator1.toBigInt());
    spWrapper.setDHGenerator2(dhGenerator2.toBigInt());

    timing.endTiming("DH-Group parameter generation");
  }

  /**
   * Generate the modulus for a prime order group. The length of the modulus and the prime
   * probability is taken from the system parameters. The modulus might actually be 1 bit less than
   * what is required by the system parameters.
   * 
   * @param rho The order of the prime order group
   * @return
   */
  private BigInt generateGroupModulus(BigInt rho, int dhModulusLength, int primeProbability) {
    BigInt capGamma;

    BigInt b; // co-factor of (Gamma - 1).
    BigInt maxB = bigIntFactory.two().pow(dhModulusLength).divide(rho);
    BigInt minB = maxB.divide(bigIntFactory.two());
    do {
      // see Table 4 of math doc as well as section 4.1
      do {
        b = randomGeneration.generateRandomNumber(minB, maxB, primeProbability);
        // b != 0 (mod order)
      } while (b.mod(rho).equals(bigIntFactory.zero()));

      // Gamma = (order * b) + 1
      capGamma = rho.multiply(b).add(bigIntFactory.one());
    } while (!capGamma.isProbablePrime(primeProbability)
        || !NumberComparison.isInInterval(capGamma, dhModulusLength - 1, dhModulusLength));
    return capGamma;
  }

  @Override
  public KeyPair generateBuildingBlockIssuerKeyPair(SystemParameters systemParameters,
      IssuerPublicKeyTemplate template) throws ConfigurationException {
    KeyPair keyPair = super.generateBuildingBlockIssuerKeyPair(systemParameters, template);
    ClKeyPairWrapper kpWrapper = new ClKeyPairWrapper(keyPair);
    IssuerPublicKeyTemplateWrapper ipkTemplateWrapper =
        new IssuerPublicKeyTemplateWrapper(template);

    EcryptSystemParametersWrapper spWrapper = new EcryptSystemParametersWrapper(systemParameters);

    int rsaModulusLength =
        (Integer) spWrapper.getParameter(EcryptSystemParametersGenerator.RSA_MODULUS_LENGTH_NAME);
    int primeProbability = spWrapper.getPrimeProbability();

    timing.startTiming();

    ClSecretKeyWrapper skWrapper = kpWrapper.getCLSecretKeyWrapper();
    ClHelper.generateSecretKey(randomGeneration, bigIntFactory, skWrapper, rsaModulusLength,
        primeProbability);

    timing.endTiming("Finished generating an RSA modulus of " + rsaModulusLength + " bits.");


    ClPublicKeyWrapper pkWrapper = kpWrapper.getCLPublicKeyWrapper();
    int numAttributes = ipkTemplateWrapper.getMaximalNumberOfAttributes();
    generatePublicKey(pkWrapper, skWrapper, systemParameters, numAttributes);

    return keyPair;
  }


  private void generatePublicKey(ClPublicKeyWrapper pk, ClSecretKeyWrapper skWrapper,
      SystemParameters systemParameters, int maximalNumberOfAttributes)
      throws ConfigurationException {

    timing.startTiming();

    EcryptSystemParametersWrapper spWrapper = new EcryptSystemParametersWrapper(systemParameters);

    int statisticalInd = spWrapper.getStatisticalInd();

    BigInt n = skWrapper.getModulus();
    BigInt pPrime = skWrapper.getSophieGermainPrimeP();
    BigInt qPrime = skWrapper.getSophieGermainPrimeQ();


    // BigInt capS = computeGeneratorQuadraticResidue((BigInt) sk.getModulus(), statisticalZK);
    HiddenOrderGroup qrGroup =
        groupFactory.createSignedQuadraticResiduesGroup(skWrapper.getModulus());
    HiddenOrderGroupElement capS = qrGroup.createRandomGenerator(randomGeneration);

    // p'*q'
    final BigInt productPQprime = pPrime.multiply(qPrime);

    // upper = p'q'-1 - 2
    final BigInt upper = productPQprime.subtract(bigIntFactory.one()).subtract(bigIntFactory.two());

    // capZ: rand num range [2 .. p'q'-1]. we pick capZ in [0..upper] and
    // then add 2.
    final BigInt x_Z =
        randomGeneration.generateRandomNumber(upper, statisticalInd).add(bigIntFactory.two());
    HiddenOrderGroupElement capZ = capS.multOp(x_Z);

    // capR[]
    HiddenOrderGroupElement[] capR = new HiddenOrderGroupElement[maximalNumberOfAttributes];
    for (int i = 0; i < maximalNumberOfAttributes; i++) {
      // pick x_R as rand num in range [2 .. p'q'-1]
      final BigInt x_R =
          randomGeneration.generateRandomNumber(upper, statisticalInd).add(bigIntFactory.two());

      capR[i] = capS.multOp(x_R);

      pk.setBase(i, capR[i].toBigInt());
    }

    // base for credential specification
    final BigInt x_Rt =
        randomGeneration.generateRandomNumber(upper, statisticalInd).add(bigIntFactory.two());
    final GroupElement<?, ?, ?> capRt = capS.multOp(x_Rt);
    pk.setRt(capRt.toBigInt());

    // base for device
    final BigInt x_Rd =
        randomGeneration.generateRandomNumber(upper, statisticalInd).add(bigIntFactory.two());
    final HiddenOrderGroupElement capRd = capS.multOp(x_Rd);
    pk.setRd(capRd.toBigInt());

    // add values to the key
    pk.setModulus(n);
    pk.setZ(capZ.toBigInt());
    pk.setS(capS.toBigInt());

    timing.endTiming("Generation of CL issuer public key.");
  }


  @Override
  public int getNumberOfAdditionalIssuanceRoundtrips() {
    return 0;
  }

  @Override
  public void addToIssuerParametersTemplate(IssuerPublicKeyTemplate issuerPublicKeyTemplate) {
    // Nothing to add
  }

  @Override
  public ZkModuleProver getZkModuleProverPresentation(SystemParameters systemParameters,
      VerifierParameters verifierParameters, PublicKey issuerPublicKey, String identifierOfModule,
      SignatureToken signatureToken, List<BigInt> encodedAttributes,
      BigInt credentialSpecificationId, @Nullable URI identifierOfSecret, String username,
      @Nullable URI identifierOfSignatureForSecret) throws ConfigurationException, ProofException {
    return new ProverModulePresentation(this, identifierOfModule, systemParameters,
        issuerPublicKey, signatureToken, encodedAttributes, credentialSpecificationId,
        identifierOfSecret, username, identifierOfSignatureForSecret, bigIntFactory, groupFactory,
        randomGeneration, esManager, buildingBlockFactory);
  }

  @Override
  public ZkModuleVerifier getZkModuleVerifierPresentation(SystemParameters systemParameters,
      VerifierParameters verifierParameters, PublicKey issuerPublicKey, String identifierOfModule,
      BigInt credentialSpecificationId, int numberOfAttributes, boolean externalDevice)
      throws ProofException, ConfigurationException {
    return new VerifierModulePresentation(this, identifierOfModule, systemParameters,
        issuerPublicKey, credentialSpecificationId, numberOfAttributes, externalDevice,
        bigIntFactory, groupFactory, buildingBlockFactory);
  }

  @Override
  public ZkModuleProverCarryOver getZkModuleProverCarryOver(SystemParameters systemParameters,
      VerifierParameters verifierParameters, PublicKey issuerPublicKey, String identifierOfModule,
      @Nullable URI identifierOfSecret,  String username, @Nullable URI identifierOfSignatureForSecret,
      BigInt credentialSpecificationId, List<Boolean> carryAttributeOver,
      List</* Nullable */BigInt> newCredentialAttributes) throws ProofException,
      ConfigurationException {
    return new ProverModuleCarryOver(this, identifierOfModule, systemParameters,
        verifierParameters, issuerPublicKey, identifierOfSecret, username, identifierOfSignatureForSecret,
        carryAttributeOver, newCredentialAttributes, credentialSpecificationId,
        buildingBlockFactory, groupFactory, bigIntFactory, randomGeneration, esManager);
  }

  @Override
  public ZkModuleVerifierCarryOver getZkModuleVerifierCarryOver(SystemParameters systemParameters,
      VerifierParameters verifierParameters, PublicKey issuerPublicKey, String identifierOfModule,
      BigInt credentialSpecificationId, List<Boolean> attributeSetByIssuer, boolean hasDevice)
      throws ConfigurationException, ProofException {
    return new VerifierModuleCarryOver(this, identifierOfModule, systemParameters,
        verifierParameters, issuerPublicKey, credentialSpecificationId, attributeSetByIssuer,
        hasDevice, buildingBlockFactory, groupFactory, bigIntFactory);
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

    return new ProverModuleIssuance(this, identifierOfModule, systemParameters, issuerPublicKey,
        issuerSecretKey, credentialSpecificationId, externalSecret, issuerSpecifiedAttributes,
        carryOverState, systemParamBB, issuerParamBB, groupFactory, bigIntFactory, randomGeneration);
  }

  @Override
  public ZkModuleVerifierIssuance getZkModuleVerifierIssuance(final SystemParameters systemParameters,
                                                              final VerifierParameters verifierParameters, final PublicKey issuerPublicKey, final String identifierOfModule,
      final BigInt credentialSpecificationId, final boolean hasDevice, final int numberOfAttributes,
      final @Nullable CarryOverStateRecipient carryOverState) throws ConfigurationException {
    final IssuerPublicKeyBuildingBlock issuerParamBB =
        buildingBlockFactory.getBuildingBlockByClass(IssuerPublicKeyBuildingBlock.class);
    final SystemParametersBuildingBlock systemParamBB =
        buildingBlockFactory.getBuildingBlockByClass(SystemParametersBuildingBlock.class);

    return new VerifierModuleIssuance(this, systemParameters, issuerPublicKey, identifierOfModule,
        carryOverState, credentialSpecificationId, numberOfAttributes, hasDevice, systemParamBB,
        issuerParamBB, groupFactory, bigIntFactory, randomGeneration, esManager);
  }

  @Override
  public @Nullable
  IssuanceExtraMessage extraIssuanceRoundRecipient(
      final @Nullable IssuanceExtraMessage messageFromIssuer, final IssuanceStateRecipient stateRecipient) {
    throw new RuntimeException(ErrorMessages.numberOfMaximalRoundsReached("CL signatures"));
  }

  @Override
  public IssuanceExtraMessage extraIssuanceRoundIssuer(final IssuanceExtraMessage messageFromRecipient,
                                                       final IssuanceStateIssuer stateIssuer) {
    throw new RuntimeException(ErrorMessages.numberOfMaximalRoundsReached("CL signatures"));
  }

  @Override
  public ListOfSignaturesAndAttributes extractSignature(
      final @Nullable IssuanceExtraMessage messageFromIssuer, final IssuanceStateRecipient stateRecipient)
      throws ConfigurationException {
    return VerifierModuleIssuance.extractSignature((ClIssuanceStateRecipient) stateRecipient, bigIntFactory);
  }
}
