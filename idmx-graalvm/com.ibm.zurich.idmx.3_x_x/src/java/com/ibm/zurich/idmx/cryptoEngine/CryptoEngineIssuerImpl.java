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
import java.util.List;

import com.ibm.zurich.idmx.annotations.Nullable;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.IssuanceOrchestrationException;
import com.ibm.zurich.idmx.interfaces.cryptoEngine.CryptoEngineIssuer;
import com.ibm.zurich.idmx.interfaces.orchestration.KeyGenerationOrchestration;
import com.ibm.zurich.idmx.interfaces.orchestration.issuance.IssuanceOrchestrationIssuer;

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

import javax.inject.Inject;


public class CryptoEngineIssuerImpl implements CryptoEngineIssuer {

  private final KeyGenerationOrchestration keyGenerationOrchestration;
  private final IssuanceOrchestrationIssuer issuanceOrchestration;

  @Inject
  public CryptoEngineIssuerImpl(final KeyGenerationOrchestration keyGenerationOrchestration,
                                final IssuanceOrchestrationIssuer issuanceOrchestration) {

    this.keyGenerationOrchestration = keyGenerationOrchestration;
    this.issuanceOrchestration = issuanceOrchestration;
  }

  @Override
  public SystemParametersTemplate createSystemParametersTemplate() throws ConfigurationException {
    return keyGenerationOrchestration.createSystemParametersTemplate();
  }

  @Override
  public SystemParameters setupSystemParameters(SystemParametersTemplate systemParametersTemplate)
      throws ConfigurationException {
    return keyGenerationOrchestration.setupSystemParameters(systemParametersTemplate);
  }

  @Override
  public IssuerPublicKeyTemplate createIssuerKeyPairTemplate() throws ConfigurationException,
      KeyManagerException {
    return keyGenerationOrchestration.createIssuerPublicKeyTemplate();
  }

  @Override
  public KeyPair setupIssuerKeyPair(final SystemParameters systemParameters,
                                    final IssuerPublicKeyTemplate issuerParametersTemplate) throws ConfigurationException,
      KeyManagerException, CredentialManagerException {

    KeyPair issuerKeyPair =
        keyGenerationOrchestration.setupIssuerKeyPair(systemParameters, issuerParametersTemplate);

    return issuerKeyPair;
  }

  @Override
  public IssuanceMessageAndBoolean initializeIssuance(final IssuancePolicy issuancePolicy,
                                                      final List<Attribute> issuerProvidedAttributes, final @Nullable URI context) throws CryptoEngineException {
    try {
      return issuanceOrchestration.initializeIssuance(issuancePolicy, issuerProvidedAttributes,
          context);
    } catch (final IssuanceOrchestrationException e) {
      throw new CryptoEngineException(e);
    }
  }

  @Override
  public IssuanceMessageAndBoolean issuanceStep(final IssuanceMessage issuanceMessage)
      throws CryptoEngineException {

    try {
      return issuanceOrchestration.issuanceStep(issuanceMessage);
    } catch (final IssuanceOrchestrationException e) {
      throw new CryptoEngineException(e);
    }
  }

  @Override
  public IssuanceTokenDescription extractIssuanceTokenDescription(final IssuanceMessage issuanceMessage) {
    return issuanceOrchestration.extractIssuanceTokenDescription(issuanceMessage);
  }

  @Override
  public IssuanceTokenAndIssuancePolicy extractIssuanceTokenAndPolicy(
      final IssuanceMessage issuanceMessage) throws IssuanceOrchestrationException {
    return issuanceOrchestration.extractIssuanceTokenAndPolicy(issuanceMessage);
  }


}
