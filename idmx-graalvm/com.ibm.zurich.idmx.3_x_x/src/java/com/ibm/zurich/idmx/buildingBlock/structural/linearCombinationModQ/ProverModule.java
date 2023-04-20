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

package com.ibm.zurich.idmx.buildingBlock.structural.linearCombinationModQ;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.ibm.zurich.idmx.buildingBlock.helper.BaseForRepresentation;
import com.ibm.zurich.idmx.buildingBlock.helper.representation.pedersen.PedersenRepresentationBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.systemParameters.EcryptSystemParametersWrapper;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.ProofException;
import com.ibm.zurich.idmx.interfaces.buildingBlock.structural.linearCombination.Term;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.group.GroupFactory;
import com.ibm.zurich.idmx.interfaces.util.group.KnownOrderGroup;
import com.ibm.zurich.idmx.interfaces.util.group.KnownOrderGroupElement;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProver;
//import com.ibm.zurich.idmx.interfaces.zkModule.state.ResidueClass;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateCollect;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateFirstRound;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateInitialize;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkProofStateSecondRound;
import com.ibm.zurich.idmx.zkModule.ZkModuleImpl;

import eu.abc4trust.xml.SystemParameters;

public class ProverModule extends ZkModuleImpl implements ZkModuleProver {

  private final String lhsAttribute;
  private final BigInt constant;
  private final List<Term> terms;
  private final EcryptSystemParametersWrapper sp;
  private final BigIntFactory bif;

  private final ZkModuleProver child;

  public ProverModule(final LinearCombinationModQBuildingBlock parent, final String identifierOfModule,
                      final String lhsAttribute, final BigInt constant, final List<Term> terms, final SystemParameters sp,
      final BigIntFactory bif, final GroupFactory gf, final PedersenRepresentationBuildingBlock pedersen)
      throws ConfigurationException {

    super(parent, identifierOfModule);

    this.lhsAttribute = lhsAttribute;
    this.constant = constant;
    this.terms = terms;
    this.sp = new EcryptSystemParametersWrapper(sp);
    this.bif = bif;


    // In this ZkModule, we transform the equation
    // A = C + sum(c_i * a_i) (mod q)
    // into the equation
    // B = C + sum(c_i * a_i) - A
    // and then do a zero-knowledge proof that
    // 1 = g^B (mod p)
    // This allows us to bypass most of the restrictions on that the "light" version of the
    // linear combination had.

    final KnownOrderGroup group =
        gf.createPrimeOrderGroup(this.sp.getDHModulus(), this.sp.getDHSubgroupOrder());
    final BaseForRepresentation base =
        BaseForRepresentation.managedAttribute(group.valueOfNoCheck(this.sp.getDHGenerator1()));
    final List<BaseForRepresentation> bases = Collections.singletonList(base);
    final String identifierOfChild = getIdentifier() + ":rep";

    final KnownOrderGroupElement commitment = group.neutralElement();

    this.child =
        pedersen.getZkModuleProver(sp, identifierOfChild, null, bases, group, commitment, null,
            null, null);
  }


  @Override
  public void initializeModule(final ZkProofStateInitialize zkBuilder) throws ConfigurationException {
    child.initializeModule(zkBuilder);

    zkBuilder.registerAttribute(lhsAttribute, false);
    zkBuilder.providesAttribute(lhsAttribute);
    zkBuilder.providesAttribute(child.identifierOfAttribute(0));
    for (final Term term : terms) {
      zkBuilder.registerAttribute(term.attribute, false);
      zkBuilder.requiresAttributeValue(term.attribute);
    }
    final List<Term> augmentedTerms = new ArrayList<Term>(terms);
    augmentedTerms.add(new Term(lhsAttribute, bif.one().negate()));
    zkBuilder.attributeLinearCombination(child.identifierOfAttribute(0), constant, augmentedTerms);
  }

  @Override
  public void collectAttributesForProof(final ZkProofStateCollect zkBuilder) {
    try {
      BigInt value = constant;
      for (final Term term : terms) {
        final BigInt attValue = zkBuilder.getValueOfAttribute(term.attribute);
        final BigInt termValue = attValue.multiply(term.constant);
        value = value.add(termValue);
      }
      final BigInt q = sp.getDHSubgroupOrder();
      final BigInt reducedValue = value.mod(q);
      zkBuilder.setValueOfAttribute(lhsAttribute, reducedValue, null);
      value = value.add(reducedValue.negate());
      zkBuilder.setValueOfAttribute(child.identifierOfAttribute(0), value, null);

      child.collectAttributesForProof(zkBuilder);
    } catch (final ConfigurationException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void firstRound(final ZkProofStateFirstRound zkBuilder) throws ConfigurationException,
      ProofException {
    child.firstRound(zkBuilder);
  }

  @Override
  public void secondRound(final ZkProofStateSecondRound zkBuilder) throws ConfigurationException {
    child.secondRound(zkBuilder);
  }

}
