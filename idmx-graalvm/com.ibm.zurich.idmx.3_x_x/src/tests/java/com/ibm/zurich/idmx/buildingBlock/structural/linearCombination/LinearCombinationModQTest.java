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
package com.ibm.zurich.idmx.buildingBlock.structural.linearCombination;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ibm.zurich.idmx.buildingBlock.factory.BuildingBlockFactory;
import com.ibm.zurich.idmx.dagger.AbcComponent;
import com.ibm.zurich.idmx.tests.TestInitialisation;
import org.junit.Before;
import org.junit.Test;

import com.ibm.zurich.idmx.buildingBlock.structural.attributeSource.AttributeSourceBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.linearCombinationModQ.LinearCombinationModQBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.reveal.RevealAttributeBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.systemParameters.EcryptSystemParametersWrapper;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.ProofException;
import com.ibm.zurich.idmx.exception.SerializationException;
import com.ibm.zurich.idmx.interfaces.buildingBlock.structural.linearCombination.Term;
import com.ibm.zurich.idmx.interfaces.proofEngine.ZkDirector;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.RandomGeneration;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProver;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleVerifier;
import com.ibm.zurich.idmx.jaxb.JaxbHelperClass;
import com.ibm.zurich.idmx.tests.TestUtils;

import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.ZkProof;

public class LinearCombinationModQTest {

  private AttributeSourceBuildingBlock source;
  private LinearCombinationModQBuildingBlock lc;
  private RevealAttributeBuildingBlock rev;
  private RandomGeneration random;
  private BigIntFactory bif;
  private ZkDirector director;
  private SystemParameters sp;
  private static final String LHS_ATTRIBUTE = "lhs";
  private static final String USERNAME = "user";

  @Before
  public void setUp() throws SerializationException {
    try {
      AbcComponent inj = TestInitialisation.INJECTOR;
      BuildingBlockFactory bbf = inj.provideBuildingBlockFactory();
      director = inj.providesZkDirector();
      source = bbf.getBuildingBlockByClass(AttributeSourceBuildingBlock.class);
      rev =bbf.getBuildingBlockByClass(RevealAttributeBuildingBlock.class);
      lc = bbf.getBuildingBlockByClass(LinearCombinationModQBuildingBlock.class);
      random = inj.provideRandomGeneration();
      director = inj.providesZkDirector();
      bif = inj.provideBigIntFactory();
      sp = TestUtils.getResource("sp_default.xml", SystemParameters.class, this);
    } catch (ConfigurationException e) {
      throw new SerializationException(e);
    }

//    Injector injector = Guice.createInjector(new CryptoTestModule());
//    source = injector.getInstance(AttributeSourceBuildingBlock.class);
//    rev = injector.getInstance(RevealAttributeBuildingBlock.class);
//    lc = injector.getInstance(LinearCombinationModQBuildingBlock.class);
//    random = injector.getInstance(RandomGeneration.class);
//    director = injector.getInstance(ZkDirector.class);
//    bif = injector.getInstance(BigIntFactory.class);
//    sp = TestUtils.getResource("sp_default.xml", SystemParameters.class, this);
  }

  @Test
  public void testEqualToRandom() throws Exception {
    BigInt constant = random.generateRandomNumber(100);
    List<Term> terms = Collections.emptyList();
    List<String> revealed = Collections.emptyList();
    Map<String, BigInt> values = Collections.emptyMap();
    runTest(constant, terms, revealed, values);
  }

  @Test
  public void testEqualToRandomReavel() throws Exception {
    BigInt constant = random.generateRandomNumber(100);
    List<Term> terms = Collections.emptyList();
    List<String> revealed = Collections.singletonList(LHS_ATTRIBUTE);
    Map<String, BigInt> values = Collections.emptyMap();
    runTest(constant, terms, revealed, values);
  }

