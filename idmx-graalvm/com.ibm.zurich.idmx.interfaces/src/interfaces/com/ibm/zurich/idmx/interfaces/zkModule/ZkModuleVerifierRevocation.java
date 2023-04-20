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

import com.ibm.zurich.idmx.exception.ConfigurationException;

import eu.abc4trust.xml.NonRevocationEvidence;

public interface ZkModuleVerifierRevocation extends ZkModuleVerifier {

  /**
   * Returns the non-revocation evidence from the proof. This method may be called only after the
   * proof has been verified.
   * 
   * @throws ConfigurationException
   */
  public NonRevocationEvidence getNonRevocationEvidence() throws ConfigurationException;
}
