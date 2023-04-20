package io.exonym.lib.pojo;

import io.exonym.lib.standard.CryptoUtils;

import java.net.URI;
import java.util.logging.Logger;

public class Namespace {


	private final static Logger logger = Logger.getLogger(Namespace.class.getName());
	public final static String URN_PREFIX = "urn:rulebook";
	public final static String URN_PREFIX_COLON = URN_PREFIX + ":";

	public final static String URN_PREFIX_DOTTED = "urn.rulebook";
	public final static String URN_PREFIX_DOTTED_DOTTED = URN_PREFIX_DOTTED + ".";

	public final static String UNIVERSAL_LINK_PREFIX = "https://trust.exonym.io/";
	public final static String UNIVERSAL_LINK_AUTHENTICATION_REQUEST = UNIVERSAL_LINK_PREFIX + "auth/?";
	public final static String UNIVERSAL_LINK_JOIN_REQUEST = UNIVERSAL_LINK_PREFIX + "join/?";
	public final static String UNIVERSAL_LINK_DELEGATE_REQUEST = UNIVERSAL_LINK_PREFIX + "delegate-request/?";
	public final static String UNIVERSAL_LINK_FILL_DELEGATE_REQUEST = UNIVERSAL_LINK_PREFIX + "fill-delegate-request/?";
	public final static String UNIVERSAL_LINK_FILLED_DELEGATE_REQUEST = UNIVERSAL_LINK_PREFIX + "filled-delegate-request/?";

	public final static String EXONYM_PREFIX = "urn:exonym:";
	public final static String ENDONYM_PREFIX = "urn:endonym:";


	public final static String EX = "urn:exonym:rulebookschema1.0";
	public final static String ABC = "http://abc4trust.eu/wp2/abcschemav1.0";


	public static URI createUid(URI base){
		String uidString = CryptoUtils.computeSha256HashAsHex(base.toString());
		logger.warning("Removed Date from UID. TODO");
		return URI.create(base + ":" + uidString);
		
	}
	
	public static URI extendUid(URI base, String extension){
		return URI.create(base.toString() + ":" + extension);
		
	}
	
	public static URI extendAndCreateUid(URI base, String extension){
		return createUid(extendUid(base, extension));
		
	}

}
