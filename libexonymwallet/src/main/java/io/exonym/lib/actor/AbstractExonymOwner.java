package io.exonym.lib.actor;

import com.ibm.zurich.idmix.abc4trust.facades.CredentialFacade;
import com.ibm.zurich.idmix.abc4trust.facades.PseudonymCryptoFacade;
import com.ibm.zurich.idmx.buildingBlock.factory.BuildingBlockFactory;
import com.ibm.zurich.idmx.buildingBlock.pseudonym.PseudonymBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.pseudonym.scopeExclusive.ScopeExclusivePseudonymBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.pseudonym.standard.StandardPseudonymBuildingBlock;
import com.ibm.zurich.idmx.device.ExternalSecretsManagerImpl;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.interfaces.cryptoEngine.CryptoEngineInspector;
import com.ibm.zurich.idmx.interfaces.cryptoEngine.CryptoEngineProver;
import com.ibm.zurich.idmx.interfaces.cryptoEngine.CryptoEngineRevocationAuthority;
import com.ibm.zurich.idmx.interfaces.cryptoEngine.CryptoEngineVerifier;
import com.ibm.zurich.idmx.interfaces.orchestration.KeyGenerationOrchestration;
import com.ibm.zurich.idmx.interfaces.orchestration.presentation.PresentationOrchestrationProver;
import com.ibm.zurich.idmx.interfaces.orchestration.presentation.PresentationOrchestrationVerifier;
import com.ibm.zurich.idmx.interfaces.proofEngine.ZkDirector;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.util.bigInt.BigIntFactoryImpl;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.cryptoEngine.user.CryptoEngineUser;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.returnTypes.IssuMsgOrCredDesc;
import eu.abc4trust.xml.*;
import io.exonym.lib.exceptions.*;
import io.exonym.lib.helpers.UIDHelper;
import io.exonym.lib.abc.util.UidType;
import io.exonym.lib.helpers.BuildPresentationTokenDescription;
import io.exonym.lib.helpers.CredentialWrapper;
import io.exonym.lib.api.AbstractIdContainer;
import io.exonym.lib.lite.SFTPLogonData;
import io.exonym.lib.pojo.AnonCredentialParameters;
import io.exonym.lib.pojo.KeyContainer;
import io.exonym.lib.pojo.MintedAnonCredential;
import io.exonym.lib.standard.ExtractObject;
import io.exonym.lib.standard.Form;
import io.exonym.lib.standard.PassStore;

import javax.crypto.Cipher;
import java.math.BigInteger;
import java.net.URI;
import java.security.SecureRandom;
import java.util.*;
import java.util.logging.Logger;

public abstract class AbstractExonymOwner extends AbstractBaseActor {
	
	private final static Logger logger = Logger.getLogger(AbstractExonymOwner.class.getName());
	protected ZkDirector director;
	protected PseudonymBuildingBlock pseudonymBB;
	protected ScopeExclusivePseudonymBuildingBlock scopeExclusivePseudonymBB;

	protected CryptoEngineUser cryptoEngineUser;

	// TODO It is perhaps worth making this private and managing the addition of credentials to the 
	// credential manager via this abstract class.  This will have the benefit of allowing proper
	// maintenance of ownedCredentials list.
	protected eu.abc4trust.abce.internal.user.credentialManager.CredentialManager credentialManagerUser;

	// Prover
	protected CryptoEngineProver cryptoEngineProver;
	protected PresentationOrchestrationProver presentationOrchestrationProver;
	
	// Verifier
	protected CryptoEngineVerifier cryptoEngineVerifier;
	protected PresentationOrchestrationVerifier presentationOrchestrationVerifier;
	protected KeyGenerationOrchestration keyGenerationOrchestration;
	
	// Further participants
	protected CryptoEngineRevocationAuthority cryptoEngineRevocationAuthority;
	protected CryptoEngineInspector cryptoEngineInspector;
	
	protected final ArrayList<URI> inspectorList = new ArrayList<>();
	
	protected final HashMap<URI, URI> specToCredentialUriMap = new HashMap<>();
	
	protected final ArrayList<OwnedCredential> ownedCredentials = new ArrayList<>();
	
