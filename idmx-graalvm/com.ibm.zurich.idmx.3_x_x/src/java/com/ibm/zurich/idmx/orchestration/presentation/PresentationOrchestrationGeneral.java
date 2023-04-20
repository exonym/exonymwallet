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

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ibm.zurich.idmix.abc4trust.facades.CredentialFacade;
import com.ibm.zurich.idmix.abc4trust.facades.InspectorParametersFacade;
import com.ibm.zurich.idmix.abc4trust.facades.IssuerParametersFacade;
import com.ibm.zurich.idmix.abc4trust.facades.RevocationAuthorityParametersFacade;
import com.ibm.zurich.idmx.annotations.Nullable;
import com.ibm.zurich.idmx.buildingBlock.factory.BuildingBlockFactory;
import com.ibm.zurich.idmx.buildingBlock.inspector.InspectorBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.pseudonym.PseudonymBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.pseudonym.scopeExclusive.ScopeExclusivePseudonymBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.pseudonym.standard.StandardPseudonymBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.rangeProof.RangeProofBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.rangeProof.fourSq.FourSquaresRangeProofBuildingBlock;
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
import com.ibm.zurich.idmx.buildingBlock.systemParameters.EcryptSystemParametersWrapper;
import com.ibm.zurich.idmx.configuration.ErrorMessages;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.ProofException;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.Pair;
import com.ibm.zurich.idmx.jaxb.wrapper.CredentialSpecificationWrapper;
import com.ibm.zurich.idmx.keypair.ra.RevocationAuthorityPublicKeyWrapper;

import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.util.AttributeConverter;
import eu.abc4trust.xml.AttributeDescription;
import eu.abc4trust.xml.AttributeInToken;
import eu.abc4trust.xml.AttributePredicate;
import eu.abc4trust.xml.CarriedOverAttribute;
import eu.abc4trust.xml.Credential;
import eu.abc4trust.xml.CredentialInToken;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.CredentialTemplate;
import eu.abc4trust.xml.InspectorPublicKey;
import eu.abc4trust.xml.IssuerParameters;
import eu.abc4trust.xml.MechanismSpecification;
import eu.abc4trust.xml.Message;
import eu.abc4trust.xml.NonRevocationEvidence;
import eu.abc4trust.xml.PresentationTokenDescription;
import eu.abc4trust.xml.PseudonymInToken;
import eu.abc4trust.xml.PseudonymWithMetadata;
import eu.abc4trust.xml.PublicKey;
import eu.abc4trust.xml.RevocationAuthorityParameters;
import eu.abc4trust.xml.RevocationInformation;
import eu.abc4trust.xml.SignatureToken;
import eu.abc4trust.xml.SystemParameters;
import eu.abc4trust.xml.VerifierParameters;

/**
 * 
 */
public abstract class PresentationOrchestrationGeneral {

  private final KeyManager keyManager;
  protected final BuildingBlockFactory buildingBlockFactory;
  protected final BigIntFactory bigIntFactory;
  protected final AttributeConverter attributeConverter;

  //TODO(ksa) not immutable - refactor
  private SystemParameters systemParameters;
  private EcryptSystemParametersWrapper spWrapper;
  private VerifierParameters verifierParameters;
  private MechanismSpecificationWrapper ms;
  private Map<URI, String> identifierByAlias;
  private final Map<URI, Map<URI, Integer>> attributeSeqByAlias = new HashMap<URI, Map<URI, Integer>>();
  private final Map<URI, Map<URI, AttributeDescription>> attributeDescriptionByAlias =
      new HashMap<URI, Map<URI, AttributeDescription>>();

  private PresentationTokenDescription ptd;

  public PresentationOrchestrationGeneral(final BigIntFactory bigIntFactory,
		  final BuildingBlockFactory buildingBlockFactory, final KeyManager keyManager,
		  final AttributeConverter attributeConverter) {

    this.keyManager = keyManager;
    this.buildingBlockFactory = buildingBlockFactory;
    this.bigIntFactory = bigIntFactory;
    this.attributeConverter = attributeConverter;
  }

