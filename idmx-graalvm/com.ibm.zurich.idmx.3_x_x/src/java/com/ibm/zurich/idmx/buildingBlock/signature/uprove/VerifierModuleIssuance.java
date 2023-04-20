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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.ibm.zurich.idmx.annotations.Nullable;
import com.ibm.zurich.idmx.buildingBlock.signature.uprove.BrandsIssuanceStateRecipient.TokenState;
import com.ibm.zurich.idmx.buildingBlock.structural.issuerKey.IssuerPublicKeyBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.systemParameters.SystemParametersBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.systemParameters.EcryptSystemParametersWrapper;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.ProofException;
import com.ibm.zurich.idmx.interfaces.signature.ListOfSignaturesAndAttributes;
import com.ibm.zurich.idmx.interfaces.state.CarryOverStateRecipient;
import com.ibm.zurich.idmx.interfaces.state.IssuanceStateRecipient;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.RandomGeneration;
import com.ibm.zurich.idmx.interfaces.util.TestVectorHelper;
import com.ibm.zurich.idmx.interfaces.util.group.GroupFactory;
import com.ibm.zurich.idmx.interfaces.util.group.KnownOrderGroup;
import com.ibm.zurich.idmx.interfaces.util.group.KnownOrderGroupElement;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleVerifier;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleVerifierIssuance;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ResidueClass;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkVerifierStateCollect;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkVerifierStateVerify;
import com.ibm.zurich.idmx.jaxb.wrapper.IssuanceExtraMessageWrapper;
import com.ibm.zurich.idmx.zkModule.ZkModuleImpl;

import eu.abc4trust.xml.IssuanceExtraMessage;
import eu.abc4trust.xml.PublicKey;
import eu.abc4trust.xml.Signature;
import eu.abc4trust.xml.SignatureToken;
import eu.abc4trust.xml.SystemParameters;

class VerifierModuleIssuance extends ZkModuleImpl implements ZkModuleVerifierIssuance {

  private final BigIntFactory bigIntFactory;
  private final GroupFactory groupFactory;
  private final RandomGeneration randomGeneration;
  private final TestVectorHelper testVector;

  private final EcryptSystemParametersWrapper sp;
  private final BrandsPublicKeyWrapper pk;
  private final @Nullable
  CarryOverStateRecipient carryOverState;
  private final boolean onDevice;
  private final BigInt credentialSpecificationId;
  private final int numberOfAttributes;

  private final List<ZkModuleVerifier> childZkModules;
  private final List<BigInt> revealedAttributes;
  private final BrandsIssuanceStateRecipient state;

  public VerifierModuleIssuance(final BrandsSignatureBuildingBlock parent,
                                final SystemParameters systemParameters, final PublicKey issuerPublicKey, final String identifierOfModule,
      final @Nullable CarryOverStateRecipient carryOverState, final BigInt credentialSpecificationId,
      final int numberOfAttributes, final boolean externalDevice, final SystemParametersBuildingBlock systemParamBB,
      final IssuerPublicKeyBuildingBlock issuerParamBB, final GroupFactory groupFactory,
      final BigIntFactory bigIntFactory, final RandomGeneration randomGeneration, final TestVectorHelper testVector) {

    super(parent, identifierOfModule);

    this.groupFactory = groupFactory;
    this.bigIntFactory = bigIntFactory;
    this.randomGeneration = randomGeneration;
    this.testVector = testVector;

    this.sp = new EcryptSystemParametersWrapper(systemParameters);
    this.pk = new BrandsPublicKeyWrapper(issuerPublicKey);

    this.carryOverState = carryOverState;
    this.credentialSpecificationId = credentialSpecificationId;
    this.onDevice = externalDevice;
    this.numberOfAttributes = numberOfAttributes;


    this.childZkModules = new ArrayList<ZkModuleVerifier>();
    final ZkModuleVerifier sp =
        systemParamBB.getZkModuleVerifier(identifierOfModule + ":sp", systemParameters);
    childZkModules.add(sp);
    final ZkModuleVerifier ip =
        issuerParamBB.getZkModuleVerifier(identifierOfModule + ":ip", systemParameters,
            issuerPublicKey);
    childZkModules.add(ip);

    this.revealedAttributes = new ArrayList<BigInt>();
    this.state = new BrandsIssuanceStateRecipient();
  }


