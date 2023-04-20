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

import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import com.ibm.zurich.idmix.abc4trust.facades.Abc4TrustSecretKeyFacade;
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
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.IssuanceOrchestrationException;
import com.ibm.zurich.idmx.exception.PresentationOrchestrationException;
import com.ibm.zurich.idmx.interfaces.orchestration.issuance.IssuanceOrchestrationIssuer;
import com.ibm.zurich.idmx.interfaces.orchestration.issuance.PhaseIssuer;
import com.ibm.zurich.idmx.interfaces.orchestration.issuance.StateIssuer;
import com.ibm.zurich.idmx.interfaces.orchestration.issuance.StateStorage;
import com.ibm.zurich.idmx.interfaces.orchestration.presentation.PresentationOrchestrationVerifier;
import com.ibm.zurich.idmx.interfaces.proofEngine.ZkDirector;
import com.ibm.zurich.idmx.interfaces.state.CarryOverStateIssuer;
import com.ibm.zurich.idmx.interfaces.state.IssuanceStateIssuer;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.Pair;
import com.ibm.zurich.idmx.interfaces.util.RandomGeneration;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProver;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProverIssuance;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProverRevocation;
import com.ibm.zurich.idmx.jaxb.wrapper.CredentialSpecificationWrapper;
import com.ibm.zurich.idmx.orchestration.presentation.MechanismSpecificationWrapper;
import com.ibm.zurich.idmx.parameters.system.SystemParametersWrapper;

import eu.abc4trust.abce.internal.issuer.credentialManager.CredentialManager;
import eu.abc4trust.abce.internal.issuer.tokenManagerIssuer.TokenManagerIssuer;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.revocationProxy.RevocationProxy;
import eu.abc4trust.util.AttributeConverter;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.AttributeDescription;
import eu.abc4trust.xml.AttributeInLogEntry;
import eu.abc4trust.xml.AttributeList;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.CredentialTemplate;
import eu.abc4trust.xml.IssuanceExtraMessage;
import eu.abc4trust.xml.IssuanceLogEntry;
import eu.abc4trust.xml.IssuanceMessage;
import eu.abc4trust.xml.IssuanceMessageAndBoolean;
import eu.abc4trust.xml.IssuancePolicy;
import eu.abc4trust.xml.IssuanceToken;
import eu.abc4trust.xml.IssuanceTokenAndIssuancePolicy;
import eu.abc4trust.xml.IssuanceTokenDescription;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.NonRevocationEvidence;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PresentationPolicy;
import eu.abc4trust.xml.PrivateKey;
import eu.abc4trust.xml.RevocationAuthorityParameters;
import eu.abc4trust.xml.SecretKey;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.VerifierParameters;
import eu.abc4trust.xml.ZkProof;

import javax.inject.Inject;

public class IssuanceOrchestrationIssuerImpl implements IssuanceOrchestrationIssuer {

  private final StateStorage<StateIssuer> storage;
  private final PresentationOrchestrationVerifier presOrch;
  private final CredentialManager credManager;
  private final KeyManager keyManager;
  private final BigIntFactory bigIntFactory;
  private final BuildingBlockFactory bbf;
  private final ZkDirector zkDirector;
  private final AttributeConverter attributeConverter;
  private final RandomGeneration randomGeneration;
  private final TokenManagerIssuer tokenManager;
  private final RevocationProxy revocationProxy;

  private static final String ISSUANCE_CONTEXT_PREFIX = "iss-ctx-";

  @Inject
  public IssuanceOrchestrationIssuerImpl(final StateStorage<StateIssuer> storage,
                                         final PresentationOrchestrationVerifier presOrch,
                                         final CredentialManager credManager,
      final KeyManager keyManager, final BigIntFactory bigIntFactory, final BuildingBlockFactory bbf,
      final ZkDirector zkDirector, final AttributeConverter attributeConverter,
      final RandomGeneration randomGeneration, final TokenManagerIssuer tokenManager,
      final RevocationProxy revocationProxy) {
    this.storage = storage;
    this.presOrch = presOrch;
    this.credManager = credManager;
    this.keyManager = keyManager;
    this.bigIntFactory = bigIntFactory;
    this.bbf = bbf;
    this.zkDirector = zkDirector;
    this.attributeConverter = attributeConverter;
    this.randomGeneration = randomGeneration;
    this.tokenManager = tokenManager;
    this.revocationProxy = revocationProxy;
  }

