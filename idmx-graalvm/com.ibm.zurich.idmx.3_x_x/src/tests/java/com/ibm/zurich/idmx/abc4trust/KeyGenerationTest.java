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
package com.ibm.zurich.idmx.abc4trust;

import java.net.URI;
import java.util.Collections;
import java.util.List;

import com.ibm.zurich.idmx.dagger.AbcComponent;
import com.ibm.zurich.idmx.tests.TestInitialisation;
import org.junit.Before;
import org.junit.Test;

import com.ibm.zurich.idmx.buildingBlock.rangeProof.SafeRSAGroupInVerifierParameters;
import com.ibm.zurich.idmx.buildingBlock.rangeProof.SafeRSAGroupInVerifierParameters.GroupDescription;
import com.ibm.zurich.idmx.jaxb.JaxbHelperClass;
import com.ibm.zurich.idmx.tests.TestUtils;
import com.ibm.zurich.idmx.tests.setup.TestSystemParameters;

import eu.abc4trust.cryptoEngine.inspector.CryptoEngineInspector;
import eu.abc4trust.cryptoEngine.issuer.CryptoEngineIssuer;
import eu.abc4trust.cryptoEngine.verifier.CryptoEngineVerifier;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.returnTypes.InspectorPublicAndSecretKey;
import eu.abc4trust.returnTypes.IssuerParametersAndSecretKey;
import eu.abc4trust.xml.FriendlyDescription;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.SecretKey;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.VerifierParameters;

public class KeyGenerationTest {
  private CryptoEngineIssuer cei;
  private KeyManager keyManager;
  private CryptoEngineInspector ceInspector;
  private CryptoEngineVerifier cev;
  private SafeRSAGroupInVerifierParameters safeRsa;

  @Before
  public void setUp() {
    AbcComponent inj = TestInitialisation.INJECTOR;
//    Injector injector = Guice.createInjector(new CryptoTestModule());

//    cei = injector.getInstance(CryptoEngineIssuer.class);
//    cev = injector.getInstance(CryptoEngineVerifier.class);
//    ceInspector = injector.getInstance(CryptoEngineInspector.class);
//    keyManager = injector.getInstance(KeyManager.class);
//    safeRsa = injector.getInstance(SafeRSAGroupInVerifierParameters.class);
    cei = inj.providesCryptoEngineIssuerAbc();
    cev = inj.providesCryptoEngineVerifierAbc();
    ceInspector = inj.providesCryptoEngineInspectorAbc();
    keyManager = inj.providesKeyManager();
    safeRsa = inj.provideBuildingBlockFactory().getSafeRSAGroupInVerifierParameters();


  }

  @Test
  public void testSystemParameterGeneration() throws Exception {
    generateSystemParameters();
  }

  @Test
  public void testIdemixIssuerKeyGeneration() throws Exception {
    SystemParameters sp = getSystemParameters();
    generateIssuerKey(sp, URI.create("cl"));
  }

//  @Test
//  public void testBrandsIssuerKeyGeneration() throws Exception {
//    SystemParameters sp = getSystemParameters();
//    generateIssuerKey(sp, URI.create("brands"));
//  }
  
  @Test
  public void testVerifierParameterGeneration() throws Exception {
    SystemParameters sp = getSystemParameters();
    getClIssuerParameters();
    
    VerifierParameters vp = cev.createVerifierParameters(sp);
    
    String vp_xml =
        JaxbHelperClass.serialize((new ObjectFactory()).createVerifierParameters(vp), true);
    System.out.println(vp_xml);
    TestUtils.saveToFile(vp_xml, TestSystemParameters.BASE_LOCATION.resolve("abc4trust/vp.xml")
        .toString());
    
    GroupDescription gd = safeRsa.getGroupDescription(vp);
    System.out.println(gd.group.toString());
  }
  
