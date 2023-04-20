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

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import eu.abc4trust.abce.internal.revocation.credentialManager.CredentialManager;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.xml.NonRevocationEvidence;
import eu.abc4trust.xml.RevocationHistory;
import eu.abc4trust.xml.RevocationLogEntry;
import eu.abc4trust.xml.SecretKey;


/**
 * 
 */
public class CredentialManagerRevocationAuthorityBasic implements CredentialManager {

  private final Map<URI, SecretKey> secretKeyMap;
  private final Map<URI, RevocationHistory> revocationHistoryMap;
  @SuppressWarnings("unused")
  private final Map<URI, RevocationLogEntry> revocationLogEntryMap;


  public CredentialManagerRevocationAuthorityBasic() {
    secretKeyMap = new ConcurrentHashMap<URI, SecretKey>();
    revocationHistoryMap = new ConcurrentHashMap<URI, RevocationHistory>();
    revocationLogEntryMap = new ConcurrentHashMap<URI, RevocationLogEntry>();
  }


  @Override
  public SecretKey getSecretKey(final URI uid) throws CredentialManagerException {
    final SecretKey sk = secretKeyMap.get(uid);
    if (sk == null) {
      System.err.println("Could not find RA private key: " + uid);
    }
    return sk;
  }

  @Override
  public void storeSecretKey(final URI secretKeyUID, final SecretKey revAuthSecretKey)
      throws CredentialManagerException {
    secretKeyMap.put(secretKeyUID, revAuthSecretKey);
  }

  @Override
  public void storeRevocationHistory(final URI historyUID, final RevocationHistory revHistory)
      throws CredentialManagerException {
    revocationHistoryMap.put(historyUID, revHistory);
  }

  @Override
  public RevocationHistory getRevocationHistory(final URI historyUID) throws CredentialManagerException {
    return revocationHistoryMap.get(historyUID);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * eu.abc4trust.abce.internal.revocation.credentialManager.CredentialManager#addRevocationLogEntry
   * (java.net.URI, eu.abc4trust.xml.RevocationLogEntry)
   */
  @Override
  public void addRevocationLogEntry(final URI logEntryUID, final RevocationLogEntry revLogEntry)
      throws CredentialManagerException {
    // TODO: tbd
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * eu.abc4trust.abce.internal.revocation.credentialManager.CredentialManager#deleteRevocationLogEntry
   * (java.net.URI)
   */
  @Override
  public void deleteRevocationLogEntry(final URI logEntryUID) throws CredentialManagerException {
    throw new RuntimeException("tbd");
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * eu.abc4trust.abce.internal.revocation.credentialManager.CredentialManager#getRevocationLogEntry
   * (java.net.URI)
   */
  @Override
  public RevocationLogEntry getRevocationLogEntry(final URI logEntryUID)
      throws CredentialManagerException {
    throw new RuntimeException("tbd");
  }


  /*
   * (non-Javadoc)
   * 
   * @see eu.abc4trust.abce.internal.revocation.credentialManager.CredentialManager#
   * storeNonRevocationEvidence(eu.abc4trust.xml.NonRevocationEvidence)
   */
  @Override
  public void storeNonRevocationEvidence(final NonRevocationEvidence nre)
      throws CredentialManagerException {
    // TODO: tbd
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * eu.abc4trust.abce.internal.revocation.credentialManager.CredentialManager#getNonRevocationEvidence
   * (java.net.URI)
   */
  @Override
  public NonRevocationEvidence getNonRevocationEvidence(final URI uid) throws CredentialManagerException {
    throw new RuntimeException("tbd");
  }

}
