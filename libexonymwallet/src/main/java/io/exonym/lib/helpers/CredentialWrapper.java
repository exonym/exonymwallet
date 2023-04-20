package io.exonym.lib.helpers;

import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.AttributeDescription;
import eu.abc4trust.xml.Credential;
import io.exonym.lib.abc.attributeEncoding.MyAttributeEncodingFactory;
import io.exonym.lib.abc.attributeType.EnumAllowedValues;
import io.exonym.lib.abc.attributeType.MyAttributeValue;

import java.math.BigInteger;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class CredentialWrapper {
	
	public final static int ENCODE_ATTRIBUTES = 0; 
	public final static int DECODE_ATTRIBUTES = 1; 
	
	private final Credential credential;
	private final ArrayList<Attribute> attributeList;

	protected final static Logger logger = Logger.getLogger(CredentialWrapper.class.getName());
	
	/**
	 * A helper class that encodes and decodes attributes to their type to flip between human-readable
	 * stored attributes and required integer values for processing of verifiable claims.
	 *  
	 * @param credential
	 * @param attributeAction
	 * @throws Exception
	 */
	public CredentialWrapper(Credential credential, int attributeAction) throws Exception {
		this.credential=credential;
		if (credential==null || 
			credential.getCredentialDescription().getAttribute() == null || 
			credential.getCredentialDescription().getAttribute().isEmpty()){
			throw new RuntimeException("Poorly formed credential (" + credential +")");	
					
		}
		attributeList = (ArrayList<Attribute>) credential.getCredentialDescription().getAttribute();
		
		if (attributeAction==ENCODE_ATTRIBUTES){
			encodeAttributes(this.attributeList);
			
		} else if (attributeAction == DECODE_ATTRIBUTES){
			decodeAttributes(this.attributeList);
			
		} else {
			throw new RuntimeException("No such attribute action.  Use CredentialWrapper.ENCODE_ATTRIBUTES, or CredentialWrapper.DECODE_ATTRIBUTES");
			
		}
	}
	
	public CredentialWrapper(List<Attribute> attributes, int attributeAction) throws Exception {
		this.credential=null;
		this.attributeList = (ArrayList<Attribute>) attributes;
		if (attributeAction==CredentialWrapper.ENCODE_ATTRIBUTES){
			encodeAttributes(this.attributeList);
			
		} else if (attributeAction==CredentialWrapper.DECODE_ATTRIBUTES){
			decodeAttributes(this.attributeList);
			
		}
	}

	public static void encodeAttributes(ArrayList<Attribute> attributeList) throws Exception{
		for (Attribute attribute : attributeList) {
			AttributeDescription ad = attribute.getAttributeDescription();
			EnumAllowedValues eav = new EnumAllowedValues(ad);
			URI encoding = attribute.getAttributeDescription().getEncoding();

			if (!(encoding.equals(UriEncoding.ANY_URI_SHA_256) || encoding.equals(UriEncoding.SHA_256))) {
				MyAttributeValue myA = MyAttributeEncodingFactory.parseValueFromEncoding(ad.getEncoding(), attribute.getAttributeValue(), eav);
				BigInteger b = myA.getIntegerValueUnderEncoding(ad.getEncoding());
				attribute.setAttributeValue(b);

			} else {
				logger.fine("Deliberately skipped an attribute encoding " +
						attribute.getAttributeDescription().getType());

			}
		}
	}

	public static void decodeAttributes(ArrayList<Attribute> attributeList) throws Exception{
		for (Attribute attribute : attributeList) {
			AttributeDescription ad = attribute.getAttributeDescription();
			EnumAllowedValues eav = new EnumAllowedValues(ad);
			BigInteger b = (BigInteger) attribute.getAttributeValue();
			URI encoding = attribute.getAttributeDescription().getEncoding();

			if (!(encoding.equals(UriEncoding.ANY_URI_SHA_256) ||
					encoding.equals(UriEncoding.SHA_256))) {
				MyAttributeValue mav = MyAttributeEncodingFactory.recoverValueFromBigInteger(ad.getEncoding(), b, eav);
				attribute.setAttributeValue(mav.getValueAsObject());
			}
		}
	}

	public Credential getCredential() {
		return credential;
		
	}
	
	public ArrayList<Attribute> getAttributeList() {
		return attributeList;
		
	}
}
