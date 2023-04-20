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
import com.ibm.zurich.idmx.buildingBlock.structural.issuerKey.IssuerPublicKeyBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.systemParameters.SystemParametersBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.systemParameters.EcryptSystemParametersWrapper;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.ProofException;
import com.ibm.zurich.idmx.interfaces.state.CarryOverStateIssuer;
import com.ibm.zurich.idmx.interfaces.state.IssuanceStateIssuer;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.RandomGeneration;
import com.ibm.zurich.idmx.interfaces.util.TestVectorHelper;
import com.ibm.zurich.idmx.interfaces.util.group.GroupFactory;
import com.ibm.zurich.idmx.interfaces.util.group.KnownOrderGroup;
import com.ibm.zurich.idmx.interfaces.util.group.KnownOrderGroupElement;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProver;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProverIssuance;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ResidueClass;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateCollect;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateFirstRound;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateInitialize;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateSecondRound;
import com.ibm.zurich.idmx.jaxb.wrapper.IssuanceExtraMessageWrapper;
import com.ibm.zurich.idmx.zkModule.ZkModuleImpl;

import eu.abc4trust.xml.IssuanceExtraMessage;
import eu.abc4trust.xml.PrivateKey;
import eu.abc4trust.xml.PublicKey;
import eu.abc4trust.xml.SystemParameters;

/**
 * ZkModuleProver for issuance of Brands signatures. Note that it doesn't actually need to perform a proof
 * during issuance, so this ZkModule is actually somewhat degenerate.
 */
class ProverModuleIssuance extends ZkModuleImpl implements ZkModuleProverIssuance {

  private final GroupFactory groupFactory;
  private final RandomGeneration randomGeneration;
  private final TestVectorHelper testVector;

  private final EcryptSystemParametersWrapper sp;
  private final BrandsPublicKeyWrapper pk;
  private final BrandsSecretKeyWrapper sk;
  private final BigInt credentialSpecificationId;
  private final boolean externalSecret;
  private final List</* Nullable */BigInt> issuerSpecifiedAttributes;
  private final @Nullable
  CarryOverStateIssuer carryOverState;

  private final List<ZkModuleProver> childZkModules;
  private final List<BigInt> listOfW;


  public ProverModuleIssuance(final BrandsSignatureBuildingBlock parent,
                              final SystemParameters systemParameters, final PublicKey issuerPublicKey, final PrivateKey issuerSecretKey,
      final String identifierOfModule, final BigInt credentialSpecificationId, final boolean externalSecret,
      final List</* Nullable */BigInt> issuerSpecifiedAttributes,
      final @Nullable CarryOverStateIssuer carryOverState, final SystemParametersBuildingBlock systemParamBB,
      final IssuerPublicKeyBuildingBlock issuerParamBB, final GroupFactory groupFactory,
      final RandomGeneration randomGeneration, final TestVectorHelper testVector) {

    super(parent, identifierOfModule);

    this.sp = new EcryptSystemParametersWrapper(systemParameters);
    this.pk = new BrandsPublicKeyWrapper(issuerPublicKey);
    this.sk = new BrandsSecretKeyWrapper(issuerSecretKey);

    this.credentialSpecificationId = credentialSpecificationId;
    this.externalSecret = externalSecret;
    this.issuerSpecifiedAttributes = issuerSpecifiedAttributes;
    this.carryOverState = carryOverState;
    this.testVector = testVector;

    this.groupFactory = groupFactory;
    this.randomGeneration = randomGeneration;

    this.childZkModules = new ArrayList<ZkModuleProver>();
    final ZkModuleProver sp =
        systemParamBB.getZkModuleProver(identifierOfModule + ":sp", systemParameters);
    childZkModules.add(sp);
    final ZkModuleProver ip =
        issuerParamBB.getZkModuleProver(identifierOfModule + ":ip", systemParameters,
            issuerPublicKey);
    childZkModules.add(ip);

    this.listOfW = new ArrayList<BigInt>();

    if (carryOverState != null) {
      if (carryOverState.getNumberOfAttributes() != issuerSpecifiedAttributes.size()) {
        throw new RuntimeException(
            "CarryOverState and issuerSpecifiedAttribute have different size in Uprove issuance.");
      }
    }
  }



