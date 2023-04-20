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

import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.ibm.zurich.idmix.abc4trust.facades.CredentialFacade;
import com.ibm.zurich.idmix.abc4trust.facades.PresentationTokenFacade;
import com.ibm.zurich.idmix.abc4trust.facades.PseudonymCryptoFacade;
import com.ibm.zurich.idmx.annotations.Nullable;
import com.ibm.zurich.idmx.buildingBlock.factory.BuildingBlockFactory;
import com.ibm.zurich.idmx.buildingBlock.inspector.InspectorBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.pseudonym.PseudonymBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.pseudonym.scopeExclusive.ScopeExclusivePseudonymBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.pseudonym.standard.StandardPseudonymBuildingBlock;
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
import com.ibm.zurich.idmx.exception.NotEnoughTokensException;
import com.ibm.zurich.idmx.exception.ProofException;
import com.ibm.zurich.idmx.interfaces.configuration.Constants;
import com.ibm.zurich.idmx.interfaces.device.ExternalSecretsManager;
import com.ibm.zurich.idmx.interfaces.proofEngine.ZkDirector;
import com.ibm.zurich.idmx.interfaces.state.CarryOverStateRecipientWithAttributes;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.Pair;
import com.ibm.zurich.idmx.interfaces.util.RandomGeneration;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProver;
import com.ibm.zurich.idmx.interfaces.zkModule.ZkModuleProverCarryOver;

import eu.abc4trust.abce.internal.user.credentialManager.CredentialManager;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.util.AttributeConverter;
import eu.abc4trust.xml.AbstractPseudonym;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.AttributeDescription;
import eu.abc4trust.xml.Credential;
import eu.abc4trust.xml.CredentialDescription;
import eu.abc4trust.xml.CredentialInToken;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.CredentialTemplate;
import eu.abc4trust.xml.MechanismSpecification;
import eu.abc4trust.xml.Message;
import eu.abc4trust.xml.NonRevocationEvidence;
import eu.abc4trust.xml.ObjectFactory;
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

class PresentationOrchestrationProverInternal extends PresentationOrchestrationGeneral {

  private final List<ZkModuleProver> proverModules;
  private List<Pair<Credential, SignatureToken>> credentials;
  private List<PseudonymWithMetadata> pseudonyms;
  private final ZkDirector zkDirector;
  private final Map<URI, URI> secretByAlias;
  private final Map<URI, Map<URI, Attribute>> attributeByAlias;
  private final RandomGeneration random;
  private CarryOverStateRecipientWithAttributes carryOverState;
  private ZkModuleProverCarryOver zkpCarryOver;
  private final CredentialManager credManager;
  private final ExternalSecretsManager deviceManager;
  private boolean simpleProof;

  private static final String CREDENTIAL_UID_PREFIX = "device-cred-";

  public PresentationOrchestrationProverInternal(final ZkDirector zkDirector,
                                                 final BigIntFactory bigIntFactory, final BuildingBlockFactory buildingBlockFactory,
      final KeyManager keyManager, final CredentialManager credManager, final AttributeConverter attributeConverter,
      final RandomGeneration random, final ExternalSecretsManager deviceManager) {
    super(bigIntFactory, buildingBlockFactory, keyManager, attributeConverter);
    proverModules = new ArrayList<ZkModuleProver>();
    this.zkDirector = zkDirector;
    this.secretByAlias = new HashMap<URI, URI>();
    this.attributeByAlias = new HashMap<URI, Map<URI, Attribute>>();
    this.random = random;
    this.credManager = credManager;
    this.deviceManager = deviceManager;
  }

  public Pair<PresentationToken, CarryOverStateRecipientWithAttributes> createProof(final String username,
    final PresentationTokenDescription ptd, final List<URI> credentialUris, final List<URI> pseudonymsUris,
      final @Nullable CredentialTemplate ct, final VerifierParameters vp) throws KeyManagerException, ConfigurationException,
      ProofException, CredentialManagerException, NotEnoughTokensException {

    this.simpleProof = ptd.isUsesSimpleProof();
    final MechanismSpecificationWrapper ms = new MechanismSpecificationWrapper();
    this.pseudonyms = loadPseudonyms(username, pseudonymsUris);
    this.credentials = loadCredentials(username, credentialUris);

    init(ptd, ms, vp);
    populateAliasMaps();
    createBuildingBlocks(username, ct);

    final ZkProof proof = zkDirector.buildProof(username, proverModules, getSp());

    if (ct != null) {
      carryOverState.coState = zkpCarryOver.recoverState();
    }

    final PresentationTokenFacade ptf = new PresentationTokenFacade();
    ptf.addZkProof(proof);
    ptf.addMechanismSpecification(getMs());
    ptf.setPresentationTokenDescription(ptd);
    ptf.setVersion(Constants.IMPLEMENTATION_VERSION);
    return new Pair<PresentationToken, CarryOverStateRecipientWithAttributes>(
        ptf.getPresentationToken(), carryOverState);
  }

