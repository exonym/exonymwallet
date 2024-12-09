package io.exonym.lib.helpers;

import eu.abc4trust.xml.*;
import eu.abc4trust.xml.AttributeInPolicy.InspectorAlternatives;
import eu.abc4trust.xml.CredentialInPolicy.IssuerAlternatives.IssuerParametersUID;
import io.exonym.lib.actor.CandidateToken;
import io.exonym.lib.actor.OwnedCredential;
import io.exonym.lib.actor.PolicyReviewItem;
import io.exonym.lib.exceptions.ErrorMessages;
import io.exonym.lib.exceptions.UxException;
import io.exonym.lib.pojo.ExternalResourceContainer;
import io.exonym.lib.pojo.IdContainer;

import java.net.URI;
import java.util.*;
import java.util.logging.Logger;

public class BuildPresentationTokenDescription {

	private final static Logger logger = Logger.getLogger(BuildPresentationTokenDescription.class.getName());
	private final ObjectFactory of = new ObjectFactory();
	private PresentationTokenDescription token = null;
	private final List<PresentationPolicy> policies; 
	private final ExternalResourceContainer external;
	
	private HashMap<URI, HashSet<CandidateToken>> policyToCandidateMap = new HashMap<>();
	private HashMap<CandidateToken, URI> candidateToPolicyUidMap = new HashMap<>();
	private HashMap<URI, PresentationPolicy> policyUidToPolicyMap = new HashMap<>();
	private HashMap<URI, CandidateToken> credentialAliasToCandidateTokenMap = new HashMap<>();
	private HashMap<URI, PolicyReviewItem> aliasToPolicyReviewItemMap = new HashMap<>();
	private HashMap<AttributeInPolicy, URI> attributeToInspectorMap = new HashMap<>();
	private HashMap<String, URI> issuerToInspectorMap = new HashMap<>();
	
	private HashSet<OwnedCredential> ownedCredentials = null;
	
	/**
	 * 1) Enter either PresentationPolicyAlternatives or a PresentationPolicy <p>
	 * 
	 * 2) setOwnedCredentials() with a list of credentials, issuers, inspectors, 
	 * and Revocation Authorities that the Owner possesses. <p> 
	 * 
	 * 3) [If there are choices as to which Policy can be fulfilled, 
	 * select the policy, and the associated credential from the returned hashmap] <p>
	 * 
	 * 3a) If the policy cannot be fulfilled based on the owned credentials, 
	 * the function throws a UxException<p> 
	 * 
	 * 4) get the PresentationTokenDescription that the user <b>should</b> able to 
	 * fulfill based on the UIDs passed to the function in step 2.   
	 *  
	 * @param policy
	 */
	public BuildPresentationTokenDescription(PresentationPolicyAlternatives policy, ExternalResourceContainer external) {
		if (policy==null){ throw new RuntimeException("Presentation Policy Alternatives not defined"); }
		this.policies = policy.getPresentationPolicy();
		this.external=external;
		associateInspectors();
		
	}

	/**
	 * 1) Enter either PresentationPolicyAlternatives or a PresentationPolicy <p>
	 * 
	 * 2) setOwnedCredentials() with a list of credentials, issuers, inspectors, 
	 * and Revocation Authorities that the Owner possesses. <p> 
	 * 
	 * 3) [If there are choices as to which Policy can be fulfilled, 
	 * select the policy, and the associated credential from the returned hashmap] <p>
	 * 
	 * 3a) If the policy cannot be fulfilled based on the owned credentials, 
	 * the function throws a UxException<p> 
	 * 
	 * 4) get the PresentationTokenDescription that the user <b>should</b> able to 
	 * fulfill based on the UIDs passed to the function in step 2.   
	 *  
	 * @param policy
	 */
	public BuildPresentationTokenDescription(PresentationPolicy policy, ExternalResourceContainer external) {
		this.policies = new ArrayList<>();
		this.policies.add(policy);
		this.external=external;
		associateInspectors();
		
	}
	
	private void associateInspectors() {
		for (PresentationPolicy p : this.policies) {
			for (CredentialInPolicy cip : p.getCredential()) {
				for (AttributeInPolicy aip : cip.getDisclosedAttribute()) {

					InspectorAlternatives ia = aip.getInspectorAlternatives(); 
					if (ia!=null) {
						List<URI> ins = ia.getInspectorPublicKeyUID();
						for (URI inspector : ins){
							String name = computeNodeNameFromInspector(inspector);
							this.issuerToInspectorMap.put(name, inspector);

						}
						if (ins!=null && !ins.isEmpty()) {
							this.attributeToInspectorMap.put(aip, ins.get(0));
							
						}
						if (ins.size() > 1) {
							logger.warning("Selecting only the first inspector in the list - choices of inspector has not been implemented.");
							
						}
					}
					
				}
			}
		}
	}

