package io.exonym.lib.exceptions;

import java.util.List;

public class PolicyNotSatisfiedException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<CredentialInTokenException> unsatisfiedCredentials = null;
	private List<AttributePredicateException> missingAttributePredicates = null;
	private List<PseudonymException> pseudonymErrors = null;
	private MessageException msg = null; 
	
	public PolicyNotSatisfiedException() {
		
	}

	public List<CredentialInTokenException> getUnsatisfiedCredentials() {
		return unsatisfiedCredentials;
	}

	public void setUnsatisfiedCredentials(List<CredentialInTokenException> unsatisfiedCredentials) {
		this.unsatisfiedCredentials = unsatisfiedCredentials;
	}

	public List<AttributePredicateException> getMissingAttributePredicates() {
		return missingAttributePredicates;
	}

	public void setMissingAttributePredicates(List<AttributePredicateException> missingAttributePredicates) {
		this.missingAttributePredicates = missingAttributePredicates;
	}

	public List<PseudonymException> getPseudonymErrors() {
		return pseudonymErrors;
	}

	public void setPseudonymErrors(List<PseudonymException> pseudonymErrors) {
		this.pseudonymErrors = pseudonymErrors;
	}

	public MessageException getMsg() {
		return msg;
	}

	public void setMsg(MessageException msg) {
		this.msg = msg;
	}

}
