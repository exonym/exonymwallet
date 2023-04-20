package io.exonym.lib.helpers;

import io.exonym.lib.exceptions.UxException;

import java.net.URI;

public class UriEncoding {
	
	
	public static void isValid(URI encoding, URI predicate) throws UxException {
		if (encoding.equals(UriEncoding.ANY_URI_SHA_256)){
			if (!(predicate.equals(UriFunction.ANYURI_EQUAL))){
				throw new UxException("Only anyUri-equal or -not equal are valid.");
				
			}
		} else if (encoding.equals(UriEncoding.ANY_URI_UTF_8)){
			if (!(predicate.equals(UriFunction.ANYURI_EQUAL))){
				throw new UxException("Only anyUri-equal or -not equal are valid.");
				
			}
		} else if (encoding.equals(UriEncoding.BOOLEAN_UNSIGNED)){
			if (!(predicate.equals(UriFunction.BOOLEAN_EQUAL))){
				throw new UxException("Only anyUri-equal or -not equal are valid.");
				
			}
		} else if (encoding.equals(UriEncoding.DATE_1870_UNSIGNED)){
			if (!(predicate.equals(UriFunction.DATE_EQUAL) ||
					predicate.equals(UriFunction.DATE_GREATER_THAN) ||
					predicate.equals(UriFunction.DATE_GREATER_THAN_OR_EQUAL) ||
					predicate.equals(UriFunction.DATE_LESS_THAN) ||
					predicate.equals(UriFunction.DATE_LESS_THAN_OR_EQUAL))){
				throw new UxException("Only date predicates are valid.");
				
			}
		} else if (encoding.equals(UriEncoding.DATE_2010_UNSIGNED)){
			if (!(predicate.equals(UriFunction.DATE_EQUAL) ||
					predicate.equals(UriFunction.DATE_GREATER_THAN) ||
					predicate.equals(UriFunction.DATE_GREATER_THAN_OR_EQUAL) ||
					predicate.equals(UriFunction.DATE_LESS_THAN) ||
					predicate.equals(UriFunction.DATE_LESS_THAN_OR_EQUAL))){
				throw new UxException("Only date predicates are valid.");
				
			}
		} else if (encoding.equals(UriEncoding.DATE_TIME_UNIX_SIGNED)){
			if (!(predicate.equals(UriFunction.DATETIME_EQUAL) ||
					predicate.equals(UriFunction.DATETIME_GREATER_THAN) ||
					predicate.equals(UriFunction.DATETIME_GREATER_THAN_OR_EQUAL) ||
					predicate.equals(UriFunction.DATETIME_LESS_THAN) ||
					predicate.equals(UriFunction.DATETIME_LESS_THAN_OR_EQUAL))){
				throw new UxException("Only Date Time predicates are valid.  You may have used a date.");
				
			}
		} else if (encoding.equals(UriEncoding.DATE_TIME_UNIX_UNSIGNED)){
			if (!(predicate.equals(UriFunction.DATETIME_EQUAL) ||
					predicate.equals(UriFunction.DATETIME_GREATER_THAN) ||
					predicate.equals(UriFunction.DATETIME_GREATER_THAN_OR_EQUAL) ||
					predicate.equals(UriFunction.DATETIME_LESS_THAN) ||
					predicate.equals(UriFunction.DATETIME_LESS_THAN_OR_EQUAL))){
				throw new UxException("Only Date Time predicates are valid.  You may have used a date.");
				
			}
		} else if (encoding.equals(UriEncoding.INTEGER_SIGNED)){
			if (!(predicate.equals(UriFunction.INTEGER_EQUAL) ||
					predicate.equals(UriFunction.INTEGER_GREATER_THAN) ||
					predicate.equals(UriFunction.INTEGER_GREATER_THAN_OR_EQUAL) ||
					predicate.equals(UriFunction.INTEGER_LESS_THAN) ||
					predicate.equals(UriFunction.INTEGER_LESS_THAN_OR_EQUAL))){
				throw new UxException("Only integer predicates are valid.");
				
			}
		} else if (encoding.equals(UriEncoding.INTEGER_UNSIGNED)){
			if (!(predicate.equals(UriFunction.INTEGER_EQUAL) ||
					predicate.equals(UriFunction.INTEGER_GREATER_THAN) ||
					predicate.equals(UriFunction.INTEGER_GREATER_THAN_OR_EQUAL) ||
					predicate.equals(UriFunction.INTEGER_LESS_THAN) ||
					predicate.equals(UriFunction.INTEGER_LESS_THAN_OR_EQUAL))){
				throw new UxException("Only integer predicates are valid.");
				
			}
		} else if (encoding.equals(UriEncoding.SHA_256)){
			throw new UxException("Predicates are not valid on SHA256.");
				
		} else if (encoding.equals(UriEncoding.STRING_PRIME)){
			throw new UxException("Predicates are not valid on String Prime.  You must use value disclosure.");
			
		} else if (encoding.equals(UriEncoding.UTF_8)){
			throw new UxException("Predicates are not valid on Strings.  You must use value disclosure.");
				
		}
	}
	
	
	/**
	* Encoding: urn:abc4trust:1.0:encoding:string:sha-256 <p>
	* Data type: http://www.w3.org/2001/XMLSchema#string <p>
	* Restrictions: none <p>
	* Inspectable: no (hash value only) <p>
	* Supported predicates: <p>
	* urn:oasis:names:tc:xacml:1.0:function:string-equal <p>
	* urn:abc4trust:1.0:function:string-not-equal <p>
	* Comments: Best suited for strings of arbitrary lengths that are unlikely to be used for inspection. <p>
	 */
	public final static URI SHA_256 = URI.create("urn:abc4trust:1.0:encoding:string:sha-256");
	