	private BuildPresentationTokenDescription bptd = null;
	
	private boolean open = false;


	/**
	 * This must be extended as final in the package where it will be used. 
	 * 
	 * @param container
	 */
	protected AbstractExonymOwner(AbstractIdContainer container) {
		super(container);
		cryptoEngineUser = INJECTOR.provideCryptoEngineUser();
		credentialManagerUser = INJECTOR.providesCredentialManagerUser();

	    // Prover
	    keyGenerationOrchestration = INJECTOR.providesKeyGenerationOrchestration();
	    cryptoEngineProver = INJECTOR.providesCryptoEngineProver();
	    presentationOrchestrationProver = INJECTOR.providesPresentationOrchestrationProver();
	    cryptoEngineRevocationAuthority = INJECTOR.providesCryptoEngineRevocationAuthority();

	    // Verifier
	    cryptoEngineVerifier = INJECTOR.providesCryptoEngineVerifier();
	    presentationOrchestrationVerifier = INJECTOR.providesPresentationOrchestrationVerifier();

		director = INJECTOR.providesZkDirector();
	    
	    // Pseudonyms
		try {
			BuildingBlockFactory bbf = INJECTOR.provideBuildingBlockFactory();
			pseudonymBB = bbf.getBuildingBlockByClass(StandardPseudonymBuildingBlock.class);
			scopeExclusivePseudonymBB = bbf.getBuildingBlockByClass(ScopeExclusivePseudonymBuildingBlock.class);

		} catch (ConfigurationException e) {
			throw new RuntimeException(e);

		}


	}

//	protected boolean openResourceIfNotLoaded(URI uid, int tmp) throws Exception {
//		return openResourceIfNotLoaded(uid, false);
//	}

	protected boolean openResourceIfNotLoaded(URI uid) throws Exception {
		if (!super.openResourceIfNotLoaded(uid)) {
			if (UidType.isCredential(uid)) {
				loadCredentialIf(uid);
				
			} else {
				throw new Exception("UID not recognized at loadIf " + uid);
				
			}
		}
		return true;
	}
	
	private void loadCredentialIf(URI uid) throws Exception {
		if (credentialManagerUser.getCredential(container.getUsername(), uid)==null){
			Credential c = container.openResource(uid, null); // Null cipher
			addCredentialToIdmx(c, null);
			
		} else {
			logger.fine("The Credential was already on the key manager " + uid);
			
		}
	}

	protected synchronized void openContainer(PassStore store){
		openContainer(store.getDecipher(), store.getEncrypt());
		
	}

	protected synchronized void authenticate(PassStore store) throws Exception {
		this.container.openResource(this.container.getUsername() + ".ss.xml", store.getDecipher());

	}
	
	protected synchronized void openContainer(Cipher dec, Cipher enc){
		try {
			if (!open){
				initSystemParameters();
				int params = 0, creds = 0; 
				ArrayList<String> issuerParams = container.getIssuerParameterList();
				for (String resource: issuerParams){
					IssuerParameters ip = container.openResource(resource);
					this.addIssuerParameters(ip);
					logger.fine("Adding " + ip.getParametersUID());
					params++;
					
				}
				ArrayList<String> credentials = container.getOwnerSecretList();
				for (String resource: credentials){
					logger.fine(resource);;
					
				}
				for (String resource: credentials){
					Object credentialOrSecret = container.openResource(resource, dec);
					if (credentialOrSecret instanceof Credential){
						Credential c = (Credential)credentialOrSecret;
						this.addCredentialToIdmx(c, enc);
						logger.fine("Adding " + c.getCredentialDescription().getCredentialUID());
						creds++; 
						
					} else if (credentialOrSecret instanceof Secret){
						this.credentialManagerUser.storeSecret(container.getUsername(), (Secret)credentialOrSecret);
						logger.fine("Adding secret");

					} else if (credentialOrSecret instanceof AnonCredentialParameters){
						logger.fine("Anon Credential Params do not get used here.");
						
					} else if (credentialOrSecret instanceof MintedAnonCredential){
						logger.fine("Minted Anon Credential does not used here.");

					} else if (credentialOrSecret instanceof KeyContainer){
						logger.fine("Key Containers not used here.");

					} else if (credentialOrSecret instanceof SFTPLogonData){
						logger.fine("SFTPCredentials not used here.");

					} else {
						throw new RuntimeException("Unhandled object type " + credentialOrSecret);
						
					}
				}
				this.open=true; 
				logger.fine("Opened " + params +  " issuer parameters and " + creds + " credentials for container" + this.container.getUsername()); //*/
			
			}
		} catch (Exception e) {
			logger.throwing("AbstractExonymOwner.class", "openContainer()", e);
			
		}
	}
	
