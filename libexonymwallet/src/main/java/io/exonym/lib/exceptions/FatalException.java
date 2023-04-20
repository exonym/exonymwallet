package io.exonym.lib.exceptions;

public class FatalException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public FatalException(String msg) {
		super(msg);
		
	}	
	
	public FatalException(String msg, Throwable e) {
		super(msg, e);
		
	}

}
