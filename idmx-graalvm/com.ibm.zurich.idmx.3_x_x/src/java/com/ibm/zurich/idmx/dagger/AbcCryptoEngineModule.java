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

import com.ibm.zurich.idmix.abc4trust.cryptoEngine.*;
import com.ibm.zurich.idmx.buildingBlock.factory.BuildingBlockFactory;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import dagger.Module;
import dagger.Provides;
import eu.abc4trust.abce.internal.issuer.tokenManagerIssuer.TokenManagerIssuer;
import eu.abc4trust.cryptoEngine.user.CryptoEngineUser;
import eu.abc4trust.cryptoEngine.verifier.CryptoEngineVerifier;
import eu.abc4trust.cryptoEngine.inspector.CryptoEngineInspector;
import eu.abc4trust.cryptoEngine.revocation.CryptoEngineRevocation;
import eu.abc4trust.cryptoEngine.issuer.CryptoEngineIssuer;

import eu.abc4trust.keyManager.KeyManager;

import javax.inject.Singleton;


/*

    CryptoEngineUser provideCryptoEngineUser();
    eu.abc4trust.cryptoEngine.verifier.CryptoEngineVerifier providesCryptoEngineVerifier();
    eu.abc4trust.cryptoEngine.inspector.CryptoEngineInspector providesCryptoEngineInspector();
    eu.abc4trust.cryptoEngine.revocation.CryptoEngineRevocation providesCryptoEngineRevocation();
    eu.abc4trust.cryptoEngine.issuer.CryptoEngineIssuer providesCryptoEngineIssuer();

 */

@Module
public class AbcCryptoEngineModule { // extends AbstractModule{
//  @Override
//  protected void configure() {
//    install(new CryptoEngineModule());
//    this.bind(BuildingBlockList.class).to(BuildingBlockListAbc4trust.class).in(Singleton.class);
//    this.bind(eu.abc4trust.cryptoEngine.user.CryptoEngineUser.class)
//    .to(Abc4TrustCryptoEngineUserImpl.class).in(Singleton.class);
//    this.bind(eu.abc4trust.cryptoEngine.verifier.CryptoEngineVerifier.class)
//    .to(Abc4TrustCryptoEngineVerifierImpl.class).in(Singleton.class);
//    this.bind(eu.abc4trust.cryptoEngine.inspector.CryptoEngineInspector.class)
//    .to(Abc4TrustCryptoEngineInspectorImpl.class).in(Singleton.class);
//    this.bind(eu.abc4trust.cryptoEngine.revocation.CryptoEngineRevocation.class)
//    .to(Abc4TrustCryptoEngineRevocationImpl.class).in(Singleton.class);
//    this.bind(eu.abc4trust.cryptoEngine.issuer.CryptoEngineIssuer.class)
//    .to(Abc4TrustCryptoEngineIssuerImpl.class).in(Singleton.class);
//  }


    @Provides
    @Singleton
    CryptoEngineUser provideCryptoEngineUser(
            final com.ibm.zurich.idmx.interfaces.cryptoEngine.CryptoEngineProver cep,
            final com.ibm.zurich.idmx.interfaces.cryptoEngine.CryptoEngineRecipient cer) {
        return new Abc4TrustCryptoEngineUserImpl(cep, cer);

    }

    @Singleton
    @Provides
    CryptoEngineVerifier providesCryptoEngineVerifier(
            com.ibm.zurich.idmx.interfaces.cryptoEngine.CryptoEngineVerifier cev){
        return new Abc4TrustCryptoEngineVerifierImpl(cev);
    }

    @Singleton
    @Provides
    CryptoEngineInspector providesCryptoEngineInspector(
            final com.ibm.zurich.idmx.interfaces.cryptoEngine.CryptoEngineInspector cryptoEngineInspector,
            final BuildingBlockFactory bbf){
        return new Abc4TrustCryptoEngineInspectorImpl(cryptoEngineInspector, bbf);
    }

    @Singleton
    @Provides
    CryptoEngineRevocation providesCryptoEngineRevocation(
            final BuildingBlockFactory bbf,
            final com.ibm.zurich.idmx.interfaces.cryptoEngine.CryptoEngineRevocationAuthority cryptoEngineRevocationAuthority,
            final eu.abc4trust.abce.internal.revocation.credentialManager.CredentialManager credentialManager) {
//            final KeyManager keyManager,
//            final BigIntFactory bigIntFactory

        return new Abc4TrustCryptoEngineRevocationImpl(cryptoEngineRevocationAuthority,
                credentialManager, bbf.getKeyManager(), bbf.getBigIntFactory());
    }

    @Singleton
    @Provides
    CryptoEngineIssuer providesCryptoEngineIssuer(
            final com.ibm.zurich.idmx.interfaces.cryptoEngine.CryptoEngineIssuer cei,
            final TokenManagerIssuer tm,
            final BuildingBlockFactory bbf) {
        return new Abc4TrustCryptoEngineIssuerImpl(cei, tm, bbf);
    }

}