	protected synchronized void setupContainerSecret(Cipher enc, Cipher dec) throws Exception{
		try {
			try {
				container.openResource(this.container.getUsername() + ".ss.xml", dec);
						
			} catch (Exception e) {
				// Only create it, if one is not already present.
				BigIntFactoryImpl bif = new BigIntFactoryImpl();
				BigInt bi = bif.random(256, new SecureRandom());
				// RecoveryPhrase phrase = new RecoveryPhrase(bi.toByteArrayUnsigned());
				Secret secret = ExternalSecretsManagerImpl.generateSecret(this.keyManager, bi.getValue(), container.getSecretUri());
				container.saveLocalResource(secret, enc);
				this.credentialManagerUser.storeSecret(container.getUsername(), secret);

			}
		} catch (NullPointerException e) {
			throw new HubException("You must call openContainer first to load the system parameters", e);
			
		} catch (Exception e) {
			throw e;
			
		}
	}
	
	protected synchronized void addContainerSecret(Secret secret) throws UxException, Exception{
		if (container.getSecretUri()==null){
			this.credentialManagerUser.storeSecret(container.getUsername(), secret);
			
		} else {
			throw new UxException("There is already a base secret established for this container");
			
		}
	}
	
	protected synchronized void addCredentialToIdmx(Credential credential, Cipher enc) throws Exception{
		try {			
			try {
				new CredentialWrapper(credential, CredentialWrapper.ENCODE_ATTRIBUTES);
				
			} catch (Exception e) {
				logger.fine("This is supposed to happen if the encoding has been done before being sent to the function:" + e.getMessage());
				
			}
			CredentialFacade cf = new CredentialFacade(credential);
			CredentialDescription cd = credential.getCredentialDescription();
			URI specification = cd.getCredentialSpecificationUID();
			URI issuerUid = cd.getIssuerParametersUID();

			openResourceIfNotLoaded(specification);
			openResourceIfNotLoaded(issuerUid);

			IssuerParameters params = this.keyManager.getIssuerParameters(issuerUid);
			URI raUid = params.getRevocationParametersUID();
			addOwned(credential);

			// Unsure if this is correct.  It sometimes fails to load RAI.
			if (credentialManagerUser.getCredential(container.getUsername(), 
						credential.getCredentialDescription().getCredentialUID())==null){

				credentialManagerUser.storeCredential(container.getUsername(), cf.getDelegateeValue());
				

				if (raUid!=null){
					openResourceIfNotLoaded(raUid);
					RevocationAuthorityParameters rap = keyManager.getRevocationAuthorityParameters(raUid); 
					
					PublicKey pk = ExtractObject.extract(rap.getCryptoParams().getContent(), PublicKey.class);
					URI riUid = URI.create(raUid + "i");
					URI publicKeyUid = pk.getPublicKeyId();
					
					openResourceIfNotLoaded(riUid);
					RevocationInformation ri = this.keyManager.getRevocationInformation(raUid, riUid);
					this.keyManager.storeRevocationInformation(publicKeyUid, ri);
					
					Credential updated = cryptoEngineProver.updateNonRevocationEvidence(container.getUsername(), credential, null, false);
					this.container.saveLocalResource(updated, true, enc);
					
				}

			}
			specToCredentialUriMap.put(credential.getCredentialDescription().getCredentialSpecificationUID(), 
										credential.getCredentialDescription().getCredentialUID());
			
		} catch (Exception e) {
			throw e; 
			
		}
	}	

