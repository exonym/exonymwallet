package io.exonym.lib.helpers;

import eu.abc4trust.xml.*;
import eu.abc4trust.xml.CredentialInPolicy.IssuerAlternatives;
import io.exonym.lib.pojo.Namespace;
import io.exonym.lib.pojo.XContainer;
import io.exonym.lib.standard.Const;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

public class PresentationPolicyManager {


	private final static Logger logger = Logger.getLogger(PresentationPolicyManager.class.getName());
	public final static URI ROOT_ALIAS = URI.create("urn:io:exonym");
	private final PresentationPolicyAlternatives ppa;  
	private CredentialInPolicy cip = null;
	private final CredentialSpecification cSpec;
	private HashMap<URI, List<IssuerAlternatives.IssuerParametersUID>> issuerUrisByNode = new HashMap<>();
	private HashMap<URI, URI> insUriByNode = new HashMap<>();
	private HashMap<String, PseudonymInPolicy> pips = new HashMap<>();

	public PresentationPolicyManager(PresentationPolicyAlternatives ppa, CredentialSpecification cspec) throws Exception {
		this.ppa = ppa;
		this.cSpec = cspec;
		extract();

	}

	public PresentationPolicyManager(PresentationPolicy pp, CredentialSpecification cSpec) throws Exception {
		PresentationPolicyAlternatives ppa = new PresentationPolicyAlternatives();
		ppa.getPresentationPolicy().add(pp);
		this.cSpec = cSpec;
		this.ppa = ppa;
		extract();

	}


	private void extract() throws Exception {
		ArrayList<PresentationPolicy> policies = (ArrayList<PresentationPolicy>) this.ppa.getPresentationPolicy();

		for (PresentationPolicy p : policies) {
			for (PseudonymInPolicy nym : p.getPseudonym()) {
				if (nym!=null && nym.getScope()!=null) {
					pips.put(nym.getScope(), nym);
					
				} else {
					String s = (nym!=null ? nym.getScope() : null);
					logger.warning("DROPPING nym " + s);
					
				}
			}
			if (p.getCredential().size()>1){
				throw new Exception();

			}
			for (CredentialInPolicy c : p.getCredential()) {
				this.cip = c;
				List<IssuerAlternatives.IssuerParametersUID> params = c.getIssuerAlternatives().getIssuerParametersUID();
				for (IssuerAlternatives.IssuerParametersUID ipuid : params) {
					URI rootUid = createRootUid(ipuid.getValue());
					List<IssuerAlternatives.IssuerParametersUID> issuers = issuerUrisByNode.remove(rootUid);
					if (issuers==null){
						issuers = new ArrayList<>();

					}
					issuers.add(ipuid);
					issuerUrisByNode.put(rootUid, issuers);
					insUriByNode.putIfAbsent(rootUid, URI.create(rootUid.toString() + ":ins"));
					
				}
			}
		}
	}
	
	private URI createRootUid(URI uid) throws Exception {
		String[] parts = uid.toString().split(":");
		if (parts.length==5) {
			return uid;

		} else if (parts.length < 5) {
			throw new Exception("The UID was invalid " + uid);
			
		}
		return URI.create(parts[0] + ":" + parts[1] + ":" + parts[2] + ":" + parts[3] + ":" + parts[4]);
		
	}

	public void addNym(ArrayList<String> scopes) {
		addNym(scopes, null);
	}

	public void addNym(ArrayList<String> scopes, URI keyBinding) {
		PseudonymInPolicy template = pips.get(Const.BINDING_ALIAS);
		
		for (String scope : scopes) {
			PseudonymInPolicy pip = new PseudonymInPolicy();
			pip.setExclusive(true);
			keyBinding = (keyBinding==null ? template.getAlias() : keyBinding);
			logger.fine("keyBinding=" + keyBinding);
			pip.setSameKeyBindingAs(keyBinding);
			pip.setScope(scope);
			logger.fine("Adding scope to policy:" + scope);
			pips.put(scope, pip);
			
		}
	}
	
	public void addNym(String scope) {
		ArrayList<String> t = new ArrayList<String>();
		t.add(scope);
		addNym(t);
		
	}

	public void addIssuer(IssuerParameters i, InspectorPublicKey ins) throws Exception {
		if (cip==null){
			cip = buildCredentialInPolicy(cSpec, i, ins.getPublicKeyUID());

		}
		URI iUid = i.getParametersUID();
		URI rapUid = i.getRevocationParametersUID();
		String raiStart = Namespace.URN_PREFIX_COLON + XContainer.stripUidSuffix(rapUid, 2) + ":rai";

		IssuerAlternatives.IssuerParametersUID ip = new IssuerAlternatives.IssuerParametersUID();
		ip.setRevocationInformationUID(URI.create(raiStart));
		ip.setValue(iUid);
		ArrayList<IssuerAlternatives.IssuerParametersUID> l = new ArrayList<>();
		l.add(ip);

		URI root = createRootUid(iUid);

		issuerUrisByNode.putIfAbsent(root, l);
		insUriByNode.putIfAbsent(root, ins.getPublicKeyUID());

	}
	
