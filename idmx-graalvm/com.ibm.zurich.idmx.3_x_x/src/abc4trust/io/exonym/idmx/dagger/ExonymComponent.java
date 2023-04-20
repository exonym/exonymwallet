package io.exonym.idmx.dagger;

import com.ibm.zurich.idmx.buildingBlock.factory.BuildingBlockFactory;
import com.ibm.zurich.idmx.buildingBlock.factory.BuildingBlockList;
import com.ibm.zurich.idmx.dagger.AbcCryptoEngineModule;
import com.ibm.zurich.idmx.dagger.CryptoEngineModule;
import com.ibm.zurich.idmx.dagger.StateStorageInMemoryModule;
import com.ibm.zurich.idmx.interfaces.buildingBlock.revocation.StateRevocationAuthority;
import com.ibm.zurich.idmx.interfaces.cryptoEngine.*;
import com.ibm.zurich.idmx.interfaces.orchestration.KeyGenerationOrchestration;
import com.ibm.zurich.idmx.interfaces.orchestration.issuance.*;
import com.ibm.zurich.idmx.interfaces.orchestration.presentation.PresentationOrchestrationInspector;
import com.ibm.zurich.idmx.interfaces.orchestration.presentation.PresentationOrchestrationProver;
import com.ibm.zurich.idmx.interfaces.orchestration.presentation.PresentationOrchestrationVerifier;
import com.ibm.zurich.idmx.interfaces.proofEngine.ZkDirector;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.RandomGeneration;
import com.ibm.zurich.idmx.interfaces.util.Timing;
import com.ibm.zurich.idmx.interfaces.util.group.GroupFactory;
import com.ibm.zurich.idmx.proofEngine.HashComputationForChallenge;
import dagger.Component;
import eu.abc4trust.abce.internal.issuer.credentialManager.CredentialManager;
import eu.abc4trust.abce.internal.issuer.tokenManagerIssuer.TokenManagerIssuer;
import eu.abc4trust.cryptoEngine.revocation.CryptoEngineRevocation;
import eu.abc4trust.cryptoEngine.user.CryptoEngineUser;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.revocationProxy.RevocationProxy;
import eu.abc4trust.revocationProxy.revauth.RevocationProxyAuthority;
import eu.abc4trust.util.AttributeConverter;

import javax.inject.Singleton;

@Singleton
@Component(
        modules = {ExonymModule.class, CryptoEngineModule.class,
                AbcCryptoEngineModule.class, StateStorageInMemoryModule.class}
)
public interface ExonymComponent {

    KeyManager providesKeyManager();

    RandomGeneration provideRandomGeneration();

    BigIntFactory provideBigIntFactory();

    Timing provideTiming();

    GroupFactory provideGroupFactory();

    HashComputationForChallenge provideHashComputationForChallenge();

    ZkDirector providesZkDirector();

    CredentialManager providesCredentialManagerIssuer();

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

    CryptoEngineUser provideCryptoEngineUser();

    eu.abc4trust.cryptoEngine.verifier.CryptoEngineVerifier providesCryptoEngineVerifierAbc();

    eu.abc4trust.cryptoEngine.inspector.CryptoEngineInspector providesCryptoEngineInspectorAbc();

    CryptoEngineRevocation providesCryptoEngineRevocationAbc();

    eu.abc4trust.cryptoEngine.issuer.CryptoEngineIssuer providesCryptoEngineIssuerAbc();

    StateStorage<StateIssuer> provideStateStorageStateIssuer();

    StateStorage<StateRecipient> providesStateStorageMapRecipient();

}
