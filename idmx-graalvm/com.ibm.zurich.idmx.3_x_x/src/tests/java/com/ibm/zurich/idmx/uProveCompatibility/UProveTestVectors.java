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
package com.ibm.zurich.idmx.uProveCompatibility;

import com.ibm.zurich.idmx.buildingBlock.factory.BuildingBlockFactory;
import com.ibm.zurich.idmx.buildingBlock.signature.SignatureBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.signature.uprove.BrandsKeyPairWrapper;
import com.ibm.zurich.idmx.buildingBlock.signature.uprove.BrandsPublicKeyWrapper;
import com.ibm.zurich.idmx.buildingBlock.signature.uprove.BrandsSecretKeyWrapper;
import com.ibm.zurich.idmx.buildingBlock.signature.uprove.BrandsSignatureBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.reveal.RevealAttributeBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.systemParameters.EcryptSystemParametersWrapper;
import com.ibm.zurich.idmx.dagger.AbcComponent;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.ProofException;
import com.ibm.zurich.idmx.exception.SerializationException;
import com.ibm.zurich.idmx.interfaces.proofEngine.ZkDirector;
import com.ibm.zurich.idmx.interfaces.signature.ListOfSignaturesAndAttributes;
import com.ibm.zurich.idmx.interfaces.state.CarryOverStateIssuer;
import com.ibm.zurich.idmx.interfaces.state.CarryOverStateRecipient;
import com.ibm.zurich.idmx.interfaces.state.IssuanceStateIssuer;
import com.ibm.zurich.idmx.interfaces.state.IssuanceStateRecipient;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.RealTestVectorHelper;
import com.ibm.zurich.idmx.interfaces.util.TestVectorHelper;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProver;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProverIssuance;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleVerifier;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleVerifierIssuance;
import com.ibm.zurich.idmx.tests.TestInitialisation;
import eu.abc4trust.xml.IssuanceExtraMessage;
import eu.abc4trust.xml.SignatureToken;
import eu.abc4trust.xml.ZkProof;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class UProveTestVectors {

  private RealTestVectorHelper tvHelper;
  private BigIntFactory bigIntFactory;
  private SignatureBuildingBlock sigBB;
  private RevealAttributeBuildingBlock revBB;
  private ZkDirector director;
  
  private static final String USERNAME = "user";

  @Before
  public void setUp() {
    try {
      AbcComponent abc = TestInitialisation.INJECTOR;
      BuildingBlockFactory bbf = abc.provideBuildingBlockFactory();

      tvHelper = (RealTestVectorHelper) bbf.getTestVectorHelper();
      bigIntFactory = abc.provideBigIntFactory();
      sigBB = bbf.getBuildingBlockByClass(BrandsSignatureBuildingBlock.class);
      revBB = bbf.getBuildingBlockByClass(RevealAttributeBuildingBlock.class);
      director = abc.providesZkDirector();

    } catch (ConfigurationException e) {
      System.err.println(e);
      assert false;
    }
  }

  @Test
  public void subgroupLiteD0() throws Exception {
    runSubgroupTest("testvectors_SG_D0_lite_doc.txt");
  }
  @Test
  public void subgroupLiteD2() throws Exception {
    runSubgroupTest("testvectors_SG_D2_lite_doc.txt");
  }
  @Test
  public void subgroupLiteD5() throws Exception {
    runSubgroupTest("testvectors_SG_D5_lite_doc.txt");
  }
  
  public void runSubgroupTest(String filename) throws Exception {
    tvHelper.loadResource(filename, getClass());
    tvHelper.loadResource("group-" + tvHelper.getValueAsString("GroupName") + ".txt", getClass());

    EcryptSystemParametersWrapper sp = Helper.generateSystemParameters(tvHelper, bigIntFactory);
    BrandsKeyPairWrapper kp = Helper.generateKeyPair(sp, tvHelper, bigIntFactory);

    issueAndPresent(sp, kp);

    tvHelper.ignoreValuesRegex("A[0-9]+");
    tvHelper.ignoreValuesRegex("g[0-9]+");
    tvHelper.ignoreValuesRegex("U");
    tvHelper.ignoreValuesRegex("domain_parameter_seed");
    assertTrue(tvHelper.finalizeTest());
  }





  private void issueAndPresent(EcryptSystemParametersWrapper sp, BrandsKeyPairWrapper kp)
      throws ConfigurationException, ProofException, SerializationException {
    
    BrandsPublicKeyWrapper pk = kp.getUProvePublicKeyWrapper();
    BrandsSecretKeyWrapper sk = kp.getUProvePrivateKeyWrapper();
    boolean device = false;
    // We use the token information field as credSpecId
    BigInt credSpecId = tvHelper.getValueAsBigInt("TI");
    int numberOfAttributes = pk.getMaximalNumberOfAttributes();
    
    System.out.println("Issuing");
    tvHelper.setPresentation(false);
    ListOfSignaturesAndAttributes sig = issue(sp, pk, sk, device, credSpecId, numberOfAttributes);
    
    System.out.println("Presenting");
    tvHelper.setPresentation(true);
    present(sp, pk, device, credSpecId, numberOfAttributes, sig);
  }

  private ListOfSignaturesAndAttributes issue(EcryptSystemParametersWrapper sp,
      BrandsPublicKeyWrapper pk, BrandsSecretKeyWrapper sk, boolean device, BigInt credSpecId,
      int numberOfAttributes) throws ConfigurationException, ProofException, SerializationException {
    
    final String identifierOfModule = "test";
    
    List<BigInt> attributes = new ArrayList<BigInt>();
    for (int i = 1; i <= numberOfAttributes; ++i) {
      // Here we bypass the "A" values and directly use the "x" values for the attributes
      attributes.add(tvHelper.getValueAsBigInt("x" + i));
    }

    CarryOverStateIssuer coiss = null;
    CarryOverStateRecipient corec = null;

    ZkModuleProverIssuance zkp =
        sigBB.getZkModuleProverIssuance(sp.getSystemParameters(), null, pk.getPublicKey(),
            sk.getSecretKey(), identifierOfModule, credSpecId, device, attributes, coiss);
    ZkProof proof = director.buildProof(USERNAME, Collections.singletonList(zkp), sp.getSystemParameters());

    ZkModuleVerifierIssuance zkv =
        sigBB.getZkModuleVerifierIssuance(sp.getSystemParameters(), null, pk.getPublicKey(),
            identifierOfModule, credSpecId, device, numberOfAttributes, corec);

    boolean ok =
        director.verifyProof(proof, Collections.singletonList(zkv), sp.getSystemParameters());
    assertTrue(ok);

    IssuanceStateIssuer stateIssuer = zkp.recoverIssuanceState();
    IssuanceStateRecipient stateRecipient = zkv.recoverIssuanceState();

    IssuanceExtraMessage iem = null;
    for (int i = 0; i < sigBB.getNumberOfAdditionalIssuanceRoundtrips(); ++i) {
      iem = sigBB.extraIssuanceRoundRecipient(iem, stateRecipient);
      iem = sigBB.extraIssuanceRoundIssuer(iem, stateIssuer);
    }

    ListOfSignaturesAndAttributes sig = sigBB.extractSignature(iem, stateRecipient);

    assertEquals(sig.attributes, attributes);
    return sig;
  }

  private void present(EcryptSystemParametersWrapper sp, BrandsPublicKeyWrapper pk, boolean device,
      BigInt credSpecId, int numberOfAttributes, ListOfSignaturesAndAttributes sig)
      throws ConfigurationException, ProofException, SerializationException {
    final String IDENTIFIER_OF_MODULE = "test";

    SignatureToken tok = sig.signature.getSignatureToken().get(0);

    URI deviceUri = null;
    URI credUri = null;
    
    List<ZkModuleProver> zkp_l = new ArrayList<ZkModuleProver>();
    List<ZkModuleVerifier> zkv_l = new ArrayList<ZkModuleVerifier>();

    // Signature zkModule
    ZkModuleProver zkp =
        sigBB.getZkModuleProverPresentation(sp.getSystemParameters(), null, pk.getPublicKey(),
            IDENTIFIER_OF_MODULE, tok, sig.attributes, credSpecId, deviceUri, USERNAME, credUri);
    zkp_l.add(zkp);

    ZkModuleVerifier zkv =
        sigBB.getZkModuleVerifierPresentation(sp.getSystemParameters(), null, pk.getPublicKey(),
            IDENTIFIER_OF_MODULE, credSpecId, numberOfAttributes, device);
    zkv_l.add(zkv);
    
    // Disclosed attributes zkModules
    String disclosed = tvHelper.getValueAsString("D");
    if (disclosed != null) {
      String[] disclosed_list = disclosed.split(",");
      for(String d: disclosed_list) {
        int index = Integer.parseInt(d)-1;
        String attributeId = zkp.identifierOfAttribute(index);
        
        ZkModuleProver zkp_d = revBB.getZkModuleProver(attributeId);
        zkp_l.add(zkp_d);
        
        ZkModuleVerifier zkv_d = revBB.getZkModuleVerifier(attributeId, null);
        zkv_l.add(zkv_d);
      }
    }

    // Build and verify proof
    ZkProof proof = director.buildProof(USERNAME, zkp_l, sp.getSystemParameters());
    boolean result =
        director.verifyProof(proof, zkv_l, sp.getSystemParameters());
    assertTrue(result);
  }
}
