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
import java.util.List;

import com.ibm.zurich.idmx.annotations.Nullable;

import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.CredentialTemplate;
import eu.abc4trust.xml.IssuanceTokenDescription;

public interface StateRecipient extends State {
  
  public PhaseRecipient getNextExpectedPhase();

  @Nullable
  public List<URI> getCredentialUrisForPresentation();

  @Nullable
  public List<URI> getPseudonymUrisForPresentation();

  @Nullable
  public List<Attribute> getSelfClaimedAttributes();
  
  @Nullable
  public IssuanceTokenDescription getIssuanceTokenDescription();

  public CredentialTemplate getCredentialTemplate();
}
