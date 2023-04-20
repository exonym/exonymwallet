package io.exonym.lib.actor;

import eu.abc4trust.xml.AttributeInToken;
import eu.abc4trust.xml.AttributePredicate;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class PolicyReviewItem {
	
	private List<AttributeInToken> attributes = new ArrayList<>();
	private List<AttributePredicate> attributePredicates = new ArrayList<>();
	private final URI alias;
	
	public PolicyReviewItem(URI alias) {
		this.alias=alias;
		
	}
	
	public void addAttributes(AttributeInToken attribute){
		if (attribute!=null){
			this.attributes.add(attribute);
			
		}
	}	
	
	public void addAttributes(List<AttributeInToken> attributes){
		if (attributes!=null){
			this.attributes.addAll(attributes);
			
		}
	}
	
	public void addAttributePredicates(AttributePredicate predicate){
		if (predicate!=null){
			this.attributePredicates.add(predicate);
			
		}
	}	
	
	public void addAttributePredicates(List<AttributePredicate> predicates){
		if (predicates!=null){
			this.attributePredicates.addAll(predicates);
			
		}
	}

	public List<AttributeInToken> getAttributes() {
		return attributes;
	}

	public List<AttributePredicate> getAttributePredicates() {
		return attributePredicates;
	}


	public URI getAlias() {
		return alias;
	}

	@Override
	public String toString(){
		return alias + ": attributes (" + attributes.size() + ") predicates (" 
						+ attributePredicates.size() + ")";
		
	}
}
