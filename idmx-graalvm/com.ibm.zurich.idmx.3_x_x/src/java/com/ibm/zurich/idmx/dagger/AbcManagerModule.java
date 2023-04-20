package com.ibm.zurich.idmx.dagger;

import com.ibm.zurich.idmix.abc4trust.manager.*;
import com.ibm.zurich.idmix.abc4trust.revocation.RevocationProxyAuthorityImpl;
import com.ibm.zurich.idmix.abc4trust.revocation.RevocationProxyImpl;
import com.ibm.zurich.idmx.buildingBlock.factory.BuildingBlockFactory;
import com.ibm.zurich.idmx.interfaces.cryptoEngine.CryptoEngineRevocationAuthority;
import com.ibm.zurich.idmx.interfaces.util.RandomGeneration;
import com.ibm.zurich.idmx.util.RandomGenerationImpl;
import com.ibm.zurich.idmx.util.bigInt.BigIntFactoryImpl;
import dagger.Module;
import dagger.Provides;
import eu.abc4trust.abce.internal.issuer.tokenManagerIssuer.TokenManagerIssuer;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManager;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.revocationProxy.RevocationProxy;
import eu.abc4trust.revocationProxy.revauth.RevocationProxyAuthority;
import eu.abc4trust.util.AttributeConverter;

import javax.inject.Singleton;

/*
    eu.abc4trust.abce.internal.issuer.credentialManager.CredentialManager providesCredentialManagerIssuer();
    eu.abc4trust.abce.internal.revocation.credentialManager.CredentialManager providesCredentialManagerRevocation();
    TokenManagerIssuer providesTokenManagerIssuer();
    eu.abc4trust.abce.internal.inspector.credentialManager.CredentialManager providesCredentialManagerInspector();
    eu.abc4trust.abce.internal.user.credentialManager.CredentialManager providesCredentialManagerUser();
    AttributeConverter providesAttributeConverter();
    RevocationProxy providesRevocationProxy();
    RevocationProxyAuthority providesRevocationProxyAuthority();
 */

@Module
public class AbcManagerModule {

    @Singleton
    @Provides
    protected KeyManager providesKeyManager(){
        return new KeyManagerBasic();
    }


    @Singleton
    @Provides
    protected CredentialManager providesCredentialManagerUser(RandomGeneration randomGeneration, final KeyManager km){
        return new CredentialManagerUserBasic(randomGeneration, km);

    }
    @Singleton
    @Provides
    RandomGeneration providesRandomGeneration(){
        return new RandomGenerationImpl(new BigIntFactoryImpl());

    }

    @Singleton
    @Provides
    RevocationProxyAuthority providesRevocationProxyAuthority(CryptoEngineRevocationAuthority cryptoEngineRA) {
        return new RevocationProxyAuthorityImpl(cryptoEngineRA);

    }

    @Singleton
    @Provides
    RevocationProxy providesRevocationProxy(RevocationProxyAuthority raProxy) {
        return new RevocationProxyImpl(raProxy);
    }

    @Provides
    @Singleton
    eu.abc4trust.abce.internal.issuer.credentialManager.CredentialManager providesCredentialManagerIssuer(){
        return new CredentialManagerIssuerBasic();
    }

    @Singleton
    @Provides
    eu.abc4trust.abce.internal.revocation.credentialManager.CredentialManager providesCredentialManagerRevocation(){
        return new CredentialManagerRevocationAuthorityBasic();
    }

    @Singleton
    @Provides
    TokenManagerIssuer providesTokenManagerIssuer(){
        return new TokenManagerIssuerBasic();
    }

    @Singleton
    @Provides
    eu.abc4trust.abce.internal.inspector.credentialManager.CredentialManager providesCredentialManagerInspector(){
        return new CredentialManagerInspectorBasic();
    }

    // .user.cred...  : in Basis Module

    @Singleton
    @Provides
    AttributeConverter providesAttributeConverter(){
        return new AttributeConverterBasic2();

    }
}