	/**
	* Encoding: urn:abc4trust:1.0:encoding:string:utf-8 <p>
	* Data type: http://www.w3.org/2001/XMLSchema#string <p>
	* Restrictions: the UTF-8 encoded string must be shorter than @MaxLength — 8 bits or @MaxLength/8 <p>
	* — 1 bytes <p>
	* Inspectable: yes <p>
	* Supported predicates: <p>
	* urn:oasis:names:tc:xacml:1.0:function:string-equal <p>
	* urn:abc4trust:1.0:function:string-not-equal <p>
	* Comments: Best suited for short strings where the possibility to use inspection should be kept open. For long strings that are likely to require inspection, please consider splitting up the attribute into <p>
	* multiple attributes with this encoding. <p>
	 */
	public final static URI UTF_8 = URI.create("urn:abc4trust:1.0:encoding:string:utf-8");
	
	/**
	* Encoding: urn:abc4trust:1.0:encoding:string:prime <p>
	* Data type: http://www.w3.org/2001/XMLSchema#string <p>
	* Restrictions: Can only be used for attributes where the value range is restricted by a list of; <p>
	* .../abc:AttributeDescription/abc:AllowedValueelements. <p>
	* Inspectable: yes <p>
	* Supported predicates: <p>
	* urn:oasis:names:tc:xacml:1.0:function:string-equal <p>
	* urn:abc4trust:1.0:function:string-not-equal <p>
	* urn:abc4trust:1.0:function:string-equal-one-of <p>
	* Comments: Best choice for attributes with a limited value range where presentation policies are likely to request showing that the attribute value is one of a given list of strings without revealing the exact value. <p>
	 */
	public final static URI STRING_PRIME = URI.create("urn:abc4trust:1.0:encoding:string:prime");
	
	/**
 	* Encoding: urn:abc4trust:1.0:encoding:anyUri:sha-256 <p>
	* Data type: http://www.w3.org/2001/XMLSchema#anyURI <p>
	* Restrictions: none <p>
	* Inspectable: no (hash value only) <p>
	* Supported predicates: <p>
	* urn:oasis:names:tc:xacml:1.0:function:anyURI-equal <p>
	* urn:abc4trust:1.0:function:anyURI-not-equal <p>
	* Comments: Best suited for URIs of arbitrary lengths that are unlikely to be used for inspection. <p>
	 */
	public final static URI ANY_URI_SHA_256 = URI.create("urn:abc4trust:1.0:encoding:anyUri:sha-256");
	