  @Override
  public void initializeModule(final ZkProofStateInitialize zkBuilder) throws ConfigurationException {
    for (int i = 0; i < issuerSpecifiedAttributes.size(); ++i) {
      if (isIssuerSetAttribute(i)) {
        final String attributeName = identifierOfAttribute(i);
        zkBuilder.registerAttribute(attributeName, false);
        zkBuilder.attributeIsRevealed(attributeName);
        final BigInt value = issuerSpecifiedAttributes.get(i);
        if (value != null) {
          zkBuilder.setValueOfAttribute(attributeName, value, ResidueClass.RESIDUE_CLASS_MOD_Q);
        } else {
          // Value is set by another building block
          zkBuilder.requiresAttributeValue(attributeName);  
        }
      }
    }

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
    // Recover attribute values from other building blocks
    for (int i = 0; i < issuerSpecifiedAttributes.size(); ++i) {
      if (isIssuerSetAttribute(i) && issuerSpecifiedAttributes.get(i) == null) {
        final String attributeName = identifierOfAttribute(i);
        if (zkBuilder.isValueOfAttributeAvailable(attributeName)) {
          final BigInt newValue = zkBuilder.getValueOfAttribute(attributeName);
          issuerSpecifiedAttributes.set(i, newValue);
        }
      }
    }

    final KnownOrderGroup group =
        groupFactory.createPrimeOrderGroup(sp.getDHModulus(), sp.getDHSubgroupOrder());
    final KnownOrderGroupElement gamma = computeGamma(group);
    zkBuilder.addNValue(getIdentifier() + ":gamma", gamma);
    testVector.checkValue(gamma.toBigInt(), "gamma");
    final KnownOrderGroupElement sigmaZ = gamma.multOp(sk.getY0());
    testVector.checkValue(sigmaZ.toBigInt(), "sigmaZ");
    zkBuilder.addDValue(getIdentifier() + ":sigma_z", sigmaZ);
    KnownOrderGroupElement g = group.valueOfNoCheck(sp.getDHGenerator1());

    final int numberOfTokens = pk.getNumberOfTokens();
    listOfW.clear();
    for (int i = 0; i < numberOfTokens; ++i) {
      BigInt w = group.createRandomIterationcounter(randomGeneration, sp.getStatisticalInd());
      if(testVector.isActive()) {
        w = testVector.getValueAsBigInt("w");
      }
      final KnownOrderGroupElement sigmaA = g.multOp(w);
      zkBuilder.addDValue(getIdentifier() + ":sigma_a:" + Integer.valueOf(i), sigmaA);
      testVector.checkValue(sigmaA.toBigInt(), "sigmaA");
      final KnownOrderGroupElement sigmaB = gamma.multOp(w);
      zkBuilder.addDValue(getIdentifier() + ":sigma_b:" + Integer.valueOf(i), sigmaB);
      testVector.checkValue(sigmaB.toBigInt(), "sigmaB");
      listOfW.add(w);
    }

    for (final ZkModuleProver zkm : childZkModules) {
      zkm.firstRound(zkBuilder);
    }
  }

  private KnownOrderGroupElement computeGamma(final KnownOrderGroup group) throws ConfigurationException {
    // gamma = g0 * PROD_i( g_i^{x_i} ) * g_r^{x_r} * g_t^{x_t}

    final KnownOrderGroupElement g0 = group.valueOfNoCheck(pk.getG0());
    KnownOrderGroupElement gamma = g0;

    final int numberOfAttributes = issuerSpecifiedAttributes.size();
    for (int i = 0; i < numberOfAttributes; ++i) {
      if (isIssuerSetAttribute(i)) {
        final BigInt xi = issuerSpecifiedAttributes.get(i);
        if (xi == null) {
          throw new RuntimeException("Cannot determine value of exponent No. " + i);
        }
        final KnownOrderGroupElement gi = group.valueOfNoCheck(pk.getGI(i + 1));
        gamma = gamma.opMultOp(gi, xi);
      } else {
        if (carryOverState == null) {
          throw new RuntimeException("Value of attribute No. " + i
              + " is not set by issuer, but no carry over state present.");
        }
        // The base is already contained in the commitment of the carry over state
      }
    }

    KnownOrderGroupElement gt = group.valueOfNoCheck(pk.getGT());
    final BigInt xt =
        BrandsSignatureHelper.computeXt(sp, pk, credentialSpecificationId, numberOfAttributes,
            externalSecret, testVector);
    testVector.checkValue(xt, "xt");
    gamma = gamma.opMultOp(gt, xt);

    if (carryOverState != null) {
      gamma = gamma.op((KnownOrderGroupElement) carryOverState.getCommitment(group));
    }

    return gamma;
  }


  private boolean isIssuerSetAttribute(int i) {
    if (carryOverState != null) {
      return carryOverState.isIssuerSetAttribute(i);
    } else {
      return (issuerSpecifiedAttributes.get(i) != null);
    }
  }


  @Override
  public void secondRound(final ZkProofStateSecondRound zkBuilder) throws ConfigurationException {
    for (final ZkModuleProver zkm : childZkModules) {
      zkm.secondRound(zkBuilder);
    }
  }


  @Override
  public List<BigInt> recoverEncodedAttributes() {
    return Collections.unmodifiableList(issuerSpecifiedAttributes);
  }


  @Override
  public IssuanceStateIssuer recoverIssuanceState() throws ConfigurationException {
    return new BrandsIssuanceStateIssuer(listOfW, sp.getDHSubgroupOrder(), sk.getY0());
  }



  public static IssuanceExtraMessage extraRound(final IssuanceExtraMessage messageFromRecipient,
                                                final BrandsIssuanceStateIssuer stateIssuer, final BigIntFactory bf) {

    // Calling computeSigmaR() destroys the issuance state.
    final IssuanceExtraMessageWrapper issuanceExtraMessageWrapper =
        new IssuanceExtraMessageWrapper(messageFromRecipient);
    final List<BigInt> sigmaR = stateIssuer.computeSigmaR(issuanceExtraMessageWrapper.getParameterListElements(), bf);
    return new IssuanceExtraMessageWrapper(sigmaR).getDelegatee();
  }
}
