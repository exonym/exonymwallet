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
package com.ibm.zurich.idmx.buildingBlock.signature.uprove;

import com.ibm.zurich.idmx.buildingBlock.factory.BuildingBlockFactory;
import com.ibm.zurich.idmx.buildingBlock.signature.IssuanceTestHelper;
import com.ibm.zurich.idmx.buildingBlock.signature.cl.ClIssuanceWithMockTest;
import com.ibm.zurich.idmx.buildingBlock.structural.constant.ConstantBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.equality.AttributeEqualityBuildingBlock;
import com.ibm.zurich.idmx.dagger.AbcComponent;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.ProofException;
import com.ibm.zurich.idmx.exception.SerializationException;
import com.ibm.zurich.idmx.interfaces.proofEngine.ZkDirector;
import com.ibm.zurich.idmx.interfaces.state.CarryOverStateIssuer;
import com.ibm.zurich.idmx.interfaces.state.CarryOverStateRecipient;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.RandomGeneration;
import com.ibm.zurich.idmx.interfaces.util.group.GroupFactory;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProver;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProverCarryOver;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleVerifier;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleVerifierCarryOver;
import com.ibm.zurich.idmx.jaxb.JaxbHelperClass;
import com.ibm.zurich.idmx.tests.TestInitialisation;
import com.ibm.zurich.idmx.tests.TestUtils;
import eu.abc4trust.xml.*;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;

@Ignore
public class UProveCarryOverTest {
  SystemParameters systemParameters;
  PublicKey publicKey;
  KeyPair keyPair;
  CredentialSpecification credentialSpecification;

  BrandsSignatureBuildingBlock UProveBuildingBlock;
  BigIntFactory bigIntFactory;
  GroupFactory groupFactory;
  RandomGeneration randomGeneration;

  ConstantBuildingBlock constantBB;
  AttributeEqualityBuildingBlock equalBB;
  ZkDirector zkDirector;

  private CarryOverStateRecipient carryOverStateRecipient;
  private CarryOverStateIssuer carryOverStateIssuer;

  private static final String IDENTIFIER_OF_PROVER_MODULE = "test";
  private static final String USERNAME = "user";
  private static final String IDENTIFIER_OF_CARRY_OVER_MODULE = "test:carryOver";
  private static final String IDENTIFIER_OF_ISSUER_MODULE = "test:advancedIssuer";
  public static final int[] ATTRIBUTE_VALUES_NEW1 = new int[] {1337, 0, 4242};
  public static final int[] ATTRIBUTE_VALUES_NEW2 = new int[] {100, 200, 300};

  @Before
  public void setUp() throws SerializationException, ConfigurationException {
    systemParameters = TestUtils.getResource("sp_default.xml", SystemParameters.class, this);
    keyPair = TestUtils.getResource("keyPair_brands.xml", KeyPair.class, this);
    credentialSpecification =
        TestUtils.getResource("../credSpec_simpleID.xml", CredentialSpecification.class, this);
    publicKey = keyPair.getPublicKey();

    AbcComponent abc = TestInitialisation.INJECTOR;
    BuildingBlockFactory bbf = abc.provideBuildingBlockFactory();

    // Objects used for the Brands signature block
    UProveBuildingBlock = bbf.getBuildingBlockByClass(BrandsSignatureBuildingBlock.class);
    bigIntFactory = abc.provideBigIntFactory();
    groupFactory = abc.provideGroupFactory();
    randomGeneration = abc.provideRandomGeneration();

    // Objects required for testing with the proof engine
    equalBB = bbf.getBuildingBlockByClass(AttributeEqualityBuildingBlock.class);
    zkDirector = abc.providesZkDirector();

  }


  @Test
  public void carryOverProof_all() throws SerializationException, ConfigurationException,
      ProofException {
    Map<Integer, Integer> carryOverMap = new HashMap<Integer, Integer>();
    carryOverMap.put(0, 2);
    carryOverMap.put(1, 1);
    carryOverMap.put(2, 0);
    carryOver_proof(carryOverMap, ATTRIBUTE_VALUES_NEW1);
  }

  @Test
  public void carryOverProof_none() throws SerializationException, ConfigurationException,
      ProofException {
    Map<Integer, Integer> carryOverMap = new HashMap<Integer, Integer>();
    carryOver_proof(carryOverMap, ATTRIBUTE_VALUES_NEW1);
  }

