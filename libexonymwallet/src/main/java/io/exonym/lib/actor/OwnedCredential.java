package io.exonym.lib.actor;

import java.net.URI;

public final class OwnedCredential {
	
	private final URI credentialSpecificationUid;
	private final URI credentialUid;
	private final URI issuerUid;
	private final URI revocationAuthoirityUid;
	private final URI inspectorUid;

	public OwnedCredential(URI credentialSpecificationUid, URI credentialUid, URI issuerUid, URI revocationAuthoirityUid, URI inspectorUid) {
		this.credentialSpecificationUid=credentialSpecificationUid;
		this.credentialUid=credentialUid;
		this.issuerUid=issuerUid;
		this.revocationAuthoirityUid=revocationAuthoirityUid;
		this.inspectorUid=inspectorUid;

	}
	
	public OwnedCredential(URI credentialSpecificationUid, URI credentialUid, URI issuerUid, URI revocationAuthoirityUid) {
		this.credentialSpecificationUid=credentialSpecificationUid;
		this.credentialUid=credentialUid;
		this.issuerUid=issuerUid;
		this.revocationAuthoirityUid=revocationAuthoirityUid;
		this.inspectorUid=null;

	}
	
	public OwnedCredential(URI credentialSpecificationUid, URI credentialUid, URI issuerUid) {
		this.credentialSpecificationUid=credentialSpecificationUid;
		this.credentialUid=credentialUid;
		this.issuerUid=issuerUid;
		this.revocationAuthoirityUid=null;
		this.inspectorUid=null;

	}

	public URI getCredentialSpecificationUid() {
		return credentialSpecificationUid;
	}

	public URI getIssuerUid() {
		return issuerUid;
	}

	public URI getRevocationAuthoirityUid() {
		return revocationAuthoirityUid;
	}

	public URI getInspectorUid() {
		return inspectorUid;
	}
	
	public URI getCredentialUid() {
		return credentialUid;
	}

	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof OwnedCredential){
			OwnedCredential c = (OwnedCredential)obj;
			return c.toString().equals(this.toString());

		} else {
			return false;

		}
	}

	public String toString(){
		return credentialSpecificationUid + " " + issuerUid;
		
	}
}