	/**
 	* Encoding: urn:abc4trust:1.0:encoding:anyUri:utf-8 <p>
	* Data type: http://www.w3.org/2001/XMLSchema#anyURI <p>
	* Restrictions: shorter than @MaxLength bytes <p>
	* Inspectable: yes <p>
	* Supported predicates: <p>
	* urn:oasis:names:tc:xacml:1.0:function:anyURI-equal <p>
	* urn:abc4trust:1.0:function:anyURI-not-equal <p>
	* Comments: Best suited for short URIs where the possibility to use inspection should be kept open. For long URIs that are likely to require inspection, please consider splitting up the attribute into multiple attributes with this encoding. <p>
	 */
	public final static URI ANY_URI_UTF_8 = URI.create("urn:abc4trust:1.0:encoding:anyUri:utf-8");
	
	/**
 	* Encoding: urn:abc4trust:1.0:encoding:anyURI:prime <p>
	* Data type: http://www.w3.org/2001/XMLSchema#string <p>
	* Restrictions: Can only be used for attributes where the value range is restricted by a list of;  <p>
	* .../abc:AttributeDescription/abc:AllowedValue elements. <p>
	* Inspectable: yes <p>
	* Supported predicates: <p>
	* urn:oasis:names:tc:xacml:1.0:function:anyURI-equal <p>
	* urn:abc4trust:1.0:function:anyURI-not-equal <p>
	* urn:abc4trust:1.0:function:anyURI-equal-one-of <p>
	* Comments: Best choice for attributes with a limited value range where presentation policies are likely to request showing that the attribute value is one of a given list of URIs without revealing the exact value. <p>
	 */
	private final static URI ANY_URI_PRIME = URI.create("urn:abc4trust:1.0:encoding:anyURI:prime");
	
	/**
 	* Encoding: urn:abc4trust:1.0:encoding:dateTime:unix:signed <p>
	* Data type: http://www.w3.org/2001/XMLSchema#dateTime <p>
	* Restrictions: none <p>
	* Inspectable: yes <p>
	* Supported predicates: <p>
	* urn:oasis:names:tc:xacml:1.0:function:dateTime-equal <p>
	* urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than <p>
	* urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than-or-equal <p>
	* urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than <p>
	* urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than-or-equal <p>
	* urn:abc4trust:1.0:function:dateTime-not-equal <p>
	* Comments: Good default choice for times that can be far in the past and/or future. Greater-than and less-than predicates may be slightly less efficient using this encoding.  <p>
	 */
	public final static URI DATE_TIME_UNIX_SIGNED = URI.create("urn:abc4trust:1.0:encoding:dateTime:unix:signed");
	
	/**
	* Encoding: urn:abc4trust:1.0:encoding:dateTime:unix:unsigned <p>
	* Data type: http://www.w3.org/2001/XMLSchema#dateTime <p>
	* Restrictions: since 1970 <p>
	* Inspectable: yes <p>
	* Supported predicates: <p>
	* urn:oasis:names:tc:xacml:1.0:function:dateTime-equal <p>
	* urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than <p>
	* urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than-or-equal <p>
	* urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than <p>
	* urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than-or-equal <p>
	* urn:abc4trust:1.0:function:dateTime-not-equal <p>
	* Comments: Best choice for times after 1970 that are likely to be used in combination with greatherthan or less-than predicates. <p>
	 */
	public final static URI DATE_TIME_UNIX_UNSIGNED = URI.create("urn:abc4trust:1.0:encoding:dateTime:unix:unsigned");
	
	/**
	* Encoding: urn:abc4trust:1.0:encoding:dateTime:prime <p>
	* Data type: http://www.w3.org/2001/XMLSchema#dateTime <p>
	* Restrictions: Can only be used for attributes where the value range is restricted by a list of <p>
	* .../abc:AttributeDescription/abc:AllowedValue elements. <p>
	* Inspectable: yes <p>
	* Supported predicates: <p>
	* urn:oasis:names:tc:xacml:1.0:function:dateTime-equal <p>
	* urn:abc4trust:1.0:function:dateTime-not-equal <p>
	* urn:abc4trust:1.0:function:dateTime-equal-one of <p>
	* Comments: Best choice for attributes with a limited value range where presentation policies are likely to request showing that the attribute value is one of a given list of times without revealing the exact value. <p>
	 */
	private final static URI DATE_TIME_PRIME = URI.create("urn:abc4trust:1.0:encoding:dateTime:prime");
	
