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
package com.ibm.zurich.idmx.device;

import java.net.URI;

public class CredentialUriOnDevice {
  private final URI deviceUid;
  private final URI credentialUri;

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((credentialUri == null) ? 0 : credentialUri.hashCode());
    result = prime * result + ((deviceUid == null) ? 0 : deviceUid.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    final CredentialUriOnDevice other = (CredentialUriOnDevice) obj;
    if (credentialUri == null) {
      if (other.credentialUri != null) return false;
    } else if (!credentialUri.equals(other.credentialUri)) return false;
    if (deviceUid == null) {
      if (other.deviceUid != null) return false;
    } else if (!deviceUid.equals(other.deviceUid)) return false;
    return true;
  }

  public CredentialUriOnDevice(final URI deviceUid, final URI credentialUri) {
    this.deviceUid = deviceUid;
    this.credentialUri = credentialUri;
  }

  public URI getDeviceUid() {
    return deviceUid;
  }

  public URI getCredentialUri() {
    return credentialUri;
  }
}
