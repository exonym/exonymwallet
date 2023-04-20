package io.exonym.lib.exceptions;

public class PeerDiscoveryException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final String nym; 

	public PeerDiscoveryException(String exonym, Throwable e) {
		super("Failed to find Peer - exonym=" + exonym, e);
		this.nym=exonym;
		
	}

	public String getNym() {
		return nym;
	}
	

}
