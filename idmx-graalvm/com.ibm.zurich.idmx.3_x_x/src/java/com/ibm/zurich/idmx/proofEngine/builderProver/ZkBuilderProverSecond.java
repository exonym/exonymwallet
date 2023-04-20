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

import com.ibm.zurich.idmx.interfaces.device.DeviceProofResponse;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.group.GroupElement;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ResidueClass;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateSecondRound;

class ZkBuilderProverSecond implements ZkProofStateSecondRound {

  private final StatePerModule state;
  private final StateForProver proverState;
  private final DeviceProofResponse deviceResponse;


  public ZkBuilderProverSecond(final StatePerModule state, final StateForProver proverState,
		  final DeviceProofResponse deviceResponse) {
    this.state = state;
    this.proverState = proverState;
    this.deviceResponse = deviceResponse;
  }


  @Override
  public BigInt getChallenge() {
    return proverState.getChallenge();
  }


  @Override
  public int getPositionInHashContributionsList() {
    return state.positionInHash;
  }


  @Override
  public boolean isRevealedAttribute(final String attributeName) {
    state.assertRegistered(attributeName);
    return proverState.isRevealedAttribute(attributeName);
  }


  @Override
  public boolean isValueOfAttributeAvailable(final String attributeName) {
    state.assertRegistered(attributeName);
    return proverState.isValueOfAttributeAvailable(attributeName);
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
  public GroupElement<?,?,?> getDValueAsGroupElement(final String name) {
    return state.getDValueAsGroupElement(name);
  }


  @Override
  public BigInt getDValueAsInteger(final String name) {
    return state.getDValueAsInteger(name);
  }


  @Override
  public byte[] getDValueAsObject(final String name) {
    return state.getDValueAsByteArray(name);
  }


  @Override
  public void setSValueOfExternalAttribute(final String attributeName, final BigInt sValue) {
    state.assertRegistered(attributeName);
    proverState.setSValueOfExternalAttribute(attributeName, sValue);
  }


  @Override
  public void addSValue(final String attributeName, final BigInt sValue) {
    if (proverState.isRegisteredAttribute(attributeName)) {
      throw new RuntimeException(
          "You must not provide SValues for regular attributes. (Use setSValueOfExternalAttribute() for external attributes.)");
    }
    state.addSValue(attributeName, sValue);
  }


  @Override
  public void addSValue(final String attributeName, final byte[] sValue) {
    if (proverState.isRegisteredAttribute(attributeName)) {
      throw new RuntimeException(
          "You must not provide SValues for regular attributes. (Use setSValueOfExternalAttribute() for external attributes.)");
    }
    state.addSValue(attributeName, sValue);
  }


  @Override
  public DeviceProofResponse getDeviceProofResponse() {
    return deviceResponse;
  }
  
  @Override
  public ResidueClass getResidueClass(String attributeName) {
    state.assertRegistered(attributeName);
    state.assertAccessAllowed(attributeName);
    ResidueClass value = proverState.getResidueClass(attributeName);
    return value;
  }

}
