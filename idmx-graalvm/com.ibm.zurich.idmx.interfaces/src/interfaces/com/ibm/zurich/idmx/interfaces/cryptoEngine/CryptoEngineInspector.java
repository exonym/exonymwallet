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

import java.util.List;

import com.ibm.zurich.idmx.exception.ConfigurationException;

import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.InspectorPublicKeyTemplate;
import eu.abc4trust.xml.IssuanceToken;
import eu.abc4trust.xml.KeyPair;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.SystemParameters;

/**
 * 
 */
public interface CryptoEngineInspector {

  /**
   * Returns a template with default values for creating a valid set of inspector parameters.
   * 
   * @throws KeyManagerException
   * @throws ConfigurationException
   */
  public InspectorPublicKeyTemplate createInspectorPublicKeyTemplate()
      throws ConfigurationException, KeyManagerException;

  /**
   * Generates an inspector key pair based on the given configuration.
   * 
   * @throws ConfigurationException
   * @throws CredentialManagerException
   * @throws KeyManagerException
   */
  public KeyPair setupInspectorKeyPair(final SystemParameters systemParameters,
		  final InspectorPublicKeyTemplate revocationAuthorityPublicKeyTemplate)
      throws ConfigurationException;

  public List<Attribute> inspect(final PresentationToken presentationToken) throws CryptoEngineException;

  public List<Attribute> inspect(final IssuanceToken issuanceToken) throws CryptoEngineException;

}
