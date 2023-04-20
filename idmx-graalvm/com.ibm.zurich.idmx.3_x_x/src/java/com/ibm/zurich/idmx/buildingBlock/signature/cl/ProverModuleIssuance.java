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

import java.math.BigInteger;
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
import com.ibm.zurich.idmx.interfaces.util.group.GroupFactory;
import com.ibm.zurich.idmx.interfaces.util.group.HiddenOrderGroup;
import com.ibm.zurich.idmx.interfaces.util.group.HiddenOrderGroupElement;
import com.ibm.zurich.idmx.interfaces.util.group.PaillierGroup;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProver;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProverIssuance;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ResidueClass;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateCollect;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateFirstRound;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateInitialize;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateSecondRound;
import com.ibm.zurich.idmx.zkModule.ZkModuleImpl;

import eu.abc4trust.xml.PrivateKey;
import eu.abc4trust.xml.PublicKey;
import eu.abc4trust.xml.SystemParameters;

/**
 * ZkModuleProver for CL issuance.
 */
class ProverModuleIssuance extends ZkModuleImpl implements ZkModuleProverIssuance {

  private final GroupFactory groupFactory;
  private final BigIntFactory bigIntFactory;
  private final RandomGeneration randomGeneration;

  private final EcryptSystemParametersWrapper sp;
  private final ClPublicKeyWrapper pk;
  private final ClSecretKeyWrapper sk;

  private final BigInt credentialSpecificationId;
  @SuppressWarnings("unused")
  private final boolean externalSecret;
  private final List</* Nullable */BigInt> issuerSpecifiedAttributes;
  private final @Nullable
  CarryOverStateIssuer carryOverState;

  private final List<ZkModuleProver> childZkModules;

  private BigInt pPrime_qPrime;
  private BigInt eInverse;
  private BigInt r_A;


  public ProverModuleIssuance(ClSignatureBuildingBlock parent, String identifierOfModule,
      SystemParameters systemParameters, PublicKey issuerPublicKey, PrivateKey issuerSecretKey,
      BigInt credentialSpecificationId, boolean externalSecret,
      List</* Nullable */BigInt> issuerSpecifiedAttributes,
      @Nullable CarryOverStateIssuer carryOverState,
      SystemParametersBuildingBlock systemParametersBuildingBlock,
      IssuerPublicKeyBuildingBlock issuerParametersBuildingBlock, GroupFactory groupFactory,
      BigIntFactory bigIntFactory, RandomGeneration randomGeneration) {
    super(parent, identifierOfModule);

    this.sp = new EcryptSystemParametersWrapper(systemParameters);
    this.pk = new ClPublicKeyWrapper(issuerPublicKey);
    this.sk = new ClSecretKeyWrapper(issuerSecretKey);

    this.credentialSpecificationId = credentialSpecificationId;
    this.externalSecret = externalSecret;
    this.issuerSpecifiedAttributes = issuerSpecifiedAttributes;
    this.carryOverState = carryOverState;

    this.groupFactory = groupFactory;
    this.bigIntFactory = bigIntFactory;
    this.randomGeneration = randomGeneration;

    this.childZkModules = new ArrayList<ZkModuleProver>();
    ZkModuleProver sp =
        systemParametersBuildingBlock.getZkModuleProver(identifierOfModule + ":sp",
            systemParameters);
    childZkModules.add(sp);
    ZkModuleProver ip =
        issuerParametersBuildingBlock.getZkModuleProver(identifierOfModule + ":ip",
            systemParameters, issuerPublicKey);
    childZkModules.add(ip);

    if (carryOverState != null) {
      if (carryOverState.getNumberOfAttributes() != issuerSpecifiedAttributes.size()) {
        throw new RuntimeException(
            "CarryOverState and issuerSpecifiedAttribute have different size in CL issuance.");
      }
    }
  }

  @Override
  public void initializeModule(ZkProofStateInitialize zkBuilder) throws ConfigurationException {
    for (int i = 0; i < issuerSpecifiedAttributes.size(); ++i) {
      if (isIssuerSetAttribute(i)) {
        String attributeName = identifierOfAttribute(i);
        zkBuilder.registerAttribute(attributeName, false, sp.getAttributeLength());
        zkBuilder.attributeIsRevealed(attributeName);
        BigInt value = issuerSpecifiedAttributes.get(i);
        if (value != null) {
          zkBuilder.setValueOfAttribute(attributeName, value, ResidueClass.INTEGER_IN_RANGE);
        } else {
          // Value is set by another building block
          zkBuilder.requiresAttributeValue(attributeName);
        }
      }
    }

    for (ZkModuleProver zkm : childZkModules) {
      zkm.initializeModule(zkBuilder);
    }
  }


