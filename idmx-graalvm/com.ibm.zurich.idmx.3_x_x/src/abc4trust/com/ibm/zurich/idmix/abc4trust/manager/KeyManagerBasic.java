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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.InspectorPublicKey;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.RevocationAuthorityParameters;
import eu.abc4trust.xml.RevocationInformation;
import eu.abc4trust.xml.SystemParameters;

import javax.inject.Inject;

/**
 * 
 */
public class KeyManagerBasic implements KeyManager {

  private final static URI DEFAULT_SYSTEM_PARAMETERS_URI = URI.create("urn:idmx:params:system");
  private final Map<URI, SystemParameters> systemParametersMap;
  private final Map<URI, IssuerParameters> issuerParameterMap;
  private final Map<URI, CredentialSpecification> credentialSpecificationMap;
  private final Map<URI, RevocationAuthorityParameters> revocationParametersMap;
  private final Map<URI, InspectorPublicKey> inspectorKeyMap;
  private final Map<URI, RevocationInformation> revocationInfoMap;

  @Inject
  public KeyManagerBasic() {
    systemParametersMap = new ConcurrentHashMap<URI, SystemParameters>();
    issuerParameterMap = new ConcurrentHashMap<URI, IssuerParameters>();
    credentialSpecificationMap = new ConcurrentHashMap<URI, CredentialSpecification>();
    revocationParametersMap = new ConcurrentHashMap<URI, RevocationAuthorityParameters>();
    inspectorKeyMap = new ConcurrentHashMap<URI, InspectorPublicKey>();
    revocationInfoMap = new ConcurrentHashMap<URI, RevocationInformation>();
  }

  @Override
  public boolean storeSystemParameters(final SystemParameters systemParameters)
      throws KeyManagerException {
    return storeSystemParameters(systemParameters, DEFAULT_SYSTEM_PARAMETERS_URI);
  }

  @Override
  public SystemParameters getSystemParameters() throws KeyManagerException {
    final SystemParameters sp = getSystemParameters(DEFAULT_SYSTEM_PARAMETERS_URI);
    if (sp == null) {
      System.err.println("System parameters not found: " + DEFAULT_SYSTEM_PARAMETERS_URI);
    }
    return sp;
  }

  public boolean storeSystemParameters(final SystemParameters systemParameters, final URI systemParameterUri) {
    systemParametersMap.put(systemParameterUri, systemParameters);
    return true;
  }

  public SystemParameters getSystemParameters(final URI systemParameterUri) {
    final SystemParameters sp = systemParametersMap.get(systemParameterUri);
    if (sp == null) {
      System.err.println("System parameters not found: " + systemParameterUri);
    }
    return sp;
  }

  @Override
  public IssuerParameters getIssuerParameters(final URI issuid) throws KeyManagerException {
    final IssuerParameters ip = issuerParameterMap.get(issuid);
    if (ip == null) {
      System.err.println("Issuer parameters not found: " + issuid);
    }
    return ip;
  }

  @Override
  public boolean storeIssuerParameters(final URI issuid, final IssuerParameters issuerParameters)
      throws KeyManagerException {
    issuerParameterMap.put(issuid, issuerParameters);
    return true;
  }


  @Override
  public RevocationAuthorityParameters getRevocationAuthorityParameters(final URI rapuid)
      throws KeyManagerException {
    final RevocationAuthorityParameters rap = revocationParametersMap.get(rapuid);
    if (rap == null) {
      System.err.println("Revocation authority parameters not found: " + rapuid);
    }
    return rap;
  }

  @Override
  public boolean storeRevocationAuthorityParameters(final URI issuid,
                                                    final RevocationAuthorityParameters revocationAuthorityParameters) throws KeyManagerException {
    revocationParametersMap.put(issuid, revocationAuthorityParameters);
    return true;
  }

  @Override
  public CredentialSpecification getCredentialSpecification(URI credspec)
      throws KeyManagerException {
    final CredentialSpecification cs = credentialSpecificationMap.get(credspec);
    if (cs == null) {
      System.err.println("Credential specification not found: " + credspec);
    }
    return cs;
  }

  @Override
  public boolean storeCredentialSpecification(final URI uid,
                                              final CredentialSpecification credentialSpecification) throws KeyManagerException {
    credentialSpecificationMap.put(uid, credentialSpecification);
    return true;
  }


  /*
   * (non-Javadoc)
   * 
   * @see eu.abc4trust.keyManager.KeyManager#getInspectorPublicKey(java.net.URI)
   */
  @Override
  public InspectorPublicKey getInspectorPublicKey(final URI ipkuid) throws KeyManagerException {
    final InspectorPublicKey ret = inspectorKeyMap.get(ipkuid);
    if(ret == null) {
      System.err.println("Could not find inspector public key: " + ipkuid);
    }
    return ret;
  }

  /*
   * (non-Javadoc)
   * 
   * @see eu.abc4trust.keyManager.KeyManager#storeInspectorPublicKey(java.net.URI,
   * eu.abc4trust.xml.InspectorPublicKey)
   */
  @Override
  public boolean storeInspectorPublicKey(final URI ipkuid, final InspectorPublicKey inspectorPublicKey)
      throws KeyManagerException {
    inspectorKeyMap.put(ipkuid, inspectorPublicKey);
    return true;
  }



  /*
   * (non-Javadoc)
   * 
   * @see eu.abc4trust.keyManager.KeyManager#getCurrentRevocationInformation(java.net.URI)
   */
  @Override
  public RevocationInformation getCurrentRevocationInformation(final URI rapuid)
      throws KeyManagerException {
    return getRevocationInformation(rapuid, null);
  }

  /*
   * (non-Javadoc)
   * 
   * @see eu.abc4trust.keyManager.KeyManager#getLatestRevocationInformation(java.net.URI)
   */
  @Override
  public RevocationInformation getLatestRevocationInformation(final URI rapuid)
      throws KeyManagerException {
    return getRevocationInformation(rapuid, null);
  }

  /*
   * (non-Javadoc)
   * 
   * @see eu.abc4trust.keyManager.KeyManager#getRevocationInformation(java.net.URI, java.net.URI)
   */
  @Override
  public RevocationInformation getRevocationInformation(final URI rapuid, final URI revinfouid)
      throws KeyManagerException {
    final RevocationInformation ri = revocationInfoMap.get(rapuid);
    if(ri == null) {
      System.out.println("Could not get revocation information: " + ri);
    }
    return ri;
  }

  /*
   * (non-Javadoc)
   * 
   * @see eu.abc4trust.keyManager.KeyManager#hasSystemParameters()
   */
  @Override
  public boolean hasSystemParameters() throws KeyManagerException {
    // TODO Auto-generated method stub
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see eu.abc4trust.keyManager.KeyManager#storeRevocationInformation(java.net.URI,
   * eu.abc4trust.xml.RevocationInformation)
   */
  @Override
  public void storeRevocationInformation(final URI informationUID,
               final RevocationInformation revocationInformation) throws KeyManagerException {
    revocationInfoMap.put(informationUID, revocationInformation);
  }

  /* (non-Javadoc)
   * @see eu.abc4trust.keyManager.KeyManager#storeCurrentRevocationInformation(eu.abc4trust.xml.RevocationInformation)
   */
  @Override
  public void storeCurrentRevocationInformation(final RevocationInformation delegateeElement)
      throws KeyManagerException {
    // TODO Auto-generated method stub
    
  }

  @Override
  public List<URI> listIssuerParameters() throws KeyManagerException {
    return new ArrayList<URI>(issuerParameterMap.keySet());
  }

}
