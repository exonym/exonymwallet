package io.exonym.lib.wallet;

import com.ibm.zurich.idmx.parameters.system.SystemParametersWrapper;
import eu.abc4trust.abce.internal.inspector.credentialManager.CredentialManagerException;
import eu.abc4trust.cryptoEngine.CryptoEngineException;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.xml.*;
import io.exonym.lib.lite.AbstractExonymInspector;
import io.exonym.lib.api.XContainerJSON;
import io.exonym.lib.standard.PassStore;
import io.exonym.lib.api.AbstractXContainer;
import io.exonym.lib.pojo.ExternalResourceContainer;

import java.net.URI;
import java.util.List;

public class ExonymInspector extends AbstractExonymInspector {


    public ExonymInspector(XContainerJSON container) throws Exception {
        super(container);
    }

    @Override
    protected void generateInspectorMaterials(URI uid, List<FriendlyDescription> friendlyDescription, PassStore store) {
        super.generateInspectorMaterials(uid, friendlyDescription, store);
    }

    @Override
    protected List<Attribute> publishInspectorMaterials(PresentationToken presentationToken) throws CryptoEngineException {
        return super.publishInspectorMaterials(presentationToken);
    }

    @Override
    protected List<Attribute> inspect(IssuanceToken issuanceToken) throws CryptoEngineException {
        return super.inspect(issuanceToken);
    }

    @Override
    protected List<Attribute> inspect(PresentationToken presentationToken) throws CryptoEngineException {
        return super.inspect(presentationToken);
    }

    @Override
    protected void addInspectorSecretKey(URI inssUid, SecretKey key) throws CredentialManagerException {
        super.addInspectorSecretKey(inssUid, key);
    }

    @Override
    protected void publishInspectorMaterials() {
        super.publishInspectorMaterials();
    }

    @Override
    public void clearStale() throws Exception {
        super.clearStale();
    }

    @Override
    protected boolean openResourceIfNotLoaded(URI uid) throws Exception {
        return super.openResourceIfNotLoaded(uid);
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
    protected AbstractXContainer getContainer() {
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
