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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.ibm.zurich.idmx.buildingBlock.systemParameters.EcryptSystemParametersWrapper;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.ProofException;
import com.ibm.zurich.idmx.exception.TopologicalSortFailedException;
import com.ibm.zurich.idmx.interfaces.buildingBlock.structural.linearCombination.LinearCombination;
import com.ibm.zurich.idmx.interfaces.buildingBlock.structural.linearCombination.Term;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.ModuleSorter;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ResidueClass;
import com.ibm.zurich.idmx.proofEngine.HashComputationForChallenge;
import com.ibm.zurich.idmx.util.ModuleSorterImpl;

import eu.abc4trust.xml.AttributePartition;
import eu.abc4trust.xml.ModuleInZkProof;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.ValueInZkProof;
import eu.abc4trust.xml.ZkProof;

class StateForVerifier {

  private final AttributeSet set;
  private BigInt challenge;
  private final List<byte[]> hashContributions;
  private final Map<String, byte[]> hashContributionPerModule;
  private final Map<String, Integer> positionOfHashContribution;
  private final Map<String, BigInt> providedSValues;
  private final Map<String, BigInt> providedAttributeValues;
  private final List<LinearCombination> linearCombinations;
  //TODO public parameters?
  public final EcryptSystemParametersWrapper sp;

  private final BigIntFactory bigIntFactory;
  private final HashComputationForChallenge hcc;
  private List<AttributePartition> attributePartitions;

  public StateForVerifier(final SystemParameters systemParameters, final BigIntFactory bigIntFactory,
                          final HashComputationForChallenge hcc) {
    this.set = new AttributeSet();
    this.challenge = null;
    this.hashContributions = new ArrayList<byte[]>();
    this.hashContributionPerModule = new HashMap<String, byte[]>();
    this.positionOfHashContribution = new HashMap<String, Integer>();
    this.providedSValues = new HashMap<String, BigInt>();
    this.providedAttributeValues = new HashMap<String, BigInt>();
    this.linearCombinations = new ArrayList<LinearCombination>();

    this.sp = new EcryptSystemParametersWrapper(systemParameters);
    this.bigIntFactory = bigIntFactory;
    this.hcc = hcc;
  }

  public boolean isRegisteredAttribute(final String attributeName) {
    return set.contains(attributeName);
  }


  public void registerAttribute(final String attributeName, final boolean isExternal,
		  final int bitLength)
      throws ProofException {
    set.insert(attributeName, isExternal, bitLength);
  }


  public void attributeIsRevealed(final String attributeName) throws ProofException {
    AttributeProperty ap = set.getProperty(attributeName);
    ap.revealed = true;
  }

  public void attributesAreEqual(final String attributeName1, final String attributeName2)
      throws ProofException {
    set.merge(attributeName1, attributeName2);
  }


  public boolean isRevealedAttribute(final String attributeName) throws ProofException {
    return set.getProperty(attributeName).revealed;
  }

  public BigInt getValueOfAttribute(final String attributeName) throws ProofException {
	final AttributeProperty ap = set.getProperty(attributeName);
    if (!ap.revealed) {
      throw new ProofException("Attribute is not revealed, cannot get value: " + attributeName);
    }
    if (ap.getValue() == null) {
      throw new ProofException("Value of attribute is not available: " + attributeName);
    }
    return ap.getValue();
  }

  public BigInt getSValueOfAttribute(final String attributeName) throws ProofException {
	final AttributeProperty ap = set.getProperty(attributeName);
    if (ap.revealed) {
      throw new ProofException("Attribute is revealed, cannot get SValue: " + attributeName);
    }
    if (ap.getSValue() == null) {
      throw new ProofException("SValue of attribute is not available: " + attributeName);
    }
    return ap.getSValue();
  }

  public boolean isValueOfAttributeAvailable(final String attributeName) throws ProofException {
    AttributeProperty ap = set.getProperty(attributeName);
    return ap.getValue() != null;
  }


  private void computeChallenge() throws ConfigurationException, ProofException {
	final byte[] hash = hcc.getHashContributionForChallenge(hashContributions, sp);
	final BigInt newChallenge = bigIntFactory.unsignedValueOf(hash);

    if (challenge == null || challenge.equals(newChallenge)) {
      challenge = newChallenge;
    } else {
      throw new ProofException(
          "Challenge of proof is incorrect: different than hash of hash contributions.");
    }
  }

