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

package com.ibm.zurich.idmx.interfaces.buildingBlock.revocation;

import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.interfaces.orchestration.issuance.State;
import com.ibm.zurich.idmx.interfaces.util.BigInt;

import eu.abc4trust.xml.PublicKey;
import eu.abc4trust.xml.RevocationHistory;
import eu.abc4trust.xml.RevocationState;


public interface StateRevocationAuthority extends State {

  public RevocationState getRevocationState();

  public void setRevocationState(final RevocationState revocationState);

  public BigInt generateRevocationHandle(final PublicKey publicKey) throws ConfigurationException;

  public RevocationHistory getHistory();

  public void setHistory(final RevocationHistory revocationHistory);

}