	private String computeNodeNameFromIssuer(URI uri) {
		String[] parts = uri.toString().split(":");
		return parts[parts.length-3];
	}

	private String computeNodeNameFromInspector(URI uri) {
		String[] parts = uri.toString().split(":");
		return parts[parts.length-2];

	}

	/**
	 * See also Constructor javadoc<p>
	 * 
	 * If this function returns null, there are no choices to be made and the 
	 * PresentationTokenDescription should be available. <p>
	 * 
	 * If this returns null and the PresentationTokenDescription is also null;
	 * The Policy cannot be satisfied. <p>
	 * 
	 * If a policy has multiple credentials with the same alias, 
	 * the user must choose which credential they want to use. <p>
	 * 
	 * If a credential has different aliases then then they are 
	 * all needed to satisfy the policy. 
	 * 
	 * @param ownedCredentials
	 * @return
	 * @throws Exception 
	 */
	public HashMap<URI, HashSet<CandidateToken>>  setOwnedCredentials(ArrayList<OwnedCredential> ownedCredentials) throws Exception {
		for (OwnedCredential cred : ownedCredentials){
			logger.info("Adding Owned: " + cred.toString());
		}
		this.ownedCredentials = new HashSet<>();
		this.ownedCredentials.addAll(ownedCredentials);

		for (OwnedCredential o : ownedCredentials){
			logger.info("User owns credential " + o);

		}
		this.policyToCandidateMap.clear();
		this.candidateToPolicyUidMap.clear();
		this.policyUidToPolicyMap.clear();
		this.credentialAliasToCandidateTokenMap.clear();
		this.token = null;
		HashMap<URI, HashSet<CandidateToken>> result = collectCandidates();
		if (result.isEmpty()){
			logger.info("There were no candidate Credentials");
			return null;  // Cannot be satisfied.
			
		} else if (result.keySet().size()==1){

			for (URI policyUid : result.keySet()){
				HashSet<CandidateToken> candidates = result.get(policyUid);
				logger.fine("empty=" + candidates.isEmpty() + " size=" + candidates.size());

				if (candidates.isEmpty() || candidates.size()==1){
					logger.info("There was only one candidate Credentials");
					selectTokens(candidates, this.attributeToInspectorMap); // TODO - inspector choice is first in list, rather than option.
					
				} else {
					logger.info("There were many candidate Credentials");
					HashSet<URI> set = new HashSet<>();

					for (CandidateToken candidate : candidates){
						set.add(candidate.getAlias());
						
					}
					if (set.size() == candidates.size()){
						selectTokens(candidates, this.attributeToInspectorMap); 
						
					}
				}
			}
			return null;
			
		}
		return result;
		
	}

