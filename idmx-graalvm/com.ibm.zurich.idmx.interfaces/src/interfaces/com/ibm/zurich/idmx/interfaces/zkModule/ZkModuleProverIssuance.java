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

package com.ibm.zurich.idmx.interfaces.zkModule;

import java.util.List;

import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.interfaces.state.IssuanceStateIssuer;
import com.ibm.zurich.idmx.interfaces.util.BigInt;

public interface ZkModuleProverIssuance extends ZkModuleProver {

  /**
   * Return a list of all attributes that were were set by the issuer, plus all attributes that were
   * carried over and revealed. This method may be called only after the proof is done.
   */
  public List</* Nullable */BigInt> recoverEncodedAttributes();

  /**
   * Returns arbitrary information that must be understood by extraIssuanceRoundIssuer(). This
   * information contains (among others) the list of encoded attributes (as above). This method may
   * be called only after the proof is done.
   * 
   * @throws ConfigurationException
   */
  public IssuanceStateIssuer recoverIssuanceState() throws ConfigurationException;
}
