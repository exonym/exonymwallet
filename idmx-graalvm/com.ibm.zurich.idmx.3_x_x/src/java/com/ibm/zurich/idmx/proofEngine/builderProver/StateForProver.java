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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import com.ibm.zurich.idmx.interfaces.util.RandomGeneration;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ResidueClass;
import com.ibm.zurich.idmx.proofEngine.HashComputationForChallenge;
import com.ibm.zurich.idmx.util.ModuleSorterImpl;

import eu.abc4trust.xml.AttributePartition;
import eu.abc4trust.xml.ModuleInZkProof;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.ZkProof;

class StateForProver {

  private final AttributeSet set;
  private BigInt challenge;

  public final EcryptSystemParametersWrapper sp;

  private final BigIntFactory bigIntFactory;
  private final RandomGeneration randomGeneration;
  private final AttributeDependencyResolver dependencyResolver;
  private boolean hasExternalSecrets;
  private HashComputationForChallenge hcc;

  public StateForProver(final SystemParameters systemParameters, final BigIntFactory bigIntFactory,
		  final RandomGeneration rg, final HashComputationForChallenge hcc) {
    this.set = new AttributeSet();
    this.challenge = null;

    this.sp = new EcryptSystemParametersWrapper(systemParameters);
    this.bigIntFactory = bigIntFactory;
    this.randomGeneration = rg;
    this.dependencyResolver =
        new AttributeDependencyResolver(new ModuleSorterImpl<String, String>());
    this.hasExternalSecrets = false;
    this.hcc = hcc;
  }


  public void assignRValues() throws ConfigurationException, ProofException {
    try {
      final List<String> rValueAssignationOrder = topologicallySortRValues();
      for (final String rValueName : rValueAssignationOrder) {
        final AttributeProperty ap = set.getProperty(rValueName);
        if (ap.linearCombinations.isEmpty()) {
          if (ap.overrideRValue == null) {
            final int rSize = sp.getSizeOfRValue(ap.bitLength);
            ap.rValue = randomGeneration.generateRandomNumber(rSize);
          } else {
            ap.rValue = ap.overrideRValue;
            System.out.println("!!! R-Value overridden for attribute " + ap.representativeName
                + "." + " This should happen only during tests.");
          }
        } else {
          // We can take any entry here
          final LinearCombination lc = ap.linearCombinations.get(0);
          ap.rValue = bigIntFactory.zero();
          for (final Term term : lc.terms) {
            final AttributeProperty termAp = set.getProperty(term.attribute);
            if (!termAp.revealed) {
              ap.rValue = (ap.rValue).add((term.constant).multiply(termAp.rValue));
            }
          }
        }
      }
    } catch (final TopologicalSortFailedException e) {
      throw new ProofException(e);
    }
  }


  private List<String> topologicallySortRValues() throws TopologicalSortFailedException,
      ProofException {
    final ModuleSorter<String, String> attSorter = new ModuleSorterImpl<String, String>();
    for (final AttributeProperty ap : set.getProperties()) {
      // Sanity checks
      if (ap.external && !ap.linearCombinations.isEmpty()) {
        throw new ProofException("Cannot do linear combination on external attributes: "
            + ap.representativeName + ".");
      }
      if (ap.overrideRValue != null && !ap.linearCombinations.isEmpty()) {
        throw new ProofException("Cannot do linear combination with overriden R-Values: "
            + ap.representativeName + ".");
      }
      // Only non-external unrevealed attributes have R-Values
      if (!ap.external && !ap.revealed) {
        attSorter.registerSupply(ap.representativeName, ap.representativeName);
        /*
         * We are conservative here, and require that the lhs attribute comes after ALL the rhs
         * attributes. This might result in failed topological sorts even if there would have been a
         * valid ordering.
         */
        for (final LinearCombination lc : ap.linearCombinations) {
          for (final Term term : lc.terms) {
            final AttributeProperty termAp = set.getProperty(term.attribute);
            if (termAp.external) {
              throw new RuntimeException("Cannot do linear combination on external attributes: "
                  + term.attribute + " in " + lc + ".");
            }
            if (!termAp.revealed) {
              attSorter.registerDemand(ap.representativeName, termAp.representativeName);
            }
          }
        }
      }
    }
    final List<String> rValueAssignationOrder = attSorter.sortModules();
    return rValueAssignationOrder;
  }


  private void assignSValuesAndCheckRange() throws ConfigurationException {
    for (final AttributeProperty ap : set.getProperties()) {
      if (!ap.external && !ap.revealed) {
        // SValue = RValue - challenge * AttributeValue
        ap.sValue = ap.rValue.subtract(challenge.multiply(ap.value));
      }
      // Check range of non-external attributes which are not part of a linear combination
      if (!ap.external && ap.linearCombinations.isEmpty()) {
        final BigInt minValue = bigIntFactory.zero();
        final BigInt maxValue = sp.getMaximumAttributeValue(ap.bitLength, bigIntFactory);

        if (ap.value.compareTo(minValue) < 0 || ap.value.compareTo(maxValue) > 0) {
          throw new RuntimeException("Attribute value " + ap.representativeName
              + "is out of range; value=" + ap.value + " expected range: " + minValue + " to "
              + maxValue);
        }
      }
    }
  }


