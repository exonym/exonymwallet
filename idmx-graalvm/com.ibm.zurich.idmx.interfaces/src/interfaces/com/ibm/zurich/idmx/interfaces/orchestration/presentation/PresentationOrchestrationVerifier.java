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

package com.ibm.zurich.idmx.interfaces.orchestration.presentation;

import com.ibm.zurich.idmx.exception.PresentationOrchestrationException;
import com.ibm.zurich.idmx.interfaces.state.CarryOverStateIssuer;
import com.ibm.zurich.idmx.interfaces.util.Pair;

import eu.abc4trust.xml.IssuanceToken;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.VerifierParameters;


/**
 * 
 */
public interface PresentationOrchestrationVerifier {

  public boolean verifyProof(final PresentationToken presentationToken, final VerifierParameters vp)
      throws PresentationOrchestrationException;

  public Pair<Boolean, CarryOverStateIssuer> verifyProof(final IssuanceToken issuanceToken, final VerifierParameters vp)
      throws PresentationOrchestrationException;

}
