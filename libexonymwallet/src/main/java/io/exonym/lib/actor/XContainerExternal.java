/*
 * Copyright (c) 2023. All Rights Reserved. Exonym GmbH
 */

package io.exonym.lib.actor;

import com.ibm.zurich.idmx.exception.SerializationException;
import com.ibm.zurich.idmx.jaxb.JaxbHelperClass;

import eu.abc4trust.xml.*;
import io.exonym.lib.abc.util.FileType;
import io.exonym.lib.abc.util.JaxbHelper;
import io.exonym.lib.exceptions.ErrorMessages;
import io.exonym.lib.exceptions.HubException;
import io.exonym.lib.exceptions.UxException;
import io.exonym.lib.adapters.PresentationPolicyAlternativesAdapter;
import io.exonym.lib.api.AbstractXContainer;
import io.exonym.lib.pojo.KeyContainer;
import io.exonym.lib.pojo.XContainerSchema;
import io.exonym.lib.wallet.ExonymOwner;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBIntrospector;
import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystemException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

public final class XContainerExternal extends AbstractXContainer {

    private final static Logger logger = Logger.getLogger(XContainerExternal.class.getName());
    private final XContainerSchema schema;

    private static SystemParameters lambda = null;

    public XContainerExternal(String schema) throws Exception {
        super("external");
        this.schema = JaxbHelper.jsonToClass(schema, XContainerSchema.class);
    }

    public XContainerExternal() throws Exception {
        super("external");
        this.schema = new XContainerSchema();

    }

    public void deleteCredential(String prefix, String suffix) throws Exception {
        HashMap<String, String> map = this.schema.getOwnerSecretStore();
        ArrayList<String> toDelete = new ArrayList<String>();
        for (String k : map.keySet()) {
            if (k.startsWith(prefix) && k.endsWith(suffix)) {
                logger.warning("Deleting " + k);
                toDelete.add(k);
            }
        }
        for (String d : toDelete) {
            map.remove(d);

        }
        commitSchema();
    }

    protected void commitSchema() throws Exception {
        updateLists();
        logger.fine("External Container - Get Schema");

    }

    // TODO
    @SuppressWarnings("unchecked")
    public void updateLists() {
        localLedgerList = new ArrayList<>(schema.getLocalLedger().keySet());
        issuerSecretList = new ArrayList<>(schema.getIssuerSecretStore().keySet());
        issuanceParameterList = new ArrayList<>(schema.getIssuanceParameterStore().keySet());
        issuancePolicyList = new ArrayList<>(schema.getIssuancePolicyStore().keySet());
        issuedList = new ArrayList<>(schema.getIssuedStore().keySet());
        ownerSecretList = new ArrayList<>(schema.getOwnerSecretStore().keySet());
        noninteractiveTokenList = new ArrayList<>(schema.getNoninteractiveTokenStore().keySet());
        inspectorList = new ArrayList<>(schema.getInspectorStore().keySet());
        revocationAuthList = new ArrayList<>(schema.getRevocationAuthStore().keySet());

    }

