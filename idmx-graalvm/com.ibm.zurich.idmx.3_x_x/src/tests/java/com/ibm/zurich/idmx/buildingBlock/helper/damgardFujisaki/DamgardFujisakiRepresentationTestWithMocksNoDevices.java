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
package com.ibm.zurich.idmx.buildingBlock.helper.damgardFujisaki;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.getCurrentArguments;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.ibm.zurich.idmx.dagger.AbcComponent;
import com.ibm.zurich.idmx.tests.TestInitialisation;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Before;
import org.junit.Test;

import com.ibm.zurich.idmx.buildingBlock.helper.BaseForRepresentation;
import com.ibm.zurich.idmx.buildingBlock.helper.representation.damgardFujisaki.DamgardFujisakiRepresentationBuildingBlock;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.ProofException;
import com.ibm.zurich.idmx.exception.SerializationException;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.RandomGeneration;
import com.ibm.zurich.idmx.interfaces.util.group.GroupElement;
import com.ibm.zurich.idmx.interfaces.util.group.GroupFactory;
import com.ibm.zurich.idmx.interfaces.util.group.HiddenOrderGroup;
import com.ibm.zurich.idmx.interfaces.util.group.HiddenOrderGroupElement;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProverCommitment;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleVerifier;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ResidueClass;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateAll;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkVerifyStateAll;
import com.ibm.zurich.idmx.tests.TestUtils;

import eu.abc4trust.xml.SystemParameters;

public class DamgardFujisakiRepresentationTestWithMocksNoDevices {
  private SystemParameters systemParameters;
  private DamgardFujisakiRepresentationBuildingBlock damgardFujisakiBB;
  private BigIntFactory bigIntFactory;
  private GroupFactory groupFactory;
  private RandomGeneration randomGeneration;

  private static final String IDENTIFIER_OF_MODULE = "test";
  private static final String USERNAME = "user";
  public static final int[] ATTRIBUTE_VALUES = new int[] {13, 21, 34, 55, 89, 144};
  public static final int[] BASE_VALUES = new int[] {4, 9, 16, 25, 36, 49};
  public static final int MODULUS = 4330237;// 2063 * 2099

  private HiddenOrderGroup group;

  @Before
  public void setUp() throws SerializationException, ConfigurationException {
    systemParameters = TestUtils.getResource("sp_default.xml", SystemParameters.class, this);
    AbcComponent inj = TestInitialisation.INJECTOR;
    bigIntFactory = inj.provideBigIntFactory();
    groupFactory = inj.provideGroupFactory();
    randomGeneration = inj.provideRandomGeneration();
    damgardFujisakiBB = inj.provideBuildingBlockFactory()
            .getBuildingBlockByClass(DamgardFujisakiRepresentationBuildingBlock.class);

//    Injector injector = Guice.createInjector(new CryptoTestModule());
//
//    bigIntFactory = injector.getInstance(BigIntFactory.class);
//    groupFactory = injector.getInstance(GroupFactory.class);
//    randomGeneration = injector.getInstance(RandomGeneration.class);
//    damgardFujisakiBB = injector.getInstance(DamgardFujisakiRepresentationBuildingBlock.class);
  }

  @Test
  public void allRevealedNoneRandom() throws SerializationException, ConfigurationException,
      ProofException {
    List<Integer> revealed = new ArrayList<Integer>();
    for (int i = 0; i < ATTRIBUTE_VALUES.length; ++i) {
      revealed.add(i);
    }
    present(revealed, new ArrayList<Integer>());
  }

  @Test
  public void noneRevealedNoneRandom() throws SerializationException, ConfigurationException,
      ProofException {
    List<Integer> revealed = new ArrayList<Integer>();
    present(revealed, new ArrayList<Integer>());
  }


  @Test
  public void noneRevealedAllRandom() throws SerializationException, ConfigurationException,
      ProofException {
    List<Integer> random = new ArrayList<Integer>();
    for (int i = 0; i < ATTRIBUTE_VALUES.length; ++i) {
      random.add(i);
    }
    present(new ArrayList<Integer>(), random);
  }

  @Test
  public void allRevealedAllRandom() throws SerializationException, ConfigurationException,
      ProofException {
    List<Integer> revealed = new ArrayList<Integer>();
    List<Integer> random = new ArrayList<Integer>();
    for (int i = 0; i < ATTRIBUTE_VALUES.length; ++i) {
      random.add(i);
      revealed.add(i);
    }
    present(revealed, random);
  }

