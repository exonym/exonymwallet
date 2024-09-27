package io.exonym.lib.api;

import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.parameters.system.SystemParametersWrapper;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.xml.*;
import io.exonym.lib.actor.AbstractExonymOwner;
import io.exonym.lib.actor.CandidateToken;
import io.exonym.lib.exceptions.UxException;
import io.exonym.lib.pojo.ExternalResourceContainer;
import io.exonym.lib.standard.PassStore;

import javax.crypto.Cipher;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;

public class ExonymOwner extends AbstractExonymOwner {

    /**
     * This must be extended as final in the package where it will be used.
     *
     * @param container
     */
    protected ExonymOwner(AbstractIdContainer container) {
        super(container);
    }

    @Override
    public void clearStale() throws Exception {
        super.clearStale();
    }

    @Override
    protected <T> T publicParameterOpener(URI uid) throws Exception {
        return super.publicParameterOpener(uid);
    }

    @Override
    protected void addInspectorParameters(InspectorPublicKey ins) throws Exception {
        super.addInspectorParameters(ins);
    }

    @Override
    public SystemParametersWrapper initSystemParameters() throws Exception {
        return super.initSystemParameters();
    }

    @Override
    protected SystemParametersWrapper initSystemParameters(String spFilename) throws Exception {
        return super.initSystemParameters(spFilename);
    }

    /**
     * Verifier Parameters are often required for PresentationPolicy
     * and PresentationPolicyAlternatives.  This function provides all
     * ExistenceActors access to these VerifierParameters
     *
     * @return
     * @throws Exception
     */
    @Override
    public VerifierParameters getVerifierParameters() throws Exception {
        return super.getVerifierParameters();
    }

    @Override
    public SystemParameters getSystemParameters() throws KeyManagerException {
        return super.getSystemParameters();
    }

    @Override
    protected void addCredentialSpecification(CredentialSpecification credentialSpecification) {
        super.addCredentialSpecification(credentialSpecification);
    }

    /**
     * Adds Issuer Parameters to this object.
     *
     * @param issuerParams
     * @return the RevocationAuthorityUid associated with
     * this Issuer if there is one else returns null.
     * @throws Exception
     */
    @Override
    protected URI addIssuerParameters(IssuerParameters issuerParams) throws Exception {
        return super.addIssuerParameters(issuerParams);
    }

    @Override
    protected AbstractIdContainer getContainer() {
        return super.getContainer();
    }

    @Override
    protected void addRevocationAuthorityParameters(RevocationAuthorityParameters rap) throws KeyManagerException {
        super.addRevocationAuthorityParameters(rap);
    }

    @Override
    protected void addRevocationInformation(URI rapUid, RevocationInformation ri) throws Exception {
        super.addRevocationInformation(rapUid, ri);
    }

    @Override
    protected boolean openResourceIfNotLoaded(URI uid) throws Exception {
        return super.openResourceIfNotLoaded(uid);
    }

    @Override
    protected synchronized void openContainer(PassStore store) {
        super.openContainer(store);
    }

    @Override
    protected synchronized void authenticate(PassStore store) throws Exception {
        super.authenticate(store);
    }

    @Override
    protected synchronized void openContainer(Cipher dec, Cipher enc) {
        super.openContainer(dec, enc);
    }

    @Override
    protected synchronized void setupContainerSecret(Cipher enc, Cipher dec) throws Exception {
        super.setupContainerSecret(enc, dec);
    }

    @Override
    protected synchronized void addContainerSecret(Secret secret) throws UxException, Exception {
        super.addContainerSecret(secret);
    }

    @Override
    protected synchronized void addCredentialToIdmx(Credential credential, Cipher enc) throws Exception {
        super.addCredentialToIdmx(credential, enc);
    }

    /**
     * Called in each step of an issuance process that
     * will result in obtaining a credential. <p>
     * <p>
     * If further information must be sent to the issuer,
     * it produces an IssuMsgOrCredDesc object.
     * Otherwise it will return null
     * and store the credential in the IdmxContainer.
     *
     * @param imab
     * @param enc
     * @return
     * @throws Exception
     */
    @Override
    protected synchronized IssuanceMessage issuanceStep(IssuanceMessageAndBoolean imab, Cipher enc) throws Exception {
        return super.issuanceStep(imab, enc);
    }

    /**
     * See proveClaim();
     *
     * @param pp
     */
    @Override
    protected PresentationTokenDescription canProveClaimFromPolicy(PresentationPolicy pp) throws Exception {
        return super.canProveClaimFromPolicy(pp);
    }

    /**
     * See proveClaim();
     *
     * @param pp
     */
    @Override
    protected PresentationTokenDescription canProveClaimFromPolicy(PresentationPolicyAlternatives pp) throws Exception {
        return super.canProveClaimFromPolicy(pp);
    }

    /**
     * See proveClaim();
     */
    @Override
    protected HashMap<URI, HashSet<CandidateToken>> chooseCredentialOptions() {
        return super.chooseCredentialOptions();
    }

    /**
     * See proveClaim();
     *
     * @param credentials
     */
    @Override
    protected PresentationTokenDescription enterChoice(HashSet<CandidateToken> credentials) throws UxException {
        return super.enterChoice(credentials);
    }

    /**
     * One objective of the Owner is prove a claim imposed on them from a third party
     * in a PresentationPolicy, or PresentationPolicyAlternatives document.<p>
     * <p>
     * To do this, the Owner generates a PresentationToken.  <p>
     * <p>
     * It is possible that an Owner can fulfil many of the PresentationPolicyAlternatives
     * that are acceptable to the Verifier, who wishes to verify the claim. They can therefore
     * choose which credential from which issuer they want to use.<p>
     * <p>
     * The procedure on receipt of a PresentationPolicy or Alternatives is to call
     * canProveClaimFromPolicy(); If this returns null with exception, call chooseCredentialOptions()
     * to return a set of all possible options.<p>
     * <p>
     * Allow the Owner to make the necessary choice via the enterChoice() method which will return
     * the required PresentationTokenDescription for this function.
     *
     * @param token
     * @param ppa
     * @return
     * @throws Exception
     */
    @Override
    protected PresentationToken proveClaim(PresentationTokenDescription token, PresentationPolicyAlternatives ppa) throws Exception {
        return super.proveClaim(token, ppa);
    }

    @Override
    protected PseudonymWithMetadata generatePseudonym(URI scope, boolean exclusive) throws ConfigurationException, KeyManagerException, CredentialManagerException, UxException {
        return super.generatePseudonym(scope, exclusive);
    }

    /**
     * This function returns true if the proof is valid and throws an exception if it is invalid.
     *
     * @param ppa
     * @param token
     * @return
     * @throws Exception
     */
    @Override
    protected boolean verifyClaim(PresentationPolicyAlternatives ppa, PresentationToken token) throws Exception {
        return super.verifyClaim(ppa, token);
    }

    @Override
    protected void checkPolicySatisfied(PresentationPolicyAlternatives ppa, PresentationToken token) throws Exception {
        super.checkPolicySatisfied(ppa, token);
    }

    @Override
    protected String nymToString(PseudonymInPolicy nym) {
        return super.nymToString(nym);
    }

    @Override
    protected String nymToString(PseudonymInToken nym) {
        return super.nymToString(nym);
    }

    @Override
    protected void acceptableIssuers(CredentialInPolicy.IssuerAlternatives issuerAlternatives) {
        super.acceptableIssuers(issuerAlternatives);
    }

    @Override
    protected ExternalResourceContainer initialzeExternalResourceContainer() {
        return PkiExternalResourceContainer.getInstance();
    }
}
