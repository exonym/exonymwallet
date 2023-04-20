package com.ibm.zurich.idmx.dagger;

import com.ibm.zurich.idmix.abc4trust.manager.CredentialManagerUserBasic;
import com.ibm.zurich.idmix.abc4trust.manager.KeyManagerBasic;
import com.ibm.zurich.idmx.buildingBlock.factory.BuildingBlockFactory;
import com.ibm.zurich.idmx.buildingBlock.factory.BuildingBlockList;
import com.ibm.zurich.idmx.buildingBlock.factory.BuildingBlockListAbc4trust;
import com.ibm.zurich.idmx.interfaces.util.*;
import com.ibm.zurich.idmx.interfaces.util.group.GroupFactory;
import com.ibm.zurich.idmx.proofEngine.HashComputationForChallenge;
import com.ibm.zurich.idmx.util.RandomGenerationImpl;
import com.ibm.zurich.idmx.util.TimingImpl;
import com.ibm.zurich.idmx.util.bigInt.BigIntFactoryImpl;
import com.ibm.zurich.idmx.util.group.GroupFactoryImpl;
import dagger.Module;
import dagger.Provides;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManager;
import eu.abc4trust.keyManager.KeyManager;

import javax.inject.Singleton;
import java.util.logging.Logger;

/*

import com.ibm.zurich.idmix.guice.BasisModule;
import com.ibm.zurich.idmx.interfaces.proofEngine.ZkDirector;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.RandomGeneration;
import com.ibm.zurich.idmx.interfaces.util.Timing;
import com.ibm.zurich.idmx.interfaces.util.group.GroupFactory;
import com.ibm.zurich.idmx.proofEngine.HashComputationForChallenge;

  KeyManager providesKeyManager();
  RandomGeneration provideRandomGeneration();
  BigIntFactory provideBigIntFactory();
  Timing provideTiming();
  GroupFactory provideGroupFactory();
  HashComputationForChallenge provideHashComputationForChallenge();
  ZkDirector providesZkDirector();
 */
@Module
public class BasisModule {

    @Singleton
    @Provides
    RandomGeneration provideRandomGeneration(BigIntFactory bif){
        return new RandomGenerationImpl(bif);

    }

    @Singleton
    @Provides
    BigIntFactory provideBigIntFactory(){
        return new BigIntFactoryImpl();

    }

    @Singleton
    @Provides
    Timing provideTiming(){
        return new TimingImpl();

    }

    @Singleton
    @Provides
    GroupFactory provideGroupFactory(){
        return new GroupFactoryImpl();

    }

    @Singleton
    @Provides
    TestVectorHelper providesTestVectorHelper(BigIntFactory bif){
        return new RealTestVectorHelper(bif);

    }

    @Singleton
    @Provides
    HashComputationForChallenge provideHashComputationForChallenge(TestVectorHelper tv){
        return new HashComputationForChallenge(tv);

    }

    @Singleton
    @Provides
    Logger provideLogger() {
        return Logger.getGlobal();

    }
}
