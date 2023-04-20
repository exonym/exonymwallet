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

import com.ibm.zurich.idmx.interfaces.buildingBlock.structural.linearCombination.Term;
import com.ibm.zurich.idmx.interfaces.device.DeviceProofSpecification;
import com.ibm.zurich.idmx.interfaces.util.BigInt;

/**
 * 
 */
public interface ZkProofStateInitialize {

  /**
   * Indicates that this module will use the given attribute during the proof. The isExternal flag
   * is used for attributes that are on external devices (such as smartcards). By default, the value
   * of the attribute is between 0 and q-1 (where q is the subgroup order in the system parameters).
   */
  public void registerAttribute(final String attributeName, final boolean isExternal);

  /**
   * Indicates that this module will use the given attribute during the proof. The isExternal flag
   * is used for attributes that are on external devices (such as smartcards). By default, the value
   * of the attribute is between 0 and 2^bitLength-1.
   */
  public void registerAttribute(final String attributeName, final boolean isExternal, final int bitLength);

  /**
   * Indicates that this module needs to know the value of the given attribute in the
   * collectAttributesForProof() function. Modules will be topologically sorted to guarantee that
   * this value is available.
   */
  public void requiresAttributeValue(final String attributeName);

  /**
   * indicates that this module will provide the value of the given attribute later, namely when the
   * module is called again though the collectAttributesForProof() function (using the attribute
   * values it required with requiresAttributeValue). In case of circular dependency an error is
   * raised. If this module knows the value of the attribute now already, it should call
   * setValueOfAttribute immediately instead.
   */
  public void providesAttribute(final String attributeName);

  /**
   * indicates that the given attribute must be revealed by the proof engine.
   */
  public void attributeIsRevealed(final String attributeName);

  /**
   * indicates that the given attribute are to be treated as one and the same by the proof engine.
   * This method will fail if an external and a non-external attribute are passed as arguments. In
   * case the two attributes have a different size, the proof engine takes the maximum of the
   * declared sizes of the attributes.
   */
  public void attributesAreEqual(final String attributeName1, final String attributeName2);

  /**
   * Indicates that the given attribute is equal to a linear combination of the attributes in the
   * list plus a constant. The proof engine will choose the R-values of all attributes accordingly.
   * The proof engine does *not* take care of computing the value of the left-hand-side attribute
   * (but have a look at LinearCombinationBuildingBlock, which does compute that value). The
   * following constraints must be respected: (1) It is acceptable for the left-hand-side attribute
   * to be outside of the normal range for attributes (i.e., it may be negative). (2) If the
   * left-hand-side (lhs) attribute is revealed, then ALL attributes in the linear combination must
   * be revealed (this is not done automatically). (3) It is acceptable to chain multiple linear
   * combination building blocks (i.e., the lhs in one block appears in the right hand side of
   * another block), but there must be no circular dependencies. (4) It is not acceptable for an
   * attribute to appear as the left-hand-side of two distinct blocks. (5) It is not permissible for
   * an attribute to appear both in the left-hand-side and the right-hand-side (after considering
   * which attributes are declared equal).
   */
  public void attributeLinearCombination(final String attributeName, final BigInt constant,
		  final List<Term> attributeNameAndMultiplicationFactor);

  /**
   * sets the value of the given attribute, so that the proof engine can share this value with other
   * modules.
   */
   
  public void setValueOfAttribute(final String attributeName, final BigInt attributeValue, final ResidueClass residueClass);

  /**
   * Returns the current proof specification object for external secrets, so that the caller can
   * register a proof to be done by the external devices.
   */
  public DeviceProofSpecification getDeviceProofSpecification();

  /**
   * For testing only. Fix the R-Value of the given attribute.
   * 
   * @param attributeName
   * @param rValue
   */
  public void overrideRValueOfAttribute(final String attributeName, final BigInt rValue);

  /**
   * Marks this building block as a signature building block.
   * The first signature building block will be moved to the front of the hash contributions list.
   */
  public void markAsSignatureBuildingBlock();
}