  @Override
  public void collectAttributesForVerify(final ZkVerifierStateCollect zkVerifier) throws ProofException,
      ConfigurationException {
    for (int i = 0; i < numberOfAttributes; ++i) {
      if (isIssuerSetAttribute(i)) {
        final String attributeName = identifierOfAttribute(i);
        zkVerifier.registerAttribute(attributeName, false);
        zkVerifier.attributeIsRevealed(attributeName);
        zkVerifier.setResidueClass(attributeName, ResidueClass.RESIDUE_CLASS_MOD_Q);
      }
    }

    for (final ZkModuleVerifier zkm : childZkModules) {
      zkm.collectAttributesForVerify(zkVerifier);
    }
  }


  private boolean isIssuerSetAttribute(final int i) {
    if (carryOverState == null) {
      return true;
    } else {
      return (carryOverState.getAttributeValues(bigIntFactory).get(i) == null);
    }
  }

  private BigInt getRandomizer() {
    if (carryOverState == null) {
      return bigIntFactory.zero();
    } else {
      return carryOverState.getOpening(bigIntFactory);
    }
  }

  @Override
  public boolean verify(ZkVerifierStateVerify zkVerifier) throws ConfigurationException,
      ProofException {

    // Recover attribute values
    revealedAttributes.clear();
    for (int i = 0; i < numberOfAttributes; ++i) {
      final BigInt newValue;
      if (isIssuerSetAttribute(i)) {
        final String attributeName = identifierOfAttribute(i);
        if (zkVerifier.isRevealedAttribute(attributeName)) {
          newValue = zkVerifier.getValueOfRevealedAttribute(attributeName);
        } else {
          throw new ConfigurationException("Attribute " + i
              + " is not revealed in Uprove issuance.");
        }
      } else {
        newValue = carryOverState.getAttributeValues(bigIntFactory).get(i);
      }
      revealedAttributes.add(newValue);
    }
    state.setAttributes(revealedAttributes);
    state.setRandomizer(getRandomizer());


    final BigInt q = sp.getDHSubgroupOrder();
    state.setQ(q);
    final BigInt modulus = sp.getDHModulus();
    state.setModulus(modulus);
    // dso: changed Group to KnownOrderGroup here to allow for invertExponent method
    final KnownOrderGroup group = groupFactory.createPrimeOrderGroup(modulus, q);
    final KnownOrderGroupElement g = group.valueOfNoCheck(sp.getDHGenerator1());
    final KnownOrderGroupElement blindedGamma = computeBlindedGamma(group);
    KnownOrderGroupElement gamma = blindedGamma.opMultOp(g, getRandomizer().negate());

    
    zkVerifier.checkNValue(getIdentifier() + ":gamma", blindedGamma);
    final KnownOrderGroupElement sigmaZ =
        zkVerifier.getDValueAsGroupElement(getIdentifier() + ":sigma_z", group);
    state.setG(g);
    final KnownOrderGroupElement g0 = group.valueOfNoCheck(pk.getG0());
    state.setG0(g0);

    final int numberOfTokens = pk.getNumberOfTokens();
    for (int i = 0; i < numberOfTokens; ++i) {
      BigInt alpha = group.createRandomIterationcounter(randomGeneration, sp.getStatisticalInd());
      BigInt beta1 = group.createRandomIterationcounter(randomGeneration, sp.getStatisticalInd());
      BigInt beta2 = group.createRandomIterationcounter(randomGeneration, sp.getStatisticalInd());
      
      if(testVector.isActive()) {
        alpha = testVector.getValueAsBigInt("alpha");
        beta1 = testVector.getValueAsBigInt("beta1");
        beta2 = testVector.getValueAsBigInt("beta2");
      }

      final KnownOrderGroupElement h = gamma.multOp(alpha);
      testVector.checkValue(h.toBigInt(), "h");
      final KnownOrderGroupElement t1 = g0.multOp(beta1).opMultOp(g, beta2);
      final KnownOrderGroupElement t2 = h.multOp(beta2);
      final KnownOrderGroupElement sigmaA =
          zkVerifier.getDValueAsGroupElement(getIdentifier() + ":sigma_a:" + Integer.valueOf(i),
              group);
      final KnownOrderGroupElement sigmaB =
          zkVerifier.getDValueAsGroupElement(getIdentifier() + ":sigma_b:" + Integer.valueOf(i),
              group);
      final KnownOrderGroupElement unblidedSigmaZ = sigmaZ.opMultOp(g0, getRandomizer().negate());
      final KnownOrderGroupElement sigmaZPrime = unblidedSigmaZ.multOp(alpha);
      testVector.checkValue(sigmaZPrime.toBigInt(), "sigmaZPrime");
      final KnownOrderGroupElement sigmaAPrime = t1.op(sigmaA);
      testVector.checkValue(sigmaAPrime.toBigInt(), "sigmaAPrime");
      final KnownOrderGroupElement unblidedSigmaB = sigmaB.opMultOp(sigmaA, getRandomizer().negate());
      final KnownOrderGroupElement sigmaBPrime = sigmaZPrime.multOp(beta1).op(t2).opMultOp(unblidedSigmaB, alpha);
      testVector.checkValue(sigmaBPrime.toBigInt(), "sigmaBPrime");
      final byte[] proverInformation;
      if(testVector.isActive()) {
        proverInformation = testVector.getValueAsBytes("PI");
      } else {
        proverInformation = null;
      }
      final BigInt sigmaCPrime =
          BrandsSignatureHelper.hashToken(sp, h, proverInformation, sigmaZPrime, sigmaAPrime,
              sigmaBPrime);
      testVector.checkValue(sigmaCPrime, "sigmaCPrime");
      final BigInt sigmaC = sigmaCPrime.add(beta1).mod(q);
      testVector.checkValue(sigmaC, "sigmaC");


      final TokenState tokenState = state.new TokenState();
      final BigInt alphaInverse = group.invertIterationcounter(alpha);
      tokenState.setAlphaInverse(alphaInverse);
      testVector.checkValue(alphaInverse, "alphaInverse");
      tokenState.setBeta2(beta2);
      tokenState.setH(h);
      tokenState.setSigmaAPrime(sigmaAPrime);
      tokenState.setSigmaBPrime(sigmaBPrime);
      tokenState.setSigmaC(sigmaC);
      tokenState.setSigmaCPrime(sigmaCPrime);
      tokenState.setSigmaZPrime(sigmaZPrime);
      tokenState.setProverInformation(proverInformation);
      state.addTokenState(tokenState);

    }

    boolean ok = true;
    for (ZkModuleVerifier zkm : childZkModules) {
      ok &= zkm.verify(zkVerifier);
    }

    return ok;
  }

