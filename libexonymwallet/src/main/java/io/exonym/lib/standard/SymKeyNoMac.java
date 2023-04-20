package io.exonym.lib.standard;


import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.Serializable;
import java.security.SecureRandom;
import java.util.logging.Logger;

public class SymKeyNoMac implements Serializable{

	private final static Logger logger = Logger.getLogger(SymKeyNoMac.class.getName());

	private static final long serialVersionUID = 1L;
	private SecretKey key;
	
	public SymKeyNoMac() {
		try {
			KeyGenerator keyGen = KeyGenerator.getInstance(Const.SYM_ENCRYPTION_ALGORITHM);
			keyGen.init(128, new SecureRandom());
			this.key = keyGen.generateKey();	 

		} catch (Exception e) {
			logger.throwing("SymKeyNoMac.class", "SymKeyNoMac()", e);
		}
		
	}
	
	public byte[] encrypt(byte[] raw) throws Exception{
		try {
			Cipher cipher = Cipher.getInstance(Const.SYM_ENCRYPTION_ALGORITHM);
			cipher.init(Cipher.ENCRYPT_MODE, key); //*/
			return cipher.doFinal(raw);
			
		} catch (Exception e) {
			throw e;  
			
		}
	}	
	
	public byte[] decipher(byte[] raw) throws Exception{	
		try {
			Cipher decipher = Cipher.getInstance(Const.SYM_ENCRYPTION_ALGORITHM);
			decipher.init(Cipher.DECRYPT_MODE, key);//*/
			return decipher.doFinal(raw);
				
		} catch (Exception e) {
			throw e; 
			
		}
	}		
}
