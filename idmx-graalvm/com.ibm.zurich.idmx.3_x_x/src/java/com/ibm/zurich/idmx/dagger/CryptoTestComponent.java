package com.ibm.zurich.idmx.dagger;

import com.ibm.zurich.idmx.dagger.*;
import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = {
        BasisModule.class,

        AbcManagerModule.class,

        CryptoEngineModule.class,
        AbcCryptoEngineModule.class,

//        FakeSmartcardModule.class,
        StateStorageInMemoryModule.class})
public interface CryptoTestComponent {



}
