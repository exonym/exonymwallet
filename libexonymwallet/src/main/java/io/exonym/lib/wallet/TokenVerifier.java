package io.exonym.lib.wallet;

import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.xml.*;
import io.exonym.lib.api.XContainerJSON;
import io.exonym.lib.exceptions.UxException;

import java.net.URI;

public class TokenVerifier {

	private final ExonymOwner owner;
	private final XContainerJSON x;

	public TokenVerifier(XContainerJSON nodeContainer) throws Exception {
		this.owner = new ExonymOwner(nodeContainer);
		this.owner.initSystemParameters();
		this.x=nodeContainer;

	}

	public void loadCredentialSpecification(URI credentialSpecUid) throws Exception {
		this.owner.openResourceIfNotLoaded(credentialSpecUid);

	}

	public void loadCredentialSpecification(CredentialSpecification credentialSpec){
		if (credentialSpec==null){
			throw new NullPointerException();

		}
		this.owner.addCredentialSpecification(credentialSpec);

	}

	public void loadIssuerParameters(URI issuerParametersUid) throws Exception {
		this.owner.openResourceIfNotLoaded(issuerParametersUid);

	}

	public void loadIssuerParameters(IssuerParameters issuerParameters) throws Exception {
		if (issuerParameters==null){
			throw new NullPointerException();

		}
		this.owner.addIssuerParameters(issuerParameters);

	}

	public void loadRevocationAuthorityParameters(RevocationAuthorityParameters rap) throws KeyManagerException {
		if (rap==null){
			throw new NullPointerException();

		}
		this.owner.addRevocationAuthorityParameters(rap);

	}

	public void loadRevocationAuthorityParameters(URI rapUid) throws Exception {
		this.owner.openResourceIfNotLoaded(rapUid);

	}

	public void loadRevocationInformation(URI rap, URI ri) throws Exception {


	}


	public void loadRevocationInformation(RevocationInformation revocationInformation) throws Exception {
		if (revocationInformation==null){
			throw new NullPointerException();

		}
		URI rai = revocationInformation.getRevocationInformationUID();
		this.owner.addRevocationInformation(rai, revocationInformation);

	}

	public void loadInspectorParams(URI ins) throws Exception {
		this.owner.openResourceIfNotLoaded(ins);

	}

	public void loadInspectorParams(InspectorPublicKey ins) throws Exception {
		if (ins==null){
			throw new NullPointerException();

		}
		this.owner.addInspectorParameters(ins);

	}

	public byte[] verifyToken(PresentationPolicyAlternatives ppa, PresentationToken pt) throws Exception {
		if (ppa==null){
			throw new NullPointerException();

		}
		if (pt==null){
			throw new NullPointerException();

		}
		if (this.owner.verifyClaim(ppa, pt)) {
			return pt.getPresentationTokenDescription().getMessage().getNonce();
			
		} else {
			throw new UxException("The token was invalid");
			
		}
	}

	// static Verify with load

}
