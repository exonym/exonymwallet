package io.exonym.idmx.managers;

import com.ibm.zurich.idmx.interfaces.util.RandomGeneration;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManager;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.xml.*;

import javax.inject.Inject;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class CredentialManagerUser implements CredentialManager {

	private final Logger logger;

	private final RandomGeneration randomGeneration;
	private final KeyManager km;
	
	private final Map<URI, Credential> credentialMap;
	private final Map<URI, Secret> secretMap;
	private final Map<URI, PseudonymWithMetadata> pseudonymMap;
	
	@Inject
	public CredentialManagerUser(final RandomGeneration randomGeneration, final KeyManager km) {
	    this.randomGeneration = randomGeneration;
	    this.km=km;
	    credentialMap = new ConcurrentHashMap<URI, Credential>();
	    pseudonymMap = new ConcurrentHashMap<URI, PseudonymWithMetadata>();
	    secretMap = new ConcurrentHashMap<URI, Secret>();
		logger = Logger.getLogger(CredentialManagerUser.class.getName());

	    logger.info("Using EXONYM credential manager " + km);
		
	}

	@Override
	public List<CredentialDescription> getCredentialDescription(String username, 
			List<URI> issuers, List<URI> credspecs) throws CredentialManagerException {
		return null;
	}

	@Override
	public CredentialDescription getCredentialDescription(String username, 
			URI creduid) throws CredentialManagerException {
		final Credential c = credentialMap.get(creduid);
		if(c==null) {
		  return null;
		  
		} else {
		  return c.getCredentialDescription();
		  
		}
	}

	@Override
	public void attachMetadataToPseudonym(String username, Pseudonym p, PseudonymMetadata md)
			throws CredentialManagerException {
		throw new RuntimeException("Not implemented");		
	}

	@Override
	public Credential getCredential(String username, URI credId) throws CredentialManagerException {
		logger.info("Searching for " + credId);
		return credentialMap.get(credId);
		
	}

	@Override
	public void storePseudonym(String username, PseudonymWithMetadata pwm) throws CredentialManagerException {
	    final URI nymUri = pwm.getPseudonym().getPseudonymUID();
	    pseudonymMap.put(nymUri, pwm);
		
	}

	@Override
	public boolean hasBeenRevoked(String username, URI creduid, URI revparsuid, List<URI> revokedatts)
			throws CredentialManagerException {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public boolean hasBeenRevoked(String username, URI creduid, URI revparsuid, List<URI> revokedatts, URI revinfouid)
			throws CredentialManagerException {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public void updateNonRevocationEvidence(String username) throws CredentialManagerException {
		throw new RuntimeException("Not implemented");
		
	}

	@Override
	public URI storeCredential(String username, Credential cred) throws CredentialManagerException {
	    URI credId = cred.getCredentialDescription().getCredentialUID();
	    if(credId == null) {
	      credId = URI.create("cred-" + randomGeneration.generateRandomUid());
	      cred.getCredentialDescription().setCredentialUID(credId);
	    }
	    credentialMap.put(credId, cred);
	    return credId;
	}

	@Override
	public void updateCredential(String username, Credential cred) throws CredentialManagerException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<URI> listCredentials(String username) throws CredentialManagerException {
		throw new RuntimeException("Not implemented");
	
	}

	@Override
	public boolean deleteCredential(String username, URI creduid) throws CredentialManagerException {
		return (this.credentialMap.remove(creduid)!=null);
	
	}

	@Override
	public List<PseudonymWithMetadata> listPseudonyms(String username, String scope, boolean onlyExclusive)
			throws CredentialManagerException {
		throw new RuntimeException("Not implemented");
		
	}

	@Override
	public PseudonymWithMetadata getPseudonym(String username, URI pseudonymUid) throws CredentialManagerException {
		return pseudonymMap.get(pseudonymUid);
		
	}

	@Override
	public boolean deletePseudonym(String username, URI pseudonymUid) throws CredentialManagerException {
		throw new RuntimeException("Not implemented");
	
	}

	@Override
	public void storeSecret(String username, Secret cred) throws CredentialManagerException {
		URI key = cred.getSecretDescription().getSecretUID();
		this.secretMap.put(key, cred);
		
	}

	@Override
	public List<SecretDescription> listSecrets(String username) throws CredentialManagerException {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public boolean deleteSecret(String username, URI secuid) throws CredentialManagerException {
		throw new RuntimeException("Not implemented");
	
	}

	@Override
	public Secret getSecret(String username, URI secuid) throws CredentialManagerException {
		return  this.secretMap.get(secuid);
				// ExternalSecretsManagerImpl.generateSecret(km, BigInteger.valueOf(1234), secuid);
	}

	@Override
	public void updateSecretDescription(String username, SecretDescription desc) throws CredentialManagerException {
		throw new RuntimeException("Not implemented");		
	}
}
