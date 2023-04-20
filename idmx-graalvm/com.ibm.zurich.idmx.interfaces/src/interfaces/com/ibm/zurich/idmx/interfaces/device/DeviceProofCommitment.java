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

public interface DeviceProofCommitment {
  /**
   * Returns the T-Value for the proof of possession of the credential public key. The device
   * chooses r_x and (if applicable) r_v as getRandomizerSizeBytes()-bytes strings and returns T =
   * gd^{r_x} * gr^{r_v} (mod n) or C = gr^{r_x} (mod n), respectively. If there are multiple proofs
   * performed on a device, the same value r_x is use for all the proofs involving that device.
   * 
   * @param deviceUid
   * @param credentialUri
   * @return
   */
  public BigInteger getCommitmentForCredential(final URI deviceUid, final URI credentialUri);

  /**
   * Returns the T-Value for the proof of possession of the given scope exclusive pseudonym. The
   * device chooses r_x as a getRandomizerSizeBytes()-bytes string and returns T =
   * (hash(scope)^cofactor)^{r_x} (mod p). If there are multiple proofs performed on a device, the
   * same value r_x is use for all the proofs involving that device.
   * 
   * @param deviceUid
   * @param scope
   * @return
   */
  public BigInteger getCommitmentForScopeExclusivePseudonym(final URI deviceUid, final URI scope);

  /**
   * Returns the T-Value for the proof of possession of the public key of the given device. The
   * device chooses r_x as a getRandomizerSizeBytes()-bytes string and returns T = g^{r_x} (mod p).
   * If there are multiple proofs performed on a device, the same value r_x is use for all the
   * proofs involving that device.
   * 
   * @param deviceUid
   * @return
   */
  public BigInteger getCommitmentForPublicKey(final URI deviceUid);
}
