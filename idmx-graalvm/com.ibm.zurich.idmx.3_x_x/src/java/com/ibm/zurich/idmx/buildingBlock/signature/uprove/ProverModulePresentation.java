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

import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import com.ibm.zurich.idmx.annotations.Nullable;
import com.ibm.zurich.idmx.buildingBlock.systemParameters.EcryptSystemParametersWrapper;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.interfaces.device.ExternalSecretsManager;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.RandomGeneration;
import com.ibm.zurich.idmx.interfaces.util.TestVectorHelper;
import com.ibm.zurich.idmx.interfaces.util.group.GroupFactory;
import com.ibm.zurich.idmx.interfaces.util.group.KnownOrderGroup;
import com.ibm.zurich.idmx.interfaces.util.group.KnownOrderGroupElement;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProver;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ResidueClass;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateCollect;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateFirstRound;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateInitialize;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateSecondRound;
import com.ibm.zurich.idmx.zkModule.ZkModuleImpl;

import eu.abc4trust.xml.PublicKey;
import eu.abc4trust.xml.SignatureToken;
import eu.abc4trust.xml.SystemParameters;

/**
 * Note: This ZkModule does not use a pedersen representation block as a child module, since this
 * module wants to compute its hash contribution itself.
 */
class ProverModulePresentation extends ZkModuleImpl implements ZkModuleProver {

  private final GroupFactory groupFactory;
  private final BigIntFactory bigIntFactory;
  private final RandomGeneration randomGeneration;
  private final TestVectorHelper testVector;

  private final EcryptSystemParametersWrapper sp;
  private final BrandsPublicKeyWrapper pk;
  private final BrandsSignatureTokenWrapper token;
  private final List<BigInt> attributes;
  @Nullable
  private final URI deviceUid;
  private final String username;
  @Nullable
  private final URI credentialUid;
  private final ExternalSecretsManager esManager;
  private final boolean device;


  public ProverModulePresentation(final BrandsSignatureBuildingBlock parent,
                                  final SystemParameters systemParameters, final PublicKey issuerPublicKey, final String identifierOfModule,
      final SignatureToken signatureToken, final List<BigInt> encodedAttributes,
      final BigInt credentialSpecificationId, final URI identifierOfSecret, final String username,
      final URI identifierOfSignatureForSecret,
      final GroupFactory groupFactory, final BigIntFactory bigIntFactory, final RandomGeneration randomGeneration,
      final ExternalSecretsManager esManager, final TestVectorHelper testVector) {

    super(parent, identifierOfModule);

    this.sp = new EcryptSystemParametersWrapper(systemParameters);
    this.pk = new BrandsPublicKeyWrapper(issuerPublicKey);
    this.token = new BrandsSignatureTokenWrapper(signatureToken);
    this.attributes = encodedAttributes;
    this.deviceUid = identifierOfSecret;
    this.credentialUid = identifierOfSignatureForSecret;

    this.groupFactory = groupFactory;
    this.bigIntFactory = bigIntFactory;
    this.randomGeneration = randomGeneration;
    this.esManager = esManager;
    this.testVector = testVector;
    this.username = username;
    
    this.device = (deviceUid != null);
  }


  @Override
  public void initializeModule(final ZkProofStateInitialize zkBuilder) throws ConfigurationException {
    zkBuilder.markAsSignatureBuildingBlock();
    
    for (int i = 0; i < attributes.size(); ++i) {
      zkBuilder.registerAttribute(identifierOfAttribute(i), false);
      zkBuilder.setValueOfAttribute(identifierOfAttribute(i), attributes.get(i), ResidueClass.RESIDUE_CLASS_MOD_Q);
      if(testVector.isActive()) {
        if(testVector.valueExists("w"+(i+1))) {
          BigInt wi = testVector.getValueAsBigInt("w" + (i+1));
          zkBuilder.overrideRValueOfAttribute(identifierOfAttribute(i), wi);
        }
      }
    }
    if (device) {
      sanityCheckOfParametersOnDevice();
      zkBuilder.registerAttribute(identifierOfSecretAttribute(), true);
      zkBuilder.getDeviceProofSpecification().addCredentialProof(deviceUid, credentialUid);
    }
  }

  private void sanityCheckOfParametersOnDevice() throws ConfigurationException {
    //TODO(ksa) Should be BigInt here?
    final BigInteger expectedGD = pk.getGD().getValue();
    final BigInteger actualGD = esManager.getBaseForDeviceSecret(username, deviceUid, credentialUid);
    if (!expectedGD.equals(actualGD)) {
      throw new RuntimeException("Incorrect base g_d for credential secret on " + deviceUid
          + " for " + credentialUid + " - expected " + expectedGD + " got " + actualGD);
    }
    final BigInteger actualGRandomizer = esManager.getBaseForCredentialSecret(username, deviceUid, credentialUid);
    if (actualGRandomizer != null && !(actualGRandomizer.equals(BigInteger.ONE))) {
      throw new RuntimeException("Base for device secret must be null on " + deviceUid + " for "
          + credentialUid + " - got " + actualGRandomizer);
    }
    final BigInteger expectedP = sp.getDHModulus().getValue();
    final BigInteger actualP = esManager.getModulus(username, deviceUid, credentialUid);
    if (!expectedP.equals(actualP)) {
      throw new RuntimeException("Incorrect modulus p on " + deviceUid + " for " + credentialUid
          + " expected " + expectedP + " got " + actualP);
    }
  }