  private KnownOrderGroupElement computeBlindedGamma(KnownOrderGroup group) throws ConfigurationException {
    // gamma = g0 * PROD_i( g_i^{x_i} ) * g_r^{x_r} * g_t^{x_t} * g^randomizer
    final KnownOrderGroupElement g0 = group.valueOfNoCheck(pk.getG0());
    KnownOrderGroupElement gamma = g0;

    for (int i = 0; i < numberOfAttributes; ++i) {
      if (isIssuerSetAttribute(i)) {
        final KnownOrderGroupElement gi = group.valueOfNoCheck(pk.getGI(i + 1));
        gamma = gamma.opMultOp(gi, revealedAttributes.get(i));
      }
    }
    if(carryOverState != null) {
      // all attributes that are not issuer set are in gamma
      gamma = gamma.op((KnownOrderGroupElement) carryOverState.getCommitment(group));
    }

    final KnownOrderGroupElement gt = group.valueOfNoCheck(pk.getGT());
    final BigInt xt =
        BrandsSignatureHelper.computeXt(sp, pk, credentialSpecificationId, numberOfAttributes,
            onDevice, testVector);
    gamma = gamma.opMultOp(gt, xt);

    return gamma;
  }

  @Override
  public List<BigInt> recoverAttributes() {
    return Collections.unmodifiableList(revealedAttributes);
  }

  @Override
  public IssuanceStateRecipient recoverIssuanceState() {
    return state;
  }


