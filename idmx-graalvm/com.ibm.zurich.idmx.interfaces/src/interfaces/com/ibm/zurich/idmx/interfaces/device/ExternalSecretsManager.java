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

import com.ibm.zurich.idmx.annotations.Nullable;

public interface ExternalSecretsManager {

  /**
   * Allocate a credential on an external device.
   * 
   * @param deviceUid The ID of the device the credential should be added to.
   * @param newCredentialUri The ID to give to the new credential.
   * @param issuerUri The ID of the issuer parameters that the new credential uses.
   * @param overwrite If true, then overwrite the credential if it already exists. If the flag is
   *        false and the credential exists, then fail.
   * @throws RuntimeException in case the overwrite flag is false and the credential exists already.
   */
  public void allocateCredential(final String username, final URI deviceUid, final URI newCredentialUri, final URI issuerUri,
		  final boolean overwrite);

  /**
   * Is the given external device present?
   * 
   * @param deviceUid The ID of the device.
   * @return True if the device with the given ID is present and ready to accept commands.
   */
  public boolean isDeviceLoaded(final String username, final URI deviceUid);

  /**
   * Does the given credential exist on the given device?
   * 
   * @param deviceUid The ID of the device.
   * @param credentialUri The ID of the credential on the device.
   * @return True if the device with the given ID is ready, and the credential with the given ID is
   *         present on the device.
   */
  public boolean doesCredentialExists(final String username, final URI deviceUid, final URI credentialUri);

  /**
   * Return the base for the public key of the given device, which is also the first
   * base used for non-scope exclusive pseudonyms. The device public key is computed as
   * D = g^x (mod p) ; non-scope exclusive
   * pseudonyms are computed as P = g^x * h^r (mod p) ; this function returns g.
   * 
   * @param deviceUid
   * @return
   */
  public BigInteger getPublicKeyBase(final String username, final URI deviceUid);

  /**
   * Return the prime modulus for the group where pseudonyms are computed in on the given device.
   * Non-scope exclusive pseudonyms are computed as P = g^x * h^r (mod p) ; scope exclusive
   * pseudonyms are computed as P = (hash(scope)^cofactor)^x (mod p) this function returns p.
   * 
   * @param deviceUid
   * @return
   */
  public BigInteger getPseudonymModulus(final String username, final URI deviceUid);

  /**
   * Return the prime subgroup order pseudonyms are computed in on the given device. Non-scope
   * exclusive pseudonyms are computed as P = g^x * h^r (mod p) ; scope exclusive pseudonyms are
   * computed as P = (hash(scope)^cofactor)^x (mod p) ; this function returns (p-1)/cofactor = order
   * of g = order of h.
   * 
   * @param deviceUid
   * @return
   */
  public BigInteger getPseudonymSubgroupOrder(final String username, final URI deviceUid);

  /**
   * Returns the base used with the device secret for the given credential on the given card.
   * Credential public keys are computed as follows: C = gd^x * gr^v (mod n) or C = gd^x (mod n) ;
   * this function returns gd.
   * 
   * @param deviceUid
   * @param credentialUri
   * @return
   */
  public BigInteger getBaseForDeviceSecret(final String username, final URI deviceUid, final URI credentialUri);

  /**
   * Returns the optional base used with the credential secret for the given credential on the given
   * card. If the credential does not have a credential secret, then this function returns null.
   * Credential public keys are computed as follows: C = gd^x * gr^v (mod n) or C = gd^x (mod n) ;
   * this function returns gr or null, respectively.
   * 
   * @param deviceUid
   * @param credentialUri
   * @return
   */
  public @Nullable
  BigInteger getBaseForCredentialSecret(final String username, final URI deviceUid, final URI credentialUri);

  /**
   * Returns the modulus used when computing with the given credential on the given device.
   * Credential public keys are computed as follows: C = gd^x * gr^v (mod n) or C = gd^x (mod n) ;
   * this function returns n.
   * 
   * @param deviceUid
   * @param credentialUri
   * @return
   */
  public BigInteger getModulus(final String username, final URI deviceUid, final URI credentialUri);

