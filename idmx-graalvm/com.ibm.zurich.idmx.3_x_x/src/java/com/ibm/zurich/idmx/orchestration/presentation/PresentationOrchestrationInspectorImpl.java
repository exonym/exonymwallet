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

import java.util.List;

import com.ibm.zurich.idmx.buildingBlock.factory.BuildingBlockFactory;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.PresentationOrchestrationException;
import com.ibm.zurich.idmx.exception.ProofException;
import com.ibm.zurich.idmx.interfaces.orchestration.presentation.PresentationOrchestrationInspector;
import com.ibm.zurich.idmx.interfaces.proofEngine.ZkDirector;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.RandomGeneration;

import eu.abc4trust.abce.internal.inspector.credentialManager.CredentialManager;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.util.AttributeConverter;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.IssuanceToken;
import eu.abc4trust.xml.PresentationToken;

import javax.inject.Inject;

/**
 * 
 */
public class PresentationOrchestrationInspectorImpl implements PresentationOrchestrationInspector {

  private final KeyManager keyManager;

  private final BigIntFactory bigIntFactory;
  private final CredentialManager credentialManager;
  private final BuildingBlockFactory bbFactory;
  private final AttributeConverter attributeConverter;
  private final RandomGeneration randomGeneration;


  @Inject
  public PresentationOrchestrationInspectorImpl(final BuildingBlockFactory buildingBlockFactory,
		  final BigIntFactory bigIntFactory, final KeyManager keyManager,
		  final CredentialManager credentialManager, final ZkDirector zkDirector,
		  final AttributeConverter attributeConverter,
		  final RandomGeneration rg) {

    this.bigIntFactory = bigIntFactory;
    this.credentialManager = credentialManager;
    this.keyManager = keyManager;
    this.bbFactory = buildingBlockFactory;
    this.attributeConverter = attributeConverter;
    this.randomGeneration = rg;
  }

  @Override
  public List<Attribute> inspect(final PresentationToken presentationToken)
      throws PresentationOrchestrationException {
    try {
      final PresentationOrchestrationInspectorInternal intern =
          new PresentationOrchestrationInspectorInternal(credentialManager, bigIntFactory,
              bbFactory, keyManager, attributeConverter, randomGeneration);
      return intern.inspect(presentationToken);
    } catch (KeyManagerException|ConfigurationException|ProofException e ) {
      throw new PresentationOrchestrationException(e);
    }
  }

  @Override
  public List<Attribute> inspect(final IssuanceToken issuanceToken)
      throws PresentationOrchestrationException {
    final PresentationToken pt = new PresentationToken();
    pt.setCryptoEvidence(issuanceToken.getCryptoEvidence());
    pt.setPresentationTokenDescription(issuanceToken.getIssuanceTokenDescription()
        .getPresentationTokenDescription());
    pt.setVersion(issuanceToken.getVersion());
    return inspect(pt);
  }

}