  @Override
  public IssuanceMessageAndBoolean initializeIssuance(final IssuancePolicy issuancePolicy,
                                                      final List<Attribute> issuerProvidedAttributes, @Nullable URI context)
      throws IssuanceOrchestrationException {

    if (context == null) {
      context = URI.create(ISSUANCE_CONTEXT_PREFIX + randomGeneration.generateRandomUid());
    }

    final boolean simpleIssuance = isSimpleIssuance(issuancePolicy);
    assertSameKeyBindingPresentForKeyBoundCreds(issuancePolicy);
    final boolean hasJointRandom;

    if (issuancePolicy.getCredentialTemplate().getUnknownAttributes() != null) {
      hasJointRandom =
          (issuancePolicy.getCredentialTemplate().getUnknownAttributes()
              .getJointlyRandomAttribute().size() > 0);
    } else {
      hasJointRandom = false;
    }

    final PhaseIssuer phase;
    if (simpleIssuance) {
      phase = PhaseIssuer.START_SIGN;
    } else if (hasJointRandom) {
      phase = PhaseIssuer.JOINT_RANDOM;
    } else {
      phase = PhaseIssuer.CHECK_ISSUANCE_TOKEN;
    }
    final int nextStep = 0;

    final StateIssuer newState =
        new StateIssuerImpl(phase, nextStep, issuancePolicy, issuerProvidedAttributes, null, null);

    if (simpleIssuance) {
      return startSignature(context, newState, true, null);
    } else {
      storage.storeState(context, newState);

      final IssuanceMessageFacade imf = new IssuanceMessageFacade();
      imf.setIssuancePolicy(issuancePolicy);
      imf.setContext(context);

      final IssuanceMessageAndBoolean ret = new ObjectFactory().createIssuanceMessageAndBoolean();
      ret.setIssuanceMessage(imf.getIssuanceMessage());
      ret.setLastMessage(false);
      ret.setIssuanceLogEntryURI(null);
      return ret;
    }
  }

  private void assertSameKeyBindingPresentForKeyBoundCreds(IssuancePolicy issuancePolicy)
      throws IssuanceOrchestrationException {
    CredentialSpecification cs;
    try {
      cs =
          keyManager.getCredentialSpecification(issuancePolicy.getCredentialTemplate()
              .getCredentialSpecUID());
    } catch (KeyManagerException e) {
      throw new IssuanceOrchestrationException(e);
    }
    if (cs.isKeyBinding()) {
      if (issuancePolicy.getCredentialTemplate().getSameKeyBindingAs() == null) {
        throw new IssuanceOrchestrationException(
            "Key bound credentials require that sameKeyBindingAs be set in the issuance Policy.");
      }
    }
  }

  @Override
  public IssuanceMessageAndBoolean issuanceStep(final IssuanceMessage issuanceMessage)
      throws IssuanceOrchestrationException {
    final URI context = issuanceMessage.getContext();
    final StateIssuer state = storage.retrieveAndDeleteState(context);
    final IssuanceMessageFacade imf = new IssuanceMessageFacade(issuanceMessage, bigIntFactory);

    switch (state.getNextExpectedPhase()) {
      case INTERACTIVE_SIGNING:
        return interactiveSigningPhase(imf, state);
      case JOINT_RANDOM:
        return jointRandomPhase(imf, state);
      case PRESENTATION_VERIFY:
        return presentationVerifyPhase(imf, state);
      case CHECK_ISSUANCE_TOKEN:
        throw new IssuanceOrchestrationException(
            "Did not call extractIssuanceTokenDescription() before calling issuanceStep. "
                + "The issuance token description was therefore not checked.");
      case START_SIGN:
        throw new RuntimeException("Start sign is a virtual phase");
      default:
        throw new RuntimeException("Unknown phase");
    }
  }


  private IssuanceMessageAndBoolean jointRandomPhase(final IssuanceMessageFacade imf, final StateIssuer state) {
    throw new RuntimeException("Joint random phase not implemented");
  }

  private IssuanceMessageAndBoolean presentationVerifyPhase(final IssuanceMessageFacade imf,
                                                            final StateIssuer state) throws IssuanceOrchestrationException {
    try {
      final IssuanceToken it = imf.getIssuanceToken();
      if (it == null) {
        throw new IssuanceOrchestrationException("Expected issuance token in issuance message");
      }
      final VerifierParameters vp = state.getIssuancePolicy().getVerifierParameters();
      final Pair<Boolean, CarryOverStateIssuer> ret = presOrch.verifyProof(it, vp);
      if (!ret.first) {
        throw new IssuanceOrchestrationException("Carry over proof failed");
      }
      final StateIssuer newState = new StateIssuerImpl(state, PhaseIssuer.START_SIGN, 0, ret.second);

      tokenManager.storeToken(it);

      return startSignature(imf.getContext(), newState, false, it);
    } catch (final PresentationOrchestrationException e) {
      throw new IssuanceOrchestrationException(e);
    }
  }

