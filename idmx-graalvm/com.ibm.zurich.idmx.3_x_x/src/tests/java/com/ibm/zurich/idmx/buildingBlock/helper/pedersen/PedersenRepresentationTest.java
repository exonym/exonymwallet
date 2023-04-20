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
package com.ibm.zurich.idmx.buildingBlock.helper.pedersen;

import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import com.ibm.zurich.idmx.buildingBlock.factory.BuildingBlockFactory;
import com.ibm.zurich.idmx.dagger.AbcComponent;
import com.ibm.zurich.idmx.tests.TestInitialisation;
import org.junit.Before;
import org.junit.Test;

import com.ibm.zurich.idmx.buildingBlock.helper.BaseForRepresentation;
import com.ibm.zurich.idmx.buildingBlock.helper.representation.pedersen.PedersenRepresentationBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.constant.ConstantBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.reveal.RevealAttributeBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.systemParameters.EcryptSystemParametersWrapper;
import com.ibm.zurich.idmx.device.ExternalSecretsManagerImpl;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.ProofException;
import com.ibm.zurich.idmx.exception.SerializationException;
import com.ibm.zurich.idmx.interfaces.proofEngine.ZkDirector;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.RandomGeneration;
import com.ibm.zurich.idmx.interfaces.util.group.GroupElement;
import com.ibm.zurich.idmx.interfaces.util.group.GroupFactory;
import com.ibm.zurich.idmx.interfaces.util.group.KnownOrderGroup;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProver;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProverCommitment;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleVerifier;
import com.ibm.zurich.idmx.tests.TestUtils;

import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.ZkProof;

public class PedersenRepresentationTest {
  SystemParameters systemParameters;
  PedersenRepresentationBuildingBlock pedersenBB;
  ConstantBuildingBlock constantBB;
  RevealAttributeBuildingBlock revealBB;
  BigIntFactory bigIntFactory;
  GroupFactory groupFactory;
  RandomGeneration randomGeneration;
  ZkDirector director;

  private static final String IDENTIFIER_OF_MODULE = "test";
  private static final String USERNAME = "user";
  public static final int[] ATTRIBUTE_VALUES = new int[] {1, 1, 2, 3, 5, 8};
  public List<BigInt> BASE_VALUES;
  public BigInt MODULUS;
  public BigInt GROUP_ORDER;
  public ExternalSecretsManagerImpl esManager;
  public EcryptSystemParametersWrapper spWrapper;
  public KeyManager km;

  //    Injector injector = Guice.createInjector(new CryptoTestModule());
//
//    bigIntFactory = injector.getInstance(BigIntFactory.class);
//    groupFactory = injector.getInstance(GroupFactory.class);
//    randomGeneration = injector.getInstance(RandomGeneration.class);
//    pedersenBB = injector.getInstance(PedersenRepresentationBuildingBlock.class);
//    constantBB = injector.getInstance(ConstantBuildingBlock.class);
//    revealBB = injector.getInstance(RevealAttributeBuildingBlock.class);
//    esManager = (ExternalSecretsManagerImpl) injector.getInstance(ExternalSecretsManager.class);
//    director = injector.getInstance(ZkDirector.class);

  @Before
  public void setUp() throws SerializationException, ConfigurationException, KeyManagerException {
    systemParameters = TestUtils.getResource("sp_default.xml", SystemParameters.class, this);
    this.spWrapper = new EcryptSystemParametersWrapper(systemParameters);

    AbcComponent inj = TestInitialisation.INJECTOR;
    BuildingBlockFactory bbf = inj.provideBuildingBlockFactory();
    bigIntFactory = inj.provideBigIntFactory();
    groupFactory = inj.provideGroupFactory();
    randomGeneration = inj.provideRandomGeneration();
    director = inj.providesZkDirector();
    pedersenBB = bbf.getBuildingBlockByClass(PedersenRepresentationBuildingBlock.class);
    constantBB = bbf.getBuildingBlockByClass(ConstantBuildingBlock.class);
    revealBB = bbf.getBuildingBlockByClass(RevealAttributeBuildingBlock.class);
    esManager = (ExternalSecretsManagerImpl) bbf.getExternalSecretsManager();

    km = inj.providesKeyManager();
    km.storeSystemParameters(systemParameters);

    GROUP_ORDER = spWrapper.getDHSubgroupOrder();
    MODULUS = spWrapper.getDHModulus();
    // setIssuerParametersForCred or so from publicKey
    
    BASE_VALUES = new ArrayList<BigInt>();
    for (int i = 0; i < ATTRIBUTE_VALUES.length; i++) {
      BigInt t = bigIntFactory.valueOf(7*(i+1));
      t = t.modPow((MODULUS.subtract(bigIntFactory.one())).divide(GROUP_ORDER), MODULUS);
      System.out.println(t);
      BASE_VALUES.add(t);
    }
  }

