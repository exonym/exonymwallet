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

import com.ibm.zurich.idmx.interfaces.device.DeviceProofCommitment;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.group.GroupElement;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ResidueClass;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateFirstRound;

class ZkBuilderProverFirst implements ZkProofStateFirstRound {

  private final StatePerModule state;
  private final StateForProver proverState;
  private final DeviceProofCommitment deviceCom;

  public ZkBuilderProverFirst(final StatePerModule state, final StateForProver proverState,
		  final DeviceProofCommitment deviceCom) {
    this.state = state;
    this.proverState = proverState;
    this.deviceCom = deviceCom;
  }


  @Override
  public boolean isRevealedAttribute(final String attributeName) {
    state.assertRegistered(attributeName);
    return proverState.isRevealedAttribute(attributeName);
  }


  @Override
  public boolean isValueOfAttributeAvailable(final String attributeName) {
    state.assertRegistered(attributeName);
    boolean available =
        proverState.isValueOfAttributeAvailable(attributeName)
            && state.isAccessAllowed(attributeName);
    return available;
  }


  @Override
  public BigInt getValueOfAttribute(final String attributeName) {
    state.assertRegistered(attributeName);
    state.assertAccessAllowed(attributeName);
    return proverState.getValueOfAttribute(attributeName);
  }


  @Override
  public BigInt getRValueOfAttribute(final String attributeName) {
    state.assertRegistered(attributeName);
    return proverState.getRValueOfAttribute(attributeName);
  }


  @Override
  public GroupElement<?, ?, ?> getDValueAsGroupElement(final String name) {
    return state.getDValueAsGroupElement(name);
  }


  @Override
  public byte[] getDValueAsObject(final String name) {
    return state.getDValueAsByteArray(name);
  }


  @Override
  public BigInt getDValueAsInteger(final String name) {
    return state.getDValueAsInteger(name);
  }


  @Override
  public void addDValue(final String name, final GroupElement<?, ?, ?> value) {
    state.addDValue(name, value);
  }


  @Override
  public void addDValue(final String name, final BigInt value) {
    state.addDValue(name, value);
  }


  @Override
  public void addDValue(final String name, final byte[] value, final byte[] hashContribution) {
    state.addDValue(name, value, hashContribution);
  }


  @Override
  public void addNValue(final String name, final byte[] hashContribution) {
    state.addNValue(name, hashContribution);
  }


  @Override
  public void addNValue(final String name, final BigInt value) {
    state.addNValue(name, value);
  }


  @Override
  public void addNValue(final String name, final GroupElement<?, ?, ?> value) {
    state.addNValue(name, value);
  }


  @Override
  public void addTValue(final String name, final GroupElement<?, ?, ?> tValue) {
    state.addTValue(name, tValue);
  }


  @Override
  public void setHashContributionOfBuildingBlock(final byte[] hashContribution) {
    state.setHashContribution(hashContribution);
  }


  @Override
  public void addDValue(final String name, final byte[] value) {
    addDValue(name, value, value);
  }


  @Override
  public DeviceProofCommitment getDeviceProofCommitment() {
    return deviceCom;
  }
  
  @Override
  public ResidueClass getResidueClass(String attributeName) {
    state.assertRegistered(attributeName);
    state.assertAccessAllowed(attributeName);
    ResidueClass value = proverState.getResidueClass(attributeName);
    return value;
  }

}