  private List<PseudonymWithMetadata> loadPseudonyms(final String username, final List<URI> pseudonymsUris)
      throws CredentialManagerException {
    final List<PseudonymWithMetadata> pseudonyms = new ArrayList<PseudonymWithMetadata>();
    for (final URI u : pseudonymsUris) {
      final PseudonymWithMetadata pwm = credManager.getPseudonym(username, u);
      pseudonyms.add(pwm);
    }
    return pseudonyms;
  }

  private List<Pair<Credential, SignatureToken>> loadCredentials(final String username, final List<URI> credentialUris)
      throws CredentialManagerException, NotEnoughTokensException {
    final List<Pair<Credential, SignatureToken>> credentials =
        new ArrayList<Pair<Credential, SignatureToken>>();
    for (final URI u : credentialUris) {
      final Credential cred = credManager.getCredential(username, u);
      final CredentialFacade credentialFacade = new CredentialFacade(cred);
      final SignatureToken st = credentialFacade.consumeToken(username, credManager);
      credentials
          .add(new Pair<Credential, SignatureToken>(credentialFacade.getDelegateeValue(), st));
    }
    return credentials;
  }

  @Override
  protected void addCredentialZkModules(final String username) throws KeyManagerException, ConfigurationException,
      ProofException {
    int counter = 0;
    final Iterator<Pair<Credential, SignatureToken>> credentialIterator = credentials.iterator();
    for (final CredentialInToken cit : getPtd().getCredential()) {
      final Pair<Credential, SignatureToken> c = credentialIterator.next();
      processCredential(username, cit, c, counter);
      counter++;
    }
  }

  @Override
  protected void addCredentialZkModule(final String username, final SignatureBuildingBlock bb, final SystemParameters sp,
                                       final VerifierParameters vp, final PublicKey ip, final URI issuerUriOnDevice, final String identifierOfModule,
      final @Nullable Pair<Credential, SignatureToken> cat, final BigInt credentialSpecificationId,
      final int numberOfAttributes, final boolean device) throws ConfigurationException, ProofException {
    if (cat == null) {
      throw new RuntimeException("Credential c cannot be null for prover");
    }
    final Credential c = cat.first;
    final SignatureToken signatureToken = cat.second;
    final List<BigInt> attributes = extractAttributes(c.getCredentialDescription());
    final URI deviceUid = c.getCredentialDescription().getSecretReference();
    if (device != (deviceUid != null)) {
      throw new RuntimeException("deviceUid and device not compatible for credential device = "
          + device + " deviceUid = " + deviceUid);
    }
    // Cred Id on device equals the "regular" credential ID
    final URI credIdOnDevice = c.getCredentialDescription().getCredentialUID();

    final ZkModuleProver zkp =
        bb.getZkModuleProverPresentation(sp, vp, ip, identifierOfModule, signatureToken,
            attributes, credentialSpecificationId, deviceUid, username, credIdOnDevice);
    proverModules.add(zkp);

    if (deviceUid != null) {
      // For debugging purposes only: real devices know the issuer associated with the card already.
      deviceManager.associateIssuer(username, deviceUid, credIdOnDevice, issuerUriOnDevice);
    }
  }

  private List<BigInt> extractAttributes(CredentialDescription cd) {
    final List<BigInt> listOfAttributes = new ArrayList<BigInt>();
    for (final Attribute a : cd.getAttribute()) {
      final BigInteger value_bi = attributeConverter.getIntegerValueOrNull(a);
      final BigInt value;
      if (value_bi == null) {
        value = null;
      } else {
        value = bigIntFactory.valueOf(value_bi);
      }
      listOfAttributes.add(value);
    }
    return listOfAttributes;
  }

