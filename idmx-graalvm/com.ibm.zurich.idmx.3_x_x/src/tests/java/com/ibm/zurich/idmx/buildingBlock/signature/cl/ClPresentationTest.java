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
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.ibm.zurich.idmx.buildingBlock.factory.BuildingBlockFactory;
import com.ibm.zurich.idmx.dagger.AbcComponent;
import com.ibm.zurich.idmx.dagger.DaggerAbcComponent;
import com.ibm.zurich.idmx.tests.TestInitialisation;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.ibm.zurich.idmix.abc4trust.facades.IssuerParametersFacade;
import com.ibm.zurich.idmx.dagger.CryptoTestModule;
import com.ibm.zurich.idmx.buildingBlock.structural.constant.ConstantBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.reveal.RevealAttributeBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.systemParameters.EcryptSystemParametersWrapper;
import com.ibm.zurich.idmx.device.ExternalSecretsManagerImpl;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.ProofException;
import com.ibm.zurich.idmx.exception.SerializationException;
import com.ibm.zurich.idmx.interfaces.device.ExternalSecretsManager;
import com.ibm.zurich.idmx.interfaces.proofEngine.ZkDirector;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.RandomGeneration;
import com.ibm.zurich.idmx.interfaces.util.group.GroupElement;
import com.ibm.zurich.idmx.interfaces.util.group.GroupFactory;
import com.ibm.zurich.idmx.interfaces.util.group.HiddenOrderGroup;
import com.ibm.zurich.idmx.interfaces.util.group.HiddenOrderGroupElement;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProver;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleVerifier;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ResidueClass;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateAll;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkVerifyStateAll;
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

@Ignore
public class ClPresentationTest {
  SystemParameters systemParameters;
  PublicKey publicKey;
  CredentialSpecification credentialSpecification;

  BigInt MODULUS;
  ExternalSecretsManagerImpl esManager;
  ClSignatureBuildingBlock clSignatureBuildingBlock;
  BigIntFactory bigIntFactory;
  GroupFactory groupFactory;
  RandomGeneration randomGeneration;

  ConstantBuildingBlock constantBB;
  RevealAttributeBuildingBlock revealBB;
  ZkDirector zkDirector;
  EcryptSystemParametersWrapper spWrapper;
  KeyManager km;

  private static final String IDENTIFIER_OF_MODULE = "test";
  private static final String USERNAME = "user";


  @Before
  public void setUp() throws SerializationException, ConfigurationException {
    systemParameters = TestUtils.getResource("../sp_default.xml", SystemParameters.class, this);
    spWrapper = new EcryptSystemParametersWrapper(systemParameters);
    credentialSpecification =
        TestUtils.getResource("../credSpec_simpleID.xml", CredentialSpecification.class, this);

    AbcComponent i = TestInitialisation.INJECTOR;;
    BuildingBlockFactory bbf = i.provideBuildingBlockFactory();
    bigIntFactory = i.provideBigIntFactory();
    groupFactory = i.provideGroupFactory();
    randomGeneration = i.provideRandomGeneration();
    zkDirector = i.providesZkDirector();
    clSignatureBuildingBlock = bbf.getBuildingBlockByClass(ClSignatureBuildingBlock.class);


//    Injector injector = Guice.createInjector(new CryptoTestModule());
//
//    // Objects used for the CL signature block
//    clSignatureBuildingBlock = injector.getInstance(ClSignatureBuildingBlock.class);
//    bigIntFactory = injector.getInstance(BigIntFactory.class);
//    groupFactory = injector.getInstance(GroupFactory.class);
//    randomGeneration = injector.getInstance(RandomGeneration.class);
//    km = injector.getInstance(KeyManager.class);
//    esManager = (ExternalSecretsManagerImpl) injector.getInstance(ExternalSecretsManager.class);
//
//    // Objects required for testing with the proof engine
//    constantBB = injector.getInstance(ConstantBuildingBlock.class);
//    revealBB = injector.getInstance(RevealAttributeBuildingBlock.class);
//    zkDirector = injector.getInstance(ZkDirector.class);
  }

