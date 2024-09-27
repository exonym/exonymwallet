package io.exonym.lib.api;

import com.ibm.zurich.idmx.jaxb.JaxbHelperClass;
import eu.abc4trust.xml.*;
import io.exonym.lib.abc.util.FileType;
import io.exonym.lib.abc.util.JaxbHelper;
import io.exonym.lib.adapters.PresentationPolicyAlternativesAdapter;
import io.exonym.lib.exceptions.HubException;
import io.exonym.lib.exceptions.UxException;
import io.exonym.lib.lite.SFTPLogonData;
import io.exonym.lib.pojo.*;
import io.exonym.lib.standard.WhiteList;

import javax.crypto.Cipher;
import java.net.URI;
import java.util.ArrayList;
import java.util.logging.Logger;

public abstract class AbstractIdContainer {
	
	protected final static ObjectFactory of = new ObjectFactory();

	protected final Logger logger;

	private final String username;
	private final URI secretUri;
	
	protected URI LOCAL_LEDGER = URI.create(Namespace.URN_PREFIX_COLON + "local");
	protected URI ISSUER_SECRET_STORE = URI.create(Namespace.URN_PREFIX_COLON + "secret");
	protected URI ISSUER_PARAMETERS_STORE = URI.create(Namespace.URN_PREFIX_COLON + "issuer-params");
	protected URI ISSUANCE_POLICY_STORE = URI.create(Namespace.URN_PREFIX_COLON + "issuer-policy");
	protected URI ISSUER_ISSUED = URI.create(Namespace.URN_PREFIX_COLON + "issuer-issued");
	protected URI OWNER_PRIVATE_STORE = URI.create(Namespace.URN_PREFIX_COLON + "owner-private");
	protected URI NONINTERACTIVE_TOKENS = URI.create(Namespace.URN_PREFIX_COLON + "non-interactive-tokens");
	protected URI INSPECTOR_STORE = URI.create(Namespace.URN_PREFIX_COLON + "inspector");
	protected URI REVOCATION_AUTH_STORE = URI.create(Namespace.URN_PREFIX_COLON + "revocation-authority");
	protected URI ACTION_PARAMS = URI.create(Namespace.URN_PREFIX_COLON + "action-params");
	protected URI DEM_PARAMS = URI.create(Namespace.URN_PREFIX_COLON + "dem-params");
	protected URI INTERACTIVE_ACTIONS = URI.create(Namespace.URN_PREFIX_COLON + "interactive-actions");
	protected URI RUNTIME_KEYS = URI.create(Namespace.URN_PREFIX_COLON + "runtime-keys");
	protected URI DELEGATES = URI.create(Namespace.URN_PREFIX_COLON + "delegates");
	
	protected ArrayList<String> localLedgerList = new ArrayList<>();
	protected ArrayList<String> issuerSecretList = new ArrayList<>();
	protected ArrayList<String> issuanceParameterList = new ArrayList<>();
	protected ArrayList<String> issuancePolicyList = new ArrayList<>();
	protected ArrayList<String> issuedList = new ArrayList<>();
	protected ArrayList<String> ownerSecretList = new ArrayList<>();
	protected ArrayList<String> noninteractiveTokenList = new ArrayList<>();
	protected ArrayList<String> inspectorList = new ArrayList<>();
	protected ArrayList<String> revocationAuthList = new ArrayList<>();


	public AbstractIdContainer(String username) throws Exception {
		if (username==null){
			throw new HubException("No name for container");
			
		}if (username.contains(" ")){
			throw new HubException("No name for container - space");

		}
		this.logger = Logger.getLogger(AbstractIdContainer.class.getName());
		this.username=username;
		this.secretUri = createSecretUri();
		
	}
	
	public synchronized String getUsername() {
		return username;

	}
	
	@SuppressWarnings("unchecked")
	public synchronized <T> T openResource(URI uri) throws Exception{
		if (WhiteList.isRulebookUid(uri)){
			return (T)openResource(uidToFileName(uri) + ".json", null);
		} else {
			return (T)openResource(uidToXmlFileName(uri), null);
		}
	}
	
	@SuppressWarnings("unchecked")
	public synchronized <T> T openResource(String fileName) throws Exception{
		return (T)openResource(fileName, null);
		
	}
	
