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
package com.ibm.zurich.idmx.orchestration.issuance;

import java.net.URI;
import java.util.LinkedHashMap;

import com.ibm.zurich.idmx.configuration.Configuration;
import com.ibm.zurich.idmx.exception.IssuanceOrchestrationException;
import com.ibm.zurich.idmx.interfaces.orchestration.issuance.State;
import com.ibm.zurich.idmx.interfaces.orchestration.issuance.StateStorage;

import javax.inject.Inject;


abstract public class StateStorageMap<STATE extends State> implements StateStorage<STATE> {

  private static final int MAX_CAPACITY = Configuration.maximumNumberOfIssuanceStates();
  private final LinkedHashMap<URI, STATE> store;

  public StateStorageMap() {
    store = new LinkedHashMap<URI, STATE>();
  }

  @Override
  public synchronized void storeState(final URI issuanceContext, final STATE state)
      throws IssuanceOrchestrationException {

    if (store.containsKey(issuanceContext)) {
      throw new IssuanceOrchestrationException("Issuance context exists already: "
          + issuanceContext);
    }
    store.put(issuanceContext, state);
    trimMapToCapacity();
  }

  private void trimMapToCapacity() {
    if (store.size() > MAX_CAPACITY) {
	  final URI firstContext = store.keySet().iterator().next();
      store.remove(firstContext);
    }
  }

  @Override
  public synchronized STATE retrieveAndDeleteState(final URI issuanceContext) {
    return store.remove(issuanceContext);
  }
}
