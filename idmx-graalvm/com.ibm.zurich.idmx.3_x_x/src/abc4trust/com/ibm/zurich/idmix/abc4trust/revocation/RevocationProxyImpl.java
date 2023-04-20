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

import com.ibm.zurich.idmix.abc4trust.facades.RevocationMessageFacade;

import eu.abc4trust.returnTypes.RevocationMessageAndBoolean;
import eu.abc4trust.revocationProxy.RevocationProxy;
import eu.abc4trust.revocationProxy.revauth.RevocationProxyAuthority;
import eu.abc4trust.xml.CryptoParams;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.Reference;
import eu.abc4trust.xml.RevocationAuthorityParameters;
import eu.abc4trust.xml.RevocationMessage;

import javax.inject.Inject;

/**
 * 
 */
public class RevocationProxyImpl implements RevocationProxy {

  private final RevocationProxyAuthority raProxy;

  @Inject
  public RevocationProxyImpl(final RevocationProxyAuthority raProxy) {
    this.raProxy = raProxy;
  }

  @Override
  public RevocationMessage processRevocationMessage(final RevocationMessage m,
                        final RevocationAuthorityParameters revpars) throws Exception {

    final CryptoParams cryptoParams;

    //final RevocationMessage revocationResponse = null;
    final RevocationMessageFacade revocationMessageFacade = new RevocationMessageFacade(m);

    if (revocationMessageFacade.revocationHandleRequested()) {
      cryptoParams = requestRevocationHandle(m, revpars.getNonRevocationEvidenceReference());
    } else {
      cryptoParams = null;
    }
    // case REQUEST_REVOCATION_INFORMATION:
    // cryptoParams = this.requestRevocationInformation(m,
    // revpars.getRevocationInfoReference());
    // break;
    // case GET_CURRENT_REVOCATION_INFORMATION:
    // cryptoParams = this.getCurrentRevocationInformation(m,
    // revpars.getRevocationInfoReference());
    // break;
    // case UPDATE_REVOCATION_EVIDENCE:
    // cryptoParams = this.revocationEvidenceUpdate(m,
    // revpars.getNonRevocationEvidenceUpdateReference());
    // break;

    final RevocationMessage rm = new ObjectFactory().createRevocationMessage();
     rm.setContext(m.getContext());
     rm.setRevocationAuthorityParametersUID(m.getRevocationAuthorityParametersUID());
     rm.setCryptoParams(cryptoParams);

    return rm;
  }

  private CryptoParams requestRevocationHandle(final RevocationMessage m,
                                               final Reference nonRevocationEvidenceReference) throws Exception {

    final RevocationMessageAndBoolean msg =
        raProxy.processRevocationMessage(m);

    final CryptoParams cryptoParams = new CryptoParams();
    cryptoParams.getContent().addAll(msg.revmess.getCryptoParams().getContent());
    return cryptoParams;
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