	@SuppressWarnings("unchecked")
	public synchronized <T> T openResource(URI uri, Cipher dec) throws Exception{
		return (T)openResource(uidToXmlFileName(uri), dec);
		
	}
	
	public synchronized void saveLocalResource(Object resource) throws Exception {
		saveLocalResource(resource, false, null);
		
	}
	
	public synchronized void saveLocalResource(Object resource, Cipher enc) throws Exception {
		saveLocalResource(resource, false, enc);
		
	}
	
	public synchronized void saveLocalResource(Object resource, boolean overwrite) throws Exception {
		saveLocalResource(resource, overwrite, null);
		
	}	

	public synchronized void saveLocalResource(Object resource, boolean overwrite, Cipher enc) throws Exception {
		if (resource instanceof Credential){
			saveCredential((Credential)resource, overwrite, enc);

		} else if (resource instanceof SFTPLogonData){
			saveSFTPCredentials((SFTPLogonData) resource, overwrite, enc);

		} else if (resource instanceof AnonCredentialParameters){
			saveAnonCredentialParameters((AnonCredentialParameters) resource, overwrite, enc);

		} else if (resource instanceof MintedAnonCredential){
			saveMintedAnonCredential((MintedAnonCredential)resource, overwrite, enc);

		} else if (resource instanceof RegisteredDevices){
			saveRegisteredDevices((RegisteredDevices)resource, overwrite);

		} else if (resource instanceof ConnectKeyContainer){
			saveConnectKeyContainer((ConnectKeyContainer)resource, overwrite);

		} else if (resource instanceof KeyContainer){
			saveKeyContainer((KeyContainer)resource, overwrite);

		} else if (resource instanceof LocalLedgerGroup){
			saveLocalLedgerGroup((LocalLedgerGroup)resource, overwrite);

		} else if (resource instanceof CredentialSpecification){
			saveCredentialSpecification((CredentialSpecification) resource, overwrite);
			
		} else if (resource instanceof PresentationPolicy){
			savePresentationPolicy((PresentationPolicy) resource, overwrite);
			
		} else if (resource instanceof PresentationPolicyAlternatives){
			throw new HubException("You must save the PresentationPolicyAlternativesAdapter "
					+ "and set the UID of that file to save objects of this type.");
			
		} else if (resource instanceof PresentationPolicyAlternativesAdapter){
			savePresentationPolicyAlternatives((PresentationPolicyAlternativesAdapter) resource, overwrite);
			
		} else if (resource instanceof IssuancePolicy){
			saveIssuancePolicy((IssuancePolicy) resource, overwrite);
			
		} else if (resource instanceof IssuanceToken){
			throw new RuntimeException("Programming error.  Use saveIssuanceToken directly to specify a name.");
			
		/* } else if (resource instanceof IssuanceLogEntry){
			saveIssuerLogEntry((IssuanceLogEntry) resource, overwrite); //*/ 
			
		} else if (resource instanceof SecretKey){
			saveIssuerSecretKey((SecretKey) resource, overwrite, enc);

		} else if (resource instanceof PrivateKey){
			saveRaPrivateKey((PrivateKey) resource, overwrite, enc);

		} else if (resource instanceof Secret){
			saveOwnerSecret((Secret) resource, overwrite, enc);
			
		} else if (resource instanceof IssuerParameters){
			saveIssuerParameters((IssuerParameters) resource, overwrite);
			
		} else if (resource instanceof InspectorPublicKey){
			saveInspectorPublicKey((InspectorPublicKey) resource, overwrite);
			
		} else if (resource instanceof PresentationToken){
			saveProofToken((PresentationToken) resource, overwrite);
			
		} else if (resource instanceof RevocationAuthorityParameters){
			saveRevocationAuthorityParameters((RevocationAuthorityParameters) resource, overwrite);

		} else if (resource instanceof RevocationHistory){
			saveRevocationHistory((RevocationHistory) resource, overwrite);

		} else if (resource instanceof RevocationInformation){
			saveRevocationAuthorityInformation((RevocationInformation) resource, overwrite);

		} else if (resource instanceof RegistrationParameters){
			saveRegistrationParameters((RegistrationParameters) resource, overwrite);

		} else if (resource instanceof Rulebook){
			saveRulebook((Rulebook) resource, overwrite);

		} else if (resource==null) {
			throw new Exception("Resource to save was null");
			
		} else {
			throw new Exception("Unrecognised Object " + resource.toString());
			
		}
		updateLists();
	}



