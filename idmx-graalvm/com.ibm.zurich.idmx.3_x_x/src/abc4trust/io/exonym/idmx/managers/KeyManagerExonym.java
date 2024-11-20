package io.exonym.idmx.managers;

import eu.abc4trust.keyManager.KeyManager;
import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.xml.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class KeyManagerExonym implements KeyManager {

    private static final URI DEFAULT_SYSTEM_PARAMETERS_URI = URI.create("urn:idmx:params:system");
    private final ConcurrentHashMap<URI, SystemParameters> systemParametersMap = new ConcurrentHashMap();
    private final ConcurrentHashMap<URI, IssuerParameters> issuerParameterMap = new ConcurrentHashMap();
    private final ConcurrentHashMap<URI, CredentialSpecification> credentialSpecificationMap = new ConcurrentHashMap();
    private final ConcurrentHashMap<URI, RevocationAuthorityParameters> revocationParametersMap = new ConcurrentHashMap();
    private final ConcurrentHashMap<URI, InspectorPublicKey> inspectorKeyMap = new ConcurrentHashMap();
    private final ConcurrentHashMap<URI, RevocationInformation> revocationInfoMap = new ConcurrentHashMap();

    private final Logger logger;

    public KeyManagerExonym() {
        logger = Logger.getLogger(KeyManagerExonym.class.getName());
    }

    @Override
    public IssuerParameters getIssuerParameters(URI issuid) throws KeyManagerException {
        IssuerParameters ip = this.issuerParameterMap.get(issuid);
        if (ip == null) {
            logger.log(Level.WARNING, "Issuer parameters not found: " + issuid);

        }
        return ip;

    }

    @Override
    public List<URI> listIssuerParameters() throws KeyManagerException {
        return new ArrayList(this.issuerParameterMap.keySet());

    }

    @Override
    public boolean storeIssuerParameters(URI issuid, IssuerParameters issuerParameters) throws KeyManagerException {
        if (issuid==null){
            throw new NullPointerException();

        } if (issuerParameters==null){
            throw new NullPointerException();

        }
        this.issuerParameterMap.put(issuid, issuerParameters);
        return true;

    }

    @Override
    public InspectorPublicKey getInspectorPublicKey(URI ipkuid) throws KeyManagerException {
        InspectorPublicKey ret = this.inspectorKeyMap.get(ipkuid);
        if (ret == null) {
            logger.log(Level.WARNING, "Could not find inspector public key: " + ipkuid);
        }
        return ret;

    }

    @Override
    public boolean storeInspectorPublicKey(URI ipkuid, InspectorPublicKey inspectorPublicKey) throws KeyManagerException {
        if (ipkuid==null){
            throw new NullPointerException();

        } if (inspectorPublicKey==null){
            throw new NullPointerException();

        }
        this.inspectorKeyMap.put(ipkuid, inspectorPublicKey);
        return true;

    }

    @Override
    public RevocationAuthorityParameters getRevocationAuthorityParameters(URI rapuid) throws KeyManagerException {
        RevocationAuthorityParameters rap = this.revocationParametersMap.get(rapuid);
        if (rap == null) {
            logger.log(Level.WARNING, "Revocation authority parameters not found: " + rapuid);

        }
        return rap;

    }

    @Override
    public RevocationInformation getRevocationInformation(URI rapuid, URI revinfouid) throws KeyManagerException {
        logger.log(Level.FINE, "REVOCATION INFORMATION REQUEST" + rapuid + " " + revinfouid);
        RevocationInformation ri = this.revocationInfoMap.get(rapuid);
        if (ri == null) {
            logger.log(Level.FINE,"Could not get revocation information: " + ri);

        }
        return ri;
    }

    @Override
    public RevocationInformation getCurrentRevocationInformation(URI rapuid) throws KeyManagerException {
        return this.getRevocationInformation(rapuid, (URI)null);
    }

    @Override
    public RevocationInformation getLatestRevocationInformation(URI rapuid) throws KeyManagerException {
        return this.getRevocationInformation(rapuid, (URI)null);
    }

    @Override
    public void storeRevocationInformation(URI uri, RevocationInformation revocationInformation) throws KeyManagerException {
        if (revocationInformation==null){
            throw new NullPointerException();
        }
        logger.log(Level.FINE, "Storing Revocation Information " + uri);
        this.revocationInfoMap.put(uri, revocationInformation);

    }

    public void clearRevocationInfoForUid(URI raiUid){
        if (raiUid==null){
            throw new NullPointerException();
        }
        String[] parts = raiUid.toString().split(":");
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            builder.append(parts[i]);
            builder.append(":");

        }
        String idCheck = builder.toString();
        System.out.println("IDMX Clearance Target" + idCheck);

        ArrayList<URI> toRemove = new ArrayList<>();
        for (URI rai : revocationInfoMap.keySet()){
            if (rai.toString().startsWith(idCheck)){
                toRemove.add(rai);

            }
        }
        for (URI remove : toRemove){
            revocationInfoMap.remove(remove);
            System.out.println("Removing: " + remove);
        }
    }

    public ConcurrentHashMap<URI, RevocationInformation> getRevocationInfoMap(){
        return revocationInfoMap;

    }

    @Override
    public void storeCurrentRevocationInformation(RevocationInformation ri) throws KeyManagerException {
        this.storeRevocationInformation(ri.getRevocationAuthorityParametersUID(), ri);
        this.storeRevocationInformation(ri.getRevocationInformationUID(), ri);

    }

    @Override
    public boolean storeRevocationAuthorityParameters(URI issuid,
                                                      RevocationAuthorityParameters rap) throws KeyManagerException {
        if (issuid==null){
            throw new NullPointerException();
        } else if (rap ==null) {
            throw new NullPointerException();
        }
        this.revocationParametersMap.put(issuid, rap);
        return true;

    }

    @Override
    public CredentialSpecification getCredentialSpecification(URI uri) throws KeyManagerException {
        return this.credentialSpecificationMap.get(uri);
    }

    @Override
    public boolean storeCredentialSpecification(URI uri, CredentialSpecification credentialSpecification) throws KeyManagerException {
        if (uri==null){
            throw new NullPointerException();

        } else if (credentialSpecification==null){
            throw new NullPointerException();

        }
        this.credentialSpecificationMap.put(uri, credentialSpecification);
        return true;

    }

    @Override
    public boolean storeSystemParameters(SystemParameters systemParameters) throws KeyManagerException {
        this.systemParametersMap.put(DEFAULT_SYSTEM_PARAMETERS_URI, systemParameters);
        return true;

    }

    @Override
    public SystemParameters getSystemParameters() throws KeyManagerException {
        return this.systemParametersMap.get(DEFAULT_SYSTEM_PARAMETERS_URI);
    }

    @Override
    public boolean hasSystemParameters() throws KeyManagerException {
        return this.systemParametersMap.containsKey(DEFAULT_SYSTEM_PARAMETERS_URI);

    }

    public void clearStale(){
        revocationInfoMap.clear();
        revocationParametersMap.clear();
        issuerParameterMap.clear();
        inspectorKeyMap.clear();
        credentialSpecificationMap.clear();

    }




}
