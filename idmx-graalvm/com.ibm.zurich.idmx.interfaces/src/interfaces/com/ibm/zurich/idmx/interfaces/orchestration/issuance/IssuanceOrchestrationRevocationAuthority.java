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

import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.IssuanceOrchestrationException;

import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.NonRevocationEvidence;


public interface IssuanceOrchestrationRevocationAuthority {

  /**
   * This method is intended to be called though the revocation proxy. This method generates a new
   * revocation handle and the corresponding non-revocation evidence. This method additionally
   * returns the URI of the latest revocation information.
   * 
   * @throws KeyManagerException
   * @throws ConfigurationException
   * @throws CredentialManagerException
   * @throws IssuanceOrchestrationException
   */
  public NonRevocationEvidence newRevocationHandle(final URI revocationAuthorityId,
		  final URI nonRevocationEvidenceId, final List<Attribute> attributes) throws ConfigurationException,
      KeyManagerException, CredentialManagerException, IssuanceOrchestrationException;

}