  private IssuanceMessageAndBoolean startSignature(final URI context, final StateIssuer state,
                                                   final boolean simpleIssuance, final IssuanceToken it) throws IssuanceOrchestrationException {
    try {
      final SystemParameters sp = keyManager.getSystemParameters();
      final SystemParametersWrapper spw = new SystemParametersWrapper(sp);
      final URI issuerUri = state.getIssuancePolicy().getCredentialTemplate().getIssuerParametersUID();
      final IssuerParameters ip = keyManager.getIssuerParameters(issuerUri);
      final SecretKey sk = credManager.getIssuerSecretKey(issuerUri);
      final IssuerParametersFacade ipf = new IssuerParametersFacade(ip);
      final SignatureBuildingBlock sigBB = bbf.getSignatureBuildingBlockById(ipf.getBuildingBlockId());
      final URI credSpecUri = state.getIssuancePolicy().getCredentialTemplate().getCredentialSpecUID();
      final CredentialSpecification cs = keyManager.getCredentialSpecification(credSpecUri);
      final CredentialSpecificationWrapper csw = new CredentialSpecificationWrapper(cs, bigIntFactory);

      final @Nullable
      CarryOverStateIssuer coState = (CarryOverStateIssuer) state.getPhaseDependantObject();
      final int extraRounds = sigBB.getNumberOfAdditionalIssuanceRoundtrips();

      final MechanismSpecificationWrapper msw = new MechanismSpecificationWrapper();
      msw.setSystemParameterId(spw.getSystemParametersId());

      final boolean hasDevice =
          (state.getIssuancePolicy().getCredentialTemplate().getSameKeyBindingAs() != null);

      final List<BigInt> issuerAttributes = extractIssuerAttributes(state.getIssuerSetAttributes(), csw);
      final Abc4TrustSecretKeyFacade isf = new Abc4TrustSecretKeyFacade(sk);
      final PrivateKey isk = isf.getPrivateKey();

      final List<ZkModuleProver> zkp_l = new ArrayList<ZkModuleProver>();
      final BigInt credSpecId = csw.getCredSpecId(spw.getHashFunction());
      final ZkModuleProverIssuance zkp_iss =
          sigBB.getZkModuleProverIssuance(sp, state.getVerifierParameters(), ipf.getPublicKey(),
              isk, "newcred:0", credSpecId, hasDevice, issuerAttributes, coState);
      zkp_l.add(zkp_iss);

      ZkModuleProverRevocation zkp_revocation = null;
      ZkModuleProver zkp = null;
      if (csw.isRevocable()) {

        // set the module name
        final String revocationModuleName = zkp_iss.getIdentifier() + ":revocation";

        // add implementation to mechanism specification
        final RevocationAuthorityParameters raParameters =
            keyManager.getRevocationAuthorityParameters(ipf.getRevocationAuthorityId());
        final RevocationAuthorityParametersFacade rapf =
            new RevocationAuthorityParametersFacade(raParameters);
        final URI implementationId = rapf.getRevocationMechanism();
        msw.setImplementationChoice(revocationModuleName, implementationId);

        // add revocation building block
        final RevocationBuildingBlock revocationBuildingBlock =
            (RevocationBuildingBlock) bbf.getBuildingBlockById(implementationId);
        final int revocationHandleIndex =
            csw.getRevocationHandleAttributeIndex(csw.getRevocationHandleAttributeDescription());
        zkp_revocation =
            revocationBuildingBlock.getZkModuleProverIssuance(sp, state.getVerifierParameters(),
                rapf.getPublicKey(), csw.getCredentialSpecification(), revocationModuleName,
                revocationProxy, bbf);
        zkp_l.add(zkp_revocation);

        // add attribute equality building block for revocation handle
        zkp =
            bbf.getBuildingBlockByClass(AttributeEqualityBuildingBlock.class).getZkModuleProver(
                zkp_iss.identifierOfAttribute(revocationHandleIndex),
                zkp_revocation.identifierOfAttribute(0), false);
        zkp_l.add(zkp);
      }

      zkp =
          bbf.getBuildingBlockByClass(MechanismSpecificationBuildingBlock.class).getZkModuleProver(
              "ms", sp, msw.getMechanismSpecification());
      zkp_l.add(zkp);

      zkp =
          bbf.getBuildingBlockByClass(IssuerPublicKeyBuildingBlock.class).getZkModuleProver(
              "newcred:0:ip", sp, ipf.getPublicKey());
      zkp_l.add(zkp);

      zkp =
          bbf.getBuildingBlockByClass(CredentialSpecificationBuildingBlock.class)
              .getZkModuleProver("newcred:0:cs", sp, cs, bigIntFactory);
      zkp_l.add(zkp);

      final ZkProof proof = zkDirector.buildProof("", zkp_l, sp);

      if (csw.isRevocable()) {
        // recover the value of the revocation handle
        final NonRevocationEvidence nonRevocationEvidence = zkp_revocation.recoverNonRevocationEvidence();
        // add revocation handle to the state
        state.setNonRevocationEvidence(nonRevocationEvidence);
      }

      final IssuanceMessageAndBoolean ret = new ObjectFactory().createIssuanceMessageAndBoolean();
      final IssuanceMessageFacade newImf = new IssuanceMessageFacade();
      newImf.setContext(context);
      if (simpleIssuance) {
        newImf.setCredentialTemplate(state.getIssuancePolicy().getCredentialTemplate());
      }
      newImf.setZkProof(proof);
      newImf.setMechanismSpecification(msw.getMechanismSpecification());
      newImf.setIssuerProvidedAttributes(state.getIssuerSetAttributes());

      final IssuanceStateIssuer isi = zkp_iss.recoverIssuanceState();
      final List<BigInt> attributes = zkp_iss.recoverEncodedAttributes();

      final IssuanceLogEntry logEntry = new ObjectFactory().createIssuanceLogEntry();
      logEntry.setIssuerParametersUID(issuerUri);
      logEntry.setIssuanceToken(it);
      final Iterator<AttributeDescription> ad_it =
          cs.getAttributeDescriptions().getAttributeDescription().iterator();
      for (final BigInt att : attributes) {
        final AttributeDescription ad = ad_it.next();
        if (att != null) {
          final AttributeInLogEntry aile = new ObjectFactory().createAttributeInLogEntry();
          aile.setAttributeType(ad.getType());
          aile.setAttributeValue(att.getValue());
          logEntry.getIssuerAttributes().add(aile);
        }
      }

      final URI logEntryUri = tokenManager.storeIssuanceLogEntry(logEntry);

      if (extraRounds > 0) {
        final StateIssuer newState = new StateIssuerImpl(state, PhaseIssuer.INTERACTIVE_SIGNING, 1, isi);
        storage.storeState(context, newState);
        ret.setLastMessage(false);
      } else {
        ret.setLastMessage(true);
      }

      if (csw.isRevocable()) {
        newImf.setNonRevocationEvidence(state.getNonRevocationEvidence());
      }

      ret.setIssuanceLogEntryURI(logEntryUri);
      ret.setIssuanceMessage(newImf.getIssuanceMessage());

      return ret;
    } catch (final Exception e) {
      throw new IssuanceOrchestrationException(e);
    }
  }