  @Test
  public void carryOverProof_one() throws SerializationException, ConfigurationException,
      ProofException {
    Map<Integer, Integer> carryOverMap = new HashMap<Integer, Integer>();
    carryOverMap.put(2, 1);
    carryOver_proof(carryOverMap, ATTRIBUTE_VALUES_NEW1);
  }


  @Test
  public void carryOverProof_all_differentCredential() throws SerializationException,
      ConfigurationException, ProofException {
    Map<Integer, Integer> carryOverMap = new HashMap<Integer, Integer>();
    carryOverMap.put(1, 1);
    carryOverMap.put(2, 0);
    carryOver_proof(carryOverMap, ATTRIBUTE_VALUES_NEW2);
  }



  @Test
  public void carryOverIssuance_one() throws SerializationException, ConfigurationException,
      ProofException {
    Map<Integer, Integer> carryOverMap = new HashMap<Integer, Integer>();
    carryOverMap.put(2, 1);
    advancedIssuance(carryOverMap, ATTRIBUTE_VALUES_NEW1);
  }

  @Test
  public void carryOverIssuance_all() throws SerializationException, ConfigurationException,
      ProofException {
    Map<Integer, Integer> carryOverMap = new HashMap<Integer, Integer>();
    carryOverMap.put(0, 2);
    carryOverMap.put(1, 1);
    carryOverMap.put(2, 0);
    advancedIssuance(carryOverMap, ATTRIBUTE_VALUES_NEW1);
  }

  @Test
  public void carryOverIssuance_all_differentCredential() throws SerializationException,
      ConfigurationException, ProofException {
    Map<Integer, Integer> carryOverMap = new HashMap<Integer, Integer>();
    carryOverMap.put(0, 1);
    carryOverMap.put(2, 0);
    advancedIssuance(carryOverMap, ATTRIBUTE_VALUES_NEW2);
  }

  @Test
  public void carryOverIssuance_none_differentCredential() throws SerializationException,
      ConfigurationException, ProofException {
    Map<Integer, Integer> carryOverMap = new HashMap<Integer, Integer>();
    advancedIssuance(carryOverMap, ATTRIBUTE_VALUES_NEW2);
  }


  private void carryOver_proof(Map<Integer, Integer> carryOverMap, int[] newAttributeValues)
      throws SerializationException, ConfigurationException, ProofException {

    Signature sig = TestUtils.getResource("brandsSignature1.xml", Signature.class, this);
    SignatureToken tok = sig.getSignatureToken().get(0);
    boolean externalDevice = false;
    BigInt credSpecId =
        ClIssuanceWithMockTest.getNumericalCredSpecId(credentialSpecification, systemParameters,
            bigIntFactory);

    // Set of attributes of credential to be presented
    List<BigInt> attributes = new ArrayList<BigInt>();
    for (int i = 0; i < IssuanceTestHelper.ATTRIBUTE_VALUES.length; i++) {
      BigInt attValue = bigIntFactory.valueOf(IssuanceTestHelper.ATTRIBUTE_VALUES[i]);
      attributes.add(attValue);
    }
    int numberOfAttributes = attributes.size();

    // Set of attributes of credential to be issued
    List<BigInt> newCredentialAttributes = new ArrayList<BigInt>();
    for (int i = 0; i < newAttributeValues.length; i++) {
      if (carryOverMap.containsValue(i)) {
        BigInt attValue = bigIntFactory.valueOf(newAttributeValues[i]);
        newCredentialAttributes.add(attValue);
      } else {
        newCredentialAttributes.add(null);
      }
    }

    // Create list of carry over attributes
    List<Boolean> carryAttributeOver = new ArrayList<Boolean>();
    List<Boolean> setByIssuer = new ArrayList<Boolean>();
    for (int i = 0; i < newCredentialAttributes.size(); i++) {
      if (carryOverMap.containsValue(i)) {
        carryAttributeOver.add(true);
        setByIssuer.add(false);
      } else {
        carryAttributeOver.add(false);
        setByIssuer.add(true);
      }
    }


    List<ZkModuleProver> modulesProver = new ArrayList<ZkModuleProver>();
    List<ZkModuleVerifier> modulesVerifier = new ArrayList<ZkModuleVerifier>();

    // Equality proof of attributes in UProve signature and CarryOverCommitment
    for (int i : carryOverMap.keySet()) {
      modulesProver.add(equalBB.getZkModuleProver(IDENTIFIER_OF_PROVER_MODULE + ":" + i,
          IDENTIFIER_OF_CARRY_OVER_MODULE + ":" + carryOverMap.get(i), false));
      modulesVerifier.add(equalBB.getZkModuleVerifier(IDENTIFIER_OF_PROVER_MODULE + ":" + i,
          IDENTIFIER_OF_CARRY_OVER_MODULE + ":" + carryOverMap.get(i), false));
    }

    // Presentation
    ZkModuleProver zkp =
        UProveBuildingBlock.getZkModuleProverPresentation(systemParameters, null, publicKey,
            IDENTIFIER_OF_PROVER_MODULE, tok, attributes, credSpecId, null, USERNAME, null);
    modulesProver.add(zkp);
    // Carry Over Proof
    zkp =
        UProveBuildingBlock.getZkModuleProverCarryOver(systemParameters, null, publicKey,
            IDENTIFIER_OF_CARRY_OVER_MODULE, null, USERNAME, null, credSpecId, carryAttributeOver,
            newCredentialAttributes);
    modulesProver.add(zkp);

    ZkProof proof = zkDirector.buildProof(USERNAME, modulesProver, systemParameters);
    carryOverStateRecipient = ((ZkModuleProverCarryOver) zkp).recoverState();
    assert (carryOverStateRecipient != null);

    // Serialization
    String xmlProof = JaxbHelperClass.serialize((new ObjectFactory()).createZkProof(proof));
    System.out.println(xmlProof);

    // Verification
    ZkModuleVerifier zkv =
        UProveBuildingBlock.getZkModuleVerifierPresentation(systemParameters, null, publicKey,
            IDENTIFIER_OF_PROVER_MODULE, credSpecId, numberOfAttributes, externalDevice);
    modulesVerifier.add(zkv);
    // Carry Over Verification
    zkv =
        UProveBuildingBlock.getZkModuleVerifierCarryOver(systemParameters, null, publicKey,
            IDENTIFIER_OF_CARRY_OVER_MODULE, credSpecId, setByIssuer, false /* hasDevice */);
    modulesVerifier.add(zkv);

    boolean result = zkDirector.verifyProof(proof, modulesVerifier, systemParameters);
    assertTrue(result);

    carryOverStateIssuer = ((ZkModuleVerifierCarryOver) zkv).recoverState();
    assert (carryOverStateIssuer != null);
  }


