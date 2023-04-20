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
import com.ibm.zurich.idmix.abc4trust.facades.IssuerParametersFacade;
import com.ibm.zurich.idmx.buildingBlock.factory.BuildingBlockFactory;
import com.ibm.zurich.idmx.buildingBlock.signature.cl.ClSignatureBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.signature.uprove.BrandsSignatureBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.systemParameters.EcryptSystemParametersGenerator;
import com.ibm.zurich.idmx.buildingBlock.systemParameters.EcryptSystemParametersTemplateWrapper;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.IssuanceOrchestrationException;
import com.ibm.zurich.idmx.parameters.issuer.IssuerPublicKeyTemplateWrapper;

import eu.abc4trust.abce.internal.issuer.tokenManagerIssuer.TokenManagerIssuer;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.cryptoEngine.issuer.CryptoEngineIssuer;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.returnTypes.IssuerParametersAndSecretKey;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.FriendlyDescription;
import eu.abc4trust.xml.IssuanceLogEntry;
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

public class Abc4TrustCryptoEngineIssuerImpl implements CryptoEngineIssuer {

  private final com.ibm.zurich.idmx.interfaces.cryptoEngine.CryptoEngineIssuer cei;
  private final TokenManagerIssuer tm;
  private final BuildingBlockFactory bbf;

  @Inject
  public Abc4TrustCryptoEngineIssuerImpl(
      final com.ibm.zurich.idmx.interfaces.cryptoEngine.CryptoEngineIssuer cei, final TokenManagerIssuer tm,
      final BuildingBlockFactory bbf) {
    this.cei = cei;
    this.tm = tm;
    this.bbf = bbf;
  }

  @Override
  public IssuanceMessageAndBoolean initIssuanceProtocol(final IssuancePolicy issuancePolicy,
                                                        final List<Attribute> issuerSpecifiedAttributes, final URI issuanceContext) throws CryptoEngineException {
    return cei.initializeIssuance(issuancePolicy, issuerSpecifiedAttributes, issuanceContext);
  }

  @Override
  public IssuanceMessageAndBoolean issuanceProtocolStep(final IssuanceMessage m)
      throws CryptoEngineException {
    return cei.issuanceStep(m);
  }

  @Override
  public SystemParameters setupSystemParameters(final int rsaKeyLength) throws CryptoEngineException {
    try {
      if (rsaKeyLength < 512) {
        // TODO(enr): Fix infinite loop for small rsaKeyLength in a better way
        throw new CryptoEngineException("Key length in crypto engine must be at least 512. "
            + "(The parameter to setupSystemParameters is expected to be an RSA key length, "
            + "not a security parameter.");
      }
      // TODO(enr): We are using some Abc4trust specific code here
      final SystemParametersTemplate spt = cei.createSystemParametersTemplate();
      final EcryptSystemParametersTemplateWrapper spw = new EcryptSystemParametersTemplateWrapper(spt);
      final double keyLength = EcryptSystemParametersGenerator.rsaEquivalentSecurityLevel(rsaKeyLength);
      spw.setSecurityLevel((int) Math.ceil(keyLength));
      spw.setStatisticalInd(80);
      spw.setHashFunction("sha-256");
      spw.setAttributeLength(256);
      final SystemParameters ret = cei.setupSystemParameters(spw.getSystemParametersTemplate());
      return ret;
    } catch (final ConfigurationException e) {
      throw new CryptoEngineException(e);
    }
  }

  @Override
  public IssuerParametersAndSecretKey setupIssuerParameters(final SystemParameters syspars,
    final int maximalNumberOfAttributes, URI technology, final URI uid, final URI revocationAuthority,
      final List<FriendlyDescription> friendlyIssuerDescription) throws CryptoEngineException {
    try {
      //TODO(ksa) overwrite??
      technology = normalizeTechnology(technology);
      final IssuerPublicKeyTemplate ipt = cei.createIssuerKeyPairTemplate();

      final IssuerPublicKeyTemplateWrapper ipw = new IssuerPublicKeyTemplateWrapper(ipt);
      ipw.setMaximalNumberOfAttributes(maximalNumberOfAttributes);
      ipw.setPublicKeyPrefix(uid);
      ipw.setTechnology(technology);
      ipw.setRevocationAuthority(revocationAuthority);

      final KeyPair kp = cei.setupIssuerKeyPair(syspars, ipw.getIssuerPublicKeyTemplate());

      final IssuerParametersFacade ipf =
          IssuerParametersFacade.initIssuerParameters(kp.getPublicKey(), syspars);
      // Overwrite parameters UID with the value from the caller
      ipf.setIssuerParametersId(uid);
      ipf.setFriendlyDescription(friendlyIssuerDescription);

      final Abc4TrustSecretKeyFacade isf = new Abc4TrustSecretKeyFacade();
      isf.setPrivateKey(kp.getPrivateKey());
      isf.setKeyId(uid);

      final IssuerParametersAndSecretKey ret = new IssuerParametersAndSecretKey();
      ret.issuerParameters = ipf.getIssuerParameters();
      ret.issuerSecretKey = isf.getSecretKey();
      return ret;
    } catch (ConfigurationException|KeyManagerException|CredentialManagerException e) {
      throw new CryptoEngineException(e);
    }
  }

  private URI normalizeTechnology(final URI technology) throws ConfigurationException {
    initializeTechnologyUris();

    final String[] tokens = technology.toString().split(":");
    for (int i = 1; i <= 2; i++) {
      final String lastToken = tokens[tokens.length - i].toLowerCase();
      if (lastToken.equals("idemix")) {
        return CL_TECHNOLOGY;
      } else if (lastToken.equals("cl")) {
        return CL_TECHNOLOGY;
      } else if (lastToken.equals("uprove")) {
        return BRANDS_TECHNOLOGY;
      } else if (lastToken.equals("u-prove")) {
        return BRANDS_TECHNOLOGY;
      } else if (lastToken.equals("brands")) {
        return BRANDS_TECHNOLOGY;
      }
    }
    throw new ConfigurationException("Unknown technology: " + technology + ".");
  }

  @Override
  public IssuanceLogEntry getIssuanceLogEntry(final URI issuanceEntryUid) throws CryptoEngineException {
    return tm.getIssuanceLogEntry(issuanceEntryUid);
  }

  @Override
  public IssuanceTokenAndIssuancePolicy extractIssuanceTokenAndPolicy(
      final IssuanceMessage issuanceMessage) throws CryptoEngineException {
    try {
      return cei.extractIssuanceTokenAndPolicy(issuanceMessage);
    } catch (final IssuanceOrchestrationException e) {
      throw new CryptoEngineException(e);
    }
  }

  @Override
  public IssuanceTokenDescription extractIssuanceTokenDescription(final IssuanceMessage issuanceMessage) {
    return cei.extractIssuanceTokenDescription(issuanceMessage);
  }

  private static URI CL_TECHNOLOGY = null;
  private static URI BRANDS_TECHNOLOGY = null;

  private void initializeTechnologyUris() {
    // We don't run this method in the constructor, as the building block factory might not be
    // ready at that moment.
    if (CL_TECHNOLOGY != null) {
      return;
    }
    try {
      //TODO change this to Implementation Id
      CL_TECHNOLOGY =
          bbf.getBuildingBlockByClass(ClSignatureBuildingBlock.class).getImplementationId();
      BRANDS_TECHNOLOGY =
          bbf.getBuildingBlockByClass(BrandsSignatureBuildingBlock.class).getImplementationId();
    } catch (final ConfigurationException e) {
      throw new RuntimeException(e);
    }
  }

}
