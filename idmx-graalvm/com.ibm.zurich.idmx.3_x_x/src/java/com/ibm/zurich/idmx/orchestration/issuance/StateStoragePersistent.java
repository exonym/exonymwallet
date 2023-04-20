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

import com.ibm.zurich.idmx.exception.IssuanceOrchestrationException;
import com.ibm.zurich.idmx.interfaces.orchestration.issuance.State;
import com.ibm.zurich.idmx.interfaces.orchestration.issuance.StateStorage;
import com.ibm.zurich.idmx.interfaces.util.ByteSerializer;

import eu.abc4trust.db.PersistentStorage;
import eu.abc4trust.db.SimpleParamTypes;

public class StateStoragePersistent<T extends State> implements StateStorage<T>{

  private final PersistentStorage ps;
  private final SimpleParamTypes table;
  
  public StateStoragePersistent(PersistentStorage ps, SimpleParamTypes table) {
    this.ps = ps;
    this.table = table;
  }
  
  @Override
  public void storeState(URI issuanceContext, T state)
      throws IssuanceOrchestrationException {
    boolean ok = ps.insertItem(table, issuanceContext,
      ByteSerializer.writeAsBytes(state));
    if(!ok) {
      throw new IssuanceOrchestrationException("Cannot store state " + issuanceContext);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public T retrieveAndDeleteState(URI issuanceContext) {
    return (T) ByteSerializer.readFromBytes(ps.getItemAndDelete(table, issuanceContext));
  }

}
