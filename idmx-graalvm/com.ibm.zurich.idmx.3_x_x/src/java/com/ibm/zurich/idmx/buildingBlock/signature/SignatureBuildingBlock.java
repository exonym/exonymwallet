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

package com.ibm.zurich.idmx.buildingBlock.signature;

import java.net.URI;
import java.util.List;

import com.ibm.zurich.idmx.annotations.InOut;
import com.ibm.zurich.idmx.annotations.Nullable;
import com.ibm.zurich.idmx.buildingBlock.systemParameters.SystemParameterGeneratorBuildingBlock;
import com.ibm.zurich.idmx.configuration.ErrorMessages;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.ProofException;
import com.ibm.zurich.idmx.interfaces.signature.ListOfSignaturesAndAttributes;
import com.ibm.zurich.idmx.interfaces.state.CarryOverStateIssuer;
import com.ibm.zurich.idmx.interfaces.state.CarryOverStateRecipient;
import com.ibm.zurich.idmx.interfaces.state.IssuanceStateIssuer;
import com.ibm.zurich.idmx.interfaces.state.IssuanceStateRecipient;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.RandomGeneration;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProver;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProverCarryOver;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProverIssuance;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleVerifier;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleVerifierCarryOver;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleVerifierIssuance;
import com.ibm.zurich.idmx.keypair.issuer.IssuerKeyPairWrapper;
import com.ibm.zurich.idmx.keypair.issuer.IssuerPublicKeyWrapper;
import com.ibm.zurich.idmx.keypair.issuer.IssuerSecretKeyWrapper;
import com.ibm.zurich.idmx.parameters.issuer.IssuerPublicKeyTemplateWrapper;
import com.ibm.zurich.idmx.util.UriUtils;

import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.xml.IssuanceExtraMessage;
import eu.abc4trust.xml.IssuerPublicKeyTemplate;
import eu.abc4trust.xml.KeyPair;
import eu.abc4trust.xml.PrivateKey;
import eu.abc4trust.xml.PublicKey;
import eu.abc4trust.xml.SignatureToken;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.VerifierParameters;

/**
 * 
 */
public abstract class SignatureBuildingBlock extends SystemParameterGeneratorBuildingBlock {

  private final RandomGeneration randomGeneration;

  protected SignatureBuildingBlock(final RandomGeneration rg) {
    this.randomGeneration = rg;
  }

  @Override
  protected String getBuildingBlockIdSuffix() {
    return "sig";
  }

  @Override
  protected String getImplementationIdSuffix() throws ConfigurationException {
    throw new ConfigurationException(ErrorMessages.wrongUsage("SignatureBuildingBlock"
        + " is a building block that requires extension - only the implementation "
        + "id of the concrete implementations may be queried."));
  }

  @Override
  public boolean contributesToIssuerKeyTemplate() {
    return true;
  }

  public abstract void addToIssuerParametersTemplate(final IssuerPublicKeyTemplate issuerPublicKeyTemplate);

  /**
   * Generates the crypto parameters used for this technology. Only signature building blocks
   * implement this method - other building blocks throw a ConfigurationException().
   * 
   * @return
   * 
   * @throws ConfigurationException
   * @throws KeyManagerException
   */
  public KeyPair generateBuildingBlockIssuerKeyPair(final SystemParameters systemParameters,
      IssuerPublicKeyTemplate template) throws ConfigurationException {

    final IssuerKeyPairWrapper keyPair = new IssuerKeyPairWrapper();
    final IssuerPublicKeyTemplateWrapper ipt = new IssuerPublicKeyTemplateWrapper(template);
    final URI publicKeyId =
        UriUtils.concat(ipt.getPublicKeyPrefix(), randomGeneration.generateRandomUid());

    final IssuerPublicKeyWrapper pk = keyPair.getIssuerPublicKeyWrapper();
    // pk.setImplementationVersion(ipt.getVersion());
    pk.setSystemParametersId(ipt.getSystemParametersId());
    pk.setPublicKeyTechnology(ipt.getTechnology());
    pk.setPublicKeyId(publicKeyId);
    pk.setMaximalNumberOfAttributes(ipt.getMaximalNumberOfAttributes());
    pk.setFriendlyDescriptions(ipt.getFriendlyDescription());
    if (ipt.hasRevocationAuthority()) {
      pk.setRevocationAuthorityId(ipt.getRevocationAuthority());
    }

    final IssuerSecretKeyWrapper sk = keyPair.getIssuerPrivateKeyFacade();
    sk.setPublicKeyId(publicKeyId);

    return keyPair.getKeyPair();
  }