	/**
	* Encoding:urn:abc4trust:1.0:encoding:date:unix:signed <p>
	* Data type: http://www.w3.org/2001/XMLSchema#date <p>
	* Restrictions: none <p>
	* Inspectable: yes <p>
	* Supported predicates: <p>
	* urn:oasis:names:tc:xacml:1.0:function:date-equal <p>
	* urn:oasis:names:tc:xacml:1.0:function:date-greater-than <p>
	* urn:oasis:names:tc:xacml:1.0:function:date-greater-than-or-equal <p>
	* urn:oasis:names:tc:xacml:1.0:function:date-less-than <p>
	* urn:oasis:names:tc:xacml:1.0:function:date-less-than-or-equal <p>
	* urn:abc4trust:1.0:function:date-not-equal <p>
	* Comments: Good default choice for dates that can be far in the past and/or future. Greater-than and less-than predicates may be less efficient using this encoding. <p>
	 */
	public final static URI DATE_UNIX_SIGNED = URI.create("urn:abc4trust:1.0:encoding:date:unix:signed");
	
	/**
	* Encoding: urn:abc4trust:1.0:encoding:date:unix:unsigned <p>
	* Data type: http://www.w3.org/2001/XMLSchema#date <p>
	* Restrictions: since 1970 <p>
	* Inspectable: yes <p>
	* Supported predicates: <p>
	* urn:oasis:names:tc:xacml:1.0:function:date-equal <p>
	* urn:oasis:names:tc:xacml:1.0:function:date-greater-than <p>
	* urn:oasis:names:tc:xacml:1.0:function:date-greater-than-or-equal <p>
	* urn:oasis:names:tc:xacml:1.0:function:date-less-than <p>
	* urn:oasis:names:tc:xacml:1.0:function:date-less-than-or-equal <p>
	* urn:abc4trust:1.0:function:date-not-equal <p>
	* Comments: Best choice for times after 1970 that are likely to be used in combination with greatherthan or less-than predicates. <p>
	 */
	public final static URI DATE_UNIX_UNSIGNED = URI.create("urn:abc4trust:1.0:encoding:date:unix:unsigned");
	
	/**
	* Encoding: urn:abc4trust:1.0:encoding:date:since1870:unsigned <p>
	* Data type: http://www.w3.org/2001/XMLSchema#date <p>
	* Restrictions: since 1870 <p>
	* Inspectable: yes <p>
	* Supported predicates: <p>
	* urn:oasis:names:tc:xacml:1.0:function:date-equal <p>
	* urn:oasis:names:tc:xacml:1.0:function:date-greater-than <p>
	* urn:oasis:names:tc:xacml:1.0:function:date-greater-than-or-equal <p>
	* urn:oasis:names:tc:xacml:1.0:function:date-less-than <p>
	* urn:oasis:names:tc:xacml:1.0:function:date-less-than-or-equal <p>
	* urn:abc4trust:1.0:function:date-not-equal <p>
	* Comments: Best choice for birth dates, which are likely to fall after 1870 but are likely to require efficient greather-than or less-than predicates. <p>
	 */
	public final static URI DATE_1870_UNSIGNED = URI.create("urn:abc4trust:1.0:encoding:date:since1870:unsigned");
	
	/**
	* Encoding:urn:abc4trust:1.0:encoding:date:since2010:unsigned <p>
	* Data type: http://www.w3.org/2001/XMLSchema#date <p>
	* Restrictions: since 2010 <p>
	* Inspectable: yes <p>
	* Supported predicates: <p>
	* urn:oasis:names:tc:xacml:1.0:function:date-equal <p>
	* urn:oasis:names:tc:xacml:1.0:function:date-greater-than <p>
	* urn:oasis:names:tc:xacml:1.0:function:date-greater-than-or-equal <p>
	* urn:oasis:names:tc:xacml:1.0:function:date-less-than <p>
	* urn:oasis:names:tc:xacml:1.0:function:date-less-than-or-equal <p>
	* urn:abc4trust:1.0:function:date-not-equal <p>
	* Comments: Best choice for expiration dates, which are likely to fall after 2010 but are likely to require efficient greather-than or less-than predicates. <p>	 */
	public final static URI DATE_2010_UNSIGNED = URI.create("urn:abc4trust:1.0:encoding:date:since2010:unsigned");
	
