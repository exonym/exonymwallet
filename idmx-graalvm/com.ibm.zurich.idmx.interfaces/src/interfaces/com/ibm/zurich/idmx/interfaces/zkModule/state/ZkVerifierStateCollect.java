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

package com.ibm.zurich.idmx.interfaces.zkModule.state;

import java.util.List;

import com.ibm.zurich.idmx.exception.ProofException;
import com.ibm.zurich.idmx.interfaces.buildingBlock.structural.linearCombination.Term;
import com.ibm.zurich.idmx.interfaces.util.BigInt;

public interface ZkVerifierStateCollect {

  public void registerAttribute(final String name, final boolean isExternal) throws ProofException;
  
  public void registerAttribute(final String name, final boolean isExternal, final int bitsize) throws ProofException;

  public void attributeIsRevealed(final String name) throws ProofException;

  public void attributesAreEqual(final String attributeName1, final String attributeName2)
      throws ProofException;

  public void attributeLinearCombination(final String attributeName, final BigInt constant,
		  final List<Term> attributeNameAndMultiplicationFactor) throws ProofException;
  
  /**
   * Call this method if the prover called setValueOfAttribute(attributeName, ..., residueClass)
   * for residueClass != UNSPECIFIED / null in the corresponding prover module.
   * @param attributeName
   * @throws ProofException 
   */
  public void setResidueClass(String attributeName, ResidueClass residueClass) throws ProofException;

  
  /**
   * This method MUST be called for EACH D-value the prover added with addDValue(String name, byte[]
   * value, byte[] hashContribution).
   */
  public byte[] getDValueAsObject(final String name) throws ProofException;
}