  /**
   * This method creates a new ZkModuleProver object that will know how to perform a proof of
   * possession of the signature. By default all attributes of the signature will be hidden unless
   * another building block (such as a RevealAttribute block) marks the attribute as revealed. The
   * generated ZkModuleProver sets the attribute values in the initialization phase for all
   * non-external attributes.
   * 
   * @throws ProofException
   * @throws ConfigurationException
   */
  public abstract ZkModuleProver getZkModuleProverPresentation(final SystemParameters systemParameters,
                                                               final VerifierParameters verifierParameters, final PublicKey issuerPublicKey, final String identifierOfModule,
      final SignatureToken signatureToken, final List<BigInt> encodedAttributes,
      final BigInt credentialSpecificationId, final @Nullable URI identifierOfSecret, final String username,
      final @Nullable URI identifierOfSignatureForSecret) throws ConfigurationException, ProofException;

  /**
   * This method creates a new ZkModuleVerifier object that will know how to verify a proof of
   * possession of a signature.
   * 
   * @throws ConfigurationException
   * @throws ProofException
   */
  public abstract ZkModuleVerifier getZkModuleVerifierPresentation(
      final SystemParameters systemParameters, final VerifierParameters verifierParameters,
      final PublicKey issuerPublicKey, final String identifierOfModule, final BigInt credentialSpecificationId,
      final int numberOfAttributes, final boolean externalDevice) throws ProofException, ConfigurationException;

  /**
   * This method creates a ZkModuleProver that will: (1) create a commitment for all the attributes
   * where includeAttributeInCommitment is true, and the secret if identifierOfSecret is non null.
   * (2) perform a proof of knowledge of the attribute values contained in these commitments; (3)
   * transfer the commitments to the verifier. By default all attributes in the commitments will be
   * hidden unless another building block (such as a RevealAttribute block) marks the attribute as
   * revealed. The generated ZkModuleProver sets the attribute values in the initialization phase
   * for all attributes where encodedAttributeValues is not null. It requires the attribute value
   * for all attributes for which includeAttributeInCommitment is true and encoded- AttributeValues
   * is null.
   * 
   * @throws ProofException
   * @throws ConfigurationException
   */
  public abstract ZkModuleProverCarryOver getZkModuleProverCarryOver(
      final SystemParameters systemParameters, final VerifierParameters verifierParameters,
      final PublicKey issuerPublicKey, final String identifierOfModule, final @Nullable URI identifierOfSecret,
      final String username, final @Nullable URI identifierOfSignatureForSecret, final BigInt credentialSpecificationId,
      final List<Boolean> carryAttributeOver, final List</* Nullable */BigInt> newCredentialAttributes)
      throws ProofException, ConfigurationException;

  /**
   * This method creates a new ZkModuleVerifier object that will know how to: (1) re- cover the
   * commitments made by the corresponding ZkModuleProver from the proof; (2) verify the proof of
   * knowledge of the opening of these commitments. In the attributeSetByIssuer list, a value of
   * false must be set if a non-null value was put in the same position in the
   * encodedAttributeValues list in getZkModuleProverCarryOver, and a value of true if there was a
   * null value.
   * 
   * @throws ProofException
   * @throws ConfigurationException
   */
  public abstract ZkModuleVerifierCarryOver getZkModuleVerifierCarryOver(
      final SystemParameters systemParameters, final VerifierParameters verifierParameters,
      final PublicKey issuerPublicKey, final String identifierOfModule, BigInt credentialSpecificationId,
      final List<Boolean> attributeSetByIssuer, final boolean hasDevice) throws ConfigurationException,
      ProofException;

