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
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eu.abc4trust.db.PersistentStorage;
import eu.abc4trust.db.SimpleParamTypes;
import eu.abc4trust.db.TokenTypes;

public class SimplePersistentStorage implements PersistentStorage {

  private final Map<SimpleParamTypes, Map<URI, byte[]>> storage;

  public SimplePersistentStorage() {
    storage = new HashMap<SimpleParamTypes, Map<URI, byte[]>>();
    for (final SimpleParamTypes type : SimpleParamTypes.values()) {
      storage.put(type, new HashMap<URI, byte[]>());
    }
  }
  
  @Override
  public void shutdown() {
    //Does nothing
  }

  @Override
  public boolean insertItem(final SimpleParamTypes table, final URI key, final byte[] _value) {
    final byte[] value = Arrays.copyOf(_value, _value.length);
    if (storage.get(table).containsKey(key)) {
      return false;
    }
    storage.get(table).put(key, value);
    return true;
  }
  
  @Override
  public int replaceItem(SimpleParamTypes table, URI key, byte[] _value) {
    final byte[] value = Arrays.copyOf(_value, _value.length);
    storage.get(table).put(key, value);
    return 1;
  }

  @Override
  public byte[] getItem(final SimpleParamTypes table, final URI key) {
    byte[] item = storage.get(table).get(key);
    if (item != null) {
      item = Arrays.copyOf(item, item.length);
    }
    return item;
  }
  
  @Override
  public byte[] getItemAndDelete(final SimpleParamTypes table, final URI key) {
    byte[] item = storage.get(table).remove(key);
    if (item != null) {
      item = Arrays.copyOf(item, item.length);
    }
    return item;
  }

  @Override
  public boolean deleteItem(final SimpleParamTypes table, final URI key) {
    return storage.get(table).remove(key) != null;
  }

  @Override
  public boolean updateItem(final SimpleParamTypes table, final URI key, final byte[] value) {
    final byte[] valueStored = Arrays.copyOf(value, value.length);
    if (!storage.get(table).containsKey(key)) {
      return false;
    }
    storage.get(table).put(key, valueStored);
    return true;
  }

  @Override
  public List<URI> listItems(final SimpleParamTypes table) {
    return new ArrayList<URI>(storage.get(table).keySet());
  }

  @Override
  public boolean associatePseudonym(final TokenTypes table, final URI tokenId, final byte[] pseudonymValue) {
    throw new RuntimeException("Method not implemented");
  }

  @Override
  public boolean isPseudonymInToken(final TokenTypes table, final byte[] pseudonymValue) {
    throw new RuntimeException("Method not implemented");
  }

  @Override
  public boolean insertCredential(final URI key, final String username, final URI issuer, final URI credSpec, final byte[] value) {
    throw new RuntimeException("Method not implemented");
  }

  @Override
  public byte[] getCredential(final URI key, final String username) {
    throw new RuntimeException("Method not implemented");
  }

  @Override
  public boolean deleteCredential(final URI key, final String username) {
    throw new RuntimeException("Method not implemented");
  }

  @Override
  public boolean updateCredential(final URI key, final String username, final byte[] value) {
    throw new RuntimeException("Method not implemented");
  }

  @Override
  public List<URI> listCredentials(final String username) {
    throw new RuntimeException("Method not implemented");
  }

  @Override
  public List<URI> listCredentials(final String username, final List<URI> issuer, final List<URI> credSpec) {
    throw new RuntimeException("Method not implemented");
  }

  @Override
  public boolean insertSecret(final URI key, final String username, final byte[] value) {
    throw new RuntimeException("Method not implemented");
  }

  @Override
  public byte[] getSecret(final URI key, final String username) {
    throw new RuntimeException("Method not implemented");
  }

  @Override
  public boolean deleteSecret(final URI key, final String username) {
    throw new RuntimeException("Method not implemented");
  }

  @Override
  public boolean updateSecret(final URI key, final String username, final byte[] value) {
    throw new RuntimeException("Method not implemented");
  }

  @Override
  public List<URI> listSecrets(final String username) {
    throw new RuntimeException("Method not implemented");
  }

  @Override
  public boolean insertPseudonym(final URI key, final String username, final String scope, final boolean isExclusive,
                                 final byte[] pseudonymValue, final byte[] value) {
    throw new RuntimeException("Method not implemented");
  }

  @Override
  public byte[] getPseudonym(final URI key, final String username) {
    throw new RuntimeException("Method not implemented");
  }

  @Override
  public boolean deletePseudonym(final URI key, final String username) {
    throw new RuntimeException("Method not implemented");
  }

  @Override
  public boolean updatePseudonym(final URI key, final String username, final byte[] value) {
    throw new RuntimeException("Method not implemented");
  }

  @Override
  public List<URI> listPseudonyms(final String username) {
    throw new RuntimeException("Method not implemented");
  }

  @Override
  public List<URI> listPseudonyms(final String username, final String scope) {
    throw new RuntimeException("Method not implemented");
  }

  @Override
  public List<URI> listPseudonyms(final String username, final String scope, final boolean isExclusive) {
    throw new RuntimeException("Method not implemented");
  }

  @Override
  public List<URI> listPseudonyms(final String username, final byte[] pseudonymValue) {
    throw new RuntimeException("Method not implemented");
  }

  @Override
  public boolean insertRevocationInformation(final URI key, final URI rev_auth, final Calendar created, final byte[] value) {
    throw new RuntimeException("Method not implemented");
  }

  @Override
  public byte[] getRevocationInformation(final URI key, final URI rev_auth) {
    throw new RuntimeException("Method not implemented");
  }

  @Override
  public byte[] getLatestRevocationInformation(final URI rev_auth) {
    throw new RuntimeException("Method not implemented");
  }

 
}
