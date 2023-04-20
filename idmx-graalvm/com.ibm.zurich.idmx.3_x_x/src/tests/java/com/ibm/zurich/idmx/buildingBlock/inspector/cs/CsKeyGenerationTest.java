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

import java.io.IOException;

import com.ibm.zurich.idmx.buildingBlock.factory.BuildingBlockFactory;
import com.ibm.zurich.idmx.buildingBlock.signature.cl.ClSignatureBuildingBlock;
import com.ibm.zurich.idmx.dagger.AbcComponent;
import com.ibm.zurich.idmx.dagger.DaggerAbcComponent;
import com.ibm.zurich.idmx.tests.TestInitialisation;
import org.junit.Before;
import org.junit.Test;

import com.ibm.zurich.idmx.dagger.CryptoTestModule;
import com.ibm.zurich.idmx.buildingBlock.systemParameters.EcryptSystemParametersWrapper;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.SerializationException;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.RandomGeneration;
import com.ibm.zurich.idmx.interfaces.util.group.GroupFactory;
import com.ibm.zurich.idmx.jaxb.JaxbHelperClass;
import com.ibm.zurich.idmx.tests.TestUtils;

import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.xml.KeyPair;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.SystemParameters;

public class CsKeyGenerationTest extends CsInspectorTestHelper {
  private BigIntFactory bigIntFactory;
  private GroupFactory groupFactory;
  private RandomGeneration randomGeneration;


  @Before
  public void setUp() throws SerializationException, ConfigurationException, KeyManagerException,
      IOException {
    systemParameters =
        TestUtils.getResource("../../signature/sp_default.xml", SystemParameters.class, this);
    spWrapper = new EcryptSystemParametersWrapper(systemParameters);
    initSystemParameters(systemParameters, keyManager);

    AbcComponent i = TestInitialisation.INJECTOR;
    BuildingBlockFactory bbf = i.provideBuildingBlockFactory();
    bigIntFactory = i.provideBigIntFactory();
    groupFactory = i.provideGroupFactory();
    randomGeneration = i.provideRandomGeneration();



//    Injector injector = Guice.createInjector(new CryptoTestModule());
//
//    // Objects used for the CS inspector block
//    bigIntFactory = injector.getInstance(BigIntFactory.class);
//    groupFactory = injector.getInstance(GroupFactory.class);
//    randomGeneration = injector.getInstance(RandomGeneration.class);
  }

  @Test
  public void generateCsKeyPairTest() throws ConfigurationException, KeyManagerException,
      SerializationException {
    KeyPair keyPair = generateCsKeyPair();
    String xmlProof = JaxbHelperClass.serialize((new ObjectFactory()).createKeyPair(keyPair));
    System.out.println(xmlProof);
  }
}
