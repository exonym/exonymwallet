package io.exonym.lib.adapters;

import eu.abc4trust.xml.IssuerParameters;
import io.exonym.lib.pojo.Namespace;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement(name="IssuerParametersAdapter")
public class IssuerParametersAdapter implements Serializable {

	private static final long serialVersionUID = 1L;
	@XmlElement(name = "IssuerParameters", namespace = Namespace.ABC)
	private IssuerParameters token;
	
	public IssuerParameters getToken() {
		return token;
	}
	public void set(IssuerParameters token) {
		this.token = token;
	}
	
}
