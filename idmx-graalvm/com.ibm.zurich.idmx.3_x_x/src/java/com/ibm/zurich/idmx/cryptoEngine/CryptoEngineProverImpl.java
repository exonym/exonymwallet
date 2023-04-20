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
import java.util.ListIterator;

import com.ibm.zurich.idmix.abc4trust.facades.CredentialFacade;
import com.ibm.zurich.idmix.abc4trust.facades.NonRevocationEvidenceFacade;
import com.ibm.zurich.idmix.abc4trust.facades.PseudonymCryptoFacade;
import com.ibm.zurich.idmix.abc4trust.facades.RevocationAuthorityParametersFacade;
import com.ibm.zurich.idmix.abc4trust.facades.RevocationInformationFacade;
import com.ibm.zurich.idmix.abc4trust.facades.RevocationLogEntryFacade;
import com.ibm.zurich.idmx.annotations.Nullable;
import com.ibm.zurich.idmx.buildingBlock.factory.BuildingBlockFactory;
import com.ibm.zurich.idmx.buildingBlock.pseudonym.PseudonymBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.pseudonym.scopeExclusive.ScopeExclusivePseudonymBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.pseudonym.standard.StandardPseudonymBuildingBlock;
import com.ibm.zurich.idmx.configuration.Configuration;
import com.ibm.zurich.idmx.configuration.ErrorMessages;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.PresentationOrchestrationException;
import com.ibm.zurich.idmx.exception.RevocationException;
import com.ibm.zurich.idmx.interfaces.cryptoEngine.CryptoEngineProver;
import com.ibm.zurich.idmx.interfaces.orchestration.presentation.PresentationOrchestrationProver;
import com.ibm.zurich.idmx.interfaces.util.group.GroupFactory;

import eu.abc4trust.abce.internal.user.credentialManager.CredentialManager;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.xml.AbstractPseudonym;
import eu.abc4trust.xml.Credential;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.NonRevocationEvidence;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.PresentationTokenDescription;
import eu.abc4trust.xml.Pseudonym;
import eu.abc4trust.xml.PseudonymMetadata;
import eu.abc4trust.xml.PseudonymWithMetadata;
import eu.abc4trust.xml.RevocationInformation;
import eu.abc4trust.xml.RevocationLogEntry;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.VerifierParameters;

import javax.inject.Inject;

/**
 * 
 */
public class CryptoEngineProverImpl implements CryptoEngineProver {

  private final PresentationOrchestrationProver presentationOrchestration;
  private final BuildingBlockFactory bbf;
  private final KeyManager keyManager;
  private final CredentialManager credentialManager;
  private final GroupFactory groupFactory;

  @Inject
  public CryptoEngineProverImpl(final PresentationOrchestrationProver presentationOrchestration,
                                final BuildingBlockFactory bbf, final KeyManager km, final CredentialManager credentialManager,
                                final GroupFactory groupFactory) {
    this.presentationOrchestration = presentationOrchestration;
    this.bbf = bbf;
    this.keyManager = km;
    this.credentialManager = credentialManager;
    this.groupFactory = groupFactory;
  }