  @Override
  protected void addPseudonymZkModules(final String username) throws ConfigurationException, ProofException {
    int counter = 0;
    final Iterator<PseudonymWithMetadata> pseudonymIterator = pseudonyms.iterator();
    for (final PseudonymInToken pit : getPtd().getPseudonym()) {
      final PseudonymWithMetadata p = pseudonymIterator.next();
      if (!Arrays.equals(pit.getPseudonymValue(), p.getPseudonym().getPseudonymValue())) {
        throw new RuntimeException("Pseudonym value was not set in presentation token");
      }
      processPseudonym(username, pit, p, counter);
      counter++;
    }
  }

  @Override
  protected void addPseudonymZkModule(final String username, final String moduleId, final SystemParameters sp, final VerifierParameters vp,
                                      final @Nullable PseudonymWithMetadata p, final URI scope, final boolean exclusive, final byte[] pseudonymValue)
      throws ProofException, ConfigurationException {
    if (p == null) {
      throw new RuntimeException("PseudonymWithMetadata p cannot be null for prover");
    }
    if(simpleProof) {
      throw new RuntimeException("Cannot have pseudonyms in simple proofs");
    }
    final Class<? extends PseudonymBuildingBlock> pseudonymImplementation =
        determinePseudonymImplementation(p);
    final PseudonymBuildingBlock bb =
        buildingBlockFactory.getBuildingBlockByClass(pseudonymImplementation);
    getMs().setImplementationChoice(moduleId, bb.getBuildingBlockId());

    final URI deviceUid = p.getPseudonym().getSecretReference();
    final PseudonymCryptoFacade pcf = new PseudonymCryptoFacade(p.getCryptoParams());
    final AbstractPseudonym ap = pcf.getAbstractPseudonym();
    final ZkModuleProver zkp = bb.getZkModuleProver(sp, vp, moduleId, ap, deviceUid, username, scope);
    proverModules.add(zkp);
  }

  @Override
  protected void addCarryOverZkModule(final String username, final SignatureBuildingBlock bb, final SystemParameters sp,
                                      final VerifierParameters vp, final PublicKey ip, final String identifierOfModule,
      final URI aliasOfSecretForCarryOver, final BigInt credSpecId, final URI issuerOnDevice,
      final List<Boolean> setByIssuer) throws ProofException, ConfigurationException {
    final List<Boolean> attributeIsCarriedOver = new ArrayList<Boolean>();
    for (final boolean b : setByIssuer) {
      attributeIsCarriedOver.add(!b);
    }

    final URI identifierOfSecret;
    final URI identifierOfSignatureForSecret;
    if (aliasOfSecretForCarryOver != null) {
      identifierOfSecret = secretByAlias.get(aliasOfSecretForCarryOver);
      carryOverState.deviceUid = identifierOfSecret;
      if (identifierOfSecret == null) {
        throw new RuntimeException("Target of alias doesn't have secret (carry over)");
      }
      // The credential UID and the identifier of the credential on the card are the same
      identifierOfSignatureForSecret = carryOverState.credentialUid;
      // Allocate credential
      deviceManager.allocateCredential(username, identifierOfSecret, identifierOfSignatureForSecret,
          issuerOnDevice, false);
    } else {
      identifierOfSignatureForSecret = null;
      identifierOfSecret = null;
    }

    // TODO include user set attributes
    final List<BigInt> newCredentialAttributes = new ArrayList<BigInt>();
    for (int i = 0; i < setByIssuer.size(); ++i) {
      newCredentialAttributes.add(null);
    }

    zkpCarryOver =
        bb.getZkModuleProverCarryOver(sp, vp, ip, identifierOfModule, identifierOfSecret,
            username, identifierOfSignatureForSecret, credSpecId, attributeIsCarriedOver,
            newCredentialAttributes);
    if(simpleProof) {
      throw new RuntimeException("Cannot have carry over in simple proofs");
    }
    proverModules.add(zkpCarryOver);
  }

