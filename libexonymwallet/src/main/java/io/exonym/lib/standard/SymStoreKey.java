package io.exonym.lib.standard;

import eu.abc4trust.smartcard.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.Serializable;
import java.net.ConnectException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.EmptyStackException;
import java.util.logging.Logger;


public class SymStoreKey implements Serializable{

	private static final long serialVersionUID = 1L;

	private final static Logger logger = Logger.getLogger(SymStoreKey.class.getName());

	private SecretKey key, macKey;


	public SymStoreKey() {
		this.key = genSecretKey();
		KeyGenerator keygen;
		
		try {
			keygen = KeyGenerator.getInstance(Const.MESSAGE_AUTHENTICATION_CODE_ALGORITHM);
			macKey = keygen.generateKey();
			
		} catch (NoSuchAlgorithmException e) {
			logger.throwing("SymStoreKey.class", "SymStoreKey()", e);
			
		}
	}
	
	public SymStoreKey(SecretKey key, SecretKey macKey){
		this.key=key;
		this.macKey=macKey;
		
	}
	
	public SymStoreKey(byte[] key, byte[] mac){
		this.key = new SecretKeySpec(key, 0, key.length, Const.SYM_ENCRYPTION_ALGORITHM);
		this.macKey = new SecretKeySpec(mac, 0, mac.length, Const.MESSAGE_AUTHENTICATION_CODE_ALGORITHM);
		
	}
	
	public static SymStoreKey build(String b64Encoded) throws Exception {
		if (b64Encoded!=null){
			String[] parts = b64Encoded.split(":");
			
			if (parts.length==2){
				byte[] sk = Base64.decode(parts[0].getBytes());
				byte[] mac = Base64.decode(parts[1].getBytes());
				return new SymStoreKey(sk, mac);
				
			} else {
				throw new Exception("Bad length");
				
			}
		} else {
			throw new Exception("Null content");
			
		}
	}
	
	public String getBase64Encoded(){
		byte[] s = key.getEncoded();
		byte[] m = macKey.getEncoded();
		String ss = Base64.encodeBytes(s);
		String ms = Base64.encodeBytes(m);
		return ss + ":" + ms;
		
	}
	
	public static SecretKey genSecretKey(){
		try {
			KeyGenerator keyGen = KeyGenerator.getInstance(Const.SYM_ENCRYPTION_ALGORITHM);
			keyGen.init(Const.SYM_KEY_SIZE, new SecureRandom());
			return keyGen.generateKey();
			
		} catch (NoSuchAlgorithmException e) {
			logger.throwing("SymStoreKey.class", "genSecretKey()", e);
			return null;
			
		}
	}

	public byte[] encrypt(byte[] raw){
		try {
			Cipher cipher = Cipher.getInstance(Const.SYM_ENCRYPTION_ALGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, key); //*/
			
			Mac mac = Mac.getInstance(Const.MESSAGE_AUTHENTICATION_CODE_ALGORITHM);
			mac.init(macKey);
			byte[] message = cipher.doFinal(raw);
			byte[] macBytes = mac.doFinal(message);
			
			byte[] result = new byte[message.length + macBytes.length + 1];
			result[0] = (byte) macBytes.length;
			
			for (int i = 1; i < result.length; i++) {
				if (i < (macBytes.length+1)){
					result[i] = macBytes[i-1];
					
				} else {
					result[i] = message[i-macBytes.length-1];
					
				}
			}

			return result;
			
		} catch (Exception e) {
			logger.throwing("SymStoreKey.class", "encrypt()", e);
			return null; 
			
		}
	}	
	
	public byte[] decipher(byte[] raw) throws Exception{	
		try {
			if (raw==null){
				throw new Exception("Null raw data passed ");
				
			} if (raw.length==0){
				throw new EmptyStackException();
				
			}
			
			byte[] macIn = new byte[raw[0]];
			byte[] msgIn = new byte[raw.length-raw[0]-1];

			for (int i = 1; i < raw.length; i++) {
				if (i<=raw[0]){
					macIn[i-1]=raw[i];
				} else {
					msgIn[i-raw[0]-1] = raw[i];
				}
			}
			
			// Checking mac
			
			Mac mac = Mac.getInstance(Const.MESSAGE_AUTHENTICATION_CODE_ALGORITHM);
			mac.init(macKey);
			mac.update(msgIn);
			byte[] generatedMac = mac.doFinal(); 
			
			if (generatedMac.length==macIn.length){
				int noMatch = 0;  
				for (int i = 0; i < generatedMac.length; i++) {
					if (macIn[i]!=generatedMac[i]){
						noMatch++; 
					}
				}
				if (noMatch>0){
					throw new SecurityException("Message Authentication Code Failure - Bad Content " + noMatch);
					
				}
				Cipher decipher = Cipher.getInstance(Const.SYM_ENCRYPTION_ALGORITHM);
				decipher.init(Cipher.DECRYPT_MODE, key);//*/
				return decipher.doFinal(msgIn);
				
			} else {
				throw new SecurityException("Message Authentication Failure - Bad Size");
				
			} 
			
		} catch (EmptyStackException e){
			logger.throwing("SymStoreKey.class", "decipher()", e);
			throw new ConnectException("Connection dropped");
			
		} catch (SecurityException e) {
			throw e;
			
		} catch (Exception e) {
			throw e;
			
		}
	}

	public SecretKey getKey() {
		return key;
	}

	public SecretKey getMacKey() {
		return macKey;
	}

}
