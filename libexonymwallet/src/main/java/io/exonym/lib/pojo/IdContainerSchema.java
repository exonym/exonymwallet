package io.exonym.lib.pojo;

import java.util.HashMap;

public class IdContainerSchema {
	
	private String username;

	private String deviceId;

	private byte[] appPublicKey;

	public IdContainerSchema() {

	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	private HashMap<String, String> localLedger = new HashMap<>();

	private HashMap<String, String> issuerSecretStore = new HashMap<>();
	
	private HashMap<String, String> issuanceParameterStore = new HashMap<>();
	
	private HashMap<String, String> issuancePolicyStore = new HashMap<>();
	
	private HashMap<String, String> issuedStore = new HashMap<>();
	
	private HashMap<String, String> ownerSecretStore = new HashMap<>();
	
	private HashMap<String, String> noninteractiveTokenStore = new HashMap<>();
	
	private HashMap<String, String> inspectorStore = new HashMap<>();
	
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
