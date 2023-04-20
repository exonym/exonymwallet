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

import com.ibm.zurich.idmix.abc4trust.manager.AttributeConverterBasic2;
import com.ibm.zurich.idmix.abc4trust.manager.CredentialManagerInspectorBasic;
import com.ibm.zurich.idmix.abc4trust.manager.CredentialManagerIssuerBasic;
import com.ibm.zurich.idmix.abc4trust.manager.CredentialManagerRevocationAuthorityBasic;
import com.ibm.zurich.idmix.abc4trust.manager.CredentialManagerUserBasic;
import com.ibm.zurich.idmix.abc4trust.manager.SimplePersistentStorage;
import com.ibm.zurich.idmix.abc4trust.manager.TokenManagerIssuerBasic;
import com.ibm.zurich.idmix.abc4trust.revocation.RevocationProxyAuthorityImpl;
import com.ibm.zurich.idmix.abc4trust.revocation.RevocationProxyImpl;
import com.ibm.zurich.idmx.buildingBlock.factory.BuildingBlockList;

import com.ibm.zurich.idmx.buildingBlock.factory.BuildingBlockListIdmx;
import com.ibm.zurich.idmx.interfaces.cryptoEngine.CryptoEngineRevocationAuthority;
import com.ibm.zurich.idmx.interfaces.util.RandomGeneration;
import dagger.Module;
import dagger.Provides;
import eu.abc4trust.abce.internal.issuer.tokenManagerIssuer.TokenManagerIssuer;
import eu.abc4trust.db.PersistentStorage;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.revocationProxy.RevocationProxy;
import eu.abc4trust.revocationProxy.revauth.RevocationProxyAuthority;
import eu.abc4trust.util.AttributeConverter;

import javax.inject.Singleton;

// import com.ibm.zurich.idmix.abc4trust.util.AttributeConverterBasic;

@Module
class IsolationModule  {

//  @Override
//  protected void configure() {
//    // ABC4Trust components (only in isolation mode needed)
//    this.bind(KeyManager.class).to(KeyManagerBasic.class).in(Singleton.class);
//    this.bind(eu.abc4trust.abce.internal.issuer.credentialManager.CredentialManager.class)
//        .to(CredentialManagerIssuerBasic.class).in(Singleton.class);
//    this.bind(eu.abc4trust.abce.internal.revocation.credentialManager.CredentialManager.class)
//        .to(CredentialManagerRevocationAuthorityBasic.class).in(Singleton.class);
//    this.bind(eu.abc4trust.abce.internal.user.credentialManager.CredentialManager.class)
//        .to(CredentialManagerUserBasic.class).in(Singleton.class);
//    this.bind(eu.abc4trust.abce.internal.inspector.credentialManager.CredentialManager.class)
//        .to(CredentialManagerInspectorBasic.class).in(Singleton.class);
//    this.bind(BuildingBlockList.class).to(BuildingBlockListAbc4trust.class).in(Singleton.class);
//    this.bind(AttributeConverter.class).to(AttributeConverterBasic2.class).in(Singleton.class);
//    this.bind(TokenManagerIssuer.class).to(TokenManagerIssuerBasic.class).in(Singleton.class);
//    this.bind(RevocationProxy.class).to(RevocationProxyImpl.class).in(Singleton.class);
//    this.bind(RevocationProxyAuthority.class).to(RevocationProxyAuthorityImpl.class)
//        .in(Singleton.class);
//    this.bind(PersistentStorage.class).to(SimplePersistentStorage.class).in(Singleton.class);
//  }


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

  @Singleton
  @Provides
  AttributeConverter providesAttributeConverter(){
    return new AttributeConverterBasic2();

  }

  @Singleton
  @Provides
  BuildingBlockList providesBuildingBlockList(){
    return new BuildingBlockListIdmx();

  }

  @Singleton
  @Provides
  RevocationProxy providesRevocationProxy(RevocationProxyAuthority raProxy) {
    return new RevocationProxyImpl(raProxy);
  }

  @Singleton
  @Provides
  RevocationProxyAuthority providesRevocationProxyAuthority(CryptoEngineRevocationAuthority cryptoEngineRA) {
    return new RevocationProxyAuthorityImpl(cryptoEngineRA);

  }

  @Singleton
  @Provides
  PersistentStorage providesPersistentStorage(){
    return new SimplePersistentStorage();

  }
}