	public void removeIssuer(ArrayList<URI> uids) throws Exception {
		for (URI uid : uids) {
			removeIssuer(uid);
			
		}
	}
	
	public void removeIssuer(URI uid) throws Exception  {
		URI nodeUid = createRootUid(uid);
		issuerUrisByNode.remove(nodeUid);
		insUriByNode.remove(nodeUid);

	}
	
	public void removeNym(ArrayList<String> scopes) {
		for (String scope : scopes) {
			removeNym(scope);
			
		}
	}
	
	public void removeNym(String scope) {
		this.pips.remove(scope);
		
	}
	
	public PresentationPolicy build() {
		PresentationPolicy policy = ppa.getPresentationPolicy().get(0);
		policy.getCredential().clear();
		policy.getPseudonym().clear();
		// Build Credential In Policy
		// Add Single Credential In Policy
		AttributeInPolicy.InspectorAlternatives ia = cip.getDisclosedAttribute().get(0)
				.getInspectorAlternatives();

		ia.getInspectorPublicKeyUID().clear();
		// cip.getIssuerAlternatives().getIssuerParametersUID().clear();
		ArrayList<IssuerAlternatives.IssuerParametersUID> newList = new ArrayList<>();

		for (URI root : issuerUrisByNode.keySet()){
			List<IssuerAlternatives.IssuerParametersUID> ipuid = issuerUrisByNode.get(root);
			if (!ipuid.isEmpty()){
				newList.addAll(ipuid);

			}
		}
		cip.getIssuerAlternatives().getIssuerParametersUID().clear();
		cip.getIssuerAlternatives().getIssuerParametersUID().addAll(newList);
		if (!cip.getIssuerAlternatives().getIssuerParametersUID().isEmpty()){
			policy.getCredential().add(cip);

		}
		cip.getDisclosedAttribute().get(0).getInspectorAlternatives().getInspectorPublicKeyUID().addAll(insUriByNode.values());
		policy.getPseudonym().addAll(pips.values());
		return policy;
		
	}

	private CredentialInPolicy buildCredentialInPolicy(CredentialSpecification cred, IssuerParameters ip, URI inspectorUid) throws Exception {
		String root = Namespace.URN_PREFIX_COLON + XContainer.stripUidSuffix(ip.getRevocationParametersUID().toString(), 2);
		URI cUid = cred.getSpecificationUID();
		URI raiUid = URI.create(root + ":rai");

		CredentialInPolicy cip = new CredentialInPolicy();
		CredentialInPolicy.CredentialSpecAlternatives csa = new CredentialInPolicy.CredentialSpecAlternatives();
		cip.setCredentialSpecAlternatives(csa);
		cip.getCredentialSpecAlternatives().getCredentialSpecUID().add(cUid);
		IssuerAlternatives issuerAlternatives = new IssuerAlternatives();
		ArrayList<IssuerAlternatives.IssuerParametersUID> list = BuildPresentationPolicy.startIssuerParams(ip.getParametersUID(), raiUid);
		issuerAlternatives.getIssuerParametersUID().addAll(list);
		cip.setIssuerAlternatives(issuerAlternatives);

		AttributeDescription ad = cred.getAttributeDescriptions().getAttributeDescription().get(0);
		AttributeInPolicy aip = new AttributeInPolicy();
		aip.setAttributeType(ad.getType());
		aip.setInspectionGrounds("The value can be inspected on presentation of proof that the creator breached network policy");
		AttributeInPolicy.InspectorAlternatives ia = new AttributeInPolicy.InspectorAlternatives();
		ia.getInspectorPublicKeyUID().add(inspectorUid);
		aip.setInspectorAlternatives(ia);
		cip.getDisclosedAttribute().add(aip);

		cip.setSameKeyBindingAs(ROOT_ALIAS);

		return cip;

	}

	public CredentialInPolicy getCredentialInPolicy() {
		return cip;
	}

	public boolean hasIssuer(URI issuerUid) throws Exception {
		IssuerAlternatives ia = cip.getIssuerAlternatives();
		for (IssuerAlternatives.IssuerParametersUID ip : ia.getIssuerParametersUID()) {
			if (ip.getValue().equals(issuerUid)) {
				return true;

			}
		}
		return false;
			
	}
	
	public boolean hasScope(String scope) {
		return pips.containsKey(scope);
		
	}

}
