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

package com.ibm.zurich.idmx.buildingBlock.revocation.cl;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import com.ibm.zurich.idmix.abc4trust.facades.NonRevocationEvidenceFacade;
import com.ibm.zurich.idmx.buildingBlock.factory.BuildingBlockFactory;
import com.ibm.zurich.idmx.buildingBlock.helper.BaseForRepresentation;
import com.ibm.zurich.idmx.buildingBlock.helper.representation.damgardFujisaki.DamgardFujisakiRepresentationBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.revocationAuthorityKey.RevocationAuthorityPublicKeyBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.systemParameters.SystemParametersBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.systemParameters.EcryptSystemParametersWrapper;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.ProofException;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.RandomGeneration;
import com.ibm.zurich.idmx.interfaces.util.group.GroupFactory;
import com.ibm.zurich.idmx.interfaces.util.group.HiddenOrderGroup;
import com.ibm.zurich.idmx.interfaces.util.group.HiddenOrderGroupElement;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProver;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateCollect;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateFirstRound;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateInitialize;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateSecondRound;
import com.ibm.zurich.idmx.zkModule.ZkModuleImpl;

import eu.abc4trust.xml.PublicKey;
import eu.abc4trust.xml.SystemParameters;


class ProverModulePresentation extends ZkModuleImpl implements ZkModuleProver {

  private final EcryptSystemParametersWrapper spWrapper;
  private final ClRevocationAuthorityPublicKeyWrapper pkWrapper;
  private final ZkModuleProver df_ce;
  private final ZkModuleProver df_cr;
  private final ZkModuleProver df_v;
  private final ZkModuleProver df_1;
  private final List<ZkModuleProver> children;
  private final HiddenOrderGroup group;
  private final String attributeId;
  private final BigInt e, r1, r2, r3, r2e, r3e;
  private final HiddenOrderGroupElement Cu;


  public ProverModulePresentation(final ClRevocationBuildingBlock parent, final String moduleId,
                                  final String attributeId, final SystemParameters systemParameters, final PublicKey raPublicKey,
      final NonRevocationEvidenceFacade nreFacade, final ClRevocationStateWrapper revState,
      final BigIntFactory bigIntFactory, final GroupFactory groupFactory, final RandomGeneration randomGeneration,
      final BuildingBlockFactory bbFactory) throws ConfigurationException, ProofException {

    super(parent, moduleId);

    this.spWrapper = new EcryptSystemParametersWrapper(systemParameters);
    this.pkWrapper = new ClRevocationAuthorityPublicKeyWrapper(raPublicKey);
    this.attributeId = attributeId;

    this.children = new ArrayList<ZkModuleProver>();
    final SystemParametersBuildingBlock sp_bb =
        bbFactory.getBuildingBlockByClass(SystemParametersBuildingBlock.class);
    final RevocationAuthorityPublicKeyBuildingBlock rpk_bb =
        bbFactory.getBuildingBlockByClass(RevocationAuthorityPublicKeyBuildingBlock.class);
    final DamgardFujisakiRepresentationBuildingBlock df_bb =
        bbFactory.getBuildingBlockByClass(DamgardFujisakiRepresentationBuildingBlock.class);

    final ZkModuleProver zkp_sp = sp_bb.getZkModuleProver(moduleId + ":sp", systemParameters);
    final ZkModuleProver zkp_rpk =
        rpk_bb.getZkModuleProver(moduleId + ":rapk", systemParameters, raPublicKey);
    group = pkWrapper.getGroup(groupFactory);

    final HiddenOrderGroupElement g = group.valueOfNoCheck(pkWrapper.getBase(0));
    final HiddenOrderGroupElement h = group.valueOfNoCheck(pkWrapper.getBase(1));
    final HiddenOrderGroupElement u = group.valueOf(nreFacade.getNonRevocationEvidenceValue());
    final HiddenOrderGroupElement v = group.valueOf(revState.getAccumulatorValue());

    e = nreFacade.getRevocationHandleValue();
    r1 = group.createRandomIterationcounter(randomGeneration, 0);
    r2 = group.createRandomIterationcounter(randomGeneration, 0);
    r3 = group.createRandomIterationcounter(randomGeneration, 0);
    r2e = r2.multiply(e);
    r3e = r3.multiply(e);
    
    if (!u.multOp(e).equals(v)) {
      System.out.println("Incorrect value of accumulator witness.\n u = " + u.toBigInt() + "\n e = "
          + e + "\n v = " + v.toBigInt() + "\n u^e = " + u.multOp(e).toBigInt());
      throw new RuntimeException("Incorrect value of accumulator witness.\n u = " + u.toBigInt() + "\n e = "
          + e + "\n v = " + v.toBigInt() + "\n u^e = " + u.multOp(e));
    }

    // System.out.println("Value of accumulator witness.\n u = " + u + "\n e = " + e
    //    + "\n v = " + v + "\n u^e = " + u.multOp(e));

    // Cu = u * h^r2
    Cu = u.opMultOp(h, r2);

    // Ce = g^e * h^r1
    final List<BaseForRepresentation> bases_ce = new ArrayList<BaseForRepresentation>();
    bases_ce.add(BaseForRepresentation.managedAttribute(g));
    bases_ce.add(BaseForRepresentation.managedAttribute(h));

    // Cr = g^r2 * h^r3
    final List<BaseForRepresentation> bases_cr = new ArrayList<BaseForRepresentation>();
    bases_cr.add(BaseForRepresentation.managedAttribute(g));
    bases_cr.add(BaseForRepresentation.managedAttribute(h));

    // v = Cu^e * (1/h)^r2e
    final List<BaseForRepresentation> bases_v = new ArrayList<BaseForRepresentation>();
    bases_v.add(BaseForRepresentation.managedAttribute(Cu));
    bases_v.add(BaseForRepresentation.managedAttribute(h.invert()));

    // 1 = Cr^e * (1/g)^r2e * (1/h)^r3e
    final List<BaseForRepresentation> bases_1 = new ArrayList<BaseForRepresentation>();
    bases_1.add(BaseForRepresentation.managedAttribute(moduleId + ":cr:C"));
    bases_1.add(BaseForRepresentation.managedAttribute(g.invert()));
    bases_1.add(BaseForRepresentation.managedAttribute(h.invert()));

    df_ce =
        df_bb.getZkModuleProver(systemParameters, moduleId + ":ce", null, bases_ce, group, null,
            null, null, null);

    df_cr =
        df_bb.getZkModuleProver(systemParameters, moduleId + ":cr", null, bases_cr, group, null,
            null, null, null);

    df_v =
        df_bb.getZkModuleProver(systemParameters, moduleId + ":v", null, bases_v, group, v, null,
            null, null);

    final HiddenOrderGroupElement commitment_1 = group.neutralElement();
    df_1 =
        df_bb.getZkModuleProver(systemParameters, moduleId + ":1", null, bases_1, group,
            commitment_1, null, null, null);

    children.add(df_ce);
    children.add(df_cr);
    children.add(df_v);
    children.add(df_1);
    children.add(zkp_sp);
    children.add(zkp_rpk);
  }

