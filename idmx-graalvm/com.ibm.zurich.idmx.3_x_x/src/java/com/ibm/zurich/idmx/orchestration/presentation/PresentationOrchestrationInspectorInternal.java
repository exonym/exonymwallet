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
package com.ibm.zurich.idmx.orchestration.presentation;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import com.ibm.zurich.idmix.abc4trust.facades.PresentationTokenFacade;
import com.ibm.zurich.idmix.abc4trust.facades.SecretKeyFacade;
import com.ibm.zurich.idmx.annotations.Nullable;
import com.ibm.zurich.idmx.buildingBlock.factory.BuildingBlockFactory;
import com.ibm.zurich.idmx.buildingBlock.inspector.InspectorBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.rangeProof.RangeProofBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.revocation.RevocationBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.signature.SignatureBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.abc4TrustMessage.Abc4TrustMessageBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.constant.ConstantBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.credentialSpecification.CredentialSpecificationBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.equality.AttributeEqualityBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.inspectorKey.InspectorPublicKeyBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.issuerKey.IssuerPublicKeyBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.mechanismSpecification.MechanismSpecificationBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.presentationTokenDescription.PresentationTokenDescriptionBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.reveal.RevealAttributeBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.revocationAuthorityKey.RevocationAuthorityPublicKeyBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.systemParameters.SystemParametersBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.verifierParameters.VerifierParametersBuildingBlock;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.ProofException;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.Pair;
import com.ibm.zurich.idmx.interfaces.util.RandomGeneration;

import eu.abc4trust.abce.internal.inspector.credentialManager.CredentialManager;
import eu.abc4trust.abce.internal.inspector.credentialManager.CredentialManagerException;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.util.AttributeConverter;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.AttributeDescription;
import eu.abc4trust.xml.Credential;
import eu.abc4trust.xml.CredentialInToken;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.MechanismSpecification;
import eu.abc4trust.xml.Message;
import eu.abc4trust.xml.NonRevocationEvidence;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.PresentationTokenDescription;
import eu.abc4trust.xml.PrivateKey;
import eu.abc4trust.xml.PseudonymInToken;
import eu.abc4trust.xml.PseudonymWithMetadata;
import eu.abc4trust.xml.PublicKey;
import eu.abc4trust.xml.RevocationInformation;
import eu.abc4trust.xml.SecretKey;
import eu.abc4trust.xml.SignatureToken;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.VerifierParameters;
import eu.abc4trust.xml.ZkProof;

class PresentationOrchestrationInspectorInternal extends PresentationOrchestrationGeneral {

  private final CredentialManager credentialManager;
  private final List<Attribute> attributes;
  //TODO(ksa) not immutable - refactor
  private ZkProof proof;
  private final RandomGeneration randomGeneration;

  public PresentationOrchestrationInspectorInternal(final CredentialManager credentialManager,
                                                    final BigIntFactory bigIntFactory, final BuildingBlockFactory buildingBlockFactory,
      final KeyManager keyManager, final AttributeConverter attributeConverter,
      final RandomGeneration randomGeneration) {
    super(bigIntFactory, buildingBlockFactory, keyManager, attributeConverter);
    this.credentialManager = credentialManager;
    this.attributes = new ArrayList<Attribute>();
    this.randomGeneration = randomGeneration;
  }

  public List<Attribute> inspect(final PresentationToken presentationToken) throws KeyManagerException,
      ConfigurationException, ProofException {

    final PresentationTokenFacade ptFacade = new PresentationTokenFacade(presentationToken);
    final PresentationTokenDescription ptd = ptFacade.getPresentationTokenDescription();
    final MechanismSpecificationWrapper ms = ptFacade.getMechanismSpecification();
    proof = ptFacade.getZkProof();

    init(ptd, ms, null);
    createBuildingBlocks(null, null);

    return attributes;
  }


  @Override
  protected void addCredentialZkModules(final String username) throws KeyManagerException, ConfigurationException,
      ProofException {
    int counter = 0;
    for (final CredentialInToken cit : getPtd().getCredential()) {
      processCredential(username, cit, null, counter);
      counter++;
    }
  }

  @Override
  protected void addPseudonymZkModules(String username) throws ConfigurationException, ProofException {
    int counter = 0;
    for (final PseudonymInToken pit : getPtd().getPseudonym()) {
      processPseudonym(username, pit, null, counter);
      counter++;
    }
  }

  @Override
  protected void addPseudonymZkModule(final String username, final String moduleId, final SystemParameters sp, final VerifierParameters vp,
                                      final @Nullable PseudonymWithMetadata p, final URI scope, final boolean exclusive, final byte[] pseudonymValue)
      throws ProofException, ConfigurationException {
    // Nothing to do
  }



  @Override
  public void addMechanismSpecificationZkModule(final MechanismSpecificationBuildingBlock bb,
                                                final String name, final SystemParameters sp, final MechanismSpecification ms) {
    // Nothing to do
  }

  @Override
  protected void addPresentationTokenZkModule(final PresentationTokenDescriptionBuildingBlock bb,
                                              final String name, final SystemParameters sp, final PresentationTokenDescription ptd) {
    // Nothing to do
  }



  @Override
  protected void addMessageZkModule(final Abc4TrustMessageBuildingBlock bb, final String name, final Message message) {
    // Nothing to do
  }



