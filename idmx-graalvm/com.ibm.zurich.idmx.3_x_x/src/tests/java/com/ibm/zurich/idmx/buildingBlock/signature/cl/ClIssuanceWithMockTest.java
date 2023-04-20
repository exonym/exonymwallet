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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.getCurrentArguments;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
//import java.util.Random;


import com.ibm.zurich.idmx.buildingBlock.factory.BuildingBlockFactory;
import com.ibm.zurich.idmx.buildingBlock.structural.equality.AttributeEqualityBuildingBlock;
import com.ibm.zurich.idmx.dagger.AbcComponent;
import com.ibm.zurich.idmx.dagger.DaggerAbcComponent;
import com.ibm.zurich.idmx.device.ExternalSecretsManagerImpl;
import com.ibm.zurich.idmx.tests.TestInitialisation;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Before;
import org.junit.Test;

import com.ibm.zurich.idmx.dagger.CryptoTestModule;
import com.ibm.zurich.idmx.buildingBlock.systemParameters.EcryptSystemParametersWrapper;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.ProofException;
import com.ibm.zurich.idmx.exception.SerializationException;
import com.ibm.zurich.idmx.interfaces.proofEngine.ZkDirector;
import com.ibm.zurich.idmx.interfaces.signature.ListOfSignaturesAndAttributes;
import com.ibm.zurich.idmx.interfaces.state.CarryOverStateIssuer;
import com.ibm.zurich.idmx.interfaces.state.CarryOverStateRecipient;
import com.ibm.zurich.idmx.interfaces.state.IssuanceStateRecipient;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.RandomGeneration;
import com.ibm.zurich.idmx.interfaces.util.group.Group;
import com.ibm.zurich.idmx.interfaces.util.group.GroupElement;
import com.ibm.zurich.idmx.interfaces.util.group.GroupFactory;
import com.ibm.zurich.idmx.interfaces.util.group.HiddenOrderGroup;
import com.ibm.zurich.idmx.interfaces.util.group.HiddenOrderGroupElement;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProver;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProverIssuance;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleVerifier;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleVerifierIssuance;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ResidueClass;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateAll;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkVerifyStateAll;
import com.ibm.zurich.idmx.jaxb.JaxbHelperClass;
import com.ibm.zurich.idmx.jaxb.wrapper.CredentialSpecificationWrapper;
import com.ibm.zurich.idmx.tests.TestUtils;
import com.ibm.zurich.idmx.tests.setup.TestSystemParameters;

import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.KeyPair;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.ZkProof;

public class ClIssuanceWithMockTest {
  SystemParameters systemParameters;
  KeyPair keyPair;
  CredentialSpecification credentialSpecification;

  ClSignatureBuildingBlock clBuildingBlock;

  BigIntFactory bigIntFactory;
  GroupFactory groupFactory;
  RandomGeneration randomGeneration;

  ZkDirector zkDirector;

  private CarryOverStateIssuer carryOverStateIssuer;
  private CarryOverStateRecipient carryOverStateRecipient;
  private static final String USERNAME = "user";

  public static final int[] ATTRIBUTE_VALUES = new int[] {1337, 1, 4242};

  @Before
  public void setUp() throws SerializationException, ConfigurationException {
    systemParameters = TestUtils.getResource("../sp_default.xml", SystemParameters.class, this);
    keyPair = TestUtils.getResource("keyPair_cl.xml", KeyPair.class, this);
    credentialSpecification =
            TestUtils.getResource("../credSpec_simpleID.xml", CredentialSpecification.class, this);

    AbcComponent i = TestInitialisation.INJECTOR;
    BuildingBlockFactory bbf = i.provideBuildingBlockFactory();
    bigIntFactory = i.provideBigIntFactory();
    groupFactory = i.provideGroupFactory();
    randomGeneration = i.provideRandomGeneration();
    zkDirector = i.providesZkDirector();
    clBuildingBlock = bbf.getBuildingBlockByClass(ClSignatureBuildingBlock.class);


//    systemParameters = TestUtils.getResource("../sp_default.xml", SystemParameters.class, this);
//    keyPair = TestUtils.getResource("keyPair_cl.xml", KeyPair.class, this);
//    credentialSpecification =
//        TestUtils.getResource("../credSpec_simpleID.xml", CredentialSpecification.class, this);
//
//    Injector injector = Guice.createInjector(new CryptoTestModule());
//    clBuildingBlock = injector.getInstance(ClSignatureBuildingBlock.class);
//    bigIntFactory = injector.getInstance(BigIntFactory.class);
//    groupFactory = injector.getInstance(GroupFactory.class);
//    randomGeneration = injector.getInstance(RandomGeneration.class);
//
//    // Used for test with the proof engine
//    zkDirector = injector.getInstance(ZkDirector.class);
  }

