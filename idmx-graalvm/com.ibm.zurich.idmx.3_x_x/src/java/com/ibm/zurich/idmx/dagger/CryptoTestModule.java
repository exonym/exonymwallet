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

//import com.google.inject.AbstractModule;
//import com.google.inject.Singleton;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.RealTestVectorHelper;
import com.ibm.zurich.idmx.interfaces.util.TestVectorHelper;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

@Module
@Deprecated
public class CryptoTestModule {

//  @Override
//  protected void configure() {
//    install(new Abc4trustModule());
//    install(new FakeSmartcardModule());
//    install(new IsolationModule());
//    this.bind(TestVectorHelper.class).to(RealTestVectorHelper.class).in(Singleton.class);
//  }

  @Provides
  @Singleton
  TestVectorHelper providesTestVectorHelper(BigIntFactory bif){
    return new RealTestVectorHelper(bif);

  }
}
