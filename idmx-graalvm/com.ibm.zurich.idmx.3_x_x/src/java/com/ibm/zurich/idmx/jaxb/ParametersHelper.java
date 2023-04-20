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

package com.ibm.zurich.idmx.jaxb;

import com.ibm.zurich.idmx.exception.ConfigurationException;

import eu.abc4trust.xml.Parameter;

/**
 * 
 */
public abstract class ParametersHelper {

  protected ParameterListHelper parameterListHelper;

  protected abstract String createParameterUriBasedOnParameterName(String parameterName);

  /**
   * Creates a parameter and adds it to the list of parameters accessed though the parameter list
   * helper.
   */
  protected void setParameter(final String parameterName, final Object parameterValue) {

    final String extendedParameterName = createParameterUriBasedOnParameterName(parameterName);
    final Parameter parameter =
        ParameterListHelper.createParameter(extendedParameterName, parameterValue);

    final int parameterIndex = parameterListHelper.getParameterIndexUsingParameterName(extendedParameterName);
    if (parameterValue != null) {
      if (parameterIndex != -1) {
        parameterListHelper.getParameterList().set(parameterIndex, parameter);
      } else {
        parameterListHelper.getParameterList().add(parameter);
      }
    } else {
      if (parameterIndex != -1) {
        parameterListHelper.getParameterList().remove(parameterIndex);
      } else {
        // Nothing to do
      }
    }
  }

  /**
   * Retrieves a parameter from the list of parameters using the given name.
   * 
   * @throws ConfigurationException
   */
  protected Object getParameter(final String parameterName) throws ConfigurationException {
    final String extendedParameterName = createParameterUriBasedOnParameterName(parameterName);
    return parameterListHelper.getParameterValueUsingParameterName(extendedParameterName);
  }

  protected boolean hasParameter(final String parameterName) {
    String extendedParameterName = createParameterUriBasedOnParameterName(parameterName);
    return parameterListHelper.parameterIsInList(extendedParameterName);
  }
}