  @Override
  public void collectAttributesForProof(ZkProofStateCollect zkBuilder) {
    // Nothing needs to be done
  }

  private BigInt w0;

  @Override
  public void firstRound(final ZkProofStateFirstRound zkBuilder) throws ConfigurationException {
    final KnownOrderGroup group =
        groupFactory.createPrimeOrderGroup(sp.getDHModulus(), sp.getDHSubgroupOrder());

    // aPreimage = h^w0 * PROD_unrevealed gi^{wi}
    final KnownOrderGroupElement h = group.valueOfNoCheck(token.getH());
    w0 = group.createRandomIterationcounter(randomGeneration, sp.getStatisticalInd());
    if(testVector.isActive()) {
      w0 = testVector.getValueAsBigInt("w0");
    }
    KnownOrderGroupElement aPreimage = h.multOp(w0);

    final List<Integer> listOfDisclosed = new ArrayList<Integer>();
    final List<BigInt> valueOfDisclosed = new ArrayList<BigInt>();
    for (int i = 0; i < attributes.size(); ++i) {
      final boolean revealed = zkBuilder.isRevealedAttribute(identifierOfAttribute(i));
      if (revealed) {
        listOfDisclosed.add(i + 1);
        valueOfDisclosed.add(attributes.get(i));
      } else {
        final KnownOrderGroupElement gi = group.valueOfNoCheck(pk.getGI(i + 1));
        final BigInt wi = zkBuilder.getRValueOfAttribute(identifierOfAttribute(i));
        aPreimage = aPreimage.opMultOp(gi, wi);
      }
    }
    if (device) {
      final BigInteger ad_bigInt =
          zkBuilder.getDeviceProofCommitment().getCommitmentForCredential(deviceUid, credentialUid);
      final KnownOrderGroupElement ad = group.valueOfNoCheck(bigIntFactory.valueOf(ad_bigInt));
      aPreimage = aPreimage.op(ad);
    }

    // a = hash(aPreimage)
    final byte[] a = hash(aPreimage);
    testVector.checkValue(a, "a");

    // Values delivered to prover, but these will not be directly added to
    // the hash contribution since we set the hash contribution manually.
    zkBuilder.addDValue(getIdentifier() + ":a", a, a);
    zkBuilder.addDValue(getIdentifier() + ":h", h);
    zkBuilder.addDValue(getIdentifier() + ":sigmaZPrime", token.getSigmaZPrime());
    zkBuilder.addDValue(getIdentifier() + ":sigmaCPrime", token.getSigmaCPrime());
    zkBuilder.addDValue(getIdentifier() + ":sigmaRPrime", token.getSigmaRPrime());
    // Prover information (optional field)
    final BigInt hasPI = (token.hasProverInformation())?bigIntFactory.one():bigIntFactory.zero();
    zkBuilder.addDValue(getIdentifier()+":hasPI", hasPI);
    if(token.hasProverInformation()) {
      zkBuilder.addDValue(getIdentifier()+":PI", token.getProverInformation().toByteArrayUnsigned());
    }
    
    final BigInt message;
    if(testVector.isActive()) {
      message = testVector.getValueAsBigInt("m");
    } else {
      message = null;
    }
    if(message != null) {
      zkBuilder.addDValue(getIdentifier()+":hasM", bigIntFactory.one());
      zkBuilder.addDValue(getIdentifier()+":m", message);
    } else {
      zkBuilder.addDValue(getIdentifier()+":hasM", bigIntFactory.zero());
    }

    // Set hash contribution
    final byte[] tokenId =
        BrandsSignatureHelper.getTokenId(token.getH(), token.getSigmaZPrime(),
            token.getSigmaCPrime(), token.getSigmaRPrime(), sp);
    testVector.checkValue(tokenId, "UIDt");

    final byte[] hashContribution =
        BrandsSignatureHelper.getHashContribution(tokenId, a, listOfDisclosed, valueOfDisclosed,
            message, sp);
    zkBuilder.setHashContributionOfBuildingBlock(hashContribution);
  }

  private byte[] hash(final KnownOrderGroupElement aPreimage) throws ConfigurationException {
    final BrandsSignatureHashing hashing = new BrandsSignatureHashing(sp);
    hashing.add(aPreimage);
    return hashing.digestRaw();
  }

  @Override
  public void secondRound(final ZkProofStateSecondRound zkBuilder) throws ConfigurationException {
    final BigInt c = zkBuilder.getChallenge();
    final BigInt q = sp.getDHSubgroupOrder();
    final BigInt alphaInverse = token.getAlphaInverse();

    final BigInt r0 = c.multiply(alphaInverse).add(w0).mod(q);
    zkBuilder.addSValue(getIdentifier() + ":r0", r0);
    
    if(device) {
      final BigInteger rd_bigInt = zkBuilder.getDeviceProofResponse().getResponseForDeviceSecretKey(deviceUid);
      final BigInt rd = bigIntFactory.valueOf(rd_bigInt);
      zkBuilder.setSValueOfExternalAttribute(identifierOfSecretAttribute(), rd);
    }
  }
}
