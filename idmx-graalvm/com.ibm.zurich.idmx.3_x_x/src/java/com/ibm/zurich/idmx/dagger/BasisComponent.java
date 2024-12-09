package com.ibm.zurich.idmx.dagger;

import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.RandomGeneration;
import com.ibm.zurich.idmx.interfaces.util.TestVectorHelper;
import com.ibm.zurich.idmx.interfaces.util.Timing;
import com.ibm.zurich.idmx.interfaces.util.group.GroupFactory;
import com.ibm.zurich.idmx.proofEngine.HashComputationForChallenge;
import dagger.Component;

import javax.inject.Singleton;
import java.util.logging.Logger;

@Singleton
@Component(modules = {
        BasisModule.class
})
public interface BasisComponent {
    RandomGeneration provideRandomGeneration();
    BigIntFactory provideBigIntFactory();
    Timing provideTiming();
    GroupFactory provideGroupFactory();
    TestVectorHelper providesTestVectorHelper();
    HashComputationForChallenge provideHashComputationForChallenge();
    Logger provideLogger();
}
