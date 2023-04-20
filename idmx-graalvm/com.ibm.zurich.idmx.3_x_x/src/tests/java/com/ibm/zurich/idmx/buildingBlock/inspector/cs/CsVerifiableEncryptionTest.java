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
package com.ibm.zurich.idmx.buildingBlock.inspector.cs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.ibm.zurich.idmx.buildingBlock.factory.BuildingBlockFactory;
import com.ibm.zurich.idmx.buildingBlock.signature.cl.ClSignatureBuildingBlock;
import com.ibm.zurich.idmx.dagger.AbcComponent;
import com.ibm.zurich.idmx.dagger.DaggerAbcComponent;
import com.ibm.zurich.idmx.tests.TestInitialisation;
import org.junit.Before;
import org.junit.Test;

import com.ibm.zurich.idmx.dagger.CryptoTestModule;
import com.ibm.zurich.idmx.buildingBlock.helper.BaseForRepresentation;
import com.ibm.zurich.idmx.buildingBlock.helper.representation.damgardFujisaki.DamgardFujisakiRepresentationBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.equality.AttributeEqualityBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.systemParameters.EcryptSystemParametersWrapper;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.ProofException;
import com.ibm.zurich.idmx.exception.SerializationException;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.Pair;
import com.ibm.zurich.idmx.interfaces.util.RandomGeneration;
import com.ibm.zurich.idmx.interfaces.util.group.GroupElement;
import com.ibm.zurich.idmx.interfaces.util.group.GroupFactory;
import com.ibm.zurich.idmx.interfaces.util.group.HiddenOrderGroup;
import com.ibm.zurich.idmx.interfaces.util.group.PaillierGroup;
import com.ibm.zurich.idmx.interfaces.util.group.PaillierGroupElement;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProver;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProverCommitment;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProverVerifiableEncryption;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleVerifier;
import com.ibm.zurich.idmx.jaxb.JaxbHelperClass;
import com.ibm.zurich.idmx.tests.TestUtils;

import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.xml.KeyPair;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.VerifierParameters;
import eu.abc4trust.xml.ZkProof;

public class CsVerifiableEncryptionTest extends CsInspectorTestHelper {

  private BigIntFactory bigIntFactory;
  private GroupFactory groupFactory;
  private RandomGeneration randomGeneration;
  private KeyPair keyPair;
  private AttributeEqualityBuildingBlock equalityBB;
  private DamgardFujisakiRepresentationBuildingBlock dfBB;
  private static final String USERNAME = "user";

  @Before
  public void setUp() throws SerializationException, ConfigurationException, KeyManagerException,
      IOException {
    systemParameters =
        TestUtils.getResource("../../signature/sp_default.xml", SystemParameters.class, this);
    spWrapper = new EcryptSystemParametersWrapper(systemParameters);
    initSystemParameters(systemParameters, keyManager);

    AbcComponent i = TestInitialisation.INJECTOR;
    BuildingBlockFactory bbf = i.provideBuildingBlockFactory();
    bigIntFactory = i.provideBigIntFactory();
    groupFactory = i.provideGroupFactory();
    randomGeneration = i.provideRandomGeneration();
    zkDirector = i.providesZkDirector();
    clBuildingBlock = bbf.getBuildingBlockByClass(ClSignatureBuildingBlock.class);
    dfBB = bbf.getBuildingBlockByClass(DamgardFujisakiRepresentationBuildingBlock.class);
    equalityBB = bbf.getBuildingBlockByClass(AttributeEqualityBuildingBlock.class);
    keyPair = generateCsKeyPair();
    String xmlKeyPair = JaxbHelperClass.serialize((new ObjectFactory()).createKeyPair(keyPair));
    System.out.println(xmlKeyPair);


//    Injector injector = Guice.createInjector(new CryptoTestModule());
//
//    // Objects used for the CS inspector block
//    bigIntFactory = injector.getInstance(BigIntFactory.class);
//    groupFactory = injector.getInstance(GroupFactory.class);
//    randomGeneration = injector.getInstance(RandomGeneration.class);
//    equalityBB = injector.getInstance(AttributeEqualityBuildingBlock.class);
//    dfBB = injector.getInstance(DamgardFujisakiRepresentationBuildingBlock.class);



  }

