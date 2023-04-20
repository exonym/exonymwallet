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

import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.ProofException;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.group.Group;
import com.ibm.zurich.idmx.interfaces.util.group.GroupElement;

public interface ZkVerifierStateVerify {

  /**
   * This method is also for regular attributes.
   */
  public BigInt getSValueAsInteger(final String name) throws ProofException;

  public byte[] getSValueAsObject(final String name) throws ProofException;

  /**
   * This method MUST be called for EACH D-value the prover added with addDValue(String name, BigInt
   * value).
   */
  public BigInt getDValueAsInteger(final String name) throws ProofException;

  /**
   * This method MUST be called for EACH D-value the prover added with addDValue(String name,
   * GroupElement value).
   * @param <GE>
   */
  public <GE extends GroupElement<?,GE,?>>
  GE getDValueAsGroupElement(final String name, final Group<?,GE,?> g) throws ProofException;

  /**
   * This method MUST be called for EACH D-value the prover added with addDValue(String name, byte[]
   * value, byte[] hashContribution).
   */
  public byte[] getDValueAsObject(final String name) throws ProofException;

  public boolean isRevealedAttribute(final String name) throws ProofException;

  /**
   * Note that when calling this method for revealed attributes that were not added by this building
   * block (and its children, parents, and children of parents), the proof engine will automatically
   * add the attribute to the D-values.
   */
  public BigInt getValueOfRevealedAttribute(String name) throws ProofException;
  
  public ResidueClass getResidueClass(String attributeName) throws ProofException;

  public BigInt getChallenge() throws ProofException;

  public List<byte[]> getHashContributions() throws ProofException;

  public int getPositionInHashContributionsList() throws ProofException;

  /**
   * This method MUST be called for each attribute where you called getDValueAsObject.
   */
  public void checkHashContributionOfDValue(final String name, final byte[] hash) throws ProofException;

  /**
   * This method MUST be called for each N-value the prover added with the corresponding method.
   */
  public void checkNValue(final String name, final byte[] hashContribution) throws ProofException;

  /**
   * This method MUST be called for each N-value the prover added with the corresponding method.
   */
  public void checkNValue(final String name, final BigInt value) throws ProofException;

  /**
   * This method MUST be called for each N-value the prover added with the corresponding method.
   */
  public void checkNValue(final String name, final GroupElement<?,?,?> value) throws ProofException;

  /**
   * This method MUST be called for each N-value the prover added with the corresponding method.
   */
  public void checkTValue(final String name, final GroupElement<?,?,?> tValue) throws ProofException;

  /**
   * You should call this if and only if the prover called getHashContributionOfBuildingBlock()
   * during the proof. Note that while you technically could bypass the hash check by calling
   * getHashContributions.get(getPositionInHashContributionsList()), this is NOT something you
   * should do.
   */
  public void checkHashContributionOfBuildingBlock(final byte[] hashContribution) throws ProofException;

  public void checkValueOfAttribute(final String attributeId, final BigInt value) throws ProofException;

}
