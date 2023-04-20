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
package com.ibm.zurich.idmix.abc4trust.cryptoEngine;

import java.net.URI;
import java.util.List;

import com.ibm.zurich.idmix.abc4trust.facades.Abc4TrustSecretKeyFacade;
import com.ibm.zurich.idmix.abc4trust.facades.InspectorParametersFacade;
import com.ibm.zurich.idmx.buildingBlock.factory.BuildingBlockFactory;
import com.ibm.zurich.idmx.buildingBlock.inspector.cs.CsInspectorBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.systemParameters.EcryptSystemParametersWrapper;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.interfaces.cryptoEngine.CryptoEngineInspector;
import com.ibm.zurich.idmx.parameters.inspector.InspectorPublicKeyTemplateWrapper;

import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.returnTypes.InspectorPublicAndSecretKey;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.FriendlyDescription;
import eu.abc4trust.xml.InspectorPublicKeyTemplate;
import eu.abc4trust.xml.IssuanceToken;
import eu.abc4trust.xml.KeyPair;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.SystemParameters;

import javax.inject.Inject;

public class Abc4TrustCryptoEngineInspectorImpl
    implements
      eu.abc4trust.cryptoEngine.inspector.CryptoEngineInspector {

  private final CryptoEngineInspector cryptoEngineInspector;
  private final BuildingBlockFactory bbf;

  // private final PresentationOrchestrationInspector orchestration;

  @Inject
  public Abc4TrustCryptoEngineInspectorImpl(final CryptoEngineInspector cryptoEngineInspector,
                                            final BuildingBlockFactory bbf
  // , PresentationOrchestrationInspector orchestration
  ) {
    this.cryptoEngineInspector = cryptoEngineInspector;
    this.bbf = bbf;
    // this.orchestration = orchestration;
  }

  @Override
  public InspectorPublicAndSecretKey setupInspectorPublicKey(final SystemParameters sp, URI mechanism,
                                                             final URI uid, final List<FriendlyDescription> friendlyInspectorDescription) throws CryptoEngineException {
    try {
      //TODO(ksa) overwrite?
      mechanism = normalizeTechnology(mechanism);

      final EcryptSystemParametersWrapper spw = new EcryptSystemParametersWrapper(sp);

      final InspectorPublicKeyTemplate publicKeyTemplate =
          cryptoEngineInspector.createInspectorPublicKeyTemplate();
      final InspectorPublicKeyTemplateWrapper templateWrapper =
          new InspectorPublicKeyTemplateWrapper(publicKeyTemplate);

      templateWrapper.setPublicKeyPrefix(uid);
      templateWrapper.setTechnology(mechanism);
      templateWrapper.setSystemParametersId(spw.getSystemParametersId());

      final KeyPair kp = cryptoEngineInspector.setupInspectorKeyPair(sp, publicKeyTemplate);

      final InspectorParametersFacade ipf =
          InspectorParametersFacade.initInspectorParameters(kp.getPublicKey(), sp);
      // Overwrite parameters UID with the value from the caller
      ipf.setInspectorId(uid);
      ipf.setFriendlyDescription(friendlyInspectorDescription);

      final Abc4TrustSecretKeyFacade isf = new Abc4TrustSecretKeyFacade();
      isf.setPrivateKey(kp.getPrivateKey());
      isf.setKeyId(uid);

      final InspectorPublicAndSecretKey keyPair = new InspectorPublicAndSecretKey();
      keyPair.publicKey = ipf.getInspectorParameters();
      keyPair.secretKey = isf.getSecretKey();
      return keyPair;
    } catch (ConfigurationException|KeyManagerException ce) {
      throw new CryptoEngineException(ce);
    }
  }

  @Override
  public List<Attribute> inspect(final PresentationToken presentationToken) throws CryptoEngineException {
    return cryptoEngineInspector.inspect(presentationToken);
    // try {
    // return orchestration.inspect(presentationToken);
    // } catch (PresentationOrchestrationException e) {
    // throw new CryptoEngineException(e);
    // }
  }

  @Override
  public List<Attribute> inspect(final IssuanceToken issuanceToken) throws CryptoEngineException {
    return cryptoEngineInspector.inspect(issuanceToken);
    // try {
    // return orchestration.inspect(issuanceToken);
    // } catch (PresentationOrchestrationException e) {
    // throw new CryptoEngineException(e);
    // }
  }

  private URI normalizeTechnology(final URI technology) throws ConfigurationException {
    initializeTechnologyUris();

    final String[] tokens = technology.toString().split(":");
    final String lastToken = tokens[tokens.length - 1].toLowerCase();
    if (lastToken.equals("idemix")) {
      return CS_TECHNOLOGY;
    } else if (lastToken.equals("cs")) {
      return CS_TECHNOLOGY;
    } else if (lastToken.equals("camenisch-shoup03")) {
      return CS_TECHNOLOGY;
    } else {
      throw new ConfigurationException("Unknown technology " + lastToken + " (" + technology + ").");
    }
  }

  private static URI CS_TECHNOLOGY = null;

  private void initializeTechnologyUris() {
    // We don't run this method in the constructor, as the building block factory might not be
    // ready at that moment.
    if (CS_TECHNOLOGY != null) {
      return;
    }
    try {
      CS_TECHNOLOGY =
          bbf.getBuildingBlockByClass(CsInspectorBuildingBlock.class).getBuildingBlockId();
    } catch (final ConfigurationException e) {
      throw new RuntimeException(e);
    }
  }
}