	private HashMap<URI, HashSet<CandidateToken>> collectCandidates() throws Exception {
		for (PresentationPolicy policy: policies){
			URI policyUid = policy.getPolicyUID();
			this.policyUidToPolicyMap.put(policyUid, policy);
			HashSet<CandidateToken> candidates = new HashSet<>();
			List<CredentialInPolicy> credentials = policy.getCredential();
			Set<URI> credentialUidsInPolicy = new HashSet<>();
			Set<URI> owned = new HashSet<>();
			
			if (!policy.getCredential().isEmpty()){
				for (CredentialInPolicy c : credentials){
					List<URI> possibleCredSpecs = c.getCredentialSpecAlternatives().getCredentialSpecUID();
					List<IssuerParametersUID> possibleIssuers = c.getIssuerAlternatives().getIssuerParametersUID();
					credentialUidsInPolicy.add(c.getAlias());
					
					for (OwnedCredential credential: ownedCredentials){
						if (possibleCredSpecs.contains(credential.getCredentialSpecificationUid())){
							ArrayList<URI> il = new ArrayList<>();
					
							for (IssuerParametersUID ip : possibleIssuers){
								il.add(ip.getValue());

							}
							if (il.contains(credential.getIssuerUid())){
								CandidateToken ct = new CandidateToken(credential, policy);
								candidates.add(ct);
								ct.setAlias(c.getAlias());
								owned.add(c.getAlias());
								
								candidateToPolicyUidMap.put(ct, policyUid);
								
							}
						}
					}
				}
			} else {
				HashSet<CandidateToken> cts = new HashSet<>();
				CandidateToken ct = new CandidateToken(null, policy);
				cts.add(ct);
				policyToCandidateMap.put(policyUid, cts);
				candidateToPolicyUidMap.put(ct, policyUid);
				
			}
			if (!candidates.isEmpty() && owned.containsAll(credentialUidsInPolicy)){
				policyToCandidateMap.put(policyUid, candidates);
				
			}
			for (CandidateToken c : candidates){
				logger.info("Candidate " + c.toString());
			}
			for (URI o : owned){
				logger.info("Owned " + o);
			}
			for (URI o : credentialUidsInPolicy){
				logger.info("UIDS in Policy " + o);
			}

		}

		if (policyToCandidateMap.isEmpty()){
			String[] ps = new String[policies.size()];
			int i = 0;
			for (PresentationPolicy policy : policies){
				ps[i] = IdContainer.convertObjectToXml(policy);
				i++;

			}
			throw new UxException(ErrorMessages.INSUFFICIENT_PRIVILEGES, ps);
			
		} else {
			for (URI uid : policyToCandidateMap.keySet()){
				HashSet<CandidateToken> t = policyToCandidateMap.get(uid);
				for (CandidateToken token : t){
					logger.info("Candidate Token= " + uid + " " + token.toString());

				}
			}
		}
		return policyToCandidateMap;
		
	}
	
	public boolean isMultiPolicyOrCredential() throws Exception {
		if (this.token==null){
			throw new RuntimeException("Token was null - Ensure setOwnedCredentials() " +
					"- if it has: check there are not more matched " +
					"OwnedCredentials that the user needs to select.");
			
		}
		return policyToCandidateMap.size() > 1;
			
	}
	
	/**
	 * See Constructor javadoc.
	 * @return
	 */
	public HashMap<URI, HashSet<CandidateToken>> getPolicyToCandidateMap() {
		return policyToCandidateMap;
		
	}
	
	/**
	 * See Constructor javadoc.
	 * @param tokens
	 * @return
	 * @throws Exception 
	 */
	public PresentationTokenDescription selectTokens(HashSet<CandidateToken> tokens,
			HashMap<AttributeInPolicy, URI> inspectorChoice) throws Exception{
		
		if (tokens == null || tokens.isEmpty()){
			throw new RuntimeException("No candidates selected");
			
		}
		if (inspectorChoice==null){
			inspectorChoice = new HashMap<>();
			
		}
		List<CandidateToken> candidateTokens = new ArrayList<>(tokens);
		PresentationPolicy pp = candidateTokens.get(0).getPresentationPolicy();
		URI baseUid = pp.getPolicyUID();
		
		for (CandidateToken ct : tokens){
			if (!baseUid.equals(this.candidateToPolicyUidMap.get(ct))){
				throw new UxException("Selected Credentials must be for the same policy");
				
			}
		}
		List<CredentialInPolicy> cipList = pp.getCredential();
		for (CredentialInPolicy cip : cipList){
			List<URI> uids = cip.getCredentialSpecAlternatives().getCredentialSpecUID();
			
			for (CandidateToken token : tokens){
				if (uids.contains(token.getOwnedCredential().getCredentialSpecificationUid())){
					credentialAliasToCandidateTokenMap.put(cip.getAlias(), token);
					break;
					
				}
			}
		}
		token = of.createPresentationTokenDescription();
		token.getAttributePredicate().addAll(pp.getAttributePredicate());
		token.getCredential().addAll(credentialInPolicyToToken(pp.getCredential(), inspectorChoice));
		token.getPseudonym().addAll(pseudonymInPolicyToToken(pp.getPseudonym()));
		token.getVerifierDrivenRevocation().addAll(vdrInPolicyToToken(pp.getVerifierDrivenRevocation()));
		token.setPolicyUID(pp.getPolicyUID());
		token.setMessage(pp.getMessage());
		return token;		

	}
	