    @Override
    public void saveIssuanceToken(IssuanceToken it, String name, Cipher store) throws Exception {
        throw new Exception();

    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T openResource(String fullFileName, Cipher dec) throws Exception {
        if (!FileType.isXmlDocument(fullFileName)){
            throw new FileSystemException("Resources are all .xml files");

        } else if (FileType.isIssuerSecret(fullFileName)){
            return openEncryptedFile(ISSUER_SECRET_STORE, fullFileName, SecretKey.class, dec);

        } else if (FileType.isInspectorPrivateKey(fullFileName)){
            return openEncryptedFile(ISSUER_SECRET_STORE, fullFileName, SecretKey.class, dec);

        } else if (FileType.isKeys(fullFileName)){
            return openXFile(OWNER_PRIVATE_STORE, fullFileName, KeyContainer.class);

        } else if (FileType.isOwnerSecret(fullFileName)){
            return openEncryptedFile(OWNER_PRIVATE_STORE, fullFileName, Secret.class, dec);

        } else if (FileType.isIssuerParameters(fullFileName)){
            return openFile(ISSUER_PARAMETERS_STORE, fullFileName, IssuerParameters.class);

        } else if (FileType.isIssuanceLog(fullFileName)){
            return openFile(ISSUER_ISSUED, fullFileName, IssuanceLogEntry.class);

        } else if (FileType.isSystemParameters(fullFileName)){
            return (T) openSystemParameters();

        } else if (FileType.isPresentationPolicyAlternatives(fullFileName)){
            return openXFile(LOCAL_LEDGER, fullFileName, PresentationPolicyAlternativesAdapter.class);

        } else if (FileType.isCredentialSpecification(fullFileName)){
            return openFile(LOCAL_LEDGER, fullFileName, CredentialSpecification.class);

        } else if (FileType.isIssuancePolicy(fullFileName)){
            return openFile(ISSUANCE_POLICY_STORE, fullFileName, IssuancePolicy.class);

        } else if (FileType.isProofToken(fullFileName)){
            return openFile(NONINTERACTIVE_TOKENS, fullFileName, PresentationToken.class);

        } else if (FileType.isInspectorPublicKey(fullFileName)){
            return openFile(INSPECTOR_STORE, fullFileName, InspectorPublicKey.class);

        } else if (FileType.isRevocationAuthority(fullFileName)){
            return openFile(REVOCATION_AUTH_STORE, fullFileName, RevocationAuthorityParameters.class);

        } else if (FileType.isRevocationInformation(fullFileName)){
            return openFile(REVOCATION_AUTH_STORE, fullFileName, RevocationInformation.class);

        } else if (FileType.isCredential(fullFileName)){ // Local
            return openEncryptedFile(OWNER_PRIVATE_STORE, fullFileName, Credential.class, dec);

        } else if (FileType.isRevocationAuthorityPrivateKey(fullFileName)){ // Local
            return openEncryptedFile(REVOCATION_AUTH_STORE, fullFileName, PrivateKey.class, dec);

        } else if (FileType.isRevocationHistory(fullFileName)){ // Local
            return openFile(REVOCATION_AUTH_STORE, fullFileName, RevocationHistory.class);

        } else if (FileType.isIssuancePolicy(fullFileName)){ // Local
            return  openFile(ISSUANCE_POLICY_STORE, fullFileName, IssuancePolicy.class);

        } else if (FileType.isPresentationPolicy(fullFileName)){ // Local
            return  openFile(ISSUANCE_POLICY_STORE, fullFileName, PresentationPolicy.class);

        } else {
            throw new FileSystemException("File type not recognized " + fullFileName);

        }
    }

    public static SystemParameters openSystemParameters() throws Exception{
        if (lambda==null) {
            try (InputStream stream = ExonymOwner.class.getClassLoader()
                    .getSystemResourceAsStream("lambda.xml")) {

                if (stream != null) {
                    byte[] in = new byte[stream.available()];
                    stream.read(in);
                    lambda = (SystemParameters) JaxbHelperClass.deserialize(new String(in, StandardCharsets.UTF_8)).getValue();

                } else {
                    throw new UxException("SYSTEM_PARAMETERS_NOT_FOUND_ON_CLASS_PATH",
                            "Unable to load system parameters", "lambda.xml");

                }
            } catch (Exception e) {
                throw e;

            }
        }
        return lambda;

    }

    public static void loadSystemParams(String lambdaXml) throws SerializationException {
        lambda = (SystemParameters) JaxbHelperClass.deserialize(lambdaXml).getValue();

    }


    @SuppressWarnings("unchecked")
    private <T> T openEncryptedFile(URI location, String fullFileName, Class<?> clazz, Cipher dec) throws Exception {
        try {
            if (dec!=null){
                HashMap<String, String> l = computeLocation(location);
                String encB64 = l.get(fullFileName);

                if (encB64!=null){
                    byte[] xml = dec.doFinal(Base64.decodeBase64(encB64));
                    ByteArrayInputStream is = new ByteArrayInputStream(xml);
                    JAXBElement<?> resourceAsJaxbElement = JaxbHelperClass.deserialize(is, true);
                    return (T) JAXBIntrospector.getValue(resourceAsJaxbElement);

                } else {
                    throw new Exception(ErrorMessages.FILE_NOT_FOUND);

                }
            } else {
                throw new UxException(ErrorMessages.INCORRECT_PARAMETERS);

            }
        } catch (BadPaddingException e){
            throw new UxException(ErrorMessages.INVALID_PASSWORD);

        }
    }

    @SuppressWarnings("unchecked")
    private <T> T openFile(URI location, String fullFileName, Class<?> clazz) throws Exception {
        HashMap<String, String> l = computeLocation(location);
        String encB64 = l.get(fullFileName);
        if (encB64!=null){
            byte[] xml = Base64.decodeBase64(encB64);
            ByteArrayInputStream is = new ByteArrayInputStream(xml);
            JAXBElement<?> resourceAsJaxbElement = JaxbHelperClass.deserialize(is, true);
            logger.fine(fullFileName + "\n" + new String(xml));
            return (T)JAXBIntrospector.getValue(resourceAsJaxbElement);

        } else {
            throw new FileNotFoundException("The file does not exist " + fullFileName + " in container " + this.getUsername());

        }
    }


    @SuppressWarnings("unchecked")
    private <T> T openXFile(URI location, String fullFileName, Class<?> clazz) throws Exception {
        logger.fine("Getting file from schema object: " + schema);
        HashMap<String, String> l = computeLocation(location);
        String encB64 = l.get(fullFileName);
        if (encB64!=null){
            byte[] xml = Base64.decodeBase64(encB64);
            logger.fine(fullFileName + "\n" + new String(xml));
            return (T)JaxbHelper.xmlToClass(new String(xml), clazz);

        } else {
            throw new Exception("The file does not exist " + fullFileName + " in container " + this.getUsername());

        }
    }

    @Override
    protected void saveEncrypted(String xml, URI location, String name, boolean overwrite, Cipher store) throws Exception {
        String b64 = Base64.encodeBase64String(store.doFinal(xml.getBytes()));
        put(b64, name, computeLocation(location), overwrite);
        commitSchema();

    }

    @Override
    protected void save(String xml, URI location, String name, boolean overwrite) throws Exception {
        String b64 = Base64.encodeBase64String(xml.getBytes());
        put(b64, name, computeLocation(location), overwrite);
        commitSchema();

    }

    protected HashMap<String, String> computeLocation(URI location) throws Exception {
        if (location.equals(LOCAL_LEDGER)){
            return schema.getLocalLedger();

        } else if (location.equals(ISSUER_SECRET_STORE)){
            return schema.getIssuerSecretStore();

        } else if (location.equals(ISSUER_PARAMETERS_STORE)){
            return schema.getIssuanceParameterStore();

        } else if (location.equals(RUNTIME_KEYS)){
            return schema.getOwnerSecretStore();

        } else if (location.equals(ISSUANCE_POLICY_STORE)){
            return schema.getIssuancePolicyStore();

        } else if (location.equals(ISSUER_ISSUED)){
            return schema.getIssuedStore();

        } else if (location.equals(OWNER_PRIVATE_STORE)){
            return schema.getOwnerSecretStore();

        } else if (location.equals(NONINTERACTIVE_TOKENS)){
            return schema.getNoninteractiveTokenStore();

        } else if (location.equals(INSPECTOR_STORE)){
            return schema.getInspectorStore();

        } else if (location.equals(REVOCATION_AUTH_STORE)){
            return schema.getRevocationAuthStore();

        } else {
            throw new HubException("Programming Error - Case for " + location + " not handled.");

        }
    }

    private void put(String b64, String name, HashMap<String, String> map, boolean overwrite) throws UxException {
        if (overwrite){
            map.put(name, b64);

        } else {
            String f = map.putIfAbsent(name, b64);
            if (f !=null && !overwrite){
                throw new UxException("The XML already exists: " + name);

            }
        }
    }

    @Override
    public void delete() throws Exception {
        logger.warning("External - Delete not possible");

    }

    public String getSchema() throws Exception {
        return JaxbHelper.serializeToJson(this.schema, XContainerSchema.class);

    }
}
