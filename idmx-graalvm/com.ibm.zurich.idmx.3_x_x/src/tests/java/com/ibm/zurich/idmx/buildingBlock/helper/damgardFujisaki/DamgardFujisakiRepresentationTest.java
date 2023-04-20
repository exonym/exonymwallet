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

import static org.junit.Assert.assertTrue;

import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import com.ibm.zurich.idmx.dagger.AbcComponent;
import com.ibm.zurich.idmx.tests.TestInitialisation;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.ibm.zurich.idmx.buildingBlock.helper.BaseForRepresentation;
import com.ibm.zurich.idmx.buildingBlock.helper.representation.damgardFujisaki.DamgardFujisakiRepresentationBuildingBlock;
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
import com.ibm.zurich.idmx.interfaces.util.group.HiddenOrderGroup;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProver;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProverCommitment;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleVerifier;
import com.ibm.zurich.idmx.keypair.issuer.IssuerPublicKeyWrapper;
import com.ibm.zurich.idmx.tests.TestUtils;

import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.xml.KeyPair;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.ZkProof;

public class DamgardFujisakiRepresentationTest {
  SystemParameters systemParameters;
  DamgardFujisakiRepresentationBuildingBlock damgardFujisakiBB;
  ConstantBuildingBlock constantBB;
  RevealAttributeBuildingBlock revealBB;
  BigIntFactory bigIntFactory;
  GroupFactory groupFactory;
  RandomGeneration randomGeneration;
  ZkDirector director;
  KeyManager km;

  private static final String IDENTIFIER_OF_MODULE = "test";
  private static final String USERNAME = "user";
  public List<BigInt> ATTRIBUTE_VALUES = new ArrayList<BigInt>();
  public List<BigInt> BASE_VALUES = new ArrayList<BigInt>();
  public BigInt MODULUS;
  public BigInt BASE_Rd;
  public BigInt BASE_S;
  public ExternalSecretsManagerImpl esManager;
  public EcryptSystemParametersWrapper spWrapper;
  public HiddenOrderGroup group;
  public KeyPair keyPair;
  public IssuerPublicKeyWrapper clpkw;

  @Before
  public void setUp() throws SerializationException, ConfigurationException, KeyManagerException {
    systemParameters = TestUtils.getResource("sp_default.xml", SystemParameters.class, this);
    keyPair = TestUtils.getResource("keyPair_cl.xml", KeyPair.class, this);

    AbcComponent inj = TestInitialisation.INJECTOR;
    bigIntFactory = inj.provideBigIntFactory();
    groupFactory = inj.provideGroupFactory();
    randomGeneration = inj.provideRandomGeneration();
    damgardFujisakiBB = inj.provideBuildingBlockFactory()
            .getBuildingBlockByClass(DamgardFujisakiRepresentationBuildingBlock.class);
    constantBB = inj.provideBuildingBlockFactory()
            .getBuildingBlockByClass(ConstantBuildingBlock.class);
    revealBB = inj.provideBuildingBlockFactory()
            .getBuildingBlockByClass(RevealAttributeBuildingBlock.class);
    km = inj.providesKeyManager();
    esManager = (ExternalSecretsManagerImpl)
            inj.provideBuildingBlockFactory().getExternalSecretsManager();

    spWrapper = new EcryptSystemParametersWrapper(systemParameters);
    km.storeSystemParameters(systemParameters);
    director = inj.providesZkDirector();

//    Injector injector = Guice.createInjector(new CryptoTestModule());
//
//    bigIntFactory = injector.getInstance(BigIntFactory.class);
//    groupFactory = injector.getInstance(GroupFactory.class);
//    randomGeneration = injector.getInstance(RandomGeneration.class);
//    damgardFujisakiBB = injector.getInstance(DamgardFujisakiRepresentationBuildingBlock.class);
//    constantBB = injector.getInstance(ConstantBuildingBlock.class);
//    revealBB = injector.getInstance(RevealAttributeBuildingBlock.class);
//    km = injector.getInstance(KeyManager.class);
//    esManager = (ExternalSecretsManagerImpl) injector.getInstance(ExternalSecretsManager.class);
//    this.spWrapper = new EcryptSystemParametersWrapper(systemParameters);
//    km.storeSystemParameters(systemParameters);
//    director = injector.getInstance(ZkDirector.class);

    // get the bases from the public key (the element names are taken from the ClPublicKeyWrapper)
    String MODULUS_NAME = "rsaModulus";
    String S_NAME = "base:S";
    String BASE_DEVICE = "base:d";
    this.clpkw = new IssuerPublicKeyWrapper(keyPair.getPublicKey());
    MODULUS = (BigInt) clpkw.getParameter(MODULUS_NAME);
    BASE_Rd = (BigInt) clpkw.getParameter(BASE_DEVICE);
    BASE_S = (BigInt) clpkw.getParameter(S_NAME);

    group = groupFactory.createSignedQuadraticResiduesGroup(MODULUS);
    for (int i = 0; i < 6; i++) {
      BASE_VALUES.add(bigIntFactory.valueOf((int) Math.pow(7 * (i + 1), 2)));
      ATTRIBUTE_VALUES.add(randomGeneration.generateRandomNumber(spWrapper
          .getMaximumAttributeValue(-1, bigIntFactory)));
    }
    // setIssuerParametersForCred or so from publicKey
  }

