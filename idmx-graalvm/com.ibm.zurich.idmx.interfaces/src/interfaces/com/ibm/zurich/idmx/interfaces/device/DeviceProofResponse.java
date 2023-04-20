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
package com.ibm.zurich.idmx.interfaces.device;

import java.math.BigInteger;
import java.net.URI;

public interface DeviceProofResponse {
  /**
   * Returns the S-value associated with the credential secret key (v).
   * Returns s_v = r_v - c * v, where c is the challenge.
   * @param deviceUid
   * @param credentialUri
   * @return
   */
  public BigInteger getResponseForCredentialSecretKey(final URI deviceUid, final URI credentialUri);
  
  /**
   * Returns the S-value associated with the device secret key (x).
   * Returns s_x = r_x - c * x, where c is the challenge.
   * @param deviceUid
   * @return
   */
  public BigInteger getResponseForDeviceSecretKey(final URI deviceUid);
}
