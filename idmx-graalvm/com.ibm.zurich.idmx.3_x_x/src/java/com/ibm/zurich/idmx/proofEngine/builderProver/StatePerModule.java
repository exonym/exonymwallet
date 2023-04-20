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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.ProofException;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.group.GroupElement;
import com.ibm.zurich.idmx.util.Hashing;

import eu.abc4trust.xml.ModuleInZkProof;
import eu.abc4trust.xml.ValueInZkProof;
import eu.abc4trust.xml.ValueWithHashInZkProof;

class StatePerModule {
  public final String moduleName;
  //TODO why is this public
  public int positionInHash;
  private byte[] hashContribution;
  boolean customHashContribution;
  boolean hashComputed;
  // Signature building blocks need to be the first ones when computing the challenge
  private boolean moveToFront;

  private final TreeSet<String> allowedAttributes;
  private final List<String> revealedAttributes;
  private final TreeMap<String, XValue> dValues;
  private final TreeMap<String, XValue> nValues;
  private final TreeMap<String, XValue> tValues;
  private final TreeMap<String, XValue> sValues;
  private final AttributeSet registeredAttributes;

  public StatePerModule(final String moduleName) {
    this.moduleName = moduleName;
    this.positionInHash = -1;
    this.allowedAttributes = new TreeSet<String>();
    this.revealedAttributes = new ArrayList<String>();
    this.dValues = new TreeMap<String, XValue>();
    this.nValues = new TreeMap<String, XValue>();
    this.tValues = new TreeMap<String, XValue>();
    this.sValues = new TreeMap<String, XValue>();
    this.customHashContribution = false;
    this.hashContribution = null;
    this.hashComputed = false;
    this.moveToFront = false;
    this.registeredAttributes = new AttributeSet();
  }
  
  public void moveToFrontOfChallengeHashList() {
    moveToFront = true;
  }

  public void registerAttribute(final String attributeName) {
    registeredAttributes.insert(attributeName, false, 0);
  }

  public void allowAccessToAttribute(final String attributeName) {
    allowedAttributes.add(attributeName);
  }

  public void assertRegistered(final String attributeName) {
    if (!registeredAttributes.contains(attributeName)) {
      throw new RuntimeException("Trying to access attribute " + attributeName
          + " which was not registered.");
    }
  }

  public void assertAccessAllowed(final String attributeName) {
    if (!allowedAttributes.contains(attributeName)) {
      throw new RuntimeException("Trying to access attribute " + attributeName
          + " which was not required nor set.");
    }
  }

  public boolean isAccessAllowed(final String attributeName) {
    return allowedAttributes.contains(attributeName);
  }

  public void addDValue(final String name, final BigInt value) {
    if (dValues.containsKey(name) && !dValues.get(name).isSame(value)) {
      throw new RuntimeException("DValue exists already " + name);
    }
    dValues.put(name, new XValue(name, value));
  };

  public void addDValue(final String name, final GroupElement<?, ?, ?> value) {
    if (dValues.containsKey(name) && !dValues.get(name).isSame(value)) {
      throw new RuntimeException("DValue exists already " + name);
    }
    dValues.put(name, new XValue(name, value));
  };

  public void addDValue(final String name, final byte[] value, final byte[] hashContribution) {
    if (dValues.containsKey(name) && !dValues.get(name).isSame(value, hashContribution)) {
      throw new RuntimeException("DValue exists already " + name);
    }
    dValues.put(name, new XValue(name, value, hashContribution));
  };

  public void addNValue(final String name, final BigInt value) {
    if (nValues.containsKey(name) && !nValues.get(name).isSame(value)) {
      throw new RuntimeException("NValue exists already " + name);
    }
    nValues.put(name, new XValue(name, value));
  };

  public void addNValue(final String name, final GroupElement<?, ?, ?> value) {
    if (nValues.containsKey(name) && !nValues.get(name).isSame(value)) {
      throw new RuntimeException("NValue exists already " + name);
    }
    nValues.put(name, new XValue(name, value));
  };

  public void addNValue(final String name, final byte[] value) {
    if (nValues.containsKey(name) && !nValues.get(name).isSame(value)) {
      throw new RuntimeException("NValue exists already " + name);
    }
    nValues.put(name, new XValue(name, value));
  };

  public void addTValue(final String name, final GroupElement<?, ?, ?> value) {
    if (tValues.containsKey(name) && !tValues.get(name).isSame(value)) {
      throw new RuntimeException("TValue exists already " + name);
    }
    tValues.put(name, new XValue(name, value));
  };

  public void addSValue(final String name, final BigInt value) {
    if (sValues.containsKey(name) && !sValues.get(name).isSame(value)) {
      throw new RuntimeException("SValue exists already " + name);
    }
    sValues.put(name, new XValue(name, value));
  };

  public void addSValue(final String name, final byte[] value) {
    if (sValues.containsKey(name) && !sValues.get(name).isSame(value)) {
      throw new RuntimeException("SValue exists already " + name);
    }
    sValues.put(name, new XValue(name, value));
  };

