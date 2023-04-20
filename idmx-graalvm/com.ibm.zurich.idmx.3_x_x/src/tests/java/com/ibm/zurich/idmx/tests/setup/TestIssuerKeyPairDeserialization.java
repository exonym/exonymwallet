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

package com.ibm.zurich.idmx.tests.setup;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import com.ibm.zurich.idmix.abc4trust.facades.IssuerParametersFacade;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.SerializationException;
import com.ibm.zurich.idmx.keypair.issuer.IssuerKeyPairWrapper;
import com.ibm.zurich.idmx.keypair.issuer.IssuerPublicKeyWrapper;
import com.ibm.zurich.idmx.parameters.system.SystemParametersWrapper;
import com.ibm.zurich.idmx.tests.TestInitialisation;
import com.ibm.zurich.idmx.tests.TestUtils;

import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.keyManager.KeyManagerException;

/**
 * 
 */
public class TestIssuerKeyPairDeserialization extends TestInitialisation {

  @Test
  public void testIssuerKeyPairDeserialization() throws SerializationException,
      ConfigurationException, KeyManagerException, IOException {

    // Initialise system parameters
    SystemParametersWrapper systemParametersFacade = initSystemParameters();

    String issuerKeyPair =
        TestUtils.loadFromFile(TestIssuerKeyPair.DEFAULT_ISSUER_KEY_PAIR_FILENAME);
    IssuerKeyPairWrapper issuerKeyPairFacade = null;
    try {
      issuerKeyPairFacade = (IssuerKeyPairWrapper) IssuerKeyPairWrapper.deserialize(issuerKeyPair);

    } catch (SerializationException e) {
      e.printStackTrace();
      System.out.println("Idmix: This error is normal in case you did not run the "
          + "serialization test before this test. Try running all tests again "
          + "to verify that this error persits.");
    }
    assertTrue(issuerKeyPairFacade.getKeyPair() != null);
    assertEquals(issuerKeyPairFacade.getPublicKeyWrapper().getSystemParametersId(),
        systemParametersFacade.getSystemParametersId());
  }

  @Test
  public void testIssuerParametersCreation() throws SerializationException, ConfigurationException,
      KeyManagerException, CredentialManagerException, IOException {

    String keyPairFilename = TestIssuerKeyPair.DEFAULT_ISSUER_KEY_PAIR_FILENAME;
    String ipFilename = TestIssuerKeyPair.DEFAULT_ISSUER_PARAMETERS_FILENAME;

    String issuerKeyPair = TestUtils.loadFromFile(keyPairFilename);
    IssuerKeyPairWrapper issuerKeyPairFacade =
        (IssuerKeyPairWrapper) IssuerKeyPairWrapper.deserialize(issuerKeyPair);

    // Initialise system parameters
    SystemParametersWrapper systemParametersFacade = initSystemParameters();

    // Create ABC issuer parameters based on the issuer public key
    IssuerParametersFacade issuerParametersFacade =
        IssuerParametersFacade
            .initIssuerParameters(issuerKeyPairFacade.getKeyPair().getPublicKey(), systemParametersFacade.getSystemParameters());
    TestUtils.saveToFile(issuerParametersFacade.serialize(), ipFilename);
    TestUtils.print(ipFilename);

    // verify that the parameters have been set properly
    assertTrue(issuerParametersFacade.getIssuerParametersId().toString()
        .startsWith(TestIssuerKeyPair.PUBLIC_KEY_PREFIX.toString()));
    assertTrue(issuerParametersFacade.getRevocationAuthorityId().equals(
        TestIssuerKeyPair.REVOCATION_AUTHORITY));

    assertTrue(((IssuerPublicKeyWrapper) issuerKeyPairFacade.getPublicKeyWrapper())
        .getMaximalNumberOfAttributes() == TestIssuerKeyPair.NUMBER_OF_ATTRIBUTES);
    assertTrue(((IssuerPublicKeyWrapper) issuerKeyPairFacade.getPublicKeyWrapper())
        .getPublicKeyId().toString().startsWith(TestIssuerKeyPair.PUBLIC_KEY_PREFIX.toString()));
    assertEquals(
        ((IssuerPublicKeyWrapper) issuerKeyPairFacade.getPublicKeyWrapper())
            .getRevocationAuthorityId(),
        TestIssuerKeyPair.REVOCATION_AUTHORITY);
    assertTrue(issuerKeyPairFacade.getPublicKeyWrapper().getSystemParametersId()
        .equals(systemParametersFacade.getSystemParametersId()));
    assertTrue(issuerParametersFacade.getSystemParametersId()
        .equals(systemParametersFacade.getSystemParametersId()));

    assertTrue(issuerKeyPairFacade.getPublicKeyWrapper().getPublicKeyId()
        .equals(issuerParametersFacade.getIssuerParametersId()));
  }

}
