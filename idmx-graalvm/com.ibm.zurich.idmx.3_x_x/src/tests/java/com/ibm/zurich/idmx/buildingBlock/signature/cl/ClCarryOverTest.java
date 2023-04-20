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
package com.ibm.zurich.idmx.buildingBlock.signature.cl;

import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ibm.zurich.idmx.buildingBlock.factory.BuildingBlockFactory;
import com.ibm.zurich.idmx.dagger.AbcComponent;
import com.ibm.zurich.idmx.dagger.DaggerAbcComponent;
import com.ibm.zurich.idmx.tests.TestInitialisation;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.ibm.zurich.idmix.abc4trust.facades.IssuerParametersFacade;
import com.ibm.zurich.idmx.dagger.CryptoTestModule;
import com.ibm.zurich.idmx.buildingBlock.structural.constant.ConstantBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.equality.AttributeEqualityBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.systemParameters.EcryptSystemParametersWrapper;
import com.ibm.zurich.idmx.device.ExternalSecretsManagerImpl;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.ProofException;
import com.ibm.zurich.idmx.exception.SerializationException;
import com.ibm.zurich.idmx.interfaces.device.ExternalSecretsManager;
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
import com.ibm.zurich.idmx.tests.TestUtils;

import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.KeyPair;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PublicKey;
import eu.abc4trust.xml.Signature;
import eu.abc4trust.xml.SignatureToken;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.ZkProof;

public class ClCarryOverTest {
  SystemParameters systemParameters;
  PublicKey publicKey;
  KeyPair keyPair;
  CredentialSpecification credentialSpecification;

  ClSignatureBuildingBlock clSignatureBuildingBlock;
  ClPublicKeyWrapper clpkw;
  BigIntFactory bigIntFactory;
  GroupFactory groupFactory;
  RandomGeneration randomGeneration;
  ExternalSecretsManagerImpl esManager;

  ConstantBuildingBlock constantBB;
  AttributeEqualityBuildingBlock equalBB;
  ZkDirector zkDirector;
  EcryptSystemParametersWrapper spWrapper;
  BigInt MODULUS;
  KeyManager km;
  private static final String USERNAME = "user";

  private CarryOverStateRecipient carryOverStateRecipient;
  private CarryOverStateIssuer carryOverStateIssuer;

  private static final String IDENTIFIER_OF_PROVER_MODULE = "test";
  private static final String IDENTIFIER_OF_CARRY_OVER_MODULE = "test:carryOver";
  private static final String IDENTIFIER_OF_ISSUER_MODULE = "test:advancedIssuer";
  // att:0 -> att:3 of ClIssuanceTest.ATTRIBUTE_VALUES
  // att:2 -> att:4 of ClIssuanceTest.ATTRIBUTE_VALUES
  // att:3 -> att:1 of ClIssuanceTest.ATTRIBUTE_VALUES
  // att:4 -> att:2 of ClIssuanceTest.ATTRIBUTE_VALUES
  // att:5 -> att:0 of ClIssuanceTest.ATTRIBUTE_VALUES
  public static final int[] ATTRIBUTE_VALUES_NEW1 = new int[] {23847239, 45687, 0, 1, 4242, 1337};
  // att:0 -> att:2 of ClIssuanceTest.ATTRIBUTE_VALUES
  // att:1 -> att:4 of ClIssuanceTest.ATTRIBUTE_VALUES
  public static final int[] ATTRIBUTE_VALUES_NEW2 = new int[] {4242, 0};
  private URI issuerUri;
  private URI deviceUri;
  private URI signatureUri;

  @Before
  public void setUp() throws SerializationException, ConfigurationException {
    AbcComponent i = TestInitialisation.INJECTOR;
    systemParameters = TestUtils.getResource("../sp_default.xml", SystemParameters.class, this);
    spWrapper = new EcryptSystemParametersWrapper(systemParameters);
    keyPair = TestUtils.getResource("keyPair_cl.xml", KeyPair.class, this);
    credentialSpecification =
            TestUtils.getResource("../credSpec_simpleID.xml", CredentialSpecification.class, this);

    BuildingBlockFactory bbf = i.provideBuildingBlockFactory();
    esManager = (ExternalSecretsManagerImpl) bbf.getExternalSecretsManager();
    clSignatureBuildingBlock = bbf.getBuildingBlockByClass(ClSignatureBuildingBlock.class);
    bigIntFactory = i.provideBigIntFactory();
    groupFactory = i.provideGroupFactory();
    randomGeneration = i.provideRandomGeneration();
    equalBB = bbf.getBuildingBlockByClass(AttributeEqualityBuildingBlock.class);
    zkDirector = i.providesZkDirector();
    km = i.providesKeyManager();

//    Injector injector = Guice.createInjector(new CryptoTestModule());
//
//
//    esManager = (ExternalSecretsManagerImpl) injector.getInstance(ExternalSecretsManager.class);
//
//    // Objects used for the CL signature block
//    clSignatureBuildingBlock = injector.getInstance(ClSignatureBuildingBlock.class);
//    bigIntFactory = injector.getInstance(BigIntFactory.class);
//    groupFactory = injector.getInstance(GroupFactory.class);
//    randomGeneration = injector.getInstance(RandomGeneration.class);
//
//    // Objects required for testing with the proof engine
//    equalBB = injector.getInstance(AttributeEqualityBuildingBlock.class);
//    zkDirector = injector.getInstance(ZkDirector.class);
//    km = injector.getInstance(KeyManager.class);
  }

