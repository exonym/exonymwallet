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
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleVerifier;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkVerifierStateCollect;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkVerifierStateVerify;
import com.ibm.zurich.idmx.zkModule.ZkModuleImpl;

import eu.abc4trust.xml.SystemParameters;

public class VerifierModule extends ZkModuleImpl implements ZkModuleVerifier {

  private final String lhsAttribute;
  private final BigInt constant;
  private final List<Term> terms;
  private final EcryptSystemParametersWrapper sp;
  private final BigIntFactory bif;

  private final ZkModuleVerifier child;

  public VerifierModule(final LinearCombinationModQBuildingBlock parent, final String identifierOfModule,
                        final String lhsAttribute, final BigInt constant, final List<Term> terms, final SystemParameters sp,
      final BigIntFactory bif, final GroupFactory gf, final PedersenRepresentationBuildingBlock pedersen)
      throws ProofException, ConfigurationException {

    super(parent, identifierOfModule);

    this.lhsAttribute = lhsAttribute;
    this.constant = constant;
    this.terms = terms;
    this.sp = new EcryptSystemParametersWrapper(sp);
    this.bif = bif;

    final KnownOrderGroup group =
        gf.createPrimeOrderGroup(this.sp.getDHModulus(), this.sp.getDHSubgroupOrder());
    final BaseForRepresentation base =
        BaseForRepresentation.managedAttribute(group.valueOfNoCheck(this.sp.getDHGenerator1()));
    final List<BaseForRepresentation> bases = Collections.singletonList(base);
    final String identifierOfChild = getIdentifier() + ":rep";

    final KnownOrderGroupElement commitment = group.neutralElement();

    this.child =
        pedersen.getZkModuleVerifier(sp, identifierOfChild, bases, commitment, null, group);
  }



  @Override
  public void collectAttributesForVerify(final ZkVerifierStateCollect zkVerifier) throws ProofException,
      ConfigurationException {
    child.collectAttributesForVerify(zkVerifier);
    zkVerifier.registerAttribute(lhsAttribute, false);
    for (final Term term : terms) {
      zkVerifier.registerAttribute(term.attribute, false);
    }
    final List<Term> augmentedTerms = new ArrayList<Term>(terms);
    augmentedTerms.add(new Term(lhsAttribute, bif.one().negate()));
    zkVerifier.attributeLinearCombination(child.identifierOfAttribute(0), constant, augmentedTerms);
  }

  @Override
  public boolean verify(final ZkVerifierStateVerify zkVerifier) throws ConfigurationException,
      ProofException {
    return child.verify(zkVerifier);
  }

}
