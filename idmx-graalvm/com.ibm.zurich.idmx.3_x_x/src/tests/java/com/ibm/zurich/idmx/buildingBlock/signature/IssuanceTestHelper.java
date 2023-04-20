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
package com.ibm.zurich.idmx.buildingBlock.signature;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.ibm.zurich.idmx.buildingBlock.factory.BuildingBlockFactory;
import com.ibm.zurich.idmx.buildingBlock.signature.cl.ClSignatureBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.equality.AttributeEqualityBuildingBlock;
import com.ibm.zurich.idmx.dagger.AbcComponent;
import com.ibm.zurich.idmx.dagger.DaggerAbcComponent;
import com.ibm.zurich.idmx.tests.TestInitialisation;
import org.junit.Test;

import com.ibm.zurich.idmix.abc4trust.facades.IssuerParametersFacade;
import com.ibm.zurich.idmx.dagger.CryptoTestModule;
import com.ibm.zurich.idmx.buildingBlock.signature.cl.ClIssuanceWithMockTest;
import com.ibm.zurich.idmx.buildingBlock.systemParameters.EcryptSystemParametersWrapper;
import com.ibm.zurich.idmx.device.ExternalSecretsManagerImpl;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.ProofException;
import com.ibm.zurich.idmx.exception.SerializationException;
import com.ibm.zurich.idmx.interfaces.device.ExternalSecretsManager;
import com.ibm.zurich.idmx.interfaces.proofEngine.ZkDirector;
import com.ibm.zurich.idmx.interfaces.signature.ListOfSignaturesAndAttributes;
import com.ibm.zurich.idmx.interfaces.state.CarryOverStateIssuer;
import com.ibm.zurich.idmx.interfaces.state.CarryOverStateRecipient;
import com.ibm.zurich.idmx.interfaces.state.IssuanceStateIssuer;
import com.ibm.zurich.idmx.interfaces.state.IssuanceStateRecipient;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.Pair;
import com.ibm.zurich.idmx.interfaces.util.RandomGeneration;
import com.ibm.zurich.idmx.interfaces.util.group.GroupFactory;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProver;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProverCarryOver;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProverIssuance;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleVerifier;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleVerifierCarryOver;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleVerifierIssuance;
import com.ibm.zurich.idmx.jaxb.JaxbHelperClass;
import com.ibm.zurich.idmx.tests.TestUtils;
import com.ibm.zurich.idmx.tests.setup.TestSystemParameters;

import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.IssuanceExtraMessage;
import eu.abc4trust.xml.KeyPair;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PublicKey;
import eu.abc4trust.xml.SignatureToken;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.ZkProof;

public abstract class IssuanceTestHelper {
   SystemParameters systemParameters;
   KeyPair keyPair;
   CredentialSpecification credentialSpecification;
  protected  KeyManager km;

  private  SignatureBuildingBlock sigBB;
  protected  BigIntFactory bigIntFactory;
  protected  GroupFactory groupFactory;
  protected  RandomGeneration randomGeneration;
  private  ZkDirector director;
  protected  ExternalSecretsManagerImpl esManager;
  private  String outputPrefix;

  public enum CarryOver {
    MANUAL, CARRY_OVER, NONE
  };

  public static final int[] ATTRIBUTE_VALUES = new int[] {1337, 1, 4242};

  public static final URI DEVICE_URI = URI.create("theSmartcard");
  public static final URI CRED_URI_ON_DEVICE = URI.create("theCredential");
  public static final URI ISSUER_ON_DEVICE = URI.create("theIssuer");
  private static final String USERNAME = "user";

