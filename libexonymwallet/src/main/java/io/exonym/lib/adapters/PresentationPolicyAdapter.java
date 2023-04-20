package io.exonym.lib.adapters;

import eu.abc4trust.xml.PresentationPolicy;
import io.exonym.lib.pojo.Namespace;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.net.URI;

@XmlRootElement(name="PresentationPolicyAdapter")
public class PresentationPolicyAdapter implements Serializable {

	private static final long serialVersionUID = 1L;
	@XmlElement(name = "PresentationPolicy", namespace = Namespace.ABC)
	private PresentationPolicy policy;
	
	@XmlElement(name = "CandidateUID", namespace = Namespace.EX)
	private URI candidateUid;
	
	@XmlElement(name = "Index", namespace = Namespace.EX)
	private int index;
	
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public URI getCandidateUid() {
		return candidateUid;
	}
	public void setCandidateUid(URI candidateUid) {
		this.candidateUid = candidateUid;
	}
	public PresentationPolicy getPolicy() {
		return policy;
	}
	public void setPolicy(PresentationPolicy policy) {
		this.policy = policy;
	}
	
	public static PresentationPolicyAdapter make(PresentationPolicy policy){
		PresentationPolicyAdapter p = new PresentationPolicyAdapter();
		p.setPolicy(policy);
		return p; 
		
	}
}
