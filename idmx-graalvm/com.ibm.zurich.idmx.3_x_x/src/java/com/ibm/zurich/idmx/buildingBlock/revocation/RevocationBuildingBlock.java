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

package com.ibm.zurich.idmx.buildingBlock.revocation;

import java.net.URI;

import com.ibm.zurich.idmx.buildingBlock.GeneralBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.factory.BuildingBlockFactory;
import com.ibm.zurich.idmx.configuration.ErrorMessages;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.ProofException;
import com.ibm.zurich.idmx.interfaces.util.RandomGeneration;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProver;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProverRevocation;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleVerifier;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleVerifierRevocation;
import com.ibm.zurich.idmx.keypair.ra.RevocationAuthorityKeyPairWrapper;
import com.ibm.zurich.idmx.keypair.ra.RevocationAuthorityPublicKeyWrapper;
import com.ibm.zurich.idmx.keypair.ra.RevocationAuthoritySecretKeyWrapper;
import com.ibm.zurich.idmx.parameters.ra.RevocationAuthorityPublicKeyTemplateWrapper;
import com.ibm.zurich.idmx.util.UriUtils;

import eu.abc4trust.revocationProxy.RevocationProxy;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.KeyPair;
import eu.abc4trust.xml.NonRevocationEvidence;
import eu.abc4trust.xml.PublicKey;
import eu.abc4trust.xml.RevocationAuthorityPublicKeyTemplate;
import eu.abc4trust.xml.RevocationInformation;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.VerifierParameters;

/**
 * 
 */
public abstract class RevocationBuildingBlock extends GeneralBuildingBlock {

  protected final RandomGeneration randomGeneration;

  public RevocationBuildingBlock(final RandomGeneration randomGeneration) {
    this.randomGeneration = randomGeneration;
  }

  @Override
  protected String getBuildingBlockIdSuffix() {
    return "revocation";
  }

  protected String getModuleIdentifier(final String rhAttribute) {
    return UriUtils.concat(getBuildingBlockId(), rhAttribute).toString();
  }

  @Override
  protected String getImplementationIdSuffix() throws ConfigurationException {
    throw new ConfigurationException(ErrorMessages.wrongUsage("RevocationBuildingBlock"
        + " is a building block that requires extension - only the implementation "
        + "id of the concrete implementations may be queried."));
  }

  @Override
  public final boolean contributesToRevocationPublicKeyTemplate() {
    return true;
  }

  public abstract void addToRevocationParametersTemplate(
      RevocationAuthorityPublicKeyTemplate revocationAuthorityPublicKeyTemplate);

  public KeyPair generateBuildingBlockRevocationAuthorityKeyPair(final SystemParameters systemParameters,
                                                                 final RevocationAuthorityPublicKeyTemplate template) throws ConfigurationException {

    final RevocationAuthorityKeyPairWrapper keyPair = new RevocationAuthorityKeyPairWrapper();
    final RevocationAuthorityPublicKeyTemplateWrapper rapkTemplateWrapper =
        new RevocationAuthorityPublicKeyTemplateWrapper(template);
    final URI publicKeyId =
        UriUtils.concat(rapkTemplateWrapper.getPublicKeyPrefix(),
            randomGeneration.generateRandomUid());
//TODO(ksa) get rid of deprecated calls
    final RevocationAuthorityPublicKeyWrapper pkWrapper =
        keyPair.getRevocationAuthorityPublicKeyWrapper();
    // pk.setImplementationVersion(ipt.getVersion());
    pkWrapper.setSystemParametersId(rapkTemplateWrapper.getSystemParametersId());
    pkWrapper.setPublicKeyTechnology(rapkTemplateWrapper.getTechnology());
    pkWrapper.setNonRevocationEvidenceReference(rapkTemplateWrapper
        .getNonRevocationEvidenceReference());
    pkWrapper.setNonRevocationEvidenceUpdateReference(rapkTemplateWrapper
        .getNonRevocationEvidenceUpdateReference());
    pkWrapper.setRevocationInformationReference(rapkTemplateWrapper
        .getRevocationInformationReference());
    pkWrapper.setPublicKeyId(publicKeyId);
    pkWrapper.setFriendlyDescriptions(rapkTemplateWrapper.getFriendlyDescription());

    final RevocationAuthoritySecretKeyWrapper skWrapper =
        keyPair.getRevocationAuthorityPrivateKeyFacade();
    skWrapper.setPublicKeyId(publicKeyId);

    return keyPair.getKeyPair();
  }

  /**
   * This method is intended to be called by the issuer. This method (1) queries the revocation
   * authority via the revocation proxy to get a new revocation handle and the associated
   * non-revocation evidence; (2) generates a ZkModuleProver that MAY be used to prove that the
   * first operation was done honestly; and that MUST transfer the non-revocation evidence in the
   * proof. This module sets the attribute value of the revocation handle in the initialization
   * phase.
   * 
   * @throws ConfigurationException
   */
  public abstract ZkModuleProverRevocation getZkModuleProverIssuance(
      final SystemParameters systemParameters, final VerifierParameters verifierParameters,
      final PublicKey raPublicKey, final CredentialSpecification credentialSpecification, final String moduleId,
      final RevocationProxy revocationProxy, final BuildingBlockFactory buildingBlockFactory)
      throws ConfigurationException;

  /**
   * This method is intended to be called by the recipient. This method creates a new
   * ZkModuleVerifier object that recovers the non-revocation evidence from the proof and checks
   * that the proof (if applicable) was done honestly.
   * 
   * Note that the value of the revocation handle is transmitted though the ZkModule of the
   * signature building block.
   * 
   * @throws ConfigurationException
   */
  public abstract ZkModuleVerifierRevocation getZkModuleVerifierIssuance(
      final SystemParameters systemParameters, final VerifierParameters verifierParameters,
      final PublicKey raPublicKey, final String moduleId, final BuildingBlockFactory buildingBlockFactory)
      throws ConfigurationException;


  /**
   * This method creates a new ZkModuleProver object that will know how to perform a proof that the
   * given attribute (which is a revocation handle) has not been revoked. This module requires the
   * attribute value of the revocation handle. It may fail early if the attribute value is not
   * consistent with the given non-revocation evidence.
   * 
   * @param attributeId
   * 
   * @throws ProofException
   * @throws ConfigurationException
   */
  public abstract ZkModuleProver getZkModuleProverPresentation(final String moduleId, final String attributeId,
                                                               final SystemParameters systemParameters, final VerifierParameters verifierParameters,
      final PublicKey raPublicKey, final RevocationInformation revocationInformation,
      final NonRevocationEvidence nonRevocationEvidence, final BuildingBlockFactory buildingBlockFactory)
      throws ConfigurationException, ProofException;

  /**
   * This method creates a new ZkModuleVerifier object that will know how to verify a proof that the
   * given attribute (which is a revocation handle) has not been revoked.
   * 
   * @throws ProofException
   * @throws ConfigurationException
   */
  public abstract ZkModuleVerifier getZkModuleVerifierPresentation(final String moduleId,
                                                                   final String attributeId, final SystemParameters systemParameters, final VerifierParameters verifierParameters,
                                                                   final PublicKey raPublicKey, final RevocationInformation revocationInformation,
                                                                   final BuildingBlockFactory buildingBlockFactory) throws ConfigurationException, ProofException;

}