  private List<BigInt> extractIssuerAttributes(final AttributeList issuerSetAttributes,
    final CredentialSpecificationWrapper csw) throws IssuanceOrchestrationException {
    final LinkedHashMap<URI, BigInt> map = new LinkedHashMap<URI, BigInt>();
    // Fill the map with all attribute types in the correct order
    for (final AttributeDescription ad : csw.getCredentialSpecification().getAttributeDescriptions()
        .getAttributeDescription()) {
      map.put(ad.getType(), null);
    }
    // Fill in the values
    for (final Attribute a : issuerSetAttributes.getAttributes()) {
      final URI type = a.getAttributeDescription().getType();
      if (!map.containsKey(type)) {
        throw new IssuanceOrchestrationException("Unknown attribute type " + type
            + " in issuer specified attributes");
      }
      final BigInteger value_bi = attributeConverter.getIntegerValueOrNull(a);
      final BigInt value;
      if (value_bi != null) {
        value = bigIntFactory.valueOf(value_bi);
      } else {
        value = null;
      }
      map.put(type, value);
    }
    return new ArrayList<BigInt>(map.values());
  }

  private IssuanceMessageAndBoolean interactiveSigningPhase(final IssuanceMessageFacade imf,
                                                            final StateIssuer state) throws IssuanceOrchestrationException {
    try {
      final URI issuerUri = state.getIssuancePolicy().getCredentialTemplate().getIssuerParametersUID();
      final IssuerParameters ip = keyManager.getIssuerParameters(issuerUri);
      final IssuerParametersFacade ipf = new IssuerParametersFacade(ip);
      final SignatureBuildingBlock sigBB = bbf.getSignatureBuildingBlockById(ipf.getBuildingBlockId());

      final int step = state.getStepOfNextExpectedPhase();
      final int maxSteps = sigBB.getNumberOfAdditionalIssuanceRoundtrips();

      final IssuanceStateIssuer stateIssuer = (IssuanceStateIssuer) state.getPhaseDependantObject();
      final IssuanceExtraMessage messageFromRecipient = imf.getAdditionalMessage();
      final IssuanceExtraMessage messageToRecipient =
          sigBB.extraIssuanceRoundIssuer(messageFromRecipient, stateIssuer);

      final IssuanceMessageFacade newImf = new IssuanceMessageFacade();
      newImf.setContext(imf.getContext());
      newImf.setAdditionalMessage(messageToRecipient);

      final IssuanceMessageAndBoolean ret = new ObjectFactory().createIssuanceMessageAndBoolean();
      ret.setIssuanceMessage(newImf.getIssuanceMessage());
      ret.setIssuanceLogEntryURI(null);

      if (step == maxSteps) {
        ret.setLastMessage(true);
      } else {
        ret.setLastMessage(false);
        StateIssuer newState =
            new StateIssuerImpl(state, state.getNextExpectedPhase(), step + 1, stateIssuer);
        storage.storeState(imf.getContext(), newState);
      }

      return ret;
    } catch (KeyManagerException|ConfigurationException e) {
      throw new IssuanceOrchestrationException(e);
    }
  }

