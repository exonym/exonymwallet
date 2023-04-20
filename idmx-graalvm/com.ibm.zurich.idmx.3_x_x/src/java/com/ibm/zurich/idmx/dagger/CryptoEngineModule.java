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
package com.ibm.zurich.idmx.dagger;

import com.ibm.zurich.idmix.abc4trust.manager.KeyManagerBasic;
import com.ibm.zurich.idmx.buildingBlock.factory.BuildingBlockFactory;
import com.ibm.zurich.idmx.buildingBlock.factory.BuildingBlockList;
import com.ibm.zurich.idmx.buildingBlock.factory.BuildingBlockListAbc4trust;
import com.ibm.zurich.idmx.buildingBlock.revocation.StateStorageMapRevocationAuthority;
import com.ibm.zurich.idmx.cryptoEngine.CryptoEngineInspectorImpl;
import com.ibm.zurich.idmx.cryptoEngine.CryptoEngineIssuerImpl;
import com.ibm.zurich.idmx.cryptoEngine.CryptoEngineProverImpl;
import com.ibm.zurich.idmx.cryptoEngine.CryptoEngineRecipientImpl;
import com.ibm.zurich.idmx.cryptoEngine.CryptoEngineRevocationAuthorityImpl;
import com.ibm.zurich.idmx.cryptoEngine.CryptoEngineVerifierImpl;
import com.ibm.zurich.idmx.interfaces.buildingBlock.revocation.StateRevocationAuthority;
import com.ibm.zurich.idmx.interfaces.cryptoEngine.CryptoEngineInspector;
import com.ibm.zurich.idmx.interfaces.cryptoEngine.CryptoEngineIssuer;
import com.ibm.zurich.idmx.interfaces.cryptoEngine.CryptoEngineProver;
import com.ibm.zurich.idmx.interfaces.cryptoEngine.CryptoEngineRecipient;
import com.ibm.zurich.idmx.interfaces.cryptoEngine.CryptoEngineRevocationAuthority;
import com.ibm.zurich.idmx.interfaces.cryptoEngine.CryptoEngineVerifier;
import com.ibm.zurich.idmx.interfaces.device.ExternalSecretsManager;
import com.ibm.zurich.idmx.interfaces.orchestration.KeyGenerationOrchestration;
import com.ibm.zurich.idmx.interfaces.orchestration.issuance.*;
import com.ibm.zurich.idmx.interfaces.orchestration.presentation.PresentationOrchestrationInspector;
import com.ibm.zurich.idmx.interfaces.orchestration.presentation.PresentationOrchestrationProver;
import com.ibm.zurich.idmx.interfaces.orchestration.presentation.PresentationOrchestrationVerifier;
import com.ibm.zurich.idmx.interfaces.proofEngine.ZkDirector;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.RandomGeneration;
import com.ibm.zurich.idmx.interfaces.util.TestVectorHelper;
import com.ibm.zurich.idmx.interfaces.util.Timing;
import com.ibm.zurich.idmx.interfaces.util.group.GroupFactory;
import com.ibm.zurich.idmx.orchestration.KeyGenerationOrchestrationImpl;
import com.ibm.zurich.idmx.orchestration.issuance.*;
import com.ibm.zurich.idmx.orchestration.presentation.PresentationOrchestrationInspectorImpl;
import com.ibm.zurich.idmx.orchestration.presentation.PresentationOrchestrationProverImpl;
import com.ibm.zurich.idmx.orchestration.presentation.PresentationOrchestrationVerifierImpl;
import com.ibm.zurich.idmx.proofEngine.HashComputationForChallenge;
import com.ibm.zurich.idmx.proofEngine.ZkDirectorImpl;
import dagger.Module;
import dagger.Provides;
import eu.abc4trust.abce.internal.issuer.credentialManager.CredentialManager;
import eu.abc4trust.abce.internal.issuer.tokenManagerIssuer.TokenManagerIssuer;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.revocationProxy.RevocationProxy;
import eu.abc4trust.util.AttributeConverter;

import javax.inject.Singleton;
import java.util.logging.Logger;


