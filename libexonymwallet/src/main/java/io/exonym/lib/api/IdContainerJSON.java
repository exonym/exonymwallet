package io.exonym.lib.api;

import com.ibm.zurich.idmx.jaxb.JaxbHelperClass;
import eu.abc4trust.xml.*;
import io.exonym.lib.abc.util.FileType;
import io.exonym.lib.abc.util.JaxbHelper;
import io.exonym.lib.adapters.PresentationPolicyAlternativesAdapter;
import io.exonym.lib.exceptions.ErrorMessages;
import io.exonym.lib.exceptions.HubException;
import io.exonym.lib.exceptions.UxException;
import io.exonym.lib.lite.SFTPLogonData;
import io.exonym.lib.pojo.KeyContainer;
import io.exonym.lib.pojo.Rulebook;
import io.exonym.lib.pojo.IdContainerSchema;
import io.exonym.lib.standard.PassStore;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBIntrospector;
import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;

public class IdContainerJSON extends AbstractIdContainer {
	
	private final IdContainerSchema schema;
	private Path testFolder;
	private Path file;

	private final String defaultPath;

	public IdContainerJSON(String username) throws Exception {
		super(username);
		this.defaultPath = "identities/";
		testFolder = generatePathToFolder();
		file = generatePathToFile();
		schema = init(false);
		updateLists();

	}

	public IdContainerJSON(String username, boolean create) throws Exception {
		super(username);
		this.defaultPath = "identities/";
		testFolder = generatePathToFolder();
		file = generatePathToFile();
		schema = init(create);
		updateLists();

	}

	public IdContainerJSON(Path path, String username) throws Exception {
		super(username);
		this.defaultPath = computePath(path);
		testFolder = generatePathToFolder();
		file = generatePathToFile();
		schema = init(false);
		updateLists();

	}

	public IdContainerJSON(Path path, String username, boolean create) throws Exception {
		super(username);
		this.defaultPath = computePath(path);
		testFolder = generatePathToFolder();
		file = generatePathToFile();
		schema = init(create);
		updateLists();

	}

