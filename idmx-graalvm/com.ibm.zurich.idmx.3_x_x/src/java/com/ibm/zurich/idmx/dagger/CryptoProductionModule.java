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

import com.ibm.zurich.idmx.buildingBlock.factory.BuildingBlockList;
import com.ibm.zurich.idmx.buildingBlock.factory.BuildingBlockListIdmx;
import com.ibm.zurich.idmx.interfaces.util.DummyTestVectorHelper;
import com.ibm.zurich.idmx.interfaces.util.TestVectorHelper;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

@Module
@Deprecated
public class CryptoProductionModule {

//  @Override
//  protected void configure() {
//    install(new CryptoEngineModule());
//    this.bind(BuildingBlockList.class).to(BuildingBlockListIdemix.class).in(Singleton.class);
//    this.bind(TestVectorHelper.class).to(DummyTestVectorHelper.class).in(Singleton.class);
//  }

  @Singleton
  @Provides
  BuildingBlockList providesBuildingBlockList(){
    return new BuildingBlockListIdmx();
  }



  @Singleton
  @Provides
  TestVectorHelper providesTestVectorHelper(){
    return new DummyTestVectorHelper();
  }
}
