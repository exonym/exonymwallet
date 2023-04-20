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

import org.junit.Before;
import org.junit.Test;

import com.ibm.zurich.idmx.exception.SerializationException;
import com.ibm.zurich.idmx.tests.TestUtils;

import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.xml.Credential;

public class IssuanceTest {

  private GeneralHelper helper;

  @Before
  public void setUp() throws Exception {
    helper = new GeneralHelper();
    helper.setupIssuance(true);

    helper.loadDefaultResources();


    // SecretKey sk = TestUtils.getResource("sk-cl.xml", SecretKey.class, this, true);
    // issuer_cm.storeIssuerSecretKey(sk.getSecretKeyUID(), sk);

    // sk = TestUtils.getResource("sk-brands.xml", SecretKey.class, this, true);
    // issuer_cm.storeIssuerSecretKey(sk.getSecretKeyUID(), sk);


    // Injector injector_issuer = Guice.createInjector(new CryptoTestModule());
    // issuer_ce = injector_issuer.getInstance(CryptoEngineIssuer.class);
    // issuer_km = injector_issuer.getInstance(KeyManager.class);
    // issuer_cm =
    // injector_issuer
    // .getInstance(eu.abc4trust.abce.internal.issuer.credentialManager.CredentialManager.class);
    //
    // Injector injector_user = Guice.createInjector(new CryptoTestModule());
    // user_ce = injector_user.getInstance(CryptoEngineUser.class);
    // user_km = injector_user.getInstance(KeyManager.class);
    // user_cm =
    // injector_user
    // .getInstance(eu.abc4trust.abce.internal.user.credentialManager.CredentialManager.class);
    //
    // deviceManager = injector_user.getInstance(ExternalSecretsManager.class);

    // Load resources
    // issuer_km.storeSystemParameters(sp);
    // user_km.storeSystemParameters(sp);

    // issuer_km.storeIssuerParameters(ip.getParametersUID(), ip);
    // user_km.storeIssuerParameters(ip.getParametersUID(), ip);

    // issuer_km.storeIssuerParameters(ip.getParametersUID(), ip);
    // user_km.storeIssuerParameters(ip.getParametersUID(), ip);



    // CredentialSpecification cs =
    // TestUtils.getResource("cs-simple.xml", CredentialSpecification.class, this, true);
    // issuer_km.storeCredentialSpecification(cs.getSpecificationUID(), cs);
    // user_km.storeCredentialSpecification(cs.getSpecificationUID(), cs);
    //
    // cs = TestUtils.getResource("cs-device.xml", CredentialSpecification.class, this, true);
    // issuer_km.storeCredentialSpecification(cs.getSpecificationUID(), cs);
    // user_km.storeCredentialSpecification(cs.getSpecificationUID(), cs);
  }


  private void loadIdmxIssuer() throws SerializationException, KeyManagerException,
      CredentialManagerException {
    helper.loadIssuerResources("ip-cl.xml", "sk-cl.xml");
  }

  private void loadBrandsIssuer() throws SerializationException, KeyManagerException,
      CredentialManagerException {
    helper.loadIssuerResources("ip-brands.xml", "sk-brands.xml");
  }



  @Test
  public void testSimpleIssuanceCl() throws Exception {
    loadIdmxIssuer();
    helper.runSimpleIssuance(URI.create("cl"));
  }

  @Test
  public void testSimpleIssuanceBrands() throws Exception {

    loadBrandsIssuer();
    helper.runSimpleIssuance(URI.create("brands"));
  }

  @Test
  public void testCarryOverClToCl() throws Exception {
    Credential c =
        TestUtils.getResource("general/credentials/credential-simple-cl.xml", Credential.class,
            this, true);
    loadIdmxIssuer();
    helper.runComplexIssuance(URI.create("cl"), c);
  }

  @Test
  public void testCarryOverBrandsToBrands() throws Exception {
    Credential c =
        TestUtils.getResource("general/credentials/credential-simple-brands.xml", Credential.class,
            this, true);
    loadBrandsIssuer();
    helper.runComplexIssuance(URI.create("brands"), c);
  }

  @Test
  public void testCarryOverClToBrands() throws Exception {
    Credential c =
        TestUtils.getResource("general/credentials/credential-simple-cl.xml", Credential.class,
            this, true);
    loadBrandsIssuer();
    helper.runComplexIssuance(URI.create("brands"), c);
  }

