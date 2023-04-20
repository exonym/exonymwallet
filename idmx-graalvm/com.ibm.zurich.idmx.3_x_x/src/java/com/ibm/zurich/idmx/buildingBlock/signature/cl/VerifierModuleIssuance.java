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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.ibm.zurich.idmx.annotations.Nullable;
import com.ibm.zurich.idmx.buildingBlock.structural.issuerKey.IssuerPublicKeyBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.systemParameters.SystemParametersBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.systemParameters.EcryptSystemParametersWrapper;
import com.ibm.zurich.idmx.configuration.ErrorMessages;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.ProofException;
import com.ibm.zurich.idmx.interfaces.device.ExternalSecretsManager;
import com.ibm.zurich.idmx.interfaces.messages.IssuanceMessageToIssuer;
import com.ibm.zurich.idmx.interfaces.signature.ListOfSignaturesAndAttributes;
import com.ibm.zurich.idmx.interfaces.state.CarryOverStateRecipient;
import com.ibm.zurich.idmx.interfaces.state.IssuanceStateRecipient;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.RandomGeneration;
import com.ibm.zurich.idmx.interfaces.util.group.Group;
import com.ibm.zurich.idmx.interfaces.util.group.GroupElement;
import com.ibm.zurich.idmx.interfaces.util.group.GroupFactory;
import com.ibm.zurich.idmx.interfaces.util.group.HiddenOrderGroupElement;
//import com.ibm.zurich.idmx.interfaces.util.group.HiddenOrderGroup;
//import com.ibm.zurich.idmx.interfaces.util.group.HiddenOrderGroupElement;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleVerifier;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleVerifierIssuance;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ResidueClass;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkVerifierStateCollect;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkVerifierStateVerify;
import com.ibm.zurich.idmx.util.NumberComparison;
import com.ibm.zurich.idmx.zkModule.ZkModuleImpl;

import eu.abc4trust.xml.PublicKey;
import eu.abc4trust.xml.Signature;
import eu.abc4trust.xml.SignatureToken;
import eu.abc4trust.xml.SystemParameters;

class VerifierModuleIssuance extends ZkModuleImpl implements ZkModuleVerifierIssuance {

  private final BigIntFactory bigIntFactory;
  private final GroupFactory groupFactory;
  @SuppressWarnings("unused")
  private final RandomGeneration randomGeneration;

  private final EcryptSystemParametersWrapper spWrapper;
  private final ClPublicKeyWrapper pk;
  private final @Nullable
  CarryOverStateRecipient carryOverStateRecipient;

  @SuppressWarnings("unused")
  private final boolean onDevice;
  private final BigInt credentialSpecificationId;
  private final int numberOfAttributes;

  private final List<ZkModuleVerifier> childZkModules;
  private final List<BigInt> attributeValues;
  private final List<Integer> unrevealedAttributes;
  private final ClIssuanceStateRecipient state;

  public VerifierModuleIssuance(final ClSignatureBuildingBlock parent, final SystemParameters systemParameters,
                                final PublicKey issuerPublicKey, final String identifierOfModule,
      final @Nullable CarryOverStateRecipient carryOverState, final BigInt credentialSpecificationId,
      final int numberOfAttributes, final boolean hasDevice, final SystemParametersBuildingBlock systemParamBB,
      final IssuerPublicKeyBuildingBlock issuerParamBB, final GroupFactory groupFactory,
      final BigIntFactory bigIntFactory, final RandomGeneration randomGeneration,
      final ExternalSecretsManager esManager) {

    super(parent, identifierOfModule);

    this.spWrapper = new EcryptSystemParametersWrapper(systemParameters);
    this.pk = new ClPublicKeyWrapper(issuerPublicKey);
    this.carryOverStateRecipient = carryOverState;
    this.credentialSpecificationId = credentialSpecificationId;
    this.onDevice = hasDevice;
    this.numberOfAttributes = numberOfAttributes;
    this.groupFactory = groupFactory;
    this.bigIntFactory = bigIntFactory;
    this.randomGeneration = randomGeneration;

    this.childZkModules = new ArrayList<ZkModuleVerifier>();
    final ZkModuleVerifier sp =
        systemParamBB.getZkModuleVerifier(identifierOfModule + ":sp", systemParameters);
    childZkModules.add(sp);
    final ZkModuleVerifier ip =
        issuerParamBB.getZkModuleVerifier(identifierOfModule + ":ip", systemParameters,
            issuerPublicKey);
    childZkModules.add(ip);

    this.attributeValues = new ArrayList<BigInt>();
    this.unrevealedAttributes = new ArrayList<Integer>();
    this.state = new ClIssuanceStateRecipient();
  }


