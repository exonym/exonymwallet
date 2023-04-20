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
import java.util.List;

import com.ibm.zurich.idmx.exception.ProofException;
import com.ibm.zurich.idmx.interfaces.buildingBlock.structural.linearCombination.LinearCombination;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ResidueClass;

class AttributeProperty implements Comparable<AttributeProperty> {
  //TODO Why is this public?
  public boolean revealed;
  public boolean external;
  public BigInt value;
  public BigInt rValue;
  public BigInt sValue;
  public String representativeName;
  public int bitLength;
  public final List<LinearCombination> linearCombinations;
  private ResidueClass residueClass;
  // For testing only
  public BigInt overrideRValue;

  public AttributeProperty() {
    linearCombinations = new ArrayList<LinearCombination>();
    residueClass = ResidueClass.UNSPECIFIED;
    // All other fields initialized to their default values
  }

  AttributeProperty merge(final AttributeProperty lhs) throws ProofException {
    AttributeProperty ret = new AttributeProperty();

    ret.revealed = this.revealed || lhs.revealed;

    if (this.external != lhs.external) {
      throw new RuntimeException("Cannot merge regular and external attributes.");
    }
    ret.external = this.external;

    // Representative: take lexicographically first
    if (this.representativeName.compareTo(lhs.representativeName) < 0) {
      ret.representativeName = this.representativeName;
    } else {
      ret.representativeName = lhs.representativeName;
    }

    ret.value = mustBeEqualOrNull(this.value, lhs.value, "values");
    ret.rValue = mustBeEqualOrNull(this.rValue, lhs.rValue, "R-Values");
    ret.overrideRValue =
        mustBeEqualOrNull(this.overrideRValue, lhs.overrideRValue, "override-R-Values");
    ret.sValue = mustBeEqualOrNull(this.sValue, lhs.sValue, "S-Values");

    ret.linearCombinations.addAll(this.linearCombinations);
    ret.linearCombinations.addAll(lhs.linearCombinations);
    
    // Integer overrides residue class
    ret.residueClass = this.residueClass.merge(lhs.residueClass);

    ret.bitLength = Math.max(this.bitLength, lhs.bitLength);

    return ret;
  }

  /**
   * If both parameters are NULL, return NULL. If one parameter is not NULL, return its value. If
   * both parameters are not NULL, check that they are in fact equal.
   * 
   * @param rhs
   * @param lhs
   * @param name
   * @return
   * @throws ProofException If both parameters are non-NULL and are not equal.
   */
  private <T> T mustBeEqualOrNull(final T rhs, final T lhs, final String name) throws ProofException {
    if (rhs == null) {
      return lhs;
    } else if (lhs == null) {
      return rhs;
    } else if (lhs.equals(rhs)) {
      return rhs;
    } else {
      throw new ProofException("Cannot merge attributes with different " + name + ".");
    }
  }

  @Override
  public int compareTo(final AttributeProperty lhs) {
    return this.representativeName.compareTo(lhs.representativeName);
  }

  public void mergeResidueClass(ResidueClass rc) {
    this.residueClass = this.residueClass.merge(rc);
  }

  public ResidueClass getResidueClass() {
    return this.residueClass;
  }
}
