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

package com.ibm.zurich.idmx.exception;

import java.net.URI;

/**
 * Indicates that there are not enough signature tokens in a given credential.
 */
public class NotEnoughTokensException extends Exception {

  private final URI credentialUri;
  private static final long serialVersionUID = -1096434715323288272L;

  public NotEnoughTokensException(final URI credentialUri) {
    super("The credential " + credentialUri + " doesn't have enough tokens");
    this.credentialUri = credentialUri;
  }
  
  public URI getCredentialUri() {
    return credentialUri;
  }
  
}