  public IssuanceTestHelper(Class<? extends SignatureBuildingBlock> sigClass, String spFile,
      String keyFile) {
    try {
      systemParameters = TestUtils.getResource(spFile, SystemParameters.class, this);
      keyPair = TestUtils.getResource(keyFile, KeyPair.class, this);
      credentialSpecification =
          TestUtils.getResource("../credSpec_simpleID.xml", CredentialSpecification.class, this);

      AbcComponent i = TestInitialisation.INJECTOR;
      BuildingBlockFactory bbf = i.provideBuildingBlockFactory();

      sigBB = bbf.getBuildingBlockByClass(sigClass);
      bigIntFactory = i.provideBigIntFactory();
      groupFactory = i.provideGroupFactory();

      director = i.providesZkDirector();
      randomGeneration = i.provideRandomGeneration();

      km = i.providesKeyManager();
      esManager = (ExternalSecretsManagerImpl) bbf.getExternalSecretsManager();

      //    Injector injector = Guice.createInjector(new CryptoTestModule());
      //    sigBB = injector.getInstance(sigClass);
      //    bigIntFactory = injector.getInstance(BigIntFactory.class);
      //    groupFactory = injector.getInstance(GroupFactory.class);
      //    director = injector.getInstance(ZkDirector.class);
      //    randomGeneration = injector.getInstance(RandomGeneration.class);
      //    km = injector.getInstance(KeyManager.class);
      //    esManager = (ExternalSecretsManagerImpl) injector.getInstance(ExternalSecretsManager.class);

      outputPrefix = sigClass.getName();

    } catch (Exception e) {
      System.err.println(e);

    }
  }

  private String signatureFilename(int i) {
    return TestSystemParameters.BASE_LOCATION.resolve("issuance/" + outputPrefix + i + ".xml")
        .toString();
  }

  @Test
  public void issuanceFromScratchTest() throws SerializationException, ConfigurationException,
      IOException, ProofException {
    List<Boolean> carryOver = Arrays.asList(false, false, false);
    issuance(carryOver, false, 101, CarryOver.NONE);
  }

  @Test
  public void issuanceWithManualCarryOverNoneTest() throws SerializationException,
      ConfigurationException, IOException, ProofException {
    List<Boolean> carryOver = Arrays.asList(false, false, false);
    issuance(carryOver, false, 101, CarryOver.MANUAL);
  }

  @Test
  public void issuanceWithManualCarryOverOneTest() throws SerializationException,
      ConfigurationException, IOException, ProofException {
    List<Boolean> carryOver = Arrays.asList(true, false, false);
    issuance(carryOver, false, 102, CarryOver.MANUAL);
  }

  @Test
  public void issuanceWithManualCarryOverAllTest() throws SerializationException,
      ConfigurationException, IOException, ProofException {
    List<Boolean> carryOver = Arrays.asList(true, true, true);
    issuance(carryOver, false, 103, CarryOver.MANUAL);
  }

  @Test
  public void issuanceWithSmartcardManualCarryOverNoneTest() throws SerializationException,
      ConfigurationException, IOException, ProofException {
    List<Boolean> carryOver = Arrays.asList(false, false, false);
    issuance(carryOver, true, 201, CarryOver.MANUAL);
  }

  @Test
  public void issuanceWithSmartcardManualCarryOverOneTest() throws SerializationException,
      ConfigurationException, IOException, ProofException {
    List<Boolean> carryOver = Arrays.asList(false, false, true);
    issuance(carryOver, true, 202, CarryOver.MANUAL);
  }

  @Test
  public void issuanceWithSmartcardManualCarryOverAllTest() throws SerializationException,
      ConfigurationException, IOException, ProofException {
    List<Boolean> carryOver = Arrays.asList(true, true, true);
    issuance(carryOver, true, 203, CarryOver.MANUAL);
  }

  // -
  @Test
  public void issuanceWithCarryOverNoneTest() throws SerializationException,
      ConfigurationException, IOException, ProofException {
    List<Boolean> carryOver = Arrays.asList(false, false, false);
    issuance(carryOver, false, 301, CarryOver.CARRY_OVER);
  }

  @Test
  public void issuanceWithCarryOverOneTest() throws SerializationException, ConfigurationException,
      IOException, ProofException {
    List<Boolean> carryOver = Arrays.asList(true, false, false);
    issuance(carryOver, false, 302, CarryOver.CARRY_OVER);
  }

  @Test
  public void issuanceWithCarryOverAllTest() throws SerializationException, ConfigurationException,
      IOException, ProofException {
    List<Boolean> carryOver = Arrays.asList(true, true, true);
    issuance(carryOver, false, 303, CarryOver.CARRY_OVER);
  }

  @Test
  public void issuanceWithSmartcardCarryOverNoneTest() throws SerializationException,
      ConfigurationException, IOException, ProofException {
    List<Boolean> carryOver = Arrays.asList(false, false, false);
    issuance(carryOver, true, 401, CarryOver.CARRY_OVER);
  }