  @Override
  public void initializeModule(final ZkProofStateInitialize zkBuilder) throws ConfigurationException {
    for (final ZkModuleProver child : children) {
      child.initializeModule(zkBuilder);
    }
    final int bitLengthR = group.getRandomIterationcounterLength(0);
    final int bitLengthRE = spWrapper.getAttributeLength() + bitLengthR;

    zkBuilder.registerAttribute(attributeId, false);
    zkBuilder.setValueOfAttribute(attributeId, e, null);

    // Ce = g^e * h^r1
    zkBuilder.attributesAreEqual(df_ce.identifierOfAttribute(0), attributeId);
    zkBuilder.setValueOfAttribute(df_ce.identifierOfAttribute(1), r1, null);
    zkBuilder.registerAttribute(df_ce.identifierOfAttribute(1), false, bitLengthR);

    // Cr = g^r2 * h^r3
    zkBuilder.setValueOfAttribute(df_cr.identifierOfAttribute(0), r2, null);
    zkBuilder.setValueOfAttribute(df_cr.identifierOfAttribute(1), r3, null);
    zkBuilder.registerAttribute(df_cr.identifierOfAttribute(0), false, bitLengthR);
    zkBuilder.registerAttribute(df_cr.identifierOfAttribute(1), false, bitLengthR);

    // v = Cu^e * (1/h)^r2e
    zkBuilder.attributesAreEqual(df_v.identifierOfAttribute(0), attributeId);
    zkBuilder.setValueOfAttribute(df_v.identifierOfAttribute(1), r2e, null);
    zkBuilder.registerAttribute(df_v.identifierOfAttribute(1), false, bitLengthRE);

    // 1 = Cr^e * (1/g)^r2e * (1/h)^r3e
    zkBuilder.attributesAreEqual(df_1.identifierOfAttribute(0), attributeId);
    zkBuilder.attributesAreEqual(df_1.identifierOfAttribute(1), df_v.identifierOfAttribute(1));
    zkBuilder.setValueOfAttribute(df_1.identifierOfAttribute(2), r3e, null);
    zkBuilder.registerAttribute(df_1.identifierOfAttribute(2), false, bitLengthRE);
  }

  @Override
  public void collectAttributesForProof(final ZkProofStateCollect zkBuilder) throws ConfigurationException {
    for (final ZkModuleProver child : children) {
      child.collectAttributesForProof(zkBuilder);
    }
  }

  @Override
  public void firstRound(final ZkProofStateFirstRound zkBuilder) throws ConfigurationException,
      ProofException {
    for (final ZkModuleProver child : children) {
      child.firstRound(zkBuilder);
    }
    zkBuilder.addDValue(getIdentifier() + ":Cu", Cu);
  }

  @Override
  public void secondRound(final ZkProofStateSecondRound zkBuilder) throws ConfigurationException {
    for (final ZkModuleProver child : children) {
      child.secondRound(zkBuilder);
    }
  }

}
