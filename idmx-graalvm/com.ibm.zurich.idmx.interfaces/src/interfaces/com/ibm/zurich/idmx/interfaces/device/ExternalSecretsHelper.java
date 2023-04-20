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
package com.ibm.zurich.idmx.interfaces.device;

import java.math.BigInteger;
import java.net.URI;

/**
 * Helper class for the verifier for operations on external devices.
 */
public interface ExternalSecretsHelper {
  /**
   * Returns the base used for scope exclusive pseudonyms for the given scope, modulus and subgroup
   * order. Returns (hash(scope)^cofactor) (mod p), where cofactor = (p-1)/subgroupOrder
   * 
   * @param scope
   * @param modulus The value p
   * @param subgroupOrder
   * @return
   */
  public BigInteger getBaseForScopeExclusivePseudonym(final URI scope, final BigInteger modulus,
		  final BigInteger subgroupOrder);
}
