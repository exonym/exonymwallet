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

import com.ibm.zurich.idmx.interfaces.device.DeviceProofSpecification;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ResidueClass;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateCollect;

class ZkBuilderProverCollect implements ZkProofStateCollect {

  private final StatePerModule state;
  private final StateForProver proverState;
  private final DeviceProofSpecification deviceSpec;


  public ZkBuilderProverCollect(final StatePerModule state, final StateForProver proverState,
		  final DeviceProofSpecification deviceSpec) {
    this.state = state;
    this.proverState = proverState;
    this.deviceSpec = deviceSpec;
  }


  @Override
  public boolean isRevealedAttribute(final String attributeName) {
    state.assertRegistered(attributeName);
    return proverState.isRevealedAttribute(attributeName);
  }


  @Override
  public BigInt getValueOfAttribute(final String attributeName) {
    state.assertRegistered(attributeName);
    state.assertAccessAllowed(attributeName);
    return proverState.getValueOfAttribute(attributeName);
  }


  @Override
  public void setValueOfAttribute(final String attributeName, final BigInt attributeValue, ResidueClass rc) {
    if(rc == null) {
      rc = ResidueClass.UNSPECIFIED;
    }
    state.assertRegistered(attributeName);
    state.allowAccessToAttribute(attributeName);
    proverState.setAttributeValue(attributeName, attributeValue);
    proverState.mergeResidueClass(attributeName, rc);
  }


  @Override
  public DeviceProofSpecification getDeviceProofSpecification() {
    return deviceSpec;
  }


  @Override
  public ResidueClass getResidueClass(String attributeName) {
    state.assertRegistered(attributeName);
    state.assertAccessAllowed(attributeName);
    ResidueClass value = proverState.getResidueClass(attributeName);
    return value;
  }
}
