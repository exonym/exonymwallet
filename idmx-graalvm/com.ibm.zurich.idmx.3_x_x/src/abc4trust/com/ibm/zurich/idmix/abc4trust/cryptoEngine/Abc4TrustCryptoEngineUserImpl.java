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

import com.ibm.zurich.idmx.interfaces.cryptoEngine.CryptoEngineProver;
import com.ibm.zurich.idmx.interfaces.cryptoEngine.CryptoEngineRecipient;

import eu.abc4trust.cryptoEngine.CredentialWasRevokedException;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.cryptoEngine.user.CryptoEngineUser;
import eu.abc4trust.returnTypes.IssuMsgOrCredDesc;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.Credential;
import eu.abc4trust.xml.IssuanceMessage;
import eu.abc4trust.xml.IssuancePolicy;
import eu.abc4trust.xml.IssuanceTokenDescription;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.PresentationTokenDescription;
import eu.abc4trust.xml.PseudonymWithMetadata;
import eu.abc4trust.xml.VerifierParameters;

import javax.inject.Inject;

public class Abc4TrustCryptoEngineUserImpl implements CryptoEngineUser {

  private final CryptoEngineProver cep;
  private final CryptoEngineRecipient cer;

  @Inject
  public Abc4TrustCryptoEngineUserImpl(final CryptoEngineProver cep, final CryptoEngineRecipient cer) {
    this.cep = cep;
    this.cer = cer;
  }

  @Override
  public PresentationToken createPresentationToken(final String username, final PresentationTokenDescription td,
                                                   final VerifierParameters vp, final List<URI> creds, final List<URI> pseudonyms) throws CryptoEngineException {
    return cep.createPresentationToken(username, td, vp, creds, pseudonyms);
  }

  @Override
  public IssuanceMessage createIssuanceToken(final String username, final IssuanceMessage im, final IssuanceTokenDescription itd,
                                             final List<URI> creduids, final List<URI> pseudonyms, final List<Attribute> atts) throws CryptoEngineException {
    return cer.preIssuancePresentation(username, im, itd, creduids, pseudonyms, atts);
  }

  @Override
  public IssuMsgOrCredDesc issuanceProtocolStep(final String username, final IssuanceMessage m) throws CryptoEngineException {
    return cer.issuanceStep(username, m);
  }

  @Override
  public Credential updateNonRevocationEvidence(final String username, final Credential cred, final URI raParametersUID,
                                                final List<URI> revokedatts) throws CryptoEngineException, CredentialWasRevokedException {

    final URI revinfouid =
        cep.updateRevocationInformation(username, raParametersUID, null).getRevocationInformationUID();
    return updateNonRevocationEvidence(username, cred, raParametersUID, revokedatts, revinfouid);
  }

  @Override
  public Credential updateNonRevocationEvidence(final String username, final Credential cred, final URI raparsuid,
                                                final List<URI> revokedatts, final URI revinfouid) throws CryptoEngineException,
      CredentialWasRevokedException {

    return cep.updateNonRevocationEvidence(username, cred, null, true);
  }

  @Override
  public PseudonymWithMetadata createPseudonym(final String username, final URI pseudonymUri, final String scope, final boolean exclusive,
                                               final URI secretReference) throws CryptoEngineException {
    return cep.createPseudonym(username, pseudonymUri, URI.create(scope), exclusive, secretReference);
  }

  @Override
  public boolean isRevoked(final String username, final Credential cred) throws CryptoEngineException {
    return cep.isRevoked(username, cred);
  }

  @Override
  public IssuancePolicy extractIssuancePolicy(final IssuanceMessage issuanceMessage) {
    return cer.extractIssuancePolicy(issuanceMessage);
  }

}
