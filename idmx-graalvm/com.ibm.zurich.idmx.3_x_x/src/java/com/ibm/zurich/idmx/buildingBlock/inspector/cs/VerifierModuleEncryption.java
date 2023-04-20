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
package com.ibm.zurich.idmx.buildingBlock.inspector.cs;

import java.util.ArrayList;
import java.util.List;

import com.ibm.zurich.idmx.buildingBlock.GeneralBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.factory.BuildingBlockFactory;
import com.ibm.zurich.idmx.buildingBlock.helper.BaseForRepresentation;
import com.ibm.zurich.idmx.buildingBlock.helper.representation.damgardFujisaki.DamgardFujisakiRepresentationBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.helper.representation.modNSquare.ModNSquareRepresentationBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.inspectorKey.InspectorPublicKeyBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.systemParameters.SystemParametersBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.verifierParameters.VerifierParametersBuildingBlock;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.ProofException;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.RandomGeneration;
//import com.ibm.zurich.idmx.interfaces.util.group.GroupElement;
import com.ibm.zurich.idmx.interfaces.util.group.GroupFactory;
import com.ibm.zurich.idmx.interfaces.util.group.HiddenOrderGroup;
import com.ibm.zurich.idmx.interfaces.util.group.HiddenOrderGroupElement;
import com.ibm.zurich.idmx.interfaces.util.group.PaillierGroup;
import com.ibm.zurich.idmx.interfaces.util.group.PaillierGroupElement;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleVerifier;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkVerifierStateCollect;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkVerifierStateVerify;
import com.ibm.zurich.idmx.zkModule.ZkModuleImpl;

import eu.abc4trust.xml.PublicKey;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.VerifierParameters;

public class VerifierModuleEncryption extends ZkModuleImpl implements ZkModuleVerifier {
  private final String idOfAttributeToEncrypt;
  @SuppressWarnings("unused")
  private final RandomGeneration rg;
  private final List<ZkModuleVerifier> proofs;
  private final BigInt n;
  private final String identifierOfModule;
  private final BigIntFactory bigIntFactory;
  private final PaillierGroup group;
  private final BigInt nFrakt;
  private final HiddenOrderGroupElement gFrakt, hFrakt;
  private final PaillierGroupElement g, h, y1, y2, y3;
  private final HiddenOrderGroup groupFrakt;
  private final byte[] label;
  private final List<BaseForRepresentation> basesForV;
  private final CsPublicKeyWrapper ipkWrapper;

  public VerifierModuleEncryption(final GeneralBuildingBlock parent, final String identifierOfModule,
                                  final SystemParameters systemParameters, final VerifierParameters verifierParameters,
      /* Inspector */final PublicKey inspectorPublicKey, final String idOfAttributeToEncrypt, final byte[] label,
      final BuildingBlockFactory bbFactory, final BigIntFactory bigIntFactory, final GroupFactory groupFactory,
      final RandomGeneration rg) throws ProofException, ConfigurationException {
    super(parent, identifierOfModule);
    this.idOfAttributeToEncrypt = idOfAttributeToEncrypt;
    this.rg = rg;
    this.identifierOfModule = identifierOfModule;
    this.bigIntFactory = bigIntFactory;
    this.label = label;

    this.ipkWrapper = new CsPublicKeyWrapper(inspectorPublicKey);
    this.n = ipkWrapper.getModulus();
    this.group = groupFactory.createPaillierGroup(n);
    this.g = group.valueOf(ipkWrapper.getG());
    this.h = group.valueOf(ipkWrapper.getH());
    this.y1 = group.valueOf(ipkWrapper.getY1());
    this.y2 = group.valueOf(ipkWrapper.getY2());
    this.y3 = group.valueOf(ipkWrapper.getY3());
    this.nFrakt = ipkWrapper.getAuxiliaryModulus();
    this.groupFrakt = groupFactory.createSignedQuadraticResiduesGroup(nFrakt);
    this.gFrakt = groupFrakt.valueOf(ipkWrapper.getAuxiliaryG());
    this.hFrakt = groupFrakt.valueOf(ipkWrapper.getAuxiliaryH());

    this.proofs = new ArrayList<ZkModuleVerifier>();
    final ModNSquareRepresentationBuildingBlock paillierBB =
        bbFactory.getBuildingBlockByClass(ModNSquareRepresentationBuildingBlock.class);
    final DamgardFujisakiRepresentationBuildingBlock dfBB =
        bbFactory.getBuildingBlockByClass(DamgardFujisakiRepresentationBuildingBlock.class);

    final List<BaseForRepresentation> basesForU = new ArrayList<BaseForRepresentation>();
    basesForU.add(BaseForRepresentation.managedAttribute(g.op(g)));
    final ZkModuleVerifier paillierForU =
        paillierBB.getZkModuleVerifier(systemParameters, identifierOfModule + ":ins:0:u",
            basesForU, null /* commitment */, identifierOfModule + ":ins:0:u:C", group);
    proofs.add(paillierForU);

    final List<BaseForRepresentation> basesForE = new ArrayList<BaseForRepresentation>();
    basesForE.add(BaseForRepresentation.managedAttribute(y1.op(y1)));
    basesForE.add(BaseForRepresentation.managedAttribute(h.op(h)));
    final ZkModuleVerifier paillierForE =
        paillierBB.getZkModuleVerifier(systemParameters, identifierOfModule + ":ins:1:e",
            basesForE, null /* commitment */, identifierOfModule + ":ins:1:e:C", group);
    proofs.add(paillierForE);

    // this base depends on the ciphertext, and will be set before the first round
    this.basesForV = new ArrayList<BaseForRepresentation>();
    basesForV.add(BaseForRepresentation.managedAttribute((PaillierGroupElement) null));
    final ZkModuleVerifier paillierForV =
        paillierBB.getZkModuleVerifier(systemParameters, identifierOfModule + ":ins:2:v",
            basesForV, null /* commitment */, identifierOfModule + ":ins:2:v:C", group);
    proofs.add(paillierForV);

    final List<BaseForRepresentation> basesForIntCom = new ArrayList<BaseForRepresentation>();
    basesForIntCom.add(BaseForRepresentation.managedAttribute(gFrakt));
    basesForIntCom.add(BaseForRepresentation.randomAttribute(hFrakt));
    final ZkModuleVerifier dfForYFrakt =
        dfBB.getZkModuleVerifier(systemParameters, identifierOfModule + ":ins:3:intCom",
            basesForIntCom, null /* commitment */, identifierOfModule + ":ins:3:intCom:C",
            groupFrakt);
    proofs.add(dfForYFrakt);

    // Proofs are ordered as proofs for (u, e, v, delta, yFrakt)

    // add SystemParameters as child building block such that they become part of the hash
    // contribution
    final SystemParametersBuildingBlock spBB =
        bbFactory.getBuildingBlockByClass(SystemParametersBuildingBlock.class);
    final ZkModuleVerifier spVerifier =
        spBB.getZkModuleVerifier(identifierOfModule + ":sp", systemParameters);
    proofs.add(spVerifier);

    // add VerifierParameters as child building block such that they become part of the hash
    // contribution
    final VerifierParametersBuildingBlock vpBB =
        bbFactory.getBuildingBlockByClass(VerifierParametersBuildingBlock.class);
    final ZkModuleVerifier vpVerifier =
        vpBB.getZkModuleVerifier(identifierOfModule + ":vp", systemParameters, verifierParameters);
    proofs.add(vpVerifier);

    // add NewIssuerPublicKey as child building block such that they become part of the hash
    // contribution
    final InspectorPublicKeyBuildingBlock ipkBB =
        bbFactory.getBuildingBlockByClass(InspectorPublicKeyBuildingBlock.class);
    final ZkModuleVerifier ipkVerifier =
        ipkBB.getZkModuleVerifier(identifierOfModule + ":ip", systemParameters, inspectorPublicKey);
    proofs.add(ipkVerifier);
  }