	private List<PseudonymInToken> pseudonymInPolicyToToken(List<PseudonymInPolicy> pseudonyms) {
		List<PseudonymInToken> result = new ArrayList<>();
		for (PseudonymInPolicy nym: pseudonyms){
			PseudonymInToken nit = of.createPseudonymInToken();
			nit.setAlias(nym.getAlias());
			nit.setExclusive(nym.isExclusive());
			nit.setScope(nym.getScope());
			nit.setSameKeyBindingAs(nym.getSameKeyBindingAs());
			result.add(nit);

		}
		return result;
		
	}

	private List<CredentialInToken> credentialInPolicyToToken(List<CredentialInPolicy> credentials, HashMap<AttributeInPolicy, URI> inspectorChoiceUID) throws Exception {
		List<CredentialInToken> result = new ArrayList<>();
		for (CredentialInPolicy cred: credentials){
			CredentialInToken cit = of.createCredentialInToken();
			URI alias = cred.getAlias();
			cit.setAlias(alias);
			cit.setSameKeyBindingAs(cit.getSameKeyBindingAs());

			List<AttributeInPolicy> aips = cred.getDisclosedAttribute();
			for (AttributeInPolicy aip : aips){
				AttributeInToken ait = of.createAttributeInToken();
				ait.setAttributeType(aip.getAttributeType());
				ait.setDataHandlingPolicy(aip.getDataHandlingPolicy());
				ait.setInspectionGrounds(aip.getInspectionGrounds());
				ait.setInspectorPublicKeyUID(inspectorChoiceUID.get(aip));
				cit.getDisclosedAttribute().add(ait);

			}
			CandidateToken ct = credentialAliasToCandidateTokenMap.get(alias);
			cit.setCredentialSpecUID(ct.getOwnedCredential().getCredentialSpecificationUid());
			cit.setIssuerParametersUID(ct.getOwnedCredential().getIssuerUid());
			cit.setRevocationInformationUID(
					findMostRecentRevocationInformationUid(
							ct.getOwnedCredential().getRevocationAuthoirityUid()));
			result.add(cit);

		}
		return result;

	}
	
	private URI findMostRecentRevocationInformationUid(URI revocationAuthoirityUid) throws Exception {
		logger.fine("Skipping the building of MOST RECENT revocation information, " +
				"in favour of it being loaded already - change made for paywall Dec21");
		return revocationAuthoirityUid;
//		if (revocationAuthoirityUid!=null){
//			RevocationInformation ti = external.openResource(XContainer.uidToFileName(revocationAuthoirityUid) + "i.xml");
//			return ti.getRevocationInformationUID();
//
//		} else {
//			return null;
//
//		}
	}

	// TODO
	private List<VerifierDrivenRevocationInToken> vdrInPolicyToToken(
			List<VerifierDrivenRevocationInPolicy> verifierDrivenRevocation) {
		List<VerifierDrivenRevocationInToken> result = new ArrayList<>();
		return result;
		
	}
	
	public HashMap<URI, PolicyReviewItem> getAliasToPolicyReviewItemMap() {
		if (aliasToPolicyReviewItemMap.isEmpty()){
			buildPolicyReviewItems();
			
		}
		return aliasToPolicyReviewItemMap;
		
	}


	private List<PolicyReviewItem> buildPolicyReviewItems(){
		List<PolicyReviewItem> result = new ArrayList<>();
		if (this.token!=null){
			List<CredentialInToken> credentials = this.token.getCredential();
			
			for (CredentialInToken cred : credentials){
				PolicyReviewItem item = new PolicyReviewItem(cred.getAlias());
				item.addAttributes(cred.getDisclosedAttribute());
				this.aliasToPolicyReviewItemMap.put(cred.getAlias(), item);
				
			}
			List<AttributePredicate> predicates = token.getAttributePredicate();
			for (AttributePredicate predicate : predicates){
				Object o = predicate.getAttributeOrConstantValue();
				
				if (o instanceof AttributePredicate.Attribute){ // TODO reference to Abc4Trust
					AttributePredicate.Attribute att = (AttributePredicate.Attribute)o;
					PolicyReviewItem pri = aliasToPolicyReviewItemMap.get(att.getCredentialAlias());
					if (pri==null){
						throw new RuntimeException("Unable to find alias in the policy's credentials " + att.getCredentialAlias());
						
					}
					// TODO there's going to be a parsing issue in that the integer will be displayed the the user.
					pri.addAttributePredicates(predicate);
					
				}
			}
		}
		return result;
		
	}
	
	/**
	 * See Constructor javadoc.
	 * @return
	 */
	public PresentationTokenDescription getPresentationTokenDescription() {
		return token;
		
	}	
}
