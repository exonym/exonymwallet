package io.exonym.lib.actor;

import com.ibm.zurich.idmx.buildingBlock.signature.cl.ClSignatureBuildingBlock;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.interfaces.proofEngine.ZkDirector;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.RandomGeneration;
import com.ibm.zurich.idmx.interfaces.util.group.GroupFactory;
import com.ibm.zurich.idmx.jaxb.JaxbHelperClass;
import com.ibm.zurich.idmx.parameters.system.SystemParametersWrapper;
import eu.abc4trust.cryptoEngine.verifier.CryptoEngineVerifier;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.xml.*;
import io.exonym.lib.helpers.NamespaceMngt;
import io.exonym.lib.abc.util.UidType;
import io.exonym.idmx.dagger.DaggerExonymComponent;
import io.exonym.idmx.dagger.ExonymComponent;
import io.exonym.idmx.managers.KeyManagerExonym;
import io.exonym.lib.helpers.UrlHelper;
import io.exonym.lib.exceptions.ErrorMessages;
import io.exonym.lib.exceptions.UxException;
import io.exonym.lib.api.AbstractXContainer;
import io.exonym.lib.pojo.ExternalResourceContainer;
import io.exonym.lib.pojo.Namespace;
import io.exonym.lib.pojo.XContainer;

import java.io.BufferedInputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.logging.Logger;

public abstract class AbstractBaseActor {


	private final static Logger logger = Logger.getLogger(AbstractBaseActor.class.getName());
	protected final BigIntFactory bigIntFactory;
	protected final GroupFactory groupFactory;
	protected final RandomGeneration randomGeneration;
	
	protected final KeyManager keyManager;
	protected final ClSignatureBuildingBlock clBuildingBlock;
	protected final ZkDirector zkDirector;
	
	protected CryptoEngineVerifier cryptoEngineVerifier;
	
	protected static ObjectFactory of = new ObjectFactory();
	protected VerifierParameters verifierParams = null;

	protected final ArrayList<URI> credentialSpecificationList = new ArrayList<>();
	protected final ArrayList<URI> issuerList = new ArrayList<>();
	protected final AbstractXContainer container;
	protected ExternalResourceContainer externalResource = null;
	protected static ExonymComponent INJECTOR = DaggerExonymComponent.create();
	protected AbstractBaseActor(AbstractXContainer container) {

		this.container=container;
		// Utils
		bigIntFactory = INJECTOR.provideBigIntFactory();
		groupFactory = INJECTOR.provideGroupFactory();
		randomGeneration = INJECTOR.provideRandomGeneration();
		// General
		keyManager = INJECTOR.providesKeyManager();
		zkDirector = INJECTOR.providesZkDirector();
		cryptoEngineVerifier = INJECTOR.providesCryptoEngineVerifierAbc();
		externalResource = initialzeExternalResourceContainer();
		ClSignatureBuildingBlock cl = null;
		try {
			cl = INJECTOR.provideBuildingBlockFactory().getBuildingBlockByClass(ClSignatureBuildingBlock.class);

		} catch (ConfigurationException e) {
			logger.info("Catastrophic failure - failed to load the CL building block");
			logger.throwing("AbstractBaseActor.class", "AbstractBaseActor()", e);


		}
		clBuildingBlock = cl;
		logger.fine("ExonymActor containerName=" + container.getUsername() + " keyManager=" + this.keyManager);

	}

	public void clearStale() throws Exception {
		if (this.keyManager instanceof KeyManagerExonym){
			KeyManagerExonym k = (KeyManagerExonym)this.keyManager;
			k.clearStale();
			logger.info("Cleared Revocation Information");

		} else {
			throw new Exception("The key manager was not an acceptable class " + this.keyManager);

		}
	}
	
	protected boolean openResourceIfNotLoaded(URI uid) throws Exception {
		if (uid!=null){

			if (UidType.isCredentialSpecification(uid)) {
				loadCredentialSpecificationIf(uid);
				return true;

			} else if (UidType.isRevocationAuthority(uid)) {
				loadRevocationAuthorityIf(uid);
				return true;

			} else if (UidType.isRevocationInformation(uid)) {
				loadRevocationInformationIf(uid);
				return true;

			} else if (UidType.isInspectorPublicKey(uid)) {
				loadInspectorParametersIf(uid);
				return true;

			} else if (UidType.isIssuancePolicy(uid)) {
				throw new Exception();

			} else if (UidType.isIssuerParameters(uid)) {
				loadIssuerParametersIf(uid);
				return true;

			} else {
				logger.warning("No UID found in Actor superclass " + uid + " searching lower levels");
				return false;
			}
		} else {
			throw new NullPointerException();

		}
	}

