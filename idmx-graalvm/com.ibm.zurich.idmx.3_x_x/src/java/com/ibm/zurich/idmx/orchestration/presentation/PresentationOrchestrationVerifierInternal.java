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
import com.ibm.zurich.idmx.annotations.Nullable;
import com.ibm.zurich.idmx.buildingBlock.factory.BuildingBlockFactory;
import com.ibm.zurich.idmx.buildingBlock.inspector.InspectorBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.pseudonym.PseudonymBuildingBlock;
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
import com.ibm.zurich.idmx.interfaces.proofEngine.ZkDirector;
import com.ibm.zurich.idmx.interfaces.state.CarryOverStateIssuer;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.Pair;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleVerifier;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleVerifierCarryOver;

import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.util.AttributeConverter;
import eu.abc4trust.xml.AttributeDescription;
import eu.abc4trust.xml.Credential;
import eu.abc4trust.xml.CredentialInToken;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.CredentialTemplate;
import eu.abc4trust.xml.MechanismSpecification;
import eu.abc4trust.xml.Message;
import eu.abc4trust.xml.NonRevocationEvidence;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.PresentationTokenDescription;
import eu.abc4trust.xml.PseudonymInToken;
import eu.abc4trust.xml.PseudonymWithMetadata;
import eu.abc4trust.xml.PublicKey;
import eu.abc4trust.xml.RevocationInformation;
import eu.abc4trust.xml.SignatureToken;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.VerifierParameters;
import eu.abc4trust.xml.ZkProof;

class PresentationOrchestrationVerifierInternal extends PresentationOrchestrationGeneral {

  private final List<ZkModuleVerifier> verifierModules;
  private final ZkDirector zkDirector;
  //TODO(ksa) not immutable - refactor
  @Nullable
  private ZkModuleVerifierCarryOver zkvCarryOver;
  private boolean simpleProof;

  public PresentationOrchestrationVerifierInternal(final ZkDirector zkDirector,
                                                   final BigIntFactory bigIntFactory, final BuildingBlockFactory buildingBlockFactory,
      final KeyManager keyManager, final AttributeConverter attributeConverter) {
    super(bigIntFactory, buildingBlockFactory, keyManager, attributeConverter);
    verifierModules = new ArrayList<ZkModuleVerifier>();
    this.zkDirector = zkDirector;
  }



  public Pair<Boolean, CarryOverStateIssuer> verifyProof(final PresentationToken presentationToken,
    final @Nullable CredentialTemplate ct, final VerifierParameters vp) throws KeyManagerException, ConfigurationException,
      ProofException {

    final PresentationTokenFacade ptFacade = new PresentationTokenFacade(presentationToken);
    final PresentationTokenDescription ptd = ptFacade.getPresentationTokenDescription();
    final MechanismSpecificationWrapper ms = ptFacade.getMechanismSpecification();
    this.simpleProof = ptd.isUsesSimpleProof();
    final ZkProof proof = ptFacade.getZkProof();

    init(ptd, ms, vp);
    createBuildingBlocks(null, ct);

    final boolean result = zkDirector.verifyProof(proof, verifierModules, getSp());

    final CarryOverStateIssuer carryOverState;
    if (zkvCarryOver != null) {
      carryOverState = zkvCarryOver.recoverState();
    } else {
      carryOverState = null;
    }
    return new Pair<Boolean, CarryOverStateIssuer>(result, carryOverState);
  }


  @Override
  protected void addCredentialZkModules(final String username) throws KeyManagerException, ConfigurationException,
      ProofException {
    int counter = 0;
    for (CredentialInToken cit : getPtd().getCredential()) {
      processCredential(username, cit, null, counter);
      counter++;
    }
  }

