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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.ibm.zurich.idmx.annotations.Nullable;
import com.ibm.zurich.idmx.buildingBlock.factory.BuildingBlockFactory;
import com.ibm.zurich.idmx.buildingBlock.helper.BaseForRepresentation;
import com.ibm.zurich.idmx.buildingBlock.helper.representation.damgardFujisaki.DamgardFujisakiRepresentationBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.systemParameters.EcryptSystemParametersWrapper;
import com.ibm.zurich.idmx.configuration.Configuration;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.ProofException;
import com.ibm.zurich.idmx.interfaces.buildingBlock.structural.linearCombination.Term;
import com.ibm.zurich.idmx.interfaces.device.ExternalSecretsManager;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.RandomGeneration;
import com.ibm.zurich.idmx.interfaces.util.group.GroupFactory;
import com.ibm.zurich.idmx.interfaces.util.group.HiddenOrderGroup;
import com.ibm.zurich.idmx.interfaces.util.group.HiddenOrderGroupElement;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProver;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProverCommitment;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ResidueClass;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateCollect;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateFirstRound;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateInitialize;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateSecondRound;
import com.ibm.zurich.idmx.zkModule.ZkModuleImpl;

import eu.abc4trust.xml.PublicKey;
import eu.abc4trust.xml.SignatureToken;
import eu.abc4trust.xml.SystemParameters;


class ProverModulePresentation extends ZkModuleImpl implements ZkModuleProver {

  private final EcryptSystemParametersWrapper spWrapper;
  private final ClPublicKeyWrapper pkWrapper;
  private final ClSignatureTokenWrapper tokenWrapper;

  private final List<BigInt> attributes;
  private final BigInt credentialSpecificationId;
  private final @Nullable
  URI identifierOfSecret;
  private final @Nullable
  URI identifierOfSignatureForSecret;
  @SuppressWarnings("unused")
  private ExternalSecretsManager esManager;
  // Communication between rounds
  private BigInt e;
  private BigInt vPrime;
  @SuppressWarnings("unused")
  private final BuildingBlockFactory bbFactory;
  private final BigIntFactory bigIntFactory;
  private final SystemParameters sp;
  @SuppressWarnings("rawtypes")
  private ZkModuleProverCommitment df;
  private final HiddenOrderGroup group;
  private int l_n;
  private int l_e;
  @SuppressWarnings("unused")
  private int lPrime_e;
  @SuppressWarnings("unused")
  private int l_v;
  @SuppressWarnings("unused")
  private int l_H;
  private int l_stat;
  private HiddenOrderGroupElement APrime;
  private boolean usesDevice;
  List<BaseForRepresentation> bases;

