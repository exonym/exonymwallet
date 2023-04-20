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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.ibm.zurich.idmx.annotations.Nullable;
import com.ibm.zurich.idmx.buildingBlock.systemParameters.EcryptSystemParametersWrapper;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.ProofException;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.group.Group;
import com.ibm.zurich.idmx.interfaces.util.group.GroupElement;
import com.ibm.zurich.idmx.util.Hashing;

import eu.abc4trust.xml.ModuleInZkProof;
import eu.abc4trust.xml.ValueInZkProof;
import eu.abc4trust.xml.ValueWithHashInZkProof;

class StatePerModule {
  private final String moduleName;
  private byte[] hashContribution;
  private boolean customHashContribution;
  private boolean fail = false;

  private final AttributeSet registeredAttributes;
  private final Set<String> checkedDValueHashes;
  private @Nullable
  Map<String, XValue> nValueInProof;
  private @Nullable
  Map<String, XValue> tValueInProof;
  private @Nullable
  List<String> revealedAttributesInProof;

  private final List<String> revealedAttributes;
  private final TreeMap<String, XValue> dValues;
  private final TreeMap<String, XValue> nValues;
  private final TreeMap<String, XValue> tValues;
  private final TreeMap<String, XValue> sValues;

  private final BigIntFactory bigIntFactory;

  public static final String BIG_INT_TYPE = "integer";
  public static final String GROUP_ELEMENT_TYPE = "groupElement";
  public static final String OCTET_STREAM_TYPE = "octetStream";

  private StatePerModule(final String moduleName, final BigIntFactory bigIntFactory) {
    this.moduleName = moduleName;
    this.registeredAttributes = new AttributeSet();
    this.revealedAttributes = new ArrayList<String>();
    this.dValues = new TreeMap<String, XValue>();
    this.nValues = new TreeMap<String, XValue>();
    this.tValues = new TreeMap<String, XValue>();
    this.sValues = new TreeMap<String, XValue>();
    this.customHashContribution = false;
    this.hashContribution = null;
    this.checkedDValueHashes = new HashSet<String>();
    this.nValueInProof = null;
    this.tValueInProof = null;
    this.revealedAttributesInProof = null;
    this.bigIntFactory = bigIntFactory;
    this.fail = false;
  }

  public void registerAttribute(final String attributeName) throws ProofException {
    registeredAttributes.insert(attributeName, false, 0);
  }

  public void assertRegistered(final String attributeName) throws ProofException {
    if (!registeredAttributes.contains(attributeName)) {
      fail();
      throw new ProofException("Trying to access attribute " + attributeName
          + " which was not registered.");
    }
  }

  public BigInt getSValueAsInteger(final String name) throws ProofException {
    if (!sValues.containsKey(name)) {
      throw new ProofException("Cannot find SValue: " + name);
    }
    return sValues.get(name).getValueAsInt(bigIntFactory);
  };

  public byte[] getSValueAsByteArray(final String name) throws ProofException {
    if (!sValues.containsKey(name)) {
      throw new ProofException("Cannot find SValue: " + name);
    }
    return sValues.get(name).getValueAsByteArray();
  };

  public BigInt getDValueAsInteger(final String name) throws ProofException {
    if (!dValues.containsKey(name)) {
      throw new ProofException("Cannot find DValue: " + name);
    }
    return dValues.get(name).getValueAsInt(bigIntFactory);
  };

  public <GE extends GroupElement<?, GE, ?>> GE getDValueAsGroupElement(final String name,
		  final Group<?, GE, ?> group) throws ProofException {
    if (!dValues.containsKey(name)) {
      throw new ProofException("Cannot find DValue: " + name);
    }
    return dValues.get(name).getValueAsGroupElement(group);
  };

  public byte[] getDValueAsByteArray(final String name) throws ProofException {
    if (!dValues.containsKey(name)) {
      throw new ProofException("Cannot find DValue: " + name);
    }
    return dValues.get(name).getValueAsByteArray();
  };

