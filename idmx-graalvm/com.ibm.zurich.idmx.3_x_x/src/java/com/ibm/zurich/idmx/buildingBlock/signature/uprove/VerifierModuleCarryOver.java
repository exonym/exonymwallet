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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

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
import com.ibm.zurich.idmx.interfaces.state.CarryOverStateIssuer;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.RandomGeneration;
import com.ibm.zurich.idmx.interfaces.util.group.GroupFactory;
import com.ibm.zurich.idmx.interfaces.util.group.KnownOrderGroup;
import com.ibm.zurich.idmx.interfaces.util.group.KnownOrderGroupElement;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleVerifier;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleVerifierCarryOver;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkVerifierStateCollect;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkVerifierStateVerify;
import com.ibm.zurich.idmx.zkModule.ZkModuleImpl;

import eu.abc4trust.xml.PublicKey;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.VerifierParameters;

class VerifierModuleCarryOver extends ZkModuleImpl implements ZkModuleVerifierCarryOver {

  private final BuildingBlockFactory buildingBlockFactory;
  private final BigIntFactory bigIntFactory;
  private final RandomGeneration randomGeneration;
  private final List<ZkModuleVerifier> childZkModules;
  private final Logger logger;
  private final List<Boolean> carryOverAttribute;
  //TODO(ksa) not immutable - refactor
  private CarryOverStateIssuer carryOverStateIssuer;
  private final String identifierOfModule;
  private final KnownOrderGroup group;
  private final BigInt credentialSpecificationId;
  private final HashMap<String, BigInt> revealedAttributes;
  private final boolean hasDevice;

  public VerifierModuleCarryOver(final BrandsSignatureBuildingBlock parent, final String identifierOfModule,
                                 final SystemParameters systemParameters, final VerifierParameters verifierParameters,
      final PublicKey newIssuerPublicKey, final List<Boolean> issuerChosenAttribute,
      final BigInt credentialSpecificationId, final boolean hasDevice, final ExternalSecretsManager esManager,
      final Logger logger, final BuildingBlockFactory buildingBlockFactory, final GroupFactory groupFactory,
      final BigIntFactory bigIntFactory, final RandomGeneration randomGeneration)
      throws ConfigurationException, ProofException {

    super(parent, identifierOfModule);

    this.revealedAttributes = new HashMap<String, BigInt>();

    this.credentialSpecificationId = credentialSpecificationId;
    this.identifierOfModule = identifierOfModule;
    this.hasDevice = hasDevice;
    this.logger = logger;
    final BrandsPublicKeyWrapper pkWrapper = new BrandsPublicKeyWrapper(newIssuerPublicKey);

    this.carryOverAttribute = new ArrayList<Boolean>();
    for (int i = 0; i < issuerChosenAttribute.size(); i++) {
      this.carryOverAttribute.add(!issuerChosenAttribute.get(i));
    }

    // TODO: check whether pk.getBases().size() >= carryOverAttribute.length

    this.buildingBlockFactory = buildingBlockFactory;
    this.bigIntFactory = bigIntFactory;
    this.randomGeneration = randomGeneration;
    final EcryptSystemParametersWrapper spWrapper = new EcryptSystemParametersWrapper(systemParameters);
    final PedersenRepresentationBuildingBlock pedersenBB =
        new PedersenRepresentationBuildingBlock(this.randomGeneration, this.buildingBlockFactory,
            esManager, this.logger, this.bigIntFactory);

    this.group =
        groupFactory
            .createPrimeOrderGroup(spWrapper.getDHModulus(), spWrapper.getDHSubgroupOrder());

    final List<BaseForRepresentation> bases = new ArrayList<BaseForRepresentation>();
    for (int i = 0; i < carryOverAttribute.size(); i++) {
      if (carryOverAttribute.get(i)) {
        bases.add(BaseForRepresentation.managedAttribute(group.valueOfNoCheck(pkWrapper
            .getGI(i + 1))));
      }
    }
    final KnownOrderGroupElement g = group.valueOfNoCheck(spWrapper.getDHGenerator1());
    bases.add(BaseForRepresentation.randomAttribute(g));
    if(hasDevice) {
      final KnownOrderGroupElement gd = group.valueOfNoCheck(pkWrapper.getGD());
      bases.add(BaseForRepresentation.deviceSecret(gd));
    }

    final ZkModuleVerifier pedersen = pedersenBB.getZkModuleVerifier(systemParameters, // systemParameters
        identifierOfModule + ":rep", // identifierOfModule,
        bases, // bases
        null, // commitment
        identifierOfModule + ":rep:C", // commitmentAsDValue
        group); // group

    this.childZkModules = new ArrayList<ZkModuleVerifier>();
    childZkModules.add(pedersen);

    // add SystemParameters as child building block such that they become part of the hash
    // contribution
    final SystemParametersBuildingBlock spBB =
        buildingBlockFactory.getBuildingBlockByClass(SystemParametersBuildingBlock.class);
    final ZkModuleVerifier spVerifier =
        spBB.getZkModuleVerifier(identifierOfModule + ":sp", systemParameters);
    childZkModules.add(spVerifier);

    // add VerifierParameters as child building block such that they become part of the hash
    // contribution
    final VerifierParametersBuildingBlock vpBB =
        buildingBlockFactory.getBuildingBlockByClass(VerifierParametersBuildingBlock.class);
    final ZkModuleVerifier vpVerifier =
        vpBB.getZkModuleVerifier(identifierOfModule + ":vp", systemParameters, verifierParameters);
    childZkModules.add(vpVerifier);

    // add NewIssuerPublicKey as child building block such that they become part of the hash
    // contribution
    final IssuerPublicKeyBuildingBlock ipkBB =
        buildingBlockFactory.getBuildingBlockByClass(IssuerPublicKeyBuildingBlock.class);
    final ZkModuleVerifier ipkVerifier =
        ipkBB.getZkModuleVerifier(identifierOfModule + ":ip", systemParameters, newIssuerPublicKey);
    childZkModules.add(ipkVerifier);
  }


