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
package com.ibm.zurich.idmx.buildingBlock.rangeProof.fourSq;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
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

import com.ibm.zurich.idmix.abc4trust.cryptoEngine.Abc4TrustCryptoEngineVerifierImpl;
import com.ibm.zurich.idmx.dagger.CryptoTestModule;
import com.ibm.zurich.idmx.annotations.Nullable;
import com.ibm.zurich.idmx.buildingBlock.rangeProof.RangeProofBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.attributeSource.AttributeSourceBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.reveal.RevealAttributeBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.systemParameters.EcryptSystemParametersWrapper;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.ProofException;
import com.ibm.zurich.idmx.exception.SerializationException;
import com.ibm.zurich.idmx.interfaces.proofEngine.ZkDirector;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProver;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleVerifier;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ResidueClass;
import com.ibm.zurich.idmx.tests.TestUtils;

import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.VerifierParameters;
import eu.abc4trust.xml.ZkProof;

public class FourSquareTest {

  private AttributeSourceBuildingBlock source;
  private RangeProofBuildingBlock rp;
  private RevealAttributeBuildingBlock rev;
  private BigIntFactory bif;
  private ZkDirector director;
  private SystemParameters sp;
  private VerifierParameters vp;
  private static final String USERNAME = "user";

  @Before
  public void setup() throws Exception {

    AbcComponent i = TestInitialisation.INJECTOR;

    BuildingBlockFactory bbf = i.provideBuildingBlockFactory();
    bif = i.provideBigIntFactory();
    director = i.providesZkDirector();
    rev = bbf.getBuildingBlockByClass(RevealAttributeBuildingBlock.class);
    rp = bbf.getBuildingBlockByClass(FourSquaresRangeProofBuildingBlock.class);
    source = bbf.getBuildingBlockByClass(AttributeSourceBuildingBlock.class);
    sp = TestUtils.getResource("sp.xml", SystemParameters.class, this);
    IssuerParameters ip = TestUtils.getResource("ip-cl.xml", IssuerParameters.class, this);

    KeyManager km = i.providesKeyManager();
    km.storeSystemParameters(sp);
    km.storeIssuerParameters(ip.getParametersUID(), ip);

    Abc4TrustCryptoEngineVerifierImpl cev = (Abc4TrustCryptoEngineVerifierImpl) i.providesCryptoEngineVerifierAbc();
    vp = cev.createVerifierParameters(sp);

//    Injector injector = Guice.createInjector(new CryptoTestModule());
//    source = injector.getInstance(AttributeSourceBuildingBlock.class);
//    rev = injector.getInstance(RevealAttributeBuildingBlock.class);
//    rp = injector.getInstance(FourSquaresRangeProofBuildingBlock.class);
//    bif = injector.getInstance(BigIntFactory.class);
//    director = injector.getInstance(ZkDirector.class);
  }