  @Test
  public void checkPrivateKey() throws ConfigurationException {
    ClKeyPairWrapper keyPairWrapper = new ClKeyPairWrapper(keyPair);

    BigInt modulus = keyPairWrapper.getCLPublicKeyWrapper().getModulus();

    BigInt p = keyPairWrapper.getCLSecretKeyWrapper().getSafePrimeP();
    BigInt q = keyPairWrapper.getCLSecretKeyWrapper().getSafePrimeQ();
    BigInt pPrime = keyPairWrapper.getCLSecretKeyWrapper().getSophieGermainPrimeP();
    BigInt qPrime = keyPairWrapper.getCLSecretKeyWrapper().getSophieGermainPrimeQ();
   
    assertEquals(p, pPrime.multiply(bigIntFactory.two()).add(bigIntFactory.one()));
    assertEquals(q, qPrime.multiply(bigIntFactory.two()).add(bigIntFactory.one()));
    assertTrue(p.isProbablePrime(100));
    assertTrue(q.isProbablePrime(100));
    assertTrue(pPrime.isProbablePrime(100));
    assertTrue(qPrime.isProbablePrime(100));
    assertEquals(modulus, p.multiply(q));
  }

  private static final String CL_SIGNATURE_FILENAME = TestSystemParameters.BASE_LOCATION.resolve(
      "issuance/clSignature1.xml").toString();

  private static final String identifierOfModule = "test.cl";

  @Test
  public void issuanceTest_allAttributesRevealed() throws SerializationException,
      ConfigurationException, IOException, ProofException {

    BigInt credSpecId =
        getNumericalCredSpecId(credentialSpecification, systemParameters, bigIntFactory);

    boolean externalDevice = false;

    List<BigInt> attributes = new ArrayList<BigInt>();
    for (int attValue : ATTRIBUTE_VALUES) {
      attributes.add(bigIntFactory.valueOf(attValue));
    }

    ListOfSignaturesAndAttributes sig = issue(externalDevice, credSpecId, attributes);

    assertEquals(sig.attributes, attributes);

    // Serialization
    String signature =
        JaxbHelperClass.serialize((new ObjectFactory()).createSignature(sig.signature));
    TestUtils.saveToFile(signature, CL_SIGNATURE_FILENAME);
  }


  @Test
  public void issuanceTest_allAttributesUnrevealed() throws SerializationException,
      ConfigurationException, IOException, ProofException {

    BigInt credSpecId =
        getNumericalCredSpecId(credentialSpecification, systemParameters, bigIntFactory);

    boolean externalDevice = false;

    // Setup attribute lists
    List<BigInt> allAttributes = new ArrayList<BigInt>();
    List<BigInt> revealedAttributes = new ArrayList<BigInt>();

    // Setup the carry over states of issuer and recipient with the attribute values
    setupCarryOverStates(1, allAttributes, revealedAttributes);

    // Run the issuance protocol
    ListOfSignaturesAndAttributes sig = issue(externalDevice, credSpecId, revealedAttributes);
    assertEquals(sig.attributes, allAttributes);

    // Serialization
    String signature =
        JaxbHelperClass.serialize((new ObjectFactory()).createSignature(sig.signature));
    TestUtils.saveToFile(signature, CL_SIGNATURE_FILENAME);
  }


