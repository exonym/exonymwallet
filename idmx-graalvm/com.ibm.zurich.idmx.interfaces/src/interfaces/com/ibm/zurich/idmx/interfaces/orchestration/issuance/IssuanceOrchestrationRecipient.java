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

import com.ibm.zurich.idmx.exception.IssuanceOrchestrationException;

import eu.abc4trust.returnTypes.IssuMsgOrCredDesc;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.IssuanceMessage;
import eu.abc4trust.xml.IssuancePolicy;
import eu.abc4trust.xml.IssuanceTokenDescription;

/**
 * 
 */
public interface IssuanceOrchestrationRecipient {

  /**
   * This method does the first step in an issuance proof. For subsequent steps, issuanceStep() must
   * be called. For "simple issuance", the verifier parameters will be NULL, and all three lists
   * empty.
   */
  public IssuanceMessage preIssuancePresentation(final String username, final IssuanceMessage issuanceMessage,
		  final IssuanceTokenDescription issuanceTokenDescription, final List<URI> listOfCredentialIds,
      final List<URI> listOfPseudonyms, final List<Attribute> userProvidedAttributes)
      throws IssuanceOrchestrationException;

  /**
   * This method continues the issuance proof.
   */
  public IssuMsgOrCredDesc issuanceStep(final String username, final IssuanceMessage issuanceMessage)
      throws IssuanceOrchestrationException;


  public IssuancePolicy extractIssuancePolicy(final IssuanceMessage issuanceMessage);
}