  @Test
  public void testAll() throws Exception {
    boolean STRICT = true;
    boolean OREQUAL = false;

    boolean OK = true;
    boolean FAIL = false;

    for (boolean revealLhs : new boolean[] {true, false}) {
      for (boolean revealRhs : new boolean[] {true, false}) {
        for (ResidueClass lhsRc : new ResidueClass[] {null, ResidueClass.INTEGER_IN_RANGE}) {
          for (ResidueClass rhsRc : new ResidueClass[] {null, ResidueClass.INTEGER_IN_RANGE}) {
            singleRangeProof(42, 1337, STRICT, revealLhs, revealRhs, lhsRc, rhsRc, OK);
            singleRangeProof(42, 1337, OREQUAL, revealLhs, revealRhs, lhsRc, rhsRc, OK);

            singleRangeProof(1337, 42, STRICT, revealLhs, revealRhs, lhsRc, rhsRc, FAIL);
            singleRangeProof(1337, 42, OREQUAL, revealLhs, revealRhs, lhsRc, rhsRc, FAIL);

            // Test difference between < and <=
            singleRangeProof(42, 42, STRICT, revealLhs, revealRhs, lhsRc, rhsRc, FAIL);
            singleRangeProof(42, 42, OREQUAL, revealLhs, revealRhs, lhsRc, rhsRc, OK);
            singleRangeProof(42, 43, STRICT, revealLhs, revealRhs, lhsRc, rhsRc, OK);
            singleRangeProof(42, 43, OREQUAL, revealLhs, revealRhs, lhsRc, rhsRc, OK);
            singleRangeProof(43, 42, STRICT, revealLhs, revealRhs, lhsRc, rhsRc, FAIL);
            singleRangeProof(43, 42, OREQUAL, revealLhs, revealRhs, lhsRc, rhsRc, FAIL);

            // Out of range
            singleRangeProof(0, 1, OREQUAL, revealLhs, revealRhs, lhsRc, rhsRc, OK);
            singleRangeProof(-1, 0, OREQUAL, revealLhs, revealRhs, lhsRc, rhsRc, FAIL);
            
            // TODO(enr): Currently max attribute value is (subgroupOrder - 1), not  2^maxAttributeSize
            // BigInteger maxPlusOne = BigInteger.valueOf(2).pow(256);
            // BigInteger max = maxPlusOne.subtract(BigInteger.ONE);
            BigInteger max = new EcryptSystemParametersWrapper(sp).getMaximumAttributeValue(0, bif).getValue();
            BigInteger maxPlusOne = max.add(BigInteger.ONE);
            singleRangeProof(BigInteger.TEN, max, OREQUAL, revealLhs, revealRhs, lhsRc, rhsRc, OK);
            singleRangeProof(BigInteger.ZERO, max, OREQUAL, revealLhs, revealRhs, lhsRc, rhsRc, OK);
            singleRangeProof(BigInteger.TEN, maxPlusOne, OREQUAL, revealLhs, revealRhs, lhsRc, rhsRc, FAIL);
          }
        }
      }
    }
  }

  @Test @Ignore
  public void speedTest() throws Exception {
    long time = System.nanoTime();
    final int iterations = 100;

    for (int i = 0; i < iterations; ++i) {
      singleRangeProof(18000000, 25000000, true, true, false, null, null, true);
      System.out.print(".");
    }
    System.out.println();

    System.out.println((System.nanoTime() - time) / (1e9 * iterations));
  }

  @Test @Ignore
  public void speedTestIdemix() throws Exception {
    long time = System.nanoTime();
    final int iterations = 100;

    for (int i = 0; i < iterations; ++i) {
      singleRangeProof(18000000, 25000000, true, true, false, null, ResidueClass.INTEGER_IN_RANGE,
          true);
      System.out.print(".");
    }
    System.out.println();

    System.out.println((System.nanoTime() - time) / (1e9 * iterations));
  }


  public void singleRangeProof(long lhsValue, long rhsValue, boolean strict, boolean revealLhs,
      boolean revealRhs, ResidueClass lhsRc, ResidueClass rhsRc, boolean ok) throws Exception {
    singleRangeProof(BigInteger.valueOf(lhsValue), BigInteger.valueOf(rhsValue), strict, revealLhs,
        revealRhs, lhsRc, rhsRc, ok);
  }

