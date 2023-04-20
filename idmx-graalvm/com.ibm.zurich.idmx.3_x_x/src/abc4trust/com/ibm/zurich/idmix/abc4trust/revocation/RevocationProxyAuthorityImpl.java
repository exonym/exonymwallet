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

package com.ibm.zurich.idmix.abc4trust.revocation;

import java.net.URI;

import com.ibm.zurich.idmix.abc4trust.facades.RevocationMessageFacade;
import com.ibm.zurich.idmx.interfaces.cryptoEngine.CryptoEngineRevocationAuthority;

import eu.abc4trust.returnTypes.RevocationMessageAndBoolean;
import eu.abc4trust.revocationProxy.RevocationProxyException;
import eu.abc4trust.revocationProxy.revauth.RevocationProxyAuthority;
import eu.abc4trust.xml.NonRevocationEvidence;
import eu.abc4trust.xml.Reference;
import eu.abc4trust.xml.RevocationMessage;

import javax.inject.Inject;

/**
 * 
 */
public class RevocationProxyAuthorityImpl implements RevocationProxyAuthority {

  private CryptoEngineRevocationAuthority cryptoEngineRA;

  @Inject
  public RevocationProxyAuthorityImpl(final CryptoEngineRevocationAuthority cryptoEngineRA) {
    this.cryptoEngineRA = cryptoEngineRA;
  }

  @Override
  public RevocationMessageAndBoolean processRevocationMessage(final RevocationMessage m) throws Exception {

    final RevocationMessageFacade revocationMessageFacade = new RevocationMessageFacade(m);

    final RevocationMessageAndBoolean response = new RevocationMessageAndBoolean();
    if (revocationMessageFacade.revocationHandleRequested()) {
      final RevocationMessage revocationResponse =
          requestRevocationHandle(revocationMessageFacade.getRevocationAuthorityParametersUID(),
              null);
      response.lastMessage = true;
      response.revmess = revocationResponse;

    }

    return response;
  }


  private RevocationMessage requestRevocationHandle(final URI revocationAuthorityId,
                                                    final Reference nonRevocationEvidenceReference) throws RevocationProxyException {
    NonRevocationEvidence nre = null;
    try {
      // TODO make this properly - what may be contained in Reference???
      final URI nonRevocationEvidenceId = null;
      if(nonRevocationEvidenceReference != null && nonRevocationEvidenceReference.getReferences().size() != 0) {
        nonRevocationEvidenceReference.getReferences().get(0);
      }
      nre =
          cryptoEngineRA.newRevocationHandle(revocationAuthorityId, nonRevocationEvidenceId, null);
    } catch (final Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    final RevocationMessageFacade revocationMessageFacade = new RevocationMessageFacade();

    revocationMessageFacade.setNonRevocationEvidence(nre);

    return revocationMessageFacade.getDelegateeValue();
  }


  // private CryptoParams getCurrentRevocationInformation(RevocationMessage m,
  // Reference revocationInfoReference) throws RevocationProxyException {
  // return this.communicationStrategy.getCurrentRevocationInformation(m, revocationInfoReference);
  // }
  //
  // private CryptoParams requestRevocationInformation(RevocationMessage m,
  // Reference revocationInformationReference) throws RevocationProxyException {
  // return this.communicationStrategy.requestRevocationInformation(m,
  // revocationInformationReference);
  // }
  //
  // private CryptoParams revocationEvidenceUpdate(RevocationMessage m,
  // Reference nonRevocationEvidenceUpdateReference) throws RevocationProxyException {
  // return this.communicationStrategy.revocationEvidenceUpdate(m,
  // nonRevocationEvidenceUpdateReference);
  // }


}