  @Test
  public void someRevealedSomeRandom() throws SerializationException, ConfigurationException,
      ProofException {
    Random generator = new Random();
    List<Integer> revealed = new ArrayList<Integer>();
    List<Integer> random = new ArrayList<Integer>();
    for (int i = 0; i < ATTRIBUTE_VALUES.length; ++i) {
      if (generator.nextInt(ATTRIBUTE_VALUES.length) < ATTRIBUTE_VALUES.length / 2) {
        random.add(i);
      }
      if (generator.nextInt(ATTRIBUTE_VALUES.length) < ATTRIBUTE_VALUES.length / 2) {
        revealed.add(i);
      }
    }
    present(revealed, random);
  }

  private void present(List<Integer> revealed, List<Integer> random) throws SerializationException,
      ConfigurationException, ProofException {
    List<BigInt> attributes = new ArrayList<BigInt>();
    for (int attValue : ATTRIBUTE_VALUES) {
      attributes.add(bigIntFactory.valueOf(attValue));
    }

    List<BaseForRepresentation> bases = new ArrayList<BaseForRepresentation>();
    HiddenOrderGroupElement b;
    group = groupFactory.createSignedQuadraticResiduesGroup(bigIntFactory.valueOf(MODULUS));
    for (int i = 0; i < BASE_VALUES.length; ++i) {
      int baseValue = BASE_VALUES[i];
      b = group.valueOf(bigIntFactory.valueOf(baseValue));
      if (random.contains(i)) {
        bases.add(BaseForRepresentation.randomAttribute(b));
      } else {
        bases.add(BaseForRepresentation.managedAttribute(b));
      }
    }

    ZkModuleProverCommitment<HiddenOrderGroupElement> zkp = damgardFujisakiBB.getZkModuleProver(systemParameters, // systemParameters
        IDENTIFIER_OF_MODULE, // identifierOfModule
        null, // identifierOfCredentialForSecret
        bases, // bases
        group, // group
        null, // potentially the commitment
        null, // deviceUid
        USERNAME, 
        null); // scope

    ZkProofStateAll dummyZkProver = programZkBuilder(revealed, random);
    zkp.initializeModule(dummyZkProver);
    zkp.collectAttributesForProof(dummyZkProver);

    zkp.firstRound(dummyZkProver);
    zkp.secondRound(dummyZkProver);
    verify(dummyZkProver);

    Random generator = new Random();
    HiddenOrderGroupElement commitment;
    String commitmentAsDValue;
    if (generator.nextInt(2) == 0) {
      commitment = zkp.recoverCommitment();
      commitmentAsDValue = null;
    } else {
      commitment = null;
      commitmentAsDValue = IDENTIFIER_OF_MODULE + ":C";
    }


    // ----------------------- VERIFIER -------------

    ZkModuleVerifier zkv =
        damgardFujisakiBB.getZkModuleVerifier(systemParameters, IDENTIFIER_OF_MODULE, bases,
            commitment, commitmentAsDValue, group);
    ZkVerifyStateAll dummyZkVerifier = programZkVerifier(attributes);
    zkv.collectAttributesForVerify(dummyZkVerifier);
    boolean result = zkv.verify(dummyZkVerifier);

    assertTrue(result);

    verify(dummyZkVerifier);

    commitment = zkp.recoverCommitment();
    if (random.size() == 0) {
      assertEquals(commitment.toBigInt(), bigIntFactory.valueOf(749660));
    }
  }



  private Set<String> revealedAttributes;
  private Map<String, HiddenOrderGroupElement> dValuesGroupElements;
  private Map<String, BigInt> dValuesBigInt;
  private Map<String, BigInt> rValues;
  private Map<String, BigInt> sValues;
  private Map<String, BigInt> aValues;
  private Map<String, HiddenOrderGroupElement> nValuesGroupElements;
  private Map<String, byte[]> nValuesByteArray;
  private HiddenOrderGroupElement tValue;
  BigInt challenge;

