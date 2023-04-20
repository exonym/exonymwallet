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

package com.ibm.zurich.idmx.util;

import java.net.URI;

import com.ibm.zurich.idmx.interfaces.configuration.Constants;

/**
 * 
 */
public class UriUtils implements Constants {

  /**
   * Concatenation of elements to form a URI of the form used in this library.
   * 
   * @param prefix
   * @param suffix
   * @return
   */
  public static URI concat(final String prefix, final String suffix) {
    return URI.create(prefix + URI_DELIMITER + suffix);
  }

  /**
   * Concatenation of elements to form a URI of the form used in this library.
   * 
   * @param prefix
   * @param suffix
   * @return
   */
  public static URI concat(final URI prefix, final String suffix) {
    return concat(prefix.toString(), suffix);
  }

  /**
   * Convenience method.
   * 
   * @param baseUri
   * @param implementationVersion
   * @param string
   * @return
   */
  public static URI concat(final String prefix, final String body, final String suffix) {
	final URI prefixNew = concat(prefix, body);
    return concat(prefixNew, suffix);
  }

  /**
   * Convenience method.
   * 
   * @param baseUri
   * @param implementationVersion
   * @param string
   * @return
   */
  public static URI concat(final URI prefix, final String body, final String suffix) {
    return concat(prefix.toString(), body, suffix);
  }

  public static URI concat(final URI prefix, final URI suffix) {
    return concat(prefix.toString(), suffix.toString());
  }

}
