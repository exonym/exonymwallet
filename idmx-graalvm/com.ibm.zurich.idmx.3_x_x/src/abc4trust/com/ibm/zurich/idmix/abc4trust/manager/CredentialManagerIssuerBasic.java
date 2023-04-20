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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import eu.abc4trust.abce.internal.issuer.credentialManager.CredentialManager;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.xml.SecretKey;


/**
 * 
 */
public class CredentialManagerIssuerBasic implements CredentialManager {

  private final Map<URI, SecretKey> secretKeyMap;


  public CredentialManagerIssuerBasic() {
    secretKeyMap = new ConcurrentHashMap<URI, SecretKey>();
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * eu.abc4trust.abce.internal.issuer.credentialManager.CredentialManager#listIssuerSecretKeys()
   */
  @Override
  public List<URI> listIssuerSecretKeys() throws CredentialManagerException {
    // TODO Auto-generated method stub
    return null;
  }


  @Override
  public SecretKey getIssuerSecretKey(final URI issuerParamsUid) throws CredentialManagerException {
    return secretKeyMap.get(issuerParamsUid);
  }


  @Override
  public void storeIssuerSecretKey(final URI issuerParamsUid, final SecretKey issuerSecretKey)
      throws CredentialManagerException {
    secretKeyMap.put(issuerParamsUid, issuerSecretKey);
  }

}