	/**
	 * Called in each step of an issuance process that 
	 * will result in obtaining a credential. <p>
	 * 
	 * If further information must be sent to the issuer, 
	 * it produces an IssuMsgOrCredDesc object.
	 * Otherwise it will return null 
	 * and store the credential in the IdmxContainer.
	 * 
	 * @return
	 * @throws Exception
	 */
	protected synchronized IssuanceMessage issuanceStep(IssuanceMessageAndBoolean imab, Cipher enc) throws Exception {
		try {
			IssuMsgOrCredDesc result = null;
			if (imab.getIssuanceMessage()!=null){
				IssuanceMessage im = imab.getIssuanceMessage();				
				IssuancePolicy policy = ExtractObject.extract(im.getContent(), IssuancePolicy.class);
				
				if (policy!=null){
					result = fulfillPolicy(im, policy);
					
				} else {	// Continue already initialized issuance
					result = simpleIssuance(im);
					
				}
				if (imab.isLastMessage()){
					storeCredential(result.cd, enc);
					return null;

				} 
				return result.im;
				
			} else { 
				throw new RuntimeException("Bad message. " + imab);
				
			}
		} catch (Exception e) {
			throw e;
			
		}
	}

	private IssuMsgOrCredDesc simpleIssuance(IssuanceMessage im) throws Exception {
		CredentialTemplate ct = ExtractObject.extract(im.getContent(), CredentialTemplate.class);
		if (ct!=null){
			openResourceIfNotLoaded(ct.getCredentialSpecUID());
			openResourceIfNotLoaded(ct.getIssuerParametersUID());

		}
		return cryptoEngineUser.issuanceProtocolStep(container.getUsername(), im);

	}

	private IssuMsgOrCredDesc fulfillPolicy(IssuanceMessage im, IssuancePolicy policy) throws Exception {
		CredentialTemplate ct = policy.getCredentialTemplate();
		
		openResourceIfNotLoaded(ct.getCredentialSpecUID());
		openResourceIfNotLoaded(ct.getIssuerParametersUID());
		
		IssuerParameters issuerParams = this.keyManager.getIssuerParameters(ct.getIssuerParametersUID());
		CredentialSpecification credSpec = this.keyManager.getCredentialSpecification(ct.getCredentialSpecUID());

		// Load any RA Params
		if (issuerParams.getRevocationParametersUID()!=null){
			openResourceIfNotLoaded(issuerParams.getRevocationParametersUID());
			
		}
		PresentationPolicy presentationPolicy = policy.getPresentationPolicy();
		if (presentationPolicy.getPseudonym().isEmpty() &&  
				presentationPolicy.getPseudonym().isEmpty() &&  
				presentationPolicy.getCredential().isEmpty()) {
			
			logger.fine("Running simple issuance");
			return cryptoEngineUser.issuanceProtocolStep(container.getUsername(), im);	
			
		} else {
			return complexIssuance(policy, im, credSpec, issuerParams);
			
		}
	}

	/*
	 * TODO: this is where we respond to the IssuancePolicy
	 */
	private IssuMsgOrCredDesc complexIssuance(IssuancePolicy policy, IssuanceMessage im,
											  CredentialSpecification credSpec, IssuerParameters issuerParams) throws Exception {
		logger.fine("Running complex issuance");
		try {
			ArrayList<URI> creduids = new ArrayList<>();
			ArrayList<URI> pseudonyms = new ArrayList<>();
			ArrayList<Attribute> atts = new ArrayList<>();

			CredentialTemplate ct = policy.getCredentialTemplate();
			IssuMsgOrCredDesc result = new IssuMsgOrCredDesc();

			PresentationPolicy pp = policy.getPresentationPolicy();
			BuildPresentationTokenDescription bptd = new BuildPresentationTokenDescription(
					pp, this.externalResource);
			bptd.setOwnedCredentials(ownedCredentials);

			PresentationTokenDescription ptd = of.createPresentationTokenDescription();
			ptd.setPolicyUID(policy.getCredentialTemplate().getIssuerParametersUID());
			ptd.setMessage(pp.getMessage());
			ptd.setPolicyUID(pp.getPolicyUID());
			ptd.getAttributePredicate().addAll(pp.getAttributePredicate());

			ptd.getCredential().addAll(resolveCredentialsInPolicyToToken(pp.getCredential(), creduids));

			ptd.getPseudonym().addAll(resolvePseudonymInPolicyToToken(pp.getPseudonym(), pseudonyms));

			// TODO attribute predicates.

			IssuanceTokenDescription itd = of.createIssuanceTokenDescription();
			itd.setCredentialTemplate(ct);
			itd.setPresentationTokenDescription(ptd);

			result.im = cryptoEngineUser.createIssuanceToken(container.getUsername(), im, itd, creduids, pseudonyms, atts);

			return result;

		} catch (Exception e) {
			throw e;

		}
	}

