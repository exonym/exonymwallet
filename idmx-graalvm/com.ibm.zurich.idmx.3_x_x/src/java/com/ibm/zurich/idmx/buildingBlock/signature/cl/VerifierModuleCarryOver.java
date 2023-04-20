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
import java.util.List;

import com.ibm.zurich.idmx.buildingBlock.factory.BuildingBlockFactory;
import com.ibm.zurich.idmx.buildingBlock.helper.BaseForRepresentation;
import com.ibm.zurich.idmx.buildingBlock.helper.representation.damgardFujisaki.DamgardFujisakiRepresentationBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.issuerKey.IssuerPublicKeyBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.systemParameters.SystemParametersBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.verifierParameters.VerifierParametersBuildingBlock;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.ProofException;
import com.ibm.zurich.idmx.interfaces.state.CarryOverStateIssuer;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.group.GroupFactory;
import com.ibm.zurich.idmx.interfaces.util.group.HiddenOrderGroup;
import com.ibm.zurich.idmx.interfaces.util.group.HiddenOrderGroupElement;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleVerifier;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleVerifierCarryOver;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkVerifierStateCollect;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkVerifierStateVerify;
import com.ibm.zurich.idmx.zkModule.ZkModuleImpl;

import eu.abc4trust.xml.PublicKey;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.VerifierParameters;

class VerifierModuleCarryOver extends ZkModuleImpl implements ZkModuleVerifierCarryOver {

  private final List<ZkModuleVerifier> childZkModules;
  private String identifierOfModule;
  private final List<Boolean> carryOverAttribute;
  private CarryOverStateIssuer carryOverStateIssuer;
  private BigInt credentialSpecificationId;
  private final HiddenOrderGroup group;
  private final boolean hasExternalSecret;
  private List<BigInteger> recoveredAttributes;

  public VerifierModuleCarryOver(ClSignatureBuildingBlock parent, String identifierOfModule,
      SystemParameters systemParameters, VerifierParameters verifierParameters,
      PublicKey newIssuerPublicKey, BigInt credentialSpecificationId,
      List<Boolean> issuerChosenAttribute, boolean hasDevice,
      BuildingBlockFactory buildingBlockFactory, GroupFactory groupFactory,
      BigIntFactory bigIntFactory) throws ConfigurationException, ProofException {

    super(parent, identifierOfModule);

    this.hasExternalSecret = hasDevice;
    this.recoveredAttributes = new ArrayList<BigInteger>();

    this.credentialSpecificationId = credentialSpecificationId;
    this.identifierOfModule = identifierOfModule;

    ClPublicKeyWrapper pkWrapper = new ClPublicKeyWrapper(newIssuerPublicKey);
    // It is more intuitive to work with carried attributes than the others
    this.carryOverAttribute = new ArrayList<Boolean>();
    for (int i = 0; i < issuerChosenAttribute.size(); i++) {
      this.carryOverAttribute.add(!issuerChosenAttribute.get(i));
    }

    // TODO check whether pk.getBases().size() >= carryOverAttribute.length

    this.group = groupFactory.createSignedQuadraticResiduesGroup(pkWrapper.getModulus());

    // The bases for the DF building block are given by the bases corresponding to the carried
    // attributes, potentially the bases used by the device, plus a final base for randomising the
    // commitment
    List<BaseForRepresentation> bases = new ArrayList<BaseForRepresentation>();
    for (int i = 0; i < issuerChosenAttribute.size(); i++) {
      if (carryOverAttribute.get(i)) {
        bases.add(BaseForRepresentation.managedAttribute(group.valueOf(pkWrapper
            .getBase(i))));
      }
    }
    HiddenOrderGroupElement S = group.valueOf(pkWrapper.getS());
    if (hasExternalSecret) {
      HiddenOrderGroupElement R0 = group.valueOfNoCheck(pkWrapper.getRd());
      bases.add(BaseForRepresentation.deviceSecret(R0));
      bases.add(BaseForRepresentation.deviceRandomizer(S, bigIntFactory.zero()));
    }
    bases.add(BaseForRepresentation.randomAttribute(S));

    DamgardFujisakiRepresentationBuildingBlock damgardFujisakiBB =
        buildingBlockFactory
            .getBuildingBlockByClass(DamgardFujisakiRepresentationBuildingBlock.class);
    ZkModuleVerifier df = damgardFujisakiBB.getZkModuleVerifier(systemParameters, // systemParameters
        identifierOfModule + ":rep", // identifierOfModule,
        bases, // bases
        null, // commitment
        identifierOfModule + ":rep:C", // commitmentAsDValue
        group); // group
    this.childZkModules = new ArrayList<ZkModuleVerifier>();
    childZkModules.add(df);

    // add SystemParameters as child building block such that they become part of the hash
    // contribution
    SystemParametersBuildingBlock spBB =
        buildingBlockFactory.getBuildingBlockByClass(SystemParametersBuildingBlock.class);
    ZkModuleVerifier spVerifier =
        spBB.getZkModuleVerifier(identifierOfModule + ":sp", systemParameters);
    childZkModules.add(spVerifier);

    // add VerifierParameters as child building block such that they become part of the hash
    // contribution
    VerifierParametersBuildingBlock vpBB =
        buildingBlockFactory.getBuildingBlockByClass(VerifierParametersBuildingBlock.class);
    ZkModuleVerifier vpVerifier =
        vpBB.getZkModuleVerifier(identifierOfModule + ":vp", systemParameters, verifierParameters);
    childZkModules.add(vpVerifier);

    // add NewIssuerPublicKey as child building block such that they become part of the hash
    // contribution
    IssuerPublicKeyBuildingBlock ipkBB =
        buildingBlockFactory.getBuildingBlockByClass(IssuerPublicKeyBuildingBlock.class);
    ZkModuleVerifier ipkVerifier =
        ipkBB.getZkModuleVerifier(identifierOfModule + ":ip", systemParameters, newIssuerPublicKey);
    childZkModules.add(ipkVerifier);
  }

