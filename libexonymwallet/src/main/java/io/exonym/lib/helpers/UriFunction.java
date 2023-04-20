package io.exonym.lib.helpers;

import java.net.URI;

public final class UriFunction {
	
	/*
	 * All private URIs are not implemented.
	 */
	public static URI STRING_EQUAL = URI.create("urn:oasis:names:tc:xacml:1.0:function:string-equal");
	public static URI BOOLEAN_EQUAL = URI.create("urn:oasis:names:tc:xacml:1.0:function:boolean-equal");
	public static URI INTEGER_EQUAL = URI.create("urn:oasis:names:tc:xacml:1.0:function:integer-equal");
	public static URI DATE_EQUAL = URI.create("urn:oasis:names:tc:xacml:1.0:function:date-equal");
	public static URI TIME_EQUAL = URI.create("urn:oasis:names:tc:xacml:1.0:function:time-equal");
	public static URI DATETIME_EQUAL = URI.create("urn:oasis:names:tc:xacml:1.0:function:dateTime-equal"); 
	public static URI ANYURI_EQUAL = URI.create("urn:oasis:names:tc:xacml:1.0:function:anyURI-equal");
	public static URI INTEGER_GREATER_THAN = URI.create("urn:oasis:names:tc:xacml:1.0:function:integer-greater-than");
	public static URI INTEGER_GREATER_THAN_OR_EQUAL = URI.create("urn:oasis:names:tc:xacml:1.0:function:integer-greater-than-or-equal");
	public static URI INTEGER_LESS_THAN = URI.create("urn:oasis:names:tc:xacml:1.0:function:integer-less-than");
	public static URI INTEGER_LESS_THAN_OR_EQUAL = URI.create("urn:oasis:names:tc:xacml:1.0:function:integer-less-than-or-equal");
	public static URI DATE_GREATER_THAN = URI.create("urn:oasis:names:tc:xacml:1.0:function:date-greater-than");
	public static URI DATE_GREATER_THAN_OR_EQUAL = URI.create("urn:oasis:names:tc:xacml:1.0:function:date-greater-than-or-equal");
	public static URI DATE_LESS_THAN = URI.create("urn:oasis:names:tc:xacml:1.0:function:date-less-than");
	public static URI DATE_LESS_THAN_OR_EQUAL = URI.create("urn:oasis:names:tc:xacml:1.0:function:date-less-than-or-equal");
	public static URI DATETIME_GREATER_THAN = URI.create("urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than");
	public static URI DATETIME_GREATER_THAN_OR_EQUAL = URI.create("urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than-or-equal");
	public static URI DATETIME_LESS_THAN = URI.create("urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than");
	public static URI DATETIME_LESS_THAN_OR_EQUAL = URI.create("urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than-or-equal");
	private static URI STRING_NOT_EQUAL = URI.create("urn:abc4trust:1.0:function:string-not-equal");
	private static URI BOOLEAN_NOT_EQUAL = URI.create("urn:abc4trust:1.0:function:boolean-not-equal");
	private static URI INTEGER_NOT_EQUAL = URI.create("urn:abc4trust:1.0:function:integer-not-equal");
	private static URI DATE_NOT_EQUAL = URI.create("urn:abc4trust:1.0:function:date-not-equal");
	private static URI TIME_NOT_EQUAL = URI.create("urn:abc4trust:1.0:function:time-not-equal");
	private static URI DATETIME_NOT_EQUAL = URI.create("urn:abc4trust:1.0:function:dateTime-not-equal");
	private static URI ANYURI_NOT_EQUAL = URI.create("urn:abc4trust:1.0:function:anyURI-not-equal");
	private static URI STRING_EQUAL_ONEOF = URI.create("urn:abc4trust:1.0:function:string-equal-oneof");
	private static URI BOOLEAN_EQUAL_ONEOF = URI.create("urn:abc4trust:1.0:function:boolean-equal-oneof");
	private static URI INTEGER_EQUAL_ONEOF = URI.create("urn:abc4trust:1.0:function:integer-equal-oneof");
	private static URI DATE_EQUAL_ONEOF = URI.create("urn:abc4trust:1.0:function:date-equal-oneof");
	private static URI TIME_EQUAL_ONEOF = URI.create("urn:abc4trust:1.0:function:time-equal-oneof");
	private static URI DATETIME_EQUAL_ONEOF = URI.create("urn:abc4trust:1.0:function:dateTime-equal-oneof");
	private static URI ANYURI_EQUAL_ONEOF = URI.create("urn:abc4trust:1.0:function:anyURI-equal-oneof");

}
