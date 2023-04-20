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

package com.ibm.zurich.idmx.buildingBlock.signature.uprove;

import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.keypair.issuer.IssuerSecretKeyWrapper;

import eu.abc4trust.xml.PrivateKey;

public class BrandsSecretKeyWrapper extends IssuerSecretKeyWrapper {

  public BrandsSecretKeyWrapper(final PrivateKey privateKey) {
    super(privateKey);
  }

  private static final String UPROVE_Y0 = "uprove:y0";

  /**
   * Getter for private key y0.
   */
  public BigInt getY0() throws ConfigurationException {
    return (BigInt) getParameter(UPROVE_Y0);
  }

  /**
   * Setter for private key y0.
   */
  public void setY0(final BigInt y0) throws ConfigurationException {
    setParameter(UPROVE_Y0, y0);
  }

}