	protected <T> T publicParameterOpener(URI uid) throws Exception {
		// Checking local container -- if fail -->
		try {
			return this.container.openResource(uid);
			
		} catch (Exception e) {
			logger.info("Failed to find parameters in local container " + uid);
			
		}
		String fn = XContainer.uidToXmlFileName(uid);
		// TODO - Check local resources -- if fail -->
		// Checking external resource 
		try {
			return this.externalResource.openResource(fn);
			
		} catch (Exception e) {
			logger.info("Failed to find parameters on network " + uid);
			throw e; 
			
		}
	}
	
	private void loadIssuerParametersIf(URI uid) throws Exception {
		if (keyManager.getIssuerParameters(uid)==null){
			IssuerParameters ip = publicParameterOpener(uid);
			addIssuerParameters(ip);
			
		}
	}

	private void loadCredentialSpecificationIf(URI uid) throws KeyManagerException, Exception {
		if (keyManager.getCredentialSpecification(uid)==null){
			CredentialSpecification credentialSpecification = publicParameterOpener(uid);
			keyManager.storeCredentialSpecification(uid, credentialSpecification);
			this.credentialSpecificationList.add(uid);
			logger.info("Add Credential Spec - " + uid + " " + this.keyManager);
			
		} else {
			logger.fine("The Credential Spec was already on the key manager " + uid);
			
		}
	}
	
	private void loadRevocationAuthorityIf(URI uid) throws KeyManagerException, Exception {
		if (keyManager.getRevocationAuthorityParameters(uid)==null){
			RevocationAuthorityParameters rap = publicParameterOpener(uid);
			keyManager.storeRevocationAuthorityParameters(uid, rap);
			logger.info("Added Revocation Authority Parameters " + uid + " to " + this.keyManager);
			
		} else {
			logger.fine("The Revocation Authority Parameters were already on the key manager " + uid);
			
		}
	}
	
	private void loadRevocationInformationIf(URI uid) throws Exception {
		URI raUid = URI.create(Namespace.URN_PREFIX_COLON + XContainer.stripUidSuffix(uid, 1) + "a");
		if (this.keyManager.getRevocationInformation(raUid, uid)==null){
			openResourceIfNotLoaded(raUid);
			RevocationAuthorityParameters rap = this.keyManager.getRevocationAuthorityParameters(raUid);
			RevocationInformation ri = publicParameterOpener(uid);
			this.addRevocationInformation(rap.getParametersUID(), ri);
			
		}
	}
	
	private void loadInspectorParametersIf(URI uid) throws Exception {
		if (keyManager.getInspectorPublicKey(uid)==null) {
			InspectorPublicKey k = publicParameterOpener(uid);
			this.keyManager.storeInspectorPublicKey(uid, k);
			logger.info("Added Inspector Parameters " + uid + " to " + this.keyManager);
			
		}
	}

	protected void addInspectorParameters(InspectorPublicKey ins) throws Exception {
		this.keyManager.storeInspectorPublicKey(ins.getPublicKeyUID(), ins);

	}

	/**
	 * All containers require knowledge of Global System Parameters.  
	 * 
	 * The term global applies to IDMX systems outside of Existence. 
	 * 
	 * The principle of Universal Composibility allows for cryptosystems that
	 * share the same parameters to facilitate cross-compatibility.
	 * 
	 * TODO Confirm System Parameters with the distributed ledger.
	 * 
	 * @return
	 * @throws Exception
	 */
	public SystemParametersWrapper initSystemParameters() throws Exception {
	  	return initSystemParameters(NamespaceMngt.DEFAULT_SYSTEM_PARAMETERS_FILENAME);
	    
	}
	
	protected SystemParametersWrapper initSystemParameters(String spFilename) throws Exception{
		try {
			if (keyManager.getSystemParameters()==null){
				try {
//					byte[] l0;
//					try {
//						l0 = UrlHelper.read(UrlHelper.LAMBDA_LOCATION.toURL());
//
//					} catch (IOException e) {
//						logger.warn("Primary lambda.xml file not found - checking failover");
//						l0 = UrlHelper.read(UrlHelper.LAMBDA_FAILOVER_LOCATION.toURL());
//
//					}
//					String systemParameters = new String(l0);

					SystemParameters params = XContainerExternal.openSystemParameters();
					SystemParametersWrapper systemParametersFacade = new SystemParametersWrapper(params);
					
					// Load the parameters to the key manager
					keyManager.storeSystemParameters(systemParametersFacade.getSystemParameters());
					if (systemParametersFacade.getSystemParameters()!=null){
						logger.fine("Initialized System Parameters");
						
					}
				} catch (Exception e) {
					throw new UxException(ErrorMessages.FILE_NOT_FOUND + ":"
							+ UrlHelper.LAMBDA_LOCATION + " or "
							+ UrlHelper.LAMBDA_FAILOVER_LOCATION, e);
					
				}
			}
			return new SystemParametersWrapper(keyManager.getSystemParameters());
			
		} catch (Exception e) {
			throw e; 		
		
		}
	}
	
