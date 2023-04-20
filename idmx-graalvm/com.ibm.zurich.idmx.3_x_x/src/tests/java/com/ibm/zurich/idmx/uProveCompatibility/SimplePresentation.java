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

import com.ibm.zurich.idmix.abc4trust.facades.Abc4TrustSecretKeyFacade;
import com.ibm.zurich.idmix.abc4trust.facades.IssuerParametersFacade;
import com.ibm.zurich.idmx.buildingBlock.signature.uprove.BrandsKeyPairWrapper;
import com.ibm.zurich.idmx.buildingBlock.systemParameters.EcryptSystemParametersWrapper;
import com.ibm.zurich.idmx.dagger.AbcComponent;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.RealTestVectorHelper;
import com.ibm.zurich.idmx.jaxb.JaxbHelperClass;
import com.ibm.zurich.idmx.tests.TestInitialisation;
import com.ibm.zurich.idmx.tests.TestUtils;
import com.ibm.zurich.idmx.tests.setup.TestSystemParameters;
import eu.abc4trust.abce.internal.issuer.credentialManager.CredentialManager;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.cryptoEngine.issuer.CryptoEngineIssuer;
import eu.abc4trust.cryptoEngine.user.CryptoEngineUser;
import eu.abc4trust.cryptoEngine.verifier.CryptoEngineVerifier;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.returnTypes.IssuMsgOrCredDesc;
import eu.abc4trust.returnTypes.IssuerParametersAndSecretKey;
import eu.abc4trust.xml.*;
import org.junit.Before;
import org.junit.Test;

