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

import java.net.URI;

public interface DeviceProofSpecification {
  /**
   * Ask the secrets manager to perform a proof of possession of the device secret key and (if
   * applicable) the credential secret key in the credential public key of the given device.
   * PoK{(x,v): C = gd^x * gr^v (mod n)} or PoK{(x): C = gr^x (mod n)}.
   * 
   * @param deviceId
   * @param credentialUri
   */
  public void addCredentialProof(final URI deviceId, final URI credentialUri);

  /**
   * Ask the secrets manager to perform a proof of possession of the device secret key in the scope
   * exclusive pseudonym of the given scope on the given device. PoK{(x): P =
   * (hash(scope)^cofactor)^x (mod p)}
   * 
   * @param deviceId
   * @param scope
   */
  public void addScopeExclusivePseudonymProof(final URI deviceId, final URI scope);

  /**
   * Ask the secrets manager to perform a proof of possession of the device secret key in the device
   * public key of the given device. PoK{(x): D = g^x (mod p)}
   * 
   * @param deviceId
   */
  public void addPublicKeyProof(final URI deviceId);
}
