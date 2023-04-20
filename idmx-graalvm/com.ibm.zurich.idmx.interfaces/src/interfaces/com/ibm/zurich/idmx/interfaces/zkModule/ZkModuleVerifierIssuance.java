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
import com.ibm.zurich.idmx.interfaces.state.IssuanceStateRecipient;
import com.ibm.zurich.idmx.interfaces.util.BigInt;

public interface ZkModuleVerifierIssuance extends ZkModuleVerifier {
  /**
   * Returns a list of all attributes in the signature. This method may only be called after the
   * proof verification has been completed.
   */
  public List</* Nullable */BigInt> recoverAttributes();

  /**
   * returns arbitrary informa- tion that must be understood by the concrete instantiation of
   * extraIssuanceRoundRecipient() and recoverSignature(). This information contains (among others)
   * the list of encoded attributes. This method may be called only after the proof verification has
   * been completed.
   * @throws ConfigurationException 
   */
  public IssuanceStateRecipient recoverIssuanceState() throws ConfigurationException;
}