  /**
   * Returns the size of the challenge the card uses, in bytes. A common value is 32 (for 256-bit
   * challenges).
   * 
   * @return
   */
  public int getChallengeSizeBytes(final String username, final URI deviceUid);

  /**
   * Returns the size of the R-values the card uses, in bytes. The size should be equal to
   * getChallengeSizeBytes() + getAttributeSizeBytes() + the statistical zero-knowledge parameter.
   * For an 80-bit statistical zero-knowledge parameter, a common return value for this function is
   * 74 ( = (256 + 256 + 80) / 8).
   * 
   * @return
   */
  public int getRandomizerSizeBytes(final String username, final URI deviceUid);

  /**
   * Returns the size of the device and credential secret the card uses, in bytes. A common value is
   * 32 (for 256-bit secrets).
   * 
   * @return
   */
  public int getAttributeSizeBytes(final String username, final URI deviceUid);

  /**
   * Returns the public key of the given device. The device public key is DP = g^x (mod p).
   * 
   * @param deviceUid
   * @return
   */
  public BigInteger getDevicePublicKey(final String username, final URI deviceUid);

  /**
   * Returns the credential public key of the given credential on the given device. Credential
   * public keys are computed as follows: C = gd^x * gr^v (mod n) or C = gd^x (mod n)
   * 
   * @param deviceUid
   * @param credentialUri
   * @return
   */
  public BigInteger getCredentialPublicKey(final String username, final URI deviceUid, final URI credentialUri);

  /**
   * Returns the value of a scope exclusive pseudonym with the given scope on the given device.
   * Scope exclusive pseudonyms are computed as P = (hash(scope)^cofactor)^x (mod p).
   * 
   * @param deviceUid
   * @param scope
   * @return
   */
  public BigInteger getScopeExclusivePseudonym(final String username, final URI deviceUid, final URI scope);

  /**
   * Returns the base used for scope exclusive pseudonyms for the given scope, modulus and subgroup
   * order. Returns (hash(scope)^cofactor) (mod p), where cofactor = (p-1)/subgroupOrder
   * 
   * @param scope
   * @param modulus The value p
   * @param subgroupOrder
   * @return
   */
  public BigInteger getBaseForScopeExclusivePseudonym(final String username, final URI scope, final BigInteger modulus,
		  final BigInteger subgroupOrder);

  /**
   * Returns the base used for scope exclusive pseudonyms for the given scope on the given device.
   * Returns (hash(scope)^cofactor) (mod p), where cofactor = (p-1)/subgroupOrder
   * 
   * @oaram deviceUid
   * @param scope
   * @return
   */
  public BigInteger getBaseForScopeExclusivePseudonym(final String username, final URI deviceUid, final URI scope);

  /**
   * Perform the first round of the Sigma-protocol with all the devices listed in the proof
   * specification and return a proof commitment object holding the T-Values (commitments = first
   * message flow of a Sigma-protocol) returned by the devices.
   * 
   * @param spec
   * @return
   */
  public DeviceProofCommitment getPresentationCommitment(final DeviceProofSpecification spec);

  /**
   * Perform the third round of a Sigma-protocol with all the devices that were involved with the
   * given proof commitment object and with the given challenge. This function returns a proof
   * response object holding the S-Values (responses = third message flow of a Sigma-protocol)
   * returned by the devices.
   * 
   * @param com
   * @param challenge
   * @return
   */
  public DeviceProofResponse getPresentationResponse(final DeviceProofCommitment com, 
		  final BigInteger challenge);

  /**
   * Factory for proof specification objects.
   * 
   * @return
   */
  public DeviceProofSpecification newProofSpec(final String username);

  /**
   * Method for testing only: associates the issuer parameters issuerUriOnDevice to the
   * credential credIdOnDevice.
   * A real implementation of an external device manager should do a no-op when this method is called.
   * @param deviceUid
   * @param credIdOnDevice
   * @param issuerUriOnDevice
   */
  public void associateIssuer(final String username, final URI deviceUid, final URI credIdOnDevice, final URI issuerUriOnDevice);
}