  public BigInt getChallenge() {
    return challenge;
  }

  public Map<String, StatePerModule> loadProof(final ZkProof proof) throws ProofException,
      ConfigurationException {

	final Map<String, StatePerModule> moduleStates = new HashMap<String, StatePerModule>();

    int hashIndex = 0;
    for (ModuleInZkProof module : proof.getModule()) {
      String name = module.getName();
      if (module.getHashContribution() != null) {
        hashContributions.add(module.getHashContribution());
        hashContributionPerModule.put(name, module.getHashContribution());
        positionOfHashContribution.put(name, hashIndex);
        hashIndex++;
      }
      StatePerModule state = StatePerModule.parse(module, bigIntFactory);
      moduleStates.put(name, state);
    }

    for (final ValueInZkProof sValue : proof.getSValue()) {
      final XValue value = XValue.parse(sValue);
      final BigInt valueAsInt = value.getValueAsInt(bigIntFactory);
      providedSValues.put(value.getName(), valueAsInt);
    }

    for (ValueInZkProof aValue : proof.getAttributeValue()) {
      final XValue value = XValue.parse(aValue);
      final BigInt valueAsInt = value.getValueAsInt(bigIntFactory);
      providedAttributeValues.put(value.getName(), valueAsInt);
    }

    if (proof.getChallenge() != null) {
      this.challenge = bigIntFactory.unsignedValueOf(proof.getChallenge());
    }

    computeChallenge();

    attributePartitions = new ArrayList<AttributePartition>(proof.getAttributePartition());

    return moduleStates;
  }


  public List<byte[]> getHashContributions() {
    return Collections.unmodifiableList(hashContributions);
  }

  public int getIndexOfHashContribution(final String moduleId) {
    Integer i = positionOfHashContribution.get(moduleId);
    if (i == null) {
      return -1;
    } else {
      return i;
    }
  }

  public void assignSAndAttributeValues() throws ProofException, ConfigurationException {
    try {
      checkAttributePartitions();
      assignSValues();
      assignAttributeValues();
      checkRangeOfSAndAttributeValues();
    } catch (TopologicalSortFailedException e) {
      throw new ProofException(e);
    }
  }

  private void checkAttributePartitions() throws ProofException {
    int totalNumberOfAttributes = 0;
    if (attributePartitions.size() > 0) {
      for (final AttributePartition ap : attributePartitions) {

        for (final String attributeName : ap.getAttributeName()) {
          totalNumberOfAttributes++;
          final String representative = set.getProperty(attributeName).representativeName;
          if (!representative.equals(ap.getRepresentativeName())) {
            printAttributePartitions();
            throw new ProofException(
                "Prover and verifier don't agree on the representative of attribute "
                    + attributeName + ". Prover: " + ap.getRepresentativeName() + ". Verifier: "
                    + representative
                    + ". This probably means the prover and verifier don't agree on which"
                    + " attributes are equal.");
          }
        }
        
        if(ap.isResidueClass() != null && ap.isResidueClass() != set.getProperty(ap.getRepresentativeName()).getResidueClass().needsRangeCheck()) {
          throw new ProofException("Prover and verifier don't agree on whether the attribute "
            + ap.getRepresentativeName() + " is an integer or a residue class.");
        }
      }
      if (attributePartitions.size() != set.getProperties().size()) {
        printAttributePartitions();
        throw new ProofException(
            "Attribute partition in proof and re-computed attribute partition are of different size");
      }
      if (totalNumberOfAttributes != set.knownObjects.size()) {
        printAttributePartitions();
        throw new ProofException("The prover and verifier don't agree on the number of attributes.");
      }
    }
  }