/*

  CryptoEngineIssuer providesCryptoEngineIssuer();
  CryptoEngineRecipient providesCryptoEngineRecipient();
  CryptoEngineProver providesCryptoEngineProver();
  CryptoEngineVerifier providesCryptoEngineVerifier();
  CryptoEngineRevocationAuthority providesCryptoEngineRevocationAuthority();
  CryptoEngineInspector providesCryptoEngineInspector();

  IssuanceOrchestrationRecipient providesIssuanceOrchestrationRecipient();
  PresentationOrchestrationProver providesPresentationOrchestrationProver();
  PresentationOrchestrationVerifier providesPresentationOrchestrationVerifier();
  IssuanceOrchestrationRevocationAuthority providesIssuanceOrchestrationRevocationAuthority();
  PresentationOrchestrationInspector providesPresentationOrchestrationInspector();

  StateStorage<StateRevocationAuthority> providesStateStorageStateRevocationAuthority();

 */



@Module
public class CryptoEngineModule {

//  @Override
//  protected void configure() {
//    // Crypto architecture components
//    //install(new StateStorageInMemory());
//    install(new StateStoragePersistent());
//  }


  // --> Utilities
  //    this.bind(RandomGeneration.class).to(RandomGenerationImpl.class).in(Singleton.class);
  //    this.bind(Timing.class).to(TimingImpl.class).in(Singleton.class);
  //    this.bind(BigIntFactory.class).to(BigIntFactoryImpl.class).in(Singleton.class);
  //    this.bind(GroupFactory.class).to(GroupFactoryImpl.class).in(Singleton.class);
  //
  //    // --> Classes for several parties
  //    this.bind(BuildingBlockFactory.class).in(Singleton.class);
  //    this.bind(ZkDirector.class).to(ZkDirectorImpl.class).in(Singleton.class);


  @Singleton
  @Provides
  CryptoEngineRevocationAuthority providesCryptoEngineRevocationAuthority(
          final KeyGenerationOrchestration keyGenerationOrchestration,
          final IssuanceOrchestrationRevocationAuthority issuanceOrchestration,
          final BuildingBlockFactory bbf,
          final eu.abc4trust.abce.internal.revocation.credentialManager.CredentialManager credentialManager,
          final StateStorage<StateRevocationAuthority> storage) {
    return new CryptoEngineRevocationAuthorityImpl(keyGenerationOrchestration, issuanceOrchestration,
            bbf.getRandomGeneration(), bbf.getKeyManager(), bbf.getGroupFactory(), credentialManager, storage);

  }


  @Singleton
  @Provides
  BuildingBlockList provideBuildingBlockList(){
    return new BuildingBlockListAbc4trust();
  }

  @Singleton
  @Provides
  BigIntFactory provideBigIntFactory(BuildingBlockFactory bbf) {
    return bbf.getBigIntFactory();
  }

  @Singleton
  @Provides
  Timing provideTiming(BuildingBlockFactory bbf) {
    return bbf.getTiming();
  }

  @Singleton
  @Provides
  GroupFactory provideGroupFactory(BuildingBlockFactory bbf) {
    return bbf.getGroupFactory();
  }

  @Singleton
  @Provides
  BuildingBlockFactory provideBuildingBlockFactory(BuildingBlockList list, eu.abc4trust.abce.internal.user.credentialManager.CredentialManager cm, KeyManager km) {
    return new BuildingBlockFactory(list, cm, km);
  }

//  @Singleton
//  @Provides
//  KeyManager providesKeyManager(BuildingBlockFactory bbf) {
//    return bbf.getKeyManager();
//  }
//
//  @Singleton
//  @Provides
//  eu.abc4trust.abce.internal.user.credentialManager.CredentialManager providesCredentialManagerUser(BuildingBlockFactory bbf) {
//    return bbf.getCredentialManagerUser();
//  }

  @Singleton
  @Provides
  TestVectorHelper providesTestVectorHelper(BuildingBlockFactory bbf) {
    return bbf.getTestVectorHelper();
  }

  @Singleton
  @Provides
  HashComputationForChallenge provideHashComputationForChallenge(BuildingBlockFactory bbf) {
    return bbf.getHashComputationForChallenge();
  }

  @Singleton
  @Provides
  Logger provideLogger(BuildingBlockFactory bbf) {
    return bbf.getLogger();
  }

  @Singleton
  @Provides
  IssuanceOrchestrationRevocationAuthority providesIssuanceOrchestrationRevocationAuthority(
          final StateStorage<StateRevocationAuthority> storage,
          final eu.abc4trust.abce.internal.revocation.credentialManager.CredentialManager credentialManager,
//          final KeyManager keyManager,
//          final RandomGeneration randomGeneration,
//          final GroupFactory groupFactory,
          final BuildingBlockFactory bbf) {

    return new IssuanceOrchestrationRevocationAuthorityImpl(
            storage, credentialManager, bbf.getKeyManager(),
            bbf.getRandomGeneration(),bbf.getGroupFactory());

  }