	/**
	 * Verifier Parameters are often required for PresentationPolicy 
	 * and PresentationPolicyAlternatives.  This function provides all
	 * ExistenceActors access to these VerifierParameters
	 *  
	 * @return
	 * @throws Exception
	 */
	public VerifierParameters getVerifierParameters() throws Exception{
		if (this.verifierParams!=null){
			return verifierParams;
			
		} else {
			return loadVerifierParams();
		}
	}

	private VerifierParameters loadVerifierParams() throws Exception {
		try (BufferedInputStream bis =
					 new BufferedInputStream(
							 ClassLoader.getSystemResourceAsStream("issuer-for-verifier-params.i.xml"))){
			IssuerParameters ip = (IssuerParameters) JaxbHelperClass.deserialize(bis, false).getValue();
			this.addIssuerParameters(ip);
			if (verifierParams==null){
				this.verifierParams = this.cryptoEngineVerifier.createVerifierParameters(getSystemParameters());

			}
			return this.verifierParams;

		} catch (Exception e) {
			throw e;

		}
	}

	public SystemParameters getSystemParameters() throws KeyManagerException{
		return this.keyManager.getSystemParameters();
		
	}

	protected void addCredentialSpecification(CredentialSpecification credentialSpecification){
		try {
			if (credentialSpecification==null){
				throw new NullPointerException();

			}
			URI uid = credentialSpecification.getSpecificationUID();
			if (keyManager.getCredentialSpecification(uid)==null){
				keyManager.storeCredentialSpecification(uid, credentialSpecification);
				this.credentialSpecificationList.add(uid);
				logger.info("Add Credential Spec - " + uid + " " + this.keyManager);
				
			}
		} catch (Exception e) {
			logger.throwing("AbstractBaseActor.class", "addCredentialSpecification()", e);
			
		}
	}
	
	/**
	 * Adds Issuer Parameters to this object.
	 * @param issuerParams
	 * @return the RevocationAuthorityUid associated with 
	 * this Issuer if there is one else returns null.
	 * @throws Exception 
	 */
	protected URI addIssuerParameters(IssuerParameters issuerParams) throws Exception{
		try {
			if (issuerParams==null){
				throw new NullPointerException();

			}
			URI issuerUid = issuerParams.getParametersUID();
			URI raUid = issuerParams.getRevocationParametersUID();
			if (raUid!=null){
				logger.fine("There already existed rap=" + raUid + "");
				
			}
			if (keyManager.getIssuerParameters(issuerUid)==null){
			    keyManager.storeIssuerParameters(issuerUid, issuerParams);
			    issuerList.add(issuerUid);
			    logger.info("Add Issuer Parameters - " + issuerUid + " " + this.keyManager);
				
			}
			return raUid;
			
		} catch (Exception e) {
			throw e; 
			
		}
	}
	
	protected AbstractXContainer getContainer() {
		return container;
	}

	protected void addRevocationAuthorityParameters(RevocationAuthorityParameters rap) throws KeyManagerException{
		if (rap==null){
			throw new NullPointerException();

		}
		URI uid = rap.getParametersUID();
		if (keyManager.getRevocationAuthorityParameters(uid)==null){
			keyManager.storeRevocationAuthorityParameters(uid, rap);
			logger.info("Added Revocation Authority Parameters " + uid + " to " + this.keyManager);
			
		}
	} 
	
	protected void addRevocationInformation(URI rapUid, RevocationInformation ri) throws Exception {
		if (ri==null || rapUid ==null){
			throw new Exception("ri=" + ri + " pkuid=" + null);

		}
		/*
		 * The duplication is a result of IDMX.  Under normal circumstances it is
		 * fine to search for the revocation information using revocationAuthorityUid [sic]  
		 * i.e. not the dedicated information uid.
		 * 
		 * Without the additional publickey item in there as a reference, the Owner will fail 
		 * to update their witness.  Without the raUid the credential will fail to issue.
		 * 
		 */
		if (this.keyManager.getRevocationInformation(ri.getRevocationAuthorityParametersUID(),
				ri.getRevocationInformationUID())==null){
			keyManager.storeRevocationInformation(rapUid, ri);
			
			if (!rapUid.toString().endsWith(":ra")){
				keyManager.storeRevocationInformation(ri.getRevocationAuthorityParametersUID(), ri);	
				
			}
			logger.info("Added revocation information " + ri.getRevocationInformationUID());

		}
	}	
	
	protected abstract ExternalResourceContainer initialzeExternalResourceContainer();
	
}