  public void singleRangeProof(BigInteger lhsValue, BigInteger rhsValue, boolean strict,
      boolean revealLhs, boolean revealRhs, ResidueClass lhsRc, ResidueClass rhsRc, boolean ok)
      throws Exception {
    
    // debug(lhsValue, rhsValue, strict, revealLhs, revealRhs, lhsRc, rhsRc, ok);
     
    ProverModule.hiddenRangeChecks.set(0);
    ProverModule.skippedBecauseInRange.set(0);
    ProverModule.skippedBecauseRevealed.set(0);
    VerifierModule.hiddenRangeChecks.set(0);
    VerifierModule.skippedBecauseInRange.set(0);
    VerifierModule.skippedBecauseRevealed.set(0);

    final String lhs = "L";
    final String rhs = "R";
    List<Inequality> inequalities = Collections.singletonList(new Inequality(lhs, rhs, strict));
    List<String> revealed = new ArrayList<>();
    if (revealLhs) {
      revealed.add(lhs);
    }
    if (revealRhs) {
      revealed.add(rhs);
    }
    Map<String, BigInt> values = new HashMap<>();
    values.put(lhs, bif.valueOf(lhsValue));
    values.put(rhs, bif.valueOf(rhsValue));
    Map<String, ResidueClass> rc = new HashMap<>();
    rc.put(lhs, lhsRc);
    rc.put(rhs, rhsRc);

    if (ok) {
      runTest(inequalities, revealed, values, rc);

      int expectedHidden = 3;
      int expectedRevealed = 0;
      int expectedInRange = 0;
      if (revealLhs) {
        expectedHidden--;
        expectedRevealed++;
      } else if (lhsRc == ResidueClass.INTEGER_IN_RANGE) {
        expectedHidden--;
        expectedInRange++;
      }
      if (revealRhs) {
        expectedHidden--;
        expectedRevealed++;
      } else if (rhsRc == ResidueClass.INTEGER_IN_RANGE) {
        expectedHidden--;
        expectedInRange++;
      }
      if (revealLhs && revealRhs) {
        expectedHidden--;
        expectedRevealed++;
      }
      assertEquals(expectedHidden, ProverModule.hiddenRangeChecks.get());
      assertEquals(expectedHidden, VerifierModule.hiddenRangeChecks.get());
      assertEquals(expectedRevealed, ProverModule.skippedBecauseRevealed.get());
      assertEquals(expectedRevealed, VerifierModule.skippedBecauseRevealed.get());
      assertEquals(expectedInRange, ProverModule.skippedBecauseInRange.get());
      assertEquals(expectedInRange, VerifierModule.skippedBecauseInRange.get());

    } else {
      try {
        runTest(inequalities, revealed, values, rc);
        fail("The proof should not have succeeded");
      } catch (RuntimeException re) {
        // expected
        assertTrue(re.getMessage().contains("delta is negative"));
      }
    }
  }

  /*
  private void debug(BigInteger lhsValue, BigInteger rhsValue, boolean strict, boolean revealLhs,
      boolean revealRhs, ResidueClass lhsRc, ResidueClass rhsRc, boolean ok) {
    System.out.println();
    System.out.println();
    System.out.print("Proof: " + lhsValue + (strict ? " < " : " <= ") + rhsValue);
    if (revealLhs) {
      System.out.print("  Reveal LHS");
    }
    if (revealRhs) {
      System.out.print("  Reveal RHS");
    }
    if (!ok) {
      System.out.print("  SHOULD FAIL");
    }
    System.out.print("   " + lhsRc);
    System.out.print("   " + rhsRc);
    System.out.println();
  }
  */

  private void runTest(List<Inequality> inequalities, List<String> revealed,
      Map<String, BigInt> values, Map<String, ResidueClass> residueClasses)
      throws ConfigurationException, ProofException, SerializationException {
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
      @Nullable
      ResidueClass rc = residueClasses.get(attribute);
      ZkModuleProver zkp_r = source.getZkModuleProver(attribute, value, rc);
      zkp_l.add(zkp_r);
      ZkModuleVerifier zkv_r = source.getZkModuleVerifier(attribute, null, rc);
      zkv_l.add(zkv_r);
    }

    int counter = 0;
    for (Inequality ineq : inequalities) {
      counter++;
      ZkModuleProver zkp_lc =
          rp.getZkModuleProver(sp, vp, ineq.lhsAttribute, ineq.rhsAttribute, ineq.strict, counter);
      zkp_l.add(zkp_lc);
      ZkModuleVerifier zkv_lc =
          rp.getZkModuleVerifier(sp, vp, ineq.lhsAttribute, ineq.rhsAttribute, ineq.strict, counter);
      zkv_l.add(zkv_lc);
    }

    ZkProof proof = director.buildProof(USERNAME, zkp_l, sp);

    // String proofXml = JaxbHelperClass.serialize(new ObjectFactory().createZkProof(proof));
    // System.out.println(proofXml);

    boolean result = director.verifyProof(proof, zkv_l, sp);
    assertTrue(result);
  }
}


class Inequality {
  public final String lhsAttribute;
  public final String rhsAttribute;
  public final boolean strict;

  public Inequality(String lhsAttribute, String rhsAttribute, boolean strict) {
    super();
    this.lhsAttribute = lhsAttribute;
    this.rhsAttribute = rhsAttribute;
    this.strict = strict;
  }
}
