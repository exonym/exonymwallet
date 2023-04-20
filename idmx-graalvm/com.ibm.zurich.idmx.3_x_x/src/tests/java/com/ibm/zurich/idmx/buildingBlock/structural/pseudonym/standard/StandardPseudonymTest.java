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
package com.ibm.zurich.idmx.buildingBlock.structural.pseudonym.standard;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;

import com.ibm.zurich.idmx.buildingBlock.factory.BuildingBlockFactory;
import com.ibm.zurich.idmx.dagger.AbcComponent;
import com.ibm.zurich.idmx.tests.TestInitialisation;
import org.junit.Before;
import org.junit.Test;

import com.ibm.zurich.idmx.buildingBlock.pseudonym.PseudonymBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.pseudonym.standard.StandardPseudonymBuildingBlock;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.ProofException;
import com.ibm.zurich.idmx.exception.SerializationException;
import com.ibm.zurich.idmx.interfaces.proofEngine.ZkDirector;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProver;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleVerifier;
import com.ibm.zurich.idmx.jaxb.JaxbHelperClass;
import com.ibm.zurich.idmx.tests.TestUtils;

import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.StandardPseudonym;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.ValueInZkProof;
import eu.abc4trust.xml.ZkProof;

public class StandardPseudonymTest {
  private SystemParameters systemParameters;
  private PseudonymBuildingBlock pseudonymBB;
  private PseudonymBuildingBlock pseudonymBB1;
  private ZkDirector director;
  private KeyManager keyManager;

  public static final URI DEVICE_URI = URI.create("theSmartcard");
  private static final String USERNAME = "user";

  @Before
  public void setUp() throws SerializationException, ConfigurationException {
    systemParameters = TestUtils.getResource("sp_default.xml", SystemParameters.class, this);
    AbcComponent inj = TestInitialisation.INJECTOR;
    BuildingBlockFactory bbf = inj.provideBuildingBlockFactory();
    pseudonymBB = bbf.getBuildingBlockByClass(StandardPseudonymBuildingBlock.class);
    pseudonymBB1 = bbf.getBuildingBlockByClass(StandardPseudonymBuildingBlock.class);
    director = inj.providesZkDirector();
    keyManager = inj.providesKeyManager();

//    Injector injector = Guice.createInjector(new CryptoTestModule());
//    pseudonymBB = injector.getInstance(StandardPseudonymBuildingBlock.class);
//    pseudonymBB1 = injector.getInstance(StandardPseudonymBuildingBlock.class);
//    director = injector.getInstance(ZkDirector.class);
//    keyManager = injector.getInstance(KeyManager.class);
  }

  // static final String signatureFilename(int i) {
  // return TestSystemParameters.BASE_LOCATION.resolve("issuance/brandsSignature" + i + ".xml")
  // .toString();
  // }

  @Test
  public void standardPseudonymTest() throws SerializationException, ConfigurationException,
      IOException, ProofException, KeyManagerException {
    String identifierOfModule = "test";
    URI deviceUid = URI.create("theSmartcard");
    keyManager.storeSystemParameters(systemParameters);
    URI scope = null; // URI.create("http://zurich.ibm.com/scope1");

    StandardPseudonym pseudonym;

    // create pseudonym

    pseudonym =
        (StandardPseudonym) pseudonymBB.createPseudonym(systemParameters, null, deviceUid, USERNAME, scope);

    // prover side

    ZkModuleProver zkp =
        pseudonymBB.getZkModuleProver(systemParameters, null, identifierOfModule, pseudonym,
            deviceUid, USERNAME, scope);
    ZkProof proof = director.buildProof(USERNAME, Collections.singletonList(zkp), systemParameters);

    System.out.println(JaxbHelperClass.serialize(new ObjectFactory().createZkProof(proof)));

    // verifier side

    ZkModuleVerifier zkv =
        pseudonymBB.getZkModuleVerifier(systemParameters, null, identifierOfModule, scope,
            pseudonym.getValue().toByteArray());

    boolean ok = director.verifyProof(proof, Collections.singletonList(zkv), systemParameters);
    assertTrue(ok);
  }


