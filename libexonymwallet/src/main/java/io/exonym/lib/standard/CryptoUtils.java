package io.exonym.lib.standard;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.logging.Logger;

public class CryptoUtils {

	private final static Logger logger = Logger.getLogger(CryptoUtils.class.getName());

	public static String computeSha256HashAsHex(String string)  {

		try {
			return computeSha256HashAsHex(string.getBytes("UTF-8"));
			
		} catch (Exception e) {
			logger.throwing("CryptoUtils.class", "computeSha256HashAsHex()", e);
			return null;
			
		} 
	}
	
	public static String computeSha256HashAsHex(byte[] bytes) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(bytes);
			byte[] digest = md.digest();
			BigInteger bigI = new BigInteger(1, digest);

			return String.format("%064x", bigI);
			
		} catch (NoSuchAlgorithmException e) {
			return null;

		}
	}

	public static String toHex(byte[] bytes){
		BigInteger bigI = new BigInteger(1, bytes);
		return String.format("%064x", bigI);

	}
	
	public static BigInteger computeSha256HashAsBigInteger(byte[] bytes) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(bytes);
			byte[] digest = md.digest();
			return new BigInteger(1, digest);
			
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;

		}
	}
	
	public static BigInteger randomNumberGreaterThanOne(int bitSize, BigInteger order, SecureRandom rnd){
		BigInteger r = new BigInteger(bitSize, rnd).mod(order);
		while (r.equals(BigInteger.ZERO) || r.equals(BigInteger.ONE)){
			r = new BigInteger(bitSize, rnd).mod(order);
			
		}
		return r;
		
	}
	
	public static int[] gcd(int p, int q) {
		if (q == 0){
		   return new int[] { p, 1, 0 };
		   
		}
		int[] vals = gcd(q, p % q);
		int d = vals[0];
		int a = vals[2];
		int b = vals[1] - (p / q) * vals[2];
		return new int[] { d, a, b };
		
	}
	
	public static byte[] generateNonce(int size){
		try {
			byte[] randomNonce = new byte[size];
			SecureRandom rnd = SecureRandom.getInstance("NativePRNG");
			rnd.setSeed(rnd.generateSeed(4));
			rnd.nextBytes(randomNonce);
			return randomNonce;

		} catch (NoSuchAlgorithmException e) {
			logger.throwing("CryptoUtils", "generateNonce", e);
			return null;

		}
	}

	public static String tempPassword(){
		return tempPassword(6);
	}

	public static String tempPassword(int length){
		return Base64.encodeBase64String(CryptoUtils.generateNonce(length));

	}
	
	public static String generateCode(int length) throws RuntimeException {
		String code = generateCodeWithSpace(length);
		return code.replaceAll(" ", "");
		
	}

	
	public static String generateCodeWithSpace(int length) throws RuntimeException {
		if (length%2!=0){
			throw new RuntimeException("The length must be even.");
			
			
		}
		SecureRandom sr = new SecureRandom();
		String code = ""; 
		int lengthPlusOne = length + 1; 
		while (code.length() < lengthPlusOne){
			code += sr.nextInt(9);
			if (code.length()==(length/2)){
				code += " ";
				
			}
		}
		return code;
		
	}	
	
	public static BigInteger sqrt(BigInteger x) {
	    BigInteger div = BigInteger.ZERO.setBit(x.bitLength()/2);
	    BigInteger div2 = div;
	    while(true) {
	        BigInteger y = div.add(x.divide(div)).shiftRight(1);
	        if (y.equals(div) || y.equals(div2))
	            return y;
	        div2 = div;
	        div = y;
	    }
	}

	public static Cipher generatePasswordCipher(int cipherMode, String password, byte[] salt) throws Exception {
		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
		salt = (salt==null ? password.getBytes() : salt);
		KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, Const.ITERATION_COUNT, Const.SYM_KEY_SIZE);
		SecretKey sk = factory.generateSecret(spec);
		SecretKeySpec secret = new SecretKeySpec(sk.getEncoded(), Const.SYM_ENCRYPTION_ALGORITHM);

		Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		cipher.init(cipherMode, secret);
		return cipher;

	}
}
