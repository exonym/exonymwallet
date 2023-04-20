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

package com.ibm.zurich.idmx.orchestration.issuance;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.ibm.zurich.idmix.abc4trust.facades.CredentialFacade;
import com.ibm.zurich.idmix.abc4trust.facades.IssuanceMessageFacade;
import com.ibm.zurich.idmix.abc4trust.facades.IssuerParametersFacade;
import com.ibm.zurich.idmix.abc4trust.facades.RevocationAuthorityParametersFacade;
import com.ibm.zurich.idmx.annotations.Nullable;
import com.ibm.zurich.idmx.buildingBlock.factory.BuildingBlockFactory;
import com.ibm.zurich.idmx.buildingBlock.revocation.RevocationBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.signature.SignatureBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.credentialSpecification.CredentialSpecificationBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.equality.AttributeEqualityBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.issuerKey.IssuerPublicKeyBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.structural.mechanismSpecification.MechanismSpecificationBuildingBlock;
import com.ibm.zurich.idmx.configuration.Configuration;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.IssuanceOrchestrationException;
import com.ibm.zurich.idmx.exception.PresentationOrchestrationException;
import com.ibm.zurich.idmx.interfaces.orchestration.issuance.IssuanceOrchestrationRecipient;
import com.ibm.zurich.idmx.interfaces.orchestration.issuance.PhaseRecipient;
import com.ibm.zurich.idmx.interfaces.orchestration.issuance.StateRecipient;
import com.ibm.zurich.idmx.interfaces.orchestration.issuance.StateStorage;
import com.ibm.zurich.idmx.interfaces.orchestration.presentation.PresentationOrchestrationProver;
import com.ibm.zurich.idmx.interfaces.proofEngine.ZkDirector;
import com.ibm.zurich.idmx.interfaces.signature.ListOfSignaturesAndAttributes;
import com.ibm.zurich.idmx.interfaces.state.CarryOverStateRecipient;
import com.ibm.zurich.idmx.interfaces.state.CarryOverStateRecipientWithAttributes;
import com.ibm.zurich.idmx.interfaces.state.IssuanceStateRecipientWithAttributes;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.Pair;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleVerifier;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleVerifierIssuance;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleVerifierRevocation;
import com.ibm.zurich.idmx.jaxb.wrapper.CredentialSpecificationWrapper;
import com.ibm.zurich.idmx.orchestration.presentation.MechanismSpecificationWrapper;
import com.ibm.zurich.idmx.parameters.system.SystemParametersWrapper;

import eu.abc4trust.abce.internal.user.credentialManager.CredentialManager;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.returnTypes.IssuMsgOrCredDesc;
import eu.abc4trust.util.AttributeConverter;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.AttributeList;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.CredentialTemplate;
import eu.abc4trust.xml.IssuanceExtraMessage;
import eu.abc4trust.xml.IssuanceMessage;
import eu.abc4trust.xml.IssuancePolicy;
import eu.abc4trust.xml.IssuanceToken;
import eu.abc4trust.xml.IssuanceTokenDescription;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.MechanismSpecification;
import eu.abc4trust.xml.NonRevocationEvidence;
import eu.abc4trust.xml.RevocationAuthorityParameters;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.VerifierParameters;
import eu.abc4trust.xml.ZkProof;

import javax.inject.Inject;

public class IssuanceOrchestrationRecipientImpl implements IssuanceOrchestrationRecipient {

  private final StateStorage<StateRecipient> storage;
  private final PresentationOrchestrationProver presOrch;
  private final CredentialManager credentialManager;
  private final KeyManager keyManager;
  private final BigIntFactory bigIntFactory;
  private final BuildingBlockFactory bbf;
  private final ZkDirector zkDirector;
  private final AttributeConverter attributeConverter;

  @Inject
  public IssuanceOrchestrationRecipientImpl(final StateStorage<StateRecipient> storage,
                                            final PresentationOrchestrationProver presOrch, final CredentialManager cm, final KeyManager km,
      final BigIntFactory bigIntFactory, final BuildingBlockFactory buildingBlockFactory,
      final ZkDirector zkDirector, final AttributeConverter attributeConverter) {
    this.storage = storage;
    this.presOrch = presOrch;
    this.credentialManager = cm;
    this.keyManager = km;
    this.bigIntFactory = bigIntFactory;
    this.bbf = buildingBlockFactory;
    this.zkDirector = zkDirector;
    this.attributeConverter = attributeConverter;
  }

