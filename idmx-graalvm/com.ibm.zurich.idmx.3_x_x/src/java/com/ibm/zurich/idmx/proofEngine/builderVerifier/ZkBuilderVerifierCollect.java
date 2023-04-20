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

import java.util.List;

import com.ibm.zurich.idmx.exception.ProofException;
import com.ibm.zurich.idmx.interfaces.buildingBlock.structural.linearCombination.LinearCombination;
import com.ibm.zurich.idmx.interfaces.buildingBlock.structural.linearCombination.Term;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ResidueClass;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkVerifierStateCollect;

class ZkBuilderVerifierCollect implements ZkVerifierStateCollect {

  private final StatePerModule state;
  private final StateForVerifier verifierState;

  public ZkBuilderVerifierCollect(final StatePerModule moduleState, final StateForVerifier state) {
    this.state = moduleState;
    this.verifierState = state;
  }

  @Override
  public void registerAttribute(final String name, final boolean isExternal) throws ProofException {
    try {
      verifierState.registerAttribute(name, isExternal);
      state.registerAttribute(name);
    } catch (ProofException e) {
      state.fail();
      throw e;
    }
  }

  @Override
  public void registerAttribute(final String name, final boolean isExternal, final int bitLength)
      throws ProofException {
    try {
      verifierState.registerAttribute(name, isExternal, bitLength);
      state.registerAttribute(name);
    } catch (ProofException e) {
      state.fail();
      throw e;
    }
  }

  @Override
  public void attributeIsRevealed(final String name) throws ProofException {
    try {
      state.assertRegistered(name);
      verifierState.attributeIsRevealed(name);
    } catch (ProofException e) {
      state.fail();
      throw e;
    }
  }

  @Override
  public void attributesAreEqual(final String attributeName1, final String attributeName2)
      throws ProofException {
    try {
      state.assertRegistered(attributeName1);
      state.assertRegistered(attributeName2);
      verifierState.attributesAreEqual(attributeName1, attributeName2);
      state.localAttributesAreEqual(attributeName1, attributeName2);
    } catch (ProofException e) {
      state.fail();
      throw e;
    }
  }

  @Override
  public void attributeLinearCombination(final String attributeName, final BigInt constant,
		  final List<Term> terms)
      throws ProofException {
    try {
      state.assertRegistered(attributeName);
      for (final Term term : terms) {
        state.assertRegistered(term.attribute);
      }
      final LinearCombination linComb = new LinearCombination(attributeName, constant, terms);
      verifierState.addLinearCombination(linComb);
    } catch (ProofException e) {
      state.fail();
      throw e;
    }
  }
  
  @Override
  public byte[] getDValueAsObject(final String name) throws ProofException {
    try {
      return state.getDValueAsByteArray(name);
    } catch (ProofException e) {
      state.fail();
      throw e;
    }
  }

  @Override
  public void setResidueClass(String attributeName, ResidueClass rc) throws ProofException  {
    if(rc == null) {
      rc = ResidueClass.UNSPECIFIED;
    }
    try {
      state.assertRegistered(attributeName);
      verifierState.mergeResidueClass(attributeName, rc);
    } catch (ProofException e) {
      state.fail();
      throw e;
    }
  }

}
