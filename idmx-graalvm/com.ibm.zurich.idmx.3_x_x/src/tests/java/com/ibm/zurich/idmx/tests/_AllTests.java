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
package com.ibm.zurich.idmx.tests;

import java.io.File;
import java.net.URI;

import com.ibm.zurich.idmx.buildingBlock.signature.uprove._UProveTestSuite;
import com.ibm.zurich.idmx.proofEngine._ProofEngineTestSuite;
import com.ibm.zurich.idmx.uProveCompatibility._UProveCompatibilityTestSuite;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.ibm.zurich.idmx.abc4trust._Abc4TrustTestSuite;
import com.ibm.zurich.idmx.buildingBlock.helper.damgardFujisaki._DamgardFujisakiTestSuite;
import com.ibm.zurich.idmx.buildingBlock.helper.modNSquare._ModNSquareTestSuite;
import com.ibm.zurich.idmx.buildingBlock.helper.pedersen._PedersenTestSuite;
import com.ibm.zurich.idmx.buildingBlock.inequality.fourSq._DecompositionTestSuite;
import com.ibm.zurich.idmx.buildingBlock.inspector.cs._CsInspectorTestSuite;
import com.ibm.zurich.idmx.buildingBlock.rangeProof.fourSq._FourSquareRangeProofSuite;
import com.ibm.zurich.idmx.buildingBlock.revocation.TestRevocation;
import com.ibm.zurich.idmx.buildingBlock.signature.cl._ClTestSuite;
//import com.ibm.zurich.idmx.buildingBlock.signature.uprove._UProveTestSuite;
import com.ibm.zurich.idmx.buildingBlock.structural.linearCombination._LinearCombinationTestSuite;
import com.ibm.zurich.idmx.buildingBlock.structural.pseudonym._PseudonymTestSuite;
import com.ibm.zurich.idmx.orchestration._OrchestrationTestSuite;
//import com.ibm.zurich.idmx.proofEngine._ProofEngineTestSuite;
import com.ibm.zurich.idmx.tests.setup.TestIssuerKeyPair;
import com.ibm.zurich.idmx.tests.setup.TestIssuerKeyPairDeserialization;
import com.ibm.zurich.idmx.tests.setup.TestRevocationAuthortiyKeyPair;
import com.ibm.zurich.idmx.tests.setup.TestSystemParameters;
import com.ibm.zurich.idmx.tests.setup.TestSystemParametersDeserialization;
//import com.ibm.zurich.idmx.uProveCompatibility._UProveCompatibilityTestSuite;
import com.ibm.zurich.idmx.util.DisjointSetTest;
import com.ibm.zurich.idmx.util.ModuleSorterTest;

@RunWith(Suite.class)
@SuiteClasses({
    // TODO this test should be setup to generate all required files
    // _GenerateConfigurationFiles.class, //

    // Independent functionality
    DisjointSetTest.class, //
    ModuleSorterTest.class, //
    _DecompositionTestSuite.class,

    // Parameter generation
    TestSystemParameters.class, //
    TestSystemParametersDeserialization.class, //
    TestIssuerKeyPair.class, //
    TestIssuerKeyPairDeserialization.class, //
    TestRevocationAuthortiyKeyPair.class, //
    TestRevocation.class, //

    // Independent modules
    _PedersenTestSuite.class, //
    _DamgardFujisakiTestSuite.class, //
    _ModNSquareTestSuite.class, //
    _FourSquareRangeProofSuite.class, //
    _PseudonymTestSuite.class, //
    _LinearCombinationTestSuite.class, //
    _CsInspectorTestSuite.class, //

    // Proof engine and dependent
    _ProofEngineTestSuite.class, //
    _ClTestSuite.class, //
    _UProveTestSuite.class, //

    // Orchestration
    _OrchestrationTestSuite.class, //
    _Abc4TrustTestSuite.class, //
    _UProveCompatibilityTestSuite.class //
})
public class _AllTests {
  @BeforeClass
  public static void cleanUpTemporaryFiles() {
    final URI BASE_LOCATION = new File(System.getProperty("user.dir")).toURI();
    final URI SEND = BASE_LOCATION.resolve("files/");
    TestUtils.deleteFilesInFolder(new File(SEND), null);
  }


  public static void overwriteFiles() {

  }
}