  /**
   * This method is intended to be used by the issuer. This method creates a ZkModule- Prover that:
   * (1) does some preliminary work useful towards generating a signature (in some implementations,
   * this method has enough information to generate the signature right now, others require
   * additional steps) based on the attributes that were carried over and the issuer-specified
   * attributes; and (2) knows how to perform a proof that step 1 was done honestly; and (3) is
   * responsible for transferring some information to the recipient (in some implementations, the
   * whole signature can be transmitted here, others require additional steps). The generated
   * ZkModuleProver sets the attribute values in the initialization phase for all attributes where
   * issuerSpecifiedAttributes is not null, and for all revealed attributes that were carried over.
   * It requires the attribute value for all attributes for which are not carried over, and which
   * have a null entry in issuerSpecifiedAttributes. All issuer-specified attributes will be
   * revealed.
   * 
   * @throws ConfigurationException
   */
  public abstract ZkModuleProverIssuance getZkModuleProverIssuance(
      final SystemParameters systemParameters, final VerifierParameters verifierParameters,
      final PublicKey issuerPublicKey, final PrivateKey issuerSecretKey, final String identifierOfModule,
      final BigInt credentialSpecificationId, final boolean externalSecret,
      final List</* Nullable */BigInt> issuerSpecifiedAttributes,
      final @Nullable CarryOverStateIssuer carryOverState) throws ConfigurationException;

  /**
   * This method is intended to be used by the recipient. This method creates a new ZkModuleVerifier
   * object that will know how to: (1) recover the information send by the corresponding
   * ZkModuleProver from the proof; (2) verify that this information was generated honestly.
   * 
   * @throws ConfigurationException
   */
  public abstract ZkModuleVerifierIssuance getZkModuleVerifierIssuance(
      final SystemParameters systemParameters, final VerifierParameters verifierParameters,
      final PublicKey issuerPublicKey, final String identifierOfModule, final BigInt credentialSpecificationId,
      final boolean hasDevice, final int numberOfAttributes, final @Nullable CarryOverStateRecipient carryOverState)
      throws ConfigurationException;

  /**
   * This method returns the number of additional communication round-trips (one message from
   * recipient to issuer and the response) needed for issuing the new signature. The recipient and
   * the issuer will need to call extraIssuanceRoundRecipient() and extraIssuanceRoundIssuer()
   * respectively that many times, before the recipient may call recoverSignature().
   */
  public abstract int getNumberOfAdditionalIssuanceRoundtrips();

  /**
   * This method is intended to be used by the recipient. This method does some steps required to
   * progress with the issuance of the signature, and outputs a message that is to be sent to the
   * issuer. The method may update the issuance state.
   */
  public abstract @Nullable
  IssuanceExtraMessage extraIssuanceRoundRecipient(
      final @Nullable IssuanceExtraMessage messageFromIssuer, final @InOut IssuanceStateRecipient stateRecipient);

  /**
   * This method is intended to be used by the issuer. This method does some steps required to
   * progress with the issuance of the signature, and outputs a message that is to be sent to the
   * recipient. The method may update the issuance state.
   */
  public abstract IssuanceExtraMessage extraIssuanceRoundIssuer(
      final IssuanceExtraMessage messageFromRecipient, final @InOut IssuanceStateIssuer stateIssuer);

  /**
   * This method is intended to be used by the recipient. This method extracts the signature from
   * the state information. It returns the signature(s) that were generated (for CL: one signature,
   * for Uprove: one signature per token), plus the list of encoded attributes that was signed.
   * 
   * @throws ConfigurationException
   */
  public abstract ListOfSignaturesAndAttributes extractSignature(
      final @Nullable IssuanceExtraMessage messageFromIssuer, final IssuanceStateRecipient stateRecipient)
      throws ConfigurationException;


}