	private List<CredentialInToken> resolveCredentialsInPolicyToToken(List<CredentialInPolicy> credentials, ArrayList<URI> creduids) throws Exception {
		ArrayList<CredentialInToken> result = new ArrayList<>();
		for (CredentialInPolicy c : credentials){
			CredentialInToken cit = of.createCredentialInToken();

			cit.setAlias(c.getAlias());
			cit.setSameKeyBindingAs(c.getSameKeyBindingAs());
			Credential credential = credentialSelector(c.getCredentialSpecAlternatives().getCredentialSpecUID());
			creduids.add(credential.getCredentialDescription().getCredentialUID());
			cit.setCredentialSpecUID(credential.getCredentialDescription().getCredentialSpecificationUID()); // TODO  Get from selecting one of the available specs
			UIDHelper helper = new UIDHelper(credential.getCredentialDescription().getIssuerParametersUID());
			cit.setIssuerParametersUID(helper.getIssuerParameters()); // TODO Get from the Credential Selected in the previous step
			cit.setRevocationInformationUID(helper.getRevocationInfoParams());

			logger.warning("DISCLOSED ATTRIBUTES NOT IMPLEMENTED.");
			result.add(cit);

		}
		return result;

	}

	private Credential credentialSelector(List<URI> possibilities) throws UxException, CredentialManagerException {
		List<URI> available = new ArrayList<>();
		for (URI possibility : possibilities){
			if (this.specToCredentialUriMap.get(possibility)!=null){
				available.add(this.specToCredentialUriMap.get(possibility));
				
			}
		}
		if (available.isEmpty()){
			throw new UxException("No credentials meet the requirements");
			
		}
		if (available.size()==1){
			return credentialManagerUser.getCredential(this.container.getUsername(), available.get(0));			
			
		} else {
			throw new RuntimeException("Choices of the same credential not yet implemented");
			
		}
	}

	private List<PseudonymInToken> resolvePseudonymInPolicyToToken(List<PseudonymInPolicy> pseudonyms, ArrayList<URI> nyms) throws ConfigurationException, KeyManagerException, CredentialManagerException, UxException {
		ArrayList<PseudonymInToken> result = new ArrayList<>();

		for (PseudonymInPolicy nym : pseudonyms){
			PseudonymInToken pit = of.createPseudonymInToken();
			PseudonymWithMetadata ap = generatePseudonym(URI.create(nym.getScope()), nym.isExclusive());
			
			pit.setAlias(nym.getAlias());
			pit.setExclusive(nym.isExclusive());
			pit.setPseudonymValue(ap.getPseudonym().getPseudonymValue());
			pit.setSameKeyBindingAs(nym.getSameKeyBindingAs());
			pit.setScope(nym.getScope());
			result.add(pit);
			nyms.add(ap.getPseudonym().getPseudonymUID());

		}
		return result;
		
	}
	
	// TODO make automatic after successful completion of the process.
	private void storeCredential(CredentialDescription cd, Cipher enc) throws Exception {
		if (cd==null){
			throw new Exception("Credential Description is null");
			
		}
		Credential cred = credentialManagerUser.getCredential(container.getUsername(), cd.getCredentialUID());
		this.addCredentialToIdmx(cred, enc);
		addOwned(cred);

		// URI credUri = URI.create(cred.getCredentialDescription().getIssuerParametersUID().toString()+"c"); 
		new CredentialWrapper(cred, CredentialWrapper.DECODE_ATTRIBUTES);
		container.saveLocalResource(cred, enc);
		new CredentialWrapper(cred, CredentialWrapper.ENCODE_ATTRIBUTES);
		
	}

