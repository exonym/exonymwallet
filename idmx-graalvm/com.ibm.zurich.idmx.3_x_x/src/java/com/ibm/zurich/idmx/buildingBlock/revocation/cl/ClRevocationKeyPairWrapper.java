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

package com.ibm.zurich.idmx.buildingBlock.revocation.cl;

import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.keypair.PublicKeyWrapper;
import com.ibm.zurich.idmx.keypair.SecretKeyWrapper;
import com.ibm.zurich.idmx.keypair.ra.RevocationAuthorityKeyPairWrapper;

import eu.abc4trust.xml.KeyPair;

class ClRevocationKeyPairWrapper extends RevocationAuthorityKeyPairWrapper {

  public ClRevocationKeyPairWrapper() {
    super();
  }

  public ClRevocationKeyPairWrapper(final KeyPair keyPair) throws ConfigurationException {
    super(keyPair);
  }

  @Override
  public SecretKeyWrapper getSecretKeyWrapper() {
    return new ClRevocationSecretKeyWrapper(getSecretKey());
  }

  @Override
  public PublicKeyWrapper getPublicKeyWrapper() {
    return new ClRevocationAuthorityPublicKeyWrapper(getPublicKey());
  }

  public ClRevocationSecretKeyWrapper getCLSecretKeyWrapper() {
    return (ClRevocationSecretKeyWrapper) this.getSecretKeyWrapper();
  }

  public ClRevocationAuthorityPublicKeyWrapper getCLPublicKeyWrapper() throws ConfigurationException {
    return (ClRevocationAuthorityPublicKeyWrapper) this.getPublicKeyWrapper();
  }

}