  public static IssuanceExtraMessage extraRound(final @Nullable IssuanceExtraMessage messageFromIssuer,
                                                final BrandsIssuanceStateRecipient stateRecipient, final BigIntFactory bf) {
    if (messageFromIssuer != null) {
      throw new RuntimeException("UProve needs only 1 extra issuance round.");
    }
    final List<BigInt> sigmaC = new ArrayList<BigInt>();
    for (final TokenState token : stateRecipient.getTokenStates()) {
      sigmaC.add(token.getSigmaC(bf));
    }
    return new IssuanceExtraMessageWrapper(sigmaC).getDelegatee();
  }

  public static ListOfSignaturesAndAttributes extractSignature(
      final IssuanceExtraMessage messageFromIssuer, final BrandsIssuanceStateRecipient stateRecipient,
      final TestVectorHelper testVector, final GroupFactory gf, final BigIntFactory bf) {
    final IssuanceExtraMessageWrapper issuanceExtraMessageWrapper =
        new IssuanceExtraMessageWrapper(messageFromIssuer);
    final List<BigInt> sigmaRList = issuanceExtraMessageWrapper.getParameterListElements();
    if (sigmaRList.size() != stateRecipient.getTokenStates().size()) {
      throw new RuntimeException(
          "UProve issuance: size of issuance state and message from issuer are different.");
    }
    final KnownOrderGroup group = gf.createPrimeOrderGroup(stateRecipient.getModulus(bf), stateRecipient.getQ(bf));
    final Signature signature = new Signature();
    signature.setCanReuseToken(false);
    for (int i = 0; i < stateRecipient.getTokenStates().size(); ++i) {
      final TokenState tokenState = stateRecipient.getTokenStates().get(i);
      final BigInt sigmaR = sigmaRList.get(i);
      testVector.checkValue(sigmaR, "sigmaR");

      final BigInt q = stateRecipient.getQ(bf);
      final BigInt sigmaRPrime = sigmaR.add(tokenState.getBeta2(bf)).mod(q);

      testVector.checkValue(sigmaRPrime, "sigmaRPrime");

      // Check UProve token
      final KnownOrderGroupElement g = stateRecipient.getG(group);
      final KnownOrderGroupElement g0 = stateRecipient.getG0(group);
      final KnownOrderGroupElement h = tokenState.getH(group);
      final KnownOrderGroupElement sigmaAPrime = tokenState.getSigmaAPrime(group);
      final KnownOrderGroupElement sigmaBPrime = tokenState.getSigmaBPrime(group);
      final KnownOrderGroupElement sigmaZPrime = tokenState.getSigmaZPrime(group);
      final BigInt sigmaCPrime = tokenState.getSigmaCPrime(bf);
      // sigmaA' * sigmaB' == (g * h)^{sigmaR'} * (g0 * sigmaZ')^{- sigmaC'}

      final KnownOrderGroupElement lhs = sigmaAPrime.op(sigmaBPrime);
      final KnownOrderGroupElement gh = g.op(h);
      final KnownOrderGroupElement g0sz = g0.op(sigmaZPrime);
      final KnownOrderGroupElement rhs = gh.multOp(sigmaRPrime).opMultOp(g0sz, sigmaCPrime.negate());
      if (!lhs.equals(rhs)) {
        System.err.println("LHS = " + lhs);
        System.err.println("RHS = " + rhs);
        throw new ArithmeticException("Failed to get proper UProve token from issuer. Token no. "
            + i);
      }

      final SignatureToken signatureToken = new SignatureToken();
      signature.getSignatureToken().add(signatureToken);
      final BrandsSignatureTokenWrapper st = new BrandsSignatureTokenWrapper(signatureToken);

      st.setSigmaZPrime(sigmaZPrime.toBigInt());
      st.setSigmaCPrime(tokenState.getSigmaCPrime(bf));
      st.setSigmaRPrime(sigmaRPrime);

      st.setH(h.toBigInt());
      st.setAlphaInverse(tokenState.getAlphaInverse(bf));
      if(tokenState.getProverInformation() != null) {
        st.setProverInformation(bf.unsignedValueOf(tokenState.getProverInformation()));
      }
    }
    return new ListOfSignaturesAndAttributes(signature, stateRecipient.getAttributes(bf));
  }

}
