package io.exonym.lib.helpers;

import java.net.URI;

public class UriAttType {
	
	public final static URI STRING_SHA_256 = URI.create("urn:abc4trust:1.0:encoding:string:sha-256");
	public final static URI STRING_UTF_8 = URI.create("urn:abc4trust:1.0:encoding:string:utf-8");
	public final static URI ANYURI_SHA_256 = URI.create("urn:abc4trust:1.0:encoding:anyUri:sha-256");
	public final static URI ANYURI_UTF_8 = URI.create("urn:abc4trust:1.0:encoding:anyUri:utf-8");
	public final static URI DATETIME_UNIX_SIGNED = URI.create("urn:abc4trust:1.0:encoding:dateTime:unix:signed");
	public final static URI DATETIME_UNIX_UNSIGNED = URI.create("urn:abc4trust:1.0:encoding:dateTime:unix:unsigned");
	public final static URI DATE_UNIX_UNSIGNED = URI.create("urn:abc4trust:1.0:encoding:date:unix:unsigned");
	public final static URI DATE_UNIX_SIGNED = URI.create("urn:abc4trust:1.0:encoding:date:unix:signed");
	public final static URI DATE_SINCE1870_UNSIGNED = URI.create("urn:abc4trust:1.0:encoding:date:since1870:unsigned");
	public final static URI DATE_SINCE2010_UNSIGNED = URI.create("urn:abc4trust:1.0:encoding:date:since2010:unsigned");
	public final static URI TIME_SINCE_MIDNIGHT_UNSIGNED = URI.create("urn:abc4trust:1.0:encoding:time:sinceMidnight:unsigned");
	public final static URI BOOLEAN_UNSIGNED = URI.create("urn:abc4trust:1.0:encoding:boolean:unsigned");
	public final static URI INTEGER_UNSIGNED = URI.create("urn:abc4trust:1.0:encoding:integer:unsigned");
	public final static URI INTEGER_SIGNED = URI.create("urn:abc4trust:1.0:encoding:integer:signed");
	public final static URI ENCODING_STRING_PRIME = URI.create("urn:abc4trust:1.0:encoding:string:prime");

}