  public boolean isRegisteredAttribute(final String attributeName) {
    return set.contains(attributeName);
  }


  public void registerAttribute(final String attributeName, final boolean isExternal,
		  final int bitLength) {
    set.insert(attributeName, isExternal, bitLength);
    if (isExternal) {
      hasExternalSecrets = true;
    }
  }


  public void attributeIsRevealed(final String attributeName) {
    final AttributeProperty ap = set.getProperty(attributeName);
    if (ap.external) {
      throw new RuntimeException("Cannot reveal external attributes " + attributeName);
    }
    ap.revealed = true;
  }


  public void attributesAreEqual(final String attributeName1, final String attributeName2)
      throws ProofException {
    set.merge(attributeName1, attributeName2);
  }


  public void setAttributeValue(final String attributeName, final BigInt attributeValue) {
    final AttributeProperty ap = set.getProperty(attributeName);
    if (ap.external) {
      throw new RuntimeException("Cannot set value of external attribute: " + attributeName);
    }
    if (ap.value != null && !ap.value.equals(attributeValue)) {
      throw new RuntimeException("Incompatible set attribute value for " + attributeName
          + " left: " + ap.value + " right " + attributeValue + " rep: " + ap.representativeName);
    }
    ap.value = attributeValue;
  }


  public boolean isRevealedAttribute(final String attributeName) {
    return set.getProperty(attributeName).revealed;
  }


  public BigInt getValueOfAttribute(final String attributeName) {
    final AttributeProperty ap = set.getProperty(attributeName);
    if (ap.external) {
      throw new RuntimeException("Cannot get value of external attribute: " + attributeName);
    }
    if (ap.value == null) {
      throw new RuntimeException("Value of attribute is not available: " + attributeName);
    }
    return ap.value;
  }


  public boolean isValueOfAttributeAvailable(final String attributeName) {
    final AttributeProperty ap = set.getProperty(attributeName);
    return ap.value != null;
  }


  public BigInt getRValueOfAttribute(final String attributeName) {
    return set.getProperty(attributeName).rValue;
  }


  public void setSValueOfExternalAttribute(final String attributeName, final BigInt sValue) {
    final AttributeProperty ap = set.getProperty(attributeName);
    if (!ap.external) {
      throw new RuntimeException("Cannot set S-Value of non-external attribute: " + attributeName);
    }
    if (ap.sValue != null && !ap.sValue.equals(sValue)) {
      throw new RuntimeException("Incompatible set attribute sValue for " + attributeName);
    }

    checkRangeOfSValue(ap.bitLength, sValue);

    ap.sValue = sValue;
  }

  private void checkRangeOfSValue(final int attributeLength, final BigInt sValue) {
    try {
      final BigInt maxAtt = sp.getMaximumAttributeValue(attributeLength, bigIntFactory);
      final int rSize = sp.getSizeOfRValue(attributeLength);
      final BigInt maxRValue = bigIntFactory.two().pow(rSize).subtract(bigIntFactory.one());
      final BigInt min = maxAtt.multiply(challenge).negate();

      if (sValue.compareTo(min) < 0 || sValue.compareTo(maxRValue) > 0) {
        throw new RuntimeException("SValue of external attribute out of range min="
            + min.toHumanReadableString() + " max=" + maxRValue.toHumanReadableString()
            + " actual=" + sValue.toHumanReadableString() + ".");
      }
    } catch (ConfigurationException e) {
      throw new RuntimeException(e);
    }
  }


  public void computeChallenge(final List<byte[]> hashContributions) throws ConfigurationException {
    if (challenge != null) {
      throw new RuntimeException("Challenge has already been computed");
    }
    
    final byte[] hash = hcc.getHashContributionForChallenge(hashContributions, sp);
    this.challenge = bigIntFactory.unsignedValueOf(hash);

    assignSValuesAndCheckRange();
  }


  public BigInt getChallenge() {
    if (challenge == null) {
      throw new RuntimeException("Challenge not yet computed");
    }
    return challenge;
  }


