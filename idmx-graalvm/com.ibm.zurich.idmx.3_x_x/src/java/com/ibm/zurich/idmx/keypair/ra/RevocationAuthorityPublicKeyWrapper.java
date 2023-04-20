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

package com.ibm.zurich.idmx.keypair.ra;

import java.net.URI;

import com.ibm.zurich.idmx.configuration.ParameterBaseName;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.interfaces.configuration.Constants;
import com.ibm.zurich.idmx.keypair.PublicKeyWrapper;

import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PublicKey;
import eu.abc4trust.xml.Reference;

public class RevocationAuthorityPublicKeyWrapper extends PublicKeyWrapper {

  private static final String NON_REVOCATION_EVIDENCE_REFERENCE_NAME = "nreReference";
  private static final String NON_REVOCATION_EVIDENCE_UPDATE_REFERENCE_NAME = "nreUpdateReference";
  private static final String REVOCATION_INFORMATION_REFERENCE_NAME =
      "revocationInformationReference";

  private static final String BASES_NAME = "base";


  protected RevocationAuthorityPublicKeyWrapper(PublicKey publicKey, int maximalNumberOfAttributes)
      throws ConfigurationException {
    super(publicKey);
  }

  public RevocationAuthorityPublicKeyWrapper(PublicKey publicKey) {
    super(publicKey);
  }



  /**
   * @deprecated create a reference parameter such that this can be fed to the getParameter() method
   *             directly.
   */
  public static URI referenceToUri(final Reference inputReference) {
    // makes sure that the inverse conversion can be done without causing trouble
    // assert (inputReference.getReferenceType().equals(URI.create("url")));
    assert (inputReference.getReferences().size() < 2);
    assert (inputReference.getReferences().get(0).getScheme() != null);
    return inputReference.getReferences().get(0);
  }

  /**
   * @deprecated create a reference parameter such that this can be fed to the getParameter() method
   *             directly.
   */
  public static Reference uriToReference(final URI inputUri) {
    final Reference reference = new ObjectFactory().createReference();
    reference.setReferenceType(URI.create(inputUri.getScheme()));
    reference.getReferences().add(inputUri);
    return reference;
  }

  public Reference getNonRevocationEvidenceReference() throws ConfigurationException {
    // TODO create a reference parameter such that this can be fed to the getParameter() method
    // directly
    // return (Reference) getParameter(NON_REVOCATION_EVIDENCE_NAME);

    final URI nreURI = (URI) getParameter(NON_REVOCATION_EVIDENCE_REFERENCE_NAME);
    return uriToReference(nreURI);
  }

  public void setNonRevocationEvidenceReference(final Reference nonRevocationEvidenceReference)
      throws ConfigurationException {
    // TODO create a reference parameter such that this can be fed to the getParameter() method
    // directly
    // setParameter(NON_REVOCATION_EVIDENCE_NAME, nonRevocationEvidence);

    setParameter(NON_REVOCATION_EVIDENCE_REFERENCE_NAME,
        referenceToUri(nonRevocationEvidenceReference));
  }

  /**
   * @deprecated use setNonRevocationEvidenceReference(Reference) instead.
   */
  public void setNonRevocationEvidenceReference(final URI nonRevocationEvidence)
      throws ConfigurationException {
    setParameter(NON_REVOCATION_EVIDENCE_REFERENCE_NAME, nonRevocationEvidence);
  }

  public Reference getNonRevocationEvidenceUpdateReference() throws ConfigurationException {
    // TODO create a reference parameter such that this can be fed to the getParameter() method
    // directly
    // return (Reference) getParameter(NON_REVOCATION_EVIDENCE_UPDATE_REFERENCE_NAME);

    final URI nreUpdateReferenceURI = (URI) getParameter(NON_REVOCATION_EVIDENCE_UPDATE_REFERENCE_NAME);
    return uriToReference(nreUpdateReferenceURI);
  }

  public void setNonRevocationEvidenceUpdateReference(Reference nonRevocationEvidenceUpdateReference)
      throws ConfigurationException {
    // TODO create a reference parameter such that this can be fed to the getParameter() method
    // directly
    // setParameter(NON_REVOCATION_EVIDENCE_UPDATE_REFERENCE_NAME,
    // nonRevocationEvidenceUpdateReference);

    setParameter(NON_REVOCATION_EVIDENCE_UPDATE_REFERENCE_NAME,
        referenceToUri(nonRevocationEvidenceUpdateReference));
  }

  /**
   * @deprecated use setNonRevocationEvidenceReferenceUpdate(Reference) instead.
   */
  public void setNonRevocationEvidenceUpdateReference(final URI nonRevocationEvidenceUpdateReference)
      throws ConfigurationException {
    setParameter(NON_REVOCATION_EVIDENCE_UPDATE_REFERENCE_NAME,
        nonRevocationEvidenceUpdateReference);
  }

  public void setRevocationInformationReference(final Reference revocationInformationReference)
      throws ConfigurationException {
    // TODO create a reference parameter such that this can be fed to the getParameter() method
    // directly

    setParameter(REVOCATION_INFORMATION_REFERENCE_NAME,
        referenceToUri(revocationInformationReference));
  }

  public Reference getRevocationInformationReference() throws ConfigurationException {
    final URI revocationInformationReferenceURI =
        (URI) getParameter(REVOCATION_INFORMATION_REFERENCE_NAME);
    return uriToReference(revocationInformationReferenceURI);
  }



  @Override
  public String getAttributeBaseIdentifier(final int i) {
    return BASES_NAME + Constants.URI_DELIMITER + String.valueOf(i);
  }


  @Override
  protected String createParameterUriBasedOnParameterName(final String parameterName) {
    return ParameterBaseName.revocationAuthorityPublicKeyParameterName(parameterName);
  }
}
