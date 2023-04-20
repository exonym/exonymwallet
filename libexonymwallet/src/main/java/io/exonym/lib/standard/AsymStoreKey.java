package io.exonym.lib.standard;

import org.apache.commons.codec.binary.Base64;
import io.exonym.lib.exceptions.ErrorMessages;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.MGF1ParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.logging.Logger;

public class AsymStoreKey implements Serializable{
	
	private final static Logger logger = Logger.getLogger(AsymStoreKey.class.getName());
	private static final long serialVersionUID = 1L;
	private PrivateKey privateKey = null;
	private PublicKey publicKey = null;
	public static int MODE_BLANK = 0; 
	public static int MODE_GENERATE = 1;
	
	private AsymStoreKey(int MODE) {
		try {
			if (MODE == MODE_GENERATE){
				KeyPairGenerator keyGen = KeyPairGenerator.getInstance(Const.ASYM_ENCRYPTION_ALGORITHM);
				keyGen.initialize(2048);
				KeyPair keyPair = keyGen.generateKeyPair();
				privateKey = keyPair.getPrivate();
				publicKey = keyPair.getPublic();

			} 
		} catch (NoSuchAlgorithmException e) {
			logger.throwing("AsymStoreKey.class", "AsymStoreKey()", e);

		}
	}

	public static AsymStoreKey tryKey(byte[] privateKeyBytes, Cipher dec){
		try {
			AsymStoreKey key = new AsymStoreKey(AsymStoreKey.MODE_BLANK);
			key.privateKey = key.assemblePrivateKey(privateKeyBytes, dec);
			if (key.privateKey==null){
				key = null; 
				
			}
			return key; 
			
		} catch (Exception e) {
			return null; 
			
		}
	} 
	
	public static AsymStoreKey build(byte[] publicKeyBytes, byte[] privateKeyBytes, Cipher dec) throws Exception{
		AsymStoreKey result = new AsymStoreKey(AsymStoreKey.MODE_BLANK);

		result.publicKey = KeyFactory.getInstance("RSA")
				.generatePublic(new X509EncodedKeySpec(publicKeyBytes));

		result.privateKey = KeyFactory.getInstance("RSA")
				.generatePrivate(new PKCS8EncodedKeySpec(dec.doFinal(privateKeyBytes)));;

		return result;
		
	}
	
	public PublicKey getPublicKey() {
		return publicKey;
		
	}
	
	/**
	 * Writes the key to the respective output streams with
	 * the bytes Base64 encoded.
	 *  
	 * encrypting  
	 * @throws Exception
	 */
	public void save(Cipher skEncCipher, OutputStream pkStream, OutputStream skStream) throws Exception{
		if (skEncCipher==null){
			throw new Exception("Cipher cannot be null");
			
		} if (pkStream == null || skStream==null){
			throw new Exception("Output stream was null");
			
		}
		byte[] pkb = Base64.encodeBase64(this.getPublicKey().getEncoded());
		pkStream.write(pkb);
		
		byte[] skb = Base64.encodeBase64(this.getEncryptedEncodedForm(skEncCipher));
		skStream.write(skb);
		
	}
	
	public static AsymStoreKey open(Cipher skDecCipher, InputStream pk, InputStream sk) throws Exception{
		if (skDecCipher==null){
			throw new Exception("Cipher cannot be null");
			
		} if (pk == null || sk == null){ 
			throw new Exception("Null input stream");
			
		}
		byte[] pkb = new byte[pk.available()];
		pk.read(pkb);
		
		byte[] skb = new byte[sk.available()];
		sk.read(skb);
		
		return AsymStoreKey.build(Base64.decodeBase64(pkb), Base64.decodeBase64(skb), skDecCipher);
		
	}

	public static AsymStoreKey open(InputStream pk) throws Exception{
		if (pk == null){ 
			throw new Exception("Null input stream");
		
		}
		byte[] pkb = new byte[pk.available()];
		pk.read(pkb);
		
		AsymStoreKey key = AsymStoreKey.blank();
		key.assembleKey(Base64.decodeBase64(pkb));
		return key; 
		
	}
	
	public byte[] getEncryptedEncodedForm(String password) throws Exception {
		Cipher cipher = CryptoUtils.generatePasswordCipher(Cipher.ENCRYPT_MODE, password, null);
		return cipher.doFinal(privateKey.getEncoded());

	}

	public byte[] getEncryptedEncodedForm(Cipher encCipher) throws Exception {
		return encCipher.doFinal(privateKey.getEncoded());
		
	}
	
	public void assembleKey(byte[] publicPart) throws InvalidKeySpecException, NoSuchAlgorithmException {
		publicKey = KeyFactory.getInstance("RSA")
				.generatePublic(new X509EncodedKeySpec(publicPart));
		
	}
	
