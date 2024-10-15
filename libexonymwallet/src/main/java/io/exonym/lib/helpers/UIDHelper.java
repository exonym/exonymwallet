package io.exonym.lib.helpers;

import eu.abc4trust.xml.CredentialInPolicy;
import eu.abc4trust.xml.CredentialInToken;
import eu.abc4trust.xml.PresentationToken;
import io.exonym.lib.exceptions.ErrorMessages;
import io.exonym.lib.exceptions.HubException;
import io.exonym.lib.exceptions.UxException;
import io.exonym.lib.pojo.Namespace;
import io.exonym.lib.pojo.IdContainer;
import io.exonym.lib.standard.WhiteList;

import java.net.URI;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UIDHelper {


    private final static Logger logger = Logger.getLogger(UIDHelper.class.getName());


    private String leadName;
    private String moderatorName;
    private  URI credentialSpec;
    private  URI presentationPolicy;
    private  URI issuedCredential;
    private  URI revocationAuthority;
    private  URI revocationAuthorityInfo;
    private  URI issuancePolicy;
    private  URI issuerParameters;
    private  URI inspectorParams;
    private  URI rulebookUID;
    private  String credentialSpecFileName;
    private  String presentationPolicyFileName;
    private  String issuedCredentialFileName;
    private  String revocationAuthorityFileName;
    private  String revocationInfoFileName;
    private  String issuancePolicyFileName;
    private  String issuerParametersFileName;
    private  String inspectorParamsFileName;
    private  String rulebookFileName;
    private String rulebookTopic;
    private String rulebookLeadTopic;
    private String rulebookModTopic;
    private  URI moderatorUid;
    private  URI leadUid;

    public static final String MQTT_WILDCARD = "/#";


    public UIDHelper(URI issuerParameters) throws Exception {
        assemble(issuerParameters.toString());

    }

    public UIDHelper(String issuerParameters) throws Exception {
        assemble(issuerParameters);

    }

    private void assemble(String issuerParamsOrLead) throws Exception {

        boolean isLead = WhiteList.isLeadUid(issuerParamsOrLead);
        String[] parts = issuerParamsOrLead.split(":");

        this.leadName = parts[3];

        this.rulebookUID = URI.create(Namespace.URN_PREFIX_COLON + parts[2]
                + ":" + UIDHelper.computeRulebookHashUid(URI.create(issuerParamsOrLead)));

        this.rulebookTopic = computeTopicFromRulebook(rulebookUID);

        this.rulebookLeadTopic = this.rulebookTopic  + "/" + leadName;

        String lead = null;

        if (!isLead){
            String mod = parts[0] + ":" +parts[1] + ":" +parts[2] + ":" +parts[3]+ ":" +parts[4] + ":" +parts[5];
            lead = parts[0] + ":" +parts[1] + ":" +parts[2] + ":" +parts[3] + ":" +parts[5];
            this.moderatorUid = URI.create(mod);
            this.moderatorName = parts[4];

            String issuerRnd = null;
            boolean containsIssuer = WhiteList.isContainsIssuerUid(issuerParamsOrLead);

            if (containsIssuer){
                issuerRnd = parts[6];
                String root = mod + ":" + issuerRnd;

                this.issuerParameters = URI.create(root + ":i");
                this.issuerParametersFileName = IdContainer.uidToXmlFileName(issuerParameters);

                issuedCredential = URI.create(root + ":ic");
                issuedCredentialFileName  = IdContainer.uidToXmlFileName(issuedCredential);

                revocationAuthority = URI.create(root + ":ra");
                revocationAuthorityFileName = IdContainer.uidToXmlFileName(revocationAuthority);

                revocationAuthorityInfo = URI.create(root + ":rai");
                revocationInfoFileName = IdContainer.uidToXmlFileName(revocationAuthorityInfo);

                issuancePolicy = URI.create(root + ":ip");
                issuancePolicyFileName = IdContainer.uidToXmlFileName(issuancePolicy);

                this.inspectorParams = URI.create(mod + ":ins");
                inspectorParamsFileName = IdContainer.uidToXmlFileName(inspectorParams);

            }
            this.rulebookModTopic = this.rulebookLeadTopic + "/" + moderatorName;

        } else {
            lead = issuerParamsOrLead;

        }

        this.leadUid = URI.create(lead);

        this.presentationPolicy = URI.create(lead + ":pp");
        presentationPolicyFileName = IdContainer.uidToXmlFileName(presentationPolicy);

        this.credentialSpec = credentialSpecFromLeadUID(this.leadUid);
        credentialSpecFileName = IdContainer.uidToXmlFileName(credentialSpec);

        this.rulebookFileName = IdContainer.uidToFileName(rulebookUID) + ".json";

    }

    public static URI computeRulebookUidFromNodeUid(URI nodeUid) throws UxException {
        if (nodeUid==null){
            throw new NullPointerException();
        }
        return URI.create(Namespace.URN_PREFIX_COLON +
                nodeUid.toString().split(":")[2] + ":" +
                computeRulebookHashUid(nodeUid));
    }



    private String computeTopicFromRulebook(URI rulebookUID) {
        return rulebookUID.toString()
                .replaceAll("urn:", "")
                .replaceAll(":", "/");
    }

    public static URI transformMaterialUid(URI origin, String suffix) {
        if (origin!=null && suffix!=null){
            String o = origin.toString();
            int i = o.lastIndexOf(":");
            o = o.substring(0,i+1);
            return URI.create(o + suffix);

        } else {
            throw new NullPointerException("origin/suffix" + origin + "/" + suffix);

        }
    }


    public static String computeRulebookTopicFromUid(URI uid) throws UxException {
        String hash = UIDHelper.computeRulebookHashUid(uid);
        String name = uid.toString().split(":")[2];
        String sum = Namespace.URN_PREFIX_COLON + name + ":" + hash;
        return sum.toString()
                .replaceAll("urn:", "")
                .replaceAll(":", "/");
    }



    public static String computeLeadNameFromModOrLeadUid(URI sourceOrAdvocateUid){
        if (sourceOrAdvocateUid==null){
            throw new NullPointerException();
        }
        return sourceOrAdvocateUid.toString().split(":")[3];
    }

    public static String computeModNameFromModUid(URI advocateUid){
        if (advocateUid==null){
            throw new NullPointerException();
        }
        return advocateUid.toString().split(":")[4];
    }

    public static String computeShortRulebookHashUid(URI leadUid) throws UxException {
        String r = computeRulebookHashUid(leadUid);
        return r.substring(0,3) + "_" + r.substring(r.length()-3, r.length());
    }

    public static String computeRulebookHashUid(URI leadUid) throws UxException {
        Pattern pattern = Pattern.compile("[0-9a-fA-F]{64}");
        Matcher matcher = pattern.matcher(leadUid.toString());

        if (matcher.find()){
            return matcher.group();
        } else {
            throw new UxException(ErrorMessages.INCORRECT_PARAMETERS + " " + leadUid);
        }
    }



    public static String computeRulebookHashFromRulebookId(String rulebookId){
        return rulebookId.split(":")[3];
    }

    public static String computeRulebookIdFromAdvocateUid(URI modUid){
        if (modUid==null){
            throw new NullPointerException();
        }
        return modUid.toString().split(":")[5];
    }

    public static URI computeRulebookIdFromLeadUid(URI leadUid){
        String[] parts = leadUid.toString().split(":");
        if (WhiteList.isLeadUid(leadUid)){
            return URI.create(Namespace.URN_PREFIX_COLON + parts[2] + ":" + parts[4]);

        } else if (WhiteList.isRulebookUid(leadUid)){
            return leadUid;

        } else {
            logger.info(leadUid.toString());
            throw new RuntimeException(ErrorMessages.INVALID_UID + " expected Lead " + leadUid);

        }
    }

    public static URI credentialSpecFromLeadUID(URI sourceUid) throws UxException {
        String[] parts = sourceUid.toString().split(":");
        return URI.create(Namespace.URN_PREFIX_COLON + parts[2] + ":" +
                UIDHelper.computeRulebookHashUid(sourceUid) + ":c");

    }

    public static String stripPrefix(URI uid){
        return stripPrefix(uid.toString());

    }

    public static String stripPrefix(String uid){
        return uid.replaceAll(Namespace.URN_PREFIX_COLON, "");

    }

    public static boolean isLeadUid(URI uid){
        return WhiteList.isLeadUid(uid);

    }

    public static boolean isModeratorUid(URI uid){
        return WhiteList.isModeratorUid(uid);

    }

    public static String uidToFileName(String uri) throws Exception{
        return IdContainer.uidToFileName(uri);
    }



    public CredentialInPolicy.IssuerAlternatives.IssuerParametersUID computeIssuerParametersUID(){
        CredentialInPolicy.IssuerAlternatives.IssuerParametersUID params = new
                CredentialInPolicy.IssuerAlternatives.IssuerParametersUID();
        params.setValue(this.getIssuerParameters());
        params.setRevocationInformationUID(this.getRevocationInfoParams());
        return params;
    }

    public static CredentialInPolicy.IssuerAlternatives.IssuerParametersUID computeIssuerParametersUID(URI issuerUID){
        URI r = URI.create(issuerUID.toString().replaceAll(":i", ":rai"));
        CredentialInPolicy.IssuerAlternatives.IssuerParametersUID params = new
                CredentialInPolicy.IssuerAlternatives.IssuerParametersUID();
        params.setValue(issuerUID);
        params.setRevocationInformationUID(r);
        return params;
    }


    public static URI fileNameToUid(String filename) throws Exception{
        return URI.create(IdContainer.fileNameToUid(filename));
    }


    public URI getRulebookUID() {
        return rulebookUID;
    }

    public String getRevocationInfoFileName() {
        return revocationInfoFileName;
    }

    public String getRulebookFileName() {
        return rulebookFileName;
    }

    public URI getCredentialSpec() {
        return credentialSpec;
    }

    public ArrayList<CredentialInPolicy.IssuerAlternatives.IssuerParametersUID> computeIssuerParametersUIDAsList(){
        ArrayList<CredentialInPolicy.IssuerAlternatives.IssuerParametersUID> list = new ArrayList<>();
        list.add(computeIssuerParametersUID());
        return list;
    }

    public ArrayList<URI> getCredentialSpecAsList() {
        ArrayList<URI> r = new ArrayList();
        r.add(credentialSpec);
        return r;
    }


    public URI getIssuedCredential() {
        return issuedCredential;
    }

    public URI getRevocationAuthority() {
        return revocationAuthority;
    }

    public URI getRevocationInfoParams() {
        return revocationAuthorityInfo;
    }

    public URI getIssuancePolicy() {
        return issuancePolicy;
    }

    public URI getIssuerParameters() {
        return issuerParameters;
    }

    public URI getPresentationPolicy() {
        return presentationPolicy;
    }

    public URI getInspectorParams() {
        return inspectorParams;
    }

    public URI getModeratorUid() {
        return moderatorUid;
    }

    public URI getLeadUid() {
        return leadUid;
    }

    public String getCredentialSpecFileName() {
        return credentialSpecFileName;
    }

    public String getPresentationPolicyFileName() {
        return presentationPolicyFileName;
    }

    public String getIssuedCredentialFileName() {
        return issuedCredentialFileName;
    }

    public String getRevocationAuthorityFileName() {
        return revocationAuthorityFileName;
    }

    public String getRevocationInformationFileName() {
        return revocationInfoFileName;
    }

    public String getIssuancePolicyFileName() {
        return issuancePolicyFileName;
    }

    public String getIssuerParametersFileName() {
        return issuerParametersFileName;
    }

    public String getInspectorParamsFileName() {
        return inspectorParamsFileName;
    }

    public String getLeadName() {
        return leadName;
    }

    public String getModeratorName() {
        return moderatorName;
    }

    public String getRulebookLeadTopic() {
        return rulebookLeadTopic;
    }

    public String getRulebookModTopic() {
        return rulebookModTopic;
    }

    public static URI computeModUidFromMaterialUID(URI modMaterialUID) throws Exception {
        if (modMaterialUID==null){
            throw new NullPointerException();
        }
        String[] parts = modMaterialUID.toString().split(":");
        StringBuilder result = new StringBuilder();
        result.append(Namespace.URN_PREFIX_COLON);
        result.append(parts[2]);
        result.append(":");
        result.append(parts[3]);
        result.append(":");
        result.append(parts[4]);
        result.append(":");
        result.append(parts[5]);
        URI moderatorUid = URI.create(result.toString());
        if (WhiteList.isModeratorUid(moderatorUid)) {
            return moderatorUid;

        } else {
            throw new UxException(moderatorUid.toString());

        }
    }
    /**
     *
     * @param modUid
     * @return [sourceUid, nodeUid]
     * @throws Exception
     */
    public static URI computeLeadUidFromModUid(URI modUid) throws Exception {
        try {
            if (modUid!=null){
                String[] discovery = modUid.toString().split(":");
                if (discovery.length==6) {
                    URI leadUid = URI.create(
                            discovery[0] + ":"
                                    + discovery[1] + ":"
                                    + discovery[2] + ":"
                                    + discovery[3] + ":"
                                    + discovery[5]);
                    return leadUid;

                } else if (discovery.length==5){
                    return modUid;

                } else {
                    throw new HubException("Invalid Node UID (6 components expected)" + modUid + " got " + discovery.length);

                }
            } else {
                throw new NullPointerException();

            }
        } catch (Exception e){
            throw e;

        }
    }

    public static String tokenFileName(URI advocateUid) throws Exception {
        return DateHelper.currentIsoUtcDateTime()
                + "-" + IdContainer.uidToFileName(advocateUid)
                + ".t.xml";
    }

    public static URI extractFirstIssuerFromPresentationToken(PresentationToken token){
        CredentialInToken credential = token.getPresentationTokenDescription().getCredential().get(0);
        credential.getCredentialSpecUID();
        return credential.getIssuerParametersUID();

    }

    public static URI ensureTrailingSlash(String uri){
        return ensureTrailingSlash(URI.create(uri));
    }

    public static URI ensureTrailingSlash(URI uri){
        logger.info("Trailing slash path" + uri);
        String path = uri.getPath();
        logger.info("Trailing slash path" + path);
        if (path == null || path.isEmpty()) {
            return uri.resolve("/");

        } else if (!path.endsWith("/")) {
            return uri.resolve(path + "/");

        }
        return uri;

    }

    public URI getRevocationAuthorityInfo() {
        return revocationAuthorityInfo;
    }

    public String getRulebookTopic() {
        return rulebookTopic;
    }

    public void out(){
        logger.info(">>>>>>>>>>>>> ");
        logger.info("> Output Projected UIDS");
        logger.info("");
        logger.info(leadName);
        logger.info(moderatorName);
        logger.info(leadUid.toString());
        logger.info(moderatorUid.toString());
        logger.info(rulebookUID.toString());
        logger.info("");
        logger.info(rulebookFileName);
        logger.info(credentialSpec.toString());
        logger.info(credentialSpecFileName);
        logger.info(presentationPolicy.toString());
        logger.info(presentationPolicyFileName);
        logger.info(issuedCredential.toString());
        logger.info(issuedCredentialFileName);
        logger.info(revocationAuthority.toString());
        logger.info(revocationAuthorityFileName);
        logger.info(revocationAuthorityInfo.toString());
        logger.info(revocationInfoFileName);
        logger.info(issuancePolicy.toString());
        logger.info(issuancePolicyFileName);
        logger.info(inspectorParams.toString());
        logger.info(inspectorParamsFileName);
        logger.info(issuerParameters.toString());
        logger.info(issuerParametersFileName);
        logger.info("");
        logger.info(rulebookTopic);
        logger.info(rulebookLeadTopic);
        logger.info(rulebookModTopic);
        logger.info(">>>>>>>>>>>>> ");

    }


}
