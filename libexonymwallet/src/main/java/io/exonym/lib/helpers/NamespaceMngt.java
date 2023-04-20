package io.exonym.lib.helpers;

import java.io.File;
import java.net.URI;

public class NamespaceMngt {
	
	public static final String VERSION = "1.0";
	public static final URI BASE_LOCATION_LEDGER = new File(System.getProperty("user.dir")).toURI().resolve("resource/");
	public static final URI BASE_LOCATION = new File(System.getProperty("user.dir")).toURI().resolve("resource/local/");
	public final static URI LEDGER = BASE_LOCATION_LEDGER.resolve("ledger/");
	
	@Deprecated
	public static final URI BASE_LOCATION_PARAMETERS = BASE_LOCATION.resolve("parameters/");
	
	@Deprecated
	public static final URI BASE_LOCATION_ISSUER = BASE_LOCATION.resolve("private/");
	@Deprecated
	public static final URI BASE_PUBLIC_LOCATION_ISSUER = BASE_LOCATION.resolve("issuance/");
	@Deprecated
	public static final URI DEFAULT_CREDENTIAL_LOCATION = BASE_LOCATION.resolve("issued/");
	@Deprecated
	public static final URI BASE_LOCATION_CRED_SPECS = BASE_LOCATION.resolve("credSpecs/");
	
	@Deprecated
	public static final String ISSUER_PARAMETERS_FILENAME = BASE_PUBLIC_LOCATION_ISSUER.resolve("").toString();
	
	@Deprecated
	private static final String ISSUER_KEY_BASE_FILENAME = BASE_LOCATION_ISSUER.resolve("").toString();
	@Deprecated
	public static final String DEFAULT_ISSUER_KEY_FILENAME = ISSUER_KEY_BASE_FILENAME + "sk.";

	public static final String SYSTEM_PARAMETERS_BASE_FILENAME = LEDGER.resolve("lambda").toString();
	public static final String DEFAULT_SYSTEM_PARAMETERS_FILENAME = LEDGER.resolve(SYSTEM_PARAMETERS_BASE_FILENAME + ".xml").toString();
	
	public static final String SYSTEM_PARAMETERS_TEMPLATE_BASE_FILENAME = BASE_LOCATION_PARAMETERS.resolve("template").toString();
	
	public static final URI XS_INTEGER = URI.create("xs:integer");
	public static final URI XS_STRING = URI.create("xs:string");
	public static final URI XS_ANY_URI = URI.create("xs:anyURI");
	public static final URI XS_DATE = URI.create("xs:date");
	public static final URI XS_TIME = URI.create("xs:time");
	public static final URI XS_DATE_TIME = URI.create("xs:dateTime");
	public static final URI XS_BOOLEAN = URI.create("xs:boolean");
	
	public static final URI ENC_STR_SHA_256 = URI.create("urn:abc4trust:1.0:encoding:string:sha-256");
	public static final URI ENC_STR_UTF_8 = URI.create("urn:abc4trust:1.0:encoding:string:utf-8");
	public static final URI ENC_ANY_URI_SHA_256  = URI.create("urn:abc4trust:1.0:encoding:anyUri:sha-256");
	public static final URI ENC_ANY_URI_UTF_8   = URI.create("urn:abc4trust:1.0:encoding:anyUri:utf-8");
	public static final URI ENC_DATE_TIME_SIGNED = URI.create("urn:abc4trust:1.0:encoding:dateTime:unix:signed");
	public static final URI ENC_DATE_TIME_UNSIGNED = URI.create("urn:abc4trust:1.0:encoding:dateTime:unix:unsigned");
	public static final URI ENC_DATE_UNSIGNED = URI.create("urn:abc4trust:1.0:encoding:date:unix:unsigned");
	public static final URI ENC_DATE_SIGNED = URI.create("urn:abc4trust:1.0:encoding:date:unix:signed");
	public static final URI ENC_DATE_1870_UNSIGNED = URI.create("urn:abc4trust:1.0:encoding:date:since1870:unsigned");
	public static final URI ENC_DATE_2010_UNSIGNED = URI.create("urn:abc4trust:1.0:encoding:date:since2010:unsigned");
	public static final URI ENC_TIME_MIDNIGHT_UNSIGNED = URI.create("urn:abc4trust:1.0:encoding:time:sinceMidnight:unsigned");
	public static final URI ENC_BOOLEAN_UNSIGNED = URI.create("urn:abc4trust:1.0:encoding:boolean:unsigned");
	public static final URI ENC_INT_UNSIGNED = URI.create("urn:abc4trust:1.0:encoding:integer:unsigned");
	public static final URI ENC_INT_SIGNED = URI.create("urn:abc4trust:1.0:encoding:integer:signed");
	public static final URI ENC_STR_PRIME = URI.create("urn:abc4trust:1.0:encoding:string:prime");	
	
}