  @Test
  public void issuanceTest_someAttributesUnrevealed() throws SerializationException,
      ConfigurationException, IOException, ProofException {

    BigInt credSpecId =
        getNumericalCredSpecId(credentialSpecification, systemParameters, bigIntFactory);

    boolean externalDevice = false;

    // Setup attribute lists
    List<BigInt> allAttributes = new ArrayList<BigInt>();
    List<BigInt> revealedAttributes = new ArrayList<BigInt>();

    // Setup the carry over states of issuer and recipient with the attribute values
    setupCarryOverStates(0.5, allAttributes, revealedAttributes);

    // Run the issuance protocol
    ListOfSignaturesAndAttributes sig = issue(externalDevice, credSpecId, revealedAttributes);
    assertEquals(sig.attributes, allAttributes);

    // Serialization
    String signature =
        JaxbHelperClass.serialize((new ObjectFactory()).createSignature(sig.signature));
    TestUtils.saveToFile(signature, CL_SIGNATURE_FILENAME);
  }

  @Test
  public void issuanceTest_proofEngine_allAttributesRevealed() throws SerializationException,
      ConfigurationException, IOException, ProofException {

    BigInt credSpecId =
        getNumericalCredSpecId(credentialSpecification, systemParameters, bigIntFactory);

    boolean externalDevice = false;

    List<BigInt> attributes = new ArrayList<BigInt>();
    for (int attValue : ATTRIBUTE_VALUES) {
      attributes.add(bigIntFactory.valueOf(attValue));
    }

    ListOfSignaturesAndAttributes sig =
        issueUsingProofEngine(externalDevice, credSpecId, attributes);
    assertEquals(sig.attributes, attributes);
  }


  @Test
  public void issuanceTest_proofEngine_allAttributesUnrevealed() throws SerializationException,
      ConfigurationException, IOException, ProofException {

    BigInt credSpecId =
        getNumericalCredSpecId(credentialSpecification, systemParameters, bigIntFactory);

    boolean externalDevice = false;

    // Setup attribute lists
    List<BigInt> allAttributes = new ArrayList<BigInt>();
    List<BigInt> revealedAttributes = new ArrayList<BigInt>();

    // Setup the carry over states of issuer and recipient with the attribute values
    setupCarryOverStates(1, allAttributes, revealedAttributes);

    // Run the issuance protocol
    ListOfSignaturesAndAttributes sig =
        issueUsingProofEngine(externalDevice, credSpecId, revealedAttributes);
    assertEquals(sig.attributes, allAttributes);

  }


  @Test
  public void issuanceTest_proofEngine_someAttributesUnrevealed() throws SerializationException,
      ConfigurationException, IOException, ProofException {

    BigInt credSpecId =
        getNumericalCredSpecId(credentialSpecification, systemParameters, bigIntFactory);

    boolean externalDevice = false;

    // Setup attribute lists
    List<BigInt> allAttributes = new ArrayList<BigInt>();
    List<BigInt> revealedAttributes = new ArrayList<BigInt>();

    // Setup the carry over states of issuer and recipient with the attribute values
    setupCarryOverStates(0.5, allAttributes, revealedAttributes);

    // Run the issuance protocol
    ListOfSignaturesAndAttributes sig =
        issueUsingProofEngine(externalDevice, credSpecId, revealedAttributes);
    assertEquals(sig.attributes, allAttributes);
  }


  public static BigInt getNumericalCredSpecId(CredentialSpecification credentialSpecification,
      SystemParameters systemParameters, BigIntFactory bigIntFactory) throws ConfigurationException {
    EcryptSystemParametersWrapper spWrapper = new EcryptSystemParametersWrapper(systemParameters);
    CredentialSpecificationWrapper credSpecWrapper =
        new CredentialSpecificationWrapper(credentialSpecification, bigIntFactory);
    return credSpecWrapper.getCredSpecId(spWrapper.getHashFunction());
  }


