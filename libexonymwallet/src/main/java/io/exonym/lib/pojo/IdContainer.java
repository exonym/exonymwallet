package io.exonym.lib.pojo;

import com.ibm.zurich.idmx.exception.SerializationException;
import com.ibm.zurich.idmx.jaxb.JaxbHelperClass;
import eu.abc4trust.xml.*;
import io.exonym.lib.helpers.NamespaceMngt;
import io.exonym.lib.abc.util.FileType;
import io.exonym.lib.abc.util.JaxbHelper;
import io.exonym.lib.api.AbstractIdContainer;
import io.exonym.lib.adapters.PresentationPolicyAlternativesAdapter;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBIntrospector;
import java.io.*;
import java.net.URI;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystemException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.logging.Logger;

public final class IdContainer extends AbstractIdContainer {


	private final static Logger logger = Logger.getLogger(IdContainer.class.getName());
	private final ArrayList<String> actionParamsList = new ArrayList<>();
	private final ArrayList<String> demParmasList = new ArrayList<>();
	private final ArrayList<String> interactiveActionsList = new ArrayList<>();
	private final ArrayList<String> runtimeKeysList = new ArrayList<>();
	private final ArrayList<String> delegatesList = new ArrayList<>();
	
	protected static String root = "local/";
	private final String localLedger = "local_ledger/";
	private final String issuerSecretStore = "issuer/";
	private final String issuerParametersStore = "issuance_parameters/";
	private final String issuancePolicyStore = "policies/";
	private final String issuerIssued = "issuance_logs/";
	private final String ownerSecretStore = "credentials/";
	private final String noninteractiveTokenStore = "tokens/";
	private final String inspectorStore = "inspector/";
	private final String revocationAuthStore = "revocation/";
	private final String actionParams = "action_params/";
	private final String demParmas = "dem_params/";
	private final String interactiveActions = "interative_actions/";
	private final String runtimeKeys = "runtime_keys/";
	private final String delegates = "delegates/";

	private SystemParameters params = null;
	
	public static final URI POLICY_BASE_NYM_POLICY = URI.create("urn:x:policy:base-pseudonym-policy"); 
	
	public static boolean bootContainerExists() throws Exception{
		URI base = NamespaceMngt.BASE_LOCATION;
		File f = new File(base.getPath());
		if (f.isDirectory()){
			return f.list().length!=0;
			
		} else {
			return false;	
			
		}
	}

	
	public IdContainer(String containerName) throws Exception {
		super(containerName);
		URI base = NamespaceMngt.BASE_LOCATION.resolve(containerName + "/");
		
		root += this.getUsername() + "/";
		File file = new File(base);
		
		if (!file.exists()){
			throw new FileNotFoundException();
			
		}
		LOCAL_LEDGER = base.resolve(localLedger);
		ISSUER_SECRET_STORE = base.resolve(issuerSecretStore);
		ISSUER_PARAMETERS_STORE = base.resolve(issuerParametersStore);
		ISSUANCE_POLICY_STORE = base.resolve(issuancePolicyStore);
		ISSUER_ISSUED = base.resolve(issuerIssued);
		OWNER_PRIVATE_STORE= base.resolve(ownerSecretStore);
		NONINTERACTIVE_TOKENS= base.resolve(noninteractiveTokenStore);
		INSPECTOR_STORE = base.resolve(inspectorStore); 
		REVOCATION_AUTH_STORE = base.resolve(revocationAuthStore);
		ACTION_PARAMS = base.resolve(actionParams);
		DEM_PARAMS = base.resolve(demParmas);
		INTERACTIVE_ACTIONS = base.resolve(interactiveActions);
		RUNTIME_KEYS = base.resolve(runtimeKeys);
		DELEGATES = base.resolve(delegates);

		updateLists();

	}