  @Singleton
  @Provides
  IssuanceOrchestrationIssuer providesIssuanceOrchestrationIssuer(
          final StateStorage<StateIssuer> storage,
          final PresentationOrchestrationVerifier presOrch,
          final CredentialManager credManager,
//          final KeyManager keyManager,
//          final BigIntFactory bigIntFactory,
//          final RandomGeneration randomGeneration,
          final BuildingBlockFactory bbf,
          final ZkDirector zkDirector,
          final AttributeConverter attributeConverter,
          final TokenManagerIssuer tokenManager,
          final RevocationProxy revocationProxy) {

    return new IssuanceOrchestrationIssuerImpl(storage, presOrch,
            credManager, bbf.getKeyManager(), bbf.getBigIntFactory(), bbf,
            zkDirector, attributeConverter, bbf.getRandomGeneration(),
            tokenManager, revocationProxy);

  }

  @Singleton
  @Provides
  ZkDirector providesZkDirector(
//          final Logger logger,
//          final BigIntFactory bigIntFactory,
//          final GroupFactory groupFactory,
//          final RandomGeneration rg,
//          final HashComputationForChallenge hcc,
          final BuildingBlockFactory bbf) {
    return new ZkDirectorImpl(bbf.getLogger(), bbf.getBigIntFactory(), bbf.getGroupFactory(),
            bbf.getRandomGeneration(), bbf.getExternalSecretsManager(), bbf.getHashComputationForChallenge());

  }

  //    // --> Credential issuer classes
  //    this.bind(CryptoEngineIssuer.class).to(CryptoEngineIssuerImpl.class).in(Singleton.class);
  //    this.bind(KeyGenerationOrchestration.class).to(KeyGenerationOrchestrationImpl.class)
  //        .in(Singleton.class);
  //    this.bind(IssuanceOrchestrationIssuer.class).to(IssuanceOrchestrationIssuerImpl.class)
  //        .in(Singleton.class);


  @Singleton
  @Provides
  CryptoEngineIssuer providesCryptoEngineIssuer(final KeyGenerationOrchestration keyGenerationOrchestration,
                                                final IssuanceOrchestrationIssuer issuanceOrchestration) {
    return new CryptoEngineIssuerImpl(keyGenerationOrchestration,issuanceOrchestration);

  }

  @Singleton
  @Provides
  KeyGenerationOrchestration providesKeyGenerationOrchestration(
//          final KeyManager keyManager,
//          final RandomGeneration randomGeneration,
          final BuildingBlockFactory bbf) {
    return new KeyGenerationOrchestrationImpl(bbf, bbf.getKeyManager(), bbf.getRandomGeneration());
  }

  //    // --> Credential recipient classes
//    this.bind(CryptoEngineRecipient.class).to(CryptoEngineRecipientImpl.class).in(Singleton.class);
//    this.bind(IssuanceOrchestrationRecipient.class).to(IssuanceOrchestrationRecipientImpl.class)
//        .in(Singleton.class);
//
//    // --> Credential presentation prover
//    this.bind(CryptoEngineProver.class).to(CryptoEngineProverImpl.class).in(Singleton.class);
//    this.bind(PresentationOrchestrationProver.class).to(PresentationOrchestrationProverImpl.class)
//        .in(Singleton.class);
//
//    // --> Credential presentation verifier
//    this.bind(CryptoEngineVerifier.class).to(CryptoEngineVerifierImpl.class).in(Singleton.class);
//    this.bind(PresentationOrchestrationVerifier.class)
//        .to(PresentationOrchestrationVerifierImpl.class).in(Singleton.class);

  @Singleton
  @Provides
  CryptoEngineRecipient providesCryptoEngineRecipient(final IssuanceOrchestrationRecipient issuanceOrchestration) {
    return new CryptoEngineRecipientImpl(issuanceOrchestration);
  }

  @Singleton
  @Provides
  IssuanceOrchestrationRecipient providesIssuanceOrchestrationRecipient(
          final StateStorage<StateRecipient> storage,
          final PresentationOrchestrationProver presOrch,
//          final eu.abc4trust.abce.internal.user.credentialManager.CredentialManager cm,
//          final KeyManager km,
//          final BigIntFactory bigIntFactory,
          final BuildingBlockFactory bbf,
          final ZkDirector zkDirector,
          final AttributeConverter attributeConverter) {

    return new IssuanceOrchestrationRecipientImpl(storage, presOrch, bbf.getCredentialManagerUser(),
            bbf.getKeyManager(), bbf.getBigIntFactory(),
            bbf,zkDirector,attributeConverter);
  }