	/**
	 * Overwriting Instance.  Only use this if you know you want the
	 * incoming schema to overwrite the existing schema.
	 *
	 * @param path
	 * @param schema
	 * @throws Exception
	 */
	public IdContainerJSON(Path path, IdContainerSchema schema) throws Exception {
		super(schema.getUsername());
		this.defaultPath = computePath(path);
		this.testFolder = generatePathToFolder();
		this.file = generatePathToFile();
		this.schema = schema;
		Files.createDirectories(file.getParent());
		Files.writeString(file,
				JaxbHelper.serializeToJson(schema, IdContainerSchema.class),
				StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
		updateLists();

	}

	private String computePath(Path path){
		return path.toString() + "/";

	}

	private Path generatePathToFile() {
		return Paths.get(testFolder.toAbsolutePath() + "/" + getUsername() + ".json");
	}

	private Path generatePathToFolder() {
		logger.fine(defaultPath + this.getUsername());
		return Paths.get(defaultPath + this.getUsername());
	}

	public void show(URI file, OutputStream out, Cipher dec) throws Exception{
		byte[] content = Files.readAllBytes(Paths.get(file));
		out.write(content);

	}
	

	public void deleteCredential(String filename, PassStore store) throws Exception {
		HashMap<String, String> map = this.schema.getOwnerSecretStore();
		String s = map.get(filename);
		if (s!=null){
			openResource(filename, store.getDecipher());
			map.remove(filename);
			commitSchema();
			logger.info("Removed: " + filename);

		} else {
			logger.info("Failed to removed: " + filename);

		}
	}

	public void deleteSftpCredential(String uid) throws Exception {
		String xmlFileName = uidToXmlFileName(uid);
		HashMap<String, String> map = this.schema.getOwnerSecretStore();
		if (map.containsKey(xmlFileName)){
			map.remove(xmlFileName);
		} else {
			throw new UxException(ErrorMessages.FILE_NOT_FOUND);

		}
		commitSchema();

	}


	protected IdContainerSchema init(boolean create) throws Exception {
		// Is creating a new container.
		if (create){
			if (!Files.exists(testFolder)){
				Files.createDirectories(testFolder);
				IdContainerSchema t = new IdContainerSchema();
				t.setUsername(getUsername());

				try (BufferedWriter fos = Files.newBufferedWriter(file)){
					String json = JaxbHelper.serializeToJson(t, IdContainerSchema.class);
					logger.fine(json);
					fos.write(json);
					return t;
					
				} catch (Exception e) {
					throw new Exception(ErrorMessages.WRITE_FILE_ERROR, e);
				}
			} else {
				throw new UxException(ErrorMessages.USER_ALREADY_EXISTS);
				
			}
		} else {
			if (Files.exists(testFolder)){
				return JaxbHelper.jsonFileToClass(file, IdContainerSchema.class);
				
			} else {
				throw new UxException(ErrorMessages.USER_DOES_NOT_EXIST, this.getUsername());
				
			}
		}
	}
	
	protected void commitSchema() throws Exception {
		String json = JaxbHelper.serializeToJson(schema, IdContainerSchema.class);
		BufferedWriter writer = Files.newBufferedWriter(this.file);
		writer.write(json);
		writer.flush();
		updateLists();

	}

	// TODO
	@SuppressWarnings("unchecked")
	public void updateLists() {
		localLedgerList = new ArrayList<>(schema.getLocalLedger().keySet());
		issuerSecretList = new ArrayList<>(schema.getIssuerSecretStore().keySet());
		issuanceParameterList = new ArrayList<>(schema.getIssuanceParameterStore().keySet());
		issuancePolicyList = new ArrayList<>(schema.getIssuancePolicyStore().keySet());
		issuedList = new ArrayList<>(schema.getIssuedStore().keySet());
		ownerSecretList = new ArrayList<>(schema.getOwnerSecretStore().keySet());
		noninteractiveTokenList = new ArrayList<>(schema.getNoninteractiveTokenStore().keySet());
		inspectorList = new ArrayList<>(schema.getInspectorStore().keySet());
		revocationAuthList = new ArrayList<>(schema.getRevocationAuthStore().keySet());  

	}
	
	@Override
	public void saveIssuanceToken(IssuanceToken it, String name, Cipher store) throws Exception {
		throw new Exception();
		
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T openResource(String fullFileName, Cipher dec) throws Exception {
		if (!FileType.isXmlDocument(fullFileName)){
			if (FileType.isRulebook(fullFileName)){
				return  openFile(LOCAL_LEDGER, fullFileName, Rulebook.class);

			} else {
				throw new FileSystemException("Resources are all .xml files " + fullFileName);

			}
		} else if (FileType.isIssuerSecret(fullFileName)){
			return openEncryptedFile(ISSUER_SECRET_STORE, fullFileName, SecretKey.class, dec);
			
		} else if (FileType.isInspectorPrivateKey(fullFileName)){
			return openEncryptedFile(ISSUER_SECRET_STORE, fullFileName, SecretKey.class, dec);

		} else if (FileType.isSftp(fullFileName)){
			return openEncryptedXFile(OWNER_PRIVATE_STORE, fullFileName, SFTPLogonData.class, dec);

		} else if (FileType.isKeys(fullFileName)){
			return openXFile(OWNER_PRIVATE_STORE, fullFileName, KeyContainer.class);

		} else if (FileType.isOwnerSecret(fullFileName)){
			return openEncryptedFile(OWNER_PRIVATE_STORE, fullFileName, Secret.class, dec);

		} else if (FileType.isIssuerParameters(fullFileName)){
			return openFile(ISSUER_PARAMETERS_STORE, fullFileName, IssuerParameters.class);

		} else if (FileType.isIssuanceLog(fullFileName)){
			return openFile(ISSUER_ISSUED, fullFileName, IssuanceLogEntry.class);
			
		} else if (FileType.isSystemParameters(fullFileName)){
			return (T) openSystemParameters();
			
		} else if (FileType.isPresentationPolicyAlternatives(fullFileName)){
			return openXFile(LOCAL_LEDGER, fullFileName, PresentationPolicyAlternativesAdapter.class);
			
		} else if (FileType.isCredentialSpecification(fullFileName)){
			return openFile(LOCAL_LEDGER, fullFileName, CredentialSpecification.class);

		} else if (FileType.isIssuancePolicy(fullFileName)){
			return openFile(ISSUANCE_POLICY_STORE, fullFileName, IssuancePolicy.class);

		} else if (FileType.isProofToken(fullFileName)){
			return openFile(NONINTERACTIVE_TOKENS, fullFileName, PresentationToken.class);
			
		} else if (FileType.isInspectorPublicKey(fullFileName)){
			return openFile(INSPECTOR_STORE, fullFileName, InspectorPublicKey.class);

		} else if (FileType.isRevocationAuthority(fullFileName)){
			return openFile(REVOCATION_AUTH_STORE, fullFileName, RevocationAuthorityParameters.class);

		} else if (FileType.isRevocationInformation(fullFileName)){
			return openFile(REVOCATION_AUTH_STORE, fullFileName, RevocationInformation.class);
			
		} else if (FileType.isCredential(fullFileName)){ // Local
			return openEncryptedFile(OWNER_PRIVATE_STORE, fullFileName, Credential.class, dec);
			
		} else if (FileType.isRevocationAuthorityPrivateKey(fullFileName)){ // Local
			return openEncryptedFile(REVOCATION_AUTH_STORE, fullFileName, PrivateKey.class, dec);

		} else if (FileType.isRevocationHistory(fullFileName)){ // Local
			return openFile(REVOCATION_AUTH_STORE, fullFileName, RevocationHistory.class);

		} else if (FileType.isIssuancePolicy(fullFileName)){ // Local
			return  openFile(ISSUANCE_POLICY_STORE, fullFileName, IssuancePolicy.class);

		} else if (FileType.isPresentationPolicy(fullFileName)){ // Local
			return  openFile(ISSUANCE_POLICY_STORE, fullFileName, PresentationPolicy.class);

		} else if (FileType.isRulebook(fullFileName)){ // Local
			return  openFile(LOCAL_LEDGER, fullFileName, Rulebook.class);

		} else {
			throw new FileSystemException("File type not recognized " + fullFileName);
			
		}
	}

	public static SystemParameters openSystemParameters() throws Exception {
		InputStream stream = ClassLoader.getSystemResourceAsStream("lambda.xml");
		byte[] b = new byte[stream.available()];
		stream.read(b);
		String s = new String(b, StandardCharsets.UTF_8);
		return JaxbHelper.xmlToClass(s, SystemParameters.class);
		
	}


	@SuppressWarnings("unchecked")
	private <T> T openEncryptedFile(URI location, String fullFileName, Class<?> clazz, Cipher dec) throws Exception {
		try {
			if (dec!=null){
				HashMap<String, String> l = computeLocation(location);
				String encB64 = l.get(fullFileName);
				if (encB64!=null){
					byte[] xml = dec.doFinal(Base64.decodeBase64(encB64.getBytes()));
					ByteArrayInputStream is = new ByteArrayInputStream(xml);
					JAXBElement<?> resourceAsJaxbElement = JaxbHelperClass.deserialize(is, true);
					return (T)JAXBIntrospector.getValue(resourceAsJaxbElement);

				} else {
					throw new Exception(ErrorMessages.FILE_NOT_FOUND);

				}
			} else {
				throw new UxException(ErrorMessages.INCORRECT_PARAMETERS);

			}
		} catch (BadPaddingException e){
			throw new UxException(ErrorMessages.INVALID_PASSWORD);

		}
	}

	private <T> T openEncryptedXFile(URI location, String fullFileName, Class<?> clazz, Cipher dec) throws Exception {
		logger.log(Level.FINE,"Getting file from schema object: " + schema);
		HashMap<String, String> l = computeLocation(location);
		String encB64 = l.get(fullFileName);
		if (encB64!=null){
			byte[] xml = dec.doFinal(Base64.decodeBase64(encB64.getBytes()));
			logger.log(Level.FINE,fullFileName + "\n" + new String(xml));
			return (T)JaxbHelper.xmlToClass(new String(xml), clazz);

		} else {
			throw new Exception("The file does not exist " + fullFileName + " in container " + this.getUsername());

		}
	}
	
	@SuppressWarnings("unchecked")
	private <T> T openFile(URI location, String fullFileName, Class<?> clazz) throws Exception {
		HashMap<String, String> l = computeLocation(location);
		String encB64 = l.get(fullFileName);
		if (encB64!=null){
			byte[] xml = Base64.decodeBase64(encB64.getBytes());
			if (clazz.equals(Rulebook.class)){
				return (T) JaxbHelper.jsonToClass(new String(xml, StandardCharsets.UTF_8), Rulebook.class);

			} else {

				ByteArrayInputStream is = new ByteArrayInputStream(xml);
				JAXBElement<?> resourceAsJaxbElement = JaxbHelperClass.deserialize(is, true);
				logger.log(Level.FINE,fullFileName + "\n" + new String(xml));
				return (T)JAXBIntrospector.getValue(resourceAsJaxbElement);

			}
		} else {
			throw new FileNotFoundException("The file does not exist " + fullFileName + " in container " + this.getUsername());
			
		}
	}





	@SuppressWarnings("unchecked")
	private <T> T openXFile(URI location, String fullFileName, Class<?> clazz) throws Exception {
		logger.log(Level.FINE,"Getting file from schema object: " + schema);
		HashMap<String, String> l = computeLocation(location);
		String encB64 = l.get(fullFileName);
		if (encB64!=null){
			byte[] xml = Base64.decodeBase64(encB64.getBytes());
			logger.log(Level.FINE,fullFileName + "\n" + new String(xml));
			return (T)JaxbHelper.xmlToClass(new String(xml), clazz);
			
		} else {
			throw new Exception("The file does not exist " + fullFileName + " in container " + this.getUsername());
			
		}
	}


	@Override
	protected void saveEncrypted(String xml, URI location, String name, boolean overwrite, Cipher store) throws Exception {
		String b64 = Base64.encodeBase64String(store.doFinal(xml.getBytes()));
		put(b64, name, computeLocation(location), overwrite);
		commitSchema();
		
	}
	
	@Override
	protected void save(String xml, URI location, String name, boolean overwrite) throws Exception {
		String b64 = Base64.encodeBase64String(xml.getBytes());
		put(b64, name, computeLocation(location), overwrite);
		commitSchema();
		
	}	
	
	protected HashMap<String, String> computeLocation(URI location) throws Exception {
		if (location.equals(LOCAL_LEDGER)){
			return schema.getLocalLedger();
			
		} else if (location.equals(ISSUER_SECRET_STORE)){
			return schema.getIssuerSecretStore();
			
		} else if (location.equals(ISSUER_PARAMETERS_STORE)){
			return schema.getIssuanceParameterStore();
			
		} else if (location.equals(RUNTIME_KEYS)){
			return schema.getOwnerSecretStore();
			
		} else if (location.equals(ISSUANCE_POLICY_STORE)){
			return schema.getIssuancePolicyStore();
			
		} else if (location.equals(ISSUER_ISSUED)){
			return schema.getIssuedStore();
			
		} else if (location.equals(OWNER_PRIVATE_STORE)){
			return schema.getOwnerSecretStore();
			
		} else if (location.equals(NONINTERACTIVE_TOKENS)){
			return schema.getNoninteractiveTokenStore();
			
		} else if (location.equals(INSPECTOR_STORE)){
			return schema.getInspectorStore();
			
		} else if (location.equals(REVOCATION_AUTH_STORE)){
			return schema.getRevocationAuthStore();

		} else {
			throw new HubException("Programming Error - Case for " + location + " not handled.");
			
		}
	}	

	private void put(String b64, String name, HashMap<String, String> map, boolean overwrite) throws UxException {
		if (overwrite){
			map.put(name, b64);
			
		} else {
			String f = map.putIfAbsent(name, b64);
			if (f !=null && !overwrite){
				throw new UxException("The XML already exists: " + name);
				
			}
		}
	}

	@Override
	public void delete() throws Exception {
		Files.deleteIfExists(this.file);
		Files.deleteIfExists(this.testFolder);

	}

	public IdContainerSchema getSchema() {
		return schema;
		
	}
}