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

package com.ibm.zurich.idmx.cryptoEngine;

import java.net.URI;

import com.ibm.zurich.idmix.abc4trust.facades.RevocationAuthorityParametersFacade;
import com.ibm.zurich.idmx.annotations.Nullable;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.PresentationOrchestrationException;
import com.ibm.zurich.idmx.interfaces.cryptoEngine.CryptoEngineVerifier;
import com.ibm.zurich.idmx.interfaces.orchestration.KeyGenerationOrchestration;
import com.ibm.zurich.idmx.interfaces.orchestration.presentation.PresentationOrchestrationVerifier;

import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.xml.IdemixVerifierParameters;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.RevocationInformation;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.VerifierParameters;
import eu.abc4trust.xml.VerifierParametersTemplate;

import javax.inject.Inject;

/**
 * 
 */
public class CryptoEngineVerifierImpl implements CryptoEngineVerifier {

  private final PresentationOrchestrationVerifier presentationOrchestration;
  private final KeyManager keyManager;
  private final KeyGenerationOrchestration keyOrchestration;

  @Inject
  public CryptoEngineVerifierImpl(final PresentationOrchestrationVerifier presentationOrchestration,
                                  final KeyManager keyManager, final KeyGenerationOrchestration keyOrchestration) {

    this.presentationOrchestration = presentationOrchestration;
    this.keyManager = keyManager;
    this.keyOrchestration = keyOrchestration;
  }

  @Override
  public boolean verifyToken(final PresentationToken presentationToken,
                             final VerifierParameters verifierParameters) throws CryptoEngineException {
    final boolean result;
    try {
      result = presentationOrchestration.verifyProof(presentationToken, verifierParameters);
    } catch (final PresentationOrchestrationException e) {
      throw new CryptoEngineException(e);
    }
    return result;
  }


  @Override
  public RevocationInformation updateRevocationInformation(final URI raParametersId,
                                                           final @Nullable URI revocationInformationId) throws CryptoEngineException {

    try {
      // get a specific version of the revocation information - if expired, the latest version will
      // be fetched from the revocation authority
      if (revocationInformationId != null) {
        return keyManager.getRevocationInformation(raParametersId, revocationInformationId);
      }

      // if the revocation information has not been already retrieved
      try {
        return keyManager.getLatestRevocationInformation(raParametersId);
      } catch (final KeyManagerException e) {
        // TODO (pbi) this should be removed and the RA parameters should use a distinct UID
        return keyManager.getLatestRevocationInformation(RevocationAuthorityParametersFacade
                .getRevocationAuthorityParametersUID(raParametersId));
      }
    } catch (final KeyManagerException e) {
      throw new CryptoEngineException(e);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.ibm.zurich.idmx.interfaces.cryptoEngine.CryptoEngineVerifier#
   * generateVerifierParameterConfigurationTemplate()
   */
  @Override
  public VerifierParametersTemplate generateVerifierParameterConfigurationTemplate() throws CryptoEngineException {
    try {
      return keyOrchestration.generateVerifierParameterConfigurationTemplate();
    } catch (ConfigurationException|KeyManagerException e) {
      throw new CryptoEngineException(e);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.ibm.zurich.idmx.interfaces.cryptoEngine.CryptoEngineVerifier#generateVerifierParameters
   * (eu.abc4trust.xml.VerifierParametersTemplate)
   */
  @Override
  public IdemixVerifierParameters generateVerifierParameters(final SystemParameters systemParameters,
      VerifierParametersTemplate verifierParametersTemplate) throws CryptoEngineException {
    try {
      return keyOrchestration.generateVerifierParameters(systemParameters, verifierParametersTemplate);
    } catch (final ConfigurationException e) {
      throw new CryptoEngineException(e);
    }
  }

}
