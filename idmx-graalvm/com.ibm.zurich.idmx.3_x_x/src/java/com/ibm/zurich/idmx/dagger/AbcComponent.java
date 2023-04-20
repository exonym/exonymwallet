package com.ibm.zurich.idmx.dagger;


import com.ibm.zurich.idmx.buildingBlock.structural.abc4TrustMessage.Abc4TrustMessageBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.credentialSpecification.CredentialSpecificationBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.presentationTokenDescription.PresentationTokenDescriptionBuildingBlock;
import com.ibm.zurich.idmx.interfaces.device.ExternalSecretsHelper;
import com.ibm.zurich.idmx.interfaces.device.ExternalSecretsManager;
import com.ibm.zurich.idmx.interfaces.orchestration.KeyGenerationOrchestration;
import com.ibm.zurich.idmx.interfaces.proofEngine.ZkDirector;
import com.ibm.zurich.idmx.proofEngine.HashComputationForChallenge;
import dagger.Component;
import javax.inject.Singleton;
import com.ibm.zurich.idmx.buildingBlock.helper.representation.damgardFujisaki.DamgardFujisakiRepresentationBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.helper.representation.modNSquare.ModNSquareRepresentationBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.helper.representation.pedersen.PedersenRepresentationBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.inspector.cs.CsInspectorBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.notEqual.inv.InverseAttributeNotEqualBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.pseudonym.scopeExclusive.ScopeExclusivePseudonymBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.pseudonym.standard.StandardPseudonymBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.rangeProof.fourSq.FourSquaresRangeProofBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.revocation.cl.ClRevocationBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.setMembership.cg.CgAttributeSetMembershipBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.signature.cl.ClSignatureBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.signature.uprove.BrandsSignatureBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.attributeSource.AttributeSourceBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.constant.ConstantBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.equality.AttributeEqualityBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.inspectorKey.InspectorPublicKeyBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.issuerKey.IssuerPublicKeyBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.linearCombination.LinearCombinationLightBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.linearCombinationModQ.LinearCombinationModQBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.mechanismSpecification.MechanismSpecificationBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.message.MessageBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.reveal.RevealAttributeBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.revocationAuthorityKey.RevocationAuthorityPublicKeyBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.systemParameters.SystemParametersBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.verifierParameters.VerifierParametersBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.systemParameters.EcryptSystemParametersGenerator;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.RandomGeneration;
import com.ibm.zurich.idmx.interfaces.util.Timing;
import com.ibm.zurich.idmx.interfaces.util.group.GroupFactory;
import com.ibm.zurich.idmx.buildingBlock.factory.BuildingBlockList;
import eu.abc4trust.abce.internal.issuer.tokenManagerIssuer.TokenManagerIssuer;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManager;
import eu.abc4trust.cryptoEngine.user.CryptoEngineUser;
import eu.abc4trust.revocationProxy.RevocationProxy;
import eu.abc4trust.revocationProxy.revauth.RevocationProxyAuthority;
import eu.abc4trust.util.AttributeConverter;
import com.ibm.zurich.idmx.buildingBlock.factory.BuildingBlockFactory;
import com.ibm.zurich.idmx.interfaces.buildingBlock.revocation.StateRevocationAuthority;
import com.ibm.zurich.idmx.interfaces.cryptoEngine.CryptoEngineInspector;
import com.ibm.zurich.idmx.interfaces.cryptoEngine.CryptoEngineIssuer;
import com.ibm.zurich.idmx.interfaces.cryptoEngine.CryptoEngineProver;
import com.ibm.zurich.idmx.interfaces.cryptoEngine.CryptoEngineRecipient;
import com.ibm.zurich.idmx.interfaces.cryptoEngine.CryptoEngineRevocationAuthority;
import com.ibm.zurich.idmx.interfaces.cryptoEngine.CryptoEngineVerifier;
import com.ibm.zurich.idmx.interfaces.orchestration.issuance.*;
import com.ibm.zurich.idmx.interfaces.orchestration.presentation.PresentationOrchestrationInspector;
import com.ibm.zurich.idmx.interfaces.orchestration.presentation.PresentationOrchestrationProver;
import com.ibm.zurich.idmx.interfaces.orchestration.presentation.PresentationOrchestrationVerifier;
import eu.abc4trust.keyManager.KeyManager;