  /**
   * @param allAttributes
   * @param revealedAttributes
   * @param unrevealedAttributes
   * @param issuerSetAttribute
   * @throws ConfigurationException
   */
  private void setupCarryOverStates(double unrevealedProbability, List<BigInt> allAttributes,
      List<BigInt> revealedAttributes) throws ConfigurationException {

    List<BigInt> unrevealedAttributes = new ArrayList<BigInt>();
    List<Boolean> carryOverAttribute = new ArrayList<Boolean>();

    ClPublicKeyWrapper pkWrapper = new ClPublicKeyWrapper(keyPair.getPublicKey());
    Group<?,?,?> group = groupFactory.createSignedQuadraticResiduesGroup(pkWrapper.getModulus());
    GroupElement commitment = group.neutralElement();

    for (int i = 0; i < ATTRIBUTE_VALUES.length; i++) {
      BigInt attributeValue = bigIntFactory.valueOf(ATTRIBUTE_VALUES[i]);
      allAttributes.add(attributeValue);

      if (Math.random() > unrevealedProbability) {
        revealedAttributes.add(attributeValue);
        unrevealedAttributes.add(null);
        carryOverAttribute.add(false);
      } else {
        revealedAttributes.add(null);
        unrevealedAttributes.add(attributeValue);
        carryOverAttribute.add(true);
        // calculate commitment for the carryOverState
        GroupElement capR = group.valueOf(pkWrapper.getBase(i));
        commitment = commitment.opMultOp(capR, attributeValue);
      }
    }

    // Setup carry-over states
    carryOverStateIssuer = new CarryOverStateIssuer(commitment, carryOverAttribute);
    carryOverStateRecipient =
        new CarryOverStateRecipient(commitment, bigIntFactory.zero(), unrevealedAttributes);
  }


  /**
   * @param externalDevice
   * @param credSpecId
   * @param attributes
   * @return
   * @throws ConfigurationException
   * @throws ProofException
   */
  private ListOfSignaturesAndAttributes issueUsingProofEngine(boolean externalDevice,
      BigInt credSpecId, List<BigInt> attributes) throws ConfigurationException, ProofException {

    int numberOfAttributes = attributes.size();

    List<ZkModuleProver> proverModules = new ArrayList<ZkModuleProver>();
    List<ZkModuleVerifier> veriferModules = new ArrayList<ZkModuleVerifier>();

    ZkModuleProver zkp =
        clBuildingBlock.getZkModuleProverIssuance(systemParameters, null, keyPair.getPublicKey(),
            keyPair.getPrivateKey(), identifierOfModule, credSpecId, externalDevice, attributes,
            carryOverStateIssuer);
    proverModules.add(zkp);

    ZkProof proof = zkDirector.buildProof(USERNAME, proverModules, systemParameters);

    ZkModuleVerifierIssuance zkv =
        clBuildingBlock.getZkModuleVerifierIssuance(systemParameters, null, keyPair.getPublicKey(),
            identifierOfModule, credSpecId, externalDevice, numberOfAttributes,
            carryOverStateRecipient);
    veriferModules.add(zkv);

    boolean result = zkDirector.verifyProof(proof, veriferModules, systemParameters);
    assertEquals(true, result);

    IssuanceStateRecipient stateRecipient = zkv.recoverIssuanceState();
    ListOfSignaturesAndAttributes sig = clBuildingBlock.extractSignature(null, stateRecipient);

    return sig;
  }

