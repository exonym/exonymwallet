package io.exonym.lib.adapters;

import eu.abc4trust.xml.Message;
import eu.abc4trust.xml.PresentationPolicy;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import io.exonym.lib.pojo.Namespace;
import io.exonym.lib.standard.CryptoUtils;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.net.URI;


@XmlRootElement(name="PresentationPolicyAdapter", namespace = Namespace.EX)
public class PresentationPolicyAlternativesAdapter implements Serializable {

	private static final long serialVersionUID = 1L;
	private PresentationPolicyAlternatives policy;
	private URI presentationPolicyAlternativeUid; 
	
	@XmlElement(name = "PresentationPolicyAlternatives", namespace = Namespace.ABC)
	public PresentationPolicyAlternatives getPolicy() {
		return policy;
		
	}
	public void setPolicy(PresentationPolicyAlternatives policy) {
		this.policy = policy;
		
	}

	@XmlElement(name = "PresentationPolicyAlternativesUID", namespace = Namespace.EX)
	public URI getPresentationPolicyAlternativeUid() {
		return presentationPolicyAlternativeUid;
		
	}
	
	public void setPresentationPolicyAlternativeUid(URI presentationPolicyAlternativeUid) {
		this.presentationPolicyAlternativeUid = presentationPolicyAlternativeUid;
		
	}
	
	public static PresentationPolicyAlternativesAdapter make(PresentationPolicyAlternatives policy, URI uid){
		PresentationPolicyAlternativesAdapter p = new PresentationPolicyAlternativesAdapter();
		p.setPolicy(policy);
		p.setPresentationPolicyAlternativeUid(uid);
		return p; 
		
	}
	
	public static PresentationPolicyAlternativesAdapter make(PresentationPolicyAlternatives policy){
		return make(policy, null);
		
	}
	
	public void makeInteractive() throws Exception{
		Message msg = new Message();
		byte[] nonce = CryptoUtils.generateNonce(16);
		msg.setNonce(nonce);
		
		if (policy!=null){
			for (PresentationPolicy pp : policy.getPresentationPolicy()){
				pp.setMessage(msg);
			
			}
		} else {
			throw new Exception("Policy was null");
			
		}
	}
}