  private ZkProofStateAll programZkBuilder(List<Integer> revealed, List<Integer> random) {
    challenge = randomGeneration.generateRandomNumber(bigIntFactory.valueOf(MODULUS));

    revealedAttributes = new HashSet<String>();
    for (Integer index : revealed) {
      revealedAttributes.add(IDENTIFIER_OF_MODULE + ":" + index);
    }

    ZkProofStateAll prover = createMock(ZkProofStateAll.class);

    // registerAttribute
    prover.registerAttribute(isA(String.class), EasyMock.anyBoolean());
    expectLastCall().anyTimes();

    prover.registerAttribute(isA(String.class), EasyMock.anyBoolean(), EasyMock.anyInt());
    expectLastCall().anyTimes();

    // providesAttribute
    prover.providesAttribute(isA(String.class));
    expectLastCall().anyTimes();

    // getValueOfAttribute
    aValues = new LinkedHashMap<String, BigInt>();
    for (int i = 0; i < ATTRIBUTE_VALUES.length; ++i) {
      if (!random.contains(i)) {
        aValues.put(IDENTIFIER_OF_MODULE + ":" + i, bigIntFactory.valueOf(ATTRIBUTE_VALUES[i]));
      }
    }
    prover.getValueOfAttribute(isA(String.class));
    expectLastCall().andAnswer(new IAnswer<BigInt>() {
      @Override
      public BigInt answer() throws Throwable {
        return aValues.get(getCurrentArguments()[0]);
      }
    }).anyTimes();

    // setAttributeValue
    prover.setValueOfAttribute(isA(String.class), isA(BigInt.class), EasyMock.anyObject(ResidueClass.class));
    expectLastCall().andAnswer(new IAnswer<Object>() {
      @Override
      public Object answer() throws Throwable {
        aValues.put((String) getCurrentArguments()[0], (BigInt) getCurrentArguments()[1]);
        return null;
      }
    }).anyTimes();

    // getRValueOfAttribute
    sValues = new LinkedHashMap<String, BigInt>();
    rValues = new LinkedHashMap<String, BigInt>();
    prover.getRValueOfAttribute(isA(String.class));
    expectLastCall().andAnswer(new IAnswer<BigInt>() {
      @Override
      public BigInt answer() throws Throwable {
        BigInt r;
        String key = (String) getCurrentArguments()[0];
        if (rValues.containsKey(key)) {
          r = rValues.get(key);
        } else {
          r = group.createRandomIterationcounter(randomGeneration, 20);
          rValues.put(key, r);
        }
        sValues.put(key, r.subtract(aValues.get(key).multiply(challenge)));
        return r;
      }
    }).anyTimes();

    // isRevealedAttribute
    prover.isRevealedAttribute(isA(String.class));
    expectLastCall().andAnswer(new IAnswer<Boolean>() {
      @Override
      public Boolean answer() throws Throwable {
        return revealedAttributes.contains(getCurrentArguments()[0]);
      }
    }).anyTimes();

    // addNValue
    nValuesGroupElements = new LinkedHashMap<String, HiddenOrderGroupElement>();
    prover.addNValue(isA(String.class), isA(GroupElement.class));
    expectLastCall().andAnswer(new IAnswer<Object>() {
      @Override
      public Object answer() throws Throwable {
        nValuesGroupElements.put((String) getCurrentArguments()[0],
            (HiddenOrderGroupElement) getCurrentArguments()[1]);
        return null;
      }
    }).anyTimes();

    nValuesByteArray = new LinkedHashMap<String, byte[]>();
    prover.addNValue(isA(String.class), isA(byte[].class));
    expectLastCall().andAnswer(new IAnswer<Object>() {
      @Override
      public Object answer() throws Throwable {
        nValuesByteArray.put((String) getCurrentArguments()[0], (byte[]) getCurrentArguments()[1]);
        return null;
      }
    }).anyTimes();

    // addTValue
    prover.addTValue(isA(String.class), isA(GroupElement.class));
    expectLastCall().andAnswer(new IAnswer<Object>() {
      @Override
      public Object answer() throws Throwable {
        tValue = (HiddenOrderGroupElement) getCurrentArguments()[1];
        return null;
      }
    }).anyTimes();

    // addDValue
    dValuesGroupElements = new LinkedHashMap<String, HiddenOrderGroupElement>();
    prover.addDValue(isA(String.class), isA(GroupElement.class));
    expectLastCall().andAnswer(new IAnswer<Object>() {
      @Override
      public Object answer() throws Throwable {
        dValuesGroupElements.put((String) getCurrentArguments()[0],
            (HiddenOrderGroupElement) getCurrentArguments()[1]);
        return null;
      }
    }).anyTimes();

    dValuesBigInt = new LinkedHashMap<String, BigInt>();
    prover.addDValue(isA(String.class), isA(BigInt.class));
    expectLastCall().andAnswer(new IAnswer<Object>() {
      @Override
      public Object answer() throws Throwable {
        dValuesBigInt.put((String) getCurrentArguments()[0], (BigInt) getCurrentArguments()[1]);
        return null;
      }
    }).anyTimes();

    // getChallenge
    prover.getChallenge();
    expectLastCall().andAnswer(new IAnswer<BigInt>() {
      @Override
      public BigInt answer() throws Throwable {
        return challenge;
      }
    }).anyTimes();

    // requiresAttribute
    prover.requiresAttributeValue(isA(String.class));
    expectLastCall().anyTimes();

    replay(prover);
    return prover;
  }