  // TODO: Add mock commands for smart card
  @Ignore
  @Test
  public void present_allHidden() throws SerializationException, ConfigurationException,
      ProofException {
    present(Collections.<Integer>emptyList());
  }

  // TODO: Add mock commands for smart card
  @Ignore
  @Test
  public void present_oneRevealed() throws SerializationException, ConfigurationException,
      ProofException {
    present(Collections.singletonList(0));
  }

  // TODO: Add mock commands for smart card
  @Ignore
  @Test
  public void present_oneHidden() throws SerializationException, ConfigurationException,
      ProofException {
    List<Integer> revealed = new ArrayList<Integer>();
    for (int i = 0; i < ClIssuanceWithMockTest.ATTRIBUTE_VALUES.length - 1; ++i) {
      revealed.add(i);
    }
    present(revealed);
  }

  // TODO: Add mock commands for smart card
  @Ignore
  @Test
  public void present_allRevealed() throws SerializationException, ConfigurationException,
      ProofException {
    List<Integer> revealed = new ArrayList<Integer>();
    for (int i = 0; i < ClIssuanceWithMockTest.ATTRIBUTE_VALUES.length; i++) {
      revealed.add(i);
    }
    present(revealed);
  }

  @Test
  public void presentProofEngine_allHidden() throws SerializationException, ConfigurationException,
      ProofException {
    present_proofEngine(Collections.<Integer>emptyList(), false);
    present_proofEngine(Collections.<Integer>emptyList(), true);
  }

  @Test
  public void presentProofEngine_oneRevealed() throws SerializationException,
      ConfigurationException, ProofException {
    present_proofEngine(Collections.singletonList(0), false);
    present_proofEngine(Collections.singletonList(0), true);
  }

  @Test
  public void presentProofEngine_oneHidden() throws SerializationException, ConfigurationException,
      ProofException {
    List<Integer> revealed = new ArrayList<Integer>();
    for (int i = 0; i < ClIssuanceWithMockTest.ATTRIBUTE_VALUES.length - 1; ++i) {
      revealed.add(i);
    }
    present_proofEngine(revealed, false);
    present_proofEngine(revealed, true);
  }

  @Test
  public void presentProofEngine_allRevealed() throws SerializationException,
      ConfigurationException, ProofException {
    List<Integer> revealed = new ArrayList<Integer>();
    for (int i = 0; i < ClIssuanceWithMockTest.ATTRIBUTE_VALUES.length; i++) {
      revealed.add(i);
    }
    present_proofEngine(revealed, false);
    present_proofEngine(revealed, true);
  }