	public abstract void updateLists() throws Exception;
	
	private void saveCredential(Credential c, boolean overwrite, Cipher store)  throws Exception {
		String name = uidToFileName(c.getCredentialDescription().getIssuerParametersUID());
		name += "c.xml";
		String xml = JaxbHelperClass.serialize(of.createCredential(c)); // true
		saveEncrypted(xml, OWNER_PRIVATE_STORE, name, overwrite, store);
		
	}

	private void saveSFTPCredentials(SFTPLogonData c, boolean overwrite, Cipher enc) throws Exception {
		String name = uidToFileName(c.getSftpUID());
		name += ".xml";
		String xml = JaxbHelper.serializeToXml(c, SFTPLogonData.class);
		saveEncrypted(xml, OWNER_PRIVATE_STORE, name, overwrite, enc);

	}


	private void saveCredentialSpecification(CredentialSpecification resource, boolean overwrite) throws Exception {
		String name = uidToFileName(resource.getSpecificationUID());
		name += ".xml";
		String xml = JaxbHelperClass.serialize(of.createCredentialSpecification(resource), true);
		save(xml, LOCAL_LEDGER, name, overwrite);
		
	}

	private void savePresentationPolicy(PresentationPolicy pp, boolean overwrite) throws Exception {
		String name = uidToFileName(pp.getPolicyUID());
		name += ".xml";
		String xml = JaxbHelperClass.serialize(of.createPresentationPolicy(pp), true);
		save(xml, ISSUANCE_POLICY_STORE, name, overwrite);
		
	}

	private void savePresentationPolicyAlternatives(PresentationPolicyAlternativesAdapter ppa, boolean overwrite) throws Exception {
		String name = uidToXmlFileName(ppa.getPresentationPolicyAlternativeUid());
		
		if (FileType.isPresentationPolicyAlternatives(name)){
			String xml = JaxbHelper.serializeToXml(ppa, PresentationPolicyAlternativesAdapter.class);
			save(xml, LOCAL_LEDGER, name, overwrite);
			
		} else { 
			throw new UxException("Presentation Policy Alternatives UIDs must end with the suffix ':ppa'");
			
		}
	}

	private void saveIssuancePolicy(IssuancePolicy ip, boolean overwrite) throws Exception {
		String name = uidToFileName(ip.getCredentialTemplate().getIssuerParametersUID());
		name += "p.xml";
		String xml = JaxbHelperClass.serialize(of.createIssuancePolicy(ip), true);
		save(xml, ISSUANCE_POLICY_STORE, name, overwrite);
		
	}

	public synchronized void saveIssuanceToken(IssuanceToken it, String name, Cipher store)  throws Exception {
		name += ".xml";
		String xml = JaxbHelperClass.serialize(of.createIssuanceToken(it), true);
		save(xml, ISSUER_ISSUED, name, false);
		// saveEncrypted(xml, ISSUER_ISSUED + name, false, store);
		
	}

	/* private void saveIssuerLogEntry(IssuanceLogEntry il, boolean overwrite) throws Exception {
		String name = uidToFileName(il.getIssuanceLogEntryUID());
		name += ".il.xml";
		String xml = JaxbHelperClass.serialize(of.createIssuanceLogEntry(il), true);
		save(xml, ISSUER_ISSUED + name, overwrite);
		
	} //*/ 

	private void saveInspectorPublicKey(InspectorPublicKey ins, boolean overwrite) throws Exception {
		String name = uidToFileName(ins.getPublicKeyUID());
		name += ".xml";
		String xml = JaxbHelperClass.serialize(of.createInspectorPublicKey(ins), true);
		save(xml, INSPECTOR_STORE, name, overwrite);
		
	}
	
	private void saveIssuerSecretKey(SecretKey is, boolean overwrite, Cipher store)  throws Exception {
		String name = uidToFileName(is.getSecretKeyUID());
		name += "s.xml";
		String xml = JaxbHelperClass.serialize(of.createIssuerSecretKey(is), true);
		saveEncrypted(xml, ISSUER_SECRET_STORE, name, overwrite, store);
		
	}	
	
