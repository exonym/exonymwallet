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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import eu.abc4trust.abce.internal.inspector.credentialManager.CredentialManager;
import eu.abc4trust.abce.internal.inspector.credentialManager.CredentialManagerException;
import eu.abc4trust.xml.SecretKey;

public class CredentialManagerInspectorBasic implements CredentialManager {

  private final ConcurrentMap<URI, SecretKey> secretKeyMap;
  
  public CredentialManagerInspectorBasic() {
    this.secretKeyMap = new ConcurrentHashMap<URI, SecretKey>();
  }
  
  @Override
  public List<URI> listInspectorSecretKeys() throws CredentialManagerException {
    return new ArrayList<URI>(secretKeyMap.keySet());
  }

  @Override
  public SecretKey getInspectorSecretKey(final URI inspectorKeyUID) throws CredentialManagerException {
    final SecretKey sk = secretKeyMap.get(inspectorKeyUID);
    if(sk == null) {
      System.err.println("Could not find inspector key: " + inspectorKeyUID);
    }
    return sk;
  }

  @Override
  public void storeInspectorSecretKey(final URI inspectorKeyUID, final SecretKey inspectorSecretKey)
      throws CredentialManagerException {
    secretKeyMap.put(inspectorKeyUID, inspectorSecretKey);
  }

}