  public Pair<List<ZkModuleProver>, List<ZkModuleVerifier>> generateProverAndVerifierModules()
      throws ConfigurationException, ProofException {
    List<ZkModuleProver> modulesProver = new ArrayList<ZkModuleProver>();
    List<ZkModuleVerifier> modulesVerifier = new ArrayList<ZkModuleVerifier>();
    VerifierParameters verifierParameters = null;

    CsPublicKeyWrapper insPkWrapper = new CsPublicKeyWrapper(keyPair.getPublicKey());
    HiddenOrderGroup group =
        groupFactory.createSignedQuadraticResiduesGroup(insPkWrapper.getModulus());
    List<BaseForRepresentation> basesDF = new ArrayList<BaseForRepresentation>();
    basesDF.add(BaseForRepresentation.randomAttribute(group.valueOf(bigIntFactory.valueOf(25))));
    ZkModuleProverCommitment zkpDf =
        dfBB.getZkModuleProver(systemParameters, "TestIdentifierDF", null, basesDF, group, null,
            null, USERNAME, null);
    ZkModuleVerifier zkvDf =
        dfBB.getZkModuleVerifier(systemParameters, "TestIdentifierDF", basesDF, null,
            "TestIdentifierDF:C", group);
    modulesProver.add(zkpDf);
    modulesVerifier.add(zkvDf);

    ZkModuleProverVerifiableEncryption zkpEnc =
        csInspectorBuildingBlock.getZkModuleProverEncryption("TestIdentifier", systemParameters,
            verifierParameters, keyPair.getPublicKey(), "TestIdentifierDF:0",
            bigIntFactory.valueOf(123123).toByteArray());
    ZkModuleVerifier zkvEnc =
        csInspectorBuildingBlock.getZkModuleVerifierEncryption("TestIdentifier", systemParameters,
            verifierParameters, keyPair.getPublicKey(), "TestIdentifierDF:0",
            bigIntFactory.valueOf(123123).toByteArray());
    modulesProver.add(zkpEnc);
    modulesVerifier.add(zkvEnc);

    Pair<List<ZkModuleProver>, List<ZkModuleVerifier>> returnPair = new Pair<List<ZkModuleProver>, List<ZkModuleVerifier>>();
    returnPair.first = modulesProver;
    returnPair.second = modulesVerifier;

    return returnPair;
  }

  @Test
  public void encryptAndProve() throws ProofException, ConfigurationException,
      SerializationException {
    Pair<List<ZkModuleProver>, List<ZkModuleVerifier>> modulesPair =
        generateProverAndVerifierModules();
    List<ZkModuleProver> modulesProver = modulesPair.first;
    List<ZkModuleVerifier> modulesVerifier = modulesPair.second;
    CsPublicKeyWrapper insPkWrapper = new CsPublicKeyWrapper(keyPair.getPublicKey());

    ZkProof proof = zkDirector.buildProof(USERNAME, modulesProver, systemParameters);

    // Serialisation
    String xmlProof = JaxbHelperClass.serialize((new ObjectFactory()).createZkProof(proof));
    System.out.println(xmlProof);

    boolean result = zkDirector.verifyProof(proof, modulesVerifier, systemParameters);
    assertTrue(result);
  }

  @Test
  public void encryptAndDecrypt() throws ProofException, ConfigurationException,
      SerializationException {
    Pair<List<ZkModuleProver>, List<ZkModuleVerifier>> modulesPair =
        generateProverAndVerifierModules();
    List<ZkModuleProver> modulesProver = modulesPair.first;
    List<ZkModuleVerifier> modulesVerifier = modulesPair.second;
    CsPublicKeyWrapper insPkWrapper = new CsPublicKeyWrapper(keyPair.getPublicKey());

    ZkProof proof = zkDirector.buildProof(USERNAME, modulesProver, systemParameters);

    GroupElement[] ciphertext =
        ((ZkModuleProverVerifiableEncryption) modulesProver.get(1)).getCiphertext();

    BigInt plaintextUsedByDF =
        ((ZkModuleProverCommitment<?>) modulesProver.get(0)).recoverRandomizers().get(0);

    CsSecretKeyWrapper insSkWrapper = new CsSecretKeyWrapper(keyPair.getPrivateKey());
    PaillierGroup group2 = groupFactory.createPaillierGroup(insPkWrapper.getModulus());
    PaillierGroupElement g = group2.valueOf(insPkWrapper.getG());

    BigInt plaintext =
        csInspectorBuildingBlock.getPlaintext(proof, "TestIdentifier",
            bigIntFactory.valueOf(123123).toByteArray(), keyPair.getPrivateKey());

    assertEquals(plaintextUsedByDF, plaintext);
  }
}