  @Test
  public void testOneTerm() throws Exception {
    BigInt constant = random.generateRandomNumber(100);
    List<Term> terms = Collections.singletonList(new Term("rhs", random.generateRandomNumber(100)));
    List<String> revealed = Collections.emptyList();
    Map<String, BigInt> values = Collections.singletonMap("rhs", random.generateRandomNumber(100));
    runTest(constant, terms, revealed, values);
  }
  
  @Test
  public void testTwoTermManual() throws Exception {
    // 42 = -2 + (5 * 10) + (-2 * 3)
    BigInt constant = bif.valueOf(-2);
    
    List<Term> terms = new ArrayList<Term>();
    terms.add(new Term("A", bif.valueOf(5)));
    terms.add(new Term("B", bif.valueOf(-2)));
    
    List<String> revealed = Collections.emptyList();
    
    Map<String, BigInt> values = new HashMap<String, BigInt>();
    values.put("A", bif.valueOf(10));
    values.put("B", bif.valueOf(3));
    values.put(LHS_ATTRIBUTE, bif.valueOf(42));
    
    runTest(constant, terms, revealed, values);
  }
  
  @Test
  public void testNegativeResult() throws Exception {
    // -42 = -2 + (-4 * 10)
    BigInt constant = bif.valueOf(-2);
    
    List<Term> terms = new ArrayList<Term>();
    terms.add(new Term("A", bif.valueOf(-4)));
    
    List<String> revealed = Collections.emptyList();
    
    BigInt q = new EcryptSystemParametersWrapper(sp).getDHSubgroupOrder();
    Map<String, BigInt> values = new HashMap<String, BigInt>();
    values.put("A", bif.valueOf(10));
    values.put(LHS_ATTRIBUTE, bif.valueOf(-42).mod(q));
    
    runTest(constant, terms, revealed, values);
  }
  
  @Test
  public void testNegativeResultReveal() throws Exception {
    // -42 = -2 + (-4 * 10)
    BigInt constant = bif.valueOf(-2);
    
    List<Term> terms = new ArrayList<Term>();
    terms.add(new Term("A", bif.valueOf(-4)));
    
    List<String> revealed = new ArrayList<String>();
    revealed.add("A");
    revealed.add(LHS_ATTRIBUTE);
    
    BigInt q = new EcryptSystemParametersWrapper(sp).getDHSubgroupOrder();
    Map<String, BigInt> values = new HashMap<String, BigInt>();
    values.put("A", bif.valueOf(10));
    values.put(LHS_ATTRIBUTE, bif.valueOf(-42).mod(q));
    
    runTest(constant, terms, revealed, values);
  }

  @Test
  public void testOneTermRevealRhs() throws Exception {
    BigInt constant = random.generateRandomNumber(100);
    List<Term> terms = Collections.singletonList(new Term("rhs", random.generateRandomNumber(100)));
    List<String> revealed = Collections.singletonList("rhs");
    Map<String, BigInt> values = Collections.singletonMap("rhs", random.generateRandomNumber(100));
    runTest(constant, terms, revealed, values);
  }

  @Test
  public void testOneTermRevealOnlyLhs() throws Exception {
    // Unlike the "Light" version, this actually works
    BigInt constant = random.generateRandomNumber(100);
    List<Term> terms = Collections.singletonList(new Term("rhs", random.generateRandomNumber(100)));
    List<String> revealed = Collections.singletonList(LHS_ATTRIBUTE);
    Map<String, BigInt> values = Collections.singletonMap("rhs", random.generateRandomNumber(100));
    runTest(constant, terms, revealed, values);
  }
  
  @Test
  public void testCircularDependency() throws Exception {
    // Equation is lhs = 0 + (1 * lhs)
    // Unlike the "Light" version, this actually works
    BigInt constant = bif.zero();
    List<Term> terms = Collections.singletonList(new Term(LHS_ATTRIBUTE, bif.one()));
    List<String> revealed = Collections.singletonList(LHS_ATTRIBUTE);
    Map<String, BigInt> values = Collections.singletonMap(LHS_ATTRIBUTE, bif.zero());
    runTest(constant, terms, revealed, values);
  }

