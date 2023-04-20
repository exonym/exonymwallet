package io.exonym.lib.pojo;

import io.exonym.lib.helpers.AbstractCouchDbObject;
import io.exonym.lib.standard.AsymStoreKey;
import io.exonym.lib.standard.CryptoUtils;

import javax.crypto.Cipher;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;

@XmlRootElement(name="XKey", namespace=Namespace.EX)
public class XKey extends AbstractCouchDbObject {
	
	private URI keyUid;
	private byte[] publicKey;
	private byte[] privateKey;
	private byte[] keyOut;
	private byte[] keyIn;
	private String hash;

	private byte[] signature;

	private String expiryDateUtc;

	public XKey() {
		this.setType("xkey");

	}

	@XmlElement(name="PublicKey", namespace=Namespace.EX)
	public byte[] getPublicKey() {
		return publicKey;
	}

	@XmlElement(name="PrivateKey", namespace=Namespace.EX)
	public byte[] getPrivateKey() {
		return privateKey;
	}

	public void setPublicKey(byte[] publicKey) {
		this.publicKey = publicKey;
	}

	public void setPrivateKey(byte[] privateKey) {
		this.privateKey = privateKey;
	}

	@XmlElement(name="KeyUID", namespace=Namespace.EX)
	public URI getKeyUid() {
		return keyUid;
	}

	@XmlElement(name="ExpiryDateUtc", namespace=Namespace.EX)
	public String getExpiryDateUtc() {
		return expiryDateUtc;
		
	}

	public void setExpiryDateUtc(String expiryDateUtc) {
		this.expiryDateUtc = expiryDateUtc;
		
	}

	public void setKeyUid(URI keyUid) {
		this.keyUid = keyUid;
		
	}
	@XmlElement(name="Signature", namespace=Namespace.EX)
	public byte[] getSignature() {
		return signature;
		
	}

	public void setSignature(byte[] signature) {
		this.signature = signature;
		
	}

	@XmlElement(name="KeyOut", namespace=Namespace.EX)
	public byte[] getKeyOut() {
		return keyOut;
	}

	public void setKeyOut(byte[] keyOut) {
		this.keyOut = keyOut;
	}

	@XmlElement(name="KeyIn", namespace=Namespace.EX)
	public byte[] getKeyIn() {
		return keyIn;
	}

	public void setKeyIn(byte[] keyIn) {
		this.keyIn = keyIn;
	}

	@XmlElement(name="HexSha256", namespace=Namespace.EX)
	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public static XKey createNew(String password) throws Exception{
		XKey result = new XKey();
		AsymStoreKey key = new AsymStoreKey();
		result.setPrivateKey(key.getEncryptedEncodedForm(password));
		result.setPublicKey(key.getPublicKey().getEncoded());
		return result;

	}

	public static AsymStoreKey assembleAsym(String password, XKey k) throws Exception{
		AsymStoreKey key = AsymStoreKey.blank();
		Cipher dec = CryptoUtils.generatePasswordCipher(Cipher.DECRYPT_MODE, password, null);
		key.assembleKey(k.getPublicKey(), k.getPrivateKey(), dec);
		return key;

	}

	public String toString() {
		return this.keyUid.toString();
		
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof XKey) {
			XKey k = (XKey)o; 
			return k.getKeyUid().equals(this.keyUid);
			
		} else {
			return false; 
		}
	}
}
