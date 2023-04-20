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

package com.ibm.zurich.idmx.tests;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.inject.Inject;
import javax.xml.bind.JAXBException;

import com.ibm.zurich.idmx.buildingBlock.factory.BuildingBlockFactory;
import com.ibm.zurich.idmx.dagger.AbcComponent;
import com.ibm.zurich.idmx.dagger.DaggerAbcComponent;
import io.exonym.idmx.dagger.DaggerExonymComponent;
import org.junit.Before;
import org.xml.sax.SAXException;

import com.ibm.zurich.idmix.abc4trust.facades.CredentialFacade;
import com.ibm.zurich.idmix.abc4trust.facades.IssuerParametersFacade;
import com.ibm.zurich.idmix.abc4trust.facades.SecretKeyFacade;
import com.ibm.zurich.idmx.buildingBlock.signature.cl.ClSignatureBuildingBlock;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.SerializationException;
import com.ibm.zurich.idmx.interfaces.cryptoEngine.CryptoEngineInspector;
import com.ibm.zurich.idmx.interfaces.cryptoEngine.CryptoEngineIssuer;
import com.ibm.zurich.idmx.interfaces.cryptoEngine.CryptoEngineProver;
import com.ibm.zurich.idmx.interfaces.cryptoEngine.CryptoEngineRecipient;
import com.ibm.zurich.idmx.interfaces.cryptoEngine.CryptoEngineRevocationAuthority;
import com.ibm.zurich.idmx.interfaces.cryptoEngine.CryptoEngineVerifier;
import com.ibm.zurich.idmx.interfaces.orchestration.KeyGenerationOrchestration;
import com.ibm.zurich.idmx.interfaces.orchestration.issuance.IssuanceOrchestrationIssuer;
import com.ibm.zurich.idmx.interfaces.orchestration.issuance.IssuanceOrchestrationRecipient;
import com.ibm.zurich.idmx.interfaces.orchestration.presentation.PresentationOrchestrationProver;
import com.ibm.zurich.idmx.interfaces.orchestration.presentation.PresentationOrchestrationVerifier;
import com.ibm.zurich.idmx.interfaces.proofEngine.ZkDirector;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.RandomGeneration;
import com.ibm.zurich.idmx.interfaces.util.group.GroupFactory;
import com.ibm.zurich.idmx.keypair.KeyPairWrapper;
import com.ibm.zurich.idmx.keypair.issuer.IssuerKeyPairWrapper;
import com.ibm.zurich.idmx.parameters.system.SystemParametersWrapper;
import com.ibm.zurich.idmx.tests.setup.TestSystemParameters;

import eu.abc4trust.abce.internal.user.credentialManager.CredentialManager;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.xml.Credential;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.KeyPair;
import eu.abc4trust.xml.SystemParameters;

/**
 * 
 */
public class TestInitialisation {


  public static final AbcComponent INJECTOR = DaggerAbcComponent.builder().build();

  @Inject
  protected BigIntFactory bigIntFactory;
  protected GroupFactory groupFactory;
  protected RandomGeneration randomGeneration;

  protected KeyManager keyManager;
  protected ClSignatureBuildingBlock clBuildingBlock;
  protected ZkDirector zkDirector;
  protected eu.abc4trust.abce.internal.issuer.credentialManager.CredentialManager credentialManagerIssuer;
  protected eu.abc4trust.abce.internal.user.credentialManager.CredentialManager credentialManagerUser;
  private static final String USERNAME = "user";

  KeyGenerationOrchestration keyGenerationOrchestration;

  // Issuer
  protected CryptoEngineIssuer cryptoEngineIssuer;
  IssuanceOrchestrationIssuer issuanceOrchestrationIssuer;

  // Recipient
  protected CryptoEngineRecipient cryptoEngineRecipient;
  IssuanceOrchestrationRecipient issuanceOrchestrationResipient;

  // Prover
  protected CryptoEngineProver cryptoEngineProver;
  PresentationOrchestrationProver presentationOrchestrationProver;

  // Verifier
  protected CryptoEngineVerifier cryptoEngineVerifier;
  PresentationOrchestrationVerifier presentationOrchestrationVerifier;

  // Further participants
  protected CryptoEngineRevocationAuthority cryptoEngineRevocationAuthority;
  protected CryptoEngineInspector cryptoEngineInspector;


