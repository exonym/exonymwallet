package io.exonym.lib.actor;

import com.ibm.zurich.idmx.util.bigInt.BigIntFactoryImpl;
import eu.abc4trust.xml.Attribute;
import eu.abc4trust.xml.AttributeDescription;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.ObjectFactory;
import io.exonym.lib.abc.attributeEncoding.*;
import io.exonym.lib.helpers.DateHelper;
import io.exonym.lib.helpers.UriEncoding;
import io.exonym.lib.abc.attributeType.EnumAllowedValues;
import io.exonym.lib.abc.attributeType.MyAttributeValue;
import io.exonym.lib.exceptions.UxException;
import io.exonym.lib.pojo.Namespace;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

public class VerifiedClaim {
	
	private final CredentialSpecification credSpec;
	private final ArrayList<Attribute> attributes = new ArrayList<>();
	private final ObjectFactory of = new ObjectFactory();
	private final ArrayList<URI> labels = new ArrayList<>();
	private final HashMap<URI, Object> labelValuesMap = new HashMap<>();
	private final BigIntFactoryImpl bif = new BigIntFactoryImpl();
	private boolean commited = false;
	private final URI revocationHandle = URI.create("http://abc4trust.eu/wp2/abcschemav1.0/revocationhandle");
	private final boolean revocable;
	
	public VerifiedClaim(CredentialSpecification credSpec) throws Exception {
		this.credSpec=credSpec;
		this.revocable=this.credSpec.isRevocable();
		
		if (this.credSpec==null){
			throw new Exception("Null Credential Specification");
			
		} if (this.credSpec.getAttributeDescriptions()!=null && 
				this.credSpec.getAttributeDescriptions().getAttributeDescription()!=null &&
				this.credSpec.getAttributeDescriptions().getAttributeDescription().isEmpty()){
			throw new Exception("Credential Specification attributes are poorly defined");
			
		}
		buildAttributeLists();
		
	}
	
	public void clear(){
		this.labelValuesMap.clear();
		
	}

	private void buildAttributeLists() {
		ArrayList<AttributeDescription> l = (ArrayList<AttributeDescription>) credSpec.getAttributeDescriptions().getAttributeDescription();
		for (Iterator<AttributeDescription> iterator = l.iterator(); iterator.hasNext();) {
			AttributeDescription attributeDescription = (AttributeDescription) iterator.next();
			if (!attributeDescription.getType().equals(revocationHandle)){
				Attribute attribute = of.createAttribute();
				attribute.setAttributeDescription(attributeDescription);
				attributes.add(attribute);
				labels.add(attributeDescription.getType());
				
			}
		}
	}
	
	public URI claimUri(String internal) {
		String uri = DateHelper.currentBareIsoUtcDate();
		uri += ":" + credSpec.getSpecificationUID().toString().replaceAll(Namespace.URN_PREFIX_COLON, "");
		uri += ":" + internal;
		return URI.create(Namespace.URN_PREFIX_COLON + uri);
		
	}	
	
	public ArrayList<Attribute> commitToDefinedAttributes() throws Exception {
		if (labelValuesMap.size()!=labels.size()){
			throw new Exception("The attributes labels have not all been set.  Credential Spec has (" + labels.size() + ") and "
					+ "map is (" + labelValuesMap.size() + ")");
			
		}
		int i = 0; 
		for (Iterator<URI> iterator = labels.iterator(); iterator.hasNext();) {
			URI uri = iterator.next();
			Attribute att = attributes.get(i);
			i++;
			att.setAttributeUID(URI.create("" + bif.random(16, new Random())));
			addAttributeToList(uri, att);
			
		} //*/
		if (revocable){
			AttributeDescription ad = this.credSpec.getAttributeDescriptions().getAttributeDescription().get(0);
			Attribute att = of.createAttribute();
			att.setAttributeUID(URI.create("" + bif.random(16, new Random())));
			att.setAttributeDescription(ad);
			attributes.add(0, att);
			
		} //*/
		this.commited=true;
		return attributes;
		
	} 