  public void finalCheck(final StateForVerifier verifierState) throws ConfigurationException,
      ProofException {
    if (fail) {
      throw new ProofException("Proof check failed earlier.");
    }

    // Optional check that T-Values are correct
    if (tValueInProof != null && tValueInProof.size() != tValues.size()) {
    	final String forgottenTValues = setDifference(tValueInProof.keySet(), tValues.keySet());
      throw new ProofException("Not all TValues were added in module " + moduleName + "; missing: "
          + forgottenTValues + ".");
    }

    // Optional check that N-Values are correct
    if (nValueInProof != null && nValueInProof.size() != nValues.size()) {
      final String forgottenNValues = setDifference(nValueInProof.keySet(), nValues.keySet());
      throw new ProofException("Not all NValues were added in module " + moduleName + "; missing: "
          + forgottenNValues + ".");
    }

    // Optional check that all revealed attributes are accounted for
    final List<BigInt> revealedAttributeValues = revealedAttributeValueList(verifierState);
    if (revealedAttributesInProof != null && !revealedAttributes.equals(revealedAttributesInProof)) {
      throw new ProofException("Different set of revealed attributes in module " + moduleName + ".");
    }

    // Check that the hash contribution of all D-Values with custom hash contribution
    // has been provided by the verifier.
    for (final XValue dValue : dValues.values()) {
      if (dValue.hasCustomHashContribution()) {
        if (!checkedDValueHashes.contains(dValue.getName())) {
          throw new ProofException("DValue " + dValue.getName()
              + " has custom hash contribution, but verifier did not re-compute it.");
        }
      }
    }

    // Check hash contribution
    final byte[] computedHash = computeHashContribution(revealedAttributeValues, verifierState.sp);
    if (!Arrays.equals(computedHash, hashContribution)) {
      throw new ProofException("Recomputed hash contribution for module " + moduleName
          + " is incorrect.");
    }
  }

  private String setDifference(final Set<String> mustHave, final Set<String> have) {
	final Set<String> forgotten = new HashSet<String>(mustHave);
    forgotten.removeAll(have);

    final StringBuilder sb = new StringBuilder();
    boolean first = true;
    for (String name : forgotten) {
      if (first) {
        first = false;
      } else {
        sb.append(", ");
      }
      sb.append(name);
    }
    return sb.toString();
  }

