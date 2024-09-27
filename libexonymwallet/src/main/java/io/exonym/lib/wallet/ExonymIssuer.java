package io.exonym.lib.wallet;

import com.ibm.zurich.idmx.parameters.system.SystemParametersWrapper;
import eu.abc4trust.abce.internal.user.credentialManager.CredentialManagerException;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.xml.*;
import io.exonym.lib.lite.AbstractExonymIssuer;
import io.exonym.lib.actor.VerifiedClaim;
import io.exonym.lib.api.AbstractIdContainer;
import io.exonym.lib.api.IdContainerJSON;
import io.exonym.lib.pojo.ExternalResourceContainer;

import javax.crypto.Cipher;
import java.math.BigInteger;
import java.net.URI;

public class ExonymIssuer extends AbstractExonymIssuer {

    public ExonymIssuer(IdContainerJSON container) throws Exception {
        super(container);
    }

    @Override
    protected boolean openResourceIfNotLoaded(URI uid) throws Exception {
        return super.openResourceIfNotLoaded(uid);
    }

    @Override
    protected IssuanceMessageAndBoolean issueInit(VerifiedClaim claim, IssuancePolicy policy, Cipher enc, URI context) throws Exception {
        return super.issueInit(claim, policy, enc, context);
    }

    @Override
    protected IssuanceMessageAndBoolean issueInit(VerifiedClaim claim, IssuancePolicy policy, URI internalDataStoreUid, Cipher enc, URI context) throws Exception {
        return super.issueInit(claim, policy, internalDataStoreUid, enc, context);
    }

    @Override
    protected IssuanceMessageAndBoolean issueStep(IssuanceMessage im, Cipher enc) throws Exception {
        return super.issueStep(im, enc);
    }

    @Override
    protected void setupAsCredentialIssuer(URI credential, URI issuer, Cipher enc) throws Exception {
        super.setupAsCredentialIssuer(credential, issuer, enc);
    }

    @Override
    protected void setupAsCredentialIssuer(URI credential, URI issuerParamsUid, URI revocationAuthorityUid, Cipher enc) throws Exception {
        super.setupAsCredentialIssuer(credential, issuerParamsUid, revocationAuthorityUid, enc);
    }

    @Override
    protected URI setupAsRevocationAuthority(URI issuerParametersUid, Cipher enc) throws Exception {
        return super.setupAsRevocationAuthority(issuerParametersUid, enc);
    }

    @Override
    protected RevocationInformation revokeCredential(URI raUid, BigInteger handle, Cipher dec) throws Exception {
        return super.revokeCredential(raUid, handle, dec);
    }

    @Override
    protected void openContainer(Cipher dec) throws Exception {
        super.openContainer(dec);
    }

    @Override
    protected void addRevocationHistory(RevocationHistory rh) throws CredentialManagerException {
        super.addRevocationHistory(rh);
    }

    @Override
    protected void addRevocationAuthorityKey(URI raUid, PrivateKey sk) throws Exception {
        super.addRevocationAuthorityKey(raUid, sk);
    }

    @Override
    protected void addIssuerSecretKey(SecretKey secretKey) {
        super.addIssuerSecretKey(secretKey);
    }

    @Override
    protected KeyPair generateRevocationAuthorityKeyPair(URI revocationUid) throws Exception {
        return super.generateRevocationAuthorityKeyPair(revocationUid);
    }

    @Override
    public BigInteger getRevocationHandle() {
        return super.getRevocationHandle();
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
    protected ExternalResourceContainer initialzeExternalResourceContainer() {
        return null;
    }
}
