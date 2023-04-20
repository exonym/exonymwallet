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

package com.ibm.zurich.idmx.zkModule;

import com.ibm.zurich.idmx.buildingBlock.GeneralBuildingBlock;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModule;

/**
 * 
 */
public abstract class ZkModuleImpl implements ZkModule {

  //TODO private vs. protected here
  private final GeneralBuildingBlock parent;
  protected final String identifierOfModule;

  // TODO move those fields if creating a new hierarchy layer with the identifierOfAttribute()
  // method
  //
  // protected final GroupFactory groupFactory;
  // protected final BigIntFactory bigIntFactory;
  // protected final RandomGeneration randomGeneration;

  public ZkModuleImpl(final GeneralBuildingBlock parent, final String identifierOfModule) {
    this.parent = parent;
    this.identifierOfModule = identifierOfModule;
  }

  @Override
  public String getIdentifier() {
    return identifierOfModule;
  }

  @Override
  public String identifierOfAttribute(final int i) {
    return getIdentifier() + ":" + Integer.toString(i);
  }

  @Override
  public String identifierOfSecretAttribute() {
    return getIdentifier() + ":secret";
  }

  @Override
  public String getBuildingBlockId() {
    return parent.getBuildingBlockId().toString();
  }

  @Override
  public String getImplementationId() throws ConfigurationException {
    return parent.getImplementationId().toString();
  }

  @Override
  public String identifierOfCommitment() {
    return getIdentifier() + ":C";
  }
}
