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
import java.util.List;

import com.ibm.zurich.idmx.annotations.Nullable;
import com.ibm.zurich.idmx.buildingBlock.factory.BuildingBlockFactory;
import com.ibm.zurich.idmx.buildingBlock.helper.BaseForRepresentation;
import com.ibm.zurich.idmx.buildingBlock.helper.representation.damgardFujisaki.DamgardFujisakiRepresentationBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.signature.SignatureBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.issuerKey.IssuerPublicKeyBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.systemParameters.SystemParametersBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.verifierParameters.VerifierParametersBuildingBlock;
// import com.ibm.zurich.idmx.buildingBlock.systemParameters.EcryptSystemParametersWrapper;
import com.ibm.zurich.idmx.configuration.ErrorMessages;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.ProofException;
import com.ibm.zurich.idmx.interfaces.device.ExternalSecretsManager;
import com.ibm.zurich.idmx.interfaces.state.CarryOverStateRecipient;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.RandomGeneration;
import com.ibm.zurich.idmx.interfaces.util.group.GroupFactory;
import com.ibm.zurich.idmx.interfaces.util.group.HiddenOrderGroup;
import com.ibm.zurich.idmx.interfaces.util.group.HiddenOrderGroupElement;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProver;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProverCarryOver;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProverCommitment;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateCollect;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateFirstRound;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateInitialize;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateSecondRound;
import com.ibm.zurich.idmx.zkModule.ZkModuleImpl;

import eu.abc4trust.xml.PublicKey;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.VerifierParameters;

class ProverModuleCarryOver extends ZkModuleImpl implements ZkModuleProverCarryOver {

  private final List<Boolean> carryAttributeOver;
  private final List<BigInt> newCredentialAttributes;
  private CarryOverStateRecipient carryOverStateRecipient;
  private final List<ZkModuleProver> childZkModules;
  private BigInt credentialSpecificationId;
  private ExternalSecretsManager esManager;
  private boolean hasExternalSecret;
  @SuppressWarnings("unused")
  private String username;

  public ProverModuleCarryOver(SignatureBuildingBlock parent, String identifierOfModule,
      SystemParameters systemParameters, VerifierParameters verifierParameters,
      PublicKey newIssuerPublicKey, @Nullable URI deviceId, String username,
      @Nullable URI identifierOfSignatureForSecret, List<Boolean> carryAttributeOver,
      List</* Nullable */BigInt> newCredentialAttributes, BigInt credentialSpecificationId,
      BuildingBlockFactory buildingBlockFactory, GroupFactory groupFactory,
      BigIntFactory bigIntFactory, RandomGeneration randomGeneration,
      ExternalSecretsManager externalSecretsManager) throws ProofException, ConfigurationException {

    super(parent, identifierOfModule);

    this.username = username;
    this.esManager = externalSecretsManager;
    this.credentialSpecificationId = credentialSpecificationId;
    ClPublicKeyWrapper pkWrapper = new ClPublicKeyWrapper(newIssuerPublicKey);

    this.carryAttributeOver = carryAttributeOver;
    this.newCredentialAttributes = newCredentialAttributes;

    // check if device is used, and register the corresponding proof
    this.hasExternalSecret = (deviceId != null);
    if ((deviceId == null) != (identifierOfSignatureForSecret == null)) {
      throw new ConfigurationException(
          "Device and signature must either both be specified or not in " + identifierOfModule);
    }
    if (hasExternalSecret) {
      esManager.newProofSpec(username).addCredentialProof(deviceId, identifierOfSignatureForSecret);
    }

    if (carryAttributeOver.size() != newCredentialAttributes.size()) {
      throw new ProofException(ErrorMessages.attributeNumberMismatch(this.getClass()
          .getSimpleName()));
    }

    HiddenOrderGroup group =
        groupFactory.createSignedQuadraticResiduesGroup(pkWrapper.getModulus());

    List<BaseForRepresentation> bases = new ArrayList<BaseForRepresentation>();
    for (int i = 0; i < newCredentialAttributes.size(); i++) {
      if (carryAttributeOver.get(i)) {
        bases.add(BaseForRepresentation.managedAttribute(group.valueOfNoCheck(pkWrapper
            .getBase(i))));
      }
    }
    HiddenOrderGroupElement S = group.valueOfNoCheck(pkWrapper.getS());

    // The bases for the DF building block are given by the bases corresponding to the carried
    // attributes, potentially the bases used by the device, plus a final base for randomising the
    // commitment
    // TODO: maybe rewrite and use externalRandomizerOffset
    if (hasExternalSecret) {
      HiddenOrderGroupElement R0 = group.valueOfNoCheck(pkWrapper.getRd());
      bases.add(BaseForRepresentation.deviceSecret(R0));
      bases.add(BaseForRepresentation.deviceRandomizer(S, bigIntFactory.zero()));
    }
    bases.add(BaseForRepresentation.randomAttribute(S));


    DamgardFujisakiRepresentationBuildingBlock damgardFujisakiBB =
        buildingBlockFactory
            .getBuildingBlockByClass(DamgardFujisakiRepresentationBuildingBlock.class);
    ZkModuleProver df = damgardFujisakiBB.getZkModuleProver(systemParameters, // systemParameters
        identifierOfModule + ":rep",// identifierOfModule
        identifierOfSignatureForSecret, // identifierOfCredentialForSecret,
        bases, // bases
        group, // group
        null, // commitment
        deviceId, // deviceUid
        username,
        null); // scope
    this.childZkModules = new ArrayList<ZkModuleProver>();
    childZkModules.add(df);

    // add SystemParameters as child building block such that they become part of the hash
    // contribution
    SystemParametersBuildingBlock spBB =
        buildingBlockFactory.getBuildingBlockByClass(SystemParametersBuildingBlock.class);
    ZkModuleProver spProver = spBB.getZkModuleProver(identifierOfModule + ":sp", systemParameters);
    childZkModules.add(spProver);

    // add VerifierParameters as child building block such that they become part of the hash
    // contribution
    VerifierParametersBuildingBlock vpBB =
        buildingBlockFactory.getBuildingBlockByClass(VerifierParametersBuildingBlock.class);
    ZkModuleProver vpProver =
        vpBB.getZkModuleProver(identifierOfModule + ":vp", systemParameters, verifierParameters);
    childZkModules.add(vpProver);

    // add NewIssuerPublicKey as child building block such that they become part of the hash
    // contribution
    IssuerPublicKeyBuildingBlock ipkBB =
        buildingBlockFactory.getBuildingBlockByClass(IssuerPublicKeyBuildingBlock.class);
    ZkModuleProver ipkProver =
        ipkBB.getZkModuleProver(identifierOfModule + ":ip", systemParameters, newIssuerPublicKey);
    childZkModules.add(ipkProver);
  }

