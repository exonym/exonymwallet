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
package com.ibm.zurich.idmx.buildingBlock.inspector.cs;

import com.ibm.zurich.idmx.buildingBlock.factory.BuildingBlockFactory;
import com.ibm.zurich.idmx.buildingBlock.signature.cl.ClSignatureBuildingBlock;
import com.ibm.zurich.idmx.dagger.AbcComponent;
import com.ibm.zurich.idmx.dagger.CryptoTestModule;
import com.ibm.zurich.idmx.buildingBlock.systemParameters.EcryptSystemParametersWrapper;
import com.ibm.zurich.idmx.dagger.DaggerAbcComponent;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.SerializationException;
import com.ibm.zurich.idmx.parameters.inspector.InspectorPublicKeyTemplateWrapper;
import com.ibm.zurich.idmx.tests.TestInitialisation;

import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.xml.InspectorPublicKeyTemplate;
import eu.abc4trust.xml.KeyPair;
import eu.abc4trust.xml.SystemParameters;

public class CsInspectorTestHelper extends TestInitialisation {
  protected CsInspectorBuildingBlock csInspectorBuildingBlock;
  protected EcryptSystemParametersWrapper spWrapper;
  protected SystemParameters systemParameters;

  public CsInspectorTestHelper() {
    try {
      AbcComponent i = TestInitialisation.INJECTOR;
      BuildingBlockFactory bbf = i.provideBuildingBlockFactory();
      bigIntFactory = i.provideBigIntFactory();
      groupFactory = i.provideGroupFactory();
      randomGeneration = i.provideRandomGeneration();
      zkDirector = i.providesZkDirector();
      csInspectorBuildingBlock = bbf.getBuildingBlockByClass(CsInspectorBuildingBlock.class);

    } catch (ConfigurationException e) {
      System.out.println("Error");

    }
//    Injector injector = Guice.createInjector(new CryptoTestModule());
//    csInspectorBuildingBlock = injector.getInstance(CsInspectorBuildingBlock.class);
  }

  public KeyPair generateCsKeyPair() throws ConfigurationException, KeyManagerException,
      SerializationException {
    // Create the template
    InspectorPublicKeyTemplate publicKeyTemplate =
        cryptoEngineInspector.createInspectorPublicKeyTemplate();
    InspectorPublicKeyTemplateWrapper templateWrapper =
        new InspectorPublicKeyTemplateWrapper(publicKeyTemplate);

    // Configure the template
    // templateWrapper.setPublicKeyPrefix(Configuration.defaultInspectorPublicKeyPrefix());
    // templateWrapper.setSystemParametersId(spWrapper.getSystemParametersId());

    KeyPair keyPair =
        csInspectorBuildingBlock.generateInspectorBuildingBlockKeyPair(systemParameters,
            templateWrapper.getInspectorPublicKeyTemplate());

    return keyPair;
  }
}
