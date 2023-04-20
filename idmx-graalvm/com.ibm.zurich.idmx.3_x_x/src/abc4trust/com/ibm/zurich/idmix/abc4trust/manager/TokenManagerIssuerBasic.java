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
package com.ibm.zurich.idmix.abc4trust.manager;

import java.net.URI;

import eu.abc4trust.abce.internal.issuer.tokenManagerIssuer.TokenManagerIssuer;
import eu.abc4trust.xml.IssuanceLogEntry;
import eu.abc4trust.xml.IssuanceToken;
import eu.abc4trust.xml.PseudonymInToken;

public class TokenManagerIssuerBasic implements TokenManagerIssuer {

  @Override
  public boolean isEstablishedPseudonym(final PseudonymInToken p) {
    // TODO Auto-generated method stub
    return true;
  }

  @Override
  public URI storeToken(final IssuanceToken it) {
    // TODO Auto-generated method stub
    return URI.create("stored-token");
  }

  @Override
  public IssuanceLogEntry getIssuanceLogEntry(final URI issuanceDataUid) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean deleteIssuanceLogEntry(final URI issuanceDataUid) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public URI storeIssuanceLogEntry(final IssuanceLogEntry issuanceLogEntry) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  @Deprecated
  public IssuanceToken getToken(final URI tokenuid) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  @Deprecated
  public boolean deleteToken(final URI tokenuid) {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public void addPeudonymForTest(final byte[] pseudonymValue) {
    // TODO Auto-generated method stub
    
  }

}
