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
package com.ibm.zurich.idmx.proofEngine.builderVerifier;

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
  DisjointSet<String> set;
  Map<Object, AttributeProperty> properties;
  Set<String> knownObjects;


  public AttributeSet() {
    this.set = new DisjointSetImpl<String>();
    this.properties = new HashMap<Object, AttributeProperty>();
    this.knownObjects = new HashSet<String>();
  }

  public boolean contains(String name) {
    return knownObjects.contains(name);
  }


  public void insert(String name, boolean external, int bitLength) throws ProofException {
    if (! knownObjects.contains(name)) {
      AttributeProperty rp = new AttributeProperty();
      rp.representativeName = name;
      rp.bitLength = bitLength;
      Object key = set.find(name);
      properties.put(key, rp);
      knownObjects.add(name);
    } else {
      AttributeProperty ap = getProperty(name);
      ap.bitLength = Math.max(ap.bitLength, bitLength);
    }
  }


  public AttributeProperty getProperty(String name) throws ProofException {
    if (!knownObjects.contains(name)) {
      throw new ProofException("Unknown key " + name);
    }
    Object key = set.find(name);
    return properties.get(key);
  }


  public void merge(String name1, String name2) throws ProofException {
    Object key1 = set.find(name1);
    Object key2 = set.find(name2);
    if (key1 == key2) {
      return;
    }

    AttributeProperty rp1 = properties.remove(key1);
    AttributeProperty rp2 = properties.remove(key2);
    AttributeProperty newRp = rp1.merge(rp2);

    set.merge(name1, name2);

    Object newKey = set.find(name1);
    properties.put(newKey, newRp);
  }

  public Collection<AttributeProperty> getProperties() {
    return properties.values();
  }
  
  public TreeSet<String> getKnownObjects() {
    return new TreeSet<String>(knownObjects);
  }

}