  private void advancedIssuance(Map<Integer, Integer> carryOverMap, int[] newAttributeValues)
      throws SerializationException, ConfigurationException, ProofException {

    carryOver_proof(carryOverMap, newAttributeValues);

    // Starting issuance with (1) new set of ATTRIBUTES, (2) attributes from CARRY_OVER_STATE
    BigInt credSpecId =
        ClIssuanceWithMockTest.getNumericalCredSpecId(credentialSpecification, systemParameters,
            bigIntFactory);

    // Set of attributes of credential to be issued
    List<BigInt> newCredentialAttributes = new ArrayList<BigInt>();
    for (int i = 0; i < newAttributeValues.length; i++) {
      // only provide the issuer with the attributes that are not carried over
      if (!carryOverMap.containsValue(i)) {
        BigInt attValue = bigIntFactory.valueOf(newAttributeValues[i]);
        newCredentialAttributes.add(attValue);
      } else {
        newCredentialAttributes.add(null);
      }
    }

    List<ZkModuleProver> modulesProver = new ArrayList<ZkModuleProver>();
    List<ZkModuleVerifier> modulesVerifier = new ArrayList<ZkModuleVerifier>();

    ZkModuleProver zkp =
        UProveBuildingBlock.getZkModuleProverIssuance(systemParameters, null, publicKey,
            keyPair.getPrivateKey(), IDENTIFIER_OF_ISSUER_MODULE, credSpecId, false,
            newCredentialAttributes, carryOverStateIssuer);
    modulesProver.add(zkp);

    ZkProof proof = zkDirector.buildProof(USERNAME, modulesProver, systemParameters);

    // Serialise proof
    String xmlProof = JaxbHelperClass.serialize((new ObjectFactory()).createZkProof(proof));
    System.out.println(xmlProof);

    ZkModuleVerifier zkv =
        UProveBuildingBlock.getZkModuleVerifierIssuance(systemParameters, null, publicKey,
            IDENTIFIER_OF_ISSUER_MODULE, credSpecId, false, newAttributeValues.length,
            carryOverStateRecipient);
    modulesVerifier.add(zkv);

    boolean result = zkDirector.verifyProof(proof, modulesVerifier, systemParameters);
    assertTrue(result);

  }
}
