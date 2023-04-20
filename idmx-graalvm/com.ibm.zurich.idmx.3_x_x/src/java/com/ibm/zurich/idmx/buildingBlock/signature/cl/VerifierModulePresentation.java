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

import com.ibm.zurich.idmx.buildingBlock.factory.BuildingBlockFactory;
import com.ibm.zurich.idmx.buildingBlock.helper.BaseForRepresentation;
import com.ibm.zurich.idmx.buildingBlock.helper.representation.damgardFujisaki.DamgardFujisakiRepresentationBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.systemParameters.EcryptSystemParametersWrapper;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.ProofException;
import com.ibm.zurich.idmx.interfaces.buildingBlock.structural.linearCombination.Term;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.group.GroupFactory;
import com.ibm.zurich.idmx.interfaces.util.group.HiddenOrderGroup;
import com.ibm.zurich.idmx.interfaces.util.group.HiddenOrderGroupElement;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleVerifier;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ResidueClass;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkVerifierStateCollect;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkVerifierStateVerify;
import com.ibm.zurich.idmx.zkModule.ZkModuleImpl;

import eu.abc4trust.xml.PublicKey;
import eu.abc4trust.xml.SystemParameters;

class VerifierModulePresentation extends ZkModuleImpl implements ZkModuleVerifier {

  private final BigIntFactory bigIntFactory;
  // private final GroupFactory groupFactory;
  // private final BuildingBlockFactory bbFactory;

  private final EcryptSystemParametersWrapper spWrapper;
  private final ClPublicKeyWrapper pkWrapper;

  private final BigInt credentialSpecificationId;
  private final int numberOfAttributes;
  private final boolean device;
  private final HiddenOrderGroup group;
  private final ZkModuleVerifier df;
  private final int l_n;
  private final int l_e;
  private int lPrime_e;
  // private int l_H;
  // private int l_v;
  private final int l_stat;
  private final SystemParameters sp;
  private final List<BaseForRepresentation> bases;

  public VerifierModulePresentation(final ClSignatureBuildingBlock parent, final String identifierOfModule,
                                    final SystemParameters systemParameters, final PublicKey issuerPublicKey,
      final BigInt credentialSpecificationId, final int numberOfAttributes, final boolean externalDevice,
      final BigIntFactory bigIntFactory, final GroupFactory groupFactory, final BuildingBlockFactory bbFactory)
      throws ProofException, ConfigurationException {

    super(parent, identifierOfModule);

    this.sp = systemParameters;
    // this.bbFactory = bbFactory;
    this.bigIntFactory = bigIntFactory;
    // this.groupFactory = groupFactory;

    this.spWrapper = new EcryptSystemParametersWrapper(systemParameters);
    this.pkWrapper = new ClPublicKeyWrapper(issuerPublicKey);

    this.credentialSpecificationId = credentialSpecificationId;
    this.numberOfAttributes = numberOfAttributes;
    this.device = externalDevice;
    // setup DF-buildingblock
    this.group = groupFactory.createSignedQuadraticResiduesGroup(pkWrapper.getModulus());

    // Load relevant pk/sp elements
    this.l_n = spWrapper.getDHModulusLength();
    this.l_e = spWrapper.getL_e();
    this.lPrime_e = spWrapper.getLPrime_e();
    // this.l_v = spWrapper.getL_v();
    // this.l_H = spWrapper.getHashLength();
    this.l_stat = spWrapper.getStatisticalInd();
    final HiddenOrderGroupElement Z = group.valueOf(pkWrapper.getZ());

    // bases are: (A', R_0, (R_i), R_s, S)
    this.bases = new ArrayList<BaseForRepresentation>();
    final String APrime = identifierOfModule + ":rep:base:0";
    bases.add(BaseForRepresentation.managedAttribute(APrime));

    if (device) {
      final HiddenOrderGroupElement R0 = group.valueOf(pkWrapper.getRd());
      bases.add(BaseForRepresentation.deviceSecret(R0));
    }

    for (int i = 0; i < numberOfAttributes; i++) {
      final HiddenOrderGroupElement R_i = group.valueOfNoCheck(pkWrapper.getBase(i));
      bases.add(BaseForRepresentation.managedAttribute(R_i));
    }
    final HiddenOrderGroupElement Rs = group.valueOf(pkWrapper.getRt());
    bases.add(BaseForRepresentation.managedAttribute(Rs));
    final HiddenOrderGroupElement S = group.valueOf(pkWrapper.getS());
    if (device) {
      bases.add(BaseForRepresentation.managedAttribute(S));
    } else {
      bases.add(BaseForRepresentation.deviceRandomizer(S, null));
    }

    final DamgardFujisakiRepresentationBuildingBlock dfBB =
        bbFactory.getBuildingBlockByClass(DamgardFujisakiRepresentationBuildingBlock.class);
    this.df = dfBB.getZkModuleVerifier(sp, identifierOfModule + ":rep", bases, Z, null, group);
  }

  @Override
  public void collectAttributesForVerify(final ZkVerifierStateCollect zkVerifier) throws ProofException,
      ConfigurationException {
    df.collectAttributesForVerify(zkVerifier);
    
    String attributeNameForE = df.identifierOfAttribute(0);
    zkVerifier.registerAttribute(attributeNameForEPrime(), false, lPrime_e);
    zkVerifier.registerAttribute(attributeNameForE, false);
    
    BigInt twoToLe = bigIntFactory.one().shiftLeft(l_e - 1);
    zkVerifier.attributeLinearCombination(attributeNameForE, twoToLe, Collections.singletonList(new Term(attributeNameForEPrime(), bigIntFactory.one())));

    int attrBeforeMessages = 1;
    if (device) {
      zkVerifier.registerAttribute(df.identifierOfAttribute(1), true);
      attrBeforeMessages = 2;
      zkVerifier.registerAttribute(identifierOfSecretAttribute(), true);
      zkVerifier.attributesAreEqual(df.identifierOfAttribute(1), identifierOfSecretAttribute());
    }

    for (int i = attrBeforeMessages; i < numberOfAttributes + attrBeforeMessages; ++i) {
      zkVerifier.registerAttribute(df.identifierOfAttribute(i), false,
          spWrapper.getAttributeLength());
      zkVerifier.registerAttribute(identifierOfAttribute(i - attrBeforeMessages), false,
          spWrapper.getAttributeLength());
      zkVerifier.setResidueClass(identifierOfAttribute(i - attrBeforeMessages),
          ResidueClass.INTEGER_IN_RANGE);
      zkVerifier.attributesAreEqual(identifierOfAttribute(i - attrBeforeMessages),
          df.identifierOfAttribute(i));
    }
    zkVerifier.registerAttribute(df.identifierOfAttribute(numberOfAttributes + attrBeforeMessages),
        false);
    zkVerifier.registerAttribute(
        df.identifierOfAttribute(numberOfAttributes + attrBeforeMessages + 1), false, l_n + l_stat
            + l_e + 2);
  }

  @Override
  public boolean verify(final ZkVerifierStateVerify zkVerifier) throws ConfigurationException,
      ProofException {
    df.verify(zkVerifier);
    zkVerifier.checkNValue(identifierOfModule + ":credSpecId", credentialSpecificationId);
    
    return true;
  }

  private final String attributeNameForEPrime() {
    return getIdentifier() + ":eprime";
  }
}