  private void present_proofEngine(List<Integer> revealed, boolean usingSmartcard)
      throws SerializationException, ConfigurationException, ProofException {

    String keyPairFile = usingSmartcard ? "keyPair_cl_smartCard.xml" : "keyPair_cl.xml";
    publicKey = TestUtils.getResource(keyPairFile, KeyPair.class, this).getPublicKey();
    ClPublicKeyWrapper clpkw = new ClPublicKeyWrapper(publicKey);

    MODULUS = clpkw.getModulus();

    Signature sig = TestUtils.getResource("clSignature1.xml", Signature.class, this);
    SignatureToken tok = sig.getSignatureToken().get(0);
    BigInt credSpecId =
        ClIssuanceWithMockTest.getNumericalCredSpecId(credentialSpecification, systemParameters,
            bigIntFactory);
    
    List<BigInt> attributes = new ArrayList<BigInt>();
    Map<String, BigInt> verifierAttributes = new HashMap<String, BigInt>();
    for (int i = 0; i < ClIssuanceWithMockTest.ATTRIBUTE_VALUES.length; i++) {
      BigInt attValue = bigIntFactory.valueOf(ClIssuanceWithMockTest.ATTRIBUTE_VALUES[i]);
      attributes.add(attValue);
      if (revealed.contains(i)) {
        verifierAttributes.put(IDENTIFIER_OF_MODULE + ":" + i, attValue);
      } else {
        verifierAttributes.put(IDENTIFIER_OF_MODULE + ":" + i, null);
      }
    }
    int numberOfAttributes = attributes.size();

    URI issuerUri = null;
    URI deviceUri = null;
    URI credentialUri = null;
    if (usingSmartcard) {
      try {
        deviceUri = URI.create("TestDevice");
        issuerUri = URI.create("TestIssuer");
        credentialUri = URI.create("TestCredential");

        km.storeSystemParameters(systemParameters);
        km.storeIssuerParameters(issuerUri, IssuerParametersFacade.initIssuerParameters(clpkw.getPublicKey(),
          systemParameters).getIssuerParameters());
        esManager.allocateCredential(USERNAME, deviceUri, credentialUri, issuerUri, true);
      } catch (KeyManagerException e) {
        throw new RuntimeException(e);
      }
    }


    List<ZkModuleProver> modulesProver = new ArrayList<ZkModuleProver>();
    List<ZkModuleVerifier> modulesVerifier = new ArrayList<ZkModuleVerifier>();

    for (int i : revealed) {
      ZkModuleProver zk = revealBB.getZkModuleProver(IDENTIFIER_OF_MODULE + ":" + i);
      modulesProver.add(zk);
    }
    ZkModuleProver zkp =
        clSignatureBuildingBlock.getZkModuleProverPresentation(systemParameters, null, publicKey,
            IDENTIFIER_OF_MODULE, tok, attributes, credSpecId, deviceUri, USERNAME, credentialUri);
    modulesProver.add(zkp);

    ZkProof proof = zkDirector.buildProof(USERNAME, modulesProver, systemParameters);

    // Serialisation
    String xmlProof = JaxbHelperClass.serialize((new ObjectFactory()).createZkProof(proof));
    System.out.println(xmlProof);

    for (int i : revealed) {
      ZkModuleVerifier zk = revealBB.getZkModuleVerifier(IDENTIFIER_OF_MODULE + ":" + i, null);
      modulesVerifier.add(zk);
    }
    ZkModuleVerifier zkv =
        clSignatureBuildingBlock.getZkModuleVerifierPresentation(systemParameters, null, publicKey,
            IDENTIFIER_OF_MODULE, credSpecId, numberOfAttributes, usingSmartcard);
    modulesVerifier.add(zkv);

    boolean result = zkDirector.verifyProof(proof, modulesVerifier, systemParameters);
    assertTrue(result);

  }


  private void present(List<Integer> revealed) throws SerializationException,
      ConfigurationException, ProofException {

    Signature sig = TestUtils.getResource("clSignature1.xml", Signature.class, this);
    SignatureToken tok = sig.getSignatureToken().get(0);

    boolean externalDevice = false;

    BigInt credSpecId =
        ClIssuanceWithMockTest.getNumericalCredSpecId(credentialSpecification, systemParameters,
            bigIntFactory);

    List<BigInt> attributes = new ArrayList<BigInt>();
    Map<String, BigInt> verifierAttributes = new HashMap<String, BigInt>();

    int numberOfAttributes = ClIssuanceWithMockTest.ATTRIBUTE_VALUES.length;
    for (int i = 0; i < numberOfAttributes; i++) {
      BigInt attValue = bigIntFactory.valueOf(ClIssuanceWithMockTest.ATTRIBUTE_VALUES[i]);
      attributes.add(attValue);
      if (revealed.contains(i)) {
        verifierAttributes.put(IDENTIFIER_OF_MODULE + ":" + i, attValue);
      } else {
        verifierAttributes.put(IDENTIFIER_OF_MODULE + ":" + i, null);
      }
    }

    ZkModuleProver zkp =
        clSignatureBuildingBlock.getZkModuleProverPresentation(systemParameters, null, publicKey,
            IDENTIFIER_OF_MODULE, tok, attributes, credSpecId, null, USERNAME, null);
    ZkProofStateAll dummyZkBuilder = programZkBuilder(revealed);
    zkp.initializeModule(dummyZkBuilder);
    zkp.collectAttributesForProof(dummyZkBuilder);
    zkp.firstRound(dummyZkBuilder);
    zkp.secondRound(dummyZkBuilder);
    verify(dummyZkBuilder);

    ZkModuleVerifier zkv =
        clSignatureBuildingBlock.getZkModuleVerifierPresentation(systemParameters, null, publicKey,
            IDENTIFIER_OF_MODULE, credSpecId, numberOfAttributes, externalDevice);
    ZkVerifyStateAll dummyZkVerifier = programZkVerifier(verifierAttributes);
    zkv.collectAttributesForVerify(dummyZkVerifier);
    boolean result = zkv.verify(dummyZkVerifier);
    assertTrue(result);

    verify(dummyZkVerifier);
  }



