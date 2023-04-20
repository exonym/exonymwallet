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

package com.ibm.zurich.idmx.keypair;

import java.net.URI;

import com.ibm.zurich.idmx.jaxb.ParameterListHelper;

import eu.abc4trust.xml.PrivateKey;

/**
 * 
 */
public abstract class SecretKeyWrapper extends KeyWrapper {

  protected final PrivateKey secretKey;

  public SecretKeyWrapper(final PrivateKey secretKey) {
    super();
    this.secretKey = secretKey;
    this.listOfParameters = this.secretKey.getParameter();
    this.parameterListHelper = new ParameterListHelper(this.secretKey.getParameter());
  }

  @Override
  public URI getPublicKeyId() {
    return secretKey.getPublicKeyId();
  }

  @Override
  public void setPublicKeyId(final URI publicKeyId) {
    secretKey.setPublicKeyId(publicKeyId);
  }

  public PrivateKey getSecretKey() {
    return secretKey;
  }
}
