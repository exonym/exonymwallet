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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.ibm.zurich.idmix.abc4trust.facades.RevocationAuthorityParametersFacade;
import com.ibm.zurich.idmix.abc4trust.facades.SecretKeyFacade;
import com.ibm.zurich.idmx.dagger.AbcComponent;
import com.ibm.zurich.idmx.configuration.Configuration;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.SerializationException;
import com.ibm.zurich.idmx.interfaces.device.ExternalSecretsManager;
import com.ibm.zurich.idmx.jaxb.JaxbHelperClass;
import com.ibm.zurich.idmx.tests.TestInitialisation;
import com.ibm.zurich.idmx.tests.TestUtils;
import com.ibm.zurich.idmx.tests.setup.TestSystemParameters;

import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.cryptoEngine.inspector.CryptoEngineInspector;
import eu.abc4trust.cryptoEngine.issuer.CryptoEngineIssuer;
import eu.abc4trust.cryptoEngine.revocation.CryptoEngineRevocation;
import eu.abc4trust.cryptoEngine.user.CryptoEngineUser;
import eu.abc4trust.cryptoEngine.verifier.CryptoEngineVerifier;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.returnTypes.IssuMsgOrCredDesc;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.AttributeList;
import eu.abc4trust.xml.Credential;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.InspectorPublicKey;
import eu.abc4trust.xml.IssuanceMessageAndBoolean;
import eu.abc4trust.xml.IssuancePolicy;
import eu.abc4trust.xml.IssuanceTokenAndIssuancePolicy;
import eu.abc4trust.xml.IssuanceTokenDescription;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.KeyPair;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.PresentationTokenDescription;
import eu.abc4trust.xml.PseudonymWithMetadata;
import eu.abc4trust.xml.RevocationAuthorityParameters;
import eu.abc4trust.xml.RevocationInformation;
import eu.abc4trust.xml.SecretKey;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.VerifierParameters;


/**
 * 
 */
public class GeneralHelper {

  protected CryptoEngineVerifier verifier_ce;
  protected CryptoEngineUser user_ce;
  protected CryptoEngineInspector inspector_ce;
  protected CryptoEngineIssuer issuer_ce;
  protected CryptoEngineRevocation ra_ce;

  protected final List<KeyManager> keyManagers;

  protected ExternalSecretsManager user_deviceManager;

  protected eu.abc4trust.abce.internal.user.credentialManager.CredentialManager user_cm;
  protected eu.abc4trust.abce.internal.inspector.credentialManager.CredentialManager inspector_cm;
  protected eu.abc4trust.abce.internal.issuer.credentialManager.CredentialManager issuer_cm;
  protected eu.abc4trust.abce.internal.revocation.credentialManager.CredentialManager ra_cm;
  private static final String USERNAME = "user";
  
  protected VerifierParameters verifierParameters;


  public GeneralHelper() {
    keyManagers = new ArrayList<KeyManager>();
  }


  public void loadDefaultResources() throws SerializationException, KeyManagerException,
      CredentialManagerException, ConfigurationException, CryptoEngineException {
    // Load resources
    loadSystemParamters("sp.xml");

    loadIssuerParamters("ip-cl.xml");
    loadIssuerParamters("ip-brands.xml");

    loadCredentialSpecification("cs-simple.xml");
    loadCredentialSpecification("cs-device.xml");
    
    verifierParameters = verifier_ce.createVerifierParameters(keyManagers.get(0).getSystemParameters());
  }


  public String resolveDefaultPath(String filename) {
    return "general/" + filename;
  }