  private boolean isSimpleIssuance(IssuancePolicy ip) {
    final PresentationPolicy pp = ip.getPresentationPolicy();

    if (pp != null) {
      if (pp.getMessage() != null) {
        return false;
      } else if (pp.getPseudonym().size() > 0) {
        return false;
      } else if (pp.getCredential().size() > 0) {
        return false;
      } else if (pp.getAttributePredicate().size() > 0) {
        return false;
      } else if (pp.getVerifierDrivenRevocation().size() > 0) {
        return false;
      }
    }

    final CredentialTemplate ct = ip.getCredentialTemplate();
    if (ct.getUnknownAttributes() != null) {
      return false;
    } else if (ct.getSameKeyBindingAs() != null) {
      return false;
    }

    return true;
  }

  @Override
  public IssuanceTokenAndIssuancePolicy extractIssuanceTokenAndPolicy(
      final IssuanceMessage issuanceMessage) throws IssuanceOrchestrationException {
    final URI context = issuanceMessage.getContext();
    final StateIssuer state = storage.retrieveAndDeleteState(context);
    if (state == null) {
      throw new IssuanceOrchestrationException("Could not retrieve issuance state with context: "
          + context);
    }
    if (state.getNextExpectedPhase() == PhaseIssuer.CHECK_ISSUANCE_TOKEN) {
      final StateIssuer newState = new StateIssuerImpl(state, PhaseIssuer.PRESENTATION_VERIFY, 0, null);
      storage.storeState(context, newState);

      final IssuanceMessageFacade imf = new IssuanceMessageFacade(issuanceMessage, bigIntFactory);
      final IssuanceToken it = imf.getIssuanceToken();
      if (it == null) {
        throw new IssuanceOrchestrationException("Malformed issuance message. "
            + "Expected it contains issuance token during the current phase.");
      }
      final IssuanceTokenAndIssuancePolicy ret =
          new ObjectFactory().createIssuanceTokenAndIssuancePolicy();
      ret.setIssuancePolicy(state.getIssuancePolicy());
      ret.setIssuanceToken(it);
      return ret;
    } else {
      // During other phases, it doesn't make sense to return anything
      storage.storeState(context, state);
      return null;
    }
  }

  @Override
  public IssuanceTokenDescription extractIssuanceTokenDescription(final IssuanceMessage issuanceMessage) {
    final IssuanceMessageFacade imf = new IssuanceMessageFacade(issuanceMessage, bigIntFactory);
    final IssuanceToken it = imf.getIssuanceToken();
    if (it == null) {
      return null;
    }
    return it.getIssuanceTokenDescription();
  }
}