  @Test
  public void allRevealedNoneRandomNoneSecret() throws SerializationException,
      ConfigurationException, ProofException {
    List<Integer> revealed = new ArrayList<Integer>();
    for (int i = 0; i < ATTRIBUTE_VALUES.size(); i++) {
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
    for (int i = 0; i < ATTRIBUTE_VALUES.size(); i++) {
      random.add(i);
    }
    present(new ArrayList<Integer>(), random, -1, -1, null, BASE_VALUES.size(), false);
  }

  @Test
  public void allRevealedAllRandomNoneSecret() throws SerializationException,
      ConfigurationException, ProofException {
    List<Integer> revealed = new ArrayList<Integer>();
    List<Integer> random = new ArrayList<Integer>();
    for (int i = 0; i < ATTRIBUTE_VALUES.size(); i++) {
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

  @Ignore
  @Test
  public void noneRevealedNoneRandomProveCardPublicKey() throws SerializationException,
      ConfigurationException, ProofException {
    present(new ArrayList<Integer>(), new ArrayList<Integer>(), 5, -1, null, BASE_VALUES.size(),
        false);
  }

  @Ignore
  @Test
  public void noneRevealedNoneRandomProveScopeExclusivePseudonym() throws SerializationException,
      ConfigurationException, ProofException {
    List<Integer> revealed = new ArrayList<Integer>();
    present(revealed, new ArrayList<Integer>(), 0, -1, null, BASE_VALUES.size(), false);
  }

  @Ignore
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
        issuerUri = new URI("TestIssuer");
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
      BigInt firstBase = BASE_Rd;
      BigInt secondBase = indexOfExternalRandomizer != -1 ? BASE_S : null;
      esManager.setFakeIssuerParam(issuerUri, MODULUS, firstBase, secondBase);
      esManager.allocateCredential(USERNAME, deviceUri, credentialUri, issuerUri, true);
    }

    List<BaseForRepresentation> bases = new ArrayList<BaseForRepresentation>();
    GroupElement<?,?,?> b;
    for (int i = 0; i < numberOfBases; i++) {
      b = group.valueOf(BASE_VALUES.get(i));
      if (random.contains(i)) {
        bases.add(BaseForRepresentation.randomAttribute(b));
      } else if (i == indexOfExternalSecret) {
        bases.add(BaseForRepresentation.deviceSecret(group.valueOf(BASE_Rd)));
      } else if (i == indexOfExternalRandomizer) {
        BigInteger a = esManager.getBaseForCredentialSecret(USERNAME, deviceUri, credentialUri);
        BigInt tmp = bigIntFactory.valueOf(a);
        bases.add(BaseForRepresentation.deviceRandomizer(group.valueOf(tmp),
            group.createRandomIterationcounter(randomGeneration, spWrapper.getStatisticalInd())));
      } else {
        bases.add(BaseForRepresentation.managedAttribute(b));
      }
    }

    ZkModuleProverCommitment zkp = damgardFujisakiBB.getZkModuleProver(systemParameters, // systemParameters,
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
            constantBB.getZkModuleProver(IDENTIFIER_OF_MODULE + ":" + i, ATTRIBUTE_VALUES.get(i));
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

    ZkModuleVerifier zkv = damgardFujisakiBB.getZkModuleVerifier(systemParameters, // systemParameters
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