  @Test @Ignore
  public void dummy_test() throws SerializationException, ConfigurationException, ProofException {
    Map<Integer, Integer> carryOverMap = new HashMap<Integer, Integer>();
    carryOverMap.put(0, 0);
    carryOverMap.put(1, 1);
    carryOverMap.put(2, 2);
    carryOver_proof(carryOverMap, ClIssuanceWithMockTest.ATTRIBUTE_VALUES, true);
  }

  public void carryOverProof_all(boolean b) throws SerializationException, ConfigurationException,
      ProofException {
    Map<Integer, Integer> carryOverMap = new HashMap<Integer, Integer>();
    carryOverMap.put(0, 5);
    carryOverMap.put(1, 3);
    carryOverMap.put(2, 4);
    carryOverMap.put(3, 0);
    carryOverMap.put(4, 2);
    carryOver_proof(carryOverMap, ATTRIBUTE_VALUES_NEW1, b);
  }

  @Test
  public void carryOverProof_all_noCard() throws SerializationException, ConfigurationException,
      ProofException {
    carryOverProof_all(false);
  }

  @Test @Ignore
  public void carryOverProof_all_withCard() throws SerializationException, ConfigurationException,
      ProofException {
    carryOverProof_all(true);
  }

  public void carryOverProof_none(boolean b) throws SerializationException, ConfigurationException,
      ProofException {
    Map<Integer, Integer> carryOverMap = new HashMap<Integer, Integer>();
    carryOver_proof(carryOverMap, ATTRIBUTE_VALUES_NEW1, b);
  }

  @Test
  public void carryOverProof_none_noCard() throws SerializationException, ConfigurationException,
      ProofException {
    carryOverProof_none(false);
  }

  @Test @Ignore
  public void carryOverProof_none_withCard() throws SerializationException, ConfigurationException,
      ProofException {
    carryOverProof_none(true);
  }

  public void carryOverProof_one(boolean b) throws SerializationException, ConfigurationException,
      ProofException {
    Map<Integer, Integer> carryOverMap = new HashMap<Integer, Integer>();
    carryOverMap.put(2, 4);
    carryOver_proof(carryOverMap, ATTRIBUTE_VALUES_NEW1, b);
  }

  @Test
  public void carryOverProof_one_noCard() throws SerializationException, ConfigurationException,
      ProofException {
    carryOverProof_one(false);
  }

  @Test @Ignore
  public void carryOverProof_one_withCard() throws SerializationException, ConfigurationException,
      ProofException {
    carryOverProof_one(true);
  }

  public void carryOverProof_all_differentCredential(boolean b) throws SerializationException,
      ConfigurationException, ProofException {
    Map<Integer, Integer> carryOverMap = new HashMap<Integer, Integer>();
    carryOverMap.put(4, 1);
    carryOverMap.put(2, 0);
    carryOver_proof(carryOverMap, ATTRIBUTE_VALUES_NEW2, b);
  }

  @Test
  public void carryOverProof_all_differentCredential_noCard() throws SerializationException,
      ConfigurationException, ProofException {
    carryOverProof_all_differentCredential(false);
  }

  @Test @Ignore
  public void carryOverProof_all_differentCredential_withCard() throws SerializationException,
      ConfigurationException, ProofException {
    carryOverProof_all_differentCredential(true);
  }


  public void carryOverIssuance_one(boolean b) throws SerializationException,
      ConfigurationException, ProofException {
    Map<Integer, Integer> carryOverMap = new HashMap<Integer, Integer>();
    carryOverMap.put(2, 4);
    advancedIssuance(carryOverMap, ATTRIBUTE_VALUES_NEW1, b);
  }