  @Override
  protected void updateCarryOverStateWithEquality(final int mySeq, final URI myType, final URI aliasOther,
                                                  final URI typeOther) {
    final Attribute a = attributeByAlias.get(aliasOther).get(typeOther);

    // Copy attribute, except for the type and the uid
    final Attribute newA = new ObjectFactory().createAttribute();
    newA.setAttributeValue(a.getAttributeValue());
    newA.setAttributeUID(URI.create(random.generateRandomUid()));
    newA.setAttributeDescription(new ObjectFactory().createAttributeDescription());
    newA.getAttributeDescription().setType(myType);
    newA.getAttributeDescription().setEncoding(a.getAttributeDescription().getEncoding());
    newA.getAttributeDescription().setDataType(a.getAttributeDescription().getDataType());

    carryOverState.attributes.set(mySeq, newA);
  }

  @Override
  protected void addRevocationZkModule(final RevocationBuildingBlock bb, final String moduleId,
                                       final String attributeId, final SystemParameters systemParameters, final VerifierParameters verifierParameters,
      final PublicKey raPublicKey, final URI revocationInformationVersion,
      final @Nullable NonRevocationEvidence nonRevocationEvidence,
      final @Nullable RevocationInformation revocationInformation,
      final BuildingBlockFactory buildingBlockFactory) throws ConfigurationException, ProofException {
    final ZkModuleProver zkp =
        bb.getZkModuleProverPresentation(moduleId, attributeId, systemParameters, verifierParameters,
            raPublicKey, revocationInformation, nonRevocationEvidence, buildingBlockFactory);
    if(simpleProof) {
      throw new RuntimeException("Cannot have revocation in simple proofs");
    }
    proverModules.add(zkp);
  }

  @Override
  protected void addInspectAttributeZkModule(final InspectorBuildingBlock bb_ins,
                                             final String inspectionModuleId, final SystemParameters systemParameters2,
      final VerifierParameters verifierParameters2, final PublicKey insKey, final URI parametersUid,
      final String attributeId, final AttributeDescription attributeDescription, final byte[] label)
      throws ConfigurationException {
    final ZkModuleProver zkp =
        bb_ins.getZkModuleProverEncryption(inspectionModuleId, systemParameters2,
            verifierParameters2, insKey, attributeId, label);
    if(simpleProof) {
      throw new RuntimeException("Cannot have inspectors in simple proofs");
    }
    proverModules.add(zkp);
  }

  @Override
  protected void addConstantZkModule(final ConstantBuildingBlock bb, final String name, final BigInt value) {
    final ZkModuleProver zkp = bb.getZkModuleProver(name, value);
    proverModules.add(zkp);
  }

  @Override
  protected void addRevealAttributeZkModule(final RevealAttributeBuildingBlock bb, final String attributeId,
                                            final BigInt value) {
    final ZkModuleProver zkp = bb.getZkModuleProver(attributeId);
    proverModules.add(zkp);
  }

  @Override
  protected void addPresentationTokenZkModule(final PresentationTokenDescriptionBuildingBlock bb,
                                              final String name, final SystemParameters systemParameters, final PresentationTokenDescription ptd) {
    final ZkModuleProver zkp = bb.getZkModuleProver(name, systemParameters, ptd);
    if(!simpleProof) {
      proverModules.add(zkp);
    }
  }

  @Override
  public void addMechanismSpecificationZkModule(final MechanismSpecificationBuildingBlock bb,
                                                final String name, final SystemParameters sp, final MechanismSpecification ms) {
    final ZkModuleProver zkp = bb.getZkModuleProver(name, sp, ms);
    if(!simpleProof) {
      proverModules.add(zkp);
    }
  }

  @Override
  protected void addMessageZkModule(final Abc4TrustMessageBuildingBlock bb, final String name, final Message message) {
    final ZkModuleProver zkp = bb.getZkModuleProver(name, message);
    if(!simpleProof) {
      proverModules.add(zkp);
    }
  }

  @Override
  protected void addCredentialSpecificationZkModule(final CredentialSpecificationBuildingBlock bb,
                                                    final String name, final SystemParameters sp, final CredentialSpecification credSpec,
      final BigIntFactory bigIntFactory) {
    final ZkModuleProver zkp = bb.getZkModuleProver(name, sp, credSpec, bigIntFactory);
    if(!simpleProof) {
      proverModules.add(zkp);
    }
  }

  @Override
  protected void addEqualityZkModule(final AttributeEqualityBuildingBlock bb, final String lhs, final String rhs,
                                     final boolean external) {
    final ZkModuleProver zkp = bb.getZkModuleProver(lhs, rhs, external);
    proverModules.add(zkp);

  }

