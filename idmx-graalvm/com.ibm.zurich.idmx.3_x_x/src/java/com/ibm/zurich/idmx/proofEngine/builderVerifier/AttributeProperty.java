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
import java.util.List;

import com.ibm.zurich.idmx.exception.ProofException;
import com.ibm.zurich.idmx.interfaces.buildingBlock.structural.linearCombination.LinearCombination;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ResidueClass;



class AttributeProperty implements Comparable<AttributeProperty> {
	
  //TODO public parameters?
	
  public boolean revealed;
  private BigInt value;
  private BigInt sValue;
  public String representativeName;
  public int bitLength;
  private ResidueClass residueClass;
  public final List<LinearCombination> linearCombinations;
  
  public boolean rangeWasChecked;
  
  public AttributeProperty() {
    linearCombinations = new ArrayList<LinearCombination>();
    residueClass = ResidueClass.UNSPECIFIED;
    // All other fields initialized to their default value
  }

  AttributeProperty merge(final AttributeProperty lhs) throws ProofException {
	final AttributeProperty ret = new AttributeProperty();

    ret.revealed = this.revealed || lhs.revealed;
    ret.linearCombinations.addAll(this.linearCombinations);
    ret.linearCombinations.addAll(lhs.linearCombinations);
    ret.rangeWasChecked = false;
    
    ret.value = mustBeEqualOrNull(this.value, lhs.value, "values");
    ret.sValue = mustBeEqualOrNull(this.sValue, lhs.sValue, "S-Values");
    
    // Integer overrides residue class
    ret.residueClass = this.residueClass.merge(lhs.residueClass);

    if (this.representativeName.compareTo(lhs.representativeName) < 0) {
      ret.representativeName = this.representativeName;
    } else {
      ret.representativeName = lhs.representativeName;
    }
    
    if(ret.value != null && ret.sValue != null) {
      throw new ProofException("Attribute cannot have both a value and an S-Value.");
    }
    
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
  private <T> T mustBeEqualOrNull(final T rhs, final T lhs, final String name)
		  throws ProofException {
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

  void assignValue(final BigInt newValue) throws ProofException {
    if (value == null || value.equals(newValue)) {
      value = newValue;
    } else {
      throw new ProofException("Trying to assign incompatible value to attribute set "
          + representativeName);
    }
  }
  
  public BigInt getValue() {
    return value;
  }
  
  public BigInt getSValue() {
    return sValue;
  }
  
  void assignSValue(final BigInt newValue) throws ProofException {
    if (sValue == null || sValue.equals(newValue)) {
      sValue = newValue;
    } else {
      throw new ProofException("Trying to assign incompatible SValue to attribute set "
          + representativeName);
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
    return residueClass;
  }
}