  private byte[] computeHashContribution(final List<BigInt> revealedAttributes,
		  final EcryptSystemParametersWrapper sp) throws ConfigurationException, ProofException {
    try {
      if (!customHashContribution) {
    	final List<byte[]> dValues = dValuesForHash();
    	final List<byte[]> nValues = nValuesForHash();
    	final List<byte[]> tValues = tValuesForHash();

        if (dValues.isEmpty() && revealedAttributes.isEmpty() && nValues.isEmpty()
            && tValues.isEmpty()) {
          return null;
        }

        final Hashing hashing = new Hashing(sp);

        hashing.add(moduleName.getBytes("UTF-8"));
        hashing.addListBytes(dValues);
        hashing.addListSignedIntegers(revealedAttributes);
        hashing.addListBytes(nValues);
        hashing.addListBytes(tValues);

        return hashing.digestRaw();
      } else {
        return hashContribution;
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
   * Returns the list of revealed attribute values, sorted by name. Side effect: populate the
   * revealedAttributes list
   * 
   * @param proverState
   * @throws ProofException
   */
  private List<BigInt> revealedAttributeValueList(final StateForVerifier proverState)
      throws ProofException {
    final List<BigInt> revealedAttributeList = new ArrayList<BigInt>();
    final TreeSet<String> representatives = new TreeSet<String>();
    for (final AttributeProperty ap : registeredAttributes.getProperties()) {
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

  public void setHashContribution(final byte[] hashContribution) throws ProofException {
    if (!Arrays.equals(this.hashContribution, hashContribution)) {
      fail();
      throw new ProofException("Custom hash contribution is incorrect in module " + moduleName
          + ".");
    }
    this.customHashContribution = true;
    this.hashContribution = hashContribution;
  }

  public void addNValue(final String name, final BigInt value) throws ProofException {
    if (nValueInProof != null && ! nValueInProof.containsKey(name)) {
      fail();
      throw new ProofException("Unknown NValue " + name);
    }
    if (nValues.containsKey(name) && !nValues.get(name).isSame(value)) {
      fail();
      throw new ProofException("NValue exists already " + name);
    }
    if (nValueInProof != null && !nValueInProof.get(name).isSame(value)) {
      fail();
      throw new ProofException("Incorrectly re-computed NValue: " + name);
    }
    nValues.put(name, new XValue(name, value));
  };

  public void addNValue(final String name, final GroupElement<?, ?, ?> value) throws ProofException {
    if (nValueInProof != null && ! nValueInProof.containsKey(name)) {
      fail();
      throw new ProofException("Unknown NValue " + name);
    }
    if (nValues.containsKey(name) && !nValues.get(name).isSame(value)) {
      fail();
      throw new ProofException("NValue exists already " + name);
    }
    if (nValueInProof != null && !nValueInProof.get(name).isSame(value)) {
      fail();
      throw new ProofException("Incorrectly re-computed NValue: " + name);
    }
    nValues.put(name, new XValue(name, value));
  };

  public void addNValue(final String name, final byte[] value) throws ProofException {
    if (nValueInProof != null && ! nValueInProof.containsKey(name)) {
      fail();
      throw new ProofException("Unknown NValue " + name);
    }
    if (nValues.containsKey(name) && !nValues.get(name).isSame(value)) {
      fail();
      throw new ProofException("NValue exists already " + name);
    }
    if (nValueInProof != null && !nValueInProof.get(name).isSame(value)) {
      fail();
      throw new ProofException("Incorrectly re-computed NValue: " + name);
    }
    nValues.put(name, new XValue(name, value));
  };

  public void addTValue(final String name, final GroupElement<?, ?, ?> value) throws ProofException {
    if (tValueInProof != null && !tValueInProof.containsKey(name)) {
      fail();
      throw new ProofException("Unknown TValue " + name);
    }
    if (tValues.containsKey(name) && !tValues.get(name).isSame(value)) {
      fail();
      throw new ProofException("TValue exists already " + name);
    }
    if (tValueInProof != null && !tValueInProof.get(name).isSame(value)) {
      fail();
      throw new ProofException("Incorrectly re-computed TValue: " + name );
    }
    tValues.put(name, new XValue(name, value));
  };

  public void checkHashContributionOfDValue(final String name, final byte[] hashContribution)
      throws ProofException {
	final XValue oldValue = dValues.get(name);
    if (!Arrays.equals(oldValue.getHashContribution(), hashContribution)) {
      fail();
      throw new ProofException("Incorrectly re-computed hash contribution of DValue: " + name);
    }
    checkedDValueHashes.add(name);
  }

  public static StatePerModule parse(final ModuleInZkProof proof, final BigIntFactory bigIntFactory)
      throws ProofException {
	final StatePerModule ret = new StatePerModule(proof.getName(), bigIntFactory);

    ret.hashContribution = proof.getHashContribution();

    // DValues
    for (final ValueWithHashInZkProof value : proof.getDValue()) {
      final XValue dValue = XValue.parse(value);
      ret.dValues.put(dValue.getName(), dValue);
    }

    // SValues
    for (final ValueInZkProof value : proof.getSValue()) {
      final XValue sValue = XValue.parse(value);
      ret.sValues.put(sValue.getName(), sValue);
    }

    // NValues - Optional field
    if (proof.getNValue().size() > 0) {
      ret.nValueInProof = new HashMap<String, XValue>();
      for (final ValueInZkProof value : proof.getNValue()) {
        final XValue nValue = XValue.parse(value);
        ret.nValueInProof.put(nValue.getName(), nValue);
      }
    }
    // TValues - Optional field
    if (proof.getTValue().size() > 0) {
      ret.tValueInProof = new HashMap<String, XValue>();
      for (final ValueInZkProof value : proof.getTValue()) {
        final XValue tValue = XValue.parse(value);
        ret.tValueInProof.put(tValue.getName(), tValue);
      }
    }
    // Revealed attributes - Optional field
    if (proof.getRevealedAttribute().size() > 0) {
      ret.revealedAttributesInProof = new ArrayList<String>(proof.getRevealedAttribute());
    }

    return ret;
  }

  void fail() {
    fail = true;
  }

  public String getModuleName() {
    return moduleName;
  }

  public void localAttributesAreEqual(final String attributeName1, final String attributeName2)
      throws ProofException {
    registeredAttributes.merge(attributeName1, attributeName2);
  }
}
