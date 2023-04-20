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
package com.ibm.zurich.idmx.buildingBlock.structural.pseudonym.scopeExclusive;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.util.Collections;

import com.ibm.zurich.idmx.buildingBlock.factory.BuildingBlockFactory;
import com.ibm.zurich.idmx.buildingBlock.pseudonym.scopeExclusive.ScopeExclusivePseudonymBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.pseudonym.standard.StandardPseudonymBuildingBlock;
import com.ibm.zurich.idmx.dagger.AbcComponent;
import com.ibm.zurich.idmx.tests.TestInitialisation;
import org.junit.Before;
import org.junit.Test;

import com.ibm.zurich.idmx.buildingBlock.pseudonym.PseudonymBuildingBlock;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.ProofException;
import com.ibm.zurich.idmx.exception.SerializationException;
import com.ibm.zurich.idmx.interfaces.proofEngine.ZkDirector;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProver;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleVerifier;
import com.ibm.zurich.idmx.tests.TestUtils;

import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.xml.ScopeExclusivePseudonym;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.ZkProof;

public class ScopeExclusivePseudonymTest {
  private SystemParameters systemParameters;
  private PseudonymBuildingBlock pseudonymBB;
  private ZkDirector director;
  private KeyManager keyManager;

  public static final URI DEVICE_URI = URI.create("theSmartcard");
  private static final String USERNAME = "user";

  @Before
  public void setUp() throws SerializationException, ConfigurationException {
    systemParameters = TestUtils.getResource("sp_default.xml", SystemParameters.class, this);

    AbcComponent inj = TestInitialisation.INJECTOR;
    BuildingBlockFactory bbf = inj.provideBuildingBlockFactory();
    pseudonymBB = bbf.getBuildingBlockByClass(ScopeExclusivePseudonymBuildingBlock.class);
    director = inj.providesZkDirector();
    keyManager = inj.providesKeyManager();


//    Injector injector = Guice.createInjector(new CryptoTestModule());
//    pseudonymBB = injector.getInstance(ScopeExclusivePseudonymBuildingBlock.class);
//    keyManager = injector.getInstance(KeyManager.class);
//    director = injector.getInstance(ZkDirector.class);
  }

  @Test
  public void scopeExclusivePseudonymTest() throws SerializationException, ConfigurationException,
      IOException, ProofException, KeyManagerException {
    String identifierOfModule = "test";

    URI deviceUid = URI.create("theSmartcard");
    keyManager.storeSystemParameters(systemParameters);
    URI scope = URI.create("http://zurich.ibm.com/scope1");

    ScopeExclusivePseudonym pseudonym;

    // create pseudonym

    pseudonym =
        (ScopeExclusivePseudonym) pseudonymBB.createPseudonym(systemParameters, null, deviceUid,
          USERNAME, scope);

    // prover side

    ZkModuleProver zkp =
        pseudonymBB.getZkModuleProver(systemParameters, null, identifierOfModule, pseudonym,
            deviceUid, USERNAME, scope);
    ZkProof proof = director.buildProof(USERNAME, Collections.singletonList(zkp), systemParameters);

    // verifier side

    ZkModuleVerifier zkv =
        pseudonymBB.getZkModuleVerifier(systemParameters, null, identifierOfModule, scope,
            pseudonym.getValue().toByteArray());

    boolean ok = director.verifyProof(proof, Collections.singletonList(zkv), systemParameters);
    assertTrue(ok);

  }

  @Test
  public void negativeScopeExclusivePseudonymTest() throws SerializationException,
      ConfigurationException, IOException, ProofException, KeyManagerException {
    String identifierOfModule = "test";

    URI deviceUid = URI.create("theSmartcard");
    keyManager.storeSystemParameters(systemParameters);
    URI scope = URI.create("http://zurich.ibm.com/scope1");
    URI scope1 = URI.create("http://zurich.ibm.com/scope2");

    ScopeExclusivePseudonym pseudonym;

    // create pseudonym

    pseudonym =
        (ScopeExclusivePseudonym) pseudonymBB.createPseudonym(systemParameters, null, deviceUid,
          USERNAME, scope);

    // prover side

    ZkModuleProver zkp =
        pseudonymBB.getZkModuleProver(systemParameters, null, identifierOfModule, pseudonym,
            deviceUid, USERNAME, scope);
    ZkProof proof = director.buildProof(USERNAME, Collections.singletonList(zkp), systemParameters);

    // verifier side

    // verify with different scope -- should fail as it uses different base for pseudonym

    ZkModuleVerifier zkv =
        pseudonymBB.getZkModuleVerifier(systemParameters, null, identifierOfModule, scope1,
            pseudonym.getValue().toByteArray());

    boolean ok = !director.verifyProof(proof, Collections.singletonList(zkv), systemParameters);
    assertTrue(ok);

  }
}
