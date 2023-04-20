package io.exonym.lib.standard;

import io.exonym.lib.exceptions.ErrorMessages;
import io.exonym.lib.exceptions.UxException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public final class PassStore {

	private final static Logger logger = Logger.getLogger(PassStore.class.getName());
	private String username;
	private final String oracle;
	private final Cipher encrypt;
	private final Cipher decipher;

	public PassStore(byte[] passwordHash) throws Exception {
		String sha256 = Form.toHex(passwordHash);
		encrypt = CryptoUtils.generatePasswordCipher(Cipher.ENCRYPT_MODE, sha256, null);
		decipher = CryptoUtils.generatePasswordCipher(Cipher.DECRYPT_MODE, sha256, null);
		oracle = CryptoUtils.computeSha256HashAsHex(sha256);

	}

	/**
	 * Returns the recovery BigInteger to convert to unsigned byte array.
	 *
	 * @param plainTextPassword
	 * @return
	 */
	public static BigInteger initNew(String plainTextPassword){
		return CryptoUtils.computeSha256HashAsBigInteger(plainTextPassword.getBytes(StandardCharsets.UTF_8));

	}

	public PassStore(String plainText, boolean validate) throws Exception {
		if (validate){
			validatePassword(plainText);
			
		}
		if (plainText==null){
			throw new NullPointerException();

		}
		String sha256 = CryptoUtils.computeSha256HashAsHex(plainText);
		encrypt = CryptoUtils.generatePasswordCipher(Cipher.ENCRYPT_MODE, sha256, null);
		decipher = CryptoUtils.generatePasswordCipher(Cipher.DECRYPT_MODE, sha256, null);
		oracle = CryptoUtils.computeSha256HashAsHex(sha256);

	}
	
	/**
	 * Verify plain text password is the same as the plain text 
	 * password used to create these ciphers.
	 *  
	 * @param plainText
	 * @return
	 * @throws UxException
	 */
	public synchronized boolean verifyPassword(String plainText) throws UxException {
		String sha256 = CryptoUtils.computeSha256HashAsHex(plainText);
		sha256 = CryptoUtils.computeSha256HashAsHex(sha256);
		if (sha256.equals(oracle)){
			return true; 
			
		} else {
			throw new UxException(ErrorMessages.PASSWORD_DID_NOT_MATCH);
			
		}
	}
	
	/**
	 * Encrypt bytes with this password store.
	 * 
	 * @param bytes
	 * @return
	 * @throws BadPaddingException 
	 * @throws IllegalBlockSizeException 
	 */
	public synchronized byte[] encrypt(byte[] bytes) throws IllegalBlockSizeException, BadPaddingException{
		if (bytes==null || bytes.length==0){
			throw new RuntimeException("You are trying to encrypt empty bytes.");
			
		}
		return encrypt.doFinal(bytes);
		
	}
	
	public synchronized Cipher getEncrypt() {
		return encrypt;
	}

	public synchronized Cipher getDecipher() {
		return decipher;
	}

	/**
	 * Decipher bytes with this password store.
	 * 
	 * @param bytes
	 * @return
	 * @throws BadPaddingException 
	 * @throws IllegalBlockSizeException 
	 */
	public synchronized byte[] decipher(byte[] bytes) throws IllegalBlockSizeException, UxException{
		try {
			if (bytes==null || bytes.length==0){
				logger.warning("You are trying to decipher empty bytes.");
				return null; 
				
			}
			return decipher.doFinal(bytes);

		} catch (BadPaddingException e) {
			throw new UxException(ErrorMessages.INVALID_PASSWORD);
			
		}
	}

	public synchronized String getUsername() {
		if (username==null){
			throw new RuntimeException("SET_USERNAME");
		}
		return username;
	}

	public synchronized void setUsername(String username) {
		this.username = username;
	}

	public synchronized  void validatePassword(String password) throws UxException{
		boolean valid = true; 
		
		if (!WhiteList.containsLowerCaseLetters(password)){
			valid = false; 
			
		} if (!WhiteList.containsNumbers(password)){
			valid = false; 
			
		} if (!WhiteList.isMinLettersAllowsNumbers(password, 7)){
			valid = false; 
			
		} if (password.length() < 14 && !WhiteList.containsUpperCaseLetters(password)){
			valid = false; 

		} if (!valid){
			throw new UxException(ErrorMessages.PASSWORD_L7_UPPER_LOWER_ONE_NUMBER);
			
		}
	}
	
	public static AsymStoreKey assembleKeyPair(String password, byte[] publicKey, byte[] privateKey) throws Exception{
		AsymStoreKey key = AsymStoreKey.blank();
		PassStore store = new PassStore(password, false);
		key.assembleKey(publicKey, privateKey, store.getDecipher());
		return key; 
		
	}
}