	private void saveOwnerSecret(Secret s, boolean overwrite, Cipher store)  throws Exception {
		String name = uidToXmlFileName(s.getSecretDescription().getSecretUID());
		String xml = JaxbHelperClass.serialize(of.createSecret(s), true);
		saveEncrypted(xml, OWNER_PRIVATE_STORE, name, overwrite, store);
		
	}
	
	private void saveRaPrivateKey(PrivateKey s, boolean overwrite, Cipher store)  throws Exception {
		String name = uidToFileName(s.getPublicKeyId())  ;
		name = name.substring(0, name.indexOf(".ra")) + ".ras.xml";
		String xml = JaxbHelperClass.serialize(of.createPrivateKey(s), true);
		saveEncrypted(xml, REVOCATION_AUTH_STORE, name, overwrite, store);
		
	}
	
	private void saveRevocationAuthorityParameters(RevocationAuthorityParameters rap, boolean overwrite) throws Exception {
		String name = uidToXmlFileName(rap.getParametersUID());
		String xml = JaxbHelperClass.serialize(of.createRevocationAuthorityParameters(rap), true);
		save(xml, REVOCATION_AUTH_STORE, name, overwrite);
		
	}
	
	private void saveRevocationAuthorityInformation(RevocationInformation ri, boolean overwrite) throws Exception {
		String name = uidToFileName(ri.getRevocationAuthorityParametersUID()) + "i.xml";
		String xml = JaxbHelperClass.serialize(of.createRevocationInformation(ri), true);
		save(xml, REVOCATION_AUTH_STORE, name, overwrite);

	}
	
	private void saveRegistrationParameters(RegistrationParameters resource, boolean overwrite) throws Exception {
		String name = uidToFileName(resource.getGroupUid()) + ".rp.xml";
		String xml = JaxbHelper.serializeToXml(resource, RegistrationParameters.class);
		save(xml, LOCAL_LEDGER, name, overwrite);
		
	}
	
	private void saveLocalLedgerGroup(LocalLedgerGroup lg, boolean overwrite) throws Exception {
		String name = uidToFileName(lg.getGroupUid()) + ".rp.xml";
		String xml = JaxbHelper.serializeToXml(lg, LocalLedgerGroup.class);
		save(xml, LOCAL_LEDGER, name, overwrite);
		
	}	
	
	private void saveAnonCredentialParameters(AnonCredentialParameters resource, boolean overwrite, Cipher store)  throws Exception {
		String name = uidToFileName(resource.getGroupUid()) + ".acp.xml";
		String xml = JaxbHelper.serializeToXml(resource, AnonCredentialParameters.class);
		saveEncrypted(xml, OWNER_PRIVATE_STORE, name, overwrite, store);
		
	}

	private void saveMintedAnonCredential(MintedAnonCredential resource, boolean overwrite, Cipher store)  throws Exception {
		String name = uidToFileName(resource.getGroupUid()) + ".mac.xml";
		String xml = JaxbHelper.serializeToXml(resource, MintedAnonCredential.class);
		saveEncrypted(xml, OWNER_PRIVATE_STORE, name, overwrite, store);

	}


	private void saveRegisteredDevices(RegisteredDevices resource, boolean overwrite)  throws Exception {
		String name = "devices.xml";
		String xml = JaxbHelper.serializeToXml(resource, RegisteredDevices.class);
		save(xml, RUNTIME_KEYS, name, overwrite);

	}
	
	private void saveKeyContainer(KeyContainer resource, boolean overwrite)  throws Exception {
		String name = "keys.xml";
		String xml = JaxbHelper.serializeToXml(resource, KeyContainer.class);
		save(xml, RUNTIME_KEYS, name, overwrite);

	}	
	
	private void saveConnectKeyContainer(ConnectKeyContainer resource, boolean overwrite) throws Exception {
		String name = resource.getFileName();
		String xml = JaxbHelper.serializeToXml(resource, ConnectKeyContainer.class);
		save(xml, RUNTIME_KEYS, name, overwrite);
		
	}
	