  @Test
  public void issuanceWithSmartcardCarryOverOneTest() throws SerializationException,
      ConfigurationException, IOException, ProofException {
    List<Boolean> carryOver = Arrays.asList(false, false, true);
    issuance(carryOver, true, 402, CarryOver.CARRY_OVER);
  }

  @Test
  public void issuanceWithSmartcardCarryOverAllTest() throws SerializationException,
      ConfigurationException, IOException, ProofException {
    List<Boolean> carryOver = Arrays.asList(true, true, true);
    issuance(carryOver, true, 403, CarryOver.CARRY_OVER);
  }

  // --------

  public void issuance(List<Boolean> carryOver, boolean device, int credentialNumber,
      CarryOver carryOverMethod) throws SerializationException, ConfigurationException,
      IOException, ProofException {
    String identifierOfModule = "test";
    BigInt credSpecId =
        ClIssuanceWithMockTest.getNumericalCredSpecId(credentialSpecification, systemParameters,
            bigIntFactory);

    List<BigInt> attributes = new ArrayList<BigInt>();
    for (int attValue : ATTRIBUTE_VALUES) {
      attributes.add(bigIntFactory.valueOf(attValue));
    }
    int numberOfAttributes = attributes.size();
    List<BigInt> allAttributes = new ArrayList<BigInt>(attributes);

    EcryptSystemParametersWrapper sp = new EcryptSystemParametersWrapper(systemParameters);

    // Program external secrets manager
    if (device) {
      programDeviceManager(sp, keyPair);
    }

    Pair<CarryOverStateIssuer, CarryOverStateRecipient> coState;
    switch (carryOverMethod) {
      case MANUAL:
        coState = constructCarryOverStateManually(carryOver, device, attributes, sp, keyPair);
        break;
      case CARRY_OVER:
        coState = constructCarryOverState(carryOver, device, attributes, sp);
        break;
      case NONE:
        coState = new Pair<CarryOverStateIssuer, CarryOverStateRecipient>(null, null);
        break;
      default:
        throw new RuntimeException();
    }
    CarryOverStateIssuer coiss = coState.first;
    CarryOverStateRecipient corec = coState.second;

    ZkModuleProverIssuance zkp =
        sigBB.getZkModuleProverIssuance(systemParameters, null, keyPair.getPublicKey(),
            keyPair.getPrivateKey(), identifierOfModule, credSpecId, device, attributes, coiss);
    ZkProof proof = director.buildProof(USERNAME, Collections.singletonList(zkp), systemParameters);

    ZkModuleVerifierIssuance zkv =
        sigBB.getZkModuleVerifierIssuance(systemParameters, null, keyPair.getPublicKey(),
            identifierOfModule, credSpecId, device, numberOfAttributes, corec);

    boolean ok = director.verifyProof(proof, Collections.singletonList(zkv), systemParameters);
    assertTrue(ok);

    IssuanceStateIssuer stateIssuer = zkp.recoverIssuanceState();
    IssuanceStateRecipient stateRecipient = zkv.recoverIssuanceState();

    IssuanceExtraMessage iem = null;
    for (int i = 0; i < sigBB.getNumberOfAdditionalIssuanceRoundtrips(); ++i) {
      iem = sigBB.extraIssuanceRoundRecipient(iem, stateRecipient);
      iem = sigBB.extraIssuanceRoundIssuer(iem, stateIssuer);
    }

    ListOfSignaturesAndAttributes sig = sigBB.extractSignature(iem, stateRecipient);

    assertEquals(sig.attributes, allAttributes);

    presentSignature(sig, device);

    // Serialization
    String signature =
        JaxbHelperClass.serialize((new ObjectFactory()).createSignature(sig.signature));
    TestUtils.saveToFile(signature, signatureFilename(credentialNumber));
  }