  @Test
  public void carryOverIssuance_one_noCard() throws SerializationException, ConfigurationException,
      ProofException {
    carryOverIssuance_one(false);
  }

  @Test @Ignore
  public void carryOverIssuance_one_withCard() throws SerializationException,
      ConfigurationException, ProofException {
    carryOverIssuance_one(true);
  }

  public void carryOverIssuance_all(boolean b) throws SerializationException,
      ConfigurationException, ProofException {
    Map<Integer, Integer> carryOverMap = new HashMap<Integer, Integer>();
    carryOverMap.put(0, 5);
    carryOverMap.put(1, 3);
    carryOverMap.put(2, 4);
    carryOverMap.put(3, 0);
    carryOverMap.put(4, 2);
    advancedIssuance(carryOverMap, ATTRIBUTE_VALUES_NEW1, b);
  }

  @Test
  public void carryOverIssuance_all_noCard() throws SerializationException, ConfigurationException,
      ProofException {
    carryOverIssuance_all(false);
  }

  @Test @Ignore
  public void carryOverIssuance_all_withCard() throws SerializationException,
      ConfigurationException, ProofException {
    carryOverIssuance_all(true);
  }

  public void carryOverIssuance_all_differentCredential(boolean b) throws SerializationException,
      ConfigurationException, ProofException {
    Map<Integer, Integer> carryOverMap = new HashMap<Integer, Integer>();
    carryOverMap.put(4, 1);
    carryOverMap.put(2, 0);
    advancedIssuance(carryOverMap, ATTRIBUTE_VALUES_NEW2, b);
  }

  @Test
  public void carryOverIssuance_all_differentCredential_noCard() throws SerializationException,
      ConfigurationException, ProofException {
    carryOverIssuance_all_differentCredential(false);
  }

  @Test @Ignore
  public void carryOverIssuance_all_differentCredential_withCard() throws SerializationException,
      ConfigurationException, ProofException {
    carryOverIssuance_all_differentCredential(true);
  }

  public void carryOverIssuance_none_differentCredential(boolean b) throws SerializationException,
      ConfigurationException, ProofException {
    Map<Integer, Integer> carryOverMap = new HashMap<Integer, Integer>();
    advancedIssuance(carryOverMap, ATTRIBUTE_VALUES_NEW2, b);
  }

  @Test
  public void carryOverIssuance_none_differentCredential_noCard() throws SerializationException,
      ConfigurationException, ProofException {
    carryOverIssuance_none_differentCredential(false);
  }

  @Test @Ignore
  public void carryOverIssuance_none_differentCredential_withCard() throws SerializationException,
      ConfigurationException, ProofException {
    carryOverIssuance_none_differentCredential(true);
  }


