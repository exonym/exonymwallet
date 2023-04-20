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

import java.util.List;

import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.PresentationOrchestrationException;
import com.ibm.zurich.idmx.interfaces.cryptoEngine.CryptoEngineInspector;
import com.ibm.zurich.idmx.interfaces.orchestration.KeyGenerationOrchestration;
import com.ibm.zurich.idmx.interfaces.orchestration.presentation.PresentationOrchestrationInspector;

import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.InspectorPublicKeyTemplate;
import eu.abc4trust.xml.IssuanceToken;
import eu.abc4trust.xml.KeyPair;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.SystemParameters;

import javax.inject.Inject;


public class CryptoEngineInspectorImpl implements CryptoEngineInspector {

  private final KeyGenerationOrchestration keyGenerationOrchestration;
  private final PresentationOrchestrationInspector presentationOrchestrationInspector;


  @Inject
  public CryptoEngineInspectorImpl(final KeyGenerationOrchestration keyGenerationOrchestration,
                                   final PresentationOrchestrationInspector presentationOrchestrationInspector) {

    this.keyGenerationOrchestration = keyGenerationOrchestration;
    this.presentationOrchestrationInspector = presentationOrchestrationInspector;

  }

  @Override
  public InspectorPublicKeyTemplate createInspectorPublicKeyTemplate()
      throws ConfigurationException, KeyManagerException {
    return keyGenerationOrchestration.createInspectorPublicKeyTemplate();
  }

  @Override
  public KeyPair setupInspectorKeyPair(final SystemParameters systemParameters,
                                       final InspectorPublicKeyTemplate template) throws ConfigurationException {
    return keyGenerationOrchestration.setupInspectorKeyPair(systemParameters, template);
  }

  @Override
  public List<Attribute> inspect(final PresentationToken presentationToken) throws CryptoEngineException {
    try {
      return presentationOrchestrationInspector.inspect(presentationToken);
    } catch (final PresentationOrchestrationException e) {
      throw new CryptoEngineException(e);
    }
  }

  @Override
  public List<Attribute> inspect(final IssuanceToken issuanceToken) throws CryptoEngineException {
    try {
      return presentationOrchestrationInspector.inspect(issuanceToken);
    } catch (final PresentationOrchestrationException e) {
      throw new CryptoEngineException(e);
    }
  }
}