  private void printAttributePartitions() throws ProofException {
	final Map<String, AttributePartition> asm = new TreeMap<String, AttributePartition>();
    for (final AttributeProperty ap : set.getProperties()) {
      final AttributePartition as = new ObjectFactory().createAttributePartition();
      as.setRepresentativeName(ap.representativeName);
      asm.put(as.getRepresentativeName(), as);
    }
    for (final String attributeName : set.getKnownObjects()) {
      final AttributeProperty ap = set.getProperty(attributeName);
      final AttributePartition as = asm.get(ap.representativeName);
      as.getAttributeName().add(attributeName);
    }
    for (final AttributePartition ap : asm.values()) {
      System.out.println("Representative: " + ap.getRepresentativeName());
      for (final String a : ap.getAttributeName()) {
        System.out.println("  - " + a);
      }
    }
  }

  private void checkRangeOfSAndAttributeValues() throws ProofException, ConfigurationException,
      TopologicalSortFailedException {
	final List<String> attributeCheckOrder = topologicallySortAttributes();

    for (final String attribute : attributeCheckOrder) {
      final AttributeProperty ap = set.getProperty(attribute);
      if (ap.revealed) {
        checkRevealedAttribute(attribute, ap);
        for (final LinearCombination lc : ap.linearCombinations) {
          checkRevealedLinearCombination(ap, lc);
        }
        ap.rangeWasChecked = true;

      } else { // Unrevealed
        if (ap.linearCombinations.isEmpty()) {
          checkUnrevealedWithoutLinearCombination(ap);
          ap.rangeWasChecked = true;
        } else {
          for (final LinearCombination lc : ap.linearCombinations) {
            checkUnrevealedLinearCombination(lc);
          }
          ap.rangeWasChecked = true;
        }
      }
    }
  }

  private void checkUnrevealedLinearCombination(final LinearCombination lc) throws ProofException {
	final BigInt targetSValue = computeDerivedSValue(lc.lhsAttribute);
	BigInt actualSValue = computeDerivedSValue(lc.constant);
    for (final Term term : lc.terms) {
      AttributeProperty termAp = set.getProperty(term.attribute);
      if (!termAp.rangeWasChecked) {
        throw new ProofException("Did not check range of attribute " + term.attribute + ".");
      }
      BigInt derivedSValue = computeDerivedSValue(term.attribute);
      actualSValue = actualSValue.add(derivedSValue.multiply(term.constant));
    }
    if (!targetSValue.equals(actualSValue)) {
      throw new ProofException("Unrevealed linear combination does not hold: " + lc);
    }
  }

  private void checkUnrevealedWithoutLinearCombination(final AttributeProperty ap) throws ProofException,
      ConfigurationException {
    if (ap.getSValue() == null) {
      throw new ProofException("Unrevealed attribute set " + ap.representativeName
          + " has no SValue");
    }
    checkRangeOfSValue(ap.bitLength, ap.getSValue());
  }

  private void checkRevealedLinearCombination(final AttributeProperty ap, final LinearCombination lc)
      throws ProofException {
	final BigInt targetValue = ap.getValue();
	BigInt actualValue = lc.constant;
    for (final Term term : lc.terms) {
      AttributeProperty termAp = set.getProperty(term.attribute);
      if (!termAp.rangeWasChecked) {
        throw new ProofException("Did not check range of attribute " + term.attribute + ".");
      }
      if (!termAp.revealed) {
        throw new ProofException("Term " + term.attribute + " in linear combination " + lc
            + " is not revealed, even though the left hand side is.");
      }
      actualValue = actualValue.add((termAp.getValue()).multiply(term.constant));
    }
    if (!targetValue.equals(actualValue)) {
      throw new ProofException("Revealed linear combination does not hold: " + lc);
    }
  }

  private void checkRevealedAttribute(String attribute, AttributeProperty ap)
      throws ProofException, ConfigurationException {
    if (ap.getValue() == null) {
      throw new ProofException("Revealed attribute " + attribute + " has no value");
    }
    if (ap.linearCombinations.isEmpty()) {
      // Don't check range of attributes that are the result of a linear combination
      checkRangeOfAttributeValue(ap.bitLength, ap.getValue());
    }
  }

  private List<String> topologicallySortAttributes() throws ProofException,
      TopologicalSortFailedException {
    final ModuleSorter<String, String> attSorter = new ModuleSorterImpl<String, String>();
    for (final AttributeProperty ap : set.getProperties()) {
      attSorter.registerSupply(ap.representativeName, ap.representativeName);
      for (final LinearCombination lc : ap.linearCombinations) {
        for (final Term term : lc.terms) {
          AttributeProperty termAp = set.getProperty(term.attribute);
          attSorter.registerDemand(ap.representativeName, termAp.representativeName);
        }
      }
    }
    final List<String> attributeCheckOrder = attSorter.sortModules();
    return attributeCheckOrder;
  }

