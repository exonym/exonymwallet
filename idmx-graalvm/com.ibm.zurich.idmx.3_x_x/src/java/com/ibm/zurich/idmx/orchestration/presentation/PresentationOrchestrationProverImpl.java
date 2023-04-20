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

package com.ibm.zurich.idmx.orchestration.presentation;

import java.net.URI;
import java.util.List;

import com.ibm.zurich.idmx.buildingBlock.factory.BuildingBlockFactory;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.NotEnoughTokensException;
import com.ibm.zurich.idmx.exception.PresentationOrchestrationException;
import com.ibm.zurich.idmx.exception.ProofException;
import com.ibm.zurich.idmx.interfaces.configuration.Constants;
import com.ibm.zurich.idmx.interfaces.device.ExternalSecretsManager;
import com.ibm.zurich.idmx.interfaces.orchestration.presentation.PresentationOrchestrationProver;
import com.ibm.zurich.idmx.interfaces.proofEngine.ZkDirector;
import com.ibm.zurich.idmx.interfaces.state.CarryOverStateRecipientWithAttributes;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.Pair;
import com.ibm.zurich.idmx.interfaces.util.RandomGeneration;

import eu.abc4trust.abce.internal.user.credentialManager.CredentialManager;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.util.AttributeConverter;
import eu.abc4trust.xml.CredentialTemplate;
import eu.abc4trust.xml.IssuanceToken;
import eu.abc4trust.xml.IssuanceTokenDescription;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.PresentationTokenDescription;
import eu.abc4trust.xml.VerifierParameters;

import javax.inject.Inject;

/**
 * 
 */
public class PresentationOrchestrationProverImpl implements PresentationOrchestrationProver {

  private final KeyManager keyManager;

  private final BigIntFactory bigIntFactory;
  private final ZkDirector zkDirector;
  private final BuildingBlockFactory bbFactory;
  private final AttributeConverter attributeConverter;
  private final RandomGeneration randomGeneration;
  private final CredentialManager credentialManager;
  private final ExternalSecretsManager deviceManager;


  @Inject
  public PresentationOrchestrationProverImpl(final BuildingBlockFactory buildingBlockFactory,
                                             final BigIntFactory bigIntFactory, final KeyManager keyManager, final ZkDirector zkDirector,
      final AttributeConverter attributeConverter, final RandomGeneration randomGeneration,
      final CredentialManager credentialManager, final ExternalSecretsManager deviceManager) {

    this.bigIntFactory = bigIntFactory;
    this.zkDirector = zkDirector;
    this.keyManager = keyManager;
    this.bbFactory = buildingBlockFactory;
    this.attributeConverter = attributeConverter;
    this.randomGeneration = randomGeneration;
    this.credentialManager = credentialManager;
    this.deviceManager = deviceManager;
  }

  @Override
  public PresentationToken createProof(final String username, final PresentationTokenDescription presentationTokenDescription,
                                       final List<URI> credentials, final List<URI> pseudonyms, final VerifierParameters vp) throws PresentationOrchestrationException {
    final PresentationOrchestrationProverInternal intern =
        new PresentationOrchestrationProverInternal(zkDirector, bigIntFactory, bbFactory,
            keyManager, credentialManager, attributeConverter, randomGeneration, deviceManager);
    try {
      final Pair<PresentationToken, CarryOverStateRecipientWithAttributes> ret =
          intern.createProof(username, presentationTokenDescription, credentials, pseudonyms, null, vp);
      return ret.first;
    } catch (KeyManagerException|ConfigurationException|ProofException|CredentialManagerException|NotEnoughTokensException e) {
      throw new PresentationOrchestrationException(e);
    }
  }


  @Override
  public Pair<IssuanceToken, CarryOverStateRecipientWithAttributes> createProof(final String username, 
    final IssuanceTokenDescription issuanceTokenDescription, final List<URI> credentials, final List<URI> pseudonyms,
      final VerifierParameters vp)
      throws PresentationOrchestrationException {
    final PresentationOrchestrationProverInternal intern =
        new PresentationOrchestrationProverInternal(zkDirector, bigIntFactory, bbFactory,
            keyManager, credentialManager, attributeConverter, randomGeneration, deviceManager);
    try {
      final PresentationTokenDescription ptd = issuanceTokenDescription.getPresentationTokenDescription();
      final CredentialTemplate ct = issuanceTokenDescription.getCredentialTemplate();
      final Pair<PresentationToken, CarryOverStateRecipientWithAttributes> ret =
          intern.createProof(username, ptd, credentials, pseudonyms, ct, vp);
      final IssuanceToken it = new IssuanceToken();
      it.setVersion(Constants.IMPLEMENTATION_VERSION);
      it.setCryptoEvidence(ret.first.getCryptoEvidence());
      it.setIssuanceTokenDescription(new IssuanceTokenDescription());
      it.getIssuanceTokenDescription().setCredentialTemplate(
          issuanceTokenDescription.getCredentialTemplate());
      it.getIssuanceTokenDescription().setPresentationTokenDescription(
          ret.first.getPresentationTokenDescription());
      return new Pair<IssuanceToken, CarryOverStateRecipientWithAttributes>(it, ret.second);
    } catch (KeyManagerException|ConfigurationException|ProofException|CredentialManagerException|NotEnoughTokensException e) {
      throw new PresentationOrchestrationException(e);
    }
  }
}
