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

package com.ibm.zurich.idmx.jaxb.wrapper;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBIntrospector;

import com.ibm.zurich.idmx.jaxb.ParameterListHelper;
import com.ibm.zurich.idmx.jaxb.ParametersHelper;

import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.SignatureToken;

public class SignatureTokenWrapper extends ParametersHelper {

  private final JAXBElement<SignatureToken> signatureToken;

  public SignatureTokenWrapper() {
    this(new ObjectFactory().createSignatureToken());
  }

  public SignatureTokenWrapper(final SignatureToken value) {
    this.signatureToken = new ObjectFactory().createSignatureToken(value);
    this.parameterListHelper = new ParameterListHelper(getSignatureToken().getParameter());
  }


  @Override
  protected String createParameterUriBasedOnParameterName(final String parameterName) {
    return parameterName;
  }
  
  
  public SignatureToken getSignatureToken() {
    return (SignatureToken) JAXBIntrospector.getValue(signatureToken);
  }
}