	private void saveRevocationHistory(RevocationHistory rh, boolean overwrite) throws Exception {
		String name = uidToFileName(rh.getRevocationAuthorityParametersUID());
		name = name.substring(0, name.indexOf(".ra")) + ".rh.xml";
		String xml = JaxbHelperClass.serialize(of.createRevocationHistory((RevocationHistory)rh));
		save(xml, REVOCATION_AUTH_STORE, name, overwrite);
		
	}

	private void saveIssuerParameters(IssuerParameters i, boolean overwrite) throws Exception {
		String name = uidToFileName(i.getParametersUID());
		name += ".xml";
		String xml = JaxbHelperClass.serialize(of.createIssuerParameters(i), true);
		save(xml, ISSUER_PARAMETERS_STORE, name, overwrite);
		
	}
	
	private void saveProofToken(PresentationToken t, boolean overwrite) throws Exception {
		String name = uidToFileName(t.getPresentationTokenDescription().getPolicyUID());
		name = stripUidSuffix(name, 1);
		name = name + ".t.xml";
		String xml = JaxbHelperClass.serialize(of.createPresentationToken(t), true);
		save(xml, NONINTERACTIVE_TOKENS, name, overwrite);
		
	}

	private void saveRulebook(Rulebook resource, boolean overwrite) throws Exception {
		String name = uidToFileName(resource.getRulebookId()) + ".json";
		String json = JaxbHelper.serializeToJson(resource, Rulebook.class);
		save(json, LOCAL_LEDGER, name, overwrite);
	}


	protected abstract void save(String xml, URI location, String name, boolean overwrite) throws Exception;

	protected abstract void saveEncrypted(String xml, URI location, String name, boolean overwrite, Cipher store)throws Exception; 
	
	
	/**
	 *  The use of XML Filenames is baked into the base actor classes because IDMX is a file based system.
	 *  
	 *  Any extending classes, to cope with database storage of data, must cope with
	 *  the request for file names and translate that to the database version.
	 *   
	 */
	public abstract <T> T openResource(String fileName, Cipher dec) throws Exception;

	public abstract void delete() throws Exception;
	
	// public abstract void publishResource(String fullFileName) throws Exception;
	
	private URI createSecretUri() {
		return URI.create(Namespace.URN_PREFIX_COLON  + this.getUsername().replaceAll("\\.", ":") + ":ss");
		
	}
	
	public static String convertObjectToXml(Object resource) throws Exception{
		if (resource instanceof Credential){
			return JaxbHelperClass.serialize(of.createCredential((Credential)resource), true);
			
		} else if (resource instanceof CredentialSpecification){
			return JaxbHelperClass.serialize(of.createCredentialSpecification((CredentialSpecification)resource), true);
			
		} else if (resource instanceof PresentationPolicy){
			return JaxbHelperClass.serialize(of.createPresentationPolicy((PresentationPolicy)resource), true);
			
		} else if (resource instanceof PresentationPolicyAlternatives){
			return JaxbHelperClass.serialize(of.createPresentationPolicyAlternatives((PresentationPolicyAlternatives)resource), true);
			
		} else if (resource instanceof IssuancePolicy){
			return JaxbHelperClass.serialize(of.createIssuancePolicy((IssuancePolicy)resource), true);
			
		} else if (resource instanceof IssuanceLogEntry){
			return JaxbHelperClass.serialize(of.createIssuanceLogEntry((IssuanceLogEntry)resource), true);
			
		} else if (resource instanceof PrivateKey){
			return JaxbHelperClass.serialize(of.createPrivateKey((PrivateKey)resource), true);

		} else if (resource instanceof SecretKey){
			return JaxbHelperClass.serialize(of.createIssuerSecretKey((SecretKey)resource), true);

		} else if (resource instanceof Secret){
			return JaxbHelperClass.serialize(of.createSecret((Secret)resource), true);
			
		} else if (resource instanceof InspectorPublicKey){
			return JaxbHelperClass.serialize(of.createInspectorPublicKey((InspectorPublicKey)resource), true);
			
		} else if (resource instanceof IssuerParameters){
			return JaxbHelperClass.serialize(of.createIssuerParameters((IssuerParameters)resource), true);
			
		} else if (resource instanceof PresentationToken){
			return JaxbHelperClass.serialize(of.createPresentationToken((PresentationToken)resource), true);

		} else if (resource instanceof PresentationTokenDescription){
			return JaxbHelperClass.serialize(of.createPresentationTokenDescription((PresentationTokenDescription)resource), true);
			
		} else if (resource instanceof RevocationAuthorityParameters){
			return JaxbHelperClass.serialize(of.createRevocationAuthorityParameters((RevocationAuthorityParameters)resource), true);
			
		} else if (resource instanceof RevocationInformation){
			return JaxbHelperClass.serialize(of.createRevocationInformation((RevocationInformation)resource), true);
			
		} else if (resource instanceof RevocationHistory) {
			return JaxbHelperClass.serialize(of.createRevocationHistory((RevocationHistory) resource));

		} else if (resource instanceof IssuanceMessageAndBoolean) {
			return JaxbHelperClass.serialize(of.createIssuanceMessageAndBoolean((IssuanceMessageAndBoolean) resource), false);

		} else if (resource instanceof IssuanceMessage) {
			return JaxbHelperClass.serialize(of.createIssuanceMessage((IssuanceMessage) resource), false);

		} else if (resource instanceof SystemParameters) {
			return JaxbHelperClass.serialize(of.createSystemParameters((SystemParameters) resource), false);

		} else {
			throw new Exception("Unrecognised Object " + resource);
			
		}		
	}

