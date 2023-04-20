package io.exonym.lib.actor;

import com.ibm.zurich.idmx.exception.SerializationException;
import com.ibm.zurich.idmx.jaxb.JaxbHelperClass;
import eu.abc4trust.xml.*;
import eu.abc4trust.xml.CredentialInPolicy.IssuerAlternatives.IssuerParametersUID;
import io.exonym.lib.exceptions.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

public class RequestFulfilled {
	
	private final static ObjectFactory of = new ObjectFactory();
	private final static Logger logger = Logger.getLogger(RequestFulfilled.class.getName());
	public static boolean issuancePolicySatisfied(IssuanceTokenAndIssuancePolicy itap) throws Exception{
		IssuanceTokenDescription itd = itap.getIssuanceToken().getIssuanceTokenDescription();
		// Defined by Owner
		PresentationTokenDescription ptd = itd.getPresentationTokenDescription();
		
		IssuancePolicy ip = itap.getIssuancePolicy();
		// Defined by Issuer.  Checked to match with Owner before calling this function.
		PresentationPolicy pp = ip.getPresentationPolicy();
		return inspect(pp, ptd);
		
	}
	
	public static boolean presentationPolicySatisfied(PresentationPolicyAlternatives ppa, PresentationToken pt) throws Exception{
		HashMap<URI, PresentationPolicy> policies  = new HashMap<>();
		for (PresentationPolicy pp : ppa.getPresentationPolicy()){
			policies.put(pp.getPolicyUID(), pp);
			
		}
		PresentationPolicy policy = policies.get(pt.getPresentationTokenDescription().getPolicyUID());
		if (policy==null){
			throw new UxException("The policy in the token does not exist as an acceptable alternative");
			
		}
		return inspect(policy, pt.getPresentationTokenDescription());
		
	}	
	
	private static boolean inspect(PresentationPolicy pp, PresentationTokenDescription ptd) throws Exception {
		List<CredentialInTokenException> unsatisfiedCredentialAliases = collectUnsatisfiedCredentials(pp.getCredential(), ptd.getCredential());
		
		List<AttributePredicateException> missingAttributePredicates = checkAttributePredicate(pp.getAttributePredicate(), ptd.getAttributePredicate());
		
		List<PseudonymException> pseudonymErrors = checkUnsatisfiedPseudonyms(pp.getPseudonym(), ptd.getPseudonym());
		
		MessageException msg = checkMessage(pp.getMessage(), ptd.getMessage());
		
		if (!(unsatisfiedCredentialAliases.isEmpty() && 
				missingAttributePredicates.isEmpty() &&
				pseudonymErrors.isEmpty() &&
				msg==null)){
			PolicyNotSatisfiedException e = new PolicyNotSatisfiedException();
			e.setMissingAttributePredicates(missingAttributePredicates);
			e.setMsg(msg);
			e.setPseudonymErrors(pseudonymErrors);
			e.setUnsatisfiedCredentials(unsatisfiedCredentialAliases);
			throw e; 
			
		}
		logger.fine("WARN -- Exonym has not implemented verification driven revocation");
		pp.getVerifierDrivenRevocation();
		ptd.getVerifierDrivenRevocation();
		
		return true; 

	}

	private static MessageException checkMessage(Message msgRequired, Message msgProvided) throws Exception {
		if (msgRequired!=null && msgRequired.getNonce()!=null){
			String nonceRequired = new String(msgRequired.getNonce(), "UTF-8");
			String nonceProvided = new String(msgProvided.getNonce(), "UTF-8");
			
			if (!nonceProvided.equals(nonceRequired)){
				return new MessageException("Nonce Error");
				
			} else {
				return null;
				
			}
		} else {
			return null;
			
		}
	}

	private static List<PseudonymException> checkUnsatisfiedPseudonyms(List<PseudonymInPolicy> nymsRequired, List<PseudonymInToken> nymsProvided) {
		
		HashMap<String, PseudonymInToken> aliasToTokenMap = new HashMap<>();
		ArrayList<PseudonymException> result = new ArrayList<>(); 
		for (PseudonymInToken nym : nymsProvided){
			aliasToTokenMap.put(nym.getScope(), nym);
			
		}
		for (PseudonymInPolicy nymRequired : nymsRequired){
			PseudonymInToken nymProvided = aliasToTokenMap.get(nymRequired.getScope());

			if (nymRequired.getSameKeyBindingAs()!=null){
				if (!nymRequired.getSameKeyBindingAs().equals(nymProvided.getSameKeyBindingAs())){
					result.add(new PseudonymException("Same key binding error", nymRequired, nymProvided));
					
				}
			}
			if (!nymRequired.getScope().equals(nymProvided.getScope())){
				result.add(new PseudonymException("Incorrect Scope", nymRequired, nymProvided));
				
			}
			if ((nymRequired.isExclusive() && !nymRequired.isExclusive()) || 
					(!nymRequired.isExclusive() && nymRequired.isExclusive())){
				result.add(new PseudonymException("Scope Exclusivity Error", nymRequired, nymProvided));
				
			}
		}
		return result;
		
	}

	private static List<AttributePredicateException> checkAttributePredicate(List<AttributePredicate> required,
                                                                             List<AttributePredicate> received) throws SerializationException {
		ArrayList<String> r = new ArrayList<>();
		ArrayList<AttributePredicateException> result = new ArrayList<>(); 
		
		for (AttributePredicate a : required){
			r.add(serializeAttributePredicate(a));
			
		}
		for (AttributePredicate a : received){
			if (!r.contains(serializeAttributePredicate(a))){
				result.add(new AttributePredicateException(a));
				
			}
		}
		return result;
	}
	
	private static String serializeAttributePredicate(AttributePredicate ap) throws SerializationException{
		return JaxbHelperClass.serialize(of.createAttributePredicate(ap));
		
	} 

	private static List<CredentialInTokenException> collectUnsatisfiedCredentials(List<CredentialInPolicy> cips,
			List<CredentialInToken> cits) {
		
		HashMap<URI, CredentialInToken> credentialsInTokens = new HashMap<>();
		List<CredentialInTokenException> result = new ArrayList<>();
		
		for (CredentialInToken cit : cits){
			credentialsInTokens.put(cit.getAlias(), cit);
			
		}
		for (CredentialInPolicy cip : cips){
			List<URI> acceptableCredentials = cip.getCredentialSpecAlternatives().getCredentialSpecUID();
			CredentialInToken cit = credentialsInTokens.get(cip.getAlias());
			if (!acceptableCredentials.contains(cit.getCredentialSpecUID())){
				result.add(new CredentialInTokenException(cit.getCredentialSpecUID(), cip));
				
			}
			List<URI> ipuids = issuerParametersUidToUid(cip.getIssuerAlternatives().getIssuerParametersUID());
			if (!ipuids.contains(cit.getIssuerParametersUID())){
				result.add(new CredentialInTokenException(cit.getIssuerParametersUID(), cip));
				
			}
		}
		return result;
		
	}
	
	private static List<URI> issuerParametersUidToUid(List<IssuerParametersUID> in){
		List<URI> result = new ArrayList<>();
		for (IssuerParametersUID uid: in){
			result.add(uid.getValue());
			
		}
		return result;
		
	}	
}
