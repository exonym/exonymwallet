package io.exonym.lib.pojo;

import io.exonym.lib.standard.SymStoreKey;

import java.io.Serializable;

// TODO visibility after port to dac
// All to be protected 
public class SymComKeys implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private SymStoreKey keyOut, keyIn;
	
	public void setKeyOut(SymStoreKey keyOut) {
		this.keyOut = keyOut;
		
	}

	public void setKeyIn(SymStoreKey keyIn) {
		this.keyIn = keyIn;
		
	}

	public SymStoreKey getKeyOut() {
		return keyOut;
		
	}

	public SymStoreKey getKeyIn() {
		return keyIn;
		
	}
	
	public static SymComKeys[] generate(){
		SymStoreKey in = new SymStoreKey();
		SymStoreKey out = new SymStoreKey();
		SymComKeys server = new SymComKeys();
		server.setKeyIn(in);
		server.setKeyOut(out);
		
		SymComKeys client = new SymComKeys();
		client.setKeyIn(out);
		client.setKeyOut(in);
		return new SymComKeys[] {server, client};
		
	}
}
