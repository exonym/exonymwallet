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

package com.ibm.zurich.idmx.buildingBlock.setMembership.cg;

import java.util.List;

import com.ibm.zurich.idmx.buildingBlock.factory.BuildingBlockFactory;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.ProofException;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleVerifier;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkVerifierStateCollect;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkVerifierStateVerify;
import com.ibm.zurich.idmx.zkModule.ZkModuleImpl;

public class VerifierModule extends ZkModuleImpl implements ZkModuleVerifier {

  private final String attributeId;
  @SuppressWarnings("unused")
  private final List<BigInt> allowedValues;
  @SuppressWarnings("unused")
  private final BuildingBlockFactory buildingBlockFactory;

  public VerifierModule(final CgAttributeSetMembershipBuildingBlock parent, final String identifierOfModule,
                        final String attributeId, final List<BigInt> allowedValues, final BuildingBlockFactory buildingBlockFactory) {

    super(parent, identifierOfModule);

    this.attributeId = attributeId;
    this.allowedValues = allowedValues;
    this.buildingBlockFactory = buildingBlockFactory;
  }


  @Override
  public void collectAttributesForVerify(final ZkVerifierStateCollect zkVerifier) throws ProofException {
    zkVerifier.registerAttribute(attributeId, false);
    // TODO
  }

  @Override
  public boolean verify(final ZkVerifierStateVerify zkVerifier) throws ConfigurationException,
      ProofException {
    // TODO
    return false;
  }

}
