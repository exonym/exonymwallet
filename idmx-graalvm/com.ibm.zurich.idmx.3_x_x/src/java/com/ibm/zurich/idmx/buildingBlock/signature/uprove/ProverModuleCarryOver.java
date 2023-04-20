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

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import com.ibm.zurich.idmx.annotations.Nullable;
import com.ibm.zurich.idmx.buildingBlock.factory.BuildingBlockFactory;
import com.ibm.zurich.idmx.buildingBlock.helper.BaseForRepresentation;
import com.ibm.zurich.idmx.buildingBlock.helper.representation.pedersen.PedersenRepresentationBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.issuerKey.IssuerPublicKeyBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.systemParameters.SystemParametersBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.verifierParameters.VerifierParametersBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.systemParameters.EcryptSystemParametersWrapper;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.ProofException;
import com.ibm.zurich.idmx.interfaces.device.ExternalSecretsManager;
import com.ibm.zurich.idmx.interfaces.state.CarryOverStateRecipient;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.RandomGeneration;
import com.ibm.zurich.idmx.interfaces.util.group.GroupFactory;
import com.ibm.zurich.idmx.interfaces.util.group.KnownOrderGroup;
import com.ibm.zurich.idmx.interfaces.util.group.KnownOrderGroupElement;
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
  //TODO(ksa) not immutable - refactor
  private CarryOverStateRecipient carryOverStateRecipient;
  private final List<ZkModuleProver> childZkModules;
  private final BigInt credentialSpecificationId;
  private final boolean isDevicePresent;
  @SuppressWarnings("unused")
  private final String username;

  public ProverModuleCarryOver(final BrandsSignatureBuildingBlock parent, final String identifierOfModule,
                               final SystemParameters systemParameters, final VerifierParameters verifierParameters,
      final PublicKey newIssuerPublicKey, final @Nullable URI identifierOfSecret, final String username,
      final @Nullable URI identifierOfSignatureForSecret, final List<Boolean> carryAttributeOver,
      final List</* Nullable */BigInt> newCredentialAttributes, final BigInt credentialSpecificationId,
      final ExternalSecretsManager esManager, final BuildingBlockFactory buildingBlockFactory,
      final GroupFactory groupFactory, final BigIntFactory bigIntFactory, final RandomGeneration randomGeneration)
      throws ConfigurationException, ProofException {
    super(parent, identifierOfModule);

    this.isDevicePresent = (identifierOfSecret != null);
    this.username = username;
    this.credentialSpecificationId = credentialSpecificationId;
    final BrandsPublicKeyWrapper pkWrapper = new BrandsPublicKeyWrapper(newIssuerPublicKey);
    final EcryptSystemParametersWrapper spWrapper = new EcryptSystemParametersWrapper(systemParameters);
    this.carryAttributeOver = carryAttributeOver;
    this.newCredentialAttributes = newCredentialAttributes;
    this.carryOverStateRecipient = null;
    final PedersenRepresentationBuildingBlock pedersenBB =
        buildingBlockFactory.getBuildingBlockByClass(PedersenRepresentationBuildingBlock.class);

    final KnownOrderGroup group =
        groupFactory
            .createPrimeOrderGroup(spWrapper.getDHModulus(), spWrapper.getDHSubgroupOrder());

    final List<BaseForRepresentation> bases = new ArrayList<BaseForRepresentation>();
    for (int i = 0; i < this.newCredentialAttributes.size(); i++) {
      if (this.carryAttributeOver.get(i)) {
        bases.add(BaseForRepresentation.managedAttribute(group.valueOfNoCheck(pkWrapper
            .getGI(i + 1))));
      }
    }
    final KnownOrderGroupElement g = group.valueOfNoCheck(spWrapper.getDHGenerator1());
    bases.add(BaseForRepresentation.randomAttribute(g));
    if(isDevicePresent) {
      final KnownOrderGroupElement gd = group.valueOfNoCheck(pkWrapper.getGD());
      bases.add(BaseForRepresentation.deviceSecret(gd));
    }

    final ZkModuleProverCommitment<?> pedersen = pedersenBB.getZkModuleProver(systemParameters, // systemParameters
        identifierOfModule + ":rep",// identifierOfModule
        identifierOfSignatureForSecret, // identifierOfCredentialForSecret,
        bases, // bases
        group, // group
        null, // commitment
        identifierOfSecret, // deviceUid
        username,
        null); // scope

    this.childZkModules = new ArrayList<ZkModuleProver>();
    childZkModules.add(pedersen);

    // add SystemParameters as child building block such that they become part of the hash
    // contribution
    final SystemParametersBuildingBlock spBB =
        buildingBlockFactory.getBuildingBlockByClass(SystemParametersBuildingBlock.class);
    final ZkModuleProver spProver = spBB.getZkModuleProver(identifierOfModule + ":sp", systemParameters);
    childZkModules.add(spProver);

    // add VerifierParameters as child building block such that they become part of the hash
    // contribution
    final VerifierParametersBuildingBlock vpBB =
        buildingBlockFactory.getBuildingBlockByClass(VerifierParametersBuildingBlock.class);
    final ZkModuleProver vpProver =
        vpBB.getZkModuleProver(identifierOfModule + ":vp", systemParameters, verifierParameters);
    childZkModules.add(vpProver);

    // add NewIssuerPublicKey as child building block such that they become part of the hash
    // contribution
    final IssuerPublicKeyBuildingBlock ipkBB =
        buildingBlockFactory.getBuildingBlockByClass(IssuerPublicKeyBuildingBlock.class);
    final ZkModuleProver ipkProver =
        ipkBB.getZkModuleProver(identifierOfModule + ":ip", systemParameters, newIssuerPublicKey);
    childZkModules.add(ipkProver);
  }


  @Override
  public void initializeModule(final ZkProofStateInitialize zkBuilder) throws ConfigurationException {
    for (final ZkModuleProver zkModuleProver : childZkModules) {
      zkModuleProver.initializeModule(zkBuilder);
    }
    
    int j = 0; // gives the index for the NEW identifier of the attribute
    for (int i = 0; i < newCredentialAttributes.size(); i++) {
      if (carryAttributeOver.get(i)) {
        zkBuilder.registerAttribute(identifierOfAttribute(i), false);
        final BigInt attributeValue = newCredentialAttributes.get(i);
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
    // Skip randomizer
    j++;
    if(isDevicePresent) {
      zkBuilder.registerAttribute(identifierOfSecretAttribute(), true);
      zkBuilder.attributesAreEqual(identifierOfSecretAttribute(), childZkModules.get(0)
        .identifierOfAttribute(j));
    }
  }

  @Override
  public void collectAttributesForProof(final ZkProofStateCollect zkBuilder) throws ConfigurationException {
    for (final ZkModuleProver module : childZkModules) {
      module.collectAttributesForProof(zkBuilder);
    }
    for (int i = 0; i < newCredentialAttributes.size(); i++) {
      if (carryAttributeOver.get(i) && newCredentialAttributes.get(i) == null) {
        zkBuilder.getValueOfAttribute(identifierOfAttribute(i));
      }
    }
  }

  @Override
  public void firstRound(final ZkProofStateFirstRound zkBuilder) throws ConfigurationException,
      ProofException {
    for (final ZkModuleProver zkModuleProver : childZkModules) {
      zkModuleProver.firstRound(zkBuilder);
    }
    zkBuilder.addNValue(getBuildingBlockId() + ":credSpecId", credentialSpecificationId);
  }

  @Override
  public void secondRound(final ZkProofStateSecondRound zkBuilder) throws ConfigurationException {
    for (final ZkModuleProver zkModuleProver : childZkModules) {
      zkModuleProver.secondRound(zkBuilder);
    }
    @SuppressWarnings("unchecked")
    final ZkModuleProverCommitment<KnownOrderGroupElement> pedersen = (ZkModuleProverCommitment<KnownOrderGroupElement>) childZkModules.get(0);

    final List<BigInt> carriedOverAttributes = new ArrayList<BigInt>(newCredentialAttributes);
    for (int i = 0; i < newCredentialAttributes.size(); i++) {
      if (carryAttributeOver.get(i)) {
        final BigInt value = zkBuilder.getValueOfAttribute(identifierOfAttribute(i));
        carriedOverAttributes.set(i, value);   
      }
    }
    
    carryOverStateRecipient =
        new CarryOverStateRecipient(pedersen.recoverCommitment(), pedersen.recoverRandomizers()
            .get(0), carriedOverAttributes);
  }

  @Override
  public CarryOverStateRecipient recoverState() {
    return carryOverStateRecipient;
  }

}