	public static String uidToFileName(URI uri) throws Exception{
		if (uri==null){
			throw new Exception("Null uri");

		}
		return uidToFileName(uri.toString());

	}

	public static String uidToFileName(String uri) throws Exception{
		if (uri==null){
			throw new Exception("Null uri");

		}
		return uri.replaceAll(":", ".").replace(Namespace.URN_PREFIX_DOTTED_DOTTED, "");

	}

	public static String uidToXmlFileName(URI uri) throws Exception{
		if (uri==null){
			throw new Exception("Null uri");
			
		}
		return uidToXmlFileName(uri.toString());
		
	}

	public static String uidToXmlFileName(String uri) throws Exception{
		if (uri==null){
			throw new Exception("Null uri");

		}
		return uri.replaceAll(":", ".").replace(Namespace.URN_PREFIX_DOTTED_DOTTED, "") + ".xml";

	}


	public static String fileNameToUid(String fileName) throws Exception{
		if (fileName==null){
			throw new Exception("Null fileName");
			
		}
		return Namespace.URN_PREFIX_COLON +  fileName.replaceAll("\\.xml", "")
				.replaceAll("\\.", ":");
		
	}
	
	public static String stripUidSuffix(URI uri, int nOfCharsExcludingColon) throws Exception{
		if (uri==null){
			throw new Exception("Null uri");
			
		}
		return stripUidSuffix(uri.toString(), nOfCharsExcludingColon); 
		
	}
	
	public static String stripUidSuffix(String uri, int nOfCharsExcludingColon) throws Exception{
		if (uri==null){
			throw new Exception("Null uri");
			
		}
		if (uri.startsWith(Namespace.URN_PREFIX_COLON )){
			uri = uri.replace(Namespace.URN_PREFIX_COLON, "");
			
		}
		return uri.substring(0, uri.length()-(nOfCharsExcludingColon+1));
		
	}		

	public ArrayList<String> getLocalLedgerList() {
		return localLedgerList;
	}

	public ArrayList<String> getIssuerSecretList() {
		return issuerSecretList;
	}

	// TODO Rename to ISSUANCE parameter list
	public ArrayList<String> getIssuerParameterList() {
		return issuanceParameterList;
	}

	public ArrayList<String> getIssuancePolicyList() {
		return issuancePolicyList;
	}

	public ArrayList<String> getIssuedList() {
		return issuedList;
	}

	public ArrayList<String> getOwnerSecretList() {
		return ownerSecretList;
	}

	public ArrayList<String> getNoninteractiveTokenList() {
		return noninteractiveTokenList;
	}

	public ArrayList<String> getInspectorList() {
		return inspectorList;
	}

	public ArrayList<String> getRevocationList() {
		return revocationAuthList;
	}
	
	public synchronized URI getSecretUri() {
		return secretUri;
		
	}
}
