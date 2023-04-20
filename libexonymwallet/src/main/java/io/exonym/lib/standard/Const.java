package io.exonym.lib.standard;

public class Const {
	
	public static final String ASYM_ENCRYPTION_ALGORITHM = "RSA";
	public static final String ASYM_STANDARD_CIPHER_ALGORITHM = "RSA/ECB/OAEPPadding";// "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
	public static final int ASYM_KEY_SIZE = 1024;
	public static final String SYM_ENCRYPTION_ALGORITHM = "AES";
	public static final int SYM_KEY_SIZE = 256;
	public static final String MESSAGE_AUTHENTICATION_CODE_ALGORITHM = "HmacSHA1";

	public static final String BINDING_ALIAS = "urn:io:exonym";


    public static final int ITERATION_COUNT = 65536;
}
