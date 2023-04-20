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

package com.ibm.zurich.idmix.abc4trust.manager;

import java.math.BigInteger;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.ibm.zurich.idmx.device.ExternalSecretsManagerImpl;
import com.ibm.zurich.idmx.interfaces.util.RandomGeneration;

import eu.abc4trust.abce.internal.user.credentialManager.CredentialManager;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.xml.Credential;
import eu.abc4trust.xml.CredentialDescription;
import eu.abc4trust.xml.Pseudonym;
import eu.abc4trust.xml.PseudonymMetadata;
import eu.abc4trust.xml.PseudonymWithMetadata;
import eu.abc4trust.xml.Secret;
import eu.abc4trust.xml.SecretDescription;

import javax.inject.Inject;

/**
 * Iso
 * 
 */
public class CredentialManagerUserBasic implements CredentialManager {

  private final RandomGeneration randomGeneration;
  private final KeyManager km;
  private final Map<URI, Credential> credentialMap;
  private final Map<URI, PseudonymWithMetadata> pseudonymMap;

  @Inject
  public CredentialManagerUserBasic(final RandomGeneration randomGeneration, final KeyManager km) {
    this.randomGeneration = randomGeneration;
    credentialMap = new ConcurrentHashMap<URI, Credential>();
    pseudonymMap = new ConcurrentHashMap<URI, PseudonymWithMetadata>();
    this.km = km;
  }


  @Override
  public URI storeCredential(final String username, final Credential cred) throws CredentialManagerException {
    URI credId = cred.getCredentialDescription().getCredentialUID();
    if(credId == null) {
      credId = URI.create("cred-" + randomGeneration.generateRandomUid());
      cred.getCredentialDescription().setCredentialUID(credId);
    }
    credentialMap.put(credId, cred);
    return credId;
  }

