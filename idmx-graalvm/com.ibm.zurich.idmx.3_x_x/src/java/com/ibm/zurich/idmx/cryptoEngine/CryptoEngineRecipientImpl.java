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

package com.ibm.zurich.idmx.cryptoEngine;

import java.net.URI;
import java.util.List;

import com.ibm.zurich.idmx.exception.IssuanceOrchestrationException;
import com.ibm.zurich.idmx.interfaces.cryptoEngine.CryptoEngineRecipient;
import com.ibm.zurich.idmx.interfaces.orchestration.issuance.IssuanceOrchestrationRecipient;

import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.returnTypes.IssuMsgOrCredDesc;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.IssuanceMessage;
import eu.abc4trust.xml.IssuancePolicy;
import eu.abc4trust.xml.IssuanceTokenDescription;

import javax.inject.Inject;

/**
 * 
 */
public class CryptoEngineRecipientImpl implements CryptoEngineRecipient {

  private final IssuanceOrchestrationRecipient issuanceOrchestration;

  @Inject
  public CryptoEngineRecipientImpl(final IssuanceOrchestrationRecipient issuanceOrchestration) {

    this.issuanceOrchestration = issuanceOrchestration;
  }

  @Override
  public IssuanceMessage preIssuancePresentation(final String username, final IssuanceMessage issuanceMessage,
                                                 final IssuanceTokenDescription issuanceTokenDescription, final List<URI> listOfCredentialIds,
      final List<URI> listOfPseudonyms, final List<Attribute> userProvidedAttributes)
      throws CryptoEngineException {
    final IssuanceMessage issuanceMessageToIssuer;
    try {
      issuanceMessageToIssuer =
          issuanceOrchestration.preIssuancePresentation(username, issuanceMessage, issuanceTokenDescription,
              listOfCredentialIds, listOfPseudonyms, userProvidedAttributes);
    } catch (final IssuanceOrchestrationException e) {
      throw new CryptoEngineException(e);
    }
    return issuanceMessageToIssuer;
  }

  @Override
  public IssuMsgOrCredDesc issuanceStep(final String username, final IssuanceMessage issuanceMessage)
      throws CryptoEngineException {
    final IssuMsgOrCredDesc issuanceMsgOrCredDesc;
    try {
      issuanceMsgOrCredDesc = issuanceOrchestration.issuanceStep(username, issuanceMessage);
    } catch (final IssuanceOrchestrationException e) {
      throw new CryptoEngineException(e);
    }
    return issuanceMsgOrCredDesc;
  }

  @Override
  public IssuancePolicy extractIssuancePolicy(final IssuanceMessage issuanceMessage) {
    return issuanceOrchestration.extractIssuancePolicy(issuanceMessage);
  }

}
