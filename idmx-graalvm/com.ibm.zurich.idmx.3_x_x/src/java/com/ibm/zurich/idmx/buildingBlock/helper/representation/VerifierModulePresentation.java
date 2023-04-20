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
package com.ibm.zurich.idmx.buildingBlock.helper.representation;

import java.util.ArrayList;
import java.util.List;

import com.ibm.zurich.idmx.annotations.Nullable;
import com.ibm.zurich.idmx.buildingBlock.GeneralBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.factory.BuildingBlockFactory;
import com.ibm.zurich.idmx.buildingBlock.helper.BaseForRepresentation;
import com.ibm.zurich.idmx.buildingBlock.systemParameters.EcryptSystemParametersWrapper;
import com.ibm.zurich.idmx.exception.ProofException;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.group.Group;
//import com.ibm.zurich.idmx.interfaces.util.group.Group;
import com.ibm.zurich.idmx.interfaces.util.group.GroupElement;
//import com.ibm.zurich.idmx.interfaces.util.group.HiddenOrderGroup;
import com.ibm.zurich.idmx.interfaces.util.group.MultOpSequence;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleVerifier;
import com.ibm.zurich.idmx.interfaces.zkModule.state.ZkVerifierStateVerify;
import com.ibm.zurich.idmx.zkModule.ZkModuleImpl;

import eu.abc4trust.xml.SystemParameters;

public abstract class VerifierModulePresentation<G extends Group<G, GE, M>, GE extends GroupElement<G, GE, M>, M extends MultOpSequence<G, GE, M>> extends ZkModuleImpl
    implements
      ZkModuleVerifier {
  protected final List<BaseForRepresentation> bases;
  protected GE commitment;
  protected String commitmentAsDValue;
  protected int indexOfSecret;
  protected int indexOfExternalRandomizer;
  protected final EcryptSystemParametersWrapper spWrapper;
  protected final G group;

  public VerifierModulePresentation(final GeneralBuildingBlock parent, final String identifierOfModule,
                                    final SystemParameters systemParameters, final List<BaseForRepresentation> bases, final G group,
      final @Nullable GE commitment, final @Nullable String commitmentAsDValue,
      final BuildingBlockFactory bbFactory) {
    super(parent, identifierOfModule);

    this.group = group;
    this.spWrapper = new EcryptSystemParametersWrapper(systemParameters);
    this.bases = bases;
    this.commitment = commitment;
    this.commitmentAsDValue = commitmentAsDValue;
    this.indexOfExternalRandomizer = -1;
    this.indexOfSecret = -1;
  }

  /*
   * check that
   * 
   * t*(C/PROD_revealed base^attribute)^-c = PROD_unrevealed base^s_i
   * 
   * this is equivalent to: t = PROD base^(attribute*c resp. s_i) * C^c
   * 
   * the latter equation just requires ONE multi-exponentiation call, and may therefore be evaluated
   * more efficiently
   */
  //@SuppressWarnings({"unchecked", "rawtypes"})
  @SuppressWarnings("unchecked")
  @Override
  public boolean verify(final ZkVerifierStateVerify zkVerifier) throws ProofException {
    // get commitment
    if(commitmentAsDValue == null) {
      commitmentAsDValue = getIdentifier() + ":C";
    }
    final GE C;
    
    if(commitment == null) {
      C = zkVerifier.getDValueAsGroupElement(commitmentAsDValue, group);
    } else {
      C = commitment;
    }


    final List<Boolean> isRevealedAttribute = new ArrayList<Boolean>();
    for (int i = 0; i < bases.size(); i++) {
      // determine which attributes were hidden and which were revealed
      isRevealedAttribute.add(zkVerifier.isRevealedAttribute(identifierOfAttribute(i)));
      // check all NValues, i.e., check that the bases are the same as
      // those the prover used
      zkVerifier.checkNValue(getIdentifier() + ":base:" + i, bases.get(i)
          .getBase(zkVerifier, group));
    }
    // check that group-description is correct
    zkVerifier.checkNValue(getIdentifier() + ":groupDescription", group.getGroupDescription());
    // get challenge
    final BigInt c = zkVerifier.getChallenge();
    // compute t as described before using the DValues
    final M tSeq = group.initializeSequence();
    for (int i = 0; i < bases.size(); i++) {
      if (isRevealedAttribute.get(i)) {
        tSeq.putMultOp((GE) bases.get(i).getBase(zkVerifier, group), zkVerifier
            .getValueOfRevealedAttribute(identifierOfAttribute(i)).multiply(c.negate()));
      } else {
        tSeq.putMultOp((GE) bases.get(i).getBase(zkVerifier, group),
            zkVerifier.getSValueAsInteger(identifierOfAttribute(i)));
      }
    }
    tSeq.putMultOp(C, c);
    final GE t = tSeq.finalizeSequence();

    zkVerifier.checkTValue(getIdentifier() + ":t", t);

    for (int i = 0; i < bases.size(); i++) {
      zkVerifier.checkNValue(getIdentifier() + ":base:" + i, bases.get(i)
          .getBase(zkVerifier, group));
    }
    zkVerifier.checkNValue(getIdentifier() + ":groupDescription", group.getGroupDescription());

    return true;
  }

  protected void findIndicesOfExternalAttributes() {
    // find index of secret key, and index of external randomizer
    for (int i = 0; i < bases.size(); i++) {
      if (bases.get(i).hasExternalSecret) {
        indexOfSecret = i;
      }
      if (bases.get(i).hasCredentialSecretKey) {
        indexOfExternalRandomizer = i;
      }
    }
  }

}
