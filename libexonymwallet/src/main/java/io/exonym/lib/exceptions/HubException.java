package io.exonym.lib.exceptions;

import java.net.URI;
import java.util.ArrayList;

public class HubException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String context;
	private URI connectUid;
	


	private ArrayList<String> info;

	public HubException(String msg) {
		super(msg);
	}

	public HubException(String msg, String... required) {
		super(msg);
		populateRequired(required);

	}

	public HubException(String msg, Throwable e, String... required) {
		super(msg, e);
		populateRequired(required);
	}

	private void populateRequired(String[] required) {
		info = new ArrayList<>();
		for (String a : required){
			info.add(a);
		}
	}

	public HubException(String msg, Throwable e){
		super(msg, e);

	}



	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}

	public URI getConnectUid() {
		return connectUid;
	}

	public void setConnectUid(URI connectUid) {
		this.connectUid = connectUid;
	}

	@Override
	public synchronized Throwable initCause(Throwable cause) {
		return super.initCause(cause);
	}
}