  @Test
  public void testOneTermRevealAll() throws Exception {
    BigInt constant = random.generateRandomNumber(100);
    List<Term> terms = Collections.singletonList(new Term("rhs", random.generateRandomNumber(100)));
    List<String> revealed = Arrays.asList(LHS_ATTRIBUTE, "rhs");
    Map<String, BigInt> values = Collections.singletonMap("rhs", random.generateRandomNumber(100));
    runTest(constant, terms, revealed, values);
  }

  @Test
  public void testTwoTerms() throws Exception {
    BigInt constant = random.generateRandomNumber(100);

    List<Term> terms = new ArrayList<Term>();
    terms.add(new Term("A", random.generateRandomNumber(100)));
    terms.add(new Term("B", random.generateRandomNumber(100)));

    List<String> revealed = Collections.emptyList();

    Map<String, BigInt> values = new HashMap<String, BigInt>();
    values.put("A", random.generateRandomNumber(100));
    values.put("B", random.generateRandomNumber(100));

    runTest(constant, terms, revealed, values);
  }

  @Test
  public void testTwoTermsNegative() throws Exception {
    BigInt constant = random.generateRandomNumber(100);

    List<Term> terms = new ArrayList<Term>();
    terms.add(new Term("A", random.generateRandomNumber(100)));
    terms.add(new Term("B", random.generateRandomNumber(50).negate()));

    List<String> revealed = Collections.emptyList();

    Map<String, BigInt> values = new HashMap<String, BigInt>();
    values.put("A", random.generateRandomNumber(100));
    values.put("B", random.generateRandomNumber(100));

    runTest(constant, terms, revealed, values);
  }

  @Test
  public void testTwoTermsRevealOne() throws Exception {
    BigInt constant = random.generateRandomNumber(100);

    List<Term> terms = new ArrayList<Term>();
    terms.add(new Term("A", random.generateRandomNumber(100)));
    terms.add(new Term("B", random.generateRandomNumber(100)));

    List<String> revealed = new ArrayList<String>();
    revealed.add("A");

    Map<String, BigInt> values = new HashMap<String, BigInt>();
    values.put("A", random.generateRandomNumber(100));
    values.put("B", random.generateRandomNumber(100));

    runTest(constant, terms, revealed, values);
  }

  @Test
  public void testTwoTermsRevealBoth() throws Exception {
    BigInt constant = random.generateRandomNumber(100);

    List<Term> terms = new ArrayList<Term>();
    terms.add(new Term("A", random.generateRandomNumber(100)));
    terms.add(new Term("B", random.generateRandomNumber(100)));

    List<String> revealed = new ArrayList<String>();
    revealed.add("A");
    revealed.add("B");

    Map<String, BigInt> values = new HashMap<String, BigInt>();
    values.put("A", random.generateRandomNumber(100));
    values.put("B", random.generateRandomNumber(100));

    runTest(constant, terms, revealed, values);
  }

  @Test
  public void testTwoTermsRevealBothAndLhs() throws Exception {
    BigInt constant = random.generateRandomNumber(100);

    List<Term> terms = new ArrayList<Term>();
    terms.add(new Term("A", random.generateRandomNumber(100)));
    terms.add(new Term("B", random.generateRandomNumber(100)));

    List<String> revealed = new ArrayList<String>();
    revealed.add("A");
    revealed.add("B");
    revealed.add(LHS_ATTRIBUTE);

    Map<String, BigInt> values = new HashMap<String, BigInt>();
    values.put("A", random.generateRandomNumber(100));
    values.put("B", random.generateRandomNumber(100));

    runTest(constant, terms, revealed, values);
  }

  @Test
  public void testManyTermsRevealNone() throws Exception {
    BigInt constant = random.generateRandomNumber(100);

    List<Term> terms = new ArrayList<Term>();
    List<String> revealed = new ArrayList<String>();
    Map<String, BigInt> values = new HashMap<String, BigInt>();

    for (char c = 'A'; c <= 'Z'; ++c) {
      String name = "" + c;
      terms.add(new Term(name, random.generateRandomNumber(100)));
      values.put(name, random.generateRandomNumber(100));
    }

    runTest(constant, terms, revealed, values);
  }