	private void addOwned(Credential cred) {
		OwnedCredential oc = new OwnedCredential(cred.getCredentialDescription().getCredentialSpecificationUID(),
				cred.getCredentialDescription().getCredentialUID(), cred.getCredentialDescription().getIssuerParametersUID());
		ownedCredentials.add(oc);

	}

	/**
	 * See proveClaim();
	 *  
	 */
	protected PresentationTokenDescription canProveClaimFromPolicy(PresentationPolicy pp) throws Exception{
		bptd = new BuildPresentationTokenDescription(pp, this.externalResource);
		return evaluatePresentationTokenDescription(bptd);
		
	}
	
	/**
	 * See proveClaim();
	 *  
	 */
	protected PresentationTokenDescription canProveClaimFromPolicy(PresentationPolicyAlternatives pp) throws Exception{
		bptd = new BuildPresentationTokenDescription(pp, this.externalResource);
		return evaluatePresentationTokenDescription(bptd);
		
	}
	
	private PresentationTokenDescription evaluatePresentationTokenDescription(BuildPresentationTokenDescription bptd) throws Exception {
		try {
			bptd.setOwnedCredentials(ownedCredentials);
			if (bptd.isMultiPolicyOrCredential()){
				return null;
				
			} else {
				return bptd.getPresentationTokenDescription();
				
			}
		} catch (UxException e) {
			throw e; 
			
		} catch (Exception e) {
			throw e; 
			
		}
	}
	
	/**
	 * See proveClaim();
	 *  
	 */
	protected HashMap<URI, HashSet<CandidateToken>> chooseCredentialOptions(){
		if (bptd!=null){
			return this.bptd.getPolicyToCandidateMap();
			
		} else {
			throw new RuntimeException("Call canCreateProofFromPolicy() first.");
			
		}
	}
	
	/**
	 * See proveClaim();
	 *  
	 */
	protected PresentationTokenDescription enterChoice(HashSet<CandidateToken> credentials) throws UxException{
		if (bptd!=null){
			try {
				logger.warning("No inspector selection implemented");
				return this.bptd.selectTokens(credentials, null);
				
			} catch (UxException e) {
				throw e;

			} catch (Exception e) {
				logger.throwing("AbstractExonymOwner.class", "enterChoice()", e);
				throw new UxException("Check connection.");
				
			}
		} else {
			throw new RuntimeException("Select canProceed() first.");
			
		}
	}
	
	/**
	 * One objective of the Owner is prove a claim imposed on them from a third party
	 * in a PresentationPolicy, or PresentationPolicyAlternatives document.<p>
	 * 
	 *  To do this, the Owner generates a PresentationToken.  <p>
	 *  
	 *  It is possible that an Owner can fulfil many of the PresentationPolicyAlternatives
	 *  that are acceptable to the Verifier, who wishes to verify the claim. They can therefore
	 *  choose which credential from which issuer they want to use.<p>
	 *  
	 *  The procedure on receipt of a PresentationPolicy or Alternatives is to call 
	 *  canProveClaimFromPolicy(); If this returns null with exception, call chooseCredentialOptions()
	 *  to return a set of all possible options.<p>
	 *  
	 *  Allow the Owner to make the necessary choice via the enterChoice() method which will return 
	 *  the required PresentationTokenDescription for this function.
	 *  
	 * @param token
	 * @param ppa
	 * @return
	 * @throws Exception
	 */
	protected PresentationToken proveClaim(PresentationTokenDescription token, 
			PresentationPolicyAlternatives ppa) throws Exception {
		
		ArrayList<URI> pseudonymUris = resolvePseudonyms((ArrayList<PseudonymInToken>) 
				token.getPseudonym());
		
		ArrayList<URI> credentialUris = resolveCredentials(
				(ArrayList<CredentialInToken>) token.getCredential());
		
		resolveInspectorParams(token.getCredential());

		try {
			return cryptoEngineProver.createPresentationToken(container.getUsername(),
					token, ppa.getVerifierParameters(), credentialUris, pseudonymUris);

		} catch (RuntimeException e) {
			throw new UxException(ErrorMessages.REVOKED, e);

		}
	}