  @Test
  public void testCarryOverBrandsToCl() throws Exception {
    Credential c =
        TestUtils.getResource("general/credentials/credential-simple-brands.xml", Credential.class,
            this, true);
    loadIdmxIssuer();
    helper.runComplexIssuance(URI.create("cl"), c);
  }

  @Test
  public void testCarryOverClToDeviceCl() throws Exception {
    Credential c =
        TestUtils.getResource("general/credentials/credential-simple-cl.xml", Credential.class,
            this, true);
    loadIdmxIssuer();
    helper.runDeviceIssuance(URI.create("cl"), c);
  }

  @Test
  public void testCarryOverBrandsToDeviceBrands() throws Exception {
    Credential c =
        TestUtils.getResource("general/credentials/credential-simple-brands.xml", Credential.class,
            this, true);
    loadBrandsIssuer();
    helper.runDeviceIssuance(URI.create("brands"), c);
  }

  @Test
  public void testCarryOverClToDeviceBrands() throws Exception {
    Credential c =
        TestUtils.getResource("general/credentials/credential-simple-cl.xml", Credential.class,
            this, true);
    loadBrandsIssuer();
    helper.runDeviceIssuance(URI.create("brands"), c);
  }

  @Test
  public void testCarryOverBrandsToDeviceCl() throws Exception {
    Credential c =
        TestUtils.getResource("general/credentials/credential-simple-brands.xml", Credential.class,
            this, true);
    loadIdmxIssuer();
    helper.runDeviceIssuance(URI.create("cl"), c);
  }

  @Test
  public void testCarryOverDeviceBrandsToDeviceCl() throws Exception {
    Credential c =
        TestUtils.getResource("general/credentials/credential-complex-device-brands-to-brands.xml",
            Credential.class, this, true);
    loadIdmxIssuer();
    helper.runDeviceIssuanceNoNym(URI.create("cl"), c);
  }

  @Test
  public void testCarryOverDeviceBrandsToDeviceBrands() throws Exception {
    Credential c =
        TestUtils.getResource("general/credentials/credential-complex-device-brands-to-brands.xml",
            Credential.class, this, true);
    loadBrandsIssuer();
    helper.runDeviceIssuanceNoNym(URI.create("brands"), c);
  }

  @Test
  public void testCarryOverDeviceClToDeviceCl() throws Exception {
    Credential c =
        TestUtils.getResource("general/credentials/credential-complex-device-cl-to-cl.xml",
            Credential.class, this, true);
    loadIdmxIssuer();
    helper.runDeviceIssuanceNoNym(URI.create("cl"), c);
  }

  @Test
  public void testCarryOverDeviceClToDeviceBrands() throws Exception {
    Credential c =
        TestUtils.getResource("general/credentials/credential-complex-device-cl-to-cl.xml",
            Credential.class, this, true);
    loadBrandsIssuer();
    helper.runDeviceIssuanceNoNym(URI.create("brands"), c);
  }

  // ---
  @Test
  public void testCarryOverDeviceBrandsToCl() throws Exception {
    Credential c =
        TestUtils.getResource("general/credentials/credential-complex-device-brands-to-brands.xml",
            Credential.class, this, true);
    loadIdmxIssuer();
    helper.runComplexIssuance(URI.create("cl"), c);
  }

  @Test
  public void testCarryOverDeviceBrandsToBrands() throws Exception {
    Credential c =
        TestUtils.getResource("general/credentials/credential-complex-device-brands-to-brands.xml",
            Credential.class, this, true);
    loadBrandsIssuer();
    helper.runComplexIssuance(URI.create("brands"), c);
  }

  @Test
  public void testCarryOverDeviceClToCl() throws Exception {
    Credential c =
        TestUtils.getResource("general/credentials/credential-complex-device-cl-to-cl.xml",
            Credential.class, this, true);
    loadIdmxIssuer();
    helper.runComplexIssuance(URI.create("cl"), c);
  }

  @Test
  public void testCarryOverDeviceClToBrands() throws Exception {
    Credential c =
        TestUtils.getResource("general/credentials/credential-complex-device-cl-to-cl.xml",
            Credential.class, this, true);
    loadBrandsIssuer();
    helper.runComplexIssuance(URI.create("brands"), c);
  }

}
