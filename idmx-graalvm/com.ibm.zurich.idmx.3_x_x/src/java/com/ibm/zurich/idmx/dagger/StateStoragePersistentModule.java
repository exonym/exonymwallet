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

import com.ibm.zurich.idmx.interfaces.orchestration.issuance.StateIssuer;
import com.ibm.zurich.idmx.interfaces.orchestration.issuance.StateRecipient;
import com.ibm.zurich.idmx.interfaces.orchestration.issuance.StateStorage;
import com.ibm.zurich.idmx.orchestration.issuance.StateStoragePersistentIssuer;
import com.ibm.zurich.idmx.orchestration.issuance.StateStoragePersistentRecipient;
import dagger.Module;
import dagger.Provides;
import eu.abc4trust.db.PersistentStorage;

import javax.inject.Singleton;

/**
 * Use this module if you want the state for issuance (issuer and recipient) to be
 * serialized and stored in the same way as other persistent data.
 * @author enr
 *
 */
@Module
public class StateStoragePersistentModule {

//  @Override
//  protected void configure() {
//    this.bind(new TypeLiteral<StateStorage<StateIssuer>>() {}).to(StateStoragePersistentIssuer.class)
//        .in(Singleton.class);
//    this.bind(new TypeLiteral<StateStorage<StateRecipient>>() {})
//        .to(StateStoragePersistentRecipient.class).in(Singleton.class);
//  }

  @Singleton
  @Provides
  StateStorage<StateIssuer> providesStateStoragePersistentIssuer(PersistentStorage ps){
    return new StateStoragePersistentIssuer(ps);
  }

  @Singleton
  @Provides
  StateStorage<StateRecipient> providesPersistentStorage(PersistentStorage ps) {
    return new StateStoragePersistentRecipient(ps);
  }

}