  private Pair<CarryOverStateIssuer, CarryOverStateRecipient> constructCarryOverState(
      List<Boolean> carryOver, boolean device, List<BigInt> attributes,
      EcryptSystemParametersWrapper sp) throws ConfigurationException, ProofException,
      SerializationException {
    final String IDENTIFIER_OF_MODULE = "co";
    PublicKey publicKey = keyPair.getPublicKey();
    URI deviceUri = device ? DEVICE_URI : null;
    URI credUri = device ? CRED_URI_ON_DEVICE : null;
    BigInt credSpecId =
        ClIssuanceWithMockTest.getNumericalCredSpecId(credentialSpecification, systemParameters,
            bigIntFactory);

    List<BigInt> coAttributes = new ArrayList<BigInt>();
    List<Boolean> setByIssuer = new ArrayList<Boolean>();
    for (int i = 0; i < carryOver.size(); ++i) {
      if (carryOver.get(i)) {
        BigInt value = attributes.get(i);
        attributes.set(i, null);
        coAttributes.add(value);
        setByIssuer.add(false);
      } else {
        coAttributes.add(null);
        setByIssuer.add(true);
      }
    }

    ZkModuleProverCarryOver zkp =
        sigBB.getZkModuleProverCarryOver(systemParameters, null, publicKey, IDENTIFIER_OF_MODULE,
            deviceUri, USERNAME, credUri, credSpecId, carryOver, coAttributes);

    ZkModuleVerifierCarryOver zkv =
        sigBB.getZkModuleVerifierCarryOver(systemParameters, null, publicKey, IDENTIFIER_OF_MODULE,
            credSpecId, setByIssuer, device);

    ZkProof proof = director.buildProof(USERNAME, Collections.singletonList(zkp), systemParameters);
    String proofAsString = JaxbHelperClass.serialize((new ObjectFactory()).createZkProof(proof));
    System.out.println(proofAsString);
    boolean result = director.verifyProof(proof, Collections.singletonList(zkv), systemParameters);
    assertTrue(result);

    CarryOverStateIssuer coiss1 = zkv.recoverState();
    CarryOverStateRecipient corec1 = zkp.recoverState();
    Pair<CarryOverStateIssuer, CarryOverStateRecipient> coState =
        new Pair<CarryOverStateIssuer, CarryOverStateRecipient>(coiss1, corec1);
    return coState;
  }

  private void presentSignature(ListOfSignaturesAndAttributes sig, boolean device)
      throws SerializationException, ConfigurationException, IOException, ProofException {
    final String IDENTIFIER_OF_MODULE = "test";
    PublicKey publicKey = keyPair.getPublicKey();

    SignatureToken tok = sig.signature.getSignatureToken().get(0);
    BigInt credSpecId =
        ClIssuanceWithMockTest.getNumericalCredSpecId(credentialSpecification, systemParameters,
            bigIntFactory);
    int numberOfAttributes = sig.attributes.size();

    URI deviceUri = device ? DEVICE_URI : null;
    URI credUri = device ? CRED_URI_ON_DEVICE : null;

    ZkModuleProver zkp =
        sigBB.getZkModuleProverPresentation(systemParameters, null, publicKey,
            IDENTIFIER_OF_MODULE, tok, sig.attributes, credSpecId, deviceUri, USERNAME, credUri);

    ZkModuleVerifier zkv =
        sigBB.getZkModuleVerifierPresentation(systemParameters, null, publicKey,
            IDENTIFIER_OF_MODULE, credSpecId, numberOfAttributes, device);

    ZkProof proof = director.buildProof(USERNAME, Collections.singletonList(zkp), systemParameters);
    // String proofAsString = JaxbHelperClass.serialize((new ObjectFactory()).createZkProof(proof));
    // System.out.println(proofAsString);
    boolean result = director.verifyProof(proof, Collections.singletonList(zkv), systemParameters);
    assertTrue(result);
  }

  protected void programDeviceManager(EcryptSystemParametersWrapper sp, KeyPair keyPair) throws ConfigurationException {
    try {
      km.storeSystemParameters(sp.getSystemParameters());
      IssuerParametersFacade ip =
          IssuerParametersFacade.initIssuerParameters(keyPair.getPublicKey(),
              sp.getSystemParameters());
      km.storeIssuerParameters(ISSUER_ON_DEVICE, ip.getIssuerParameters());
    } catch (KeyManagerException e) {
      throw new RuntimeException();
    }
    esManager.allocateCredential(USERNAME, DEVICE_URI, CRED_URI_ON_DEVICE, ISSUER_ON_DEVICE, true);
  }

  protected abstract Pair<CarryOverStateIssuer, CarryOverStateRecipient> constructCarryOverStateManually(
      List<Boolean> carryOver, boolean device, List<BigInt> attributes,
      EcryptSystemParametersWrapper sp, KeyPair keyPair) throws ConfigurationException;

}