  /**
   * Returns an injector for the issuer (and sets up a crypto engine, key manager and credential
   * manager).
   */
  private AbcComponent setupIssuer() {
    AbcComponent inj = TestInitialisation.INJECTOR;
//    Injector injector = Guice.createInjector(new CryptoTestModule());
    issuer_ce = inj.providesCryptoEngineIssuerAbc();
    keyManagers.add(inj.providesKeyManager());
    issuer_cm = inj.providesCredentialManagerIssuer();
//        injector
//            .getInstance(eu.abc4trust.abce.internal.issuer.credentialManager.CredentialManager.class);
    // TODO(enr): In tests, the revocation handle will be queried from the Revocation Authority
    // that was generated from the issuer's injector, thus the RA and the issuer must share the
    // same Guice-scope.
    ra_cm =inj.providesCredentialManagerRevocation();
//        injector
//            .getInstance(eu.abc4trust.abce.internal.revocation.credentialManager.CredentialManager.class);
    ra_ce = inj.providesCryptoEngineRevocationAbc();

    return inj;
  }


  /**
   * Returns an injector for the inspector (and sets up a crypto engine, key manager and credential
   * manager).
   */
  private AbcComponent setupInspector() {
    AbcComponent inj = TestInitialisation.INJECTOR;
//    Injector injector = Guice.createInjector(new CryptoTestModule());
    inspector_ce = inj.providesCryptoEngineInspectorAbc();
    keyManagers.add(inj.providesKeyManager());
    inspector_cm = inj.providesCredentialManagerInspector();
//        injector
//            .getInstance(eu.abc4trust.abce.internal.inspector.credentialManager.CredentialManager.class);

    return inj;
  }

  /**
   * Returns an injector for the revocation authority (and sets up a crypto engine, key manager and
   * credential manager).
   */
  private AbcComponent setupRevocationAuthority() {
    // See comment at issuer
    if (issuer_ce == null) {
      return setupIssuer();
    }
    return null;
  }

  /**
   * Returns an injector for the user (and sets up a crypto engine, key manager, credential manager
   * and device manager).
   */
  private AbcComponent setupUser() {
    AbcComponent inj = TestInitialisation.INJECTOR;
//    Injector injector = Guice.createInjector(new CryptoTestModule());
    user_ce = inj.provideCryptoEngineUser();
    keyManagers.add(inj.providesKeyManager());
    user_cm = inj.providesCredentialManagerUser();

    user_deviceManager = inj.provideBuildingBlockFactory().getExternalSecretsManager();
    return inj;
  }

  /**
   * Returns the injector for the verifier (including key manager and crypto engine).
   */
  private AbcComponent setupVerifier() {
    AbcComponent inj = TestInitialisation.INJECTOR;
//    Injector injector = Guice.createInjector(new CryptoTestModule());
    verifier_ce = inj.providesCryptoEngineVerifierAbc(); // injector.getInstance(CryptoEngineVerifier.class);
    keyManagers.add(inj.providesKeyManager()); //injector.getInstance(KeyManager.class));
    return inj;
  }

  /**
   * @throws KeyManagerException
   */
  protected void loadSystemParamters(String fileName) throws SerializationException,
      KeyManagerException {
    SystemParameters sp =
        TestUtils.getResource(resolveDefaultPath(fileName), SystemParameters.class, this, true);

    for (KeyManager keyManager : keyManagers) {
      keyManager.storeSystemParameters(sp);
    }
  }

  /**
   * @throws KeyManagerException
   * 
   */
  protected void loadIssuerParamters(String fileName) throws SerializationException,
      KeyManagerException {
    IssuerParameters ip =
        TestUtils.getResource(resolveDefaultPath(fileName), IssuerParameters.class, this, true);

    for (KeyManager keyManager : keyManagers) {
      keyManager.storeIssuerParameters(ip.getParametersUID(), ip);
    }
  }

  /**
   * @throws KeyManagerException
   * 
   */
  protected void loadRevocationParameters(RevocationAuthorityParameters raParameters)
      throws SerializationException, KeyManagerException {

    for (KeyManager keyManager : keyManagers) {
      keyManager.storeRevocationAuthorityParameters(raParameters.getParametersUID(), raParameters);
    }
  }

  /**
   * @throws KeyManagerException
   * 
   */
  protected void loadCredentialSpecification(String fileName) throws SerializationException,
      KeyManagerException {
    CredentialSpecification credSpec =
        TestUtils.getResource(resolveDefaultPath(fileName), CredentialSpecification.class, this,
            true);

    for (KeyManager keyManager : keyManagers) {
      keyManager.storeCredentialSpecification(credSpec.getSpecificationUID(), credSpec);
    }
  }


