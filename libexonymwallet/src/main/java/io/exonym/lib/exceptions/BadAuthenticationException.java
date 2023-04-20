package io.exonym.lib.exceptions;

import io.exonym.lib.pojo.KeyContainer;

public class BadAuthenticationException extends SecurityException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final byte[] received;
	private KeyContainer keys;
	private String channel;

	public BadAuthenticationException(String msg, byte[] received) {
		super(msg);
		this.received=received;
		
	}

	public KeyContainer getKeys() {
		return keys;
	}

	public void setKeys(KeyContainer keys) {
		this.keys = keys;
	}

	public byte[] getReceived() {
		return received;
	}

	@Override
	public synchronized Throwable initCause(Throwable cause) {
		return super.initCause(cause);
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	
}
