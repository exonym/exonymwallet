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

package com.ibm.zurich.idmix.abc4trust;

import com.ibm.zurich.idmx.configuration.ErrorMessages;
import com.ibm.zurich.idmx.exception.ConfigurationException;

/**
 * 
 */
public class AbcUriConfigurator {

  private static final String XML_BASIC_URI = "urn:abc4trust:1.0:hashalgorithm:";

  // Non-instantiable Class
  private AbcUriConfigurator() {
    throw new AssertionError(ErrorMessages.nonInstantiationErrorMessage());
  }

  public static String prependBasicUri(final String suffix) {
    return XML_BASIC_URI + suffix;
  }

  public static String removeBasicUri(final String hashFunctionUri) throws ConfigurationException {
    if (hashFunctionUri.startsWith(XML_BASIC_URI)) {
      return hashFunctionUri.substring(XML_BASIC_URI.length());
    }
    throw new ConfigurationException(
        "Idmix: String does not start with the basic URI as indicated.");
  }

}