	private void addAttributeToList(URI uri, Attribute attribute) throws Exception {
		Object value = labelValuesMap.get(uri);
		URI dataType = attribute.getAttributeDescription().getEncoding();
		
		EnumAllowedValues allowed = new EnumAllowedValues(attribute.getAttributeDescription());
		MyAttributeValue att = VerifiedClaim.computeMyAttributeValue(dataType, value, allowed);
		
		attribute.setAttributeValue(
				att.getIntegerValueUnderEncoding(
						attribute.getAttributeDescription().getEncoding()));
		
	}
	
	public static MyAttributeValue computeMyAttributeValue(URI dataType, Object value, 
			EnumAllowedValues allowed) throws Exception { 
		
		MyAttributeValue att = null;
		if (dataType.equals(UriEncoding.ANY_URI_SHA_256)){
			att = new MyEncodingUriSha256(value, allowed); 
			
		} else if (dataType.equals(UriEncoding.ANY_URI_UTF_8)){
			att = new MyEncodingUriUtf8(value, allowed);
			
		} else if (dataType.equals(UriEncoding.BOOLEAN_UNSIGNED)){
			att = new MyEncodingBoolean(value, allowed);
			
		} else if (dataType.equals(UriEncoding.DATE_1870_UNSIGNED)){
			att = new MyEncodingDateSince1870(value, allowed);
			
		} else if (dataType.equals(UriEncoding.DATE_2010_UNSIGNED)){
			att = new MyEncodingDateSince2010(value, allowed);
			
		} else if (dataType.equals(UriEncoding.DATE_TIME_UNIX_SIGNED)){
			att = new MyEncodingDateTimeSigned(value, allowed);
			
		} else if (dataType.equals(UriEncoding.DATE_TIME_UNIX_UNSIGNED)){
			att = new MyEncodingDateTimeUnsigned(value, allowed);
			
		} else if (dataType.equals(UriEncoding.DATE_UNIX_SIGNED)){
			att = new MyEncodingDateSigned(value, allowed);
			
		} else if (dataType.equals(UriEncoding.DATE_UNIX_UNSIGNED)){
			att = new MyEncodingDateUnsigned(value, allowed);
			
		} else if (dataType.equals(UriEncoding.INTEGER_SIGNED)){
			att = new MyEncodingIntegerSigned(value, allowed);
			
		} else if (dataType.equals(UriEncoding.INTEGER_UNSIGNED)){
			att = new MyEncodingIntegerUnsigned(value, allowed);
			
		} else if (dataType.equals(UriEncoding.SHA_256)){
			att = new MyEncodingStringSha256(value, allowed);
			
		} else if (dataType.equals(UriEncoding.STRING_PRIME)){
			att = new MyEncodingStringPrime(value, allowed);
			
		} else if (dataType.equals(UriEncoding.UTF_8)){
			att = new MyEncodingStringUtf8(value, allowed);
			
		} else {
			throw new Exception("Data Type " + dataType.toString() + " not a valid URI");
			
		}
		return att;
	}

	public boolean isComplete() throws UxException{
		if (labelValuesMap.size()!=(labels.size())){
			return false; 
			
		}
		ArrayList<URI> failed = new ArrayList<>();
		String failedList = "{ ";
		for (URI label : this.labels){
			if (labelValuesMap.get(label)==null){ 
				failed.add(label);
				failedList += label.toString() + ", ";
				
			}
		}
		if (failed.isEmpty()){
			return true;
			
		} else {
			failedList = failedList.substring(0, failedList.length()-3) + "}";
			this.clear();
			throw new UxException("The following attributes have not been completed: " + failedList);
			
		}
	}
	
	public boolean isCommited() {
		return commited;
		
	}

	public ArrayList<URI> getLabels() {
		return labels;
	
	}

	public HashMap<URI, Object> getLabelValuesMap() {
		return labelValuesMap;
	
	}
	
	public CredentialSpecification getCredSpec() {
		return credSpec;
		
	}
}