  @Override
  protected void addPseudonymZkModules(final String username) throws ConfigurationException, ProofException {
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
    final URI implementation = getMs().getImplementationChoice(moduleId);
    final PseudonymBuildingBlock bb =
        (PseudonymBuildingBlock) buildingBlockFactory.getBuildingBlockById(implementation);
    if (exclusive != bb.isScopeExclusive()) {
      throw new ProofException(
          "The given pseudonym implementation does not have the same value of scopeExclusive");
    }
    final ZkModuleVerifier zkv_p = bb.getZkModuleVerifier(sp, vp, moduleId, scope, pseudonymValue);
    if(simpleProof) {
      throw new RuntimeException("Cannot have pseudonyms in simple proofs");
    }
    verifierModules.add(zkv_p);
  }



  @Override
  public void addMechanismSpecificationZkModule(final MechanismSpecificationBuildingBlock bb,
                                                final String name, final SystemParameters sp, final MechanismSpecification ms) {
    final ZkModuleVerifier zkv = bb.getZkModuleVerifier(name, sp, ms);
    if(!simpleProof) {
      verifierModules.add(zkv);
    }
  }

  @Override
  protected void addPresentationTokenZkModule(final PresentationTokenDescriptionBuildingBlock bb,
                                              final String name, final SystemParameters sp, final PresentationTokenDescription ptd) {
    final ZkModuleVerifier zkv = bb.getZkModuleVerifier(name, sp, ptd);
    if(!simpleProof) {
      verifierModules.add(zkv);
    }
  }



  @Override
  protected void addMessageZkModule(final Abc4TrustMessageBuildingBlock bb, final String name, final Message message) {
    final ZkModuleVerifier zkv = bb.getZkModuleVerifier(name, message);
    if(!simpleProof) {
      verifierModules.add(zkv);
    }
  }



  @Override
  protected void addCredentialSpecificationZkModule(final CredentialSpecificationBuildingBlock bb,
                                                    final String name, final SystemParameters sp, final CredentialSpecification credSpec,
      final BigIntFactory bigIntFactory) {
    final ZkModuleVerifier zkv = bb.getZkModuleVerifier(name, sp, credSpec, bigIntFactory);
    if(!simpleProof) {
      verifierModules.add(zkv);
    }
  }



  @Override
  protected void addEqualityZkModule(final AttributeEqualityBuildingBlock bb, final String lhs, final String rhs,
                                     final boolean external) {
    final ZkModuleVerifier zkv = bb.getZkModuleVerifier(lhs, rhs, external);
    verifierModules.add(zkv);
  }



  @Override
  protected void addSystemParametersZkModule(final SystemParametersBuildingBlock bb, final String name,
                                             final SystemParameters sp) {
    final ZkModuleVerifier zkv = bb.getZkModuleVerifier(name, sp);
    if(!simpleProof) {
      verifierModules.add(zkv);
    }
  }



  @Override
  protected void addVerifierParametersZkModule(final VerifierParametersBuildingBlock bb, final String name,
                                               final SystemParameters sp, final VerifierParameters vp) {
    final ZkModuleVerifier zkv = bb.getZkModuleVerifier(name, sp, vp);
    if(!simpleProof) {
      verifierModules.add(zkv);
    }
  }



  @Override
  protected void addConstantZkModule(final ConstantBuildingBlock bb, final String name, final BigInt value) {
    final ZkModuleVerifier zkv = bb.getZkModuleVerifier(name, value);
    verifierModules.add(zkv);
  }

  
  @Override
  protected void addInequalityZkModule(final RangeProofBuildingBlock bb, final String lhs, final String rhs,
                                       final boolean strict, final SystemParameters sp, final VerifierParameters verifierParameters, final int counter) throws ConfigurationException {
    final ZkModuleVerifier zkv = bb.getZkModuleVerifier(sp, verifierParameters, lhs, rhs, strict, counter);
    if(!simpleProof) {
      verifierModules.add(zkv);
    }
  }

  @Override
  protected void addIssuerKeyZkModule(final IssuerPublicKeyBuildingBlock bb, final String name,
                                      final SystemParameters sp, final PublicKey ip) {
    final ZkModuleVerifier zkv = bb.getZkModuleVerifier(name, sp, ip);
    if(!simpleProof) {
      verifierModules.add(zkv);
    }
  }