  /**
   * @param externalDevice
   * @param credSpecId
   * @param attributes
   * @return
   * @throws ConfigurationException
   * @throws ProofException
   */
  private ListOfSignaturesAndAttributes issue(boolean externalDevice, BigInt credSpecId,
      List<BigInt> attributes) throws ConfigurationException, ProofException {

    int numberOfAttributes = attributes.size();

    ZkModuleProverIssuance zkp =
        clBuildingBlock.getZkModuleProverIssuance(systemParameters, null, keyPair.getPublicKey(),
            keyPair.getPrivateKey(), identifierOfModule, credSpecId, externalDevice, attributes,
            carryOverStateIssuer);
    ZkProofStateAll dummyZkBuilder = programZkBuilder();
    zkp.initializeModule(dummyZkBuilder);
    zkp.collectAttributesForProof(dummyZkBuilder);
    zkp.firstRound(dummyZkBuilder);
    zkp.secondRound(dummyZkBuilder);
    verify(dummyZkBuilder);

    ZkModuleVerifierIssuance zkv =
        clBuildingBlock.getZkModuleVerifierIssuance(systemParameters, null, keyPair.getPublicKey(),
            identifierOfModule, credSpecId, externalDevice, numberOfAttributes,
            carryOverStateRecipient);
    ZkVerifyStateAll dummyZkVerifier = programZkVerifier();
    zkv.collectAttributesForVerify(dummyZkVerifier);
    boolean verificationSuccess = zkv.verify(dummyZkVerifier);
    assertEquals(true, verificationSuccess);
    IssuanceStateRecipient stateRecipient = zkv.recoverIssuanceState();
    verify(dummyZkVerifier);

    ListOfSignaturesAndAttributes sig = clBuildingBlock.extractSignature(null, stateRecipient);

    return sig;
  }

  // private List<String> dValueLabels;
  private Map<String, Object> dValues;
  private List<String> nValueLabels;
  private List<GroupElement> nValues;
  private Map<String, BigInt> attributeValues;
  private Map<String, Integer> attributeLengths;
  private Map<String, BigInt> rValues;
  private Map<String, BigInt> sValues;
  private BigInt challenge;


  private ZkProofStateAll programZkBuilder() {
    ZkProofStateAll builder = createMock(ZkProofStateAll.class);

    builder.registerAttribute(isA(String.class), eq(false));
    expectLastCall().anyTimes();

    attributeLengths = new HashMap<String, Integer>();
    builder.registerAttribute(isA(String.class), eq(false), EasyMock.anyInt());
    expectLastCall().andAnswer(new IAnswer<Object>() {
      @Override
      public Object answer() throws Throwable {
        attributeLengths.put((String) getCurrentArguments()[0], (Integer) getCurrentArguments()[2]);
        return null;
      }
    }).anyTimes();

    builder.attributeIsRevealed(isA(String.class));
    expectLastCall().anyTimes();

    attributeValues = new HashMap<String, BigInt>();
    builder.setValueOfAttribute(isA(String.class), isA(BigInt.class), EasyMock.anyObject(ResidueClass.class));
    expectLastCall().andAnswer(new IAnswer<Object>() {
      @Override
      public Object answer() throws Throwable {
        attributeValues.put((String) getCurrentArguments()[0], (BigInt) getCurrentArguments()[1]);
        return null;
      }
    }).anyTimes();

    builder.isValueOfAttributeAvailable(isA(String.class));
    expectLastCall().andAnswer(new IAnswer<Object>() {
      @Override
      public Object answer() throws Throwable {
        return true;
      }
    }).anyTimes();

    builder.getValueOfAttribute(isA(String.class));
    expectLastCall().andAnswer(new IAnswer<Object>() {
      @Override
      public Object answer() throws Throwable {
        return attributeValues.get(getCurrentArguments()[0]);
      }
    }).anyTimes();


    // FIXME: program correctly using the attribute length
    rValues = new HashMap<String, BigInt>();
    builder.getRValueOfAttribute(isA(String.class));
    expectLastCall().andAnswer(new IAnswer<Object>() {
      @Override
      public Object answer() throws Throwable {
        EcryptSystemParametersWrapper spWrapper =
            new EcryptSystemParametersWrapper(systemParameters);
        String attributeName = (String) getCurrentArguments()[0];
        int attributeLength = attributeLengths.get(attributeName);
        BigInt rValue = randomGeneration.generateRandomNumber(256);
        // attributeLength + spWrapper.getStatisticalZeroKnowledge() + spWrapper.getHashLength());
        rValues.put(attributeName, rValue);
        return rValue;
      }
    }).anyTimes();

    builder.getChallenge();
    expectLastCall().andAnswer(new IAnswer<Object>() {
      @Override
      public Object answer() throws Throwable {
        challenge = randomGeneration.generateRandomNumber(256);
        return challenge;
      }
    }).anyTimes();

    nValueLabels = new ArrayList<String>();
    nValues = new ArrayList<GroupElement>();
    builder.addNValue(isA(String.class), isA(GroupElement.class));
    expectLastCall().andAnswer(new IAnswer<Object>() {
      @Override
      public Object answer() throws Throwable {
        nValueLabels.add((String) getCurrentArguments()[0]);
        nValues.add((GroupElement) getCurrentArguments()[1]);
        return null;
      }
    }).anyTimes();

    builder.addNValue(isA(String.class), isA(byte[].class));
    expectLastCall().anyTimes();

    dValues = new HashMap<String, Object>();
    builder.addDValue(isA(String.class), isA(GroupElement.class));
    expectLastCall().andAnswer(new IAnswer<Object>() {
      @Override
      public Object answer() throws Throwable {
        dValues.put((String) getCurrentArguments()[0], (GroupElement) getCurrentArguments()[1]);
        return null;
      }
    }).anyTimes();

    builder.addDValue(isA(String.class), isA(BigInt.class));
    expectLastCall().andAnswer(new IAnswer<Object>() {
      @Override
      public Object answer() throws Throwable {
        dValues.put((String) getCurrentArguments()[0], (BigInt) getCurrentArguments()[1]);
        return null;
      }
    }).anyTimes();


    sValues = new HashMap<String, BigInt>();
    builder.addSValue(isA(String.class), isA(BigInt.class));
    expectLastCall().andAnswer(new IAnswer<Object>() {
      @Override
      public Object answer() throws Throwable {
        sValues.put((String) getCurrentArguments()[0], (BigInt) getCurrentArguments()[1]);
        return null;
      }
    }).anyTimes();

    replay(builder);
    return builder;
  }