  /**
   * @throws SerializationException
   * @throws KeyManagerException
   */
  private void loadInspectorPublicKey(String fileName) throws SerializationException,
      KeyManagerException {
    InspectorPublicKey insPk =
        TestUtils.getResource(resolveDefaultPath(fileName), InspectorPublicKey.class, this, true);

    for (KeyManager keyManager : keyManagers) {
      keyManager.storeInspectorPublicKey(insPk.getPublicKeyUID(), insPk);
    }
  }


  void setupIssuance(boolean includingRevocation) {

    setupIssuer();
    setupVerifier();
    setupUser();

    if (includingRevocation) {
      setupRevocationAuthority();
    }
  }

  void setupPresentation(boolean includingInspection, boolean includingRevocation) {

    setupVerifier();

    setupUser();

    if (includingInspection) {
      setupInspector();
    }

    if (includingRevocation) {
      setupRevocationAuthority();
    }

  }

  void loadRevocationResources(String keyPairFileName) throws SerializationException,
      KeyManagerException, CredentialManagerException, ConfigurationException,
      CryptoEngineException {


    // Load revocation authority keys
    KeyPair keyPair =
        TestUtils.getResource(resolveDefaultPath(keyPairFileName), KeyPair.class, this, false);
    RevocationAuthorityParametersFacade raParametersFacade =
        RevocationAuthorityParametersFacade.initRevocationAuthorityParameters(keyPair
            .getPublicKey());
    URI raParametersId = raParametersFacade.getRevocationAuthorityParametersId();
    raParametersFacade.setRevocationAuthorityParametersId(raParametersId);

    // Load the public key
    loadRevocationParameters(raParametersFacade.getRevocationAuthorityParameters());


    SecretKeyFacade skFacade =
        SecretKeyFacade.initSecretKey(raParametersId, keyPair.getPrivateKey());
    skFacade.getSecretKey().setSecretKeyUID(raParametersId);

    ra_cm.storeSecretKey(raParametersId, skFacade.getSecretKey());
    RevocationInformation rInfo = ra_ce.updateRevocationInformation(raParametersId);
    for (KeyManager km : keyManagers) {
      km.storeRevocationInformation(raParametersId, rInfo);
    }
    System.out.println(rInfo);
  }


  void loadIssuerResources(String publicKeyFileName, String secretKeyFileName)
      throws SerializationException, KeyManagerException, CredentialManagerException {

    // Load the public key
    loadIssuerParamters(publicKeyFileName);

    SecretKey issuerSk =
        TestUtils.getResource(resolveDefaultPath(secretKeyFileName), SecretKey.class, this, true);
    issuer_cm.storeIssuerSecretKey(issuerSk.getSecretKeyUID(), issuerSk);
  }

  void loadInspectorResources(String publicKeyFileName, String secretKeyFileName)
      throws SerializationException, KeyManagerException,
      eu.abc4trust.abce.internal.inspector.credentialManager.CredentialManagerException {

    // Load the public key
    loadInspectorPublicKey(publicKeyFileName);

    SecretKey insSk =
        TestUtils.getResource(resolveDefaultPath(secretKeyFileName), SecretKey.class, this, true);
    inspector_cm.storeInspectorSecretKey(insSk.getSecretKeyUID(), insSk);
  }

  void loadCredential(Credential cred) throws CredentialManagerException {
    user_cm.storeCredential(USERNAME, cred);
  }

  /**
   * Saves the credential locally of it does not exist - otherwise it saves it to a default
   * location.
   */
  private void saveCredentialToFile(String c_xml, String credentialName) throws IOException {

    String credentialPath = resolveDefaultPath("credentials/" + credentialName);

    String currentPath = System.getProperty("user.dir").replace("\\", "/");
    String packagePath = "src/tests/resources/com/ibm/zurich/idmx/abc4trust";
    currentPath = currentPath + "/" + packagePath;
    credentialPath = currentPath + "/" + credentialPath;

    File file = new File(credentialPath);
    if (file.exists()) {
      credentialPath =
          TestSystemParameters.BASE_LOCATION.resolve("abc4trust/" + credentialName).toString();
    } else {
      credentialPath = file.toURI().toString();
    }
    TestUtils.saveToFile(c_xml, credentialPath);
  }

