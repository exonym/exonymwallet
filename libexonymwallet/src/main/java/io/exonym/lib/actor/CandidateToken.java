package io.exonym.lib.actor;

import eu.abc4trust.xml.PresentationPolicy;

import java.net.URI;

public final class CandidateToken {
	
	private final PresentationPolicy presentationPolicy;
	private final OwnedCredential ownedCredential;
	private URI alias = null;
	
	public CandidateToken(OwnedCredential ownedCredential, PresentationPolicy presentationPolicy) {
		this.ownedCredential=ownedCredential;
		this.presentationPolicy=presentationPolicy;
		
	}

	public PresentationPolicy getPresentationPolicy() {
		return presentationPolicy;
		
	}

	public OwnedCredential getOwnedCredential() {
		return ownedCredential;
	}
	
	public URI getAlias() {
		return alias;
		
	}

	public void setAlias(URI alias) {
		this.alias = alias;
		
	}

	@Override
	public int hashCode() {
		if (this.ownedCredential!=null) {
			return this.ownedCredential.hashCode();

		} else if (this.presentationPolicy!=null){
			return this.presentationPolicy.hashCode();

		} else {
			return super.hashCode();

		}
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof CandidateToken){
			CandidateToken c = (CandidateToken)obj;
			if (this.ownedCredential==null && c.ownedCredential==null){
				return false;

			} else {
				return c.getOwnedCredential().equals(this.getOwnedCredential());

			}
		} else {
			return false;

		}
	}

	public String toString(){
		if (this.ownedCredential!=null){
			return ownedCredential.getCredentialSpecificationUid() + " "
					+ ownedCredential.getIssuerUid() + " " + alias;

		} else {
			return "null Owned " + super.toString();

		}
	}
}