  @Test
  public void testCsInspectorKeyGeneration() throws Exception {
    SystemParameters sp = getSystemParameters();
    
    URI technology = URI.create("idemix");
    URI uid = URI.create("iss-" + technology);
    
    List<FriendlyDescription> friendlyDescription = Collections.emptyList();
    InspectorPublicAndSecretKey keyPair = 
    ceInspector.setupInspectorPublicKey(sp, technology, uid, friendlyDescription);
    
    String ip_xml =
        JaxbHelperClass.serialize((new ObjectFactory()).createInspectorPublicKey(keyPair.publicKey), true);
    System.out.println(ip_xml);
    TestUtils.saveToFile(ip_xml,
        TestSystemParameters.BASE_LOCATION.resolve("abc4trust/inspector-" + technology + ".xml")
            .toString());

    String sk_xml =
        JaxbHelperClass.serialize((new ObjectFactory()).createInspectorSecretKey(keyPair.secretKey), true);
    System.out.println(sk_xml);
    TestUtils.saveToFile(sk_xml,
        TestSystemParameters.BASE_LOCATION.resolve("abc4trust/inspector-sk-" + technology + ".xml")
            .toString());
  }

  private SystemParameters generateSystemParameters() throws Exception {
    final int keySize = 600;
    SystemParameters sp = cei.setupSystemParameters(keySize);
    String sp_xml =
        JaxbHelperClass.serialize((new ObjectFactory()).createSystemParameters(sp), true);
    // System.out.println(sp_xml);
    TestUtils.saveToFile(sp_xml, TestSystemParameters.BASE_LOCATION.resolve("abc4trust/sp.xml")
        .toString());
    return sp;
  }

  private SystemParameters getSystemParameters() throws Exception {
    String sp_xml =
        TestUtils.loadFromFile(TestSystemParameters.BASE_LOCATION.resolve("abc4trust/sp.xml")
            .toString());
    SystemParameters sp;
    if (sp_xml == null) {
      sp = generateSystemParameters();
    } else {
      sp = (SystemParameters) JaxbHelperClass.deserialize(sp_xml, true).getValue();
    }
    keyManager.storeSystemParameters(sp);
    return sp;
  }
  
  private IssuerParameters getClIssuerParameters() throws Exception {
    String ip_xml =
        TestUtils.loadFromFile(TestSystemParameters.BASE_LOCATION.resolve("abc4trust/ip-cl.xml")
            .toString());
    IssuerParameters ip;
    if (ip_xml == null) {
      SystemParameters sp = getSystemParameters();
      ip = generateIssuerKey(sp, URI.create("cl")).issuerParameters;
    } else {
      ip = (IssuerParameters) JaxbHelperClass.deserialize(ip_xml, true).getValue();
    }
    System.out.println("Storing Issuer Params" + ip);
    keyManager.storeIssuerParameters(ip.getParametersUID(), ip);
    return ip;
  }

  private IssuerParametersAndSecretKey generateIssuerKey(SystemParameters sp, URI technology)
      throws Exception {
    int maximalNumberOfAttributes = 10;
    URI uid = technology;
    URI revocationAuthority = null;
    List<FriendlyDescription> friendlyIssuerDescription = Collections.emptyList();

    IssuerParametersAndSecretKey ip_sk =
        cei.setupIssuerParameters(sp, maximalNumberOfAttributes, technology, uid,
            revocationAuthority, friendlyIssuerDescription);

    IssuerParameters ip = ip_sk.issuerParameters;
    String ip_xml =
        JaxbHelperClass.serialize((new ObjectFactory()).createIssuerParameters(ip), true);
    // System.out.println(ip_xml);
    TestUtils.saveToFile(ip_xml,
        TestSystemParameters.BASE_LOCATION.resolve("abc4trust/ip-" + technology + ".xml")
            .toString());

    SecretKey sk = ip_sk.issuerSecretKey;
    String sk_xml =
        JaxbHelperClass.serialize((new ObjectFactory()).createIssuerSecretKey(sk), true);
    // System.out.println(sk_xml);
    TestUtils.saveToFile(sk_xml,
        TestSystemParameters.BASE_LOCATION.resolve("abc4trust/sk-" + technology + ".xml")
            .toString());

    return ip_sk;
  }
}
