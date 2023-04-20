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

package com.ibm.zurich.idmx.orchestration.issuance;

import java.net.URI;
import java.util.List;

import com.ibm.zurich.idmix.abc4trust.facades.NonRevocationEvidenceFacade;
import com.ibm.zurich.idmix.abc4trust.facades.RevocationAuthorityParametersFacade;
import com.ibm.zurich.idmix.abc4trust.facades.RevocationHistoryFacade;
import com.ibm.zurich.idmix.abc4trust.facades.RevocationInformationFacade;
import com.ibm.zurich.idmix.abc4trust.facades.RevocationLogEntryFacade;
import com.ibm.zurich.idmix.abc4trust.facades.SecretKeyFacade;
import com.ibm.zurich.idmx.buildingBlock.factory.BuildingBlockFactory;
import com.ibm.zurich.idmx.buildingBlock.revocation.cl.ClRevocationAuthorityPublicKeyWrapper;
import com.ibm.zurich.idmx.buildingBlock.revocation.cl.ClRevocationSecretKeyWrapper;
import com.ibm.zurich.idmx.buildingBlock.revocation.cl.ClRevocationStateWrapper;
import com.ibm.zurich.idmx.configuration.Configuration;
import com.ibm.zurich.idmx.configuration.ErrorMessages;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.IssuanceOrchestrationException;
import com.ibm.zurich.idmx.interfaces.buildingBlock.revocation.StateRevocationAuthority;
import com.ibm.zurich.idmx.interfaces.orchestration.issuance.IssuanceOrchestrationRevocationAuthority;
import com.ibm.zurich.idmx.interfaces.orchestration.issuance.StateStorage;
import com.ibm.zurich.idmx.interfaces.proofEngine.ZkDirector;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.RandomGeneration;
import com.ibm.zurich.idmx.interfaces.util.group.GroupFactory;
import com.ibm.zurich.idmx.interfaces.util.group.HiddenOrderGroup;
import com.ibm.zurich.idmx.interfaces.util.group.HiddenOrderGroupElement;
import com.ibm.zurich.idmx.interfaces.util.group.KnownOrderGroup;
import com.ibm.zurich.idmx.keypair.PublicKeyWrapper;
import com.ibm.zurich.idmx.keypair.SecretKeyWrapper;
import com.ibm.zurich.idmx.keypair.ra.RevocationAuthorityPublicKeyWrapper;

import eu.abc4trust.abce.internal.issuer.tokenManagerIssuer.TokenManagerIssuer;
import eu.abc4trust.abce.internal.revocation.credentialManager.CredentialManager;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.revocationProxy.RevocationProxy;
import eu.abc4trust.util.AttributeConverter;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.NonRevocationEvidence;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PrivateKey;
import eu.abc4trust.xml.PublicKey;
import eu.abc4trust.xml.RevocationAuthorityParameters;
import eu.abc4trust.xml.RevocationInformation;
import eu.abc4trust.xml.RevocationState;
import eu.abc4trust.xml.SecretKey;
import eu.abc4trust.xml.SystemParameters;

import javax.inject.Inject;

