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
import com.ibm.zurich.idmx.buildingBlock.signature.cl.ClIssuanceWithMockTest;
import com.ibm.zurich.idmx.dagger.AbcComponent;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.ProofException;
import com.ibm.zurich.idmx.exception.SerializationException;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.RandomGeneration;
import com.ibm.zurich.idmx.interfaces.util.group.GroupElement;
import com.ibm.zurich.idmx.interfaces.util.group.GroupFactory;
import com.ibm.zurich.idmx.interfaces.util.group.KnownOrderGroup;
import com.ibm.zurich.idmx.interfaces.util.group.KnownOrderGroupElement;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProver;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleVerifier;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ResidueClass;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateAll;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkVerifyStateAll;
import com.ibm.zurich.idmx.tests.TestInitialisation;
import com.ibm.zurich.idmx.tests.TestUtils;
import eu.abc4trust.xml.*;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.Map.Entry;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertTrue;

public class UProvePresentationTest {
  SystemParameters systemParameters;
  PublicKey publicKey;
  CredentialSpecification credentialSpecification;

  BrandsSignatureBuildingBlock uproveBB;
  BigIntFactory bigIntFactory;
  GroupFactory groupFactory;
  RandomGeneration randomGeneration;

  private static final String IDENTIFIER_OF_MODULE = "test";
  private static final String USERNAME = "user";


  @Before
  public void setUp() throws SerializationException, ConfigurationException {
    systemParameters = TestUtils.getResource("../sp_default.xml", SystemParameters.class, this);
    publicKey = TestUtils.getResource("keyPair_brands.xml", KeyPair.class, this).getPublicKey();
    credentialSpecification =
        TestUtils.getResource("../credSpec_simpleID.xml", CredentialSpecification.class, this);

    AbcComponent abc = TestInitialisation.INJECTOR;
    BuildingBlockFactory bbf = abc.provideBuildingBlockFactory();
    uproveBB = bbf.getBuildingBlockByClass(BrandsSignatureBuildingBlock.class);
    bigIntFactory = abc.provideBigIntFactory();
    groupFactory = abc.provideGroupFactory();
    randomGeneration = abc.provideRandomGeneration();
  }


  @Test
  public void presentAllHidden() throws SerializationException, ConfigurationException,
      ProofException {
    present(Collections.<Integer>emptyList());
  }


  @Test
  public void presentOneRevealed() throws SerializationException, ConfigurationException,
      ProofException {
    present(Collections.singletonList(0));
  }


  @Test
  public void presentAllRevealed() throws SerializationException, ConfigurationException,
      ProofException {
    List<Integer> revealed = new ArrayList<Integer>();
    for (int i = 0; i < UProveIssuanceWithMockTest.ATTRIBUTE_VALUES.length; ++i) {
      revealed.add(i);
    }
    present(revealed);
  }


  private void present(List<Integer> revealed) throws SerializationException,
      ConfigurationException, ProofException {
    Signature sig = TestUtils.getResource("brandsSignature1.xml", Signature.class, this);
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
    ZkProofStateAll dummyZkBuilder = programZkBuilder(revealed);
    zkp.initializeModule(dummyZkBuilder);
    zkp.collectAttributesForProof(dummyZkBuilder);
    zkp.firstRound(dummyZkBuilder);
    zkp.secondRound(dummyZkBuilder);
    verify(dummyZkBuilder);

    ZkModuleVerifier zkv =
        uproveBB.getZkModuleVerifierPresentation(systemParameters, null, publicKey,
            IDENTIFIER_OF_MODULE, credSpecId, numberOfAttributes, externalDevice);
    ZkVerifyStateAll dummyZkVerifier = programZkVerifier(attributes);
    zkv.collectAttributesForVerify(dummyZkVerifier);
    boolean result = zkv.verify(dummyZkVerifier);
    assertTrue(result);

    verify(dummyZkVerifier);
  }


  private Map<String, GroupElement<?, ?, ?>> dValuesGE;
  private Map<String, byte[]> dValuesO;
  private Map<String, BigInt> dValuesI;
  private Map<String, BigInt> rValues;
  private Map<String, BigInt> sValues;
  private Map<String, BigInt> aValues;
  private Set<String> revealedAttributes;
  private byte[] hashContribution;
  BigInt challenge;