	public IdContainer(String containerName, boolean create) throws Exception {
		super(containerName);
		URI base = NamespaceMngt.BASE_LOCATION.resolve(containerName + "/");
		root += containerName + "/";
		File file = new File(base);
		if (create && file.exists()){
			throw new FileAlreadyExistsException(file.getAbsolutePath());
			
		}
		LOCAL_LEDGER = base.resolve(localLedger);
		ISSUER_SECRET_STORE = base.resolve(issuerSecretStore);
		ISSUER_PARAMETERS_STORE = base.resolve(issuerParametersStore);
		ISSUANCE_POLICY_STORE = base.resolve(issuancePolicyStore);
		ISSUER_ISSUED = base.resolve(issuerIssued);
		OWNER_PRIVATE_STORE= base.resolve(ownerSecretStore);
		NONINTERACTIVE_TOKENS= base.resolve(noninteractiveTokenStore);
		INSPECTOR_STORE = base.resolve(inspectorStore); 
		REVOCATION_AUTH_STORE = base.resolve(revocationAuthStore);
		ACTION_PARAMS = base.resolve(actionParams);
		DEM_PARAMS = base.resolve(demParmas);
		INTERACTIVE_ACTIONS = base.resolve(interactiveActions);
		RUNTIME_KEYS = base.resolve(runtimeKeys);
		DELEGATES = base.resolve(delegates);
		updateLists();
		
	}

	
	public synchronized void delete(){
		ArrayList<File> files = new ArrayList<>();
		files.add(new File(LOCAL_LEDGER));
		files.add(new File(ISSUER_SECRET_STORE));
		files.add(new File(ISSUER_PARAMETERS_STORE));
		files.add(new File(ISSUANCE_POLICY_STORE));
		files.add(new File(ISSUER_ISSUED));
		files.add(new File(OWNER_PRIVATE_STORE));
		files.add(new File(NONINTERACTIVE_TOKENS));
		files.add(new File(INSPECTOR_STORE));
		files.add(new File(REVOCATION_AUTH_STORE));
		files.add(new File(ACTION_PARAMS));
		files.add(new File(DEM_PARAMS));
		files.add(new File(INTERACTIVE_ACTIONS));
		files.add(new File(RUNTIME_KEYS));
		files.add(new File(DELEGATES));
		files.add(new File(NamespaceMngt.BASE_LOCATION.resolve(this.getUsername()+ "/")));
		
		for(File file:files){
			if (file.isDirectory()){
				File[] child = file.listFiles();
				for (int i = 0; i < child.length; i++) {
					child[i].delete();
					
				}
			}
			file.delete();
			
		}
	}
	
	public void updateLists() {
		localLedgerList.clear();
		issuerSecretList.clear();
		issuanceParameterList.clear();
		issuancePolicyList.clear();
		issuedList.clear();
		ownerSecretList.clear();
		noninteractiveTokenList.clear();
		inspectorList.clear();
		revocationAuthList.clear();
		
		actionParamsList.clear();
		demParmasList.clear();
		interactiveActionsList.clear();
		runtimeKeysList.clear();
		delegatesList.clear();
		
		listResources(LOCAL_LEDGER, localLedgerList);
		listResources(ISSUER_SECRET_STORE, issuerSecretList);
		listResources(ISSUER_PARAMETERS_STORE, issuanceParameterList);
		listResources(ISSUANCE_POLICY_STORE, issuancePolicyList);
		listResources(ISSUER_ISSUED, issuedList);
		listResources(OWNER_PRIVATE_STORE, ownerSecretList);
		listResources(NONINTERACTIVE_TOKENS, noninteractiveTokenList);
		listResources(INSPECTOR_STORE, inspectorList);
		listResources(REVOCATION_AUTH_STORE, revocationAuthList);
		listResources(ACTION_PARAMS, actionParamsList);
		listResources(DEM_PARAMS, demParmasList);
		listResources(INTERACTIVE_ACTIONS, interactiveActionsList);
		listResources(RUNTIME_KEYS, runtimeKeysList);
		listResources(DELEGATES, delegatesList);
		
	}

	private void listResources(URI store, ArrayList<String> list) {
		File folder = new File(store);
		if (!folder.exists()){
			folder.mkdirs();
			
		}
		File[] files = folder.listFiles();
		for (File file: files){
			if (file.getName().endsWith(".xml")){
				list.add(file.getName());
				
			}
		}
	}
	
//	public DeviceAssetLibrary getDeviceAssetLibrary(Cipher dec) throws Exception {
//		updateLists();
//		DeviceAssetLibrary dal = new DeviceAssetLibrary();
//		dal.setUsername(this.getUsername());
//		dal.setLastUpdateTimeUtc(DateHelper.currentIsoUtcDateTime());
//
//		for (String s : ownerSecretList) {
//			if (FileType.isAnonCredentialParameters(s)){
//				AnonCredentialParameters acp = openResource(s, dec);
//				dal.setCountry(acp.getCountryG2());
//
//			} else if (FileType.isCredential(s)){
//				Credential c = openResource(s, dec);
//
//				if (c!=null){
//					CredentialDescription cd = c.getCredentialDescription();
//					URI issuerParams = cd.getIssuerParametersUID();
//					URI credSpec = cd.getCredentialSpecificationUID();
//
//					CredentialSpecification spec = openResource(credSpec);
//					CredentialSpecificationAdapter csa = new CredentialSpecificationAdapter();
//					csa.setSpec(spec);
//
//					IssuerParameters params = openResource(issuerParams);
//					IssuerParametersAdapter ipa = new IssuerParametersAdapter();
//					ipa.set(params);
//
//					dal.getCredentials().add(csa);
//					dal.getIssuers().add(ipa);
//
//				}
//			} else {
//				logger.info("Skipping file type");
//
//			}
//		}
//		return dal;
//
//	}
	
