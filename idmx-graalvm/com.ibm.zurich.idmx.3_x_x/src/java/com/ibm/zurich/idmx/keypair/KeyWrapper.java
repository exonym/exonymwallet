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

import java.util.List;

import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.interfaces.keypair.KeyWrapperInterface;
import com.ibm.zurich.idmx.jaxb.ParametersHelper;

import eu.abc4trust.xml.Parameter;

/**
 * 
 */
public abstract class KeyWrapper extends ParametersHelper implements KeyWrapperInterface {

  protected List<Parameter> listOfParameters;

  @Override
  public Object getParameter(final String parameterName) throws ConfigurationException {

    // String parameterUri = createParameterUriBasedOnParameterName(parameterName);
    // return super.getParameter(parameterUri);
    return super.getParameter(parameterName);
  }

  @Override
  public boolean hasParameter(final String parameterName) {
    // String parameterUri = createParameterUriBasedOnParameterName(parameterName);
    // return super.hasParameter(parameterUri);
    return super.hasParameter(parameterName);

  }

  @Override
  public void setParameter(final String parameterName, final Object parameterValue) {

    // String parameterUri = createParameterUriBasedOnParameterName(parameterName);
    // super.setParameter(parameterUri, parameterValue);
    super.setParameter(parameterName, parameterValue);
  }

//  protected abstract String createParameterUriBasedOnParameterName(String parameterName);

}