  protected void init(final PresentationTokenDescription ptd, final MechanismSpecificationWrapper ms,
		  final VerifierParameters vp)
      throws KeyManagerException, ProofException, ConfigurationException {
    this.ptd = ptd;
    this.ms = ms;
    systemParameters = keyManager.getSystemParameters();
    spWrapper = new EcryptSystemParametersWrapper(systemParameters);
    verifierParameters = vp;

    if (ms.getSystemParameterUri() == null) {
      ms.setSystemParameterId(spWrapper.getSystemParametersId());
    }
    if (!ms.getSystemParameterUri().equals(spWrapper.getSystemParametersId())) {
      throw new ProofException("Mechanism specification has wrong system parameters id");
    }
  }

  protected void createBuildingBlocks(final String username, final @Nullable CredentialTemplate ct)
      throws ConfigurationException, ProofException, KeyManagerException {
    identifierByAlias = determineIdentifierByAlias();
    addMessageZkModules();
    addPseudonymZkModules(username);
    addCredentialZkModules(username);
    if (ct != null) {
      addCredentialTemplateZkModule(username, ct);
    }
    addPredicateZkModules();
    addSystemParameters();
    addVerifierParameters();
    addMechanismSpecification();
    addPresentationToken();
  }


  private void addCredentialTemplateZkModule(final String username, final CredentialTemplate ct)
      throws ConfigurationException, KeyManagerException, ProofException {
    final String identifierOfModule = "newcred:0";

    final URI credSpecUid = ct.getCredentialSpecUID();
    final CredentialSpecification credSpec = keyManager.getCredentialSpecification(credSpecUid);
    final CredentialSpecificationWrapper credSpecWrapper =
        new CredentialSpecificationWrapper(credSpec, bigIntFactory);

    final URI issuerParameterUid = ct.getIssuerParametersUID();
    final IssuerParameters issuerParameters = keyManager.getIssuerParameters(issuerParameterUid);
    if (issuerParameters == null) {
      throw new ProofException(ErrorMessages.missingElement(issuerParameterUid.toString(),
          "keyManager"));
    }
    final IssuerParametersFacade ipFacade = new IssuerParametersFacade(issuerParameters);
    final PublicKey ip = ipFacade.getPublicKey();

    final Map<URI, Integer> attributeSeq = getAttributeSeq(credSpec);

    final BigInt credentialSpecificationId = credSpecWrapper.getCredSpecId(spWrapper.getHashFunction());

    addCredentialSpecification(credSpec, identifierOfModule);
    addIssuerKey(ip, identifierOfModule);

    final int numberOfAttributes = credSpecWrapper.getNumberOfAttributes();

    final URI aliasForSecret = ct.getSameKeyBindingAs();

    final List<Boolean> setByIssuer = new ArrayList<Boolean>();
    for (int i = 0; i < numberOfAttributes; ++i) {
      setByIssuer.add(true);
    }

    initializeCarryOverState(numberOfAttributes);

    if (ct.getUnknownAttributes() != null) {
      for (final CarriedOverAttribute coa : ct.getUnknownAttributes().getCarriedOverAttribute()) {
	    final URI aliasOther = coa.getSourceCredentialInfo().getAlias();
        final String idOther = identifierByAlias.get(aliasOther);
        final URI typeOther = coa.getSourceCredentialInfo().getAttributeType();
        final int attSeqOther = attributeSeqByAlias.get(aliasOther).get(typeOther);
        final URI myType = coa.getTargetAttributeType();
        final int mySeq = attributeSeq.get(myType);
        final AttributeEqualityBuildingBlock bb_e =
            buildingBlockFactory.getBuildingBlockByClass(AttributeEqualityBuildingBlock.class);
        final String lhs = identifierOfModule + ":" + mySeq;
        final String rhs = idOther + ":" + attSeqOther;
        final boolean external = false;
        addEqualityZkModule(bb_e, lhs, rhs, external);
        updateCarryOverStateWithEquality(mySeq, myType, aliasOther, typeOther);

        setByIssuer.set(mySeq, false);
      }
    }

    // TODO include user set attributes

    final SignatureBuildingBlock bb =
        buildingBlockFactory.getSignatureBuildingBlockById(ipFacade.getBuildingBlockId());
    addCarryOverZkModule(username, bb, systemParameters, verifierParameters, ip,
        identifierOfModule, aliasForSecret, credentialSpecificationId, issuerParameterUid,
        setByIssuer);

    addSameKeyBindingAs(identifierOfModule, ct.getSameKeyBindingAs());
  }