	public synchronized ConnectKeyContainer discoverConnectKeys(URI deviceUid){
		int latest = computeLatest(deviceUid); 
		if (latest!=0){
			try {
				String fileName = latest + "-" + deviceUid + ".kc.xml";
				logger.info("Discovered latest key set for user " + this.getUsername());
				return (ConnectKeyContainer) openXFile(new File(RUNTIME_KEYS.resolve(fileName).getPath()), ConnectKeyContainer.class);

			} catch (Exception e) {
				logger.throwing("XContainer.class", "discoverConnectKeys()", e);
				return null;
				
			}
		} else {
			return null;
			
		}
	}

	private int computeLatest(URI deviceUid) {
		runtimeKeysList.clear();
		listResources(RUNTIME_KEYS, runtimeKeysList);
		int latest = 0;
		for (String file : runtimeKeysList){
			logger.info(file);
			if (file.contains(deviceUid.toString())){
				try {
					String[] parts = file.split("-");
					int date = Integer.parseInt(parts[0]);
					if (latest < date){
						latest = date; 
						
					}
				} catch (Exception e) {
					logger.throwing("XContainer.class", "computeLatest()", e);
					
				}
			}
		}
		return latest;
	}
	
	public AnonCredentialParameters openAnonCredential(Cipher dec) throws Exception {
		updateLists();
		for (String s: ownerSecretList){
			if (s.contains(".acp.xml")){
				return openResource(s, dec);
				
			}
		} 
		throw new Exception("Container has not been fully defined.  There is no Anonymous Credential.");
		
	}

