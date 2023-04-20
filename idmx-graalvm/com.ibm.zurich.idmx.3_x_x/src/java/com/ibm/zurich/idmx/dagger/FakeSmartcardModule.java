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

import com.ibm.zurich.idmx.buildingBlock.factory.BuildingBlockFactory;
import com.ibm.zurich.idmx.device.ExternalSecretsHelperImpl;
import com.ibm.zurich.idmx.device.ExternalSecretsManagerImpl;
import com.ibm.zurich.idmx.interfaces.device.ExternalSecretsHelper;
import com.ibm.zurich.idmx.interfaces.device.ExternalSecretsManager;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.RandomGeneration;
import dagger.Module;
import dagger.Provides;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManager;
import eu.abc4trust.keyManager.KeyManager;

import javax.inject.Singleton;

@Module
class FakeSmartcardModule {


  /*
    ExternalSecretsManager providesExternalSecretsManager();
    ExternalSecretsHelper providesExternalSecretsHelper();
   */

//  protected void configure() {
//    this.bind(ExternalSecretsManager.class).to(ExternalSecretsManagerImpl.class).in(Singleton.class);
//    this.bind(ExternalSecretsHelper.class).to(ExternalSecretsHelperImpl.class).in(Singleton.class);
//  }

  @Singleton
  @Provides
  ExternalSecretsManager providesExternalSecretsManager(
          final BigIntFactory bigIntFactory,
          final RandomGeneration randomGeneration,
          final KeyManager keyManager,
          final BuildingBlockFactory bbf,
          final CredentialManager cm) {

    return new ExternalSecretsManagerImpl(
            bigIntFactory, randomGeneration, keyManager, bbf, cm);

  }

  @Singleton
  @Provides
  ExternalSecretsHelper providesExternalSecretsHelper(
          final BigIntFactory bigIntFactory){
    return new ExternalSecretsHelperImpl(bigIntFactory);
  }
}