  @Test
  public void allRevealedNoneRandomNoneSecret() throws SerializationException,
      ConfigurationException, ProofException {
    List<Integer> revealed = new ArrayList<Integer>();
    for (int i = 0; i < ATTRIBUTE_VALUES.length; i++) {
      revealed.add(i);
    }
    present(revealed, new ArrayList<Integer>(), -1, -1, null, BASE_VALUES.size(), false);
  }

  @Test
  public void noneRevealedNoneRandomNoneSecret() throws SerializationException,
      ConfigurationException, ProofException {
    List<Integer> revealed = new ArrayList<Integer>();
    present(revealed, new ArrayList<Integer>(), -1, -1, null, BASE_VALUES.size(), false);
  }

  @Test
  public void noneRevealedAllRandomNoneSecret() throws SerializationException,
      ConfigurationException, ProofException {
    List<Integer> random = new ArrayList<Integer>();
    for (int i = 0; i < ATTRIBUTE_VALUES.length; i++) {
      random.add(i);
    }
    present(new ArrayList<Integer>(), random, -1, -1, null, BASE_VALUES.size(), false);
  }

  @Test
  public void allRevealedAllRandomNoneSecret() throws SerializationException,
      ConfigurationException, ProofException {
    List<Integer> revealed = new ArrayList<Integer>();
    List<Integer> random = new ArrayList<Integer>();
    for (int i = 0; i < ATTRIBUTE_VALUES.length; i++) {
      random.add(i);
      revealed.add(i);
    }
    present(revealed, random, -1, -1, null, BASE_VALUES.size(), false);
  }

  @Test
  public void someRevealedSomeRandomNoneSecret() throws SerializationException,
      ConfigurationException, ProofException {
    List<Integer> revealed = new ArrayList<Integer>();
    revealed.add(0);
    revealed.add(1);
    revealed.add(2);
    List<Integer> random = new ArrayList<Integer>();
    random.add(0);
    random.add(2);
    random.add(4);
    random.add(5);
    present(revealed, random, -1, -1, null, BASE_VALUES.size(), false);
  }


  @Test
  public void noneRevealedNoneRandomProveCardPublicKey() throws SerializationException,
      ConfigurationException, ProofException {
    present(new ArrayList<Integer>(), new ArrayList<Integer>(), 5, -1, null, BASE_VALUES.size(),
        false);
  }

  @Test
  public void noneRevealedNoneRandomProveScopeExclusivePseudonym() throws SerializationException,
      ConfigurationException, ProofException {
    List<Integer> revealed = new ArrayList<Integer>();
    present(revealed, new ArrayList<Integer>(), 0, -1, null, BASE_VALUES.size(), false);
  }

  @Test
  public void oneRevealedOneRandomProveCardPublicKey() throws SerializationException,
      ConfigurationException, ProofException {
    List<Integer> revealed = new ArrayList<Integer>();
    List<Integer> random = new ArrayList<Integer>();
    random.add(1);
    revealed.add(2);
    present(revealed, random, 5, -1, null, BASE_VALUES.size(), false);
  }

  @Test
  public void oneRevealedOneRandomOneBaseCredential() throws SerializationException,
      ConfigurationException, ProofException {
    List<Integer> revealed = new ArrayList<Integer>();
    List<Integer> random = new ArrayList<Integer>();
    random.add(1);
    revealed.add(2);
    present(revealed, random, 5, -1, null, BASE_VALUES.size(), true);
  }

  @Test
  public void oneRevealedOneRandomTwoBaseCredential() throws SerializationException,
      ConfigurationException, ProofException {
    List<Integer> revealed = new ArrayList<Integer>();
    List<Integer> random = new ArrayList<Integer>();
    random.add(1);
    revealed.add(2);
    present(revealed, random, 4, 5, null, BASE_VALUES.size(), true);
  }