  /**
   * Runs the issuance protocol using the entities as initialised.
   */
  public Credential runSimpleIssuance(URI issuerUri) throws Exception, CryptoEngineException {
    IssuancePolicy policy =
        TestUtils.getResource(resolveDefaultPath("policy-scratch.xml"), IssuancePolicy.class, this,
            true);
    // TODO remove this hack
    policy.getCredentialTemplate().setIssuerParametersUID(issuerUri);
    AttributeList issuerAttributes =
        TestUtils.getResource(resolveDefaultPath("atts-all.xml"), AttributeList.class, this, true);

    IssuanceMessageAndBoolean imab;
    IssuMsgOrCredDesc imcd;
    imab = issuer_ce.initIssuanceProtocol(policy, issuerAttributes.getAttributes(), null);

    // This is simple issuance, user doesn't have to generate an issuance token
    while (!imab.isLastMessage()) {
      imcd = user_ce.issuanceProtocolStep(USERNAME, imab.getIssuanceMessage());
      imab = issuer_ce.issuanceProtocolStep(imcd.im);
    }
    imcd = user_ce.issuanceProtocolStep(USERNAME, imab.getIssuanceMessage());

    assertNotNull(imcd.cd);
    assertNull(imcd.im);

    Credential c = user_cm.getCredential(USERNAME, imcd.cd.getCredentialUID());

    String c_xml = JaxbHelperClass.serialize((new ObjectFactory()).createCredential(c), true);
    System.out.println(c_xml);
    String credentialName = "credential-simple-" + issuerUri + ".xml";

    saveCredentialToFile(c_xml, credentialName);

    return c;
  }

  public Credential runComplexIssuance(URI issuerUri, Credential sourceCred) throws Exception,
      CryptoEngineException {
    URI sourceUri = sourceCred.getCredentialDescription().getCredentialUID();
    URI sourceIssuer = sourceCred.getCredentialDescription().getIssuerParametersUID();
    URI sourceCredSpec = sourceCred.getCredentialDescription().getCredentialSpecificationUID();
    URI sourceSecret = sourceCred.getCredentialDescription().getSecretReference();

    if (sourceSecret != null) {
      user_deviceManager.allocateCredential(USERNAME, sourceSecret, sourceUri, sourceIssuer, true);
    }

    IssuancePolicy policy =
        TestUtils.getResource(resolveDefaultPath("policy-carryover.xml"), IssuancePolicy.class,
            this, true);
    policy.getCredentialTemplate().setIssuerParametersUID(issuerUri);
    policy.getPresentationPolicy().getCredential().get(0).getIssuerAlternatives()
        .getIssuerParametersUID().get(0).setValue(sourceIssuer);
    policy.getPresentationPolicy().getCredential().get(0).getCredentialSpecAlternatives()
        .getCredentialSpecUID().set(0, sourceCredSpec);

    IssuanceTokenDescription itd =
        TestUtils.getResource(resolveDefaultPath("token-carryover.xml"),
            IssuanceTokenDescription.class, this, true);
    itd.setCredentialTemplate(policy.getCredentialTemplate());
    itd.getPresentationTokenDescription().getCredential().get(0)
        .setIssuerParametersUID(sourceIssuer);
    itd.getPresentationTokenDescription().getCredential().get(0)
        .setCredentialSpecUID(sourceCredSpec);

    List<Attribute> issuerAttributes = Collections.emptyList();

    IssuanceMessageAndBoolean imab;
    IssuMsgOrCredDesc imcd = new IssuMsgOrCredDesc();
    imab = issuer_ce.initIssuanceProtocol(policy, issuerAttributes, null);

    // Complex issuance requires pre-issuance presentation
    user_cm.storeCredential(USERNAME, sourceCred);
    List<URI> creds = Collections.singletonList(sourceUri);
    List<URI> nyms = Collections.emptyList();
    List<Attribute> atts = Collections.emptyList();
    imcd.im = user_ce.createIssuanceToken(USERNAME, imab.getIssuanceMessage(), itd, creds, nyms, atts);

    // Issuer needs to check if token satisfies policy
    IssuanceTokenAndIssuancePolicy itip = issuer_ce.extractIssuanceTokenAndPolicy(imcd.im);
    assertEquals(JaxbHelperClass.serialize(new ObjectFactory().createIssuancePolicy(policy)), JaxbHelperClass.serialize(new ObjectFactory().createIssuancePolicy(itip.getIssuancePolicy())));
    assertEquals(JaxbHelperClass.serialize(new ObjectFactory().createIssuanceTokenDescription(itd),
        true), JaxbHelperClass.serialize(new ObjectFactory().createIssuanceTokenDescription(itip
        .getIssuanceToken().getIssuanceTokenDescription()), true));

    while (!imab.isLastMessage()) {
      imab = issuer_ce.issuanceProtocolStep(imcd.im);
      imcd = user_ce.issuanceProtocolStep(USERNAME, imab.getIssuanceMessage());
    }

    assertNotNull(imcd.cd);
    assertNull(imcd.im);

    Credential cNew = user_cm.getCredential(USERNAME, imcd.cd.getCredentialUID());

    String c_xml = JaxbHelperClass.serialize((new ObjectFactory()).createCredential(cNew), true);
    System.out.println(c_xml);
    String credentialName =
        "credential-complex-" + sourceCred.getCredentialDescription().getIssuerParametersUID()
            + "-to-" + issuerUri + ".xml";

    saveCredentialToFile(c_xml, credentialName);

    return cNew;
  }