  @Override
  public void collectAttributesForVerify(final ZkVerifierStateCollect zkVerifier) throws ProofException,
      ConfigurationException {
    for (final ZkModuleVerifier module : childZkModules) {
      module.collectAttributesForVerify(zkVerifier);
    }

    int j = 0; // gives the index for the NEW identifier of the attribute
    for (int i = 0; i < carryOverAttribute.size(); i++) {
      if (carryOverAttribute.get(i)) {
        zkVerifier.registerAttribute(identifierOfAttribute(i), false);
        zkVerifier.attributesAreEqual(identifierOfAttribute(i), childZkModules.get(0)
            .identifierOfAttribute(j));
        j++;
      }
    }
    // Skip randomizer
    j++;
    if(hasDevice) {
      zkVerifier.registerAttribute(identifierOfSecretAttribute(), true);
      zkVerifier.attributesAreEqual(identifierOfSecretAttribute(), childZkModules.get(0)
        .identifierOfAttribute(j));
    }
  }

  @Override
  public boolean verify(final ZkVerifierStateVerify zkVerifier) throws ConfigurationException,
      ProofException {
    zkVerifier.checkNValue(getBuildingBlockId() + ":credSpecId", credentialSpecificationId);

    boolean success = true;
    for (ZkModuleVerifier zkModule : childZkModules) {
      success &= zkModule.verify(zkVerifier);
    }

    final KnownOrderGroupElement C =
        zkVerifier.getDValueAsGroupElement(identifierOfModule + ":rep:C", group);
    carryOverStateIssuer = new CarryOverStateIssuer(C, carryOverAttribute);

    // TODO: check this
    for (int i = 0; i < carryOverAttribute.size(); i++) {
      if (carryOverAttribute.get(i) && zkVerifier.isRevealedAttribute(identifierOfAttribute(i))) {
        revealedAttributes.put(identifierOfAttribute(i),
            zkVerifier.getValueOfRevealedAttribute(identifierOfAttribute(i)));
      }
    }

    return success;
  }

  /*
   * iterate over all new attributes; if it was carried over AND revealed, it is added to the list
   */
  @Override
  public List<BigInteger> recoverAttributes() {
    final List<BigInteger> recAtt = new ArrayList<BigInteger>();
    for (int i = 0; i < carryOverAttribute.size(); i++) {
      if (carryOverAttribute.get(i) && revealedAttributes.containsKey(identifierOfAttribute(i))) {
        recAtt.add(revealedAttributes.get(identifierOfAttribute(i)).getValue());
      } else {
        recAtt.add(null);
      }
    }
    return recAtt;
  }

  @Override
  public CarryOverStateIssuer recoverState() {
    return carryOverStateIssuer;
  }

}
