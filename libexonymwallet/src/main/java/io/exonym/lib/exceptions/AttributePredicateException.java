package io.exonym.lib.exceptions;

import eu.abc4trust.xml.AttributePredicate;

public class AttributePredicateException extends Exception {

	private final AttributePredicate token;
	
	public AttributePredicateException(AttributePredicate token) {
		this.token=token;
		
	}

	public AttributePredicate getToken() {
		return token;
	}

}
