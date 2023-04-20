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
package com.ibm.zurich.idmx.buildingBlock.inspector.cs;

import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.keypair.PublicKeyWrapper;
import com.ibm.zurich.idmx.keypair.SecretKeyWrapper;
import com.ibm.zurich.idmx.keypair.inspector.InspectorKeyPairWrapper;

import eu.abc4trust.xml.KeyPair;

public class CsKeyPairWrapper extends InspectorKeyPairWrapper {

  public CsKeyPairWrapper() {
    super();
  }

  public CsKeyPairWrapper(final KeyPair issuerKeyPair) throws ConfigurationException {
    super(issuerKeyPair);
  }

  @Override
  public SecretKeyWrapper getSecretKeyWrapper() {
    return new CsSecretKeyWrapper(getSecretKey());
  }

  @Override
  public PublicKeyWrapper getPublicKeyWrapper() {
    return new CsPublicKeyWrapper(getPublicKey());
  }

  public CsSecretKeyWrapper getCSSecretKeyWrapper() {
    return (CsSecretKeyWrapper) this.getSecretKeyWrapper();
  }

  public CsPublicKeyWrapper getCSPublicKeyWrapper() throws ConfigurationException {
    return (CsPublicKeyWrapper) this.getPublicKeyWrapper();
  }
}
