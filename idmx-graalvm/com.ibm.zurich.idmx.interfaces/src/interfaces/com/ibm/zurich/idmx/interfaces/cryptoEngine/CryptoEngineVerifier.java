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

import com.ibm.zurich.idmx.annotations.Nullable;

import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.xml.IdemixVerifierParameters;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.RevocationInformation;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.VerifierParameters;
import eu.abc4trust.xml.VerifierParametersTemplate;

/**
 * 
 */
public interface CryptoEngineVerifier {

  /**
   * This method verifies the given presentation token.
   * 
   * @throws CryptoEngineException
   */
  public boolean verifyToken(final PresentationToken presentationToken,
		  final VerifierParameters verifierParameters) throws CryptoEngineException;

  /**
   * This method returns a specific version of the revocation information (or the latest version if
   * none is specified). You may provide the current revocation information to enable a quicker
   * update.
   * 
   * @throws CryptoEngineException
   */
  public RevocationInformation updateRevocationInformation(final URI revocationAuthority,
		  final @Nullable URI revocationInformationId /* If null: latest version */)
      throws CryptoEngineException;


  /**
   * This method generates a template for the verifier parameter configuration. This template will
   * have to be filled out manually, and then given to generateVerifierParameters().
   * @throws CryptoEngineException 
   */
  public VerifierParametersTemplate generateVerifierParameterConfigurationTemplate() throws CryptoEngineException;


  /**
   * This method generates verifier parameters based on the given configuration.
   * @throws CryptoEngineException 
   */
  public IdemixVerifierParameters generateVerifierParameters(final SystemParameters systemParameters,
		  final VerifierParametersTemplate verifierParametersTemplate) throws CryptoEngineException;

}