  @Override
  public void initializeModule(ZkProofStateInitialize zkBuilder) throws ConfigurationException {
    for (ZkModuleProver zkModuleProver : childZkModules) {
      zkModuleProver.initializeModule(zkBuilder);
    }
    // The values contained in the computed commitment must be equal to those that are to be carried
    // over; j is the index of the corresponding base inside the DF block; if values are not yet
    // known, they are required
    int j = 0; // gives the index for the NEW identifier of the attribute
    for (int i = 0; i < newCredentialAttributes.size(); i++) {
      if (carryAttributeOver.get(i)) {
        zkBuilder.registerAttribute(identifierOfAttribute(i), false);
        BigInt attributeValue = newCredentialAttributes.get(i);
        if (attributeValue == null) {
          zkBuilder.requiresAttributeValue(identifierOfAttribute(i));
        } else {
          zkBuilder.setValueOfAttribute(identifierOfAttribute(i), attributeValue, null);
        }
        zkBuilder.attributesAreEqual(identifierOfAttribute(i), childZkModules.get(0)
            .identifierOfAttribute(j));
        j++;
      }
    }
    // in case a smartcard is present, the commitment needs to be bound to the same device secret
    // key
    if (hasExternalSecret) {
      zkBuilder.registerAttribute(identifierOfSecretAttribute(), true);
      zkBuilder.attributesAreEqual(identifierOfSecretAttribute(), childZkModules.get(0)
          .identifierOfAttribute(j));
    }
  }

  @Override
  public void collectAttributesForProof(ZkProofStateCollect zkBuilder) throws ConfigurationException {
    for (ZkModuleProver module : childZkModules) {
      module.collectAttributesForProof(zkBuilder);
    }
    for (int i = 0; i < newCredentialAttributes.size(); i++) {
      if (carryAttributeOver.get(i) && newCredentialAttributes.get(i) == null) {
        zkBuilder.getValueOfAttribute(identifierOfAttribute(i));
      }
    }
  }

  @Override
  public void firstRound(ZkProofStateFirstRound zkBuilder) throws ConfigurationException,
      ProofException {
    for (ZkModuleProver zkModuleProver : childZkModules) {
      zkModuleProver.firstRound(zkBuilder);
    }
    zkBuilder.addNValue(getBuildingBlockId() + ":credSpecId", credentialSpecificationId);
  }

  @Override
  public void secondRound(ZkProofStateSecondRound zkBuilder) throws ConfigurationException {
    for (ZkModuleProver zkModuleProver : childZkModules) {
      zkModuleProver.secondRound(zkBuilder);
    }
    @SuppressWarnings("unchecked")
    ZkModuleProverCommitment<HiddenOrderGroupElement> df = (ZkModuleProverCommitment<HiddenOrderGroupElement>) childZkModules.get(0);
    
    List<BigInt> carriedOverAttributes = new ArrayList<BigInt>(newCredentialAttributes);
    for (int i = 0; i < newCredentialAttributes.size(); i++) {
      if (carryAttributeOver.get(i)) {
        BigInt value = zkBuilder.getValueOfAttribute(identifierOfAttribute(i));
        carriedOverAttributes.set(i, value);   
      }
    }

    carryOverStateRecipient =
        new CarryOverStateRecipient(df.recoverCommitment(), df.recoverRandomizers().get(0),
          carriedOverAttributes);
  }

  @Override
  public CarryOverStateRecipient recoverState() {
    return carryOverStateRecipient;
  }

}
