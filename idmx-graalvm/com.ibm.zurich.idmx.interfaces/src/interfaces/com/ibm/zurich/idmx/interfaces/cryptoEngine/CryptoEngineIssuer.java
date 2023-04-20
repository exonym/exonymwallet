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

import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.IssuanceMessage;
import eu.abc4trust.xml.IssuanceMessageAndBoolean;
import eu.abc4trust.xml.IssuancePolicy;
import eu.abc4trust.xml.IssuanceTokenAndIssuancePolicy;
import eu.abc4trust.xml.IssuanceTokenDescription;
import eu.abc4trust.xml.IssuerPublicKeyTemplate;
import eu.abc4trust.xml.KeyPair;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.SystemParametersTemplate;

/**
 * 
 */
public interface CryptoEngineIssuer {

  /**
   * Returns a template with default values for creating a valid set of system parameters.
   * 
   * @throws ConfigurationException
   */
  public SystemParametersTemplate createSystemParametersTemplate() throws ConfigurationException;

  /**
   * Generates system parameters based on the given system parameters configuration.
   * 
   * @throws ConfigurationException
   */
  public SystemParameters setupSystemParameters(final SystemParametersTemplate systemParametersTemplate)
      throws ConfigurationException;

  /**
   * Returns a template with default values for setting up issuer parameters (i.e., an issuer key
   * pair).
   * 
   * @throws ConfigurationException
   * @throws KeyManagerException
   */
  public IssuerPublicKeyTemplate createIssuerKeyPairTemplate() throws ConfigurationException,
      KeyManagerException;

  /**
   * Generates issuer parameters based on the given issuer parameters configuration.
   * 
   * @throws ConfigurationException
   * @throws KeyManagerException
   * @throws CredentialManagerException
   */
  public KeyPair setupIssuerKeyPair(final SystemParameters systemParameters,
		  final IssuerPublicKeyTemplate issuerParametersTemplate) throws ConfigurationException,
      KeyManagerException, CredentialManagerException;

  /**
   * This method does the first step in an issuance proof. For subsequent steps, continueIssuance()
   * must be called. For "simple issuance", the verifier parameters will be NULL, and all three
   * lists empty.
   * 
   * @throws CryptoEngineException
   */
  public IssuanceMessageAndBoolean initializeIssuance(final IssuancePolicy issuancePolicy,
		  final List<Attribute> issuerProvidedAttributes,
                                               final @Nullable URI context) throws CryptoEngineException;

  /**
   * This method continues the issuance proof. This method also returns true if this is the last
   * message of the issuance. This method also returns the URI of the log entry that was stored
   * during the issuance protocol.
   * 
   * @throws CryptoEngineException
   */
  public IssuanceMessageAndBoolean issuanceStep(final IssuanceMessage issuanceMessage)
      throws CryptoEngineException;

  /**
   * This method looks for an IssuanceTokenDescription inside the issuance message. This method
   * returns the issuance token, or NULL if none could be found. It is guaranteed that this method
   * returns a non-null value before a new credential is actually issued, so that the upper layers
   * may abort the issuance protocol if a certain condition is not satisfied (such as the absence of
   * a registered pseudonym).
   */
  public IssuanceTokenDescription extractIssuanceTokenDescription(final IssuanceMessage issuanceMessage);

  /**
   * If the given issuance message contains an issuance token, this method returns said
   * issuance token together with the issuance policy this issuer was initialized with.
   * Otherwise return null. If this method returns non-null, the caller is expected to check the
   * issuance token against the issuance policy. It is mandatory to call this method
   * before each call to issuanceStep (otherwise the issuer will fail during verification of
   * the issuance token).
   * 
   * @param issuanceMessage
   * @return
   * @throws IssuanceOrchestrationException 
   */
  @Nullable
  public IssuanceTokenAndIssuancePolicy extractIssuanceTokenAndPolicy(
		  final IssuanceMessage issuanceMessage) throws IssuanceOrchestrationException;
}