  public ProverModulePresentation(ClSignatureBuildingBlock parent, String identifierOfModule,
      SystemParameters systemParameters, PublicKey issuerPublicKey, SignatureToken signatureToken,
      List<BigInt> encodedAttributes, BigInt credentialSpecificationId, URI deviceId, String username,
      URI identifierOfSignatureForSecret, BigIntFactory bigIntFactory, GroupFactory groupFactory,
      RandomGeneration randomGeneration, ExternalSecretsManager esManager,
      BuildingBlockFactory bbFactory) throws ConfigurationException, ProofException {

    super(parent, identifierOfModule);

    this.usesDevice = (deviceId != null);
    if (usesDevice) {
      esManager.newProofSpec(username).addCredentialProof(deviceId, identifierOfSignatureForSecret);
    }

    this.sp = systemParameters;
    this.bbFactory = bbFactory;
    this.esManager = esManager;
    this.bigIntFactory = bigIntFactory;

    this.spWrapper = new EcryptSystemParametersWrapper(systemParameters);
    this.pkWrapper = new ClPublicKeyWrapper(issuerPublicKey);
    this.tokenWrapper = new ClSignatureTokenWrapper(signatureToken);

    this.attributes = encodedAttributes;
    this.credentialSpecificationId = credentialSpecificationId;
    this.identifierOfSecret = deviceId;
    this.identifierOfSignatureForSecret = identifierOfSignatureForSecret;

    // setup DF-buildingblock proof goal is:
    // Z =A'^e R_0^x \prod R_i^m_i R_s^credSpecId S^{v'+v_x}, where
    // * (e,A,v) is the signature, and (e,A'=A*S^a_r,v'=v+e*a_r) is a re-randomization
    // * x is the device secret key, and v_x the credential secret key
    // * v' is the external randomizer offset
    this.group = groupFactory.createSignedQuadraticResiduesGroup(pkWrapper.getModulus());

    // Load relevant pk/sp elements
    this.l_n = spWrapper.getDHModulusLength();
    this.l_e = spWrapper.getL_e();
    this.lPrime_e = spWrapper.getLPrime_e();
    this.l_v = spWrapper.getL_v();
    this.l_H = spWrapper.getHashLength();
    this.l_stat = spWrapper.getStatisticalInd();
    HiddenOrderGroupElement Z = group.valueOf(pkWrapper.getZ());

    // compute A'
    HiddenOrderGroupElement S = group.valueOf(pkWrapper.getS());
    HiddenOrderGroupElement A = group.valueOf(tokenWrapper.getA());
    BigInt r_a = randomGeneration.generateRandomNumber(l_n + l_stat);
    this.APrime = A.opMultOp(S, r_a.negate());

    // get e
    this.e = tokenWrapper.getE();

    // compute v'
    BigInt v = tokenWrapper.getV();
    this.vPrime = v.add(e.multiply(r_a));

    // bases are: (A', R_0, (R_i), R_s, S)
    this.bases = new ArrayList<BaseForRepresentation>();
    bases.add(BaseForRepresentation.managedAttribute(APrime));
    HiddenOrderGroupElement R0 = group.valueOf(pkWrapper.getRd());

    if (usesDevice) {
      bases.add(BaseForRepresentation.deviceSecret(R0));
    }

    for (int i = 0; i < attributes.size(); i++) {
      HiddenOrderGroupElement R_i = group.valueOfNoCheck(pkWrapper.getBase(i));
      bases.add(BaseForRepresentation.managedAttribute(R_i));
    }
    HiddenOrderGroupElement Rs = group.valueOf(pkWrapper.getRt());
    bases.add(BaseForRepresentation.managedAttribute(Rs));
    if (usesDevice) {
      bases.add(BaseForRepresentation.deviceRandomizer(S, vPrime));
    } else {
      bases.add(BaseForRepresentation.managedAttribute(S));
    }

    if (Configuration.reComputeSignature() && !usesDevice) {

      HiddenOrderGroupElement Q =
          (HiddenOrderGroupElement) ClHelper.computeQ(groupFactory, pkWrapper,
              group.neutralElement(), encodedAttributes, credentialSpecificationId, v);
      HiddenOrderGroupElement reComputedQ = A.multOp(e);
      if (!Q.equals(reComputedQ)) {
        throw new RuntimeException("Signature is not valid");
      }

      HiddenOrderGroupElement QTilde =
          (HiddenOrderGroupElement) ClHelper.computeQ(groupFactory, pkWrapper,
              group.neutralElement(), encodedAttributes, credentialSpecificationId, vPrime);
      HiddenOrderGroupElement reComputedQTilde = APrime.multOp(e);
      if (!QTilde.equals(reComputedQTilde)) {
        throw new RuntimeException("Randomised signature is not valid");
      }
    }

    DamgardFujisakiRepresentationBuildingBlock dfBB =
        bbFactory.getBuildingBlockByClass(DamgardFujisakiRepresentationBuildingBlock.class);
    this.df = dfBB.getZkModuleProver(sp, // systemParameters
        identifierOfModule + ":rep", // identifierOfModule
        identifierOfSignatureForSecret,// identifierOfCredentialForSecret
        bases,// bases
        group, // group
        Z,// commitment = left hand side of proof goal
        deviceId, // deviceUid
        username,
        null); // scope
  }

