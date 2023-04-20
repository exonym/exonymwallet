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

import java.util.List;

import com.ibm.zurich.idmx.buildingBlock.GeneralBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.factory.BuildingBlockFactory;
import com.ibm.zurich.idmx.buildingBlock.helper.representation.pedersen.PedersenRepresentationBuildingBlock;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.ProofException;
import com.ibm.zurich.idmx.interfaces.buildingBlock.structural.linearCombination.Term;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.group.GroupFactory;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProver;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleVerifier;

import eu.abc4trust.xml.SystemParameters;

import javax.inject.Inject;

/**
 * This building block declares a linear combination among several attributes (modulo the DH
 * subgroup order in the system parameters) . This block takes care of computing the value of the
 * left-hand-side attribute based on the right hand side. In contrast to the "light" building block,
 * it is acceptable for the lhs attribute to be revealed when not all the rhs-attributes are, for
 * one attribute to appear as the lhs-attribute of multiple linear combination blocks. The
 * constraint on circular dependencies is relaxed; those are permissible if some other block
 * provides the value of the lhs-attribute. This flexibility comes at a cost of doing a proof of a
 * one-base exponentiation.
 */
public class LinearCombinationModQBuildingBlock extends GeneralBuildingBlock {

  private final BigIntFactory bif;
  private final BuildingBlockFactory bbf;
  private final GroupFactory gf;

  @Inject
  public LinearCombinationModQBuildingBlock(final BigIntFactory bif, final BuildingBlockFactory bbf,
                                            final GroupFactory gf) {
    this.bif = bif;
    this.bbf = bbf;
    this.gf = gf;
  }

  @Override
  protected String getBuildingBlockIdSuffix() {
    return "s-linear-modq";
  }

  @Override
  protected String getImplementationIdSuffix() {
    return "s-linear-modq";
  }

  public ZkModuleProver getZkModuleProver(final String identifierOfModule, final String lhsAttribute,
                                          final BigInt constant, final List<Term> terms, final SystemParameters sp) {
    try {
      final PedersenRepresentationBuildingBlock pedersen =
          bbf.getBuildingBlockByClass(PedersenRepresentationBuildingBlock.class);
      return new ProverModule(this, identifierOfModule, lhsAttribute, constant, terms, sp, bif,
          gf, pedersen);
    } catch (final ConfigurationException e) {
      throw new RuntimeException(e);
    }
  }

  public ZkModuleVerifier getZkModuleVerifier(final String identifierOfModule, final String lhsAttribute,
                                              final BigInt constant, final List<Term> terms, final SystemParameters sp) {
    try {
      final PedersenRepresentationBuildingBlock pedersen =
          bbf.getBuildingBlockByClass(PedersenRepresentationBuildingBlock.class);
      return new VerifierModule(this, identifierOfModule, lhsAttribute, constant, terms, sp, bif, gf, 
          pedersen);
    } catch (ConfigurationException|ProofException e) {
      throw new RuntimeException(e);
    }
  }

}