  public BigInt getDValueAsInteger(final String name) {
    return dValues.get(name).getValueAsInt();
  };
  
  public boolean isMovedToFront() {
    return moveToFront;
  }

  public GroupElement<?, ?, ?> getDValueAsGroupElement(final String name) {
    return dValues.get(name).getValueAsGroupElement();
  };

  public byte[] getDValueAsByteArray(final String name) {
    return dValues.get(name).getValueAsByteArray();
  };

  public byte[] getHashContribution() {
    if (!hashComputed) {
      throw new RuntimeException("Hash contribution was not yet computed");
    }
    return hashContribution;
  }

  public void computeHashContribution(final StateForProver proverState) throws ConfigurationException {
    if (hashComputed) {
      throw new RuntimeException("Cannot compute hash twice");
    }
    hashComputed = true;

    try {
      if (!customHashContribution) {
        final List<byte[]> dValues = dValuesForHash();
        final List<BigInt> revealedAttributes = revealedAttributeValueList(proverState);
        final List<byte[]> nValues = nValuesForHash();
        final List<byte[]> tValues = tValuesForHash();

        if (dValues.isEmpty() && revealedAttributes.isEmpty() && nValues.isEmpty()
            && tValues.isEmpty()) {
          this.hashContribution = null;
          return;
        }

        final Hashing hashing = new Hashing(proverState.sp);

        hashing.add(moduleName.getBytes("UTF-8"));
        hashing.addListBytes(dValues);
        hashing.addListSignedIntegers(revealedAttributes);
        hashing.addListBytes(nValues);
        hashing.addListBytes(tValues);

        this.hashContribution = hashing.digestRaw();
      } else {
        if (!(nValues.isEmpty() && tValues.isEmpty())) {
          throw new RuntimeException(
              "Cannot have N- or T-values if using a custom hash contribution. (Maybe one of your child zkModules sets the hash contribution.)");
        }
        // Nothing to do
      }
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }



  private List<byte[]> xValuesForHash(final Collection<XValue> values) {
    final List<byte[]> ret = new ArrayList<byte[]>();
    for (final XValue value : values) {
      ret.add(value.getHashContribution());
    }
    return ret;
  }

  /**
   * Returns the list of hash contributions of all D-Values, sorted by name.
   */
  private List<byte[]> dValuesForHash() {
    return xValuesForHash(dValues.values());
  }

  /**
   * Returns the list of hash contributions of all N-Values, sorted by name.
   */
  private List<byte[]> nValuesForHash() {
    return xValuesForHash(nValues.values());
  }

  /**
   * Returns the list of hash contributions of all T-Values, sorted by name.
   */
  private List<byte[]> tValuesForHash() {
    return xValuesForHash(tValues.values());
  }

  /**
   * Returns the list of revealed attribute values, sorted by name Side effect: populate the
   * revealedAttributes list
   * 
   * @param proverState
   */
  private List<BigInt> revealedAttributeValueList(final StateForProver proverState) {
    final List<BigInt> revealedAttributeList = new ArrayList<BigInt>();
    final TreeSet<String> representatives = new TreeSet<String>();
    for(final AttributeProperty ap: registeredAttributes.getProperties()) {
      representatives.add(ap.representativeName);
    }
    for (final String attributeName : representatives) {
      if (proverState.isRevealedAttribute(attributeName)) {
        final BigInt value = proverState.getValueOfAttribute(attributeName);
        revealedAttributeList.add(value);
        revealedAttributes.add(attributeName);
      }
    }
    return revealedAttributeList;
  }

  public void setHashContribution(final byte[] hashContribution) {
    if (this.customHashContribution) {
      throw new RuntimeException(
          "Cannot set hash contribution twice. (Maybe one of your child zkModules sets the hash contribution.)");
    }
    this.customHashContribution = true;
    this.hashContribution = hashContribution;
  }

  public ModuleInZkProof serialize(final boolean verboseProof) {
    ModuleInZkProof ret = new ModuleInZkProof();
    ret.setName(moduleName);
    ret.setHashContribution(hashContribution);

    for (final XValue dValue : dValues.values()) {
      final ValueWithHashInZkProof value = dValue.serializeWithHash();
      ret.getDValue().add(value);
    }
    for (final XValue sValue : sValues.values()) {
      final ValueInZkProof value = sValue.serialize();
      ret.getSValue().add(value);
    }

    if (verboseProof) {
      for (final XValue nValue : nValues.values()) {
        final ValueInZkProof value = nValue.serialize();
        ret.getNValue().add(value);
      }
      for (final XValue tValue : tValues.values()) {
        final ValueInZkProof value = tValue.serialize();
        ret.getTValue().add(value);
      }
      for (final String attributeName : revealedAttributes) {
        ret.getRevealedAttribute().add(attributeName);
      }
    }
    return ret;
  }

  public void localAttributesAreEqual(final String attributeName1, final String attributeName2) throws ProofException {
    registeredAttributes.merge(attributeName1, attributeName2);
  }
}
