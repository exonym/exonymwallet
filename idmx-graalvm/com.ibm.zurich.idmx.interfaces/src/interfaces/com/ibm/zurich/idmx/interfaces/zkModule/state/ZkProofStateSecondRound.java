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

import com.ibm.zurich.idmx.interfaces.device.DeviceProofResponse;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.group.GroupElement;

/**
 * 
 */
public interface ZkProofStateSecondRound {

  public BigInt getChallenge();

  public int getPositionInHashContributionsList();

  public boolean isRevealedAttribute(final String attributeName);

  public boolean isValueOfAttributeAvailable(final String attributeName);

  public BigInt getValueOfAttribute(final String attributeName);
  
  public ResidueClass getResidueClass(final String attributeName);

  public BigInt getRValueOfAttribute(final String attributeName);

  public GroupElement<?,?,?> getDValueAsGroupElement(final String name);

  public BigInt getDValueAsInteger(final String name);

  public byte[] getDValueAsObject(final String name);

  /**
   * Set S-value of external attributes (eg. on smartcard). Do not use for regular attributes.
   */
  public void setSValueOfExternalAttribute(final String name, final BigInt sValue);

  /**
   * Call this method to add SValues for private/internal attributes. Do not use for regular
   * attributes.
   */
  public void addSValue(final String attributeName, final BigInt sValue);

  /**
   * Call this method to add SValues for private/internal attributes. Do not use for regular
   * attributes.
   */
  public void addSValue(final String attributeName, final byte[] sValue);
  
  /**
   * Returns the current proof response object for external secrets, so that
   * the caller can recover the S-Values for the proofs performed by the external devices.
   */
  public DeviceProofResponse getDeviceProofResponse();
}