  @Override
  public IssuanceMessage preIssuancePresentation(final String username, final IssuanceMessage issuanceMessage,
                                                 final IssuanceTokenDescription itd, final List<URI> listOfCredentialIds, final List<URI> listOfPseudonyms,
      final List<Attribute> userProvidedAttributes) throws IssuanceOrchestrationException {
    final IssuanceMessageFacade imf = new IssuanceMessageFacade(issuanceMessage, bigIntFactory);
    final IssuancePolicy ip = imf.getIssuancePolicy();
    if (ip == null) {
      throw new IssuanceOrchestrationException(
          "The given issuance message does not contain an issuance policy (complex issuance).");
    }
    final VerifierParameters vp = ip.getVerifierParameters();
    final URI context = imf.getContext();


    boolean hasJointRandom = false;
    if (ip.getCredentialTemplate().getUnknownAttributes() != null) {
      hasJointRandom =
          (ip.getCredentialTemplate().getUnknownAttributes().getJointlyRandomAttribute().size() > 0);
    }

    final PhaseRecipient phase =
        hasJointRandom ? PhaseRecipient.JOINT_RANDOM : PhaseRecipient.PRESENTATION_PROOF;
    final int nextStep = 0;
    final StateRecipientImpl state =
        new StateRecipientImpl(phase, nextStep, ip.getCredentialTemplate(), itd, vp,
            listOfCredentialIds, listOfPseudonyms, userProvidedAttributes, null);
    storage.storeState(context, state);

    final IssuMsgOrCredDesc ret = issuanceStep(username, issuanceMessage);
    return ret.im;
  }

  @Override
  public IssuMsgOrCredDesc issuanceStep(final String username, final IssuanceMessage issuanceMessage)
      throws IssuanceOrchestrationException {
    final IssuanceMessageFacade imf = new IssuanceMessageFacade(issuanceMessage, bigIntFactory);
    final URI context = imf.getContext();

    StateRecipient state = storage.retrieveAndDeleteState(context);
    if (state == null) {
      state = initializeStateForSimpleIssuance(imf);
    }

    switch (state.getNextExpectedPhase()) {
      case JOINT_RANDOM:
        return jointRandomPhase(imf, state);
      case PRESENTATION_PROOF:
        return presentationProofPhase(username, imf, state);
      case INTERACTIVE_SIGNING:
        return interactiveSigningPhase(username, imf, state);
      default:
        throw new RuntimeException("Unknown phase in storage");
    }
  }

  private StateRecipient initializeStateForSimpleIssuance(final IssuanceMessageFacade imf)
      throws IssuanceOrchestrationException {
    final StateRecipient state;
    // First issuance message of simple issuance must contain a credential template
    if (imf.getIssuancePolicy() != null) {
      throw new IssuanceOrchestrationException(
          "The given issuance message requires a pre-issuance presentation");
    }
    final CredentialTemplate ct = imf.getCredentialTemplate();
    if (ct == null) {
      throw new IssuanceOrchestrationException(
          "The given issuance message does not contain a credential template (simple issuance)");
    }
    final PhaseRecipient phase = PhaseRecipient.INTERACTIVE_SIGNING;
    final int nextStep = 0;
    final List<URI> creds = Collections.emptyList();
    final List<URI> nyms = Collections.emptyList();
    final List<Attribute> atts = Collections.emptyList();
    state = new StateRecipientImpl(phase, nextStep, ct, null, null, creds, nyms, atts, null);
    return state;
  }

  private IssuMsgOrCredDesc jointRandomPhase(final IssuanceMessageFacade imf, final StateRecipient state) {
    throw new RuntimeException("Joint random phase not implemented");
  }

