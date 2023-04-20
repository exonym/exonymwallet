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

import javax.inject.Inject;
import javax.xml.datatype.XMLGregorianCalendar;

import com.ibm.zurich.idmix.abc4trust.facades.RevocationAuthorityParametersFacade;
import com.ibm.zurich.idmix.abc4trust.facades.RevocationHistoryFacade;
import com.ibm.zurich.idmix.abc4trust.facades.RevocationInformationFacade;
import com.ibm.zurich.idmix.abc4trust.facades.RevocationLogEntryFacade;
import com.ibm.zurich.idmix.abc4trust.facades.SecretKeyFacade;
import com.ibm.zurich.idmx.annotations.Nullable;
import com.ibm.zurich.idmx.buildingBlock.revocation.cl.ClRevocationAuthorityPublicKeyWrapper;
import com.ibm.zurich.idmx.buildingBlock.revocation.cl.ClRevocationEventWrapper;
import com.ibm.zurich.idmx.buildingBlock.revocation.cl.ClRevocationSecretKeyWrapper;
import com.ibm.zurich.idmx.buildingBlock.revocation.cl.ClRevocationStateWrapper;
import com.ibm.zurich.idmx.configuration.Configuration;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.IssuanceOrchestrationException;
import com.ibm.zurich.idmx.interfaces.buildingBlock.revocation.StateRevocationAuthority;
import com.ibm.zurich.idmx.interfaces.cryptoEngine.CryptoEngineRevocationAuthority;
import com.ibm.zurich.idmx.interfaces.orchestration.KeyGenerationOrchestration;
import com.ibm.zurich.idmx.interfaces.orchestration.issuance.IssuanceOrchestrationRevocationAuthority;
import com.ibm.zurich.idmx.interfaces.orchestration.issuance.StateStorage;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.RandomGeneration;
import com.ibm.zurich.idmx.interfaces.util.group.GroupFactory;
import com.ibm.zurich.idmx.keypair.SecretKeyWrapper;
import com.ibm.zurich.idmx.keypair.ra.RevocationAuthorityPublicKeyWrapper;
import com.ibm.zurich.idmx.orchestration.issuance.IssuanceOrchestrationRevocationAuthorityImpl;

import eu.abc4trust.abce.internal.revocation.credentialManager.CredentialManager;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.KeyPair;
import eu.abc4trust.xml.NonRevocationEvidence;
import eu.abc4trust.xml.RevocationAuthorityParameters;
import eu.abc4trust.xml.RevocationAuthorityPublicKeyTemplate;
import eu.abc4trust.xml.RevocationHistory;
import eu.abc4trust.xml.RevocationInformation;
import eu.abc4trust.xml.SecretKey;
import eu.abc4trust.xml.SystemParameters;


public class CryptoEngineRevocationAuthorityImpl implements CryptoEngineRevocationAuthority {

  private final KeyGenerationOrchestration keyGenerationOrchestration;
  private final IssuanceOrchestrationRevocationAuthority issuanceOrchestration;
  private final RandomGeneration randomGeneration;

  private final KeyManager keyManager;
  private final CredentialManager credentialManager;
  // private final StateStorage<StateRevocationAuthority> storage;
  private final GroupFactory groupFactory;

  @Inject
  public CryptoEngineRevocationAuthorityImpl(final KeyGenerationOrchestration keyGenerationOrchestration,
                                             final IssuanceOrchestrationRevocationAuthority issuanceOrchestration,
      final RandomGeneration randomGeneration, final KeyManager keyManager, final GroupFactory groupFactory,
      final CredentialManager credentialManager, final StateStorage<StateRevocationAuthority> storage) {

    this.keyGenerationOrchestration = keyGenerationOrchestration;
    this.issuanceOrchestration = issuanceOrchestration;
    this.randomGeneration = randomGeneration;
    this.groupFactory = groupFactory;

    this.keyManager = keyManager;
    this.credentialManager = credentialManager;
  }

  @Override
  public RevocationAuthorityPublicKeyTemplate createRevocationAuthorityPublicKeyTemplate()
      throws ConfigurationException, KeyManagerException {
    return keyGenerationOrchestration.createRevocationAuthorityPublicKeyTemplate();
  }

  @Override
  public KeyPair setupRevocationAuthorityKeyPair(final SystemParameters systemParameters,
      final RevocationAuthorityPublicKeyTemplate template) throws ConfigurationException,
      CredentialManagerException, KeyManagerException {
    return keyGenerationOrchestration.setupRevocationAuthorityKeyPair(systemParameters, template);
  }



