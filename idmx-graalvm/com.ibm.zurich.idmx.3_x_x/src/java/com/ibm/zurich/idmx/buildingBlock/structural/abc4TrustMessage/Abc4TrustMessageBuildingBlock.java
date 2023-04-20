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

package com.ibm.zurich.idmx.buildingBlock.structural.abc4TrustMessage;

import com.ibm.zurich.idmx.buildingBlock.GeneralBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.message.MessageBuildingBlock;
import com.ibm.zurich.idmx.exception.SerializationException;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProver;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleVerifier;
import com.ibm.zurich.idmx.jaxb.JaxbHelperClass;

import eu.abc4trust.xml.Message;
import eu.abc4trust.xml.ObjectFactory;

public class Abc4TrustMessageBuildingBlock extends GeneralBuildingBlock {

  private final MessageBuildingBlock bb;

  public Abc4TrustMessageBuildingBlock() {
    this.bb = new MessageBuildingBlock();
  }

  @Override
  protected String getBuildingBlockIdSuffix() {
    return "s-abc4trust-m-c14n";
  }

  @Override
  protected String getImplementationIdSuffix() {
    return "s-abc4trust-m-c14n";
  }

  private byte[] messageToBytes(final Message message) {
    try {
      return JaxbHelperClass.canonicalXml(new ObjectFactory().createMessage(message));
    } catch (final SerializationException e) {
      throw new RuntimeException(e);
    }
  }

  public ZkModuleProver getZkModuleProver(final String identifierOfModule, final Message message) {
    return bb.getZkModuleProver(identifierOfModule, messageToBytes(message));
  }

  public ZkModuleVerifier getZkModuleVerifier(final String identifierOfModule, final Message message) {
    return bb.getZkModuleVerifier(identifierOfModule, messageToBytes(message));
  }
}
