/*
 * Copyright (c) 2023. All Rights Reserved. Exonym GmbH
 */

package io.exonym.lib.wallet;

import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.parameters.system.SystemParametersWrapper;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.xml.*;
import io.exonym.lib.actor.AbstractExonymOwner;
import io.exonym.lib.actor.CandidateToken;
import io.exonym.lib.api.*;
import io.exonym.lib.exceptions.UxException;
import io.exonym.lib.standard.PassStore;
import io.exonym.lib.pojo.ExternalResourceContainer;

import javax.crypto.Cipher;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;

public final class ExonymOwner extends AbstractExonymOwner {

    private static ExonymOwner VERIFIER = null;

    /**
     *
     * @param container
     */
    public ExonymOwner(AbstractIdContainer container) {
        super(container);
    }

    @Override
    protected synchronized void authenticate(PassStore store) throws Exception {
        super.authenticate(store);
    }

    @Override
    protected ExternalResourceContainer initialzeExternalResourceContainer() {
        return PkiExternalResourceContainer.getInstance();

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
    public synchronized void openContainer(PassStore store) {
        super.openContainer(store);
    }

    @Override
    protected synchronized void openContainer(Cipher dec, Cipher enc) {
        super.openContainer(dec, enc);
    }

    @Override
    public synchronized void setupContainerSecret(Cipher enc, Cipher dec) throws Exception {
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

    @Override
    protected synchronized IssuanceMessage issuanceStep(IssuanceMessageAndBoolean imab, Cipher enc) throws Exception {
        return super.issuanceStep(imab, enc);
    }

    @Override
    protected PresentationTokenDescription canProveClaimFromPolicy(PresentationPolicy pp) throws Exception {
        return super.canProveClaimFromPolicy(pp);
    }

    @Override
    protected PresentationTokenDescription canProveClaimFromPolicy(PresentationPolicyAlternatives pp) throws Exception {
        return super.canProveClaimFromPolicy(pp);
    }

    @Override
    protected HashMap<URI, HashSet<CandidateToken>> chooseCredentialOptions() {
        return super.chooseCredentialOptions();
    }

    @Override
    protected PresentationTokenDescription enterChoice(HashSet<CandidateToken> credentials) throws UxException {
        return super.enterChoice(credentials);
    }

    @Override
    protected PresentationToken proveClaim(PresentationTokenDescription token, PresentationPolicyAlternatives ppa) throws Exception {
        return super.proveClaim(token, ppa);
    }

    @Override
    protected PseudonymWithMetadata generatePseudonym(URI scope, boolean exclusive) throws ConfigurationException, KeyManagerException, CredentialManagerException, UxException {
        return super.generatePseudonym(scope, exclusive);
    }

    @Override
    public boolean verifyClaim(PresentationPolicyAlternatives ppa, PresentationToken token) throws Exception {
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

    public static ExonymOwner verifierOnly() throws Exception {
        if (VERIFIER==null){
            PkiExternalResourceContainer pki = PkiExternalResourceContainer.getInstance();
            NetworkMapMemory networkMap = NetworkMapMemory.getInstance();
            CacheInMemory cache = CacheInMemory.getInstance();
            pki.setNetworkMapAndCache(networkMap, cache);
            ExonymOwner owner = new ExonymOwner(new IdContainerJsonMemory());
            PassStore store = new PassStore("password", false);
            owner.openContainer(store);
            owner.setupContainerSecret(store.getEncrypt(), store.getDecipher());
            // load sybil in preparation
            owner.openResourceIfNotLoaded(networkMap.nmiForSybilTestNet().getLastIssuerUID());
            VERIFIER=owner;

        }
        return VERIFIER;

    }

}