  @Override
  public Credential getCredential(final String username, final URI credId) throws CredentialManagerException {
    return credentialMap.get(credId);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * eu.abc4trust.abce.internal.user.credentialManager.CredentialManager#getCredentialDescription
   * (java.util.List, java.util.List)
   */
  @Override
  public List<CredentialDescription> getCredentialDescription(final String username, final List<URI> issuers, final List<URI> credspecs)
      throws CredentialManagerException {
    // TODO Auto-generated method stub
    throw new RuntimeException("not implemented");
  }


  /*
   * (non-Javadoc)
   * 
   * @see
   * eu.abc4trust.abce.internal.user.credentialManager.CredentialManager#getCredentialDescription
   * (java.net.URI)
   */
  @Override
  public CredentialDescription getCredentialDescription(final String username, final URI creduid)
      throws CredentialManagerException {
    final Credential c = credentialMap.get(creduid);
    if(c==null) {
      return null;
    } else {
      return c.getCredentialDescription();
    }
  }


  /*
   * (non-Javadoc)
   * 
   * @see
   * eu.abc4trust.abce.internal.user.credentialManager.CredentialManager#attachMetadataToPseudonym
   * (eu.abc4trust.xml.Pseudonym, eu.abc4trust.xml.PseudonymMetadata)
   */
  @Override
  public void attachMetadataToPseudonym(final String username, final Pseudonym p, final PseudonymMetadata md)
      throws CredentialManagerException {
    // TODO Auto-generated method stub
    throw new RuntimeException("not implemented");
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * eu.abc4trust.abce.internal.user.credentialManager.CredentialManager#storePseudonym(eu.abc4trust
   * .xml.PseudonymWithMetadata)
   */
  @Override
  public void storePseudonym(final String username, final PseudonymWithMetadata pwm) throws CredentialManagerException {
    final URI nymUri = pwm.getPseudonym().getPseudonymUID();
    pseudonymMap.put(nymUri, pwm);
  }


  /*
   * (non-Javadoc)
   * 
   * @see
   * eu.abc4trust.abce.internal.user.credentialManager.CredentialManager#hasBeenRevoked(java.net
   * .URI, java.net.URI, java.util.List)
   */
  @Override
  public boolean hasBeenRevoked(final String username, final URI creduid, final URI revparsuid, final List<URI> revokedatts)
      throws CredentialManagerException {
    // TODO Auto-generated method stub
    throw new RuntimeException("not implemented");
  }


  /*
   * (non-Javadoc)
   * 
   * @see
   * eu.abc4trust.abce.internal.user.credentialManager.CredentialManager#hasBeenRevoked(java.net
   * .URI, java.net.URI, java.util.List, java.net.URI)
   */
  @Override
  public boolean hasBeenRevoked(final String username, final URI creduid, final URI revparsuid, final List<URI> revokedatts, final URI revinfouid)
      throws CredentialManagerException {
    // TODO Auto-generated method stub
    throw new RuntimeException("not implemented");
  }


  /*
   * (non-Javadoc)
   * 
   * @see
   * eu.abc4trust.abce.internal.user.credentialManager.CredentialManager#updateNonRevocationEvidence
   * ()
   */
  @Override
  public void updateNonRevocationEvidence(final String username) throws CredentialManagerException {
    // TODO Auto-generated method stub
    throw new RuntimeException("not implemented");
  }



  /*
   * (non-Javadoc)
   * 
   * @see
   * eu.abc4trust.abce.internal.user.credentialManager.CredentialManager#updateCredential(eu.abc4trust
   * .xml.Credential)
   */
  @Override
  public void updateCredential(final String username, final Credential cred) throws CredentialManagerException {
    // TODO Auto-generated method stub
    throw new RuntimeException("not implemented");
  }


  /*
   * (non-Javadoc)
   * 
   * @see eu.abc4trust.abce.internal.user.credentialManager.CredentialManager#listCredentials()
   */
  @Override
  public List<URI> listCredentials(final String username) throws CredentialManagerException {
    // TODO Auto-generated method stub
    throw new RuntimeException("not implemented");
  }


  /*
   * (non-Javadoc)
   * 
   * @see
   * eu.abc4trust.abce.internal.user.credentialManager.CredentialManager#deleteCredential(java.net
   * .URI)
   */
  @Override
  public boolean deleteCredential(final String username, final URI creduid) throws CredentialManagerException {
    // TODO Auto-generated method stub
    throw new RuntimeException("not implemented");
  }


  /*
   * (non-Javadoc)
   * 
   * @see
   * eu.abc4trust.abce.internal.user.credentialManager.CredentialManager#listPseudonyms(java.lang
   * .String, boolean)
   */
  @Override
  public List<PseudonymWithMetadata> listPseudonyms(final String username, final String scope, final boolean onlyExclusive)
      throws CredentialManagerException {
    // TODO Auto-generated method stub
    throw new RuntimeException("not implemented");
  }


  /*
   * (non-Javadoc)
   * 
   * @see
   * eu.abc4trust.abce.internal.user.credentialManager.CredentialManager#getPseudonym(java.net.URI)
   */
  @Override
  public PseudonymWithMetadata getPseudonym(final String username, final URI pseudonymUid) throws CredentialManagerException {
    return pseudonymMap.get(pseudonymUid);
  }


  /*
   * (non-Javadoc)
   * 
   * @see
   * eu.abc4trust.abce.internal.user.credentialManager.CredentialManager#deletePseudonym(java.net
   * .URI)
   */
  @Override
  public boolean deletePseudonym(final String username, final URI pseudonymUid) throws CredentialManagerException {
    // TODO Auto-generated method stub
    throw new RuntimeException("not implemented");
  }


  /*
   * (non-Javadoc)
   * 
   * @see
   * eu.abc4trust.abce.internal.user.credentialManager.CredentialManager#storeSecret(eu.abc4trust
   * .xml.Secret)
   */
  @Override
  public void storeSecret(final String username, final Secret cred) throws CredentialManagerException {
    // TODO Auto-generated method stub
    throw new RuntimeException("not implemented");
  }


  /*
   * (non-Javadoc)
   * 
   * @see eu.abc4trust.abce.internal.user.credentialManager.CredentialManager#listSecrets()
   */
  @Override
  public List<SecretDescription> listSecrets(final String username) throws CredentialManagerException {
    // TODO Auto-generated method stub
    throw new RuntimeException("not implemented");
  }


  /*
   * (non-Javadoc)
   * 
   * @see
   * eu.abc4trust.abce.internal.user.credentialManager.CredentialManager#deleteSecret(java.net.URI)
   */
  @Override
  public boolean deleteSecret(final String username, final URI secuid) throws CredentialManagerException {
    // TODO Auto-generated method stub
    throw new RuntimeException("not implemented");
  }


  /*
   * (non-Javadoc)
   * 
   * @see
   * eu.abc4trust.abce.internal.user.credentialManager.CredentialManager#getSecret(java.net.URI)
   */
  @Override
  public Secret getSecret(final String username, final URI secuid) throws CredentialManagerException {
    return ExternalSecretsManagerImpl.generateSecret(km, BigInteger.valueOf(1234), secuid);
  }


  /*
   * (non-Javadoc)
   * 
   * @see
   * eu.abc4trust.abce.internal.user.credentialManager.CredentialManager#updateSecretDescription
   * (eu.abc4trust.xml.SecretDescription)
   */
  @Override
  public void updateSecretDescription(final String username, final SecretDescription desc) throws CredentialManagerException {
    // TODO Auto-generated method stub
    throw new RuntimeException("not implemented");
  }


}
