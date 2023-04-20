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
package com.ibm.zurich.idmx.interfaces.orchestration.issuance;

import java.net.URI;

import com.ibm.zurich.idmx.exception.IssuanceOrchestrationException;

public interface StateStorage<STATE extends State> {
  /**
   * Stores the given issuance state into the state storage.
   * This function throws an exception if an object with the given issuance state is already
   * present.
   * @param issuanceContext
   * @param state
   * @throws IssuanceOrchestrationException 
   */
  void storeState(final URI issuanceContext, final STATE state) throws IssuanceOrchestrationException;
  
  /**
   * Retrieves the issuance state with the given context from storage, and deletes it from
   * storage.
   * If no such state exists, null is returned.
   * @param issuanceContext
   * @return
   * @throws IssuanceOrchestrationException 
   */
  STATE retrieveAndDeleteState(final URI issuanceContext);
}
