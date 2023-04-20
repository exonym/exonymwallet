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
package com.ibm.zurich.idmix.abc4trust.cryptoEngine;

//import java.math.BigInteger;
import java.math.BigInteger;
import java.net.URI;
import java.util.GregorianCalendar;
import java.util.List;

import com.ibm.zurich.idmix.abc4trust.facades.RevocationAuthorityParametersFacade;
import com.ibm.zurich.idmix.abc4trust.facades.SecretKeyFacade;
import com.ibm.zurich.idmx.interfaces.cryptoEngine.CryptoEngineRevocationAuthority;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.keypair.ra.RevocationAuthorityKeyPairWrapper;
import com.ibm.zurich.idmx.parameters.ra.RevocationAuthorityPublicKeyTemplateWrapper;

import eu.abc4trust.abce.internal.revocation.credentialManager.CredentialManager;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.cryptoEngine.revocation.CryptoEngineRevocation;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.KeyPair;
import eu.abc4trust.xml.NonRevocationEvidence;
import eu.abc4trust.xml.NonRevocationEvidenceUpdate;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.Reference;
import eu.abc4trust.xml.RevocationAuthorityParameters;
import eu.abc4trust.xml.RevocationInformation;
import eu.abc4trust.xml.SystemParameters;

import javax.inject.Inject;

public class Abc4TrustCryptoEngineRevocationImpl implements CryptoEngineRevocation {

  private final CryptoEngineRevocationAuthority cryptoEngineRevocationAuthority;
  private final CredentialManager credentialManager;
  private final KeyManager keyManager;

  private final BigIntFactory bigIntFactory;

  @Inject
  public Abc4TrustCryptoEngineRevocationImpl(
      final CryptoEngineRevocationAuthority cryptoEngineRevocationAuthority,
      final CredentialManager credentialManager, final KeyManager keyManager, final BigIntFactory bigIntFactory) {

    this.cryptoEngineRevocationAuthority = cryptoEngineRevocationAuthority;
    this.keyManager = keyManager;
    this.credentialManager = credentialManager;
    this.bigIntFactory = bigIntFactory;
  }

  @Override
  public RevocationAuthorityParameters setupRevocationAuthorityParameters(final int keyLength,
                                                                          final URI cryptographicMechanism, final URI uid, final Reference revocationInfoReference,
      final Reference nonRevocationEvidenceReference, final Reference nonRevocationUpdateReference)
      throws CryptoEngineException {

    // TODO this should use an ID to get the system parameters
    final SystemParameters systemParameters;
    try {
      systemParameters = keyManager.getSystemParameters();
    } catch (final KeyManagerException e) {
      throw new CryptoEngineException(e);
    }

    // Setup a template manually
    final RevocationAuthorityPublicKeyTemplateWrapper raKeyPairTemplateWrapper =
        new RevocationAuthorityPublicKeyTemplateWrapper();
    raKeyPairTemplateWrapper.setModulusLength(keyLength);
    raKeyPairTemplateWrapper.setNonRevocationEvidenceReference(nonRevocationEvidenceReference);
    raKeyPairTemplateWrapper.setNonRevocationEvidenceUpdateReference(nonRevocationUpdateReference);
    raKeyPairTemplateWrapper.setRevocationInformationReference(revocationInfoReference);
    raKeyPairTemplateWrapper.setPublicKeyPrefix(uid);
    raKeyPairTemplateWrapper.setSystemParametersId(systemParameters.getSystemParametersUID());
    raKeyPairTemplateWrapper.setTechnology(cryptographicMechanism);

    KeyPair raKeyPair;
    RevocationAuthorityParametersFacade raParametersFacade = null;
    try {
      raKeyPair =
          cryptoEngineRevocationAuthority.setupRevocationAuthorityKeyPair(systemParameters,
              raKeyPairTemplateWrapper.getRevocationAuthorityPublicKeyTemplate());

      final RevocationAuthorityKeyPairWrapper raKeyPairWrapper =
          new RevocationAuthorityKeyPairWrapper(raKeyPair);

      // Create the issuer parameters
      raParametersFacade =
          RevocationAuthorityParametersFacade.initRevocationAuthorityParameters(raKeyPairWrapper
              .getKeyPair().getPublicKey());

      // Wrap private key into abc4trust secret key
      final SecretKeyFacade secretKeyFacade =
          SecretKeyFacade.initSecretKey(raParametersFacade.getRevocationAuthorityParametersId(),
              raKeyPair.getPrivateKey());

      // Store the elements into credential manager and key manager respectively
      credentialManager.storeSecretKey(raParametersFacade.getRevocationAuthorityParametersId(),
          secretKeyFacade.getSecretKey());
      keyManager.storeRevocationAuthorityParameters(
          raParametersFacade.getRevocationAuthorityParametersId(),
          raParametersFacade.getRevocationAuthorityParameters());

    } catch (final Exception e) {
      throw new CryptoEngineException(e);
    }

    return raParametersFacade.getRevocationAuthorityParameters();
  }