	public ArrayList<URI> listAllMods(PresentationTokenDescription token) throws Exception {
		List<CredentialInToken> tokens = token.getCredential();
		ArrayList<URI> mods = new ArrayList<>();
		for (CredentialInToken creds : tokens){
			mods.add(UIDHelper.computeModUidFromMaterialUID(creds.getIssuerParametersUID()));

		}
		return mods;

	}

	private ArrayList<URI> resolvePseudonyms(ArrayList<PseudonymInToken> nyms)
			throws ConfigurationException, KeyManagerException, CredentialManagerException, UxException {
		
		ArrayList<URI> result = new ArrayList<>();
		for (PseudonymInToken nym : nyms){
			URI scopeAndUid = URI.create(nym.getScope());
			PseudonymWithMetadata absNym = generatePseudonym(scopeAndUid, nym.isExclusive());
			nym.setPseudonymValue(absNym.getPseudonym().getPseudonymValue());
			result.add(scopeAndUid);
			
		}
		return result;
		
	}
	
	private ArrayList<URI> resolveCredentials(ArrayList<CredentialInToken> credentials) throws Exception {
		ArrayList<URI> result = new ArrayList<>(); 
		for (CredentialInToken credential : credentials){
			try {
				URI c = this.specToCredentialUriMap.get(credential.getCredentialSpecUID());
				result.add(c);
				
			} catch (Exception e) {
				throw new UxException("Cannot find credential specification for " + credential);
				
			}
		}
		return result;
		
	}
	
	private void resolveInspectorParams(List<CredentialInToken> credential) throws Exception {
		for (CredentialInToken cit : credential) {
			for (AttributeInToken ait : cit.getDisclosedAttribute()) {
				logger.fine("Inspector UID=" + ait.getInspectorPublicKeyUID());
				openResourceIfNotLoaded(ait.getInspectorPublicKeyUID());
				
			}
		}
	}

	protected PseudonymWithMetadata generatePseudonym(URI scope, boolean exclusive) throws ConfigurationException, KeyManagerException, CredentialManagerException, UxException {
		AbstractPseudonym nym = null;
		try {
			if (exclusive){
				nym = (ScopeExclusivePseudonym) scopeExclusivePseudonymBB.createPseudonym(getSystemParameters(),
						null, container.getSecretUri(), container.getUsername(), scope);

			} else {
				nym = (StandardPseudonym) pseudonymBB.createPseudonym(getSystemParameters(),
						null, container.getSecretUri(), container.getUsername(), scope);

			}
		} catch (NullPointerException e) {
			throw new UxException("It is possible you haven't opened your container or setup your container secret.");

		}
		Pseudonym nym0 = of.createPseudonym();
		nym0.setExclusive(exclusive);
		nym0.setPseudonymUID(scope);
		nym0.setPseudonymValue(nym.getValue().toByteArray());
		nym0.setScope(nym.getScope().toString());
		nym0.setSecretReference(container.getSecretUri());
		PseudonymWithMetadata result = of.createPseudonymWithMetadata();
		PseudonymMetadata pm = of.createPseudonymMetadata();
		PseudonymCryptoFacade pcf = new PseudonymCryptoFacade();
		pcf.setAbstractPseudonym(nym);
		result.setCryptoParams(pcf.getCryptoParams());
		result.setPseudonym(nym0);
		result.setPseudonymMetadata(pm);
		
		pm.setHumanReadableData("Display Name");
		this.credentialManagerUser.storePseudonym(container.getUsername(), result);
		return result;
		
	}
	