  public Credential runDeviceIssuance(URI issuerUri, Credential sourceCred) throws Exception,
      CryptoEngineException {
    URI sourceUri = sourceCred.getCredentialDescription().getCredentialUID();
    URI sourceIssuer = sourceCred.getCredentialDescription().getIssuerParametersUID();
    URI sourceCredSpec = sourceCred.getCredentialDescription().getCredentialSpecificationUID();

    IssuancePolicy policy =
        TestUtils.getResource(resolveDefaultPath("policy-devicecarryover.xml"),
            IssuancePolicy.class, this, true);
    policy.getCredentialTemplate().setIssuerParametersUID(issuerUri);
    policy.getPresentationPolicy().getCredential().get(0).getIssuerAlternatives()
        .getIssuerParametersUID().get(0).setValue(sourceIssuer);
    policy.getPresentationPolicy().getCredential().get(0).getCredentialSpecAlternatives()
        .getCredentialSpecUID().set(0, sourceCredSpec);

    String scope = policy.getPresentationPolicy().getPseudonym().get(0).getScope();
    boolean exclusive = policy.getPresentationPolicy().getPseudonym().get(0).isExclusive();
    URI secret = URI.create("secret");
    URI pseudonymUri = URI.create("nym");
    PseudonymWithMetadata pwm = user_ce.createPseudonym(USERNAME, pseudonymUri, scope, exclusive, secret);
    String nym_xml =
        JaxbHelperClass.serialize((new ObjectFactory()).createPseudonymWithMetadata(pwm));
    System.out.println(nym_xml);
    user_cm.storePseudonym(USERNAME, pwm);

    IssuanceTokenDescription itd =
        TestUtils.getResource(resolveDefaultPath("token-devicecarryover.xml"),
            IssuanceTokenDescription.class, this, true);
    itd.setCredentialTemplate(policy.getCredentialTemplate());
    itd.getPresentationTokenDescription().getCredential().get(0)
        .setIssuerParametersUID(sourceIssuer);
    itd.getPresentationTokenDescription().getPseudonym().get(0)
        .setPseudonymValue(pwm.getPseudonym().getPseudonymValue());
    itd.getPresentationTokenDescription().getCredential().get(0)
        .setCredentialSpecUID(sourceCredSpec);

    List<Attribute> issuerAttributes = Collections.emptyList();

    IssuanceMessageAndBoolean imab;
    IssuMsgOrCredDesc imcd = new IssuMsgOrCredDesc();
    imab = issuer_ce.initIssuanceProtocol(policy, issuerAttributes, null);

    // Complex issuance requires pre-issuance presentation
    user_cm.storeCredential(USERNAME, sourceCred);
    List<URI> creds = Collections.singletonList(sourceUri);
    List<URI> nyms = Collections.singletonList(pseudonymUri);
    List<Attribute> atts = Collections.emptyList();
    imcd.im = user_ce.createIssuanceToken(USERNAME, imab.getIssuanceMessage(), itd, creds, nyms, atts);

    // Issuer needs to check if token satisfies policy
    IssuanceTokenAndIssuancePolicy itip = issuer_ce.extractIssuanceTokenAndPolicy(imcd.im);
    assertEquals(
      JaxbHelperClass.serialize(new ObjectFactory().createIssuancePolicy(policy)),
      JaxbHelperClass.serialize(new ObjectFactory().createIssuancePolicy(itip.getIssuancePolicy())));
    assertEquals(JaxbHelperClass.serialize(new ObjectFactory().createIssuanceTokenDescription(itd),
        true), JaxbHelperClass.serialize(new ObjectFactory().createIssuanceTokenDescription(itip
        .getIssuanceToken().getIssuanceTokenDescription()), true));

    while (!imab.isLastMessage()) {
      imab = issuer_ce.issuanceProtocolStep(imcd.im);
      imcd = user_ce.issuanceProtocolStep(USERNAME, imab.getIssuanceMessage());
    }

    assertNotNull(imcd.cd);
    assertNull(imcd.im);

    Credential cNew = user_cm.getCredential(USERNAME, imcd.cd.getCredentialUID());

    String c_xml = JaxbHelperClass.serialize((new ObjectFactory()).createCredential(cNew), true);
    System.out.println(c_xml);
    String credentialName =
        "credential-complex-device-"
            + sourceCred.getCredentialDescription().getIssuerParametersUID() + "-to-" + issuerUri
            + ".xml";

    saveCredentialToFile(c_xml, credentialName);

    return cNew;
  }