  @Override
  protected void addCredentialSpecificationZkModule(final CredentialSpecificationBuildingBlock bb,
                                                    final String name, final SystemParameters sp, final CredentialSpecification credSpec,
      final BigIntFactory bigIntFactory) {
    // Nothing to do
  }



  @Override
  protected void addEqualityZkModule(final AttributeEqualityBuildingBlock bb, final String lhs, final String rhs,
                                     final boolean external) {
    // Nothing to do
  }



  @Override
  protected void addSystemParametersZkModule(final SystemParametersBuildingBlock bb, final String name,
                                             final SystemParameters sp) {
    // Nothing to do
  }



  @Override
  protected void addVerifierParametersZkModule(final VerifierParametersBuildingBlock bb, final String name,
                                               final SystemParameters sp, final VerifierParameters vp) {
    // Nothing to do
  }



  @Override
  protected void addConstantZkModule(final ConstantBuildingBlock bb, final String name, final BigInt value) {
    // Nothing to do
  }



  @Override
  protected void addIssuerKeyZkModule(final IssuerPublicKeyBuildingBlock bb, final String name,
                                      final SystemParameters sp, final PublicKey ip) {
    // Nothing to do
  }

  @Override
  protected void addInspectorKeyZkModule(final InspectorPublicKeyBuildingBlock bb, final String name,
                                         final SystemParameters sp, final PublicKey ip) {
    // Nothing to do
  }

  @Override
  protected void addRevocationKeyZkModule(final RevocationAuthorityPublicKeyBuildingBlock bb,
                                          final String name, final SystemParameters sp, final PublicKey ip) {
    // Nothing to do
  }

  @Override
  protected void addRevealAttributeZkModule(final RevealAttributeBuildingBlock bb, final String attributeId,
                                            final BigInt value) {
    // Nothing to do
  }



  @Override
  protected void addCredentialZkModule(final String username, final SignatureBuildingBlock bb, final SystemParameters sp,
                                       final VerifierParameters vp, final PublicKey ip, final URI issuerUriOnDevice, final String identifierOfModule,
      final @Nullable Pair<Credential, SignatureToken> c, final BigInt credentialSpecificationId,
      final int numberOfAttributes, final boolean device) throws ProofException, ConfigurationException {
    // Nothing to do
  }

  @Override
  protected void addCarryOverZkModule(final String username, final SignatureBuildingBlock bb, final SystemParameters sp,
                                      final VerifierParameters vp, final PublicKey ip, final String identifierOfModule,
      final URI aliasOfSecretForCarryOver, final BigInt credSpecId, final URI issuerOnDevice,
      List<Boolean> setByIssuer) throws ProofException, ConfigurationException {
    // Nothing to do
  }

  @Override
  protected void addInspectAttributeZkModule(final InspectorBuildingBlock bb_ins,
                                             final String inspectionModuleId, SystemParameters systemParameters2,
      final VerifierParameters verifierParameters2, PublicKey insKey, URI parametersUid,
      final String attributeId, AttributeDescription attributeDescription, byte[] label)
      throws ConfigurationException, ProofException {
    try {
      final SecretKey sk = credentialManager.getInspectorSecretKey(parametersUid);
      if (sk == null) {
        // Ignore this module
        return;
      }
      final SecretKeyFacade skf = new SecretKeyFacade(sk);
      final PrivateKey privateKey = skf.getPrivateKey();
      final BigInt value = bb_ins.getPlaintext(proof, inspectionModuleId, label, privateKey);

      final Attribute att = new ObjectFactory().createAttribute();
      final Object attributeValue =
          attributeConverter.recoverValueFromEncodedValue(value.getValue(), attributeDescription);
      att.setAttributeValue(attributeValue);
      att.setAttributeDescription(attributeDescription);
      final String attUid = attributeId + "-" + randomGeneration.generateRandomUid();
      att.setAttributeUID(URI.create(attUid));
      attributes.add(att);
    } catch (final CredentialManagerException e) {
      // Ignore this module
      return;
    }
  }


  @Override
  protected void initializeCarryOverState(final int numberOfAttributes) {
    // Nothing to do
  }



  @Override
  protected void updateCarryOverStateWithEquality(final int mySeq, final URI myType, final URI aliasOther,
                                                  final URI typeOther) {
    // Nothing to do
  }

  @Override
  protected void failedToLoadParameter(final URI parameter) throws ProofException {
    System.err.println("Warning: Could not load resource: " + parameter);
    // No need to throw an exception
  }

  @Override
  protected void addRevocationZkModule(final RevocationBuildingBlock bb, final String moduleId,
                                       final String attributeId, final SystemParameters systemParameters, final VerifierParameters verifierParameters,
      final PublicKey raPublicKey, final URI revocationInformationVersion,
      final @Nullable NonRevocationEvidence nonRevocationEvidence, final @Nullable RevocationInformation ri,
      final BuildingBlockFactory buildingBlockFactory) throws ConfigurationException, ProofException {
    // Nothing to do
  }

  @Override
  protected void addInequalityZkModule(final RangeProofBuildingBlock bb, final String lhs, final String rhs,
                                       final boolean strict, final SystemParameters sp, final VerifierParameters verifierParameters, int counter) {
    // Nothing to do
  }

}