  @Override
  public NonRevocationEvidence newRevocationHandle(final URI raParametersId, final URI nonRevocationEvidenceId,
                                                   final List<Attribute> attributes) throws CryptoEngineException {

    try {
      return issuanceOrchestration.newRevocationHandle(raParametersId, nonRevocationEvidenceId,
          attributes);
    } catch (ConfigurationException|KeyManagerException|CredentialManagerException|IssuanceOrchestrationException e) {
      throw new CryptoEngineException(e);
    }
  }


  @Override
  public RevocationInformation updateRevocationInformation(final URI raParametersUID,
                                                           final @Nullable URI revocationInformationUID,
      final @Nullable RevocationInformation currentRevocationInformation) throws CryptoEngineException,
      KeyManagerException {

    RevocationInformation revocationInformation;
    if (revocationInformationUID != null) {
      revocationInformation =
          keyManager.getRevocationInformation(raParametersUID, revocationInformationUID);
    } else {
      revocationInformation = keyManager.getCurrentRevocationInformation(raParametersUID);
    }

    if (revocationInformation == null) {
      // TODO(enr): Better encapsulation
      try {
        revocationInformation =
            ((IssuanceOrchestrationRevocationAuthorityImpl) issuanceOrchestration)
                .initRevocation(((IssuanceOrchestrationRevocationAuthorityImpl) issuanceOrchestration)
                    .getRevocationAuthorityPublicKeyWrapper(raParametersUID).getPublicKey());
      } catch (ConfigurationException|KeyManagerException|CredentialManagerException e) {
        throw new CryptoEngineException(e);
      }
    }

    return revocationInformation;
  }

  // TODO this method is in the CryptoEngineRevocationAuthorityImpl and
  // IssuanceOrchestrationRecovationAuthorityImpl - put it in a proper location!
  private RevocationAuthorityPublicKeyWrapper getRevocationAuthorityPublicKeyWrapper(
      final URI revocationAuthorityUri) throws KeyManagerException {
    final RevocationAuthorityParameters raParameters =
        keyManager.getRevocationAuthorityParameters(revocationAuthorityUri);
    final RevocationAuthorityParametersFacade rapf =
        new RevocationAuthorityParametersFacade(raParameters);
    return new ClRevocationAuthorityPublicKeyWrapper(rapf.getPublicKey());
  }

  // TODO this method is in the CryptoEngineRevocationAuthorityImpl and
  // IssuanceOrchestrationRecovationAuthorityImpl - put it in a proper location!
  private ClRevocationSecretKeyWrapper getSecretKeyWrapper(final URI revocationAuthorityUri)
      throws CredentialManagerException {
    final SecretKey secretKey = credentialManager.getSecretKey(revocationAuthorityUri);
    final SecretKeyFacade skf = new SecretKeyFacade(secretKey);
    return new ClRevocationSecretKeyWrapper(skf.getPrivateKey());
  }



