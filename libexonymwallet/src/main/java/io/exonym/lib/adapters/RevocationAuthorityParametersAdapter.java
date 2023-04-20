package io.exonym.lib.adapters;

import eu.abc4trust.xml.RevocationAuthorityParameters;
import io.exonym.lib.pojo.Namespace;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement(name="RevocationAuthorityParametersAdapter")
public class RevocationAuthorityParametersAdapter implements Serializable {

	private static final long serialVersionUID = 1L;
	@XmlElement(name = "RevocationAuthorityParameters", namespace = Namespace.ABC)
	private RevocationAuthorityParameters parameters;
	
	public RevocationAuthorityParameters getParameters() {
		return parameters;
	}
	
	public void setParameters(RevocationAuthorityParameters parameters) {
		this.parameters = parameters;
		
	}
}
