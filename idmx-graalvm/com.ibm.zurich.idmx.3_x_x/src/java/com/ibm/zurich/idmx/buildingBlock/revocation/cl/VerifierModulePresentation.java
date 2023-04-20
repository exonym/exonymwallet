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

import java.util.ArrayList;
import java.util.List;

import com.ibm.zurich.idmx.buildingBlock.factory.BuildingBlockFactory;
import com.ibm.zurich.idmx.buildingBlock.helper.BaseForRepresentation;
import com.ibm.zurich.idmx.buildingBlock.helper.representation.damgardFujisaki.DamgardFujisakiRepresentationBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.revocationAuthorityKey.RevocationAuthorityPublicKeyBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.systemParameters.SystemParametersBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.systemParameters.EcryptSystemParametersWrapper;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.ProofException;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.RandomGeneration;
import com.ibm.zurich.idmx.interfaces.util.group.GroupFactory;
import com.ibm.zurich.idmx.interfaces.util.group.HiddenOrderGroup;
import com.ibm.zurich.idmx.interfaces.util.group.HiddenOrderGroupElement;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleVerifier;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkVerifierStateCollect;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkVerifierStateVerify;
import com.ibm.zurich.idmx.zkModule.ZkModuleImpl;

import eu.abc4trust.xml.PublicKey;
import eu.abc4trust.xml.SystemParameters;

class VerifierModulePresentation extends ZkModuleImpl implements ZkModuleVerifier {

  private final EcryptSystemParametersWrapper spWrapper;
  private final ClRevocationAuthorityPublicKeyWrapper pkWrapper;
  private final ZkModuleVerifier df_ce;
  private final ZkModuleVerifier df_cr;
  private final ZkModuleVerifier df_v;
  private final ZkModuleVerifier df_1;
  private final List<ZkModuleVerifier> children;
  private final HiddenOrderGroup group;
  private final String attributeId;

  public VerifierModulePresentation(final ClRevocationBuildingBlock parent, final String moduleId,
                                    final String attributeId, final SystemParameters systemParameters, final PublicKey raPublicKey,
      final ClRevocationStateWrapper revState, final BigIntFactory bigIntFactory, final GroupFactory groupFactory,
      final RandomGeneration randomGeneration, final BuildingBlockFactory bbFactory)
      throws ConfigurationException, ProofException {
    super(parent, moduleId);

    this.spWrapper = new EcryptSystemParametersWrapper(systemParameters);
    this.pkWrapper = new ClRevocationAuthorityPublicKeyWrapper(raPublicKey);
    this.attributeId = attributeId;

    this.children = new ArrayList<ZkModuleVerifier>();
    final SystemParametersBuildingBlock sp_bb =
        bbFactory.getBuildingBlockByClass(SystemParametersBuildingBlock.class);
    final RevocationAuthorityPublicKeyBuildingBlock rpk_bb =
        bbFactory.getBuildingBlockByClass(RevocationAuthorityPublicKeyBuildingBlock.class);
    final DamgardFujisakiRepresentationBuildingBlock df_bb =
        bbFactory.getBuildingBlockByClass(DamgardFujisakiRepresentationBuildingBlock.class);

    final ZkModuleVerifier zkv_sp = sp_bb.getZkModuleVerifier(moduleId + ":sp", systemParameters);
    final ZkModuleVerifier zkv_rpk =
        rpk_bb.getZkModuleVerifier(moduleId + ":rapk", systemParameters, raPublicKey);
    group = pkWrapper.getGroup(groupFactory);

    final HiddenOrderGroupElement g = group.valueOfNoCheck(pkWrapper.getBase(0));
    final HiddenOrderGroupElement h = group.valueOfNoCheck(pkWrapper.getBase(1));
    final HiddenOrderGroupElement v = group.valueOf(revState.getAccumulatorValue());

    // System.out.println("value of accumulator.\n v = " + v);


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
    bases_v.add(BaseForRepresentation.managedAttribute(getIdentifier() + ":Cu"));
    bases_v.add(BaseForRepresentation.managedAttribute(h.invert()));

    // 1 = Cr^e * (1/g)^r2e * (1/h)^r3e
    final List<BaseForRepresentation> bases_1 = new ArrayList<BaseForRepresentation>();
    bases_1.add(BaseForRepresentation.managedAttribute(moduleId + ":cr:C"));
    bases_1.add(BaseForRepresentation.managedAttribute(g.invert()));
    bases_1.add(BaseForRepresentation.managedAttribute(h.invert()));

    df_ce =
        df_bb.getZkModuleVerifier(systemParameters, moduleId + ":ce", bases_ce, null, null, group);

    df_cr =
        df_bb.getZkModuleVerifier(systemParameters, moduleId + ":cr", bases_cr, null, null, group);

    df_v = df_bb.getZkModuleVerifier(systemParameters, moduleId + ":v", bases_v, v, null, group);

    final HiddenOrderGroupElement commitment_1 = group.neutralElement();
    df_1 =
        df_bb.getZkModuleVerifier(systemParameters, moduleId + ":1", bases_1, commitment_1, null,
            group);

    children.add(df_ce);
    children.add(df_cr);
    children.add(df_v);
    children.add(df_1);
    children.add(zkv_sp);
    children.add(zkv_rpk);
  }

  @Override
  public void collectAttributesForVerify(final ZkVerifierStateCollect zkVerifier) throws ProofException,
      ConfigurationException {
    for (final ZkModuleVerifier child : children) {
      child.collectAttributesForVerify(zkVerifier);
    }

    final int bitLengthR = group.getRandomIterationcounterLength(0);
    final int bitLengthRE = spWrapper.getAttributeLength() + bitLengthR;

    zkVerifier.registerAttribute(attributeId, false);

    // Ce = g^e * h^r1
    zkVerifier.attributesAreEqual(df_ce.identifierOfAttribute(0), attributeId);
    zkVerifier.registerAttribute(df_ce.identifierOfAttribute(1), false, bitLengthR);

    // Cr = g^r2 * h^r3
    zkVerifier.registerAttribute(df_cr.identifierOfAttribute(0), false, bitLengthR);
    zkVerifier.registerAttribute(df_cr.identifierOfAttribute(1), false, bitLengthR);

    // v = Cu^e * (1/h)^r2e
    zkVerifier.attributesAreEqual(df_v.identifierOfAttribute(0), attributeId);
    zkVerifier.registerAttribute(df_v.identifierOfAttribute(1), false, bitLengthRE);

    // 1 = Cr^e * (1/g)^r2e * (1/h)^r3e
    zkVerifier.attributesAreEqual(df_1.identifierOfAttribute(0), attributeId);
    zkVerifier.attributesAreEqual(df_1.identifierOfAttribute(1), df_v.identifierOfAttribute(1));
    zkVerifier.registerAttribute(df_1.identifierOfAttribute(2), false, bitLengthRE);
  }

  @Override
  public boolean verify(final ZkVerifierStateVerify zkVerifier) throws ConfigurationException,
      ProofException {
    boolean ok = true;
    for (final ZkModuleVerifier child : children) {
      ok = ok && child.verify(zkVerifier);
    }
    return ok;
  }


}