	/**
	 * This function returns true if the proof is valid and throws an exception if it is invalid.
	 * 
	 * @param ppa
	 * @param token
	 * @return
	 * @throws Exception
	 */
	protected boolean verifyClaim(PresentationPolicyAlternatives ppa, PresentationToken token) throws Exception {
		checkPolicySatisfied(ppa, token);
		
		List<CredentialInToken> credentialUids = token.getPresentationTokenDescription().getCredential();
		for (CredentialInToken cit : credentialUids){
			try {
				openResourceIfNotLoaded(cit.getCredentialSpecUID());
				openResourceIfNotLoaded(cit.getIssuerParametersUID());
				for (AttributeInToken ait : cit.getDisclosedAttribute()) {
					openResourceIfNotLoaded(ait.getInspectorPublicKeyUID());
					
				}
				IssuerParameters ip = this.keyManager.getIssuerParameters(cit.getIssuerParametersUID());
				URI ra = this.addIssuerParameters(ip);
				
				if (ra!=null){
					URI rai = URI.create(ra + "i");
					openResourceIfNotLoaded(ra);
					openResourceIfNotLoaded(rai);
					
				}
			} catch (Exception e) {
				logger.fine("Expected 'could not find' message " + e.getMessage());
				// Do nothing
			}
		}
		boolean result = cryptoEngineVerifier.verifyToken(token, ppa.getVerifierParameters());
		if (result){
			return result;
			
		} else {
			throw new PolicyNotSatisfiedException();
			
		} 
	}

	protected void checkPolicySatisfied(PresentationPolicyAlternatives ppa, PresentationToken token) throws Exception {
		try {
			RequestFulfilled.presentationPolicySatisfied(ppa, token);

		} catch (PolicyNotSatisfiedException e) {
			logger.severe("Message:" + e.getMsg());
			pseudonymsOut(e.getPseudonymErrors());
			missingAttributes(e.getMissingAttributePredicates());
			missingCredentials(e.getUnsatisfiedCredentials());
			throw e;

		} catch (Exception e) {
			throw e;

		}
	}

	private void pseudonymsOut(List<PseudonymException> pseudonymErrors) {
		logger.severe("Unsatisfactory Pseudonyms");

		for (PseudonymException nym : pseudonymErrors){
			logger.severe("Message From Error: " + nym.getMessage());
			logger.severe("Pseudonyms Required: " + nymToString(nym.getNymInPolicy()));
			logger.severe("Pseudonyms Provided:" + nymToString(nym.getNymInToken()));

		}
	}

	protected String nymToString(PseudonymInPolicy nym) {
		return "\n\tscope:\t" + nym.getScope() + "\n\talias\t"
				+ nym.getAlias() + "\n\tbinding\t" + nym.getSameKeyBindingAs();

	}

	protected String nymToString(PseudonymInToken nym) {
		return "\n\tscope:\t" + nym.getScope() + "\n\talias\t"
				+ nym.getAlias() + "\n\tbinding\t" + nym.getSameKeyBindingAs();

	}

	private void missingAttributes(List<AttributePredicateException> missingAttributePredicates) throws Exception {
		logger.severe("Missing Attributes");
		for (AttributePredicateException att : missingAttributePredicates){
			throw new Exception("Not implemented " + att.getToken().getFunction());

		}
	}

	private void missingCredentials(List<CredentialInTokenException> unsatisfiedCredentials) {
		logger.severe("Missing Credentials");
		for (CredentialInTokenException cred : unsatisfiedCredentials){
			logger.severe(cred.getMessage());
			CredentialInPolicy cip = cred.getCip();
			logger.severe("alias=" + cip.getAlias());
			acceptableIssuers(cip.getIssuerAlternatives());

		}
	}

	protected void acceptableIssuers(CredentialInPolicy.IssuerAlternatives issuerAlternatives) {
		for (CredentialInPolicy.IssuerAlternatives.IssuerParametersUID ip :
				issuerAlternatives.getIssuerParametersUID()){
			logger.severe(ip.getValue() + " " + ip.getRevocationInformationUID());

		}
	}

	public static byte[] toUnsignedByteArray(BigInteger bigInt) throws Exception {
		if (bigInt==null){
			throw new NullPointerException();

		}
		return INJECTOR.provideBigIntFactory().valueOf(bigInt).toByteArrayUnsigned();

	}

	public static byte[] toUsablePassStoreInitByteArray(String passAsSha256Hex) throws UxException {
		return INJECTOR.provideBigIntFactory()
				.unsignedValueOf(Form.toBigInteger(passAsSha256Hex).toByteArray())
				.toByteArrayUnsigned();

		// return .unsignedValueOf(in).toByteArrayUnsigned();
	}
}
