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
import com.ibm.zurich.idmx.buildingBlock.signature.IssuanceTestHelper;
import com.ibm.zurich.idmx.buildingBlock.signature.cl.ClIssuanceWithMockTest;
import com.ibm.zurich.idmx.buildingBlock.systemParameters.EcryptSystemParametersWrapper;
import com.ibm.zurich.idmx.dagger.AbcComponent;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.ProofException;
import com.ibm.zurich.idmx.exception.SerializationException;
import com.ibm.zurich.idmx.interfaces.proofEngine.ZkDirector;
import com.ibm.zurich.idmx.interfaces.signature.ListOfSignaturesAndAttributes;
import com.ibm.zurich.idmx.interfaces.state.IssuanceStateIssuer;
import com.ibm.zurich.idmx.interfaces.state.IssuanceStateRecipient;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.group.GroupElement;
import com.ibm.zurich.idmx.interfaces.util.group.GroupFactory;
import com.ibm.zurich.idmx.interfaces.util.group.KnownOrderGroup;
import com.ibm.zurich.idmx.interfaces.util.group.KnownOrderGroupElement;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProver;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProverIssuance;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleVerifier;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleVerifierIssuance;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ResidueClass;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateAll;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkVerifyStateAll;
import com.ibm.zurich.idmx.jaxb.JaxbHelperClass;
import com.ibm.zurich.idmx.tests.TestInitialisation;
import com.ibm.zurich.idmx.tests.TestUtils;
import com.ibm.zurich.idmx.tests.setup.TestSystemParameters;
import eu.abc4trust.xml.*;
import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.easymock.EasyMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class UProveIssuanceWithMockTest {
  private SystemParameters systemParameters;
  private KeyPair keyPair;
  private CredentialSpecification credentialSpecification;

  private BrandsSignatureBuildingBlock uproveBB;
  private BigIntFactory bigIntFactory;
  private GroupFactory groupFactory;
  private ZkDirector zkDirector;

  public static final int[] ATTRIBUTE_VALUES = IssuanceTestHelper.ATTRIBUTE_VALUES;
  private static final String USERNAME = "user";

  @Before
  public void setUp() throws SerializationException, ConfigurationException {
    systemParameters = TestUtils.getResource("../sp_default.xml", SystemParameters.class, this);
    keyPair = TestUtils.getResource("keyPair_brands.xml", KeyPair.class, this);
    credentialSpecification =
        TestUtils.getResource("../credSpec_simpleID.xml", CredentialSpecification.class, this);

    AbcComponent abc = TestInitialisation.INJECTOR;
    BuildingBlockFactory bbf = abc.provideBuildingBlockFactory();

    uproveBB = bbf.getBuildingBlockByClass(BrandsSignatureBuildingBlock.class);
    bigIntFactory = abc.provideBigIntFactory();
    groupFactory = abc.provideGroupFactory();
    zkDirector = abc.providesZkDirector();

  }

  @Test
  public void checkPrivateKey() throws ConfigurationException {
    BrandsKeyPairWrapper kp = new BrandsKeyPairWrapper(keyPair);
    EcryptSystemParametersWrapper sp = new EcryptSystemParametersWrapper(systemParameters);
    BigInt y0 = kp.getUProvePrivateKeyWrapper().getY0();

    KnownOrderGroup group =
        groupFactory.createPrimeOrderGroup(sp.getDHModulus(), sp.getDHSubgroupOrder());
    KnownOrderGroupElement g0 = group.valueOfNoCheck(kp.getUProvePublicKeyWrapper().getG0());
    KnownOrderGroupElement g = group.valueOfNoCheck(sp.getDHGenerator1());

    assertEquals(g0, g.multOp(y0));
  }


  @Test
  public void testIssuance() throws SerializationException, ConfigurationException, IOException,
      ProofException {
    String identifierOfModule = "test";
    boolean externalDevice = false;
    BigInt credSpecId =
        ClIssuanceWithMockTest.getNumericalCredSpecId(credentialSpecification, systemParameters,
            bigIntFactory);
    List<BigInt> attributes = new ArrayList<BigInt>();
    for (int attValue : ATTRIBUTE_VALUES) {
      attributes.add(bigIntFactory.valueOf(attValue));
    }
    int numberOfAttributes = attributes.size();

    ZkModuleProverIssuance zkp =
        uproveBB.getZkModuleProverIssuance(systemParameters, null, keyPair.getPublicKey(),
            keyPair.getPrivateKey(), identifierOfModule, credSpecId, externalDevice, attributes,
            null);
    ZkProofStateAll dummyZkBuilder = programZkBuilder();
    zkp.initializeModule(dummyZkBuilder);
    zkp.collectAttributesForProof(dummyZkBuilder);
    zkp.firstRound(dummyZkBuilder);
    zkp.secondRound(dummyZkBuilder);
    IssuanceStateIssuer stateIssuer = zkp.recoverIssuanceState();
    verify(dummyZkBuilder);

    ZkModuleVerifierIssuance zkv =
        uproveBB.getZkModuleVerifierIssuance(systemParameters, null, keyPair.getPublicKey(),
            identifierOfModule, credSpecId, externalDevice, numberOfAttributes, null);
    ZkVerifyStateAll dummyZkVerifier = programZkVerifier(attributes);
    zkv.collectAttributesForVerify(dummyZkVerifier);
    zkv.verify(dummyZkVerifier);
    IssuanceStateRecipient stateRecipient = zkv.recoverIssuanceState();
    verify(dummyZkVerifier);

    IssuanceExtraMessage im1 = uproveBB.extraIssuanceRoundRecipient(null, stateRecipient);
    IssuanceExtraMessage im2 = uproveBB.extraIssuanceRoundIssuer(im1, stateIssuer);

    ListOfSignaturesAndAttributes sig = uproveBB.extractSignature(im2, stateRecipient);

    assertEquals(sig.attributes, attributes);

    // Serialization
    String signature =
        JaxbHelperClass.serialize((new ObjectFactory()).createSignature(sig.signature));
    TestUtils.saveToFile(signature,
        TestSystemParameters.BASE_LOCATION.resolve("issuance/brandsSignature1.xml").toString());
  }

  @Test
  public void testIssuanceAndProof() throws SerializationException, ConfigurationException,
      IOException, ProofException {

    String identifierOfModule = "test:issuance";
    boolean externalDevice = false;
    BigInt credSpecId =
        ClIssuanceWithMockTest.getNumericalCredSpecId(credentialSpecification, systemParameters,
            bigIntFactory);
    List<BigInt> attributes = new ArrayList<BigInt>();
    for (int attValue : ATTRIBUTE_VALUES) {
      attributes.add(bigIntFactory.valueOf(attValue));
    }
    int numberOfAttributes = attributes.size();

    ZkModuleProverIssuance zkp =
        uproveBB.getZkModuleProverIssuance(systemParameters, null, keyPair.getPublicKey(),
            keyPair.getPrivateKey(), identifierOfModule, credSpecId, externalDevice, attributes,
            null);

    ZkProof proof = zkDirector.buildProof(USERNAME, Collections.singletonList(zkp), systemParameters);
    IssuanceStateIssuer stateIssuer = zkp.recoverIssuanceState();

    ZkModuleVerifierIssuance zkv =
        uproveBB.getZkModuleVerifierIssuance(systemParameters, null, keyPair.getPublicKey(),
            identifierOfModule, credSpecId, externalDevice, numberOfAttributes, null);

    boolean result =
        zkDirector.verifyProof(proof, Collections.singletonList(zkv), systemParameters);
    assertTrue(result);
    IssuanceStateRecipient stateRecipient = zkv.recoverIssuanceState();

    IssuanceExtraMessage im1 = uproveBB.extraIssuanceRoundRecipient(null, stateRecipient);
    IssuanceExtraMessage im2 = uproveBB.extraIssuanceRoundIssuer(im1, stateIssuer);

    ListOfSignaturesAndAttributes sig = uproveBB.extractSignature(im2, stateRecipient);

    assertEquals(sig.attributes, attributes);

    // Prove using the signature we just created

    identifierOfModule = "test:proof";
    PublicKey publicKey = keyPair.getPublicKey();
    Signature signature = sig.signature;

    // Get token from signature
    SignatureToken tok = signature.getSignatureToken().get(0);


    ZkModuleProver zkp_proof =
        uproveBB.getZkModuleProverPresentation(systemParameters, null, publicKey,
            identifierOfModule, tok, attributes, credSpecId, null, USERNAME, null);

    ZkModuleVerifier zkv_proof =
        uproveBB.getZkModuleVerifierPresentation(systemParameters, null, publicKey,
            identifierOfModule, credSpecId, numberOfAttributes, externalDevice);

    proof = zkDirector.buildProof(USERNAME, Collections.singletonList(zkp_proof), systemParameters);

    result = zkDirector.verifyProof(proof, Collections.singletonList(zkv_proof), systemParameters);
    assertTrue(result);
  }



  private List<String> dValueLabels;
  private List<GroupElement<?, ?, ?>> dValues;
  private List<String> nValueLabels;
  private List<GroupElement<?, ?, ?>> nValues;

  private ZkProofStateAll programZkBuilder() {
    ZkProofStateAll builder = createMock(ZkProofStateAll.class);

    builder.registerAttribute(isA(String.class), eq(false));
    expectLastCall().anyTimes();

    builder.attributeIsRevealed(isA(String.class));
    expectLastCall().anyTimes();

    builder.setValueOfAttribute(isA(String.class), isA(BigInt.class), EasyMock.anyObject(ResidueClass.class));
    expectLastCall().anyTimes();

    nValueLabels = new ArrayList<String>();
    nValues = new ArrayList<GroupElement<?, ?, ?>>();
    builder.addNValue(isA(String.class), isA(GroupElement.class));
    expectLastCall().andAnswer(new IAnswer<Object>() {
      @Override
      public Object answer() throws Throwable {
        nValueLabels.add((String) getCurrentArguments()[0]);
        nValues.add((GroupElement<?, ?, ?>) getCurrentArguments()[1]);
        return null;
      }
    }).anyTimes();

    builder.addNValue(isA(String.class), isA(byte[].class));
    expectLastCall().anyTimes();

    dValueLabels = new ArrayList<String>();
    dValues = new ArrayList<GroupElement<?, ?, ?>>();
    builder.addDValue(isA(String.class), isA(GroupElement.class));
    expectLastCall().andAnswer(new IAnswer<Object>() {
      @Override
      public Object answer() throws Throwable {
        dValueLabels.add((String) getCurrentArguments()[0]);
        dValues.add((GroupElement<?, ?, ?>) getCurrentArguments()[1]);
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

    verifier.attributeIsRevealed(isA(String.class));
    expectLastCall().anyTimes();
    
    verifier.setResidueClass(isA(String.class), EasyMock.anyObject(ResidueClass.class));
    expectLastCall().anyTimes();

    expect(verifier.isRevealedAttribute(isA(String.class))).andReturn(true).anyTimes();

    for (int i = 0; i < attributes.size(); ++i) {
      expect(verifier.getValueOfRevealedAttribute("test:" + i)).andReturn(attributes.get(i));
    }

    verifier.checkNValue(isA(String.class), isA(byte[].class));
    expectLastCall().anyTimes();

    for (int i = 0; i < nValues.size(); ++i) {
      String label = nValueLabels.get(i);
      GroupElement<?, ?, ?> nValue = nValues.get(i);
      verifier.checkNValue(eq(label), eq(nValue));
    }

    for (int i = 0; i < dValues.size(); ++i) {
      String label = dValueLabels.get(i);
      KnownOrderGroupElement dValue = (KnownOrderGroupElement)dValues.get(i);
      expect(verifier.getDValueAsGroupElement(eq(label), isA(KnownOrderGroup.class))).andReturn(dValue);
    }

    replay(verifier);
    return verifier;
  }
}