  protected abstract void initializeCarryOverState(final int numberOfAttributes);

  protected abstract void updateCarryOverStateWithEquality(final int mySeq, final URI myType,
		  final URI aliasOther, final URI typeOther);

  Map<URI, String> determineIdentifierByAlias() {
    final Map<URI, String> identifierByAlias = new HashMap<URI, String>();

    int counter = -1;
    for (final CredentialInToken cit : ptd.getCredential()) {
      counter++;

      final URI alias = cit.getAlias();
      if (alias == null) {
        continue;
      }
      final String identifier = "sig:" + counter;
      identifierByAlias.put(alias, identifier);
    }

    counter = -1;
    for (final PseudonymInToken pit : ptd.getPseudonym()) {
      counter++;

      final URI alias = pit.getAlias();
      if (alias == null) {
        continue;
      }
      final String identifier = "nym:" + counter;
      identifierByAlias.put(alias, identifier);
    }

    return identifierByAlias;
  }

  void addMessageZkModules() throws ConfigurationException {
    Message message = ptd.getMessage();
    if (message != null) {
      final Abc4TrustMessageBuildingBlock bb =
          buildingBlockFactory.getBuildingBlockByClass(Abc4TrustMessageBuildingBlock.class);
      addMessageZkModule(bb, "msg:0", message);
    }
  }

  void addSameKeyBindingAs(final String moduleId, final URI aliasOther) throws ConfigurationException {
    if (aliasOther != null) {
      final String identifier = identifierByAlias.get(aliasOther);
      final boolean external = true;
      final String secretOfOther = identifier + ":secret";
      final String mySecret = moduleId + ":secret";

      final AttributeEqualityBuildingBlock bb =
          buildingBlockFactory.getBuildingBlockByClass(AttributeEqualityBuildingBlock.class);
      addEqualityZkModule(bb, mySecret, secretOfOther, external);
    }
  }

  Class<? extends PseudonymBuildingBlock> determinePseudonymImplementation(PseudonymInToken pit) {
    if (pit.isExclusive()) {
      return ScopeExclusivePseudonymBuildingBlock.class;
    } else {
      return StandardPseudonymBuildingBlock.class;
    }
  }

  protected boolean isInspectable(final AttributeInToken ait) {
    return ait.getInspectorPublicKeyUID() != null;
  }

  protected Map<URI, Integer> getAttributeSeq(final CredentialSpecification credSpec) {
    final Map<URI, Integer> attributeSeq = new HashMap<URI, Integer>();
    int counter = -1;
    for (final AttributeDescription ad : credSpec.getAttributeDescriptions().getAttributeDescription()) {
      counter++;
      attributeSeq.put(ad.getType(), counter);
    }
    return attributeSeq;
  }

  protected Map<URI, AttributeDescription> getAttributeDesc(final CredentialSpecification credSpec) {
    final Map<URI, AttributeDescription> attributeEnc = new HashMap<URI, AttributeDescription>();
    for (final AttributeDescription ad : credSpec.getAttributeDescriptions().getAttributeDescription()) {
      attributeEnc.put(ad.getType(), ad);
    }
    return attributeEnc;
  }

  protected void addCredentialSpecification(final CredentialSpecification credSpec,
      String identifierOfModule) throws ConfigurationException {
    final CredentialSpecificationBuildingBlock bb =
        buildingBlockFactory.getBuildingBlockByClass(CredentialSpecificationBuildingBlock.class);
    addCredentialSpecificationZkModule(bb, identifierOfModule + ":cs", systemParameters, credSpec,
        bigIntFactory);
  }


  protected void addIssuerKey(final PublicKey ip, final String identifierOfModule)
      throws ConfigurationException {
    final IssuerPublicKeyBuildingBlock bb =
        buildingBlockFactory.getBuildingBlockByClass(IssuerPublicKeyBuildingBlock.class);
    addIssuerKeyZkModule(bb, identifierOfModule + ":ip", systemParameters, ip);
  }

