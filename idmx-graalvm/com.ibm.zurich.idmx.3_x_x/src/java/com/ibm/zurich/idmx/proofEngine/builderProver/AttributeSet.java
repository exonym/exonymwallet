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
package com.ibm.zurich.idmx.proofEngine.builderProver;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.ibm.zurich.idmx.exception.ProofException;
import com.ibm.zurich.idmx.interfaces.util.DisjointSet;
import com.ibm.zurich.idmx.util.DisjointSetImpl;

class AttributeSet {
  private final DisjointSet<String> set;
  private final Map<Object, AttributeProperty> properties;
  private final Set<String> knownObjects;


  public AttributeSet() {
    this.set = new DisjointSetImpl<String>();
    this.properties = new HashMap<Object, AttributeProperty>();
    this.knownObjects = new HashSet<String>();
  }

  public boolean contains(String name) {
    return knownObjects.contains(name);
  }


  public void insert(final String name, final boolean external, final int bitLength) {
    if (knownObjects.contains(name)) {
      final Object key = set.find(name);
      final boolean oldExternal = properties.get(key).external;
      if (oldExternal != external) {
        throw new RuntimeException("Trying to insert incompatible object.");
      }
      properties.get(key).bitLength = Math.max(properties.get(key).bitLength, bitLength);
    } else {
      final AttributeProperty rp = new AttributeProperty();
      rp.external = external;
      rp.representativeName = name;
      rp.bitLength = bitLength;
      final Object key = set.find(name);
      properties.put(key, rp);
      knownObjects.add(name);
    }
  }


  public AttributeProperty getProperty(String name) {
    if (!knownObjects.contains(name)) {
      throw new RuntimeException("Unknown key " + name);
    }
    Object key = set.find(name);
    return properties.get(key);
  }
  
  public void merge(final String name1, final String name2) throws ProofException {
    
    final Object key1 = set.find(name1);
    final Object key2 = set.find(name2);
    if (key1 == key2) {
      return;
    }

    final AttributeProperty rp1 = properties.remove(key1);
    final AttributeProperty rp2 = properties.remove(key2);
    final AttributeProperty newRp = rp1.merge(rp2);

    set.merge(name1, name2);

    final Object newKey = set.find(name1);
    properties.put(newKey, newRp);
  }

  public Collection<AttributeProperty> getProperties() {
    return properties.values();
  }
  
  public TreeSet<String> getKnownObjects() {
    return new TreeSet<String>(knownObjects);
  }

}