import javax.xml.bind.JAXBElement;
import java.net.URI;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class SimplePresentation {

  private BigIntFactory bigIntFactory;
  private CryptoEngineIssuer cei;
  private CryptoEngineUser ceu;
  private CryptoEngineVerifier cev;
  private ObjectFactory of;

  private final String SYSTEM_PARAMETERS_FILE = "1.3.6.1.4.1.311.75.1.1.1";
  private final String CRED_SPEC_FILE = "credSpec.xml";
  private final String ISSUANCE_POLICY_FILE = "issuancePolicy.xml";
  private final String ISSUER_ATTRIBUTES_FILE = "issuerAttributes.xml";
  private static final String USERNAME = "user";

  @Before
  public void setUp() {
    AbcComponent injector = TestInitialisation.INJECTOR;
    bigIntFactory = injector.provideBigIntFactory();
    cei = injector.providesCryptoEngineIssuerAbc();
    ceu = injector.provideCryptoEngineUser();
    cev = injector.providesCryptoEngineVerifierAbc();
    of = new ObjectFactory();
  }

  @Test
  public void simplePresentation0() throws Exception {
    runSimpleTest("ptd0.xml", "simple-0", false);
  }

  @Test
  public void simplePresentation2() throws Exception {
    runSimpleTest("ptd2.xml", "simple-2", false);
  }

  @Test
  public void simplePresentation5() throws Exception {
    runSimpleTest("ptd5.xml", "simple-5", false);
  }
  
  @Test
  public void ownKeyPresentation0() throws Exception {
    runOwnKeyTest("ptd0.xml", "ownKey-0", false);
  }
  
  @Test
  public void ownKeyPresentation2() throws Exception {
    runOwnKeyTest("ptd2.xml", "ownKey-2", false);
  }
  
  @Test
  public void ownKeyPresentation5() throws Exception {
    runOwnKeyTest("ptd5.xml", "ownKey-5", false);
  }
  
  @Test
  public void simplePresentation0_SimpleProof() throws Exception {
    runSimpleTest("ptd0.xml", "simple-0", true);
  }

  @Test
  public void simplePresentation2_SimpleProof() throws Exception {
    runSimpleTest("ptd2.xml", "simple-2", true);
  }

  @Test
  public void simplePresentation5_SimpleProof() throws Exception {
    runSimpleTest("ptd5.xml", "simple-5", true);
  }
  
  @Test
  public void ownKeyPresentation0_SimpleProof() throws Exception {
    runOwnKeyTest("ptd0.xml", "ownKey-0", true);
  }
  
  @Test
  public void ownKeyPresentation2_SimpleProof() throws Exception {
    runOwnKeyTest("ptd2.xml", "ownKey-2", true);
  }
  
  @Test
  public void ownKeyPresentation5_SimpleProof() throws Exception {
    runOwnKeyTest("ptd5.xml", "ownKey-5", true);
  }
  
  private void runOwnKeyTest(String ptdName, String testName, boolean simpleProof) throws Exception {
    generateAndStoreKeys(CRED_SPEC_FILE, testName);
    runTest(ptdName, testName, simpleProof);
  }

  private void generateAndStoreKeys(String csName, String testName) throws Exception {
    AbcComponent injector = TestInitialisation.INJECTOR;
    KeyManager km = injector.providesKeyManager();
    CredentialManager icm = injector.providesCredentialManagerIssuer();
    
    // Generate SP & store
    SystemParameters sp = cei.setupSystemParameters(1248);
    km.storeSystemParameters(sp);
    
    // Generate issuer parameters
    URI ipuid = URI.create("uprovecompatibility:ip");
    URI technology = URI.create("U-Prove");
    IssuerParametersAndSecretKey ipsk = cei.setupIssuerParameters(sp, 10, technology, ipuid, null, null);
    km.storeIssuerParameters(ipsk.issuerParameters.getParametersUID(), ipsk.issuerParameters);
    icm.storeIssuerSecretKey(ipsk.issuerParameters.getParametersUID(), ipsk.issuerSecretKey);
    
    // Load CS
    CredentialSpecification cs =
        TestUtils.getResource(csName, CredentialSpecification.class, this, true);
    km.storeCredentialSpecification(cs.getSpecificationUID(), cs);
    
    // save
    saveToFile(of.createSystemParameters(sp), testName, "systemParameters");
    saveToFile(of.createIssuerParameters(ipsk.issuerParameters), testName, "issuerParameters");
    saveToFile(of.createIssuerSecretKey(ipsk.issuerSecretKey), testName, "issuerSecretKey");
    saveToFile(of.createCredentialSpecification(cs), testName, "credentialSpecification");
  }

  private void runSimpleTest(String ptdName, String testName, boolean simpleProof) throws Exception {
    loadAndStoreKeys(SYSTEM_PARAMETERS_FILE, CRED_SPEC_FILE, testName);
    runTest(ptdName, testName, simpleProof);
  }

  private void runTest(String ptdName, String testName, boolean simpleProof) throws Exception {
    PresentationTokenDescription ptd =
        TestUtils.getResource(ptdName, PresentationTokenDescription.class, this, true);
    if(simpleProof) {
      ptd.setUsesSimpleProof(true);
    }
    IssuancePolicy isspol =
        TestUtils.getResource(ISSUANCE_POLICY_FILE, IssuancePolicy.class, this, true);
    AttributeList il =
        TestUtils.getResource(ISSUER_ATTRIBUTES_FILE, AttributeList.class, this, true);

    runTest(ptd, isspol, il.getAttributes(), testName);
  }

  private void loadAndStoreKeys(String groupName, String csName, String testName)
      throws Exception {
    // Load
    RealTestVectorHelper tvHelperForSp = new RealTestVectorHelper(bigIntFactory);
    tvHelperForSp.loadResource("group-" + groupName + ".txt", getClass());
    tvHelperForSp.loadResource("issuerKey.txt", getClass());
    EcryptSystemParametersWrapper spw =
        Helper.generateSystemParameters(tvHelperForSp, bigIntFactory);
    SystemParameters sp = spw.getSystemParameters();
    BrandsKeyPairWrapper kpw = Helper.generateKeyPair(spw, tvHelperForSp, bigIntFactory);
    IssuerParametersFacade ipf =
        IssuerParametersFacade.initIssuerParameters(kpw.getIssuerPublicKeyWrapper().getPublicKey(),
            sp);
    IssuerParameters ip = ipf.getIssuerParameters();
    Abc4TrustSecretKeyFacade isf = new Abc4TrustSecretKeyFacade();
    isf.setPrivateKey(kpw.getUProvePrivateKeyWrapper().getSecretKey());
    isf.setKeyId(ip.getParametersUID());
    SecretKey sk = isf.getSecretKey();
    CredentialSpecification cs =
        TestUtils.getResource(csName, CredentialSpecification.class, this, true);
    
    // Store in key manager and credential manager
    AbcComponent injector = TestInitialisation.INJECTOR;
    KeyManager km = injector.providesKeyManager();
    km.storeSystemParameters(sp);
    km.storeIssuerParameters(ip.getParametersUID(), ip);
    km.storeCredentialSpecification(cs.getSpecificationUID(), cs);
    CredentialManager icm = injector.providesCredentialManagerIssuer();
    icm.storeIssuerSecretKey(ip.getParametersUID(), sk);
    
    // save
    saveToFile(of.createSystemParameters(sp), testName, "systemParameters");
    saveToFile(of.createIssuerParameters(ip), testName, "issuerParameters");
    saveToFile(of.createIssuerSecretKey(sk), testName, "issuerSecretKey");
    saveToFile(of.createCredentialSpecification(cs), testName, "credentialSpecification");
  }

  private void runTest(PresentationTokenDescription ptd, IssuancePolicy isspol, List<Attribute> al,
      String testName) throws Exception {
    // Issue
    Credential c = issue(isspol, al);
    saveToFile(of.createCredential(c), testName, "credential");

    // Present
    PresentationToken pt =
        ceu.createPresentationToken(USERNAME, ptd, null,
            Collections.singletonList(c.getCredentialDescription().getCredentialUID()),
            Collections.<URI>emptyList());
    boolean res = cev.verifyToken(pt, null);
    assertTrue(res);

    saveToFile(of.createPresentationToken(pt), testName, "presentationToken");
  }

  private Credential issue(IssuancePolicy isspol, List<Attribute> al) throws CryptoEngineException,
      CredentialManagerException {
    IssuanceMessageAndBoolean imab = cei.initIssuanceProtocol(isspol, al, URI.create("bla"));
    IssuMsgOrCredDesc imocd = ceu.issuanceProtocolStep(USERNAME, imab.getIssuanceMessage());
    imab = cei.issuanceProtocolStep(imocd.im);
    imocd = ceu.issuanceProtocolStep(USERNAME, imab.getIssuanceMessage());
    assertNotNull(imocd.cd);
    eu.abc4trust.abce.internal.user.credentialManager.CredentialManager cmu =
        TestInitialisation.INJECTOR.providesCredentialManagerUser();

    Credential c = cmu.getCredential(USERNAME, imocd.cd.getCredentialUID());
    return c;
  }

  private <T> void saveToFile(JAXBElement<T> el, String test, String filename) throws Exception {
    String xml = JaxbHelperClass.serialize(el);
    TestUtils.saveToFile(
        xml,
        TestSystemParameters.BASE_LOCATION.resolve(
            "com/ibm/zurich/idmx/uProveCompatibility/" + test + "/" + filename + ".xml").toString());
    System.out.println("=== " + test + " === " + filename);
    System.out.println(xml);
    System.out.println();
  }
}
