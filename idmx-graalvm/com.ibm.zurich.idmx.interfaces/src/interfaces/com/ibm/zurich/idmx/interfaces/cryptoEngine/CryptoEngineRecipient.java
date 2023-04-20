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

import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.returnTypes.IssuMsgOrCredDesc;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.IssuanceMessage;
import eu.abc4trust.xml.IssuancePolicy;
import eu.abc4trust.xml.IssuanceTokenDescription;

/**
 * 
 */
public interface CryptoEngineRecipient {

  /**
   * Starts the issuance protocol on the side of the recipient of the credential.
   * 
   * @throws CryptoEngineException
   */
  public IssuanceMessage preIssuancePresentation(final String username, final IssuanceMessage issuanceMessage,
		  final IssuanceTokenDescription issuanceTokenDescription, final List<URI> listOfCredentialIds,
      final List<URI> pseudonyms, final List<Attribute> atts) throws CryptoEngineException;

  /**
   * On input an incoming issuance message m, this method first extracts the context attribute and
   * obtains the cryptographic state information that is stored under the same context value. It
   * then invokes the mechanism-specific cryptographic routines for one step in an interactive
   * issuance protocol.
   * 
   * If the newly issued credential is subject to Issuer-driven revocation, then, depending on the
   * revocation mechanism, this method may interact with the Revocation Authority by calling
   * RevocationProxy.processRevocationMessage(m, revpars). The method either returns an outgoing
   * issuance message or a description of the newly issued credential to indicate a successful
   * completion of the protocol. In the former case, the method eventually also stores new
   * cryptographic state information associated to the context attribute, and attaches the context
   * attribute to the outgoing message. If the invoked cryptographic routines complete the issuance
   * protocol, the method stores the obtained credential with all the cryptographic metadata in the
   * credential store by calling CredentialManager.storeCredential(cred: Credential) and returns the
   * credential description.
   * 
   * @throws CryptoEngineException
   */
  public IssuMsgOrCredDesc issuanceStep(final String username, final IssuanceMessage issuanceMessage)
      throws CryptoEngineException;

  public IssuancePolicy extractIssuancePolicy(IssuanceMessage issuanceMessage);
}