import com.ibm.zurich.idmx.buildingBlock.structural.abc4TrustMessage.Abc4TrustMessageBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.credentialSpecification.CredentialSpecificationBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.presentationTokenDescription.PresentationTokenDescriptionBuildingBlock;


/*
    GUICE Version: Abc4trustModule.java
 */
//
@Singleton
@Component(modules = {
//        BasisModule.class,

        AbcManagerModule.class,

        CryptoEngineModule.class,
        AbcCryptoEngineModule.class,

//        FakeSmartcardModule.class,
        StateStorageInMemoryModule.class})

public interface AbcComponent {

    KeyManager providesKeyManager();
    RandomGeneration provideRandomGeneration();
    BigIntFactory provideBigIntFactory();
    Timing provideTiming();
    GroupFactory provideGroupFactory();
    HashComputationForChallenge provideHashComputationForChallenge();
    ZkDirector providesZkDirector();

    eu.abc4trust.abce.internal.issuer.credentialManager.CredentialManager providesCredentialManagerIssuer();
    eu.abc4trust.abce.internal.revocation.credentialManager.CredentialManager providesCredentialManagerRevocation();
    TokenManagerIssuer providesTokenManagerIssuer();
    eu.abc4trust.abce.internal.inspector.credentialManager.CredentialManager providesCredentialManagerInspector();
    eu.abc4trust.abce.internal.user.credentialManager.CredentialManager providesCredentialManagerUser();
    AttributeConverter providesAttributeConverter();
    RevocationProxy providesRevocationProxy();
    RevocationProxyAuthority providesRevocationProxyAuthority();

    IssuanceOrchestrationIssuer providesIssuanceOrchestrationIssuer();
    BuildingBlockFactory provideBuildingBlockFactory();
    BuildingBlockList provideBuildingBlockList();
    KeyGenerationOrchestration providesKeyGenerationOrchestration();

    com.ibm.zurich.idmx.interfaces.cryptoEngine.CryptoEngineIssuer providesCryptoEngineIssuer();
    com.ibm.zurich.idmx.interfaces.cryptoEngine.CryptoEngineRecipient providesCryptoEngineRecipient();
    com.ibm.zurich.idmx.interfaces.cryptoEngine.CryptoEngineProver providesCryptoEngineProver();
    com.ibm.zurich.idmx.interfaces.cryptoEngine.CryptoEngineVerifier providesCryptoEngineVerifier();
    com.ibm.zurich.idmx.interfaces.cryptoEngine.CryptoEngineRevocationAuthority providesCryptoEngineRevocationAuthority();
    com.ibm.zurich.idmx.interfaces.cryptoEngine.CryptoEngineInspector providesCryptoEngineInspector();

    IssuanceOrchestrationRecipient providesIssuanceOrchestrationRecipient();
    PresentationOrchestrationProver providesPresentationOrchestrationProver();
    PresentationOrchestrationVerifier providesPresentationOrchestrationVerifier();
    IssuanceOrchestrationRevocationAuthority providesIssuanceOrchestrationRevocationAuthority();
    PresentationOrchestrationInspector providesPresentationOrchestrationInspector();

    StateStorage<StateRevocationAuthority> providesStateStorageStateRevocationAuthority();

    CryptoEngineUser provideCryptoEngineUser();
    eu.abc4trust.cryptoEngine.verifier.CryptoEngineVerifier providesCryptoEngineVerifierAbc();
    eu.abc4trust.cryptoEngine.inspector.CryptoEngineInspector providesCryptoEngineInspectorAbc();
    eu.abc4trust.cryptoEngine.revocation.CryptoEngineRevocation providesCryptoEngineRevocationAbc();
    eu.abc4trust.cryptoEngine.issuer.CryptoEngineIssuer providesCryptoEngineIssuerAbc();

    // Note this moved to the BuildingBlockFactory.class because of a circular reference
    // that could not be handled by Dagger2, but could be handled by Guice.

    //    ExternalSecretsManager providesExternalSecretsManager();
    //    ExternalSecretsHelper providesExternalSecretsHelper();

    StateStorage<StateIssuer> provideStateStorageStateIssuer();
    StateStorage<StateRecipient> providesStateStorageMapRecipient();


}