  private ZkProofStateAll programZkBuilder(List<Integer> revealed) {
    challenge = randomGeneration.generateRandomNumber(500);

    revealedAttributes = new HashSet<String>();
    for (Integer index : revealed) {
      revealedAttributes.add(IDENTIFIER_OF_MODULE + ":" + index);
    }

    ZkProofStateAll builder = createMock(ZkProofStateAll.class);
    
    builder.markAsSignatureBuildingBlock();

    builder.registerAttribute(isA(String.class), eq(false));
    expectLastCall().anyTimes();

    aValues = new LinkedHashMap<String, BigInt>();
    builder.setValueOfAttribute(isA(String.class), isA(BigInt.class), EasyMock.anyObject(ResidueClass.class));
    expectLastCall().andAnswer(new IAnswer<Object>() {
      @Override
      public Object answer() throws Throwable {
        aValues.put((String) getCurrentArguments()[0], (BigInt) getCurrentArguments()[1]);
        return null;
      }
    }).anyTimes();

    expect(builder.isRevealedAttribute(isA(String.class))).andAnswer(new IAnswer<Boolean>() {
      @Override
      public Boolean answer() throws Throwable {
        String key = (String) getCurrentArguments()[0];
        return revealedAttributes.contains(key);
      }
    }).anyTimes();

    rValues = new LinkedHashMap<String, BigInt>();
    builder.getRValueOfAttribute(isA(String.class));
    expectLastCall().andAnswer(new IAnswer<BigInt>() {
      @Override
      public BigInt answer() throws Throwable {
        String key = (String) getCurrentArguments()[0];
        BigInt rValue = randomGeneration.generateRandomNumber(500);
        rValues.put(key, rValue);
        BigInt sValue = rValue.subtract(aValues.get(key).multiply(challenge));
        sValues.put(key, sValue);
        return rValue;
      }
    }).anyTimes();

    dValuesO = new LinkedHashMap<String, byte[]>();
    builder.addDValue(isA(String.class), isA(byte[].class), isA(byte[].class));
    expectLastCall().andAnswer(new IAnswer<Object>() {
      @Override
      public Object answer() throws Throwable {
        dValuesO.put((String) getCurrentArguments()[0], (byte[]) getCurrentArguments()[1]);
        return null;
      }
    }).anyTimes();

    dValuesGE = new LinkedHashMap<String, GroupElement<?, ?, ?>>();
    builder.addDValue(isA(String.class), isA(GroupElement.class));
    expectLastCall().andAnswer(new IAnswer<Object>() {
      @Override
      public Object answer() throws Throwable {
        dValuesGE.put((String) getCurrentArguments()[0],
            (GroupElement<?, ?, ?>) getCurrentArguments()[1]);
        return null;
      }
    }).anyTimes();

    dValuesI = new LinkedHashMap<String, BigInt>();
    builder.addDValue(isA(String.class), isA(BigInt.class));
    expectLastCall().andAnswer(new IAnswer<Object>() {
      @Override
      public Object answer() throws Throwable {
        dValuesI.put((String) getCurrentArguments()[0], (BigInt) getCurrentArguments()[1]);
        return null;
      }
    }).anyTimes();

    builder.setHashContributionOfBuildingBlock(isA(byte[].class));
    expectLastCall().andAnswer(new IAnswer<Object>() {
      @Override
      public Object answer() throws Throwable {
        hashContribution = (byte[]) getCurrentArguments()[0];
        return null;
      }
    }).once();

    expect(builder.getChallenge()).andReturn(challenge);

    sValues = new LinkedHashMap<String, BigInt>();
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


  @SuppressWarnings("unchecked")
  private ZkVerifyStateAll programZkVerifier(List<BigInt> attributes) throws ProofException {
    ZkVerifyStateAll verifier = createMock(ZkVerifyStateAll.class);

    verifier.registerAttribute(isA(String.class), eq(false));
    expectLastCall().anyTimes();
    
    verifier.setResidueClass(isA(String.class), EasyMock.anyObject(ResidueClass.class));
    expectLastCall().anyTimes();

    expect(verifier.isRevealedAttribute(isA(String.class))).andAnswer(new IAnswer<Boolean>() {
      @Override
      public Boolean answer() throws Throwable {
        String key = (String) getCurrentArguments()[0];
        return revealedAttributes.contains(key);
      }
    }).anyTimes();

    for (Entry<String, BigInt> entry : aValues.entrySet()) {
      String label = entry.getKey();
      BigInt value = entry.getValue();
      if (revealedAttributes.contains(label)) {
        expect(verifier.getValueOfRevealedAttribute(eq(label))).andReturn(value);
      }
    }

    for (Entry<String, GroupElement<?, ?, ?>> entry : dValuesGE.entrySet()) {
      String label = entry.getKey();
      KnownOrderGroupElement dValue = (KnownOrderGroupElement)entry.getValue();
      expect(verifier.getDValueAsGroupElement(eq(label), isA(KnownOrderGroup.class))).andReturn(dValue);
    }

    for (Entry<String, BigInt> entry : dValuesI.entrySet()) {
      String label = entry.getKey();
      BigInt dValue = entry.getValue();
      expect(verifier.getDValueAsInteger(eq(label))).andReturn(dValue);
    }

    for (Entry<String, byte[]> entry : dValuesO.entrySet()) {
      String label = entry.getKey();
      byte[] dValue = entry.getValue();
      expect(verifier.getDValueAsObject(eq(label))).andReturn(dValue);
    }

    for (Entry<String, BigInt> entry : sValues.entrySet()) {
      String label = entry.getKey();
      BigInt sValue = entry.getValue();
      expect(verifier.getSValueAsInteger(eq(label))).andReturn(sValue);
    }

    expect(verifier.getChallenge()).andReturn(challenge);

    verifier.checkHashContributionOfBuildingBlock(aryEq(hashContribution));

    replay(verifier);
    return verifier;
  }
}