  private void present(List<Integer> revealed, List<Integer> random, int indexOfExternalSecret,
      int indexOfExternalRandomizer, String scopeString, int numberOfBases, boolean proveCredential)
      throws SerializationException, ConfigurationException, ProofException {
    URI deviceUri = null;
    URI credentialUri = null;
    URI issuerUri = null;
    URI scope = null;

    if (scopeString != null) {
      try {
        scope = new URI(scopeString);
      } catch (URISyntaxException e) {
        e.printStackTrace();
      }
    }

    if (!(indexOfExternalSecret == -1)) {
      try {
        issuerUri = new URI("ISSUER");
        deviceUri = new URI("TestDevice");
      } catch (URISyntaxException e) {
        e.printStackTrace();
      }
    }

    if (proveCredential) {
      try {
        credentialUri = new URI("TestCredential");
      } catch (URISyntaxException e) {
        e.printStackTrace();
      }
      if (indexOfExternalRandomizer != -1) {
        esManager.setFakeIssuerParam(issuerUri, spWrapper.getDHModulus(), spWrapper.getDHGenerator1(),
            spWrapper.getDHGenerator2());
      } else {
        esManager.setFakeIssuerParam(issuerUri, spWrapper.getDHModulus(), spWrapper.getDHGenerator1(), null);
      }
      esManager.allocateCredential(USERNAME, deviceUri, credentialUri, issuerUri, true);
    }

    List<BaseForRepresentation> bases = new ArrayList<BaseForRepresentation>();
    GroupElement<?, ?, ?> b;
    KnownOrderGroup group =
        groupFactory.createPrimeOrderGroup(MODULUS,GROUP_ORDER);
    for (int i = 0; i < numberOfBases; i++) {
      b = group.valueOf(BASE_VALUES.get(i));
      if (random.contains(i)) {
        bases.add(BaseForRepresentation.randomAttribute(b));
      } else if (i == indexOfExternalSecret) {
        BigInt tmp = spWrapper.getDHGenerator1();
        bases.add(BaseForRepresentation.deviceSecret(group.valueOf(tmp)));
      } else if (i == indexOfExternalRandomizer) {
        bases.add(BaseForRepresentation.deviceRandomizer(
            group.valueOf(spWrapper.getDHGenerator2()), null));
      } else {
        bases.add(BaseForRepresentation.managedAttribute(b));
      }
    }

    ZkModuleProverCommitment zkp = pedersenBB.getZkModuleProver(systemParameters, // systemParameters,
        IDENTIFIER_OF_MODULE, // identifierOfModule
        credentialUri, // identifierOfCredentialForSecret
        bases, // bases
        group, // group
        null, // potentially the commitment
        deviceUri, // deviceUid
        USERNAME, 
        scope); // scope


    List<ZkModuleProver> modulesProver = new ArrayList<ZkModuleProver>();
    List<ZkModuleVerifier> modulesVerifier = new ArrayList<ZkModuleVerifier>();


    ZkModuleProver zkp_constant = null;
    ZkModuleVerifier zkv_constant = null;
    for (int i = 0; i < numberOfBases; i++) {
      if (!(bases.get(i).chooseExponentRandomly || bases.get(i).hasCredentialSecretKey || bases
          .get(i).hasExternalSecret)) {
        zkp_constant =
            constantBB.getZkModuleProver(IDENTIFIER_OF_MODULE + ":" + i,
                bigIntFactory.valueOf(ATTRIBUTE_VALUES[i]));
        modulesProver.add(zkp_constant);
        zkv_constant = constantBB.getZkModuleVerifier(IDENTIFIER_OF_MODULE + ":" + i, null);
        modulesVerifier.add(zkv_constant);
      }
    }


    for (int i : revealed) {
      ZkModuleProver zk = revealBB.getZkModuleProver(IDENTIFIER_OF_MODULE + ":" + i);
      modulesProver.add(zk);
    }

    modulesProver.add(zkp);

    ZkProof proof = director.buildProof(USERNAME, modulesProver, systemParameters);

    ZkModuleVerifier zkv = pedersenBB.getZkModuleVerifier(systemParameters, // systemParameters
        IDENTIFIER_OF_MODULE, // identifierOfModule
        bases, // bases
        null, // commitment
        IDENTIFIER_OF_MODULE + ":C", // commitmentAsDValue
        group); // group


    for (int i : revealed) {
      ZkModuleVerifier zk = revealBB.getZkModuleVerifier(IDENTIFIER_OF_MODULE + ":" + i, null);
      modulesVerifier.add(zk);
    }

    modulesVerifier.add(zkv);

    boolean result = director.verifyProof(proof, modulesVerifier, systemParameters);
    assertTrue(result);

  }
}
