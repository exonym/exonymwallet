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
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.IssuanceOrchestrationException;
import com.ibm.zurich.idmx.interfaces.util.BigInt;

import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.KeyPair;
import eu.abc4trust.xml.NonRevocationEvidence;
import eu.abc4trust.xml.RevocationAuthorityPublicKeyTemplate;
import eu.abc4trust.xml.RevocationHistory;
import eu.abc4trust.xml.RevocationInformation;
import eu.abc4trust.xml.SystemParameters;

/**
 * 
 */
public interface CryptoEngineRevocationAuthority {

  /**
   * Returns a template with default values for creating a valid set of revocation authority
   * parameters.
   * 
   * @throws KeyManagerException
   * @throws ConfigurationException
   */
  public RevocationAuthorityPublicKeyTemplate createRevocationAuthorityPublicKeyTemplate()
      throws ConfigurationException, KeyManagerException;

  /**
   * Generates revocation parameters based on the given configuration.
   * 
   * @throws ConfigurationException
   * @throws CredentialManagerException
   * @throws KeyManagerException
   */
  public KeyPair setupRevocationAuthorityKeyPair(final SystemParameters systemParameters,
		  final RevocationAuthorityPublicKeyTemplate revocationAuthorityPublicKeyTemplate)
      throws ConfigurationException, CredentialManagerException, KeyManagerException;


  /**
   * This method is intended to be called though the revocation proxy. This method generates a new
   * revocation handle and the corresponding non-revocation evidence. This method additionally
   * returns the URI of the latest revocation information.
   * 
   * @throws KeyManagerException
   * @throws ConfigurationException
   * @throws CredentialManagerException
   * @throws IssuanceOrchestrationException
   */
  public NonRevocationEvidence newRevocationHandle(final URI revocationAuthorityId,
		  final URI nonRevocationEvidenceId, final List<Attribute> attributes) throws CryptoEngineException;

  /**
   * This method revokes the specified revocation handle, and returns the URI of the latest
   * revocation information.
   * 
   * @throws IssuanceOrchestrationException
   * @throws KeyManagerException
   * @throws CredentialManagerException
   * @throws ConfigurationException
   * @throws CryptoEngineException
   */
  public URI revoke(final URI revocationAuthorityUri, final BigInt revocationHandle)
      throws CryptoEngineException;

  /**
   * Returns the revocation history.
   * 
   * @throws CryptoEngineException
   */
  public RevocationHistory getRevocationHistory(final URI revocationAuthorityUri)
      throws CryptoEngineException;

  // /**
  // * This method is intended to be called through the revocation proxy. This method helps the
  // caller
  // * update a non-revocation evidence to a specific version (or the latest version). The structure
  // * of the request and the return value are implementation-specific.
  // */
  // public NreUpdateMessage updateNonRevocationEvidence(URI revocationAuthorityUri,
  // @Nullable URI previousNre, @Nullable URI versionOfNre, NreUpdateMessage updateRequest);

  /**
   * This method is intended to be called through the revocation proxy. This method returns a
   * specific version of the revocation information.
   * 
   * @throws CryptoEngineException
   * @throws KeyManagerException 
   */
  public RevocationInformation updateRevocationInformation(final URI revocationAuthorityUri,
		  final @Nullable URI versionOfRevocation,
		  final @Nullable RevocationInformation currentRevocationInformation) throws CryptoEngineException, KeyManagerException;

}