  @Override
  protected void addInspectorKeyZkModule(final InspectorPublicKeyBuildingBlock bb, final String name,
                                         final SystemParameters sp, final PublicKey ip) {
    final ZkModuleVerifier zkv = bb.getZkModuleVerifier(name, sp, ip);
    if(!simpleProof) {
      verifierModules.add(zkv);
    }
  }

  @Override
  protected void addRevocationKeyZkModule(final RevocationAuthorityPublicKeyBuildingBlock bb,
                                          final String name, final SystemParameters sp, final PublicKey ip) {
    final ZkModuleVerifier zkv = bb.getZkModuleVerifier(name, sp, ip);
    if(!simpleProof) {
      verifierModules.add(zkv);
    }
  }

  @Override
  protected void addRevealAttributeZkModule(final RevealAttributeBuildingBlock bb, final String attributeId,
                                            final BigInt value) {
    ZkModuleVerifier zkv = bb.getZkModuleVerifier(attributeId, value);
    verifierModules.add(zkv);
  }



  @Override
  protected void addCredentialZkModule(final String username, final SignatureBuildingBlock bb, final SystemParameters sp,
                                       final VerifierParameters vp, final PublicKey ip, final URI issuerUriOnDevice, final String identifierOfModule,
      final @Nullable Pair<Credential, SignatureToken> c, final BigInt credentialSpecificationId,
      final int numberOfAttributes, final boolean device) throws ProofException, ConfigurationException {
    final ZkModuleVerifier zkv =
        bb.getZkModuleVerifierPresentation(sp, vp, ip, identifierOfModule,
            credentialSpecificationId, numberOfAttributes, device);
    verifierModules.add(zkv);
  }

  @Override
  protected void addCarryOverZkModule(final String username, final SignatureBuildingBlock bb, final SystemParameters sp,
                                      final VerifierParameters vp, final PublicKey ip, final String identifierOfModule,
      final URI aliasOfSecretForCarryOver, final BigInt credSpecId, final URI issuerOnDevice,
      final List<Boolean> setByIssuer) throws ProofException, ConfigurationException {
    final boolean onDevice = (aliasOfSecretForCarryOver != null);
    zkvCarryOver =
        bb.getZkModuleVerifierCarryOver(sp, vp, ip, identifierOfModule, credSpecId, setByIssuer,
            onDevice);
    if(simpleProof) {
      throw new RuntimeException("Cannot have carry over in simple proofs");
    }
    verifierModules.add(zkvCarryOver);
  }

  @Override
  protected void addInspectAttributeZkModule(final InspectorBuildingBlock bb_ins,
                                             final String inspectionModuleId, final SystemParameters systemParameters2,
      final VerifierParameters verifierParameters2, final PublicKey insKey, final URI parametersUid,
      final String attributeId, final AttributeDescription attributeDescription, final byte[] label)
      throws ConfigurationException, ProofException {
    final ZkModuleVerifier zkv =
        bb_ins.getZkModuleVerifierEncryption(inspectionModuleId, systemParameters2,
            verifierParameters2, insKey, attributeId, label);
    if(simpleProof) {
      throw new RuntimeException("Cannot have inspect attributes in simple proofs");
    }
    verifierModules.add(zkv);
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
    throw new ProofException("Could not load resource: " + parameter);
  }

  @Override
  protected void addRevocationZkModule(final RevocationBuildingBlock bb, final String moduleId,
                                       final String attributeId, final SystemParameters systemParameters, final VerifierParameters verifierParameters,
      final PublicKey raPublicKey, final URI revocationInformationVersion,
      final @Nullable NonRevocationEvidence nonRevocationEvidence,
      final @Nullable RevocationInformation revocationInformation,
      final BuildingBlockFactory buildingBlockFactory) throws ConfigurationException, ProofException {
    ZkModuleVerifier zkv =
        bb.getZkModuleVerifierPresentation(moduleId, attributeId, systemParameters,
            verifierParameters, raPublicKey, revocationInformation, buildingBlockFactory);
    if(simpleProof) {
      throw new RuntimeException("Cannot have revocation in simple proofs");
    }
    verifierModules.add(zkv);
  }

}