  /**
   * Returns the "derived" S-Value of the attribute, namely: - If the attribute is undisclosed,
   * return its S-Value. - If the attribute is disclosed, return -challenge * value
   * 
   * @param lhsAttribute
   * @return
   * @throws ProofException
   */
  private BigInt computeDerivedSValue(final String attributeName) throws ProofException {
    final AttributeProperty ap = set.getProperty(attributeName);
    if (ap.revealed) {
      return computeDerivedSValue(ap.getValue());
    } else {
      return ap.getSValue();
    }
  }

  /**
   * Returns -challenge * constant
   */
  private BigInt computeDerivedSValue(final BigInt constant) {
    return challenge.multiply(constant).negate();
  }

  private void checkRangeOfSValue(final int bitLength, final BigInt sValue)
		  throws ProofException,
      ConfigurationException {
	final BigInt maxAtt = sp.getMaximumAttributeValue(bitLength, bigIntFactory);
	final BigInt minAtt = bigIntFactory.zero();
	checkRangeOfSValue(sValue, bitLength, minAtt, maxAtt);
  }
  
  private void checkRangeOfSValue(BigInt sValue, int bitLengthOfAttributeForRValue,
                                 BigInt minAtt, BigInt maxAtt) throws ConfigurationException, ProofException {
    final int rSize = sp.getSizeOfRValue(bitLengthOfAttributeForRValue);
    final BigInt maxRValue = bigIntFactory.two().pow(rSize).subtract(bigIntFactory.one());

    // SValue = RValue - C*attributeValue
    // minimum = -C*maxAttributeValue
    // maximum = maxRValue - C*minAtt
    final BigInt min = maxAtt.multiply(challenge).negate();
    final BigInt max = maxRValue.subtract(challenge.multiply(minAtt));
    if (sValue.compareTo(min) < 0 || sValue.compareTo(max) > 0) {
      throw new ProofException("SValue out of range min=" + min.toHumanReadableString() + " max="
          + maxRValue.toHumanReadableString() + " actual=" + sValue.toHumanReadableString() + ".");
    }
  }

  private void checkRangeOfAttributeValue(final int bitLength, final BigInt value)
      throws ConfigurationException, ProofException {
    final BigInt max = sp.getMaximumAttributeValue(bitLength, bigIntFactory);
    final BigInt min = bigIntFactory.zero();

    // maximum = maxRvalue (excluded)
    if (value.compareTo(min) < 0 || value.compareTo(max) > 0) {
      throw new ProofException("Attribute value out of range min=" + min.toHumanReadableString()
          + " max=" + max.toHumanReadableString() + " actual=" + value.toHumanReadableString()
          + ".");
    }
  }

  private void assignAttributeValues() throws ProofException {
    for (final Entry<String, BigInt> entry : providedAttributeValues.entrySet()) {
    	final AttributeProperty ap = set.getProperty(entry.getKey());
      ap.assignValue(entry.getValue());
    }
  }

  private void assignSValues() throws ProofException {
    for (final Entry<String, BigInt> entry : providedSValues.entrySet()) {
      final AttributeProperty ap = set.getProperty(entry.getKey());
      ap.assignSValue(entry.getValue());
    }
  }

  public void registerAttribute(final String name, final boolean isExternal)
		  throws ProofException {
    registerAttribute(name, isExternal, 0);
  }

  public void addLinearCombination(final LinearCombination linComb) throws ProofException {
    linearCombinations.add(linComb);
    final AttributeProperty ap = set.getProperty(linComb.lhsAttribute);
    ap.linearCombinations.add(linComb);
  }

  public void mergeResidueClass(String attributeName, ResidueClass rc) throws ProofException {
    set.getProperty(attributeName).mergeResidueClass(rc);
  }

  public ResidueClass getResidueClass(String attributeName) throws ProofException {
    return set.getProperty(attributeName).getResidueClass();
  }
}