  public Credential runDeviceIssuanceNoNym(URI issuerUri, Credential sourceCred) throws Exception,
      CryptoEngineException {
    URI sourceUri = sourceCred.getCredentialDescription().getCredentialUID();
    URI sourceIssuer = sourceCred.getCredentialDescription().getIssuerParametersUID();
    URI sourceCredSpec = sourceCred.getCredentialDescription().getCredentialSpecificationUID();
    URI sourceSecret = sourceCred.getCredentialDescription().getSecretReference();

    if (sourceSecret != null) {
      user_deviceManager.allocateCredential(USERNAME, sourceSecret, sourceUri, sourceIssuer, true);
    }

    IssuancePolicy policy =
        TestUtils.getResource(resolveDefaultPath("policy-devicecarryovernonym.xml"),
            IssuancePolicy.class, this, true);
    policy.getCredentialTemplate().setIssuerParametersUID(issuerUri);
    policy.getPresentationPolicy().getCredential().get(0).getIssuerAlternatives()
        .getIssuerParametersUID().get(0).setValue(sourceIssuer);
    policy.getPresentationPolicy().getCredential().get(0).getCredentialSpecAlternatives()
        .getCredentialSpecUID().set(0, sourceCredSpec);

    IssuanceTokenDescription itd =
        TestUtils.getResource(resolveDefaultPath("token-devicecarryovernonym.xml"),
            IssuanceTokenDescription.class, this, true);
    itd.setCredentialTemplate(policy.getCredentialTemplate());
    itd.getPresentationTokenDescription().getCredential().get(0)
        .setIssuerParametersUID(sourceIssuer);
    itd.getPresentationTokenDescription().getCredential().get(0)
        .setCredentialSpecUID(sourceCredSpec);

    List<Attribute> issuerAttributes = Collections.emptyList();

    IssuanceMessageAndBoolean imab;
    IssuMsgOrCredDesc imcd = new IssuMsgOrCredDesc();
    imab = issuer_ce.initIssuanceProtocol(policy, issuerAttributes, null);

    // Complex issuance requires pre-issuance presentation
    user_cm.storeCredential(USERNAME, sourceCred);
    List<URI> creds = Collections.singletonList(sourceUri);
    List<URI> nyms = Collections.emptyList();
    List<Attribute> atts = Collections.emptyList();
    imcd.im = user_ce.createIssuanceToken(USERNAME, imab.getIssuanceMessage(), itd, creds, nyms, atts);

    // Issuer needs to check if token satisfies policy
    IssuanceTokenAndIssuancePolicy itip = issuer_ce.extractIssuanceTokenAndPolicy(imcd.im);
    assertEquals(
      JaxbHelperClass.serialize(new ObjectFactory().createIssuancePolicy(policy)),
      JaxbHelperClass.serialize(new ObjectFactory().createIssuancePolicy(itip.getIssuancePolicy())));
    assertEquals(JaxbHelperClass.serialize(new ObjectFactory().createIssuanceTokenDescription(itd),
        true), JaxbHelperClass.serialize(new ObjectFactory().createIssuanceTokenDescription(itip
        .getIssuanceToken().getIssuanceTokenDescription()), true));

    while (!imab.isLastMessage()) {
      imab = issuer_ce.issuanceProtocolStep(imcd.im);
      imcd = user_ce.issuanceProtocolStep(USERNAME, imab.getIssuanceMessage());
    }

    assertNotNull(imcd.cd);
    assertNull(imcd.im);

    Credential cNew = user_cm.getCredential(USERNAME, imcd.cd.getCredentialUID());

    String c_xml = JaxbHelperClass.serialize((new ObjectFactory()).createCredential(cNew), true);
    System.out.println(c_xml);
    String credentialName =
        "credential-complex-deviceNoNym-"
            + sourceCred.getCredentialDescription().getIssuerParametersUID() + "-to-" + issuerUri
            + ".xml";

    saveCredentialToFile(c_xml, credentialName);

    return cNew;
  }



