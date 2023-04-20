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
import com.ibm.zurich.idmx.exception.ProofException;
import com.ibm.zurich.idmx.interfaces.buildingBlock.structural.linearCombination.Term;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleVerifier;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkVerifierStateCollect;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkVerifierStateVerify;
import com.ibm.zurich.idmx.zkModule.ZkModuleImpl;

public class VerifierModule extends ZkModuleImpl implements ZkModuleVerifier {

  private final String lhsAttribute;
  private final BigInt constant;
  private final List<Term> terms;

  public VerifierModule(final LinearCombinationLightBuildingBlock parent, final String identifierOfModule,
                        final String lhsAttribute, final BigInt constant, final List<Term> terms) {

    super(parent, identifierOfModule);

    this.lhsAttribute = lhsAttribute;
    this.constant = constant;
    this.terms = terms;
  }



  @Override
  public void collectAttributesForVerify(final ZkVerifierStateCollect zkVerifier) throws ProofException {
    zkVerifier.registerAttribute(lhsAttribute, false);
    for (final Term term : terms) {
      zkVerifier.registerAttribute(term.attribute, false);
    }
    zkVerifier.attributeLinearCombination(lhsAttribute, constant, terms);
  }

  @Override
  public boolean verify(final ZkVerifierStateVerify zkVerifier) throws ConfigurationException,
      ProofException {
    // Nothing to do
    return true;
  }

}
