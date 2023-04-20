package io.exonym.lib.exceptions;

import eu.abc4trust.xml.PseudonymInPolicy;
import eu.abc4trust.xml.PseudonymInToken;

public class PseudonymException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final PseudonymInPolicy nymInPolicy;
	private final PseudonymInToken nymInToken;
	
	public PseudonymException(String msg, PseudonymInPolicy nymInPolicy, PseudonymInToken nymInToken) {
		super(msg);
		this.nymInPolicy=nymInPolicy;
		this.nymInToken=nymInToken;
		
	}

	public PseudonymInPolicy getNymInPolicy() {
		return nymInPolicy;
	}

	public PseudonymInToken getNymInToken() {
		return nymInToken;
	}



}
