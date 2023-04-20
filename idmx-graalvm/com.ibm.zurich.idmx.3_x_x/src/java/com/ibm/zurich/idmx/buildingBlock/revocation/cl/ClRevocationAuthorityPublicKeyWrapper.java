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
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.group.GroupFactory;
import com.ibm.zurich.idmx.interfaces.util.group.HiddenOrderGroup;
import com.ibm.zurich.idmx.keypair.ra.RevocationAuthorityPublicKeyWrapper;

import eu.abc4trust.xml.PublicKey;

public class ClRevocationAuthorityPublicKeyWrapper extends RevocationAuthorityPublicKeyWrapper {

  // Public key element names
  private static final String MODULUS_NAME = "rsaModulus";

  public ClRevocationAuthorityPublicKeyWrapper(final PublicKey publicKey) {
    super(publicKey);
  }


  public void setModulus(final BigInt parameterValue) {
    setParameter(MODULUS_NAME, parameterValue);
  }

  public BigInt getModulus() throws ConfigurationException {
    return (BigInt) getParameter(MODULUS_NAME);
  }
  
  public HiddenOrderGroup getGroup(final GroupFactory gf) throws ConfigurationException {
    return getGroup(gf, getModulus());
  }
  
  public static HiddenOrderGroup getGroup(final GroupFactory gf, final BigInt modulus) throws ConfigurationException {
    return gf.createSignedQuadraticResiduesGroup(modulus);
  }
}
