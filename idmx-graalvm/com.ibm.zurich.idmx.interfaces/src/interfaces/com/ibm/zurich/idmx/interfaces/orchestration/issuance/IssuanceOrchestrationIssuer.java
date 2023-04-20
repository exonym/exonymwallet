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
import com.ibm.zurich.idmx.exception.IssuanceOrchestrationException;

import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.IssuanceMessage;
import eu.abc4trust.xml.IssuanceMessageAndBoolean;
import eu.abc4trust.xml.IssuancePolicy;
import eu.abc4trust.xml.IssuanceTokenAndIssuancePolicy;
import eu.abc4trust.xml.IssuanceTokenDescription;


public interface IssuanceOrchestrationIssuer {

  /**
   * This method does the first step in an issuance proof. For subsequent steps, issuanceStep() must
   * be called. If the provided context is NULL, the context will be generated at random (and be set
   * inside the issuance message). This method also returns true if this is the last message of the
   * issuance. This method also returns the URI of the log entry that was stored during the issuance
   * protocol.
   * 
   * @throws IssuanceOrchestrationException
   */
  public IssuanceMessageAndBoolean initializeIssuance(final IssuancePolicy issuancePolicy,
		  final List<Attribute> issuerProvidedAttributes, final @Nullable URI context)
      throws IssuanceOrchestrationException;


  public IssuanceMessageAndBoolean issuanceStep(final IssuanceMessage issuanceMessage)
      throws IssuanceOrchestrationException;

  /**
   * This method looks for an IssuanceTokenDescription inside the issuance message. This method
   * returns the issuance token, or NULL if none could be found. It is guaranteed that this method
   * returns a non-null value before a new credential is actually issued, so that the upper layers
   * may abort the issuance protocol if a certain condition is not satisfied (such as the absence of
   * a registered pseudonym).
   */
  public IssuanceTokenDescription extractIssuanceTokenDescription(final IssuanceMessage issuanceMessage);

  /**
   * If the given issuance message contains an issuance token, this method returns said issuance
   * token together with the issuance policy this issuer was initialized with. Otherwise return
   * null. If this method returns non-null, the caller is expected to check the issuance token
   * against the issuance policy. It is mandatory to call this method before each call to
   * issuanceStep (otherwise the issuer will fail during verification of the issuance token).
   * 
   * @throws IssuanceOrchestrationException
   */
  @Nullable
  public IssuanceTokenAndIssuancePolicy extractIssuanceTokenAndPolicy(
		  final IssuanceMessage issuanceMessage) throws IssuanceOrchestrationException;

}
