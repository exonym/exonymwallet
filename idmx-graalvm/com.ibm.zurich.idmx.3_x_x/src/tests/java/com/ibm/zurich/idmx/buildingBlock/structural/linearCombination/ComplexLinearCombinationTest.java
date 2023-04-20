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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ibm.zurich.idmx.buildingBlock.factory.BuildingBlockFactory;
import com.ibm.zurich.idmx.dagger.AbcComponent;
import com.ibm.zurich.idmx.tests.TestInitialisation;
import org.junit.Before;
import org.junit.Test;

import com.ibm.zurich.idmx.buildingBlock.structural.attributeSource.AttributeSourceBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.reveal.RevealAttributeBuildingBlock;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.ProofException;
import com.ibm.zurich.idmx.exception.SerializationException;
import com.ibm.zurich.idmx.interfaces.buildingBlock.structural.linearCombination.LinearCombination;
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

public class ComplexLinearCombinationTest {

  private AttributeSourceBuildingBlock source;
  private LinearCombinationLightBuildingBlock lc;
  private RevealAttributeBuildingBlock rev;
  private RandomGeneration random;
  private BigIntFactory bif;
  private ZkDirector director;
  private SystemParameters sp;
  private static final String USERNAME = "user";

  @Before
  public void setUp() throws SerializationException {
    try {
      AbcComponent inj = TestInitialisation.INJECTOR;
      BuildingBlockFactory bbf = inj.provideBuildingBlockFactory();
      director = inj.providesZkDirector();
      source = bbf.getBuildingBlockByClass(AttributeSourceBuildingBlock.class);
      rev =bbf.getBuildingBlockByClass(RevealAttributeBuildingBlock.class);
      lc = bbf.getBuildingBlockByClass(LinearCombinationLightBuildingBlock.class);
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
//    lc = injector.getInstance(LinearCombinationLightBuildingBlock.class);
//    random = injector.getInstance(RandomGeneration.class);
//    bif = injector.getInstance(BigIntFactory.class);
//    director = injector.getInstance(ZkDirector.class);
//    sp = TestUtils.getResource("sp_default.xml", SystemParameters.class, this);
  }

  @Test
  public void testManyEquations() throws Exception {
    List<LinearCombination> combinations = new ArrayList<LinearCombination>();
    List<String> revealed = new ArrayList<String>();
    Map<String, BigInt> values = new HashMap<String, BigInt>();

    // F = A + C + D
    {
      List<Term> terms = new ArrayList<Term>();
      terms.add(new Term("A", random.generateRandomNumber(50)));
      terms.add(new Term("C", random.generateRandomNumber(50)));
      terms.add(new Term("D", random.generateRandomNumber(50)));
      combinations.add(new LinearCombination("F", random.generateRandomNumber(50), terms));
    }
    // C = A + B
    {
      List<Term> terms = new ArrayList<Term>();
      terms.add(new Term("A", random.generateRandomNumber(50)));
      terms.add(new Term("B", random.generateRandomNumber(50)));
      combinations.add(new LinearCombination("C", random.generateRandomNumber(50), terms));
    }
    // E = F + A
    {
      List<Term> terms = new ArrayList<Term>();
      terms.add(new Term("F", random.generateRandomNumber(50)));
      terms.add(new Term("A", random.generateRandomNumber(50)));
      combinations.add(new LinearCombination("E", random.generateRandomNumber(50), terms));
    }
    // X = E + F
    {
      List<Term> terms = new ArrayList<Term>();
      terms.add(new Term("E", random.generateRandomNumber(50)));
      terms.add(new Term("F", random.generateRandomNumber(50)));
      combinations.add(new LinearCombination("X", random.generateRandomNumber(50), terms));
    }
    
    values.put("A", random.generateRandomNumber(50));
    values.put("B", random.generateRandomNumber(50));
    values.put("D", random.generateRandomNumber(50));
    
    revealed.add("A");
    revealed.add("B");
    revealed.add("C");

    runTest(combinations, revealed, values);
  }
  
  @Test(expected=ProofException.class)
  public void testMultipleLhs() throws Exception {
    List<LinearCombination> combinations = new ArrayList<LinearCombination>();
    List<String> revealed = new ArrayList<String>();
    Map<String, BigInt> values = new HashMap<String, BigInt>();

    // 42 = 10 + 4 * 8
    // A = 10 + 4 * B
    {
      List<Term> terms = new ArrayList<Term>();
      terms.add(new Term("B", bif.valueOf(4)));
      combinations.add(new LinearCombination("A", bif.valueOf(10), terms));
    }

    // 42 = 13 + 3 * 8 + 1 * 5
    // A = 13 + 3 * B + 1 * C
    {
      List<Term> terms = new ArrayList<Term>();
      terms.add(new Term("B", bif.valueOf(3)));
      terms.add(new Term("C", bif.one()));
      combinations.add(new LinearCombination("A", bif.valueOf(13), terms));
    }

    values.put("B", bif.valueOf(8));
    values.put("C", bif.valueOf(5));

    // This fails, as A is computed via 2 different equations
    runTest(combinations, revealed, values);
  }
  
  @Test(expected=ProofException.class)
  public void testCircularDependency() throws Exception {
    List<LinearCombination> combinations = new ArrayList<LinearCombination>();
    List<String> revealed = new ArrayList<String>();
    Map<String, BigInt> values = new HashMap<String, BigInt>();

    // A = B + C
    {
      List<Term> terms = new ArrayList<Term>();
      terms.add(new Term("B", bif.one()));
      terms.add(new Term("C", bif.one()));
      combinations.add(new LinearCombination("A", bif.zero(), terms));
    }
    
    // B = A - 2 * D
    {
      List<Term> terms = new ArrayList<Term>();
      terms.add(new Term("A", bif.one()));
      terms.add(new Term("D", bif.two().negate()));
      combinations.add(new LinearCombination("B", bif.zero(), terms));
    }

    values.put("B", bif.valueOf(20));
    values.put("C", bif.valueOf(10));
    values.put("D", bif.valueOf(5));

    // This will fail, as there is a circular dependency
    runTest(combinations, revealed, values);
  }

  private void runTest(List<LinearCombination> combinations, List<String> revealed,
      Map<String, BigInt> values) throws ConfigurationException, ProofException,
      SerializationException {
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

    int counter = 0;
    for(LinearCombination combination: combinations) {
      counter++;
      String nameOfModule = "lc-" + counter + "-" + combination.lhsAttribute;
      ZkModuleProver zkp_lc = lc.getZkModuleProver(nameOfModule, combination.lhsAttribute, combination.constant, combination.terms);
      zkp_l.add(zkp_lc);
      ZkModuleVerifier zkv_lc = lc.getZkModuleVerifier(nameOfModule, combination.lhsAttribute, combination.constant, combination.terms);
      zkv_l.add(zkv_lc);
    }

    ZkProof proof = director.buildProof(USERNAME, zkp_l, sp);

    String proofXml = JaxbHelperClass.serialize(new ObjectFactory().createZkProof(proof));
    System.out.println(proofXml);

    boolean result = director.verifyProof(proof, zkv_l, sp);
    assertTrue(result);
  }

}
