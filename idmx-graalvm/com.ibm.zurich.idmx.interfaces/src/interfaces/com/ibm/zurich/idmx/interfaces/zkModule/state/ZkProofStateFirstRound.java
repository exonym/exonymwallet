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

import com.ibm.zurich.idmx.interfaces.device.DeviceProofCommitment;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.group.GroupElement;

/**
 * 
 */
public interface ZkProofStateFirstRound {

  public boolean isRevealedAttribute(final String attributeName);

  public boolean isValueOfAttributeAvailable(final String attributeName);

  public BigInt getValueOfAttribute(final String attributeName);

  public ResidueClass getResidueClass(final String attributeName);

  public BigInt getRValueOfAttribute(final String attributeName);

  /**
   * At this stage, this method may only be called for D-values (values delivered to the verifier)
   * that were generated by any of the (transitive closure) of the children of the top-level module
   * this module belongs to, and only if the top-level module guarantees that the D-value has
   * already been added at the point this method is called. This is because 1) the proof engine does
   * not provide any guarantees on the order in which the top-level modules get called, and 2) the
   * D-value must be available before this method is called.
   */
  public GroupElement<?,?,?> getDValueAsGroupElement(final String name);

  /**
   * See comment for getDValueAsGroupElement().
   */
  public byte[] getDValueAsObject(final String name);

  /**
   * See comment for getDValueAsGroupElement().
   */
  public BigInt getDValueAsInteger(final String name);

  public void addDValue(final String name, final GroupElement<?,?,?> value);

  public void addDValue(final String name, BigInt value);
  
  public void addDValue(final String name, final byte[] value);

  public void addDValue(final String name, final byte[] value, final byte[] hashContribution);

  void addNValue(final String name, final byte[] hashContribution);

  void addNValue(final String name, final BigInt value);

  void addNValue(final String name, final GroupElement<?,?,?> value);

  void addTValue(final String name, final GroupElement<?,?,?> tValue);

  /**
   * Only a top-level building block should call this method. You should call this method only if
   * you wish to compute the hash contribution of the current top-level building block manually, and
   * not via the added D-, T- and N- values. You should call this method only if you really know
   * what you are doing.
   */
  void setHashContributionOfBuildingBlock(final byte[] hashContribution);

  /**
   * Returns the current proof commitment object for external secrets, so that
   * the caller can recover the T-Values for the proofs performed by the external devices.
   */
  public DeviceProofCommitment getDeviceProofCommitment();
}