  private IssuMsgOrCredDesc presentationProofPhase(final String username, final IssuanceMessageFacade imf, final StateRecipient state)
      throws IssuanceOrchestrationException {
    try {
      final Pair<IssuanceToken, CarryOverStateRecipientWithAttributes> res =
          presOrch.createProof(username, state.getIssuanceTokenDescription(),
              state.getCredentialUrisForPresentation(), state.getPseudonymUrisForPresentation(),
              state.getVerifierParameters());
      final IssuanceToken it = res.first;
      final CarryOverStateRecipientWithAttributes coState = res.second;

      final PhaseRecipient nextPhase = PhaseRecipient.INTERACTIVE_SIGNING;
      final int nextStep = 0;
      final StateRecipient newState = new StateRecipientImpl(state, nextPhase, nextStep, coState);
      storage.storeState(imf.getContext(), newState);

      final IssuanceMessageFacade newImf = new IssuanceMessageFacade();
      newImf.setIssuanceToken(it);
      newImf.setContext(imf.getContext());
      return new IssuMsgOrCredDesc(newImf.getIssuanceMessage());

    } catch (PresentationOrchestrationException e) {
      throw new IssuanceOrchestrationException(e);
    }
  }

  private IssuMsgOrCredDesc interactiveSigningPhase(String username, IssuanceMessageFacade imf, StateRecipient state)
      throws IssuanceOrchestrationException {
    try {
      final SystemParameters sp = keyManager.getSystemParameters();
      final SystemParametersWrapper spw = new SystemParametersWrapper(sp);
      final URI issuerUri = state.getCredentialTemplate().getIssuerParametersUID();
      final IssuerParameters ip = keyManager.getIssuerParameters(issuerUri);
      final IssuerParametersFacade ipf = new IssuerParametersFacade(ip);
      final SignatureBuildingBlock sigBB = bbf.getSignatureBuildingBlockById(ipf.getBuildingBlockId());
      final URI credSpecUri = state.getCredentialTemplate().getCredentialSpecUID();
      final CredentialSpecification cs = keyManager.getCredentialSpecification(credSpecUri);
      final CredentialSpecificationWrapper csw = new CredentialSpecificationWrapper(cs, bigIntFactory);

      final int step = state.getStepOfNextExpectedPhase();
      final int extraRounds = sigBB.getNumberOfAdditionalIssuanceRoundtrips();
      IssuanceStateRecipientWithAttributes issState = null;

      if (step == 0) {
        // Verify proof & create issuance state
        final CarryOverStateRecipientWithAttributes coStateWA =
            (CarryOverStateRecipientWithAttributes) state.getPhaseDependantObject();
        final CarryOverStateRecipient coState;
        boolean hasDevice = false;
        if (coStateWA != null) {
          coState = coStateWA.coState;
          hasDevice = (coStateWA.deviceUid != null);
        } else {
          coState = null;
        }
        final ZkProof proof = imf.getZkProof();
        final AttributeList issuerAttributes = imf.getIssuerProvidedAttributes();
        final MechanismSpecification ms = imf.getMechanismSpecification();
        final MechanismSpecificationWrapper msw = new MechanismSpecificationWrapper(ms);
        if (!msw.getSystemParameterUri().equals(spw.getSystemParametersId())) {
          throw new IssuanceOrchestrationException("Incompatible system parameters id");
        }

        final List<ZkModuleVerifier> zkv_l = new ArrayList<ZkModuleVerifier>();
        final BigInt credSpecId = csw.getCredSpecId(spw.getHashFunction());
        final ZkModuleVerifierIssuance zkv_iss =
            sigBB.getZkModuleVerifierIssuance(sp, state.getVerifierParameters(),
                ipf.getPublicKey(), "newcred:0", credSpecId, hasDevice,
                csw.getNumberOfAttributes(), coState);
        zkv_l.add(zkv_iss);

        ZkModuleVerifier zkv =
            bbf.getBuildingBlockByClass(MechanismSpecificationBuildingBlock.class)
                .getZkModuleVerifier("ms", sp, ms);
        zkv_l.add(zkv);

        zkv =
            bbf.getBuildingBlockByClass(IssuerPublicKeyBuildingBlock.class).getZkModuleVerifier(
                "newcred:0:ip", sp, ipf.getPublicKey());
        zkv_l.add(zkv);

        zkv =
            bbf.getBuildingBlockByClass(CredentialSpecificationBuildingBlock.class)
                .getZkModuleVerifier("newcred:0:cs", sp, cs, bigIntFactory);
        zkv_l.add(zkv);


        ZkModuleVerifierRevocation zkv_revocation = null;
        if (csw.isRevocable()) {

          // set the module name
          final String revocationModuleName = zkv_iss.getIdentifier() + ":revocation";

          // read implementation from mechanism specification
          final URI implementationId = msw.getImplementationChoice(revocationModuleName);

          final RevocationAuthorityParameters raParameters =
              keyManager.getRevocationAuthorityParameters(ipf.getRevocationAuthorityId());
          final RevocationAuthorityParametersFacade rapf =
              new RevocationAuthorityParametersFacade(raParameters);

          if (Configuration.debug()) {
            final URI implementationIdIssuerParameters = rapf.getRevocationMechanism();
            if (!implementationId.equals(implementationIdIssuerParameters)) {
              throw new ConfigurationException(
                  "Revocation mechanism specified in issuer parameters and in mechanism specification do not match.");
            }
          }

          final int revocationHandleIndex =
              csw.getRevocationHandleAttributeIndex(csw.getRevocationHandleAttributeDescription());

          final RevocationBuildingBlock revocationBuildingBlock =
              (RevocationBuildingBlock) bbf.getBuildingBlockById(implementationId);

          zkv_revocation =
              revocationBuildingBlock.getZkModuleVerifierIssuance(sp,
                  state.getVerifierParameters(), rapf.getPublicKey(), revocationModuleName, bbf);
          zkv_l.add(zkv_revocation);

          // add attribute equality building block for revocation handle
          zkv =
              bbf.getBuildingBlockByClass(AttributeEqualityBuildingBlock.class)
                  .getZkModuleVerifier(zkv_iss.identifierOfAttribute(revocationHandleIndex),
                      zkv_revocation.identifierOfAttribute(0), false);
          zkv_l.add(zkv);
        }
        final boolean proofOk = zkDirector.verifyProof(proof, zkv_l, sp);
        if (!proofOk) {
          throw new IssuanceOrchestrationException("Issuance proof did not verify");
        }

        issState = new IssuanceStateRecipientWithAttributes();
        issState.coState = coStateWA;
        issState.issuerAttributes = issuerAttributes.getAttributes();
        issState.isr = zkv_iss.recoverIssuanceState();

        if (csw.isRevocable()) {
          // NonRevocationEvidence nre = zkv_revocation.getNonRevocationEvidence();
          final NonRevocationEvidence nre = imf.getNonRevocationEvidence();
          issState.nre = nre;
        }
      } else {
        // extract issuance state from phase-dependant-object
        issState = (IssuanceStateRecipientWithAttributes) state.getPhaseDependantObject();
        if (issState == null) {
          throw new RuntimeException("issState is null from state object");
        }
      }

      if (step == extraRounds) {
        // Extract credential
        final @Nullable
        IssuanceExtraMessage messageFromIssuer = imf.getAdditionalMessage();

        final ListOfSignaturesAndAttributes sig = sigBB.extractSignature(messageFromIssuer, issState.isr);

        final CredentialFacade newCf =
            new CredentialFacade(cs, attributeConverter, state.getCredentialTemplate(),
                issState.coState, issState.issuerAttributes, issState.nre, sig);

        credentialManager.storeCredential(username, newCf.getDelegateeValue());
        return new IssuMsgOrCredDesc(newCf.getCredentialDescription());
      } else {
        // Continue issuance
        final IssuanceMessageFacade newImf = new IssuanceMessageFacade();
        newImf.setContext(imf.getContext());

        final @Nullable
        IssuanceExtraMessage messageFromIssuer = imf.getAdditionalMessage();
        final IssuanceExtraMessage messageToIssuer =
            sigBB.extraIssuanceRoundRecipient(messageFromIssuer, issState.isr);
        newImf.setAdditionalMessage(messageToIssuer);

        final StateRecipient newState =
            new StateRecipientImpl(state, state.getNextExpectedPhase(), step + 1, issState);
        storage.storeState(imf.getContext(), newState);

        return new IssuMsgOrCredDesc(newImf.getIssuanceMessage());
      }
    } catch (ConfigurationException|KeyManagerException|CredentialManagerException e) {
      throw new IssuanceOrchestrationException(e);
    }
  }

  @Override
  public IssuancePolicy extractIssuancePolicy(final IssuanceMessage issuanceMessage) {
    // First issuance message of complex issuance must contain an issuance policy
    final IssuanceMessageFacade imf = new IssuanceMessageFacade(issuanceMessage, bigIntFactory);
    return imf.getIssuancePolicy();
  }

}