  @Override
  public URI revoke(final URI revocationAuthorityId, final BigInt revocationHandleValue)
      throws CryptoEngineException {

    // Retrieve public and secret key
    final RevocationAuthorityPublicKeyWrapper rapkWrapper;
    final SecretKeyWrapper raskWrapper;
    try {
      rapkWrapper = getRevocationAuthorityPublicKeyWrapper(revocationAuthorityId);
      raskWrapper = getSecretKeyWrapper(revocationAuthorityId);
    } catch (KeyManagerException|CredentialManagerException e) {
      throw new CryptoEngineException(e);
    }

    final RevocationInformation revocationInformation;
    try {
      revocationInformation = keyManager.getCurrentRevocationInformation(revocationAuthorityId);
    } catch (final KeyManagerException e) {
      throw new CryptoEngineException(e);
    }


    // Retrieve the state and history, create if empty.
    // StateRevocationAuthority raState = retrieveRevocationAuthorityState(revocationAuthorityId);
    // if (raState == null) {
    if (revocationInformation == null) {
      throw new RuntimeException(" revoke is invoked before the revocation has been initialised.");
      // try {
      // // TODO(enr): Better encapsulation
      // // raState =
      // revocationInformation =
      // ((IssuanceOrchestrationRevocationAuthorityImpl) issuanceOrchestration)
      // .initRevocation(((IssuanceOrchestrationRevocationAuthorityImpl) issuanceOrchestration)
      // .getRevocationAuthorityPublicKeyWrapper(revocationAuthorityId).getPublicKey());
      // } catch (ConfigurationException e) {
      // throw new CryptoEngineException(e);
      // } catch (KeyManagerException e) {
      // throw new CryptoEngineException(e);
      // } catch (CredentialManagerException e) {
      // throw new CryptoEngineException(e);
      // }
    }

    final RevocationInformationFacade revocationInformationFacade =
        new RevocationInformationFacade(revocationInformation);

    // RevocationState revocationState = raState.getRevocationState();
    // RevocationState revocationState = ;
    ClRevocationStateWrapper revocationStateWrapper =
        new ClRevocationStateWrapper(revocationInformationFacade.getRevocationState());
    final RevocationHistoryFacade publicRevocationHistoryFacade =
        new RevocationHistoryFacade(revocationInformationFacade.getRevocationHistory());
    // RevocationHistoryFacade privateRevocationHistoryFacade =
    // new RevocationHistoryFacade(credentialManager.getRevocationHistory(RevocationHistoryFacade
    // .getRevocationHistoryUID(revocationAuthorityId)));


    // check history whether this revocation handle has already been revoked
    try {
      if (!publicRevocationHistoryFacade.revocationHandleHasBeenRevoked(revocationHandleValue)) {

        final URI revocationEventId =
            URI.create("urn:idmx:3.0:revocation:log:entry:" + randomGeneration.generateRandomUid());
        //TODO(ksa) ???
        final XMLGregorianCalendar now = null;
        final ClRevocationEventWrapper revocationEventWrapper;
        final RevocationLogEntryFacade revocationLogEntryFacade;
        try {
          revocationEventWrapper =
              ClRevocationEventWrapper.removePrime(revocationStateWrapper.getRevocationState(),
                  revocationHandleValue, now, rapkWrapper.getPublicKey(), raskWrapper.getSecretKey(),
                  groupFactory);

          revocationStateWrapper =
              ClRevocationStateWrapper.applyEvent(revocationStateWrapper, revocationEventWrapper,
                  rapkWrapper, groupFactory);

          // update the revocation information
          revocationLogEntryFacade =
              publicRevocationHistoryFacade.addRevocationEvent(revocationEventWrapper.getDelegateeValue());
          revocationInformationFacade.setRevocationHistory(publicRevocationHistoryFacade
              .getDelegateeElement());
          revocationInformationFacade.setRevocationState(revocationStateWrapper.getRevocationState());

          // TODO do we need to add it to the private history as well?

        } catch (final ConfigurationException e) {
          throw new CryptoEngineException(e);
        }


        try {
          credentialManager.addRevocationLogEntry(revocationEventId,
              revocationLogEntryFacade.getDelegateeValue());
        } catch (final CredentialManagerException ex) {
          throw new CryptoEngineException(ex);
        }

        try {
          keyManager.storeCurrentRevocationInformation(revocationInformationFacade
              .getDelegateeElement());
        } catch (final KeyManagerException ex) {
          throw new CryptoEngineException(ex);
        }


        // raState.setHistory(publicRevocationHistoryFacade.getDelegateeElement());
        // storeRevocationAuthorityState(revocationAuthorityId, raState);


        // updateRevocationInformation(revocationAuthorityId, null, null);
        return revocationEventId;
      }
    } catch (final ConfigurationException e) {
      // TODO Auto-generated catch block
      if (Configuration.printStackTraces()) e.printStackTrace();
      throw new CryptoEngineException(e);
    }
    // the revocation handle has already been revoked
    return null;
  }



  // private StateRevocationAuthority retrieveRevocationAuthorityState(URI raParamtersId) {
  // StateRevocationAuthority revAuthState = null;
  // try {
  // revAuthState = storage.retrieveAndDeleteState(raParamtersId);
  // } catch (Exception ex) {
  // return null;
  // }
  // return revAuthState;
  // }
  //
  //
  // private void storeRevocationAuthorityState(URI raParamtersId, StateRevocationAuthority raState)
  // throws CryptoEngineException {
  // try {
  // storage.storeState(raParamtersId, raState);
  // } catch (IssuanceOrchestrationException e) {
  // throw new CryptoEngineException(e);
  // }
  // }



  @Override
  public RevocationHistory getRevocationHistory(final URI raParametersId) throws CryptoEngineException {
    // StateRevocationAuthority raState = retrieveRevocationAuthorityState(revocationAuthorityUri);
    // storeRevocationAuthorityState(revocationAuthorityUri, raState);
    //
    // return raState.getHistory();
    try {
      return credentialManager.getRevocationHistory(RevocationHistoryFacade
          .getRevocationHistoryUID(raParametersId));
    } catch (final CredentialManagerException e) {
      throw new CryptoEngineException(e);
    }
  }

}