  /**
   * Runs the verification protocol using the entities as initialised.
   */
  public PresentationToken runPresentationProtocol(PresentationTokenDescription ptd, List<URI> creds)
      throws Exception {

    List<URI> nyms = Collections.emptyList();

    if (!ptd.getPseudonym().isEmpty()) {
      String scope = ptd.getPseudonym().get(0).getScope();
      boolean exclusive = ptd.getPseudonym().get(0).isExclusive();
      URI secret = URI.create("secret");
      URI pseudonymUri = URI.create("nym");
      PseudonymWithMetadata pwm = user_ce.createPseudonym(USERNAME, pseudonymUri, scope, exclusive, secret);
      if (Configuration.debug()) {
        String nym_xml =
            JaxbHelperClass.serialize((new ObjectFactory()).createPseudonymWithMetadata(pwm));
        System.out.println(nym_xml);
      }
      user_cm.storePseudonym(USERNAME, pwm);

      ptd.getPseudonym().get(0).setPseudonymValue(pwm.getPseudonym().getPseudonymValue());

      nyms = Collections.singletonList(pseudonymUri);
    }

    PresentationToken pt = user_ce.createPresentationToken(USERNAME, ptd, verifierParameters, creds, nyms);

    if (Configuration.debug()) {
      String pt_xml = JaxbHelperClass.serialize((new ObjectFactory()).createPresentationToken(pt));
      System.out.println(pt_xml);
    }

    assertEquals(true, verifier_ce.verifyToken(pt, null));

    return pt;
  }

  /**
   * @param pt
   * @throws CryptoEngineException
   * @throws SerializationException
   */
  void inspect(PresentationToken pt) throws CryptoEngineException, SerializationException {
    List<Attribute> attributes = inspector_ce.inspect(pt);
    assertEquals(1, attributes.size());
    if (Configuration.debug()) {
      System.out.println(JaxbHelperClass.serialize(
          new ObjectFactory().createAttribute(attributes.get(0)), true));
    }
  }



}
