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
package com.ibm.zurich.idmx.proofEngine;

import com.ibm.zurich.idmx.buildingBlock.factory.BuildingBlockFactory;
import com.ibm.zurich.idmx.buildingBlock.signature.cl.ClIssuanceWithMockTest;
import com.ibm.zurich.idmx.buildingBlock.signature.uprove.BrandsSignatureBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.signature.uprove.UProveIssuanceWithMockTest;
import com.ibm.zurich.idmx.dagger.AbcComponent;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.ProofException;
import com.ibm.zurich.idmx.exception.SerializationException;
import com.ibm.zurich.idmx.interfaces.proofEngine.ZkDirector;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProver;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleVerifier;
import com.ibm.zurich.idmx.jaxb.JaxbHelperClass;
import com.ibm.zurich.idmx.tests.TestInitialisation;
import com.ibm.zurich.idmx.tests.TestUtils;
import com.ibm.zurich.idmx.tests.setup.TestSystemParameters;
import eu.abc4trust.xml.*;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertTrue;

public class ProofEngineTest {

  ZkDirector director;
  SystemParameters systemParameters;
  CredentialSpecification credentialSpecification;
  private static final String USERNAME = "user";

  @Before
  public void setUp() throws SerializationException {
    AbcComponent abc = TestInitialisation.INJECTOR;
    director = abc.providesZkDirector();
    systemParameters =
        TestUtils.getResource("../sp_default.xml", SystemParameters.class,
            UProveIssuanceWithMockTest.class);
    credentialSpecification =
        TestUtils.getResource("../credSpec_simpleID.xml", CredentialSpecification.class,
            UProveIssuanceWithMockTest.class);
  }

  @Test
  public void testEmpty() throws ConfigurationException, ProofException {
    ZkProof proof = director.buildProof(USERNAME, Collections.<ZkModuleProver>emptyList(), systemParameters);
    boolean result =
        director.verifyProof(proof, Collections.<ZkModuleVerifier>emptyList(), systemParameters);
    assertTrue(result);
  }

  private static final String UPROVE_PROOF_FILENAME = TestSystemParameters.BASE_LOCATION.resolve(
      "proofs/proof_uprove.xml").toString();

  @Test
  public void testSingleModule_UProve() throws SerializationException, ConfigurationException,
      IOException, ProofException {
    final String IDENTIFIER_OF_MODULE = "test";
    PublicKey publicKey =
        TestUtils
            .getResource("keyPair_brands.xml", KeyPair.class, UProveIssuanceWithMockTest.class)
            .getPublicKey();

    AbcComponent abc = TestInitialisation.INJECTOR;
    BuildingBlockFactory bbf = abc.provideBuildingBlockFactory();

    BrandsSignatureBuildingBlock uproveBB = bbf.getBuildingBlockByClass(BrandsSignatureBuildingBlock.class);
    BigIntFactory bigIntFactory = abc.provideBigIntFactory();

    Signature sig =
        TestUtils.getResource("brandsSignature1.xml", Signature.class,
            UProveIssuanceWithMockTest.class);
    SignatureToken tok = sig.getSignatureToken().get(0);
    boolean externalDevice = false;

    BigInt credSpecId =
        ClIssuanceWithMockTest.getNumericalCredSpecId(credentialSpecification, systemParameters,
            bigIntFactory);

    List<BigInt> attributes = new ArrayList<BigInt>();
    for (int attValue : UProveIssuanceWithMockTest.ATTRIBUTE_VALUES) {
      attributes.add(bigIntFactory.valueOf(attValue));
    }
    int numberOfAttributes = attributes.size();

    ZkModuleProver zkp =
        uproveBB.getZkModuleProverPresentation(systemParameters, null, publicKey,
            IDENTIFIER_OF_MODULE, tok, attributes, credSpecId, null, USERNAME, null);

    ZkModuleVerifier zkv =
        uproveBB.getZkModuleVerifierPresentation(systemParameters, null, publicKey,
            IDENTIFIER_OF_MODULE, credSpecId, numberOfAttributes, externalDevice);

    ZkProof proof = director.buildProof(USERNAME, Collections.singletonList(zkp), systemParameters);

    // Serialization
    String xmlProof = JaxbHelperClass.serialize((new ObjectFactory()).createZkProof(proof));
    TestUtils.saveToFile(xmlProof, UPROVE_PROOF_FILENAME);

    boolean result = director.verifyProof(proof, Collections.singletonList(zkv), systemParameters);
    assertTrue(result);
  }
  
}