	@SuppressWarnings("unchecked")
	public synchronized <T> T openResource(String fullFileName, Cipher dec) throws Exception{
		if (!FileType.isXmlDocument(fullFileName)){
			throw new FileSystemException("Resources are all .xml files");
			
		} else if (FileType.isIssuerSecret(fullFileName)){ // Local
			return (T) openEncryptedFile(ISSUER_SECRET_STORE.resolve(fullFileName).getPath(), SecretKey.class, dec); //root + issuerSecretStore + 
			
		} else if (FileType.isOwnerSecret(fullFileName)){ // Local
			return (T) openEncryptedFile(OWNER_PRIVATE_STORE.resolve(fullFileName).getPath(), Secret.class, dec);
			
		} else if (FileType.isMintedAnonCredential(fullFileName)){ // Local
			return (T) openEncryptedXFile(new File(OWNER_PRIVATE_STORE.resolve(fullFileName).getPath()), MintedAnonCredential.class, dec);
			
		} else if (FileType.isKeys(fullFileName)){ // Local
			return (T) openXFile(new File(RUNTIME_KEYS.resolve(fullFileName).getPath()), KeyContainer.class);
			
		} else if (FileType.isConnectKeys(fullFileName)){ // Local
			throw new RuntimeException("Open Connect Keys using the dedicated method discoverConnectKeys()");
			
		} else if (FileType.isDevices(fullFileName)){ // Local
			return (T) openXFile(new File(RUNTIME_KEYS.resolve(fullFileName).getPath()), RegisteredDevices.class);
			
		} else if (FileType.isAnonCredentialParameters(fullFileName)){ // Local
			return (T) openEncryptedXFile(new File(OWNER_PRIVATE_STORE.resolve(fullFileName).getPath()), AnonCredentialParameters.class, dec);
			
		} else if (FileType.isIssuanceLog(fullFileName)){ // Local
			return (T) openFile(ISSUER_ISSUED.resolve(fullFileName).getPath(), IssuanceLogEntry.class);
			
		} else if (FileType.isCredential(fullFileName)){ // Local
			return (T) openEncryptedFile(OWNER_PRIVATE_STORE.resolve(fullFileName).getPath(), Credential.class, dec);
			
		} else if (FileType.isRevocationAuthorityPrivateKey(fullFileName)){ // Local
			return (T) openEncryptedFile(REVOCATION_AUTH_STORE.resolve(fullFileName).getPath(), PrivateKey.class, dec);

		} else if (FileType.isRevocationHistory(fullFileName)){ // Local
			return openFile(REVOCATION_AUTH_STORE.resolve(fullFileName).getPath(), RevocationHistory.class);

		} else if (FileType.isPresentationPolicy(fullFileName)){ // Local
			return (T) openFile(ISSUANCE_POLICY_STORE.resolve(fullFileName).getPath(), IssuerParameters.class);

		}
		try {
			if (FileType.isIssuerParameters(fullFileName)){
				return (T) openFile(ISSUER_PARAMETERS_STORE.resolve(fullFileName).getPath(), IssuerParameters.class);
				
			} else if (FileType.isGroup(fullFileName)){ // Local
				return (T) JaxbHelper.xmlFileToClass(Paths.get(LOCAL_LEDGER.resolve(fullFileName).getPath()), LocalLedgerGroup.class);

			} else if (FileType.isSystemParameters(fullFileName)){
				return (T) openFile(LOCAL_LEDGER.resolve(fullFileName).getPath(), SystemParameters.class);
				
			} else if (FileType.isRegistrationParams(fullFileName)){
				return (T) JaxbHelper.xmlFileToClass(Paths.get(LOCAL_LEDGER.resolve(fullFileName).getPath()), RegistrationParameters.class);
				
			} else if (FileType.isPresentationPolicyAlternatives(fullFileName)){
				return (T) JaxbHelper.xmlFileToClass(Paths.get(LOCAL_LEDGER.resolve(fullFileName).getPath()), PresentationPolicyAlternativesAdapter.class);
				
			} else if (FileType.isCredentialSpecification(fullFileName)){
				return (T) openFile(LOCAL_LEDGER.resolve(fullFileName).getPath(), CredentialSpecification.class); // root + localLedger + 

			} else if (FileType.isIssuancePolicy(fullFileName)){
				return (T) openFile(ISSUANCE_POLICY_STORE.resolve(fullFileName).getPath(), IssuancePolicy.class); // root + issuancePolicyStore + 

			} else if (FileType.isProofToken(fullFileName)){ // !!!! !Local TODO Move after implemented on ledger
				return (T) openFile(NONINTERACTIVE_TOKENS.resolve(fullFileName).getPath(), PresentationToken.class); //root + noninteractiveTokenStore + 
				
			} else if (FileType.isInspectorPublicKey(fullFileName)){
				return (T) openFile(INSPECTOR_STORE.resolve(fullFileName).getPath(), InspectorPublicKey.class); //root + inspectorStore + 

			} else if (FileType.isRevocationAuthority(fullFileName)){
				return (T) openFile(REVOCATION_AUTH_STORE.resolve(fullFileName).getPath(), RevocationAuthorityParameters.class);

			} else if (FileType.isRevocationInformation(fullFileName)){
				return (T) openFile(REVOCATION_AUTH_STORE.resolve(fullFileName).getPath(), RevocationInformation.class);

			} else {
				throw new FileSystemException("File type not recognized " + fullFileName);
				
			}
		} catch (FileSystemException e) {
			throw e; 
			
		} catch (Exception e) {
			throw new Exception("The ledger architecture was changed and searching for different resources is handled in the BaseActor");
			
		}
	}

	
	public synchronized SystemParameters getParams() throws Exception{
		if (this.params==null){
			this.params = openFile(NamespaceMngt.LEDGER.resolve("lambda.xml").getPath(), SystemParameters.class);
			
		}
		return params;
		
	}
	
	@SuppressWarnings("unchecked")
	private synchronized <T> T openEncryptedFile(String fileName, Class<?> clazz, Cipher dec) throws Exception {
		if (dec!=null){
			try (FileInputStream fis = new FileInputStream(new File(fileName))){
				byte[] bytes = new byte[fis.available()];
				fis.read(bytes);
				bytes = Base64.decodeBase64(bytes);
				bytes = dec.doFinal(bytes);
				ByteArrayInputStream is = new ByteArrayInputStream(bytes);
				JAXBElement<?> resourceAsJaxbElement = JaxbHelperClass.deserialize(is, true);
				return (T)JAXBIntrospector.getValue(resourceAsJaxbElement);
				
			} catch (IllegalBlockSizeException e) {
				logger.throwing("XContainer.class", "openEncryptedFile()", e);
				throw e;
				
			} catch (SerializationException e) {
				RuntimeException e0 = new RuntimeException("Error unmarshalling file " + fileName + " it's likely that you're passing in an incorrect Cipher.");
				e0.initCause(e);
				throw e0; 
				
			} catch (Exception e) {
				throw e; 
				
			}
		} else {
			throw new RuntimeException("Encrypted data with no cipher, or plain text secret data.");
			
		}
	}		
	