public class IssuanceOrchestrationRevocationAuthorityImpl
    implements
      IssuanceOrchestrationRevocationAuthority {

  @SuppressWarnings("unused")
  private final StateStorage<StateRevocationAuthority> storage;
  private final CredentialManager credentialManager;
  private final KeyManager keyManager;
  private final RandomGeneration randomGeneration;
  private final GroupFactory groupFactory;

  // private final BigIntFactory bigIntFactory;
  // private final BuildingBlockFactory bbf;
  // private final ZkDirector zkDirector;
  // private final AttributeConverter attributeConverter;
  // private final TokenManagerIssuer tokenManager;
  // private final RevocationProxy revocationProxy;


  @Inject
  public IssuanceOrchestrationRevocationAuthorityImpl(
      final StateStorage<StateRevocationAuthority> storage, final CredentialManager credentialManager,
      final KeyManager keyManager, final RandomGeneration randomGeneration, final GroupFactory groupFactory) {

    this.storage = storage;
    this.credentialManager = credentialManager;
    this.keyManager = keyManager;
    this.randomGeneration = randomGeneration;
    this.groupFactory = groupFactory;

    // this.bigIntFactory = bigIntFactory;
    // this.bbf = bbf;
    // this.zkDirector = zkDirector;
    // this.attributeConverter = attributeConverter;
    // this.tokenManager = tokenManager;
    // this.revocationProxy = revocationProxy;
  }



  @Override
  public NonRevocationEvidence newRevocationHandle(final URI revocationAuthorityId,
                                                   final URI nonRevocationEvidenceId, final List<Attribute> attributes) throws ConfigurationException,
      KeyManagerException, CredentialManagerException, IssuanceOrchestrationException {

    // Retrieve public and secret key.
    final PublicKeyWrapper rapkWrapper = getRevocationAuthorityPublicKeyWrapper(revocationAuthorityId);
    final SecretKeyWrapper raskWrapper = getSecretKeyWrapper(revocationAuthorityId);


    // StateRevocationAuthority raState = storage.retrieveAndDeleteState(revocationAuthorityId);
    // if (raState == null) {
    // raState = initRevocation(rapkWrapper.getPublicKey());
    // }
    // assert (raState.getRevocationState() != null);

    final RevocationInformation revocationInformation =
        keyManager.getCurrentRevocationInformation(revocationAuthorityId);
    if (revocationInformation == null) {
      initRevocation(rapkWrapper.getPublicKey());
    }

    final RevocationInformationFacade revocationInformationFacade =
        new RevocationInformationFacade(
            keyManager.getCurrentRevocationInformation(revocationAuthorityId));
    final ClRevocationStateWrapper revocationStateWrapper =
        new ClRevocationStateWrapper(revocationInformationFacade.getRevocationState());

    // TODO use the Id to get the appropriate system parameters
    // SystemParameters systemParameters =
    // keyManager.getSystemParameters(rapkWrapper.getSystemParametersId());
    final SystemParameters systemParameters = keyManager.getSystemParameters();

    // Create a revocation handle
    // BigInt revocationHandleValue = raState.generateRevocationHandle(rapkWrapper.getPublicKey());
    final BigInt revocationHandleValue =
        revocationStateWrapper.generateRevocationHandle(systemParameters,
            rapkWrapper.getPublicKey(), randomGeneration);

    // // save the state
    // storage.storeState(revocationAuthorityId, raState);

    final NonRevocationEvidence nre =
        calculateWitness(revocationStateWrapper.getRevocationState(), revocationHandleValue,
            rapkWrapper.getPublicKey(), raskWrapper.getSecretKey());

    final NonRevocationEvidenceFacade nreFacade = new NonRevocationEvidenceFacade(nre);
    final Attribute revocationHandleAttribute;
    if (attributes != null && !attributes.isEmpty()) {
      revocationHandleAttribute = attributes.get(0);
    } else {
      revocationHandleAttribute = new ObjectFactory().createAttribute();
      revocationHandleAttribute.setAttributeDescription(new ObjectFactory()
          .createAttributeDescription());
      revocationHandleAttribute.getAttributeDescription().setType(
          URI.create("http://abc4trust.eu/wp2/abcschemav1.0/revocationhandle"));
      revocationHandleAttribute.getAttributeDescription().setDataType(URI.create("xs:integer"));
      revocationHandleAttribute.getAttributeDescription().setEncoding(
          URI.create("urn:abc4trust:1.0:encoding:integer:unsigned"));
    }
    nreFacade.fillIn(nonRevocationEvidenceId, revocationHandleValue, revocationHandleAttribute);
    try {
      credentialManager.storeNonRevocationEvidence(nreFacade.getDelegateeElement());
    } catch (final CredentialManagerException ex) {
      throw new IssuanceOrchestrationException(ex);
    }


    // update revocation history (on the revocation authority side)
    final URI raParametersUID =
        RevocationAuthorityParametersFacade.getRevocationAuthorityParametersUID(rapkWrapper
            .getPublicKeyId());
    final RevocationHistoryFacade revocationHistoryFacade =
        new RevocationHistoryFacade(credentialManager.getRevocationHistory(RevocationHistoryFacade
            .getRevocationHistoryUID(raParametersUID)));

    final RevocationLogEntryFacade revocationLogEntryFacade =
        new RevocationLogEntryFacade(nreFacade, false);
    credentialManager.addRevocationLogEntry(revocationLogEntryFacade.getRevocationLogEntryId(),
        revocationLogEntryFacade.getDelegateeValue());

    revocationHistoryFacade.addRevocationLogEntry(revocationLogEntryFacade.getDelegateeValue());
    credentialManager.storeRevocationHistory(revocationHistoryFacade.getRevocationHistoryId(),
        revocationHistoryFacade.getDelegateeElement());



    // try {
    //
    // // TODO make this CLEANER!!!
    // URI revocationLogEntryUid =
    // URI.create("urn:abc4trust:1.0:revocation:log:entry"
    // + randomGeneration.generateRandomUid());
    //
    // AttributeInLogEntry rHandleInLog = new AttributeInLogEntry();
    // rHandleInLog.setAttributeType(revocationHandleAttribute.getAttributeDescription().getType());
    // rHandleInLog.setAttributeValue(revocationHandleAttribute.getAttributeValue());
    //
    // RevocationLogEntryFacade revocationLogEntryFacade =
    // new RevocationLogEntryFacade(revocationLogEntryUid, rHandleInLog, nreFacade.getCreated(),
    // false);
    //
    // credentialManager.addRevocationLogEntry(revocationHandleAttribute.getAttributeUID(),
    // revocationLogEntryFacade.getDelegateeValue());
    // } catch (CredentialManagerException ex) {
    // throw new IssuanceOrchestrationException(ex);
    // }

    return nreFacade.getDelegateeElement();
  }

  // TODO this method is in the CryptoEngineRevocationAuthorityImpl and
  // IssuanceOrchestrationRecovationAuthorityImpl - put it in a proper location!
  public RevocationAuthorityPublicKeyWrapper getRevocationAuthorityPublicKeyWrapper(
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

  public RevocationInformation initRevocation(final PublicKey pk) throws ConfigurationException,
      KeyManagerException, CredentialManagerException {

    final RevocationAuthorityPublicKeyWrapper raPublicKeyWrapper =
        new ClRevocationAuthorityPublicKeyWrapper(pk);

    // TODO get the system parameters using the system parameters id
    // SystemParameters systemParameters =
    // keyManager.getSystemParameters(raPublicKeyWrapper.getSystemParametersId());
    final SystemParameters systemParameters = keyManager.getSystemParameters();

    final ClRevocationStateWrapper revocationStateWrapper =
        ClRevocationStateWrapper.getEmptyAccumulator(systemParameters,
            raPublicKeyWrapper.getPublicKey(), groupFactory);

    // Create Revocation History (this may be obsolete)
    final RevocationHistoryFacade revocationHistoryFacade =
        new RevocationHistoryFacade(raPublicKeyWrapper.getPublicKeyId());
    credentialManager.storeRevocationHistory(revocationHistoryFacade.getRevocationHistoryId(),
        revocationHistoryFacade.getDelegateeElement());

    // Create Revocation Information
    final RevocationInformationFacade revocationInformationFacade =
        new RevocationInformationFacade(revocationStateWrapper.getRevocationState(),
            revocationHistoryFacade.getDelegateeElement());
    keyManager.storeCurrentRevocationInformation(revocationInformationFacade.getDelegateeElement());


    // return new StateRevocationAuthorityImpl(keyManager.getSystemParameters(),
    // revocationStateWrapper.getRevocationState(), new ObjectFactory().createRevocationHistory(),
    // randomGeneration);
    return revocationInformationFacade.getDelegateeElement();
  }

  /**
   * Compute witness using private key.
   * 
   * @throws ConfigurationException
   */
  public NonRevocationEvidence calculateWitness(final RevocationState revocationState,
                                                final BigInt revocationHandleValue, final PublicKey raPk, final PrivateKey raSk) throws ConfigurationException {

    final ClRevocationSecretKeyWrapper skWrapper = new ClRevocationSecretKeyWrapper(raSk);
    final ClRevocationAuthorityPublicKeyWrapper pkWrapper =
        new ClRevocationAuthorityPublicKeyWrapper(raPk);
    final ClRevocationStateWrapper revocationStateWrapper = new ClRevocationStateWrapper(revocationState);

    if (!skWrapper.getModulus().equals(pkWrapper.getModulus())) {
      throw new RuntimeException(
          ErrorMessages
              .wrongUsage("invalid public key / secret key combination in RevocationEvent:calculateWitness"));
    }

    //TODO(ksa) do not believe
    final BigInt p = skWrapper.getSophieGermainPrimeP(); //skWrapper.getSafePrimeP();
    final BigInt q = skWrapper.getSophieGermainPrimeQ();//skWrapper.getSafePrimeQ();
    final KnownOrderGroup group = groupFactory.createResiduesModPQGroup(p, q);
    final HiddenOrderGroup heg = pkWrapper.getGroup(groupFactory);

    // TODO check if the elements are group elements
    final BigInt rhInverse = group.valueOfNoCheck(revocationHandleValue).invert().toBigInt();
    final HiddenOrderGroupElement witness =
        heg.valueOfNoCheck(revocationStateWrapper.getAccumulatorValue()).multOp(rhInverse);

    if (Configuration.debug()) {
      System.err.println("New revocation witness");
      System.err.println("u = " + witness.toBigInt());
      System.err.println("e = " + revocationHandleValue);
      System.err.println("v = " + revocationStateWrapper.getAccumulatorValue());
      System.err.println("u^e = " + witness.multOp(revocationHandleValue).toBigInt());
      System.err.println("mod = " + pkWrapper.getModulus());
    }

    return new NonRevocationEvidenceFacade(revocationState, witness.toBigInt(), raPk)
        .getDelegateeElement();
  }
}
