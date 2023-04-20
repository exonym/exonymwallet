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
package com.ibm.zurich.idmx.proofEngine.builderProver;

import java.util.List;

import com.ibm.zurich.idmx.exception.ProofException;
import com.ibm.zurich.idmx.interfaces.buildingBlock.structural.linearCombination.LinearCombination;
import com.ibm.zurich.idmx.interfaces.buildingBlock.structural.linearCombination.Term;
import com.ibm.zurich.idmx.interfaces.device.DeviceProofSpecification;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ResidueClass;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateInitialize;

class ZkBuilderProverInitialize implements ZkProofStateInitialize {

  private final StatePerModule state;
  private final StateForProver proverState;
  private final DeviceProofSpecification deviceSpec;

  public ZkBuilderProverInitialize(final StatePerModule state, final StateForProver proverState,
		  final DeviceProofSpecification deviceSpec) {
    this.state = state;
    this.proverState = proverState;
    this.deviceSpec = deviceSpec;
  }


  @Override
  public void registerAttribute(final String attributeName, final boolean isExternal) {
    proverState.registerAttribute(attributeName, isExternal);
    state.registerAttribute(attributeName);
  }


  @Override
  public void registerAttribute(final String attributeName, final boolean isExternal,
		  final int bitLength) {
    proverState.registerAttribute(attributeName, isExternal, bitLength);
    state.registerAttribute(attributeName);
  }

  @Override
  public void overrideRValueOfAttribute(final String attributeName, final BigInt rValue) {
    state.assertRegistered(attributeName);
    proverState.overrideRValueOfAttribute(attributeName, rValue);
  }


  @Override
  public void requiresAttributeValue(final String attributeName) {
    state.assertRegistered(attributeName);
    state.allowAccessToAttribute(attributeName);

    proverState.registerDemand(state.moduleName, attributeName);
  }


  @Override
  public void providesAttribute(final String attributeName) {
    state.assertRegistered(attributeName);

    proverState.registerSupply(state.moduleName, attributeName);
  }


  @Override
  public void attributeIsRevealed(final String attributeName) {
    state.assertRegistered(attributeName);
    proverState.attributeIsRevealed(attributeName);
  }


  @Override
  public void attributesAreEqual(final String attributeName1, final String attributeName2) {
    try {
      state.assertRegistered(attributeName1);
      state.assertRegistered(attributeName2);
      proverState.attributesAreEqual(attributeName1, attributeName2);
      state.localAttributesAreEqual(attributeName1, attributeName2);
    } catch (ProofException e) {
      throw new RuntimeException(e);
    }
  }



  @Override
  //TODO(ksa) side effect?
  public void setValueOfAttribute(final String attributeName, final BigInt attributeValue, final ResidueClass rc) {
    state.assertRegistered(attributeName);
    state.allowAccessToAttribute(attributeName);
    proverState.setAttributeValue(attributeName, attributeValue);
    if(rc == null) {
      proverState.mergeResidueClass(attributeName, ResidueClass.UNSPECIFIED);
    } else {
      proverState.mergeResidueClass(attributeName, rc);
    }
  }


  @Override
  public DeviceProofSpecification getDeviceProofSpecification() {
    return deviceSpec;
  }


  @Override
  public void attributeLinearCombination(final String attributeName, final BigInt constant,
		  final List<Term> terms) {
    state.assertRegistered(attributeName);
    for (final Term term : terms) {
      state.assertRegistered(term.attribute);
    }
    final LinearCombination linComb = new LinearCombination(attributeName, constant, terms);
    proverState.addLinearCombination(linComb);
  }


  @Override
  public void markAsSignatureBuildingBlock() {
    state.moveToFrontOfChallengeHashList();
  }

}
