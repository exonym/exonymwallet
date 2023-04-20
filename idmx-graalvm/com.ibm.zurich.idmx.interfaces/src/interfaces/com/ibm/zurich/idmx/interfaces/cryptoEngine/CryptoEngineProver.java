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

package com.ibm.zurich.idmx.interfaces.cryptoEngine;

import java.net.URI;
import java.util.List;

import com.ibm.zurich.idmx.annotations.Nullable;

import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.xml.Credential;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.PresentationTokenDescription;
import eu.abc4trust.xml.PseudonymWithMetadata;
import eu.abc4trust.xml.RevocationInformation;
import eu.abc4trust.xml.VerifierParameters;

/**
 * 
 */
public interface CryptoEngineProver {

  /**
   * This method asks the crypto engine to conduct a presentation proof.
   * 
   * @throws CryptoEngineException
   */
  public PresentationToken createPresentationToken(final String username, 
		  final PresentationTokenDescription presentationTokenDescription,
      final VerifierParameters verifierParameters, final List<URI> credentialUris, final List<URI> pseudonymUris)
      throws CryptoEngineException;

  /**
   * This method generates a description of the proof that createPresentationToken() would create.
   * This description is intended to be shown in the user interface.
   */
  public String describePresentationToken(final String username, 
		  final PresentationTokenDescription presentationTokenDescription,
      final VerifierParameters verifierParameters, final List<URI> credentialUris, final List<URI> pseudonymUris);

  /**
   * This method updates the non-revocation evidence in the credential to the specified version (or
   * the latest version if none is specified). This method MAY refuse to update to an earlier
   * version of the NRE. If the allowLinkableUpdate is false, this method MUST do the update in an
   * unlikable manner.
   * 
   * This method is also responsible for updating the credential in the credential manager if
   * needed.
   * 
   * @throws CryptoEngineException
   */
  public Credential updateNonRevocationEvidence(final String username, final Credential credential,
		  final @Nullable URI versionOfNre /* If null: latest version */, final boolean allowLinkableUpdate) throws CryptoEngineException;

  /**
   * This method returns a specific version of the revocation information (or the latest version if
   * none is specified). You may provide the current revocation information to enable a quicker
   * update.
   * 
   * @throws CryptoEngineException
   */
  public RevocationInformation updateRevocationInformation(final String username, final URI revocationAuthority,
		  final @Nullable URI revocationInformationId /* If null: latest version */) throws CryptoEngineException;

  /**
   * This method creates a new pseudonym.
   * 
   * @throws CryptoEngineException
   */
  public PseudonymWithMetadata createPseudonym(final String username, final URI pseudonymUri, final URI scope,
		  final boolean isScopeExclusive, final URI secretLocation /* e.g., smartcard URI */)
      throws CryptoEngineException;

  /**
   * @param cred
   * @return
   * @throws CryptoEngineException
   */
  public boolean isRevoked(final String username, final Credential cred) throws CryptoEngineException;

}