  @Override
  public PresentationToken createPresentationToken(final String username, 
                                                   final PresentationTokenDescription presentationTokenDescription,
      final VerifierParameters verifierParameters, final List<URI> credentialIds, final List<URI> pseudonymIds)
      throws CryptoEngineException {
    try {
      return presentationOrchestration.createProof(username, presentationTokenDescription, credentialIds,
          pseudonymIds, verifierParameters);
    } catch (final PresentationOrchestrationException e) {
      throw new CryptoEngineException(e);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.ibm.zurich.idmx.interfaces.cryptoEngine.CryptoEngineProver#describePresentationToken(eu
   * .abc4trust.xml.PresentationTokenDescription, eu.abc4trust.xml.VerifierParameters,
   * java.util.List, java.util.List)
   */
  @Override
  public String describePresentationToken(final String username, 
                                          final PresentationTokenDescription presentationTokenDescription,
      final VerifierParameters verifierParameters, final List<URI> credentialUris, final List<URI> pseudonymUris) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Credential updateNonRevocationEvidence(final String username, final Credential credential, final @Nullable URI versionOfNre,
                                                final boolean allowLinkableUpdate) throws CryptoEngineException {

    final CredentialFacade credentialFacade = new CredentialFacade(credential);
    final NonRevocationEvidenceFacade nreFacade =
        new NonRevocationEvidenceFacade(credentialFacade.getNonRevocationEvidence());

    try {
      final URI raParametersId = nreFacade.getRevocationAuthorityParametersId();

      final RevocationInformation revocationInformation =
          updateRevocationInformation(username, raParametersId, null);

      credentialFacade.setNonRevocationEvidence(updateWitness(nreFacade, revocationInformation));

      //credentialManager.deleteCredential(username, credentialFacade.getCredentialUID());
      credentialManager.storeCredential(username, credentialFacade.getDelegateeValue());

    } catch (final ConfigurationException ex) {
      throw new CryptoEngineException(ex);
    } catch (RevocationException ex) {
      if (ex.getMessage().equals(ErrorMessages.valueHasBeenRevoked())) {
        credentialFacade.setRevoked(true);
        try {
          credentialManager.deleteCredential(username, credentialFacade.getCredentialUID());
          credentialManager.storeCredential(username, credentialFacade.getDelegateeValue());
        } catch (final CredentialManagerException e) {
          // the credential manager may throw this because we try to store a revoked credential
          // TODO only ignore the relevant exception and not all credential manager exceptions
        }
      }
    } catch (final CredentialManagerException e) {
      if (Configuration.printStackTraces()) {
        e.printStackTrace();
      }
      throw new CryptoEngineException(e);
    }

    return credentialFacade.getDelegateeValue();
  }


  @Override
  public RevocationInformation updateRevocationInformation(final String username, final URI raParametersId,
                                                           final @Nullable URI revocationInformationId) throws CryptoEngineException {

    RevocationInformation revocationInformation = null;

    try {
      // get a specific version of the revocation information - if expired, the latest version will
      // be fetched from the revocation authority
      if (revocationInformationId != null) {
        return keyManager.getRevocationInformation(raParametersId, revocationInformationId);
      }

      // if the revocation information has not been already retrieved
      try {
        revocationInformation = keyManager.getLatestRevocationInformation(raParametersId);
      } catch (final KeyManagerException e) {
        // TODO (pbi) this should be removed and the RA parameters should use a distinct UID
        revocationInformation =
            keyManager.getLatestRevocationInformation(RevocationAuthorityParametersFacade
                .getRevocationAuthorityParametersUID(raParametersId));
      }
    } catch (final KeyManagerException e) {
      throw new CryptoEngineException(e);
    }
    return revocationInformation;
  }


  @Override
  public boolean isRevoked(final String username, final Credential cred) throws CryptoEngineException {
    try {
      final CredentialSpecification credSpec =
          keyManager.getCredentialSpecification(cred.getCredentialDescription()
              .getCredentialSpecificationUID());
      if (!credSpec.isRevocable()) {
        return false;
      }
    } catch (final KeyManagerException e) {
      throw new CryptoEngineException(e);
    }

    final CredentialFacade credentialFacade =
        new CredentialFacade(updateNonRevocationEvidence(username, cred, null, false));
    if (credentialFacade.isRevoked()) {
//      try {
//      credentialManager.deleteCredential(username, credentialFacade.getCredentialUID());
//      credentialManager.storeCredential(username, credentialFacade.getDelegateeValue());
//      } catch (CredentialManagerException e) {
//      //ignore the exception thrown because of the credential that is to be stored being revoked
//      //TODO only ignore the relevant exception and not all credential manager exceptions
//      }
      return true;
    } else {
      return false;
    }
  }


  @Override
  public PseudonymWithMetadata createPseudonym(final String username, final URI pseudonymId, final URI scope,
                                               final boolean isScopeExclusive, final URI secretLocation) throws CryptoEngineException {
    try {
      final PseudonymBuildingBlock bb;
      if (isScopeExclusive) {
        bb = bbf.getBuildingBlockByClass(ScopeExclusivePseudonymBuildingBlock.class);
      } else {
        bb = bbf.getBuildingBlockByClass(StandardPseudonymBuildingBlock.class);
      }
      final SystemParameters sp = keyManager.getSystemParameters();
      final VerifierParameters verifierParameters = null;
      final AbstractPseudonym ap = bb.createPseudonym(sp, verifierParameters, secretLocation, username, scope);

      final ObjectFactory of = new ObjectFactory();

      final Pseudonym nym = of.createPseudonym();
      nym.setExclusive(isScopeExclusive);
      nym.setPseudonymUID(pseudonymId);
      nym.setPseudonymValue(bb.getPseudonymAsBytes(ap));
      nym.setScope(scope.toString());
      nym.setSecretReference(secretLocation);

      final PseudonymCryptoFacade pcf = new PseudonymCryptoFacade();
      pcf.setAbstractPseudonym(ap);

      final PseudonymMetadata metadata = of.createPseudonymMetadata();
      metadata.setMetadata(of.createMetadata());
      metadata.setHumanReadableData(scope.toString());

      final PseudonymWithMetadata pwm = of.createPseudonymWithMetadata();
      pwm.setCryptoParams(pcf.getCryptoParams());
      pwm.setPseudonymMetadata(metadata);
      pwm.setPseudonym(nym);

      return pwm;
    } catch (ConfigurationException|KeyManagerException e) {
      throw new CryptoEngineException(e);
    }
  }


  private NonRevocationEvidence updateWitness(final NonRevocationEvidenceFacade nreFacade,
                                              final RevocationInformation revocationInformation) throws RevocationException,
      ConfigurationException {

    final int nreEpoch = nreFacade.getEpoch();
    NonRevocationEvidenceFacade currentWitness = nreFacade;

    final RevocationInformationFacade revocationInformationFacade =
        new RevocationInformationFacade(revocationInformation);


    final List<RevocationLogEntry> revocationLogEntries =
        revocationInformationFacade.getRevocationLogEntries();
    final ListIterator<RevocationLogEntry> listIterator =
        revocationLogEntries.listIterator(revocationLogEntries.size());
    while (listIterator.hasPrevious()) {
      final RevocationLogEntry revocationLogEntry = listIterator.previous();
      final RevocationLogEntryFacade revocationLogEntryFacade =
          new RevocationLogEntryFacade(revocationLogEntry);

      if (nreEpoch >= revocationLogEntryFacade.getNewEpoch()) {
        // The witness has already been updated with this event
        continue;
      }
      currentWitness =
          currentWitness.updateWitness(revocationLogEntryFacade.getRevocationEvent(), groupFactory);
    }
    return currentWitness.getDelegateeElement();
  }
}