	/**
	* Encoding: urn:abc4trust:1.0:encoding:date:prime <p>
	* Data type:http://www.w3.org/2001/XMLSchema#date <p>
	* Restrictions: Can only be used for attributes where the value range is restricted by a list of <p>
	* .../abc:AttributeDescription/abc:AllowedValue elements. <p>
	* Inspectable: yes <p>
	* Supported predicates: <p>
	* urn:oasis:names:tc:xacml:1.0:function:date-equal <p>
	* urn:abc4trust:1.0:function:date-not-equal <p>
	* urn:abc4trust:1.0:function:date-equal-one of <p>
	* Comments: Best choice for attributes with a limited value range where presentation policies are likely to request showing that the attribute value is one of a given list of dates without revealing the exact value. <p>
	 */
	private final static URI DATE_PRIME = URI.create("urn:abc4trust:1.0:encoding:date:prime");
	
	/**
	* Encoding: urn:abc4trust:1.0:encoding:boolean:unsigned <p>
	* Data type: http://www.w3.org/2001/XMLSchema#boolean <p>
	* Restrictions: none <p>
	* Inspectable: yes <p>
	* Supported predicates: <p>
	* urn:oasis:names:tc:xacml:1.0:function:boolean-equal <p>
	* urn:abc4trust:1.0:function:boolean-not-equal <p>
	 */
	public final static URI BOOLEAN_UNSIGNED = URI.create("urn:abc4trust:1.0:encoding:boolean:unsigned");
	
	/**
	* Encoding: urn:abc4trust:1.0:encoding:integer:unsigned  <p>
	* Data type: http://www.w3.org/2001/XMLSchema#integer <p>
	* Restrictions: positive (including zero) <p>
	* Inspectable: yes <p>
	* Supported predicates: <p>
	* urn:oasis:names:tc:xacml:1.0:function:integer-equal <p>
	* urn:oasis:names:tc:xacml:1.0:function:integer-greater-than <p>
	* urn:oasis:names:tc:xacml:1.0:function:integer-greater-than-or-equal <p>
	* urn:oasis:names:tc:xacml:1.0:function:integer-less-than <p>
	* urn:oasis:names:tc:xacml:1.0:function:integer-less-than-or-equal <p>
	* urn:abc4trust:1.0:function:integer-not-equal <p>
	* Comments: Best for integers that cannot take negative values. <p>
	 */
	public final static URI INTEGER_UNSIGNED = URI.create("urn:abc4trust:1.0:encoding:integer:unsigned");

	/**
	* Encoding: urn:abc4trust:1.0:encoding:integer:signed <p>
	* Data type: http://www.w3.org/2001/XMLSchema#integer <p>
	* Restrictions: none <p>
	* Inspectable: yes <p>
	* Supported predicates: <p>
	* urn:oasis:names:tc:xacml:1.0:function:integer-equal <p>
	* urn:oasis:names:tc:xacml:1.0:function:integer-greater-than <p>
	* urn:oasis:names:tc:xacml:1.0:function:integer-greater-than-or-equal <p>
	* urn:oasis:names:tc:xacml:1.0:function:integer-less-than <p>
	* urn:oasis:names:tc:xacml:1.0:function:integer-less-than-or-equal <p>
	* urn:abc4trust:1.0:function:integer-not-equal <p>	 
	* 
	*/
	public final static URI INTEGER_SIGNED = URI.create("urn:abc4trust:1.0:encoding:integer:signed");
	
	/**
	 * Data type: http://www.w3.org/2001/XMLSchema#integer
		Restrictions: Can only be used for attributes where the value range is restricted by a list of
		.../abc:AttributeDescription/abc:AllowedValue elements. <p>
		Inspectable: yes <p>
		Supported predicates: <p>
		urn:oasis:names:tc:xacml:1.0:function:integer-equal<p>
		urn:abc4trust:1.0:function:integer-not-equal<p>
		urn:abc4trust:1.0:function:integer-equal-one of<p>
		Comments: Best choice for attributes with a limited value range where presentation policies are
		likely to request showing that the attribute value is one of a given list of integers without revealing
		the exact value.
	 */
	private final static URI INTEGER_PRIME = URI.create("urn:abc4trust:1.0:encoding:integer:prime");

	
}