	@SuppressWarnings("unchecked")
	private synchronized <T> T openFile(String fileName, Class<?> clazz) throws Exception {
		try (FileInputStream fis = new FileInputStream(new File(fileName))){
			JAXBElement<?> resourceAsJaxbElement = JaxbHelperClass.deserialize(fis, true);
			return (T)JAXBIntrospector.getValue(resourceAsJaxbElement);
			
		} catch (Exception e) {
			throw e; 
			
		}
	}		
	
	protected synchronized void saveEncrypted(String xml, URI location, String fileName, boolean overwrite, Cipher enc) throws Exception {
		if (enc!=null){
			String encXml = Base64.encodeBase64String(enc.doFinal(xml.getBytes()));
			save(encXml, location, fileName, overwrite);
			
		} else {
			throw new RuntimeException("Encrypted data in plain text error.");
			
		}
	}	

	protected synchronized void save(String xml, URI location, String fileName, boolean overwrite) throws Exception {
		logger.info("saving:  " + location + fileName);
	    File file = new File(URI.create(location + fileName));
	    if (!overwrite){
	    	if (file.exists()){
	          	throw new FileAlreadyExistsException("The file already exists " + fileName);
	          	
	        }
	    }
	    if (!file.getParentFile().exists()) {
	        file.getParentFile().mkdirs();
	        
	    } 
	    try (OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(file), "UTF-8")){
	    	out.write(xml);
		    out.flush();
			
		} catch (Exception e) {
			throw e; 
			
		}
	}
	
	@SuppressWarnings("unchecked")
	private synchronized <T> T openEncryptedXFile(File file, Class<?> clazz, Cipher dec) throws Exception {
		if (dec == null){
			throw new RuntimeException("Attempting to open encrypted file without a cipher.");
			
		}
		try (FileInputStream fis = new FileInputStream(file)){
			byte[] bytes = new byte[fis.available()];
			fis.read(bytes);
			String xml = new String(dec.doFinal(Base64.encodeBase64(bytes)));
			return (T) JaxbHelper.xmlToClass(xml, clazz);
			
		} catch (Exception e) {
			throw e; 
			
		}
	}
	
	@SuppressWarnings("unchecked")
	private synchronized <T> T openXFile(File file, Class<?> clazz) throws Exception {
		try (FileInputStream fis = new FileInputStream(file)){
			byte[] bytes = new byte[fis.available()];
			fis.read(bytes);
			String xml = new String(bytes);
			return (T) JaxbHelper.xmlToClass(xml, clazz);
			
		} catch (Exception e) {
			throw e; 
			
		}
	}
	
	public static boolean exists(String name){
		try {
			new IdContainer(name);
			return true; 
			
		} catch (Exception e) {
			return false; 
			
		}
	} 

	public URI getLOCAL_LEDGER() {
		return LOCAL_LEDGER;
	}


	public URI getISSUER_SECRET_STORE() {
		return ISSUER_SECRET_STORE;
	}


	public URI getISSUER_PARAMETERS_STORE() {
		return ISSUER_PARAMETERS_STORE;
	}


	public URI getISSUANCE_POLICY_STORE() {
		return ISSUANCE_POLICY_STORE;
	}


	public URI getISSUER_ISSUED() {
		return ISSUER_ISSUED;
	}


	public URI getOWNER_PRIVATE_STORE() {
		return OWNER_PRIVATE_STORE;
	}


	public URI getNONINTERACTIVE_TOKENS() {
		return NONINTERACTIVE_TOKENS;
	}


	public URI getINSPECTOR_STORE() {
		return INSPECTOR_STORE;
	}


	public URI getREVOCATION_AUTH_STORE() {
		return REVOCATION_AUTH_STORE;
	}


	public URI getACTION_PARAMS() {
		return ACTION_PARAMS;
	}


	public URI getDEM_PARAMS() {
		return DEM_PARAMS;
	}


	public URI getINTERACTIVE_ACTIONS() {
		return INTERACTIVE_ACTIONS;
	}


	public URI getRUNTIME_KEYS() {
		return RUNTIME_KEYS;
	}


	public URI getDELEGATES() {
		return DELEGATES;
		
	}
}