  @Override
  public void collectAttributesForVerify(ZkVerifierStateCollect zkVerifier) throws ProofException,
      ConfigurationException {
    for (ZkModuleVerifier module : childZkModules) {
      module.collectAttributesForVerify(zkVerifier);
    }

    // The values contained in the computed commitment must be equal to those that are to be carried
    // over; j is the index of the corresponding base inside the DF block
    int j = 0; // gives the index for the NEW identifier of the attribute
    for (int i = 0; i < carryOverAttribute.size(); i++) {
      if (carryOverAttribute.get(i)) {
        zkVerifier.registerAttribute(identifierOfAttribute(i), false);
        zkVerifier.attributesAreEqual(identifierOfAttribute(i), childZkModules.get(0)
            .identifierOfAttribute(j));
        j++;
      }
    }
    // in case a smartcard is present, the commitment needs to be bound to the same device secret
    // key
    if (hasExternalSecret) {
      zkVerifier.registerAttribute(identifierOfSecretAttribute(), true);
      zkVerifier.attributesAreEqual(identifierOfSecretAttribute(), childZkModules.get(0)
          .identifierOfAttribute(j));
    }
  }

  @Override
  public boolean verify(ZkVerifierStateVerify zkVerifier) throws ConfigurationException,
      ProofException {
    zkVerifier.checkNValue(getBuildingBlockId() + ":credSpecId", credentialSpecificationId);

    boolean success = true;
    for (ZkModuleVerifier zkModule : childZkModules) {
      success &= zkModule.verify(zkVerifier);
    }

    HiddenOrderGroupElement C =
        zkVerifier.getDValueAsGroupElement(identifierOfModule + ":rep:C", group);
    carryOverStateIssuer = new CarryOverStateIssuer(C, carryOverAttribute);

    // all known (i.e., revealed and non-issuer-chosen) attributes are collected. The remaining
    // values are set to null
    for (int i = 0; i < carryOverAttribute.size(); i++) {
      if (carryOverAttribute.get(i) && zkVerifier.isRevealedAttribute(identifierOfAttribute(i))) {
        recoveredAttributes.add(zkVerifier.getValueOfRevealedAttribute(identifierOfAttribute(i))
            .getValue());
      } else {
        recoveredAttributes.add(null);
      }
    }

    return success;
  }

  @Override
  public List<BigInteger> recoverAttributes() {
    return recoveredAttributes;
  }

  @Override
  public CarryOverStateIssuer recoverState() {
    return carryOverStateIssuer;
  }

}
