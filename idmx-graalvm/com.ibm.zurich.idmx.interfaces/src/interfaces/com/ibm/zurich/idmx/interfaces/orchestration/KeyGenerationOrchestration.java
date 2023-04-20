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

package com.ibm.zurich.idmx.interfaces.orchestration;

import com.ibm.zurich.idmx.exception.ConfigurationException;

import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.xml.IdemixVerifierParameters;
import eu.abc4trust.xml.InspectorPublicKeyTemplate;
import eu.abc4trust.xml.IssuerPublicKeyTemplate;
import eu.abc4trust.xml.KeyPair;
import eu.abc4trust.xml.RevocationAuthorityPublicKeyTemplate;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.SystemParametersTemplate;
//import eu.abc4trust.xml.VerifierParameters;
import eu.abc4trust.xml.VerifierParametersTemplate;

/**
 * 
 */
public interface KeyGenerationOrchestration {

  /**
   * Returns a system parameters template that is required to setup new system parameters.
   * 
   * @throws ConfigurationException
   */
  public SystemParametersTemplate createSystemParametersTemplate() throws ConfigurationException;

  /**
   * Returns new system parameters based on the given configuration (i.e., the system parameters
   * template).
   * 
   * @throws ConfigurationException
   */
  public SystemParameters setupSystemParameters(final SystemParametersTemplate systemParametersTemplate)
      throws ConfigurationException;



  /**
   * Returns an issuers public key template that is required to setup new issuers public key.
   * 
   * @throws ConfigurationException
   * @throws KeyManagerException
   */
  public IssuerPublicKeyTemplate createIssuerPublicKeyTemplate() throws ConfigurationException,
      KeyManagerException;

  /**
   * Returns issuer public key based on the given configuration (i.e., the issuers public key
   * template).
   * 
   * @throws KeyManagerException
   * @throws ConfigurationException
   * @throws CredentialManagerException
   */
  public KeyPair setupIssuerKeyPair(SystemParameters systemParameters,
      IssuerPublicKeyTemplate issuerParametersTemplate) throws KeyManagerException,
      ConfigurationException, CredentialManagerException;



  /**
   * Returns an revocation authority public key template that is required to setup a new revocation
   * authority public key.
   */
  public RevocationAuthorityPublicKeyTemplate createRevocationAuthorityPublicKeyTemplate()
      throws ConfigurationException, KeyManagerException;

  /**
   * Returns revocation authority public key based on the given configuration (i.e., the revocation
   * authority public key template).
   * 
   * @throws ConfigurationException
   */
  public KeyPair setupRevocationAuthorityKeyPair(SystemParameters systemParameters,
      RevocationAuthorityPublicKeyTemplate revocationAuthorityPublicKeyTemplate)
      throws ConfigurationException;



  /**
   * Returns an inspector public key template that is required to setup a new inspector public key.
   */
  public InspectorPublicKeyTemplate createInspectorPublicKeyTemplate()
      throws ConfigurationException, KeyManagerException;

  /**
   * Returns inspector public key based on the given configuration (i.e., the inspector public key
   * template).
   * 
   * @throws ConfigurationException
   */
  public KeyPair setupInspectorKeyPair(SystemParameters systemParameters,
      InspectorPublicKeyTemplate inspectorPublicKeyTemplate) throws ConfigurationException;

  /**
   * This method generates a template for the verifier parameter configuration. This template will
   * have to be filled out manually, and then given to generateVerifierParameters().
   * @throws ConfigurationException 
   * @throws KeyManagerException 
   */
  public VerifierParametersTemplate generateVerifierParameterConfigurationTemplate() throws ConfigurationException, KeyManagerException;


  /**
   * This method generates verifier parameters based on the given configuration.
   * @throws ConfigurationException 
   */
  public IdemixVerifierParameters generateVerifierParameters(SystemParameters systemParameters,
      VerifierParametersTemplate verifierParametersTemplate) throws ConfigurationException;
}
