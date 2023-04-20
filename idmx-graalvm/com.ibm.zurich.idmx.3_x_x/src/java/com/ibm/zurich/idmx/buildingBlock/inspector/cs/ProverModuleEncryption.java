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
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProver;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProverCommitment;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProverVerifiableEncryption;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateCollect;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateFirstRound;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateInitialize;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateSecondRound;
import com.ibm.zurich.idmx.zkModule.ZkModuleImpl;

import eu.abc4trust.xml.PublicKey;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.VerifierParameters;

public class ProverModuleEncryption extends ZkModuleImpl
    implements
      ZkModuleProverVerifiableEncryption {

  private final List<ZkModuleProver> proofs;
  private final String idOfAttributeToEncrypt;
  private final RandomGeneration rg;
  private final BigInt n;
  private final String identifierOfModule;
  private PaillierGroupElement u, e, v, y2y3H;
  private BigInt r;
  private final PaillierGroupElement g, h, y1, y2, y3;
  private final HiddenOrderGroupElement gFrakt, hFrakt;
  private final BigInt nFrakt;
  private final PaillierGroupElement[] ciphertext;
  private final PaillierGroup group;
  @SuppressWarnings("unused")
  private final BuildingBlockFactory bbFactory;
  @SuppressWarnings("unused")
  private final SystemParameters systemParameters;
  private final byte[] label;
  private final List<BaseForRepresentation> basesForV;
  private final BigIntFactory bigIntFactory;
  private final HiddenOrderGroup groupFrakt;
  private final CsPublicKeyWrapper ipkWrapper;

  public ProverModuleEncryption(final GeneralBuildingBlock parent, final String identifierOfModule,
                                final SystemParameters systemParameters, final VerifierParameters verifierParameters,
      /* Inspector */final PublicKey inspectorPublicKey, final byte[] label, final String idOfAttributeToEncrypt,
      final BuildingBlockFactory bbFactory, final BigIntFactory bigIntFactory, final GroupFactory groupFactory,
      final RandomGeneration rg) throws ConfigurationException {
    super(parent, identifierOfModule);
    this.idOfAttributeToEncrypt = idOfAttributeToEncrypt;
    this.rg = rg;
    this.bigIntFactory = bigIntFactory;
    this.label = label;
    this.identifierOfModule = identifierOfModule;
    this.ciphertext = new PaillierGroupElement[3];
    this.bbFactory = bbFactory;
    this.systemParameters = systemParameters;
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

    this.proofs = new ArrayList<ZkModuleProver>();
    final ModNSquareRepresentationBuildingBlock paillierBB =
        bbFactory.getBuildingBlockByClass(ModNSquareRepresentationBuildingBlock.class);
    final DamgardFujisakiRepresentationBuildingBlock dfBB =
        bbFactory.getBuildingBlockByClass(DamgardFujisakiRepresentationBuildingBlock.class);

    final List<BaseForRepresentation> basesForU = new ArrayList<BaseForRepresentation>();
    basesForU.add(BaseForRepresentation.managedAttribute(g.op(g)));
    final ZkModuleProverCommitment<PaillierGroupElement> paillierForU =
        paillierBB.getZkModuleProver(systemParameters, identifierOfModule + ":ins:0:u", null,
            basesForU, group, null, null, null, null);
    proofs.add(paillierForU);

    final List<BaseForRepresentation> basesForE = new ArrayList<BaseForRepresentation>();
    basesForE.add(BaseForRepresentation.managedAttribute(y1.op(y1)));
    basesForE.add(BaseForRepresentation.managedAttribute(h.op(h)));
    final ZkModuleProverCommitment<PaillierGroupElement> paillierForE =
        paillierBB.getZkModuleProver(systemParameters, identifierOfModule + ":ins:1:e", null,
            basesForE, group, null, null, null, null);
    proofs.add(paillierForE);

    // this base depends on the ciphertext, and will be set before the first round
    this.basesForV = new ArrayList<BaseForRepresentation>();
    basesForV.add(BaseForRepresentation.managedAttribute((PaillierGroupElement) null));
    final ZkModuleProverCommitment<PaillierGroupElement> paillierForV =
        paillierBB.getZkModuleProver(systemParameters, identifierOfModule + ":ins:2:v", null,
            basesForV, group, null, null, null, null);
    proofs.add(paillierForV);

    final List<BaseForRepresentation> basesForIntCom = new ArrayList<BaseForRepresentation>();
    basesForIntCom.add(BaseForRepresentation.managedAttribute(gFrakt));
    basesForIntCom.add(BaseForRepresentation.randomAttribute(hFrakt));
    final ZkModuleProverCommitment<?> dfForYFrakt =
        dfBB.getZkModuleProver(systemParameters, identifierOfModule + ":ins:3:intCom", null,
            basesForIntCom, groupFrakt, null, null, null, null);
    proofs.add(dfForYFrakt);
    // Proofs are ordered as proofs for (u, e, v, yFrakt)

    // add SystemParameters as child building block such that they become part of the hash
    // contribution
    final SystemParametersBuildingBlock spBB =
        bbFactory.getBuildingBlockByClass(SystemParametersBuildingBlock.class);
    final ZkModuleProver spProver = spBB.getZkModuleProver(identifierOfModule + ":sp", systemParameters);
    proofs.add(spProver);

    // add VerifierParameters as child building block such that they become part of the hash
    // contribution
    final VerifierParametersBuildingBlock vpBB =
        bbFactory.getBuildingBlockByClass(VerifierParametersBuildingBlock.class);
    final ZkModuleProver vpProver =
        vpBB.getZkModuleProver(identifierOfModule + ":vp", systemParameters, verifierParameters);
    proofs.add(vpProver);

    // add NewIssuerPublicKey as child building block such that they become part of the hash
    // contribution
    final InspectorPublicKeyBuildingBlock ipkBB =
        bbFactory.getBuildingBlockByClass(InspectorPublicKeyBuildingBlock.class);
    final ZkModuleProver ipkProver =
        ipkBB.getZkModuleProver(identifierOfModule + ":ip", systemParameters, inspectorPublicKey);
    proofs.add(ipkProver);
  }

  @Override
  public void initializeModule(final ZkProofStateInitialize zkBuilder) throws ConfigurationException {
    for (final ZkModuleProver module : proofs) {
      module.initializeModule(zkBuilder);
    }
    // require message to encrypt and define it equal to the message in the encryption scheme
    zkBuilder.registerAttribute(idOfAttributeToEncrypt, false);
    zkBuilder.requiresAttributeValue(idOfAttributeToEncrypt);
    zkBuilder.attributesAreEqual(idOfAttributeToEncrypt, proofs.get(1).identifierOfAttribute(1));
    // choose r randomly and ensure that it is also used within the encryption scheme
    zkBuilder.registerAttribute(identifierOfModule + ":r", false, n.bitLength() - 2);
    //TODO(ksa) why is r global?
    r = rg.generateRandomNumber(n.shiftRight(2));
    zkBuilder.setValueOfAttribute(identifierOfModule + ":r", r, null);
    zkBuilder.attributesAreEqual(identifierOfModule + ":r", proofs.get(0).identifierOfAttribute(0));
    // attribute to encrypt must be equal to the attribute contained in the ciphertext
    zkBuilder.attributesAreEqual(idOfAttributeToEncrypt, proofs.get(1).identifierOfAttribute(1));
    // define equality relations required by the scheme
    zkBuilder.attributesAreEqual(proofs.get(0).identifierOfAttribute(0), proofs.get(1)
        .identifierOfAttribute(0));
    zkBuilder.attributesAreEqual(proofs.get(0).identifierOfAttribute(0), proofs.get(2)
        .identifierOfAttribute(0));
    zkBuilder.attributesAreEqual(proofs.get(1).identifierOfAttribute(1), proofs.get(3)
        .identifierOfAttribute(0));
  }

  @Override
  public void collectAttributesForProof(final ZkProofStateCollect zkBuilder) throws ConfigurationException {
    for (final ZkModuleProver module : proofs) {
      module.collectAttributesForProof(zkBuilder);
    }
  }

  @Override
  public void firstRound(final ZkProofStateFirstRound zkBuilder) throws ConfigurationException,
      ProofException {
    u = g.multOp(r);
    e = y1.multOp(r);
    e = e.opMultOp(h, zkBuilder.getValueOfAttribute(idOfAttributeToEncrypt));

    final CsInspectorHelper CsHelper = new CsInspectorHelper();
    final BigInt hk = ipkWrapper.getHashKey();
    this.y2y3H =
        y2.opMultOp(y3, CsHelper.computeHash(hk, u, e, label, ipkWrapper.getHashFunction(), bigIntFactory));

    v = y2y3H.multOp(r);
    v = group.valueOf(abs(v.toBigInt(), n.multiply(n)));
    ciphertext[0] = u;
    ciphertext[1] = e;
    ciphertext[2] = v;

    // set base for the proof for v
    basesForV.set(0, BaseForRepresentation.managedAttribute(y2y3H.op(y2y3H)));

    for (final ZkModuleProver module : proofs) {
      module.firstRound(zkBuilder);
    }
    // TODO: add NValues
    zkBuilder.addNValue(identifierOfModule + ":y2y3H", y2y3H);
    // add D Values
    zkBuilder.addDValue(identifierOfModule + ":ciphertext:u", u);
    zkBuilder.addDValue(identifierOfModule + ":ciphertext:e", e);
    zkBuilder.addDValue(identifierOfModule + ":ciphertext:v", v);
  }

  @Override
  public void secondRound(final ZkProofStateSecondRound zkBuilder) throws ConfigurationException {
    for (final ZkModuleProver module : proofs) {
      module.secondRound(zkBuilder);
    }
  }

  @Override
public PaillierGroupElement[] getCiphertext() {
    return ciphertext;
  }

  private static BigInt abs(final BigInt a, final BigInt n2) {
    final BigInt n2Half = n2.shiftRight(1);
    if (a.compareTo(n2Half) > 0) {
      return (n2.subtract(a).mod(n2));
    } else {
      return a.mod(n2);
    }
  }
}
