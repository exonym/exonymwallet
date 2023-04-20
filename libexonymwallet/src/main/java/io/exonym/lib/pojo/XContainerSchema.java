package io.exonym.lib.pojo;

import java.util.HashMap;

//@XmlRootElement(name="XContainerSchema")
//@XmlType(name = "XContainerSchema", namespace = Namespace.EX)
public class XContainerSchema {
	
//	@XmlElement(name = "Username", namespace = Namespace.EX)
	private String username;

	// paywall
//	@XmlElement(name = "DeviceId", namespace = Namespace.EX)
	private String deviceId;

//	@XmlElement(name = "AppPublicKey", namespace = Namespace.EX)
	private byte[] appPublicKey;

	public XContainerSchema() {

	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

//	@XmlElement(name = "LocalLedger", namespace = Namespace.EX)
	private HashMap<String, String> localLedger = new HashMap<>();

//	@XmlElement(name = "Issuer", namespace = Namespace.EX)
	private HashMap<String, String> issuerSecretStore = new HashMap<>();
	
//	@XmlElement(name = "IssuerParameters", namespace = Namespace.EX)
	private HashMap<String, String> issuanceParameterStore = new HashMap<>();
	
//	@XmlElement(name = "IssuancePolicy", namespace = Namespace.EX)
	private HashMap<String, String> issuancePolicyStore = new HashMap<>();
	
//	@XmlElement(name = "IssuerLogs", namespace = Namespace.EX)
	private HashMap<String, String> issuedStore = new HashMap<>();
	
//	@XmlElement(name = "Credentials", namespace = Namespace.EX)
	private HashMap<String, String> ownerSecretStore = new HashMap<>();
	
//	@XmlElement(name = "Tokens", namespace = Namespace.EX)
	private HashMap<String, String> noninteractiveTokenStore = new HashMap<>();
	
//	@XmlElement(name = "Inspector", namespace = Namespace.EX)
	private HashMap<String, String> inspectorStore = new HashMap<>();
	
//	@XmlElement(name = "Revocation", namespace = Namespace.EX)
	private HashMap<String, String> revocationAuthStore = new HashMap<>();

	public HashMap<String, String> getLocalLedger() {
		return localLedger;
	}

	public void setLocalLedger(HashMap<String, String> localLedger) {
		this.localLedger = localLedger;
	}

	public HashMap<String, String> getIssuerSecretStore() {
		return issuerSecretStore;
	}

	public void setIssuerSecretStore(HashMap<String, String> issuerSecretStore) {
		this.issuerSecretStore = issuerSecretStore;
	}

	public HashMap<String, String> getIssuanceParameterStore() {
		return issuanceParameterStore;
	}

	public void setIssuanceParameterStore(HashMap<String, String> issuanceParameterStore) {
		this.issuanceParameterStore = issuanceParameterStore;
	}

	public HashMap<String, String> getIssuancePolicyStore() {
		return issuancePolicyStore;
	}

	public void setIssuancePolicyStore(HashMap<String, String> issuancePolicyStore) {
		this.issuancePolicyStore = issuancePolicyStore;
	}

	public HashMap<String, String> getIssuedStore() {
		return issuedStore;
	}

	public void setIssuedStore(HashMap<String, String> issuedStore) {
		this.issuedStore = issuedStore;
	}

	public HashMap<String, String> getOwnerSecretStore() {
		return ownerSecretStore;
	}

	public void setOwnerSecretStore(HashMap<String, String> ownerSecretStore) {
		this.ownerSecretStore = ownerSecretStore;
	}

	public HashMap<String, String> getNoninteractiveTokenStore() {
		return noninteractiveTokenStore;
	}

	public void setNoninteractiveTokenStore(HashMap<String, String> noninteractiveTokenStore) {
		this.noninteractiveTokenStore = noninteractiveTokenStore;
	}

	public HashMap<String, String> getInspectorStore() {
		return inspectorStore;
	}

	public void setInspectorStore(HashMap<String, String> inspectorStore) {
		this.inspectorStore = inspectorStore;
	}

	public HashMap<String, String> getRevocationAuthStore() {
		return revocationAuthStore;
	}

	public void setRevocationAuthStore(HashMap<String, String> revocationAuthStore) {
		this.revocationAuthStore = revocationAuthStore;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public byte[] getAppPublicKey() {
		return appPublicKey;
	}

	public void setAppPublicKey(byte[] appPublicKey) {
		this.appPublicKey = appPublicKey;
	}
}