  @Override
  public void collectAttributesForVerify(final ZkVerifierStateCollect zkVerifier) throws ProofException,
      ConfigurationException {
    for (int i = 0; i < numberOfAttributes; i++) {
      if (isIssuerSetAttribute(i)) {
        final String attributeName = identifierOfAttribute(i);
        zkVerifier.registerAttribute(attributeName, false);
        zkVerifier.attributeIsRevealed(attributeName);
        zkVerifier.setResidueClass(attributeName, ResidueClass.INTEGER_IN_RANGE);
      }
    }

    for (final ZkModuleVerifier zkm : childZkModules) {
      zkm.collectAttributesForVerify(zkVerifier);
    }
  }

  private boolean isIssuerSetAttribute(final int i) {
    if (carryOverStateRecipient == null) {
      return true;
    } else {
      return (carryOverStateRecipient.getAttributeValues(bigIntFactory).get(i) == null);
    }
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  @Override
  public boolean verify(final ZkVerifierStateVerify zkVerifier) throws ConfigurationException,
      ProofException {

    final List<BigInt> issuerSetAttributes = new ArrayList<BigInt>();
    // Recover attribute values
    attributeValues.clear();
    for (int i = 0; i < numberOfAttributes; ++i) {
      final BigInt newValue;
      if (isIssuerSetAttribute(i)) {
        final String attributeName = identifierOfAttribute(i);
        if (zkVerifier.isRevealedAttribute(attributeName)) {
          newValue = zkVerifier.getValueOfRevealedAttribute(attributeName);
        } else {
          throw new ConfigurationException("Attribute " + i + " is not revealed.");
        }
        issuerSetAttributes.add(newValue);
      } else {
        newValue = carryOverStateRecipient.getAttributeValues(bigIntFactory).get(i);
        unrevealedAttributes.add(i);
        issuerSetAttributes.add(null);
      }
      attributeValues.add(newValue);
    }

    // Verify proof
    final Group group = groupFactory.createSignedQuadraticResiduesGroup(pk.getModulus());

    // Retrieve elements from ZK verifier
    final BigInt challenge = zkVerifier.getChallenge();
    final GroupElement<?,?,?> capA = zkVerifier.getDValueAsGroupElement(getIdentifier() + ":A", group);
    final BigInt e = zkVerifier.getDValueAsInteger(getIdentifier() + ":e");
    final BigInt vPrimePrime = zkVerifier.getDValueAsInteger(getIdentifier() + ":vPrimePrime");
    final BigInt s_e = zkVerifier.getSValueAsInteger(getIdentifier() + ":s_e");

    // We use the issuer provided v'' in case of simple issuance
    final BigInt vPrime =
        (carryOverStateRecipient == null) ? bigIntFactory.zero() : carryOverStateRecipient
            .getOpening(bigIntFactory);
    GroupElement commitment = null;
    if (carryOverStateRecipient != null) {
      commitment = carryOverStateRecipient.getCommitment(group);
    }
    commitment = (commitment == null) ? group.neutralElement() : commitment;

    final BigInt v = vPrime.add(vPrimePrime);

    // Re-compute and verify Q
    final HiddenOrderGroupElement capQ =
        ClHelper.computeQ(groupFactory, pk, (HiddenOrderGroupElement)commitment, issuerSetAttributes,
            credentialSpecificationId, vPrimePrime);
    zkVerifier.checkNValue(getIdentifier() + ":Q", capQ);
    final GroupElement capQHat = capA.multOp(e);
    if (!capQ.equals(capQHat)) {
      return false;
    }

    // Re-compute and verify ATilde
    final GroupElement capAHat = capA.multOp(challenge.add(s_e.multiply(e)));

    zkVerifier.checkNValue(getIdentifier() + ":ATilde", capAHat);

    // Verify e
    final int l_e = spWrapper.getL_e();
    final int lPrime_e = spWrapper.getLPrime_e();
    final int primeProbability = spWrapper.getPrimeProbability();
    checkE(e, l_e, lPrime_e, primeProbability);

    // Add CL signature to state
    state.setA(capA);
    state.setE(e);
    state.setV(v);
    state.setAttributes(attributeValues);

    boolean success = true;
    for (final ZkModuleVerifier zkm : childZkModules) {
      success &= zkm.verify(zkVerifier);
    }

    return success;
  }

  @Override
  public List<BigInt> recoverAttributes() {
    return Collections.unmodifiableList(attributeValues);
  }

  @Override
  public IssuanceStateRecipient recoverIssuanceState() throws ConfigurationException {
    return state;
  }

  public static IssuanceMessageToIssuer extraRound() {
    return null;
  }

  public static ListOfSignaturesAndAttributes extractSignature(
      final ClIssuanceStateRecipient stateRecipient, final BigIntFactory bigIntFactory)
      throws ConfigurationException {

    final Signature signature = new Signature();
    signature.setCanReuseToken(true);

    final SignatureToken signatureToken = new SignatureToken();
    signature.getSignatureToken().add(signatureToken);
    final ClSignatureTokenWrapper st = new ClSignatureTokenWrapper(signatureToken);
    st.setA(stateRecipient.getA(bigIntFactory));
    st.setE(stateRecipient.getE(bigIntFactory));
    st.setV(stateRecipient.getV(bigIntFactory));

    return new ListOfSignaturesAndAttributes(signature, stateRecipient.getAttributes(bigIntFactory));
  }

  // @SuppressWarnings("rawtypes")
  // private GroupElement computeQ(BigInt v, List<BigInt> attributeValues)
  // throws ConfigurationException {
  //
  // Group group = groupFactory.createSignedQuadraticResiduesGroup(pk.getModulus());
  //
  // GroupElement capS = group.valueOf(pk.getS());
  // GroupElement capZ = group.valueOf(pk.getZ());
  //
  // GroupElement capQ = group.neutralElement();
  // GroupElement capR;
  // for (int i = 0; i < attributeValues.size(); i++) {
  // capR = group.valueOf(pk.getBase(i));
  // capQ = capQ.opMultOp(capR, attributeValues.get(i));
  // }
  // capQ = capQ.opMultOp(capS, v);
  // capQ = capQ.invert();
  // capQ = capZ.op(capQ);
  //
  // return capQ;
  // }

  private static void checkE(final BigInt e, final int l_e, final int lPrime_e, final int primeProbability) {

    // check whether e is a prime number
    if (!e.isProbablePrime(primeProbability)) {
      throw new RuntimeException(ErrorMessages.parameterWrong("e is not prime."));
    }

    final BigIntFactory bigIntFactory = e.getFactory();
    final BigInt one = bigIntFactory.one();

    // check length of e
    final BigInt lower = one.shiftLeft(l_e - 1);
    final BigInt upper = lower.add(one.shiftLeft(lPrime_e - 1));
    if (!NumberComparison.isInInterval(e, lower, upper)) {
      throw new RuntimeException(ErrorMessages.parameterWrong("e is not of the expected length."));
    }
  }



}