  @Override
  public void initializeModule(ZkProofStateInitialize zkBuilder) throws ConfigurationException {
    zkBuilder.markAsSignatureBuildingBlock();
    
    // secrets are (e, x, (m_i), credSpecId, v'+v_x)
    
    // The following is a range proof that e is larger than 2^(l_e-2)
    // (This is an "inexact range proof": the proof always goes
    // through if eprime is between 0 and 2^lPrime_e; if eprime is outside the range, the
    // probability of success decreases, until it is negligible when e < 2^(l_e-2))
    BigInt twoToLe = bigIntFactory.one().shiftLeft(l_e - 1);
    String attributeNameForE = df.identifierOfAttribute(0);
    zkBuilder.registerAttribute(attributeNameForEPrime(), false, lPrime_e);
    zkBuilder.registerAttribute(attributeNameForE, false);
    zkBuilder.setValueOfAttribute(attributeNameForE, e, null);
    zkBuilder.setValueOfAttribute(attributeNameForEPrime(), e.subtract(twoToLe), null);
    
    // The linear combination ensures that the proof engine hides only the first lPrime_e
    // bits of e with the R-value. This forces the R-value to have less bits than e, so that
    // our inexact range proof can be used.
    zkBuilder.attributeLinearCombination(attributeNameForE, twoToLe, Collections.singletonList(new Term(attributeNameForEPrime(), bigIntFactory.one())));


    int attrBeforeMessages = 1;
    if (usesDevice) {
      zkBuilder.registerAttribute(df.identifierOfAttribute(1), true);
      zkBuilder.registerAttribute(identifierOfSecretAttribute(), true);
      zkBuilder.attributesAreEqual(df.identifierOfAttribute(1), identifierOfSecretAttribute());
      attrBeforeMessages++;
    }

    for (int i = attrBeforeMessages; i < attributes.size() + attrBeforeMessages; i++) {
      zkBuilder.registerAttribute(df.identifierOfAttribute(i), false,
          spWrapper.getAttributeLength());
      zkBuilder.registerAttribute(identifierOfAttribute(i - attrBeforeMessages), false,
          spWrapper.getAttributeLength());
      zkBuilder.setValueOfAttribute(identifierOfAttribute(i - attrBeforeMessages),
          attributes.get(i - attrBeforeMessages), ResidueClass.INTEGER_IN_RANGE);
      zkBuilder.attributesAreEqual(identifierOfAttribute(i - attrBeforeMessages),
          df.identifierOfAttribute(i));
    }

    zkBuilder.registerAttribute(df.identifierOfAttribute(attributes.size() + attrBeforeMessages),
        false);
    zkBuilder.setValueOfAttribute(df.identifierOfAttribute(attributes.size() + attrBeforeMessages),
        credentialSpecificationId, null);

    if (usesDevice) {
      zkBuilder.registerAttribute(
          df.identifierOfAttribute(attributes.size() + attrBeforeMessages + 1), true, l_n + l_stat
              + l_e + 2);
    } else {
      zkBuilder.registerAttribute(
          df.identifierOfAttribute(attributes.size() + attrBeforeMessages + 1), false, l_n + l_stat
              + l_e + 2);
      zkBuilder.setValueOfAttribute(
          df.identifierOfAttribute(attributes.size() + attrBeforeMessages + 1), vPrime, null);
    }

    df.initializeModule(zkBuilder);
  }

  @Override
  public void collectAttributesForProof(ZkProofStateCollect zkBuilder) throws ConfigurationException {
    df.collectAttributesForProof(zkBuilder);
  }

  @Override
  public void firstRound(ZkProofStateFirstRound zkBuilder) throws ConfigurationException,
      ProofException {
    df.firstRound(zkBuilder);
    zkBuilder.addDValue(identifierOfModule + ":rep:base:0", APrime);
    zkBuilder.addNValue(identifierOfModule + ":credSpecId", credentialSpecificationId);
  }

  @Override
  public void secondRound(ZkProofStateSecondRound zkBuilder) throws ConfigurationException {
    df.secondRound(zkBuilder);
  }

  private final String attributeNameForEPrime() {
    return getIdentifier() + ":eprime";
  }

}
