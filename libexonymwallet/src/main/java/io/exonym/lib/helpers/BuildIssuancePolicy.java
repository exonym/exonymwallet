package io.exonym.lib.helpers;

import eu.abc4trust.xml.*;
import io.exonym.lib.pojo.Namespace;
import io.exonym.lib.pojo.XContainer;

import java.net.URI;

public class BuildIssuancePolicy {
	
	private static final ObjectFactory of = new ObjectFactory();
	private IssuancePolicy ip = of.createIssuancePolicy();
	private CredentialTemplate ct = of.createCredentialTemplate();
	
	/*
	 * TODO Allow for Issuance Policy imports for editing.
	 * 
	 */
	public BuildIssuancePolicy(URI credSpec, URI issuerParameters) throws Exception {
		PresentationPolicy pp = of.createPresentationPolicy();
		ip.setVersion(NamespaceMngt.VERSION);
		ip.setPresentationPolicy(pp);
		
		ip.setCredentialTemplate(ct);
		ct.setCredentialSpecUID(credSpec);
		ct.setIssuerParametersUID(issuerParameters);
		definePresentationPolicyUid(issuerParameters);
		
	}
	
	private void definePresentationPolicyUid(URI issuerParameters) throws Exception {
		String value = XContainer.stripUidSuffix(issuerParameters, 1);
		URI policyUid = URI.create(Namespace.URN_PREFIX_COLON + value + ":pp");
		ip.getPresentationPolicy().setPolicyUID(policyUid);
		
	}
	
	/**
	 * Helper method to add a pseudonym to the policy.
	 * 
	 * @param alias
	 * @param sameKeyBinding
	 * @param scope
	 * @param exclusive
	 */
	public void addPseudonym(String scope, boolean exclusive, String alias, String sameKeyBinding){
		PseudonymInPolicy pseudonym = of.createPseudonymInPolicy();
		pseudonym.setAlias(URI.create(alias));
		if (sameKeyBinding!=null){
			pseudonym.setSameKeyBindingAs(URI.create(sameKeyBinding));
			
		}
		pseudonym.setScope(scope);
		pseudonym.setExclusive(exclusive);
		this.addPseudonym(pseudonym);
		
	}
	
	/**
	 * Helper method to add a pseudonym to the policy.
	 * 
	 * @param alias
	 * @param sameKeyBinding
	 * @param scope
	 * @param exclusive
	 */
	public static PseudonymInPolicy createPseudonym(String scope, boolean exclusive, String alias, String sameKeyBinding){
		PseudonymInPolicy pseudonym = of.createPseudonymInPolicy();
		pseudonym.setAlias(URI.create(alias));
		if (sameKeyBinding!=null){
			pseudonym.setSameKeyBindingAs(URI.create(sameKeyBinding));
			
		}
		pseudonym.setScope(scope);
		pseudonym.setExclusive(exclusive);
		return pseudonym;
		
	}	
	
	public void addPseudonym(PseudonymInPolicy pseudonym){
		ip.getPresentationPolicy().getPseudonym().add(pseudonym);
		ip.getCredentialTemplate().setSameKeyBindingAs(pseudonym.getAlias());
		
	}
	
	public IssuancePolicy getIssuancePolicy() {
		return ip;
	}
	
	public PresentationPolicy getPresentaionPolicy(){
		return ip.getPresentationPolicy();
		
	}
	
}
