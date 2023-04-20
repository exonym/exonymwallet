package io.exonym.lib.adapters;

import eu.abc4trust.xml.InspectorPublicKey;
import io.exonym.lib.pojo.Namespace;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@XmlRootElement(name="InspectorParametersAdapter")
public class InspectorParametersAdapter implements Serializable {

	private static final long serialVersionUID = 1L;
	
	@XmlElement(name = "InspectorParameters", namespace = Namespace.ABC)
	private InspectorPublicKey inspectorParameters;

	public InspectorPublicKey getInspectorParameters() {
		return inspectorParameters;
	}

	public void setInspectorParameters(InspectorPublicKey inspectorParameters) {
		this.inspectorParameters = inspectorParameters;
	}
		
}