  protected void addInspectorKey(final PublicKey ip, final String identifierOfModule)
      throws ConfigurationException {
    final InspectorPublicKeyBuildingBlock bb =
        buildingBlockFactory.getBuildingBlockByClass(InspectorPublicKeyBuildingBlock.class);
    addInspectorKeyZkModule(bb, identifierOfModule + ":inspectorKey", systemParameters, ip);
  }

  protected void addRevocationKey(final PublicKey ip, final String identifierOfModule)
      throws ConfigurationException {
    final RevocationAuthorityPublicKeyBuildingBlock bb =
        buildingBlockFactory
            .getBuildingBlockByClass(RevocationAuthorityPublicKeyBuildingBlock.class);
    addRevocationKeyZkModule(bb, identifierOfModule + ":inspectorKey", systemParameters, ip);
  }

  void addPredicateZkModules() throws ConfigurationException {
    int counter = -1;
    for (final AttributePredicate ap : ptd.getAttributePredicate()) {
      counter++;

      final URI function = ap.getFunction();
      final AttributeDescription ad = determineAttDesc(ap.getAttributeOrConstantValue());
      if (ad == null) {
        System.err.println("Predicate containing only constants: ignored");
        continue;
      }
      int attributeSeq = -1;
      for (final Object attributeOrConstant : ap.getAttributeOrConstantValue()) {
        attributeSeq++;
        processConstant(attributeOrConstant, counter, attributeSeq, ad);
      }

      PredicateDecoder.PredicateType type = PredicateDecoder.getPredicateType(function);
      final boolean STRICT = true;
      final boolean OREQUAL = false;
      final boolean GREATER = true;
      final boolean LESS = false;
      switch (type) {
        case EQUALITY:
          addEqualityPredicate(ap, counter);
          break;
        case INEQUALITY:
          throw new RuntimeException("Not-equal predicate not yet implemented.");
          // TODO
          // addNotEqualPredicate();
          // break;
        case ONEOF:
          throw new RuntimeException("One-of predicate not yet implemented.");
          // TODO
          // addSetMembershipPredicate();
          // break;
        case GREATER_EQUAL:
          addInequalityPredicate(ap, counter, OREQUAL, GREATER);
          break;
        case GREATER_THAN:
          addInequalityPredicate(ap, counter, STRICT, GREATER);
          break;
        case LESS_EQUAL:
          addInequalityPredicate(ap, counter, OREQUAL, LESS);
          break;
        case LESS_THAN:
          addInequalityPredicate(ap, counter, STRICT, LESS);
          break;
        default:
          throw new RuntimeException("Predicate not implemented");
      }
    }
  }

  private AttributeDescription determineAttDesc(final List<Object> listOfAttributeOrConstantValue) {
    for (final Object attOrConstant : listOfAttributeOrConstantValue) {
      if (attOrConstant instanceof AttributePredicate.Attribute) {
        final AttributePredicate.Attribute att = (AttributePredicate.Attribute) attOrConstant;
        final URI alias = att.getCredentialAlias();
        final URI type = att.getAttributeType();
        final AttributeDescription attDesc = attributeDescriptionByAlias.get(alias).get(type);
        return attDesc;
      }
    }
    return null;
  }

  protected void processPseudonym(final String username, final PseudonymInToken pit,
		  final @Nullable PseudonymWithMetadata p, final int counter)
				  throws ConfigurationException, ProofException {
    final String moduleId = "nym:" + counter;
    final URI scope = URI.create(pit.getScope());
    final boolean exclusive = pit.isExclusive();
    addPseudonymZkModule(username, moduleId, systemParameters, verifierParameters, p, scope,
        exclusive, pit.getPseudonymValue());
    addSameKeyBindingAs(moduleId, pit.getSameKeyBindingAs());
  }

