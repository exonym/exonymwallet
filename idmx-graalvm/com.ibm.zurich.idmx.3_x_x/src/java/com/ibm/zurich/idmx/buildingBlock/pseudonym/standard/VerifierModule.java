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
package com.ibm.zurich.idmx.buildingBlock.pseudonym.standard;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.ibm.zurich.idmx.buildingBlock.factory.BuildingBlockFactory;
import com.ibm.zurich.idmx.buildingBlock.helper.BaseForRepresentation;
import com.ibm.zurich.idmx.buildingBlock.helper.representation.pedersen.PedersenRepresentationBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.pseudonym.PseudonymBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.systemParameters.EcryptSystemParametersWrapper;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.ProofException;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.group.KnownOrderGroup;
import com.ibm.zurich.idmx.interfaces.util.group.KnownOrderGroupElement;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleVerifier;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkVerifierStateCollect;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkVerifierStateVerify;
import com.ibm.zurich.idmx.util.group.GroupFactoryImpl;
import com.ibm.zurich.idmx.zkModule.ZkModuleImpl;

import eu.abc4trust.xml.SystemParameters;

public class VerifierModule extends ZkModuleImpl implements ZkModuleVerifier {
  ZkModuleVerifier delegatee;

  private final KnownOrderGroupElement comm;

  // pseudonym assumed to be a byte[] corresponding to the BigInteger representation of the
  // pseudonym
  public VerifierModule(final PseudonymBuildingBlock parent, final String identifierOfModule,
                        final SystemParameters systemParameters, final URI scope, final byte[] pseudonym,
      final BuildingBlockFactory bbFactory, final Logger logger, final BigIntFactory bigIntFactory)
      throws ProofException, ConfigurationException {

    super(parent, identifierOfModule);

    final EcryptSystemParametersWrapper syspar = new EcryptSystemParametersWrapper(systemParameters);

    final KnownOrderGroup group =
        new GroupFactoryImpl().createPrimeOrderGroup(syspar.getDHModulus(),
            syspar.getDHSubgroupOrder());
    final KnownOrderGroupElement base1 = group.valueOf(syspar.getDHGenerator1());
    final KnownOrderGroupElement base2 = group.valueOf(syspar.getDHGenerator2());

    final List<BaseForRepresentation> basesForRep = new ArrayList<BaseForRepresentation>();
    final BaseForRepresentation baseForRep1 = BaseForRepresentation.deviceSecret(base1);
    final BaseForRepresentation baseForRep2 = BaseForRepresentation.managedAttribute(base2);
    basesForRep.add(baseForRep1);
    basesForRep.add(baseForRep2);

    comm = group.valueOf(parent.getPseudonymValueFromBytes(pseudonym));

    final PedersenRepresentationBuildingBlock pedersenBB =
        bbFactory.getBuildingBlockByClass(PedersenRepresentationBuildingBlock.class);
    delegatee =
        pedersenBB.getZkModuleVerifier(systemParameters, identifierOfModule + ":rep", basesForRep,
            comm, null, group);
  }

  @Override
  public void collectAttributesForVerify(final ZkVerifierStateCollect zkVerifier) throws ProofException,
      ConfigurationException {
    delegatee.collectAttributesForVerify(zkVerifier);

    zkVerifier.registerAttribute(identifierOfSecretAttribute(), true);
    zkVerifier
        .attributesAreEqual(identifierOfSecretAttribute(), delegatee.identifierOfAttribute(0));
  }

  @Override
  public boolean verify(final ZkVerifierStateVerify zkVerifier) throws ConfigurationException,
      ProofException {
    return delegatee.verify(zkVerifier);
  }

}
