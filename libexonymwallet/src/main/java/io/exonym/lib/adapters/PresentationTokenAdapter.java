package io.exonym.lib.adapters;

import eu.abc4trust.xml.PresentationToken;
import io.exonym.lib.pojo.Namespace;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement(name="PresentationTokenAdapter")
public class PresentationTokenAdapter implements Serializable {

	private static final long serialVersionUID = 1L;
	@XmlElement(name = "PresentationToken", namespace = Namespace.ABC)
	private PresentationToken token;
	
	public PresentationToken getToken() {
		return token;
		
	}
	public void setToken(PresentationToken token) {
		this.token = token;
		
	}
	
	public static PresentationTokenAdapter make(PresentationToken token){
		PresentationTokenAdapter p = new PresentationTokenAdapter();
		p.setToken(token);
		return p;
		
	}
}