  @Override
  protected void addSystemParametersZkModule(final SystemParametersBuildingBlock bb, final String name,
                                             final SystemParameters sp) {
    final ZkModuleProver zkp = bb.getZkModuleProver(name, sp);
    if(!simpleProof) {
      proverModules.add(zkp);
    }
  }

  @Override
  protected void addVerifierParametersZkModule(final VerifierParametersBuildingBlock bb, final String name,
                                               final SystemParameters sp, final VerifierParameters vp) {
    final ZkModuleProver zkp = bb.getZkModuleProver(name, sp, vp);
    if(!simpleProof) {
      proverModules.add(zkp);
    }
  }

  @Override
  protected void addIssuerKeyZkModule(final IssuerPublicKeyBuildingBlock bb, final String name,
                                      final SystemParameters sp, final PublicKey ip) {
    final ZkModuleProver zkp = bb.getZkModuleProver(name, sp, ip);
    if(!simpleProof) {
      proverModules.add(zkp);
    }
  }

  @Override
  protected void addInspectorKeyZkModule(final InspectorPublicKeyBuildingBlock bb, final String name,
                                         final SystemParameters sp, final PublicKey ip) {
    final ZkModuleProver zkp = bb.getZkModuleProver(name, sp, ip);
    if(!simpleProof) {
      proverModules.add(zkp);
    }
  }

  @Override
  protected void addRevocationKeyZkModule(final RevocationAuthorityPublicKeyBuildingBlock bb,
                                          final String name, final SystemParameters sp, final PublicKey ip) {
    final ZkModuleProver zkp = bb.getZkModuleProver(name, sp, ip);
    if(!simpleProof) {
      proverModules.add(zkp);
    }
  }

  Class<? extends PseudonymBuildingBlock> determinePseudonymImplementation(final PseudonymWithMetadata p) {
    if (p.getPseudonym().isExclusive()) {
      return ScopeExclusivePseudonymBuildingBlock.class;
    } else {
      return StandardPseudonymBuildingBlock.class;
    }
  }

  private void populateAliasMaps() {
    secretByAlias.clear();
    attributeByAlias.clear();

    final Iterator<Pair<Credential, SignatureToken>> credentialIterator = credentials.iterator();
    for (final CredentialInToken cit : getPtd().getCredential()) {
      final Credential c = credentialIterator.next().first;

      final URI alias = cit.getAlias();
      if (alias == null) {
        continue;
      }

      final URI secret = c.getCredentialDescription().getSecretReference();
      if (secret != null) {
        secretByAlias.put(alias, secret);
      }

      final Map<URI, Attribute> attributeMap = new HashMap<URI, Attribute>();
      for (final Attribute a : c.getCredentialDescription().getAttribute()) {
        final URI type = a.getAttributeDescription().getType();
        attributeMap.put(type, a);
      }
      attributeByAlias.put(alias, attributeMap);
    }

    final Iterator<PseudonymWithMetadata> pseudonymIterator = pseudonyms.iterator();
    for (final PseudonymInToken pit : getPtd().getPseudonym()) {
      final URI alias = pit.getAlias();
      final PseudonymWithMetadata p = pseudonymIterator.next();
      if (alias == null) {
        continue;
      }

      final URI secret = p.getPseudonym().getSecretReference();
      if (secret != null) {
        secretByAlias.put(alias, secret);
      }
    }
  }

  @Override
protected void initializeCarryOverState(final int numberOfAttributes) {
    carryOverState = new CarryOverStateRecipientWithAttributes();
    carryOverState.credentialUid = URI.create(CREDENTIAL_UID_PREFIX + random.generateRandomUid());
    carryOverState.attributes = new ArrayList<Attribute>();
    for (int i = 0; i < numberOfAttributes; ++i) {
      carryOverState.attributes.add(null);
    }
  }

  @Override
  protected void failedToLoadParameter(final URI parameter) throws ProofException {
    throw new ProofException("Could not load resource: " + parameter);
  }

  @Override
  protected void addInequalityZkModule(final RangeProofBuildingBlock bb, final String lhs, final String rhs,
                                       final boolean strict, final SystemParameters sp, final VerifierParameters verifierParameters, final int counter) throws ConfigurationException {
    final ZkModuleProver zkp = bb.getZkModuleProver(sp, verifierParameters, lhs, rhs, strict, counter);
    if(!simpleProof) {
      proverModules.add(zkp);
    }
  }

}