  @Test
  public void bitFlipPseudonymTest() throws SerializationException, ConfigurationException,
      IOException, ProofException, KeyManagerException {
    String identifierOfModule = "test";

    URI deviceUid = URI.create("theSmartcard");
    keyManager.storeSystemParameters(systemParameters);
    URI scope = null; // URI.create("http://zurich.ibm.com/scope1");

    StandardPseudonym pseudonym;

    // create pseudonym

    pseudonym =
        (StandardPseudonym) pseudonymBB.createPseudonym(systemParameters, null, deviceUid, USERNAME, scope);

    // prover side

    ZkModuleProver zkp =
        pseudonymBB.getZkModuleProver(systemParameters, null, identifierOfModule, pseudonym,
            deviceUid, USERNAME, scope);
    ZkProof proof = director.buildProof(USERNAME, Collections.singletonList(zkp), systemParameters);

    System.out.println(JaxbHelperClass.serialize(new ObjectFactory().createZkProof(proof)));

    ValueInZkProof valInZKP = proof.getSValue().get(0);
    byte[] tempVal = valInZKP.getValue();
    int len = tempVal.length;
    int ind = len - 1;
    // System.out.println(((int) tempVal[ind]));
    if ((tempVal[ind] & 0x0004) == 0x0004) {
      tempVal[ind] = (byte) (tempVal[ind] & 0xFFFB);
      // System.out.println((int) tempVal[ind]);
    } else {
      tempVal[ind] = (byte) (tempVal[ind] | 0x0004);
      // System.out.println((int) tempVal[ind]);
    }

    valInZKP.setValue(tempVal);
    proof.getSValue().set(0, valInZKP);

    // verifier side

    ZkModuleVerifier zkv =
        pseudonymBB.getZkModuleVerifier(systemParameters, null, identifierOfModule, scope,
            pseudonym.getValue().toByteArray());

    boolean ok = !director.verifyProof(proof, Collections.singletonList(zkv), systemParameters);
    assertTrue(ok);
  }

  // prover and verifier use different pseudonym -- should not be provable
  @Test
  public void failingPseudonymTest() throws SerializationException, ConfigurationException,
      IOException, ProofException, KeyManagerException {
    String identifierOfModule = "test";

    URI deviceUid = URI.create("theSmartcard");
    keyManager.storeSystemParameters(systemParameters);
    URI scope = null; // URI.create("http://zurich.ibm.com/scope1");
    // esManager.getBaseForDeviceSecret(deviceUid, credentialUri);

    // TODO problem: esManager does not work without a credential URI; null pointer

    StandardPseudonym pseudonym;
    StandardPseudonym pseudonym1;

    // create pseudonym

    pseudonym =
        (StandardPseudonym) pseudonymBB.createPseudonym(systemParameters, null, deviceUid, USERNAME, scope);

    pseudonym1 =
        (StandardPseudonym) pseudonymBB1.createPseudonym(systemParameters, null, deviceUid, USERNAME, scope);

    // prover side

    ZkModuleProver zkp =
        pseudonymBB.getZkModuleProver(systemParameters, null, identifierOfModule, pseudonym,
            deviceUid, USERNAME, scope);
    ZkProof proof = director.buildProof(USERNAME, Collections.singletonList(zkp), systemParameters);

    System.out.println(JaxbHelperClass.serialize(new ObjectFactory().createZkProof(proof)));

    // verifier side

    ZkModuleVerifier zkv =
        pseudonymBB.getZkModuleVerifier(systemParameters, null, identifierOfModule, scope,
            pseudonym1.getValue().toByteArray());

    boolean ok = !director.verifyProof(proof, Collections.singletonList(zkv), systemParameters);
    assertTrue(ok);

  }
}