  @Test
  public void testManyTermsRevealSome() throws Exception {
    BigInt constant = random.generateRandomNumber(100);

    List<Term> terms = new ArrayList<Term>();
    List<String> revealed = new ArrayList<String>();
    Map<String, BigInt> values = new HashMap<String, BigInt>();

    for (char c = 'A'; c <= 'Z'; ++c) {
      String name = "" + c;
      terms.add(new Term(name, random.generateRandomNumber(100)));
      values.put(name, random.generateRandomNumber(100));
      if (random.generateRandomNumber(1).intValue() == 0) {
        revealed.add(name);
      }
    }

    runTest(constant, terms, revealed, values);
  }

  @Test
  public void testManyTermsRevealAllRhs() throws Exception {
    BigInt constant = random.generateRandomNumber(100);

    List<Term> terms = new ArrayList<Term>();
    List<String> revealed = new ArrayList<String>();
    Map<String, BigInt> values = new HashMap<String, BigInt>();

    for (char c = 'A'; c <= 'Z'; ++c) {
      String name = "" + c;
      terms.add(new Term(name, random.generateRandomNumber(100)));
      values.put(name, random.generateRandomNumber(100));
      revealed.add(name);
    }

    runTest(constant, terms, revealed, values);
  }

  @Test
  public void testManyTermsRevealAll() throws Exception {
    BigInt constant = random.generateRandomNumber(100);

    List<Term> terms = new ArrayList<Term>();
    List<String> revealed = new ArrayList<String>();
    Map<String, BigInt> values = new HashMap<String, BigInt>();

    for (char c = 'A'; c <= 'Z'; ++c) {
      String name = "" + c;
      terms.add(new Term(name, random.generateRandomNumber(100)));
      values.put(name, random.generateRandomNumber(100));
      revealed.add(name);
    }
    revealed.add(LHS_ATTRIBUTE);

    runTest(constant, terms, revealed, values);
  }

  private void runTest(BigInt constant, List<Term> terms, List<String> revealed,
      Map<String, BigInt> values) throws ConfigurationException, ProofException,
      SerializationException {
    final String nameOfModule = "lc";
    List<ZkModuleProver> zkp_l = new ArrayList<ZkModuleProver>();
    List<ZkModuleVerifier> zkv_l = new ArrayList<ZkModuleVerifier>();

    for (String attribute : revealed) {
      ZkModuleProver zkp_r = rev.getZkModuleProver(attribute);
      zkp_l.add(zkp_r);
      ZkModuleVerifier zkv_r = rev.getZkModuleVerifier(attribute, null);
      zkv_l.add(zkv_r);
    }

    for (String attribute : values.keySet()) {
      BigInt value = values.get(attribute);
      ZkModuleProver zkp_r = source.getZkModuleProver(attribute, value, null);
      zkp_l.add(zkp_r);
      ZkModuleVerifier zkv_r = source.getZkModuleVerifier(attribute, null, null);
      zkv_l.add(zkv_r);
    }

    ZkModuleProver zkp_lc = lc.getZkModuleProver(nameOfModule, LHS_ATTRIBUTE, constant, terms, sp);
    zkp_l.add(zkp_lc);
    ZkModuleVerifier zkv_lc = lc.getZkModuleVerifier(nameOfModule, LHS_ATTRIBUTE, constant, terms, sp);
    zkv_l.add(zkv_lc);

    ZkProof proof = director.buildProof(USERNAME, zkp_l, sp);

    String proofXml = JaxbHelperClass.serialize(new ObjectFactory().createZkProof(proof));
    System.out.println(proofXml);

    boolean result = director.verifyProof(proof, zkv_l, sp);
    assertTrue(result);
  }

}