  private Map<String, Object> dValues;
  private List<String> nValueLabels;
  private List<GroupElement> nValues;
  private Map<String, BigInt> attributeValues;
  private Map<String, Integer> attributeLengths;
  private Map<String, BigInt> rValues;
  private Map<String, BigInt> sValues;
  private BigInt challenge;
  private Set<String> revealedAttributes;


  private ZkProofStateAll programZkBuilder(List<Integer> revealed) {
    ZkProofStateAll builder = createMock(ZkProofStateAll.class);

    revealedAttributes = new HashSet<String>();
    for (Integer index : revealed) {
      revealedAttributes.add(IDENTIFIER_OF_MODULE + ":" + index);
    }

    expect(builder.isRevealedAttribute(isA(String.class))).andAnswer(new IAnswer<Boolean>() {
      @Override
      public Boolean answer() throws Throwable {
        String key = (String) getCurrentArguments()[0];
        return revealedAttributes.contains(key);
      }
    }).anyTimes();

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

    builder.getValueOfAttribute(isA(String.class));
    expectLastCall().andAnswer(new IAnswer<Object>() {
      @Override
      public Object answer() throws Throwable {
        return attributeValues.get(getCurrentArguments()[0]);
      }
    }).anyTimes();

    builder.requiresAttributeValue(isA(String.class));
    expectLastCall().anyTimes();

    rValues = new HashMap<String, BigInt>();
    builder.getRValueOfAttribute(isA(String.class));
    expectLastCall().andAnswer(new IAnswer<Object>() {
      @Override
      public Object answer() throws Throwable {
        EcryptSystemParametersWrapper spWrapper =
            new EcryptSystemParametersWrapper(systemParameters);
        String attributeName = (String) getCurrentArguments()[0];
        int attributeLength = attributeLengths.get(attributeName);
        BigInt rValue = randomGeneration.generateRandomNumber(attributeLength);
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

  private ZkVerifyStateAll programZkVerifier(final Map<String, BigInt> verifierAttributeValues)
      throws ProofException {
    ZkVerifyStateAll verifier = createMock(ZkVerifyStateAll.class);

    verifier.registerAttribute(isA(String.class), eq(false), EasyMock.anyInt());
    expectLastCall().anyTimes();

    verifier.attributeIsRevealed(isA(String.class));
    expectLastCall().anyTimes();

    expect(verifier.isRevealedAttribute(isA(String.class))).andAnswer(new IAnswer<Boolean>() {
      @Override
      public Boolean answer() throws Throwable {
        String key = (String) getCurrentArguments()[0];
        return (verifierAttributeValues.get(key) != null);
      }
    }).anyTimes();

    Iterator<String> iterator = verifierAttributeValues.keySet().iterator();
    while (iterator.hasNext()) {
      String label = iterator.next();
      expect(verifier.getValueOfRevealedAttribute(label)).andReturn(
          verifierAttributeValues.get(label)).anyTimes();
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
      if (verifierAttributeValues.get(label) == null) {
        BigInt s = r.subtract(attributeValues.get(label).multiply(challenge));
        expect(verifier.getSValueAsInteger(eq(label))).andReturn(s);
      }
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