	public void assembleKey(byte[] publicPart, byte[] privatePart, Cipher dec) throws Exception {
		publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(publicPart));
		privateKey = assemblePrivateKey(privatePart, dec);
		
	}

	public void assembleKey(byte[] publicPart, byte[] privatePart) throws Exception {
		publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(publicPart));
		privateKey = assemblePrivateKey(privatePart);

	}

	private PrivateKey assemblePrivateKey(byte[] privatePart) throws Exception {
		return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(privatePart));
	}

	private PrivateKey assemblePrivateKey(byte[] privatePart, Cipher cipher) throws Exception {
		byte[] result = cipher.doFinal(privatePart);
		return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(result));
		
	}

	public void assembleKey(PublicKey pk) throws Exception {
		publicKey = pk;

	}	

	public void assembleKey(PrivateKey sk) throws Exception {
		privateKey = sk;
		
	}	
	
	public byte[] encrypt(byte[] bytes){
		try {
			Cipher encode = Cipher.getInstance(Const.ASYM_STANDARD_CIPHER_ALGORITHM);
			OAEPParameterSpec oaepParams = new OAEPParameterSpec("SHA-256", "MGF1",
					new MGF1ParameterSpec("SHA-256"), PSource.PSpecified.DEFAULT);
			encode.init(Cipher.ENCRYPT_MODE, publicKey, oaepParams);
			return encode.doFinal(bytes);
			
		} catch (Exception e) {
			logger.throwing("AsymStoreKey.class", "encrypt()", e);
			return null; 
			
		}
	}

	public static byte[] encrypt(byte[] bytes, PublicKey publicKey) throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException,
			NoSuchPaddingException, InvalidAlgorithmParameterException {
		try {
			Cipher encode = Cipher.getInstance(Const.ASYM_STANDARD_CIPHER_ALGORITHM);
			OAEPParameterSpec oaepParams = new OAEPParameterSpec("SHA-256", "MGF1",
					new MGF1ParameterSpec("SHA-256"), PSource.PSpecified.DEFAULT);
			encode.init(Cipher.ENCRYPT_MODE, publicKey, oaepParams);
			return encode.doFinal(bytes);

		} catch (Exception e) {
			throw e;

		}
	}

	public byte[] decipher(byte[] bytes) throws Exception {
		try {
			if (this.privateKey==null){
				throw new Exception("Private Key is null:  you might mean decipherWithPublicKey()");

			}
			if (bytes!=null){
				logger.fine("byte length at decipher" + bytes.length);
			}
			
			if (bytes==null || bytes.length==0){
				return null;

			}
			Cipher decipher = Cipher.getInstance(Const.ASYM_STANDARD_CIPHER_ALGORITHM);
			OAEPParameterSpec oaepParams = new OAEPParameterSpec("SHA-256", "MGF1",
					new MGF1ParameterSpec("SHA-256"), PSource.PSpecified.DEFAULT);
			decipher.init(Cipher.DECRYPT_MODE, privateKey, oaepParams);//*/
			return decipher.doFinal(bytes);

		} catch (Exception e) {
			throw e;

		}
	}

	public byte[] encryptWithPrivateKey(byte[] bytes){
		try {
			Cipher encode = Cipher.getInstance(Const.ASYM_ENCRYPTION_ALGORITHM);
			encode.init(Cipher.ENCRYPT_MODE, privateKey);
			return encode.doFinal(bytes);
			
		} catch (Exception e) {
			logger.throwing("AsymStoreKey.class", "encryptWithPrivateKey()", e);
			return null; 
			
		}
	}

	public byte[] decipherWithPublicKey(byte[] bytes) throws Exception {
		try {
			if (bytes==null || bytes.length==0){
				return null; 
				
			}
			Cipher decipher = Cipher.getInstance(Const.ASYM_ENCRYPTION_ALGORITHM);
			decipher.init(Cipher.DECRYPT_MODE, publicKey);//*/ 
			return decipher.doFinal(bytes);
			
		} catch (Exception e) {
			throw e; 
			
		}
	}	
	
	/**
	 * 
	 * @param bytesToSign the complete bytes to sign
	 * @return
	 */
	public byte[] sign(byte[] bytesToSign){
		try {
			Signature signature = Signature.getInstance("SHA256withRSA");
			signature.initSign(privateKey);
			signature.update(bytesToSign);
			return signature.sign();

		} catch (Exception e) {
			logger.throwing("AsymStoreKey.class", "sign()", e);
			return null; 
			
		}		
	}

	public boolean verifySignature(byte[] source, byte[] sig) throws Exception {
		try {
			Signature signature = Signature.getInstance("SHA256withRSA");
			signature.initVerify(publicKey);
			signature.update(source);

			if (!signature.verify(sig)) {
				throw new Exception(ErrorMessages.FAILED_TO_AUTHORIZE);

			} else {
				return true;

			}
		} catch (Exception e) {
			throw e; 
			
		}
	}
	
	public AsymStoreKey() {
		try {
			KeyPairGenerator keyGen = KeyPairGenerator.getInstance(Const.ASYM_ENCRYPTION_ALGORITHM);
			keyGen.initialize(2048);
			KeyPair keyPair = keyGen.generateKeyPair();
			privateKey = keyPair.getPrivate();
			publicKey = keyPair.getPublic();
			
		} catch (NoSuchAlgorithmException e) {
			logger.throwing("AsymStoreKey.class", "AsymStoreKey()", e);

		}
	}
	
	public boolean isKeyPair(){
		return this.privateKey!=null;
		
	}
	
	public static AsymStoreKey blank(){
		return new AsymStoreKey(AsymStoreKey.MODE_BLANK);
		
	}

}