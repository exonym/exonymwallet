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

import com.ibm.zurich.idmx.interfaces.device.DeviceProofSpecification;
import com.ibm.zurich.idmx.interfaces.util.BigInt;

public interface ZkProofStateCollect {

  /**
   * Indicates whether the given attribute is revealed.
   */
  public boolean isRevealedAttribute(final String attributeName);

  /**
   * Returns the value of the given attribute. You may only call this method if re
   * quiresAttributeValue() has been called in the previous round on the same attribute.
   */
  public BigInt getValueOfAttribute(final String attributeName);
  
  /**
   * Returns whether the given attribute is a residue class. You may only call this method if re
   * quiresAttributeValue() has been called in the previous round on the same attribute.
   */
  public ResidueClass getResidueClass(final String attributeName);

  /**
   * Sets the value of the given attribute. you must call this method for all attributes for which
   * you called providesAttributeValue() in the previous round.
   */
  public void setValueOfAttribute(final String attributeName, final BigInt attributeValue, final ResidueClass residueClass);
  
  /**
   * Returns the current proof specification object for external secrets, so that
   * the caller can register a proof to be done by the external devices.
   */
  public DeviceProofSpecification getDeviceProofSpecification();
}