  @Override
  public void collectAttributesForVerify(final ZkVerifierStateCollect zkVerifier) throws ProofException,
      ConfigurationException {
    for (final ZkModuleVerifier module : proofs) {
      module.collectAttributesForVerify(zkVerifier);
    }

    // require message to encrypt and define it equal to the message in the encryption scheme
    zkVerifier.registerAttribute(idOfAttributeToEncrypt, false);
    zkVerifier.attributesAreEqual(idOfAttributeToEncrypt, proofs.get(1).identifierOfAttribute(1));
    // choose r randomly and ensure that it is also used within the encryption scheme
    zkVerifier.registerAttribute(identifierOfModule + ":r", false, n.bitLength() - 2);
    zkVerifier
        .attributesAreEqual(identifierOfModule + ":r", proofs.get(0).identifierOfAttribute(0));
    // attribute to encrypt must be equal to the attribute contained in the ciphertext
    zkVerifier.attributesAreEqual(idOfAttributeToEncrypt, proofs.get(1).identifierOfAttribute(1));
    // define equality relations required by the scheme
    zkVerifier.attributesAreEqual(proofs.get(0).identifierOfAttribute(0), proofs.get(1)
        .identifierOfAttribute(0));
    zkVerifier.attributesAreEqual(proofs.get(0).identifierOfAttribute(0), proofs.get(2)
        .identifierOfAttribute(0));
    zkVerifier.attributesAreEqual(proofs.get(1).identifierOfAttribute(1), proofs.get(3)
        .identifierOfAttribute(0));
  }

  @Override
  public boolean verify(final ZkVerifierStateVerify zkVerifier) throws ConfigurationException,
      ProofException {
    // set base for v depending on ciphertext
    final PaillierGroupElement u =
        zkVerifier.getDValueAsGroupElement(identifierOfModule + ":ciphertext:u", group);
    final PaillierGroupElement e =
        zkVerifier.getDValueAsGroupElement(identifierOfModule + ":ciphertext:e", group);
    final PaillierGroupElement v =
        zkVerifier.getDValueAsGroupElement(identifierOfModule + ":ciphertext:v", group);

    final PaillierGroupElement uSquare =
        zkVerifier.getDValueAsGroupElement(identifierOfModule + ":ins:0:u:C", group);
    final PaillierGroupElement eSquare =
        zkVerifier.getDValueAsGroupElement(identifierOfModule + ":ins:1:e:C", group);
    final PaillierGroupElement vSquare =
        zkVerifier.getDValueAsGroupElement(identifierOfModule + ":ins:2:v:C", group);

    if (!u.op(u).equals(uSquare) || !e.op(e).equals(eSquare) || !v.op(v).equals(vSquare)) {
      return false;
    }

    final CsInspectorHelper CsHelper = new CsInspectorHelper();
    final BigInt hk = ipkWrapper.getHashKey();
    final PaillierGroupElement y2y3H =
        y2.opMultOp(y3,
            CsHelper.computeHash(hk, u, e, label, ipkWrapper.getHashFunction(), bigIntFactory));
    basesForV.set(0, BaseForRepresentation.managedAttribute(y2y3H.op(y2y3H)));

    for (final ZkModuleVerifier module : proofs) {
      module.verify(zkVerifier);
    }
    // TODO: check NValues
    zkVerifier.checkNValue(identifierOfModule + ":y2y3H", y2y3H);
    // TODO: Check size of responses
    return true;
  }

}
