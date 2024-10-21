package io.exonym.lib.standard;

import java.net.URI;
import java.nio.file.Path;

public class Const {
	
	public static final String ASYM_ENCRYPTION_ALGORITHM = "RSA";
	public static final String ASYM_STANDARD_CIPHER_ALGORITHM = "RSA/ECB/OAEPPadding";// "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";
	public static final int ASYM_KEY_SIZE = 1024;
	public static final String SYM_ENCRYPTION_ALGORITHM = "AES";
	public static final int SYM_KEY_SIZE = 256;
	public static final String MESSAGE_AUTHENTICATION_CODE_ALGORITHM = "HmacSHA1";
	public static final String BINDING_ALIAS = "urn:io:exonym";
    public static final int ITERATION_COUNT = 65536;
	public static final String LEAD = "lead";
	public static final String MODERATOR = "moderator";
	public static final String SIGNATURES_XML = "signatures.xml";
	public static final String LEADS_XML = "leads.xml";
	public static final URI TRUST_NETWORK_UID = URI.create("urn:rulebook:trust-network:ni");

	public static final String ENDPOINT_JOIN = "/join";
	public static final String ENDPOINT_APPEAL = "/meta-mod";
	public static final String ENDPOINT_REGISTER = "/register";

}