  private ZkVerifyStateAll programZkVerifier(List<BigInt> attributes) throws ProofException {
    ZkVerifyStateAll verifier = createMock(ZkVerifyStateAll.class);

    // isRevealedAttribute
    verifier.isRevealedAttribute(isA(String.class));
    expectLastCall().andAnswer(new IAnswer<Boolean>() {
      @Override
      public Boolean answer() throws Throwable {
        return revealedAttributes.contains(getCurrentArguments()[0]);
      }
    }).anyTimes();

    // checkNValue
    verifier.checkNValue(isA(String.class), isA(GroupElement.class));
    expectLastCall().andAnswer(new IAnswer<Boolean>() {
      @Override
      public Boolean answer() throws Throwable {
        return nValuesGroupElements.get(getCurrentArguments()[0]).equals(
            getCurrentArguments()[1]);
      }
    }).anyTimes();

    verifier.checkNValue(isA(String.class), isA(byte[].class));
    expectLastCall().andAnswer(new IAnswer<Boolean>() {
      @Override
      public Boolean answer() throws Throwable {
        return nValuesByteArray.get(getCurrentArguments()[0]).equals(
            getCurrentArguments()[1]);
      }
    }).anyTimes();

    // getChallenge
    verifier.getChallenge();
    expectLastCall().andAnswer(new IAnswer<BigInt>() {
      @Override
      public BigInt answer() throws Throwable {
        return challenge;
      }
    }).anyTimes();

    // getValueOfRevealedAttribute
    verifier.getValueOfRevealedAttribute(isA(String.class));
    expectLastCall().andAnswer(new IAnswer<BigInt>() {
      @Override
      public BigInt answer() throws Throwable {
        return aValues.get(getCurrentArguments()[0]);
      }
    }).anyTimes();

    // getDValueAsGroupElement
    verifier.getDValueAsGroupElement(isA(String.class), isA(HiddenOrderGroup.class));
    expectLastCall().andAnswer(new IAnswer<HiddenOrderGroupElement>() {
      @Override
      public HiddenOrderGroupElement answer() throws Throwable {
        return dValuesGroupElements.get(getCurrentArguments()[0]);
      }
    }).anyTimes();

    // getSValueAsInteger
    verifier.getSValueAsInteger(isA(String.class));
    expectLastCall().andAnswer(new IAnswer<BigInt>() {
      @Override
      public BigInt answer() throws Throwable {
        String key = ((String) getCurrentArguments()[0]);
        return sValues.get(key);
      }
    }).anyTimes();

    // checkTValue
    verifier.checkTValue(isA(String.class), isA(GroupElement.class));
    expectLastCall().andAnswer(new IAnswer<Object>() {
      @Override
      public Object answer() throws Throwable {
        HiddenOrderGroupElement t = (HiddenOrderGroupElement) getCurrentArguments()[1];
        assertEquals(t, tValue);
        return null;
      }
    }).anyTimes();

    // registerAttribute
    verifier.registerAttribute(isA(String.class), EasyMock.anyBoolean());
    expectLastCall().anyTimes();

    verifier.registerAttribute(isA(String.class), EasyMock.anyBoolean(), EasyMock.anyInt());
    expectLastCall().anyTimes();

    replay(verifier);
    return verifier;
  }
}