  @Override
  public void collectAttributesForProof(ZkProofStateCollect zkBuilder) throws ConfigurationException {
    for (ZkModuleProver zkm : childZkModules) {
      zkm.collectAttributesForProof(zkBuilder);
    }
  }

  @Override
  public void firstRound(ZkProofStateFirstRound zkBuilder) throws ConfigurationException,
      ProofException {

    // TODO (pbi): this could be provided by the parent building block
    // Recover attribute values from other building blocks
    for (int i = 0; i < issuerSpecifiedAttributes.size(); ++i) {
      if (isIssuerSetAttribute(i) && issuerSpecifiedAttributes.get(i) == null) {
        String attributeName = identifierOfAttribute(i);
        if (zkBuilder.isValueOfAttributeAvailable(attributeName)) {
          BigInt newValue = zkBuilder.getValueOfAttribute(attributeName);
          issuerSpecifiedAttributes.set(i, newValue);
        }
      }
    }

    // Retrieve the bit lengths from the system parameters wrapper
    int l_e = sp.getL_e();
    int lPrime_e = sp.getLPrime_e();
    int l_v = sp.getL_v();
    int primeProbability = sp.getPrimeProbability();

    // Compute the CL signature values
    BigInt twoToLe = bigIntFactory.one().shiftLeft(l_e - 1);
    BigInt e = null;
    do {
      e = randomGeneration.generateRandomNumber(lPrime_e - 1).add(twoToLe);
    } while (!e.isProbablePrime(primeProbability));

    BigInt vTilde = randomGeneration.generateRandomNumber(l_v - 1);
    BigInt vPrimePrime = bigIntFactory.one().shiftLeft(l_v - 1).add(vTilde);

    final HiddenOrderGroup group = groupFactory.createSignedQuadraticResiduesGroup(pk.getModulus());
    final HiddenOrderGroupElement capU =
        ((carryOverState == null) ? (HiddenOrderGroupElement)group.neutralElement() : 
          (HiddenOrderGroupElement)carryOverState.getCommitment(group));
    final HiddenOrderGroupElement capQ =
        ClHelper.computeQ(groupFactory, pk, capU, issuerSpecifiedAttributes,
            credentialSpecificationId, vPrimePrime);

    // p = 2*p' + 1, q = 2*q' + 1
    final BigInt pPrime = sk.getSophieGermainPrimeP();
    final BigInt qPrime = sk.getSophieGermainPrimeQ();
    pPrime_qPrime = pPrime.multiply(qPrime);

    // FIXME: use appropriate group here!
    eInverse = e.modInverse(pPrime_qPrime);
    /**
     * Due to a bug in the IBM JVM, we serialize and de-serialize eInverse here. I couldn't find the
     * root cause. (enr)
     */
    eInverse = bigIntFactory.valueOf(new BigInteger(eInverse.toString()));

    final HiddenOrderGroupElement capA = capQ.multOp(eInverse);

    zkBuilder.addDValue(getIdentifier() + ":A", capA);
    zkBuilder.addDValue(getIdentifier() + ":e", e);
    zkBuilder.addDValue(getIdentifier() + ":vPrimePrime", vPrimePrime);
    zkBuilder.addNValue(getIdentifier() + ":Q", capQ);

    // FIXME: use appropriate group (as for eInverse)
    // Create the proof: SPK{(eInverse): A = Q.pow(eInverse)}
    final PaillierGroup pPrimeqPrimeGroup = groupFactory.createPaillierGroup(pPrime_qPrime);
    r_A = pPrimeqPrimeGroup.createRandomIterationcounter(randomGeneration, sp.getStatisticalInd());
    final HiddenOrderGroupElement capATilde = capQ.multOp(r_A);

    zkBuilder.addNValue(getIdentifier() + ":ATilde", capATilde);

    for (final ZkModuleProver zkm : childZkModules) {
      zkm.firstRound(zkBuilder);
    }
  }

  private boolean isIssuerSetAttribute(final int i) {
    if (carryOverState != null) {
      return carryOverState.isIssuerSetAttribute(i);
    } else {
      return (issuerSpecifiedAttributes.get(i) != null);
    }
  }

  @Override
  public void secondRound(final ZkProofStateSecondRound zkBuilder) throws ConfigurationException {

    final BigInt c = zkBuilder.getChallenge();

    // local s-value
    zkBuilder.addSValue(getIdentifier() + ":s_e",
        r_A.subtract(eInverse.multiply(c)).mod(pPrime_qPrime));

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
    return null;
  }

}
