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
package com.ibm.zurich.idmx.interfaces.util;

public class DummyTestVectorHelper implements TestVectorHelper {

  @Override
  public boolean isActive() {
    return false;
  }

  @Override
  public BigInt getValueAsBigInt(final String valueName) {
    throw new RuntimeException("Getter not implemented for DummyTestVectorHelper.");
  }

  @Override
  public byte[] getValueAsBytes(final String valueName) {
    throw new RuntimeException("Getter not implemented for DummyTestVectorHelper.");
  }

  @Override
  public void checkValue(final BigInt value, final String valueName) {
    // Do nothing
  }

  @Override
  public void checkValue(final byte[] value, final String valueName) {
    // Do nothing
  }

  @Override
  public boolean finalizeTest() {
    return true;
  }

  @Override
  public String getValueAsString(final String valueName) {
    throw new RuntimeException("Getter not implemented for DummyTestVectorHelper.");
  }

  @Override
  public boolean valueExists(final String valueName) {
    return false;
  }

  @Override
  public boolean isPresentation() {
    return false;
  }

}
