package io.exonym.lib.helpers;

import eu.abc4trust.xml.*;
import io.exonym.lib.pojo.Namespace;
import io.exonym.lib.pojo.Rulebook;
import io.exonym.lib.api.RulebookVerifier;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class BuildCredentialSpecification {
	
	private final CredentialSpecification spec = new ObjectFactory().createCredentialSpecification();
	public final static URI REVOCATION_HANDLE_UID = URI.create("http://abc4trust.eu/wp2/abcschemav1.0/revocationhandle");
	private AttributeDescriptions attributes = new AttributeDescriptions();
	private static ObjectFactory of = new ObjectFactory();
	
	public BuildCredentialSpecification(URI uid, boolean revocable) {
		super();
		attributes.setMaxLength(256);
		spec.setAttributeDescriptions(attributes);
		spec.setKeyBinding(true);
		spec.setRevocable(revocable);
		if (revocable){
			addAttribute(BuildCredentialSpecification.revocationHandleAttributeDescription());
			
		}
		spec.setVersion("1.0");
		spec.setSpecificationUID(uid);
		
	}
	
	public void addAttribute(AttributeDescription att){
		attributes.getAttributeDescription().add(att);
		
	}
	
	public void setKeyBinding(boolean keyBinding){
		spec.setKeyBinding(keyBinding);
		
	}

	public CredentialSpecification getCredentialSpecification() {
		return spec;
		
	}

	public AttributeDescriptions getAttributes() {
		return attributes;
		
	}

	public static AttributeDescription createAttributeDescription(URI dataType, URI encoding, URI type){
		return createAttributeDescription(dataType, encoding, type, null, null);
		
	} 
	
	public static AttributeDescription createAttributeDescription(URI dataType, URI encoding, URI type, 
			List<String> allowedValues){
		return createAttributeDescription(dataType, encoding, type, allowedValues, null);
		
	} 

	public static AttributeDescription createAttributeDescription(URI dataType, URI encoding, URI type, 
			ArrayList<FriendlyDescription> friendlyDescriptions){
		return createAttributeDescription(dataType, encoding, type, null, friendlyDescriptions);
		
	} 
	
	public static AttributeDescription createAttributeDescription(URI dataType, URI encoding, URI type, 
			List<String> allowedValues, List<FriendlyDescription> friendlyDescriptions){
		AttributeDescription at = of.createAttributeDescription();
		at.setDataType(dataType);
		at.setEncoding(encoding);
		at.setType(type);
		if (allowedValues!=null){
			at.getAllowedValue().addAll(allowedValues);
			
		}
		if (friendlyDescriptions!=null){
			at.getFriendlyAttributeName().addAll(friendlyDescriptions);	
			
		}
		return at;
		
	} 
	
	public static AttributeDescription revocationHandleAttributeDescription(){
		AttributeDescription result = new AttributeDescription();
	    result.setType(BuildCredentialSpecification.REVOCATION_HANDLE_UID);
	    result.setDataType(URI.create("xs:integer"));
	    result.setEncoding(URI.create("urn:abc4trust:1.0:encoding:integer:unsigned"));
	    return result;

	}

	public static CredentialSpecification buildSybilCredentialSpecification(RulebookVerifier verifier){
		BuildCredentialSpecification bcs = new BuildCredentialSpecification(
				verifier.getRulebook().computeCredentialSpecId(), true);

		AttributeDescription ad = BuildCredentialSpecification
				.createAttributeDescription(UriDataType.ANY_URI, UriEncoding.STRING_PRIME,
						URI.create(Namespace.URN_PREFIX_COLON + "sybil-class"));

		List<String> allowed = ad.getAllowedValue();
		allowed.add(Rulebook.SYBIL_CLASS_PERSON);
		allowed.add(Rulebook.SYBIL_CLASS_ENTITY);
		allowed.add(Rulebook.SYBIL_CLASS_REPRESENTATIVE);
		allowed.add(Rulebook.SYBIL_CLASS_ROBOT);
		allowed.add(Rulebook.SYBIL_CLASS_PRODUCT);
		bcs.addAttribute(ad);
		return bcs.getCredentialSpecification();

	}

}