  protected void processCredential(final String username, final CredentialInToken cit,
		  final @Nullable Pair<Credential, SignatureToken> c, final int counter)
				  throws KeyManagerException,
      ConfigurationException, ProofException {
    try {
      final String identifierOfModule = "sig:" + counter;
      final URI alias = cit.getAlias();

      final URI credSpecUid = cit.getCredentialSpecUID();
      final CredentialSpecification credSpec = keyManager.getCredentialSpecification(credSpecUid);
      if (credSpec == null) {
        failedToLoadParameter(credSpecUid);
        // If this is not a problem then ignore this credential
        return;
      }
      final CredentialSpecificationWrapper credSpecWrapper =
          new CredentialSpecificationWrapper(credSpec, bigIntFactory);
      final Map<URI, Integer> attributeSeq = getAttributeSeq(credSpec);
      final Map<URI, AttributeDescription> attributeDesc = getAttributeDesc(credSpec);
      if (alias != null) {
        attributeSeqByAlias.put(alias, attributeSeq);
        attributeDescriptionByAlias.put(alias, attributeDesc);
      }

      signatureAndRevocationInCredential(username, cit, c, identifierOfModule, credSpec,
          credSpecWrapper, attributeSeq);
      inspectionInCredential(cit, identifierOfModule, attributeSeq, attributeDesc);
      addSameKeyBindingAs(identifierOfModule, cit.getSameKeyBindingAs());
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  private void signatureAndRevocationInCredential(final String username, final CredentialInToken cit,
		  final @Nullable Pair<Credential, SignatureToken> c, final String identifierOfModule,
		  final CredentialSpecification credSpec, final CredentialSpecificationWrapper credSpecWrapper,
		  final Map<URI, Integer> attributeSeq) throws KeyManagerException, ProofException,
      ConfigurationException {
	final URI issuerParameterUid = cit.getIssuerParametersUID();
    final IssuerParameters issuerParameters = keyManager.getIssuerParameters(issuerParameterUid);
    if (issuerParameters == null) {
      failedToLoadParameter(issuerParameterUid);
      // If this is not a problem, then skip this signature + revocation proof.
      return;
    }
    final IssuerParametersFacade ipFacade = new IssuerParametersFacade(issuerParameters);
    final PublicKey ip = ipFacade.getPublicKey();

    final BigInt credentialSpecificationId = credSpecWrapper.getCredSpecId(spWrapper.getHashFunction());

    addCredentialSpecification(credSpec, identifierOfModule);
    addIssuerKey(ip, identifierOfModule);

    final int numberOfAttributes = credSpecWrapper.getNumberOfAttributes();
    final boolean externalDevice = credSpecWrapper.isKeyBinding();

    final SignatureBuildingBlock bb =
        buildingBlockFactory.getSignatureBuildingBlockById(ipFacade.getBuildingBlockId());
    addCredentialZkModule(username, bb, systemParameters, verifierParameters, ip,
        issuerParameterUid, identifierOfModule, c, credentialSpecificationId, numberOfAttributes,
        externalDevice);

    if (credSpecWrapper.isRevocable()) {
      final URI raType = credSpecWrapper.getRevocationHandleAttributeDescription().getType();
      final int raIndex = attributeSeq.get(raType);
      final String attributeId = identifierOfModule + ":" + raIndex;
      revocationInCredential(c, identifierOfModule, ipFacade, attributeId);
    }
  }

  private void revocationInCredential(final @Nullable Pair<Credential, SignatureToken> c,
		  final String identifierOfModule, final IssuerParametersFacade ipFacade,
		  final String attributeId)
      throws KeyManagerException, ProofException, ConfigurationException {
    final URI rapuid = ipFacade.getRevocationAuthorityId();
    final RevocationAuthorityParameters revocationAuthorityParameters =
        keyManager.getRevocationAuthorityParameters(rapuid);
    if (revocationAuthorityParameters == null) {
      failedToLoadParameter(rapuid);
      // If this is not a problem, then skip this revocation proof.
      return;
    }
    final RevocationAuthorityParametersFacade raParametersFacade =
        new RevocationAuthorityParametersFacade(revocationAuthorityParameters);
    final RevocationAuthorityPublicKeyWrapper raPublicKeyWrapper =
        new RevocationAuthorityPublicKeyWrapper(raParametersFacade.getPublicKey());
    RevocationInformation rInfo = keyManager.getCurrentRevocationInformation(rapuid);
    if (rInfo == null) {
      rInfo = keyManager.getLatestRevocationInformation(rapuid);
    }

    // NRE must have been updated already
    NonRevocationEvidence nre = null;
    if (c != null) {
      final CredentialFacade credentialFacade = new CredentialFacade(c.first);
      nre = credentialFacade.getNonRevocationEvidence();
    }

    final RevocationBuildingBlock revocationBB =
        (RevocationBuildingBlock) buildingBlockFactory.getBuildingBlockById(raPublicKeyWrapper
            .getPublicKeyTechnology());
    final String moduleId = identifierOfModule + ":rev";

    addRevocationZkModule(revocationBB, moduleId, attributeId, systemParameters,
        verifierParameters, raPublicKeyWrapper.getPublicKey(), null, nre, rInfo,
        buildingBlockFactory);
    addRevocationKey(raPublicKeyWrapper.getPublicKey(), moduleId + ":key");
  }

  private void inspectionInCredential(final CredentialInToken cit, final String identifierOfModule,
		  final Map<URI, Integer> attributeSeq, final Map<URI, AttributeDescription> attributeDesc)
      throws KeyManagerException, ProofException, ConfigurationException,
      UnsupportedEncodingException {
    int inspectionCounter = -1;
    for (final AttributeInToken ait : cit.getDisclosedAttribute()) {
      final URI type = ait.getAttributeType();
      final int attributeSeqNum = attributeSeq.get(type);
      final String attributeId = identifierOfModule + ":" + attributeSeqNum;
      final AttributeDescription ad = attributeDesc.get(type);
      if (isInspectable(ait)) {
        inspectionCounter++;
        final String inspectionModuleId = identifierOfModule + ":ins:" + inspectionCounter;
        final URI inspectorId = ait.getInspectorPublicKeyUID();
        final InspectorPublicKey insParam = keyManager.getInspectorPublicKey(inspectorId);
        if (insParam == null) {
          failedToLoadParameter(inspectorId);
          // If this is not a problem, continue with loop
          continue;
        }
        final InspectorParametersFacade insWrap = new InspectorParametersFacade(insParam);
        final PublicKey insKey = insWrap.getPublicKey();
        final URI technology = insWrap.getBuildingBlockId();
        final InspectorBuildingBlock bb_ins =
            (InspectorBuildingBlock) buildingBlockFactory.getBuildingBlockById(technology);
        final byte[] label = ait.getInspectionGrounds().getBytes("UTF-8");
        addInspectAttributeZkModule(bb_ins, inspectionModuleId, systemParameters,
            verifierParameters, insKey, inspectorId, attributeId, ad, label);
        addInspectorKey(insKey, inspectionModuleId);
      } else {
        final RevealAttributeBuildingBlock bb_r =
            buildingBlockFactory.getBuildingBlockByClass(RevealAttributeBuildingBlock.class);
        final BigInteger value_bi = attributeConverter.getValueUnderEncoding(ait.getAttributeValue(), ad);
        final BigInt value = bigIntFactory.valueOf(value_bi);
        addRevealAttributeZkModule(bb_r, attributeId, value);
      }
    }
  }


  private void processConstant(final Object attributeOrConstant, final int counter,
		  final int attributeSeq, final AttributeDescription ad) throws ConfigurationException {
    if (!(attributeOrConstant instanceof AttributePredicate.Attribute)) {
      final String name =
          identifierOfAttributeOrConstantValue(attributeOrConstant, counter, attributeSeq);
      final BigInteger value_bi = attributeConverter.getValueUnderEncoding(attributeOrConstant, ad);
      final BigInt value = bigIntFactory.valueOf(value_bi);

      final ConstantBuildingBlock bb =
          buildingBlockFactory.getBuildingBlockByClass(ConstantBuildingBlock.class);
      addConstantZkModule(bb, name, value);
    }
  }


  private void addEqualityPredicate(final AttributePredicate ap, final int counter)
      throws ConfigurationException {
    final String lhs =
        identifierOfAttributeOrConstantValue(ap.getAttributeOrConstantValue().get(0), counter, 0);
    final String rhs =
        identifierOfAttributeOrConstantValue(ap.getAttributeOrConstantValue().get(1), counter, 1);
    final boolean external = false;
    final AttributeEqualityBuildingBlock bb =
        buildingBlockFactory.getBuildingBlockByClass(AttributeEqualityBuildingBlock.class);
    addEqualityZkModule(bb, lhs, rhs, external);
  }

  private void addInequalityPredicate(final AttributePredicate ap, final int counter,
		  final boolean strict, final boolean greater) throws ConfigurationException {
    final int lhsArgNum;
    final int rhsArgNum;
    if (greater) {
      // Flip arguments if greater
      lhsArgNum = 1;
      rhsArgNum = 0;
    } else {
      lhsArgNum = 0;
      rhsArgNum = 1;
    }

    final String lhs =
        identifierOfAttributeOrConstantValue(ap.getAttributeOrConstantValue().get(lhsArgNum),
            counter, lhsArgNum);
    final String rhs =
        identifierOfAttributeOrConstantValue(ap.getAttributeOrConstantValue().get(rhsArgNum),
            counter, rhsArgNum);

    final RangeProofBuildingBlock bb =
        buildingBlockFactory.getBuildingBlockByClass(FourSquaresRangeProofBuildingBlock.class);
    addInequalityZkModule(bb, lhs, rhs, strict, systemParameters, verifierParameters, counter);
  }

  private String identifierOfAttributeOrConstantValue(final Object attributeOrConstantValue,
		  final int predicateSeq, final int attributeSeq) {
    if (attributeOrConstantValue instanceof AttributePredicate.Attribute) {
      final AttributePredicate.Attribute att = (AttributePredicate.Attribute) attributeOrConstantValue;
      final URI attributeType = att.getAttributeType();
      final URI credentialAlias = att.getCredentialAlias();
      final String sigIdentifier = identifierByAlias.get(credentialAlias);
      final int attInCredSeq = attributeSeqByAlias.get(credentialAlias).get(attributeType);
      return sigIdentifier + ":" + attInCredSeq;
    } else {
      return "constant:" + predicateSeq + ":" + attributeSeq;
    }
  }

  private void addSystemParameters() throws ConfigurationException {
    final SystemParametersBuildingBlock bb =
        buildingBlockFactory.getBuildingBlockByClass(SystemParametersBuildingBlock.class);
    addSystemParametersZkModule(bb, "param:sp", systemParameters);
  }


  private void addVerifierParameters() throws ConfigurationException {
    final VerifierParametersBuildingBlock bb =
        buildingBlockFactory.getBuildingBlockByClass(VerifierParametersBuildingBlock.class);
    addVerifierParametersZkModule(bb, "param:vp", systemParameters, verifierParameters);
  }



  private void addMechanismSpecification() throws ConfigurationException {
    final MechanismSpecificationBuildingBlock bb =
        buildingBlockFactory.getBuildingBlockByClass(MechanismSpecificationBuildingBlock.class);
    addMechanismSpecificationZkModule(bb, "param:ms", systemParameters,
        ms.getMechanismSpecification());
  }



  private void addPresentationToken() throws ConfigurationException {
    final PresentationTokenDescriptionBuildingBlock bb =
        buildingBlockFactory
            .getBuildingBlockByClass(PresentationTokenDescriptionBuildingBlock.class);
    addPresentationTokenZkModule(bb, "param:pt", systemParameters, ptd);
  }

  protected abstract void addCredentialZkModules(final String username) throws KeyManagerException,
      ConfigurationException, ProofException;

  protected abstract void addCredentialZkModule(final String username, final SignatureBuildingBlock bb,
		  final SystemParameters sp, final VerifierParameters vp, final PublicKey ip,
		  final URI issuerUriOnDevice, final String identifierOfModule,
		  final @Nullable Pair<Credential, SignatureToken> c, final BigInt credentialSpecificationId,
		  final int numberOfAttributes, final boolean device)
      throws ProofException, ConfigurationException;

  protected abstract void addPseudonymZkModules(final String username) throws ConfigurationException,
      ProofException;

  protected abstract void addPseudonymZkModule(final String username, final String moduleId,
		  final SystemParameters sp, final VerifierParameters vp,
		  final @Nullable PseudonymWithMetadata p, final URI scope,
		  final boolean exclusive, final byte[] pseudonymValue) throws ProofException, ConfigurationException;

  protected abstract void addRevocationZkModule(final RevocationBuildingBlock bb, final String moduleId,
	  final String attributeId, final SystemParameters systemParameters, final VerifierParameters verifierParameters,
      final PublicKey raPublicKey, final URI revocationInformationVersion,
      final @Nullable NonRevocationEvidence nonRevocationEvidence,
      final  @Nullable RevocationInformation revocationInformation,
      final BuildingBlockFactory buildingBlockFactory) throws ConfigurationException, ProofException;

  protected abstract void addCarryOverZkModule(final String username, final SignatureBuildingBlock bb,
		  final SystemParameters sp, final VerifierParameters vp, final PublicKey ip, final String identifierOfModule,
      final URI aliasOfSecretForCarryOver, final BigInt credSpecId, final URI issuerOnDevice,
      List<Boolean> setByIssuer) throws ProofException, ConfigurationException;

  protected abstract void addRevealAttributeZkModule(final RevealAttributeBuildingBlock bb,
		  final String attributeId, final BigInt value);

  protected abstract void addConstantZkModule(final ConstantBuildingBlock bb, final String name, final BigInt value);

  protected abstract void addEqualityZkModule(final AttributeEqualityBuildingBlock bb, final String lhs,
		  final String rhs, final boolean external);

  protected abstract void addInequalityZkModule(final RangeProofBuildingBlock bb, final String lhs, final String rhs,
		  final boolean strict, final SystemParameters sp, final VerifierParameters verifierParameters, final int counter) throws ConfigurationException;

  protected abstract void addCredentialSpecificationZkModule(
		  final CredentialSpecificationBuildingBlock bb, final String name, final SystemParameters sp,
      final CredentialSpecification credSpec, final BigIntFactory bigIntFactory);

  protected abstract void addSystemParametersZkModule(final SystemParametersBuildingBlock bb,
		  final String name, final SystemParameters sp);

  protected abstract void addVerifierParametersZkModule(final VerifierParametersBuildingBlock bb,
		  final String name, final SystemParameters sp, final VerifierParameters vp);

  protected abstract void addMechanismSpecificationZkModule(final MechanismSpecificationBuildingBlock bb,
		  final String name, final SystemParameters sp, final MechanismSpecification ms);

  protected abstract void addPresentationTokenZkModule(
		  final PresentationTokenDescriptionBuildingBlock bb, final String name,
		  final SystemParameters systemParameters, final PresentationTokenDescription ptd);

  protected abstract void addIssuerKeyZkModule(final IssuerPublicKeyBuildingBlock bb, final String name,
		  final SystemParameters sp, final PublicKey ip);

  protected abstract void addInspectorKeyZkModule(final InspectorPublicKeyBuildingBlock bb, final String name,
		  final SystemParameters sp, final PublicKey ip);

  protected abstract void addRevocationKeyZkModule(final RevocationAuthorityPublicKeyBuildingBlock bb,
		  final String name, final SystemParameters sp, final PublicKey ip);

  protected abstract void addInspectAttributeZkModule(final InspectorBuildingBlock bb_ins,
		  final String inspectionModuleId, final SystemParameters systemParameters2,
		  final VerifierParameters verifierParameters2, final PublicKey insKey, final URI parametersUid,
		  final String attributeId, final AttributeDescription attributeDescription, final byte[] label)
      throws ConfigurationException, ProofException;

  protected abstract void addMessageZkModule(final Abc4TrustMessageBuildingBlock bb, final String name,
		  final Message message);

  protected MechanismSpecificationWrapper getMs() {
    return ms;
  }

  protected SystemParameters getSp() {
    return systemParameters;
  }

  protected PresentationTokenDescription getPtd() {
    return ptd;
  }

  protected abstract void failedToLoadParameter(final URI parameter) throws ProofException;

}
