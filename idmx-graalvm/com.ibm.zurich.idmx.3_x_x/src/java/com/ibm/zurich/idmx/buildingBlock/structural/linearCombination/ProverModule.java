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

package com.ibm.zurich.idmx.buildingBlock.structural.linearCombination;

import java.util.List;

import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.interfaces.buildingBlock.structural.linearCombination.Term;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProver;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateCollect;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateFirstRound;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateInitialize;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateSecondRound;
import com.ibm.zurich.idmx.zkModule.ZkModuleImpl;

public class ProverModule extends ZkModuleImpl implements ZkModuleProver {

  private final String lhsAttribute;
  private final BigInt constant;
  private final List<Term> terms;

  public ProverModule(final LinearCombinationLightBuildingBlock parent, final String identifierOfModule,
                      final String lhsAttribute, final BigInt constant, final List<Term> terms) {

    super(parent, identifierOfModule);

    this.lhsAttribute = lhsAttribute;
    this.constant = constant;
    this.terms = terms;
  }


  @Override
  public void initializeModule(final ZkProofStateInitialize zkBuilder) {
    zkBuilder.registerAttribute(lhsAttribute, false);
    zkBuilder.providesAttribute(lhsAttribute);
    for (final Term term : terms) {
      zkBuilder.registerAttribute(term.attribute, false);
      zkBuilder.requiresAttributeValue(term.attribute);
    }
    zkBuilder.attributeLinearCombination(lhsAttribute, constant, terms);
  }

  @Override
  public void collectAttributesForProof(final ZkProofStateCollect zkBuilder) {
    BigInt value = constant;
    for (final Term term : terms) {
      final BigInt attValue = zkBuilder.getValueOfAttribute(term.attribute);
      final BigInt termValue = attValue.multiply(term.constant);
      value = value.add(termValue);
    }
    zkBuilder.setValueOfAttribute(lhsAttribute, value, null);
  }

  @Override
  public void firstRound(final ZkProofStateFirstRound zkBuilder) throws ConfigurationException {
    // Nothing to do
  }

  @Override
  public void secondRound(final ZkProofStateSecondRound zkBuilder) {
    // Nothing to do
  }

}