  private ZkVerifyStateAll programZkVerifier() throws ProofException {
    ZkVerifyStateAll verifier = createMock(ZkVerifyStateAll.class);

    verifier.registerAttribute(isA(String.class), eq(false));
    expectLastCall().anyTimes();

    verifier.attributeIsRevealed(isA(String.class));
    expectLastCall().anyTimes();
    
    verifier.setResidueClass(isA(String.class), EasyMock.anyObject(ResidueClass.class));
    expectLastCall().anyTimes();

    expect(verifier.isRevealedAttribute(isA(String.class))).andReturn(true).anyTimes();

    Iterator<String> iterator = attributeValues.keySet().iterator();
    while (iterator.hasNext()) {
      String label = iterator.next();
      expect(verifier.getValueOfRevealedAttribute(label)).andReturn(attributeValues.get(label))
          .anyTimes();
    }

    verifier.checkNValue(isA(String.class), isA(byte[].class));
    expectLastCall().anyTimes();

    for (int i = 0; i < nValues.size(); ++i) {
      String label = nValueLabels.get(i);
      GroupElement nValue = nValues.get(i);
      verifier.checkNValue(eq(label), eq(nValue));
    }

    iterator = dValues.keySet().iterator();
    while (iterator.hasNext()) {
      String label = iterator.next();
      Object s = dValues.get(label);
      if (s instanceof BigInt) {
        expect(verifier.getDValueAsInteger(eq(label))).andReturn((BigInt) s);
      } else if (s instanceof GroupElement) {
        expect(verifier.getDValueAsGroupElement(eq(label), isA(HiddenOrderGroup.class))).andReturn(
            (HiddenOrderGroupElement) s);
      }
    }

    iterator = rValues.keySet().iterator();
    while (iterator.hasNext()) {
      String label = iterator.next();
      BigInt r = rValues.get(label);
      BigInt s = attributeValues.get(label).multiply(challenge).add(r);
      expect(verifier.getSValueAsInteger(eq(label))).andReturn(s);
    }
    iterator = sValues.keySet().iterator();
    while (iterator.hasNext()) {
      String label = iterator.next();
      BigInt s = sValues.get(label);
      expect(verifier.getSValueAsInteger(eq(label))).andReturn(s);
    }

    expect(verifier.getChallenge()).andReturn(challenge).anyTimes();

    replay(verifier);
    return verifier;
  }


}