  @Singleton
  @Provides
  CryptoEngineProver providesCryptoEngineProver(final PresentationOrchestrationProver presentationOrchestration,
                                                final BuildingBlockFactory bbf,
                                                final KeyManager km,
                                                final eu.abc4trust.abce.internal.user.credentialManager.CredentialManager credentialManager,
                                                final GroupFactory groupFactory) {
    return new CryptoEngineProverImpl(presentationOrchestration,
            bbf, km,credentialManager,groupFactory);
  }

  @Singleton
  @Provides
  PresentationOrchestrationProver providesPresentationOrchestrationProver(
          final BuildingBlockFactory bbf,
//          final BigIntFactory bigIntFactory,
//          final KeyManager keyManager,
//          final RandomGeneration randomGeneration,
          final ZkDirector zkDirector,
          final AttributeConverter attributeConverter,
          final eu.abc4trust.abce.internal.user.credentialManager.CredentialManager credentialManager) {

    return new PresentationOrchestrationProverImpl(bbf,
            bbf.getBigIntFactory(), bbf.getKeyManager(), zkDirector,
            attributeConverter, bbf.getRandomGeneration(), bbf.getCredentialManagerUser(),
            bbf.getExternalSecretsManager());

  }

  @Singleton
  @Provides
  CryptoEngineVerifier providesCryptoEngineVerifier(
          final BuildingBlockFactory bbf,
          final PresentationOrchestrationVerifier presentationOrchestration,
//          final KeyManager keyManager,
          final KeyGenerationOrchestration keyOrchestration) {
    return new CryptoEngineVerifierImpl(presentationOrchestration, bbf.getKeyManager(), keyOrchestration);

  }

  @Singleton
  @Provides
  PresentationOrchestrationVerifier providesPresentationOrchestrationVerifier(
          final BuildingBlockFactory bbf,
//          final BigIntFactory bigIntFactory,
//          final KeyManager keyManager,
//          final eu.abc4trust.abce.internal.user.credentialManager.CredentialManager credentialManager,
          final ZkDirector zkDirector,
          final AttributeConverter attributeConverter) {

    return new PresentationOrchestrationVerifierImpl(bbf, bbf.getBigIntFactory(), bbf.getKeyManager(),
            bbf.getCredentialManagerUser(), zkDirector, attributeConverter);

  }

//
//    // --> Revocation authority
//    this.bind(CryptoEngineRevocationAuthority.class).to(CryptoEngineRevocationAuthorityImpl.class)
//        .in(Singleton.class);
//    this.bind(IssuanceOrchestrationRevocationAuthority.class)
//        .to(IssuanceOrchestrationRevocationAuthorityImpl.class).in(Singleton.class);


//
//    // --> Inspector
//    this.bind(CryptoEngineInspector.class).to(CryptoEngineInspectorImpl.class).in(Singleton.class);
//    this.bind(PresentationOrchestrationInspector.class)
//        .to(PresentationOrchestrationInspectorImpl.class).in(Singleton.class);

  @Singleton
  @Provides
  CryptoEngineInspector providesCryptoEngineInspector(
          final KeyGenerationOrchestration keyGenerationOrchestration,
          final PresentationOrchestrationInspector presentationOrchestrationInspector) {
    return new CryptoEngineInspectorImpl(keyGenerationOrchestration, presentationOrchestrationInspector);
  }

  @Singleton
  @Provides
  PresentationOrchestrationInspector providesPresentationOrchestrationInspector(
          final BuildingBlockFactory bbf,
//          final BigIntFactory bigIntFactory,
//          final KeyManager keyManager,
//          final RandomGeneration rg,
          final eu.abc4trust.abce.internal.inspector.credentialManager.CredentialManager credentialManager,
          final ZkDirector zkDirector,
          final AttributeConverter attributeConverter) {

    return new PresentationOrchestrationInspectorImpl(bbf, bbf.getBigIntFactory(), bbf.getKeyManager(),
            credentialManager, zkDirector, attributeConverter, bbf.getRandomGeneration());

  }

  //
  //    this.bind(new TypeLiteral<StateStorage<StateRevocationAuthority>>() {})
  //        .to(StateStorageMapRevocationAuthority.class).in(Singleton.class);
  @Singleton
  @Provides
  StateStorage<StateRevocationAuthority> providesStateStorageStateRevocationAuthority(){
    return new StateStorageMapRevocationAuthority();

  }
}