  public ZkProof serialize(final Collection<StatePerModule> statesPerModule,
		  final boolean verboseProof) {
    if (challenge == null) {
      throw new RuntimeException("Challenge not yet computed");
    }

    final ZkProof proof = new ObjectFactory().createZkProof();

    for (final StatePerModule statePerModule : statesPerModule) {
      ModuleInZkProof module = statePerModule.serialize(verboseProof);
      proof.getModule().add(module);
    }

    // Sort SValues and attribute values alphabetically (by representative)
    final List<AttributeProperty> attributeProperties =
        new ArrayList<AttributeProperty>(set.getProperties());
    Collections.sort(attributeProperties);
    for (final AttributeProperty ap : attributeProperties) {
      if (ap.revealed) {
        if (ap.value == null) {
          throw new RuntimeException("Attribute " + ap.representativeName
              + " is revealed but has no value.");
        }
        final XValue value = new XValue(ap.representativeName, ap.value);
        proof.getAttributeValue().add(value.serialize());
      } else {
        if (ap.sValue == null) {
          throw new RuntimeException("Attribute " + ap.representativeName
              + " is unrevealed but has no sValue.");
        }
        final XValue value = new XValue(ap.representativeName, ap.sValue);
        proof.getSValue().add(value.serialize());
      }
    }

    if (verboseProof) {
      proof.setChallenge(challenge.toByteArrayUnsigned());

      final Map<String, AttributePartition> asm = new TreeMap<String, AttributePartition>();
      for (final AttributeProperty ap : set.getProperties()) {
        final AttributePartition as = new ObjectFactory().createAttributePartition();
        as.setRepresentativeName(ap.representativeName);
        as.setResidueClass(ap.getResidueClass().needsRangeCheck());
        asm.put(as.getRepresentativeName(), as);
      }
      for (final String attributeName : set.getKnownObjects()) {
        final AttributeProperty ap = set.getProperty(attributeName);
        final AttributePartition as = asm.get(ap.representativeName);
        as.getAttributeName().add(attributeName);
      }
      proof.getAttributePartition().addAll(asm.values());
    }

    return proof;
  }


  public void registerAttribute(final String attributeName, final boolean isExternal) {
    registerAttribute(attributeName, isExternal, 0);
  }

  public boolean hasExternalSecrets() {
    return hasExternalSecrets;
  }

  public void registerDemand(final String moduleName, final String attributeName) {
    if (set.getProperty(attributeName).external) {
      throw new RuntimeException("Cannot require() external attributes.");
    }
    dependencyResolver.registerDemand(moduleName, attributeName);
  }

  public void registerSupply(final String moduleName, final String attributeName) {
    if (set.getProperty(attributeName).external) {
      throw new RuntimeException("Cannot provide() external attributes.");
    }
    dependencyResolver.registerSupply(moduleName, attributeName);
  }

  public List<String> sortModules(final Set<String> listOfModules) throws TopologicalSortFailedException {
    for (final String moduleName : listOfModules) {
      dependencyResolver.registerModule(moduleName);
    }
    return dependencyResolver.sortModulesOrThrow(set);
  }


  public void overrideRValueOfAttribute(final String attributeName, final BigInt rValue) {
    // Testing only
    final AttributeProperty ap = set.getProperty(attributeName);
    ap.overrideRValue = rValue;
  }


  public void addLinearCombination(final LinearCombination linComb) {
    final AttributeProperty ap = set.getProperty(linComb.lhsAttribute);
    ap.linearCombinations.add(linComb);
  }


  public void checkLinearCombinations() throws ProofException {
    for (final AttributeProperty ap : set.getProperties()) {
      if (!ap.revealed && ap.linearCombinations.size() > 1) {
        throw new ProofException("An unrevealed attribute cannot be computed via several "
            + "linear combination blocks.");
      }
      for (final LinearCombination lc : ap.linearCombinations) {
        boolean allRevealed = true;
        BigInt actualValue = lc.constant;
        for (final Term term : lc.terms) {
          final AttributeProperty termAp = set.getProperty(term.attribute);
          allRevealed = allRevealed && termAp.revealed;
          actualValue = actualValue.add((term.constant).multiply(termAp.value));
          if (termAp.representativeName.equals(ap.representativeName)) {
            throw new ProofException("The rhs attribute " + term.attribute
                + " is equal to the lhs " + "attribute in linear combination " + lc
                + ". This type of circular dependency is not permitted.");
          }
        }
        if (!actualValue.equals(ap.value)) {
          throw new ProofException("Linear combination " + lc + " does not hold. Actual = "
              + actualValue + " expected = " + ap.value);
        }
        if (ap.revealed && !allRevealed) {
          throw new ProofException("The lhs attribute in linear combination " + lc
              + " is revealed, but not all rhs attributes are revealed. "
              + "Please manually reveal the rhs attributes in the proof.");
        }
      }
    }
  }


  public void mergeResidueClass(String attributeName, ResidueClass residueClass) {
    set.getProperty(attributeName).mergeResidueClass(residueClass);
  }
  
  public ResidueClass getResidueClass(String attributeName) {
    return set.getProperty(attributeName).getResidueClass();
  }
}