  @Override
  public NonRevocationEvidence generateNonRevocationEvidence(URI revParUid,
      List<Attribute> attributes) throws CryptoEngineException {

    final URI nonRevocationEvidenceUID = URI.create("nre:uid");
    NonRevocationEvidence nre = null;
    // try {
    nre =
        cryptoEngineRevocationAuthority.newRevocationHandle(revParUid, nonRevocationEvidenceUID,
            attributes);
    // } catch (ConfigurationException e) {
    // throw new CryptoEngineException(e);
    // } catch (KeyManagerException e) {
    // throw new CryptoEngineException(e);
    // } catch (CredentialManagerException e) {
    // throw new CryptoEngineException(e);
    // } catch (IssuanceOrchestrationException e) {
    // throw new CryptoEngineException(e);
    // }

    return nre;
  }

  @Override
  public RevocationInformation revoke(final URI raParametersUID, final List<Attribute> attributes)
      throws CryptoEngineException {

    // The attributes contain the revocation handles as IssuerAttribute objects.
    if (attributes.size() != 1) {
      throw new IllegalArgumentException(
          "Attributes does not contain the expected number of revocation handles which is 1");
    }

    //was commented
    Attribute revocationHandleAttribute = attributes.get(0);
    BigInteger revocationHandleValue = (BigInteger) revocationHandleAttribute.getAttributeValue();
    
    final URI revocationLogEntry =
        cryptoEngineRevocationAuthority.revoke(raParametersUID,
            bigIntFactory.valueOf(revocationHandleValue));
    
    
    
    System.out.println(revocationLogEntry);
    return updateRevocationInformation(raParametersUID);
  }

  @Override
  public NonRevocationEvidenceUpdate generateNonRevocationEvidenceUpdate(final URI revAuthParamsUid,
                                                                         final int epoch) throws CryptoEngineException {
    // TODO Auto-generated method stub
    final NonRevocationEvidenceUpdate ret = new ObjectFactory().createNonRevocationEvidenceUpdate();
    ret.setCreated(new GregorianCalendar());
    ret.setCryptoParams(new ObjectFactory().createCryptoParams());
    ret.setExpires(new GregorianCalendar());
    ret.setNonRevocationEvidenceUID(URI.create("nre-uid"));
    ret.setNonRevocationEvidenceUpdateUID(URI.create("nre-update-uri"));
    ret.setRevocationAuthorityParametersUID(revAuthParamsUid);
    return ret;
  }

  @Override
  public RevocationInformation updateRevocationInformation(final URI revParUid)
      throws CryptoEngineException {

    try {
      return cryptoEngineRevocationAuthority.updateRevocationInformation(revParUid, null, null);
    } catch (final KeyManagerException e) {
      throw new CryptoEngineException(e);
    }
  }

  @Override
  public RevocationInformation getRevocationInformation(final URI revParamsUid, final URI revInfoUid)
      throws CryptoEngineException {
    try {
      return cryptoEngineRevocationAuthority.updateRevocationInformation(revParamsUid, null, null);
    } catch (final KeyManagerException e) {
      throw new CryptoEngineException(e);
    }
  }

}
