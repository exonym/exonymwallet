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

import com.ibm.zurich.idmx.annotations.Nullable;
import com.ibm.zurich.idmx.buildingBlock.factory.BuildingBlockFactory;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.PresentationOrchestrationException;
import com.ibm.zurich.idmx.exception.ProofException;
import com.ibm.zurich.idmx.interfaces.orchestration.presentation.PresentationOrchestrationVerifier;
import com.ibm.zurich.idmx.interfaces.proofEngine.ZkDirector;
import com.ibm.zurich.idmx.interfaces.state.CarryOverStateIssuer;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.Pair;

import eu.abc4trust.abce.internal.user.credentialManager.CredentialManager;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.util.AttributeConverter;
import eu.abc4trust.xml.CredentialTemplate;
import eu.abc4trust.xml.IssuanceToken;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.VerifierParameters;

import javax.inject.Inject;

/**
 * 
 */
public class PresentationOrchestrationVerifierImpl implements PresentationOrchestrationVerifier {

  private final KeyManager keyManager;

  private final BigIntFactory bigIntFactory;
  private final ZkDirector zkDirector;
  private final BuildingBlockFactory bbFactory;
  private final AttributeConverter attributeConverter;


  @Inject
  public PresentationOrchestrationVerifierImpl(
      final BuildingBlockFactory buildingBlockFactory, final BigIntFactory bigIntFactory,
      final KeyManager keyManager, final CredentialManager credentialManager, final ZkDirector zkDirector,
      final AttributeConverter attributeConverter) {

    this.bigIntFactory = bigIntFactory;
    this.zkDirector = zkDirector;
    this.keyManager = keyManager;
    this.bbFactory = buildingBlockFactory;
    this.attributeConverter = attributeConverter;
  }

  @Override
  public boolean verifyProof(final PresentationToken presentationToken, final VerifierParameters vp)
      throws PresentationOrchestrationException {
    return verifyProof_old(presentationToken, null, vp).first;
  }


  @Override
  public Pair<Boolean, CarryOverStateIssuer> verifyProof(final IssuanceToken issuanceToken, final VerifierParameters vp)
      throws PresentationOrchestrationException {
    final PresentationToken pt = new PresentationToken();
    pt.setCryptoEvidence(issuanceToken.getCryptoEvidence());
    pt.setPresentationTokenDescription(issuanceToken.getIssuanceTokenDescription()
        .getPresentationTokenDescription());
    pt.setVersion(issuanceToken.getVersion());

    final CredentialTemplate credentialTemplate =
        issuanceToken.getIssuanceTokenDescription().getCredentialTemplate();
    return verifyProof_old(pt, credentialTemplate, vp);
  }


  public Pair<Boolean, CarryOverStateIssuer> verifyProof_old(final PresentationToken presentationToken,
    final @Nullable CredentialTemplate ct, final VerifierParameters vp) throws PresentationOrchestrationException {
    PresentationOrchestrationVerifierInternal intern =
        new PresentationOrchestrationVerifierInternal(zkDirector, bigIntFactory, bbFactory, keyManager,
            attributeConverter);
    try {

      return intern.verifyProof(presentationToken, ct, vp);

    } catch (KeyManagerException|ConfigurationException|ProofException e) {
      throw new PresentationOrchestrationException(e);
    }
  }

}
