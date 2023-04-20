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
import com.ibm.zurich.idmx.parameters.issuer.IssuerPublicKeyTemplateWrapper;

import eu.abc4trust.xml.IssuerPublicKeyTemplate;

class BrandsKeyPairTemplateWrapper extends IssuerPublicKeyTemplateWrapper {
  public BrandsKeyPairTemplateWrapper() {
    super();
  }

  public BrandsKeyPairTemplateWrapper(final IssuerPublicKeyTemplate issuerPublicKeyTemplate) {
    super(issuerPublicKeyTemplate);
  }

  public static final String NUMBER_OF_TOKENS_LABEL = "uprove:tokens";

  public void setNumberOfUProveTokens(final int tokens) {
    this.setParameter(NUMBER_OF_TOKENS_LABEL, tokens);
  }

  public int getNumberOfUProveTokens() throws ConfigurationException {
    return (Integer) this.getParameter(NUMBER_OF_TOKENS_LABEL);
  }
}
