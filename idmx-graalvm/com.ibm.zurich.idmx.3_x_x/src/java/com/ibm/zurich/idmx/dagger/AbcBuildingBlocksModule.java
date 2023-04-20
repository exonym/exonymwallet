package com.ibm.zurich.idmx.dagger;

import com.ibm.zurich.idmx.buildingBlock.structural.abc4TrustMessage.Abc4TrustMessageBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.credentialSpecification.CredentialSpecificationBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.presentationTokenDescription.PresentationTokenDescriptionBuildingBlock;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

@Module
public class AbcBuildingBlocksModule {

    /*
        Abc4TrustMessageBuildingBlock providesAbc4TrustMessageBuildingBlock();
        CredentialSpecificationBuildingBlock providesCredentialSpecificationBuildingBlock();
        PresentationTokenDescriptionBuildingBlock providesPresentationTokenDescriptionBuildingBlock();
     */

    @Singleton
    @Provides
    Abc4TrustMessageBuildingBlock providesAbc4TrustMessageBuildingBlock(){
        return new Abc4TrustMessageBuildingBlock();
    }


    @Singleton
    @Provides
    CredentialSpecificationBuildingBlock providesCredentialSpecificationBuildingBlock(){
        return new CredentialSpecificationBuildingBlock();
    }


    @Singleton
    @Provides
    PresentationTokenDescriptionBuildingBlock providesPresentationTokenDescriptionBuildingBlock(){
        return new PresentationTokenDescriptionBuildingBlock();
    }


}