  @Before
  public void setup() {
    try {
      // Utils
      bigIntFactory = INJECTOR.provideBigIntFactory();
      groupFactory = INJECTOR.provideGroupFactory();
      randomGeneration = INJECTOR.provideRandomGeneration();

      BuildingBlockFactory bbf = INJECTOR.provideBuildingBlockFactory();

      // General
      keyManager = INJECTOR.providesKeyManager();
      clBuildingBlock = bbf.getBuildingBlockByClass(ClSignatureBuildingBlock.class);
      zkDirector = INJECTOR.providesZkDirector();

      // Issuer
      keyGenerationOrchestration = INJECTOR.providesKeyGenerationOrchestration();
      cryptoEngineIssuer = INJECTOR.providesCryptoEngineIssuer();
      issuanceOrchestrationIssuer = INJECTOR.providesIssuanceOrchestrationIssuer();
      credentialManagerIssuer = INJECTOR.providesCredentialManagerIssuer();

      // Recipient
      cryptoEngineRecipient = INJECTOR.providesCryptoEngineRecipient();
      issuanceOrchestrationResipient = INJECTOR.providesIssuanceOrchestrationRecipient();
      credentialManagerUser = INJECTOR.providesCredentialManagerUser();

      // Prover
      keyGenerationOrchestration = INJECTOR.providesKeyGenerationOrchestration();
      cryptoEngineProver = INJECTOR.providesCryptoEngineProver();
      presentationOrchestrationProver = INJECTOR.providesPresentationOrchestrationProver();

      // Verifier
      cryptoEngineVerifier = INJECTOR.providesCryptoEngineVerifier();
      presentationOrchestrationVerifier =
          INJECTOR.providesPresentationOrchestrationVerifier();

      // Revocation authority
      cryptoEngineRevocationAuthority = INJECTOR.providesCryptoEngineRevocationAuthority();
      // Inspector
      cryptoEngineInspector = INJECTOR.providesCryptoEngineInspector();

    } catch (ConfigurationException e) {
      System.out.println(e);

    }
  }


  /**
   * Initializes the system parameters such that they are available for the key manager.
   */
  public SystemParametersWrapper initSystemParameters(SystemParameters systemParameters,
      KeyManager keyManager) throws SerializationException, KeyManagerException, IOException {

    SystemParametersWrapper systemParametersFacade = new SystemParametersWrapper(systemParameters);

    // Load the parameters to the key manager
    keyManager.storeSystemParameters(systemParametersFacade.getSystemParameters());

    SystemParametersWrapper systemParametersWrapper =
        new SystemParametersWrapper(keyManager.getSystemParameters());
    return systemParametersWrapper;
  }


  /**
   * Convenience method.
   */
  public SystemParametersWrapper initSystemParameters() throws SerializationException,
      KeyManagerException, IOException {
    return initSystemParameters(TestSystemParameters.DEFAULT_SYSTEM_PARAMETERS_FILENAME);
  }

  
  public SystemParametersWrapper initSystemParameters(String spFilename)
      throws SerializationException, KeyManagerException, IOException {

    String systemParameters = TestUtils.loadFromFile(spFilename);
    SystemParametersWrapper systemParametersFacade =
        SystemParametersWrapper.deserialize(systemParameters);

    // Load the parameters to the key manager
    keyManager.storeSystemParameters(systemParametersFacade.getSystemParameters());

    SystemParametersWrapper systemParametersWrapper =
        new SystemParametersWrapper(keyManager.getSystemParameters());
    return systemParametersWrapper;
  }

  /**
   * Initializes the issuer parameters such that they are available from the key manager.
   */
  public void initIssuerKeyPair(KeyPair keyPair,
      eu.abc4trust.abce.internal.issuer.credentialManager.CredentialManager credentialManager,
      KeyManager keyManager) throws SerializationException, KeyManagerException,
      ConfigurationException, CredentialManagerException, IOException {

    KeyPairWrapper issuerKeyPairFacade = new IssuerKeyPairWrapper(keyPair);

    SystemParameters sp = keyManager.getSystemParameters();
    // Create the issuer parameters from key pair
    IssuerParametersFacade issuerParametersFacade =
        IssuerParametersFacade.initIssuerParameters(
            issuerKeyPairFacade.getKeyPair().getPublicKey(), sp);

    // Wrap private key into abc4trust secret key
    SecretKeyFacade secretKeyFacade =
        SecretKeyFacade.initSecretKey(issuerParametersFacade.getIssuerParametersId(),
            issuerKeyPairFacade.getKeyPair().getPrivateKey());

    // Store the elements into credential manager and key manager respectively
    credentialManager.storeIssuerSecretKey(issuerParametersFacade.getIssuerParametersId(),
        secretKeyFacade.getSecretKey());
    keyManager.storeIssuerParameters(issuerParametersFacade.getIssuerParametersId(),
        issuerParametersFacade.getIssuerParameters());

  }


  public void initCredentialSpecification(CredentialSpecification credSpec, KeyManager keyManager)
      throws JAXBException, SAXException, KeyManagerException, UnsupportedEncodingException {

    // Initialise credential specification using the ABC4Trust mechanisms
    // CredentialSpecification idcardCredSpec =
    // (CredentialSpecification) XmlUtils.getObjectFromXML(
    // this.getClass().getResourceAsStream(credentialSpecificationLocation), true);

    keyManager.storeCredentialSpecification(credSpec.getSpecificationUID(), credSpec);

  }

  public CredentialFacade initCredential(Credential credential, CredentialManager credentialManager)
      throws JAXBException, SAXException, KeyManagerException, UnsupportedEncodingException,
      CredentialManagerException {

    CredentialFacade credentialFacade = new CredentialFacade(credential);
    credentialManager.storeCredential(USERNAME, credentialFacade.getDelegateeValue());

    return credentialFacade;
  }

}