  private void carryOver_proof(Map<Integer, Integer> carryOverMap, int[] newAttributeValues,
      boolean withExternalDevice) throws SerializationException, ConfigurationException,
      ProofException {
    String keyPairFile = withExternalDevice ? "keyPair_cl_smartCard.xml" : "keyPair_cl.xml";
    publicKey = TestUtils.getResource(keyPairFile, KeyPair.class, this).getPublicKey();
    clpkw = new ClPublicKeyWrapper(publicKey);
    MODULUS = clpkw.getModulus();

    Signature sig = TestUtils.getResource("clSignature1.xml", Signature.class, this);
    SignatureToken tok = sig.getSignatureToken().get(0);
    BigInt credSpecId =
        ClIssuanceWithMockTest.getNumericalCredSpecId(credentialSpecification, systemParameters,
            bigIntFactory);

    // Set of attributes of credential to be presented
    List<BigInt> attributes = new ArrayList<BigInt>();
    for (int i = 0; i < ClIssuanceWithMockTest.ATTRIBUTE_VALUES.length; i++) {
      BigInt attValue = bigIntFactory.valueOf(ClIssuanceWithMockTest.ATTRIBUTE_VALUES[i]);
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
    List<Boolean> issuerChosenAttribute = new ArrayList<Boolean>();
    for (int i = 0; i < newCredentialAttributes.size(); i++) {
      if (carryOverMap.containsValue(i)) {
        carryAttributeOver.add(true);
        issuerChosenAttribute.add(false);
      } else {
        carryAttributeOver.add(false);
        issuerChosenAttribute.add(true);
      }
    }

    List<ZkModuleProver> modulesProver = new ArrayList<ZkModuleProver>();
    List<ZkModuleVerifier> modulesVerifier = new ArrayList<ZkModuleVerifier>();

    // Equaliy proof of attributes in CL signature and CarryOverCommitment
    for (int i : carryOverMap.keySet()) {
      modulesProver.add(equalBB.getZkModuleProver(IDENTIFIER_OF_PROVER_MODULE + ":" + i,
          IDENTIFIER_OF_CARRY_OVER_MODULE + ":" + carryOverMap.get(i), false));
      modulesVerifier.add(equalBB.getZkModuleVerifier(IDENTIFIER_OF_PROVER_MODULE + ":" + i,
          IDENTIFIER_OF_CARRY_OVER_MODULE + ":" + carryOverMap.get(i), false));
    }

    this.issuerUri = null;
    this.deviceUri = null;
    this.signatureUri = null;
    if (withExternalDevice) {
      try {
        deviceUri = URI.create("TestDevice");
        issuerUri = URI.create("TestIssuer");
        signatureUri = URI.create("TestCredential");

        km.storeSystemParameters(systemParameters);
        km.storeIssuerParameters(issuerUri,
            IssuerParametersFacade.initIssuerParameters(clpkw.getPublicKey(), systemParameters)
                .getIssuerParameters());
        esManager.allocateCredential(USERNAME, deviceUri, signatureUri, issuerUri, true);
      } catch (KeyManagerException e) {
        throw new RuntimeException(e);
      }
    }

    // Presentation
    ZkModuleProver zkp =
        clSignatureBuildingBlock.getZkModuleProverPresentation(systemParameters, null, publicKey,
            IDENTIFIER_OF_PROVER_MODULE, tok, attributes, credSpecId, deviceUri, USERNAME, signatureUri);
    modulesProver.add(zkp);
    // Carry Over Proof
    zkp =
        clSignatureBuildingBlock.getZkModuleProverCarryOver(systemParameters, null, publicKey,
            IDENTIFIER_OF_CARRY_OVER_MODULE, deviceUri, USERNAME, signatureUri, credSpecId,
            carryAttributeOver, newCredentialAttributes);
    modulesProver.add(zkp);

    ZkProof proof = zkDirector.buildProof(USERNAME, modulesProver, systemParameters);
    carryOverStateRecipient = ((ZkModuleProverCarryOver) zkp).recoverState();
    assert (carryOverStateRecipient != null);

    // Serialization
    String xmlProof = JaxbHelperClass.serialize((new ObjectFactory()).createZkProof(proof));
    System.out.println(xmlProof);

    // Verification
    ZkModuleVerifier zkv =
        clSignatureBuildingBlock.getZkModuleVerifierPresentation(systemParameters, null, publicKey,
            IDENTIFIER_OF_PROVER_MODULE, credSpecId, numberOfAttributes, withExternalDevice);
    modulesVerifier.add(zkv);
    // Carry Over Verification
    zkv =
        clSignatureBuildingBlock.getZkModuleVerifierCarryOver(systemParameters, null, publicKey,
            IDENTIFIER_OF_CARRY_OVER_MODULE, credSpecId, issuerChosenAttribute, withExternalDevice);
    modulesVerifier.add(zkv);

    boolean result = zkDirector.verifyProof(proof, modulesVerifier, systemParameters);
    assertTrue(result);

    carryOverStateIssuer = ((ZkModuleVerifierCarryOver) zkv).recoverState();
    assert (carryOverStateIssuer != null);
  }


  private void advancedIssuance(Map<Integer, Integer> carryOverMap, int[] newAttributeValues,
      boolean withExternalDevice) throws SerializationException, ConfigurationException,
      ProofException {

    carryOver_proof(carryOverMap, newAttributeValues, withExternalDevice);

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
        clSignatureBuildingBlock.getZkModuleProverIssuance(systemParameters, null, publicKey,
            keyPair.getPrivateKey(), IDENTIFIER_OF_ISSUER_MODULE, credSpecId, withExternalDevice,
            newCredentialAttributes, carryOverStateIssuer);
    modulesProver.add(zkp);

    ZkProof proof = zkDirector.buildProof(USERNAME, modulesProver, systemParameters);

    // Serialise proof
    String xmlProof = JaxbHelperClass.serialize((new ObjectFactory()).createZkProof(proof));
    System.out.println(xmlProof);
    ZkModuleVerifier zkv =
        clSignatureBuildingBlock.getZkModuleVerifierIssuance(systemParameters, null, publicKey,
            IDENTIFIER_OF_ISSUER_MODULE, credSpecId, withExternalDevice, newAttributeValues.length,
            carryOverStateRecipient);
    modulesVerifier.add(zkv);

    boolean result = zkDirector.verifyProof(proof, modulesVerifier, systemParameters);
    assertTrue(result);

  }
}
