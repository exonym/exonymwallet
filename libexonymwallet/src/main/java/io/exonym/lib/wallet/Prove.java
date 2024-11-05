package io.exonym.lib.wallet;

import com.google.gson.JsonObject;
import eu.abc4trust.xml.*;
import io.exonym.lib.actor.NodeVerifier;
import io.exonym.lib.helpers.BuildPresentationPolicy;
import io.exonym.lib.helpers.DateHelper;
import io.exonym.lib.helpers.UIDHelper;
import io.exonym.lib.abc.util.FileType;
import io.exonym.lib.abc.util.JaxbHelper;
import io.exonym.lib.api.AbstractIdContainer;
import io.exonym.lib.api.PkiExternalResourceContainer;
import io.exonym.lib.lite.*;
import io.exonym.lib.pojo.*;
import io.exonym.lib.exceptions.ErrorMessages;
import io.exonym.lib.exceptions.UxException;
import io.exonym.lib.standard.PassStore;
import io.exonym.lib.standard.QrCode;
import io.exonym.lib.standard.WhiteList;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.ClientProtocolException;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Instant;
import java.time.Period;
import java.util.*;
import java.util.logging.Logger;

public class Prove {

    private final static Logger logger = Logger.getLogger(Prove.class.getName());
    private ExonymToolset exo;

    public static final ArrayList<String> BLACKLIST = new ArrayList<>();

    public static final String[] BLACKLIST_PSEUDONYMS = {
            "urn:io:exonym",
            "urn:io:exonym:sybil",
            "",
            "urn",
            "urn:",

    };

    public static final URI DEFAULT_ALIAS = URI.create(BLACKLIST_PSEUDONYMS[0]);

    static {
        for (String b : BLACKLIST_PSEUDONYMS) {
            BLACKLIST.add(b);
        }
    }


    protected Prove(PassStore store, Path root) throws Exception {
        this.exo = new ExonymToolset(store, root);

    }
    protected Prove(PassStore store, Path root, IdContainerSchema schema) throws Exception {
        this.exo = new ExonymToolset(store, root, schema);

    }

    protected String nonInteractiveProofRequest(String nonInteractiveProofRequestJson) throws Exception {
        NonInteractiveProofRequest request = JaxbHelper.jsonToClass(
                nonInteractiveProofRequestJson,
                NonInteractiveProofRequest.class);
        return nonInteractiveProofRequest(request);

    }


    protected String nonInteractiveProofRequest(NonInteractiveProofRequest request) throws Exception {
        List<String> issuers = new ArrayList<>(request.getIssuerUids());
        // we are not allowing pseudonyms in non-interactive proofs before p2p use cases are targeted.
        // this is because the use of cross-domain nyms needs to be limited.
        List<String> nyms = new ArrayList<>();
        if (request.getMetadata()==null){
            throw new UxException(ErrorMessages.INSPECTION_RESULT_REQUIRED);

        }
        for (String issuer : issuers){
            if (!issuer.endsWith(":i")){
                throw new UxException("ISSUER_UID_REQUIRED", issuer);

            }
        }

        return proofForRulebooks((ArrayList<String>) issuers,
                (ArrayList<String>) nyms,
                request.getMetadata());

    }

    /**
     * Used for user generation of non-interactive proofs
     * <p>
     * <p>
     * An ideal would be to SFTP it to the user's choice of location.
     *
     * @param issuerUids
     * @param pseudonyms
     * @param metadata
     * @return the serialized proof token
     * @throws Exception
     */
    private String proofForRulebooks(ArrayList<String> issuerUids,
                                       ArrayList<String> pseudonyms,
                                       JsonObject metadata) throws Exception {
        PresentationPolicy pp = proofRequest(issuerUids, pseudonyms, metadata);
        PresentationToken token = proveFromPresentationPolicy(pp);
        return IdContainer.convertObjectToXml(token);

    }

    protected String proofForRulebookSSO(String challengeB64) throws Exception {
        String isolate = WalletUtils.isolateUniversalLinkContent(challengeB64);
        String ssoCJson = decodeRequest(isolate);
        logger.info(ssoCJson);
        SsoChallenge c = jsonToClass(ssoCJson, SsoChallenge.class);
        logger.info(c.getChallenge());
        JsonObject o = new JsonObject();
        o.addProperty("c", c.getChallenge());

        FulfillmentReport report = fulfillmentReport(c);
        if (report.isProvable()){
            return proofForRulebookSSO(report.getIssuersToUse(), c.getDomain().toString(), o);

        } else {
            return JaxbHelper.serializeToJson(report, FulfillmentReport.class);

        }
    }

    protected String proofForRulebookSSOAnon(String challengeB64) throws Exception {
        String isolate = WalletUtils.isolateUniversalLinkContent(challengeB64);
        String ssoCJson = decodeRequest(isolate);
        logger.info(ssoCJson);
        SsoChallenge c = jsonToClass(ssoCJson, SsoChallenge.class);
        logger.info(c.getChallenge());
        JsonObject o = new JsonObject();
        o.addProperty("c", c.getChallenge());

        FulfillmentReport report = fulfillmentReport(c);
        if (report.isProvable()){
            return proofForRulebookSSO(report.getIssuersToUse(), null, o);

        } else {
            return JaxbHelper.serializeToJson(report, FulfillmentReport.class);

        }
    }

    // {insufficient privileges | authenticate}

    protected String generateDelegationRequestForThirdParty(String uLink, String name) throws Exception {
        DelegateRequest domainAndURL = AuthenticationWrapper.unwrapFromUniversalLink(uLink, DelegateRequest.class);
        String target =domainAndURL.getDomain().toString();
        boolean valid = verifyPseudonym(target, true);
        if (valid){
            if (WhiteList.url(target)){
                PresentationPolicy policy = proofRequest(WalletUtils.emptyList(),
                        WalletUtils.wrapInList(target), null, true);

                PresentationPolicyAlternatives ppa = WalletUtils.openPPA(policy);

                DelegationRequest request = new DelegationRequest();
                request.setRequestDate(DateHelper.currentIsoUtcDateTime());
                request.setName(name);
                request.setService(target);
                String b64Token = WalletUtils.idmxToUniversalLink(Namespace.UNIVERSAL_LINK_FILL_DELEGATE_REQUEST, ppa);

                request.setLink(b64Token);
                request.setQrPngB64(QrCode.computeQrCodeAsPngB64(b64Token, 300));

                return JaxbHelper.serializeToJson(request, DelegationRequest.class);

            }
        }
        throw new UxException(ErrorMessages.UNEXPECTED_PSEUDONYM_REQUEST + ":"
                + uLink + " " + valid + " " + WhiteList.url(target));

    }

    protected String fillDelegationRequest(String uLink) throws Exception {
        String ppaString = WalletUtils.isolateUniversalLinkContent(uLink);
        PresentationPolicyAlternatives ppa = WalletUtils.openPPA(ppaString);
        verifyPseudonym(ppa, true);
        PresentationTokenDescription ptd = exo.getOwner().canProveClaimFromPolicy(ppa);
        PresentationToken pt = exo.getOwner().proveClaim(ptd, ppa);

        HashMap<String, String> map = new HashMap<>();
        String link = WalletUtils.idmxToUniversalLink(Namespace.UNIVERSAL_LINK_FILLED_DELEGATE_REQUEST, pt);
        map.put("link", link);

        return JaxbHelper.serializeToJson(map, HashMap.class);

    }

    public String verifyDelegationRequest(String requestLink, String proofLink) throws Exception {
        String r = WalletUtils.isolateUniversalLinkContent(requestLink);
        String p = WalletUtils.isolateUniversalLinkContent(proofLink);
        PresentationPolicyAlternatives ppa = WalletUtils.idmxFromUniversalLink(r);
        PresentationToken pt = WalletUtils.idmxFromUniversalLink(p);
        boolean satisfied = exo.getOwner().verifyClaim(ppa, pt);
        if (satisfied){
            ArrayList<URI> nyms = WalletUtils.extractPseudonyms(pt);
            if (nyms.size()==1){
                return nyms.get(0).toString();

            } else {
                throw new UxException(ErrorMessages.TOKEN_INVALID, "Unexpected Endonyms=" + nyms.size());

            }
        } else {
            throw new UxException(ErrorMessages.TOKEN_INVALID, "The token did not verify");

        }
    }

    private FulfillmentReport fulfillmentReport(SsoChallenge c) throws Exception {
        HashMap<String, RulebookAuth> honest = c.getHonestUnder();
        FulfillmentReport report = null;

        if (honest.isEmpty() && c.isSybil()){
            report = justAddSybil();

        } else if (honest.isEmpty()) {
            report = new FulfillmentReport();

        } else {
            report = fulfillRulebookRequirements(honest, c);

        }
        boolean valid = true;
        if (c.getDomain()!=null){
            String endonymScope = c.getDomain().toString();
            valid = verifyPseudonym(endonymScope, true);
            report.setEndonymUnderDomain(endonymScope);

        }
        if (valid){
            return report;

        } else {
            throw new UxException(ErrorMessages.UNEXPECTED_PSEUDONYM_REQUEST,
                    c.getDomain().toString());
        }

    }

    private FulfillmentReport justAddSybil() throws Exception {
        WalletReport openWallet = peekInWallet(exo.getId());
        FulfillmentReport report = new FulfillmentReport();
        addSybilToReport(openWallet, report);
        return report;

    }

    private void addSybilToReport(WalletReport openWallet, FulfillmentReport report) throws Exception {
        URI sybil = exo.getNetworkMap().nmiForSybilTestNet().getLastIssuerUID();
        if (openWallet.hasSybil()){
            report.getIssuersToUse().add(sybil.toString());

        } else {
            RulebookAuth auth = new RulebookAuth();
            auth.setRulebookUID(Rulebook.SYBIL_RULEBOOK_UID_TEST);
            report.getMissing().add(auth);

        }
    }

    private FulfillmentReport fulfillRulebookRequirements(HashMap<String, RulebookAuth> honest, SsoChallenge c) throws Exception {
        WalletReport openWallet = peekInWallet(exo.getId());
        HashMap<String, HashSet<URI>> rulebooksToIssuers = openWallet.getRulebooksToIssuers();
        FulfillmentReport report = new FulfillmentReport();

        for (String rulebook : honest.keySet()){
            HashSet<URI> issuers = rulebooksToIssuers.get(rulebook);
            if (issuers!=null){
                RulebookAuth required = honest.get(rulebook);

                issuers = WalletReport.safeList(issuers, required);
                if (issuers.isEmpty()){
                    report.getMissing().add(required);

                } else if (issuers.size()==1){
                    List<URI> tmp = new ArrayList<>(issuers);
                    report.getIssuersToUse().add(tmp.get(0).toString());

                } else {
                    report.getUserChoices().add(rulebook, issuers);

                }
            } else {
                report.getMissing().add(honest.get(rulebook));

            }
        }
        report.isProvable();
//        addSybilToReport(openWallet, report);
        return report;

    }



    private String proofForRulebookSSO(ArrayList<String> issuerUids,
                                         String pseudonym,
                                         JsonObject metadata) throws Exception {
        try {
            if (WhiteList.url(pseudonym)){
                List<String> nyms = List.of(pseudonym);
                PresentationPolicy pp = proofRequest(issuerUids, nyms, metadata, true);
                PresentationToken token = proveFromPresentationPolicy(pp);
                String xml = IdContainer.convertObjectToXml(token);
                String[] parts = pseudonym.split("//");
                String protocol = parts[0];
                String domain = parts[1].split("/")[0];
                String target = protocol + "//" + domain + "/exonym";
                logger.info(target);
                Http client = new Http();

                return client.basicPost(target, xml);

            } else if (pseudonym==null){
                PresentationPolicy pp = proofRequest(issuerUids, null, metadata, true);
                PresentationToken token = proveFromPresentationPolicy(pp);
                return IdContainer.convertObjectToXml(token);

            } else {
                throw new UxException(ErrorMessages.UNEXPECTED_PSEUDONYM_REQUEST,
                        "SSO authentications require a domain name");

            }
        } catch (ClientProtocolException e) {
            throw new UxException(ErrorMessages.SSO_END_POINT_404, e);

        }
    }

    private PresentationToken proveFromPresentationPolicy(PresentationPolicy pp) throws Exception {
        ExonymOwner owner = exo.getOwner();
        PresentationTokenDescription ptd = owner.canProveClaimFromPolicy(pp);
        PresentationPolicyAlternatives ppa = WalletUtils.openPPA(pp);

        if (ptd==null){
            determineUnfulfilled(ptd, ppa);
            return null;

        } else {
            try {
                return owner.proveClaim(ptd, ppa);

            } catch (UxException e) {
                logger.info("Witness error - attempting to update revocation information");
                ArrayList<URI> mods = owner.listAllMods(ptd);
                for (URI mod : mods){
                    refreshNode(mod);

                }
                this.exo.getOwner().clearStale();
                this.exo.reopen(ptd);
                return owner.proveClaim(ptd, ppa);
            }
        }
    }

    private void refreshNode(URI mod) throws Exception {
        NetworkMapItemModerator nmiMod = (NetworkMapItemModerator)
                this.exo.getNetworkMap().nmiForNode(mod);
        NodeVerifier verified = this.exo.getNetworkMap()
                .openNodeVerifier(nmiMod.getStaticURL0(), false);
        ArrayList<String> rais = new ArrayList<>(verified.getAllRevocationInformationFileNames());
        // TODO if we need multiple parameters
        this.exo.getCache().store(
                verified.getRevocationInformation(rais.get(0)));

    }

    protected PresentationPolicy proofRequest(ArrayList<String> issuerUids,
                                              ArrayList<String> pseudonyms) throws Exception {
        return proofRequest(issuerUids, pseudonyms, null, false);

    }

    protected PresentationPolicy proofRequest(ArrayList<String> issuerUids,
                                              ArrayList<String> pseudonyms, JsonObject metadata) throws Exception {
        return proofRequest(issuerUids, pseudonyms, metadata, false);

    }

    private PresentationPolicy proofRequest(ArrayList<String> issuerUids,
                                              List<String> pseudonyms,
                                              JsonObject metadata, boolean allowURLs) throws Exception {
        URI ppUID = URI.create(Namespace.URN_PREFIX_COLON +
                UUID.randomUUID().toString().replaceAll("-", "") + ":pp");

        HashMap<URI, UIDHelper> helpers = new HashMap<>();
        PkiExternalResourceContainer external = PkiExternalResourceContainer.getInstance();
        BuildPresentationPolicy bpp = new BuildPresentationPolicy(ppUID, external);
        if (metadata==null){
            bpp.makeInteractive();

        } else {
            bpp.makeNonInteractive(metadata.toString());

        }
        if (!issuerUids.isEmpty()){
            HashSet<URI> credentials = new HashSet<>();

            HashMap<URI, ArrayList<CredentialInPolicy.IssuerAlternatives.IssuerParametersUID>>
                    credentialToIssuers = new HashMap<>();

            for (String issuer : issuerUids){
                UIDHelper helper = new UIDHelper(issuer);
                helpers.put(helper.getCredentialSpec(), helper);
                credentials.add(helper.getCredentialSpec());

                ArrayList<CredentialInPolicy.IssuerAlternatives.IssuerParametersUID>
                        issuers = credentialToIssuers.getOrDefault(
                        helper.getCredentialSpec(),
                        new ArrayList<>());

                issuers.add(helper.computeIssuerParametersUID());
                credentialToIssuers.put(helper.getCredentialSpec(), issuers);
                ExonymOwner owner = exo.getOwner();
                owner.openResourceIfNotLoaded(helper.getRevocationInfoParams());

            }
            URI sybilTestnetUID = exo.getNetworkMap().nmiForSybilTestNet().getLastIssuerUID();
            UIDHelper sybilHelper = new UIDHelper(sybilTestnetUID);
            bpp.addCredentialInPolicy(sybilHelper.getCredentialSpecAsList(),
                    sybilHelper.computeIssuerParametersUIDAsList(),
                    UUID.randomUUID().toString(), DEFAULT_ALIAS);

            CredentialSpecification sybilCS = external.openResource(sybilHelper.getCredentialSpecFileName());
            AttributeDescription attDesc = sybilCS.getAttributeDescriptions()
                    .getAttributeDescription().get(0);

            for (URI credential : credentials){
                UIDHelper helper = helpers.get(credential);

                bpp.addCredentialInPolicy(helper.getCredentialSpecAsList(),
                        credentialToIssuers.get(credential), UUID.randomUUID().toString(), DEFAULT_ALIAS);

                bpp.addDisclosableAttributeForCredential(
                        helper.getCredentialSpec(), attDesc,
                        BuildPresentationPolicy.createInspectorAlternatives(helper.getInspectorParams()),
                        "On suspected violation of rulebook");

            }
        }
        bpp.addPseudonym(BLACKLIST_PSEUDONYMS[0], false, BLACKLIST_PSEUDONYMS[0]);
        pseudonyms = pseudonyms==null ? Collections.EMPTY_LIST : pseudonyms;
        for (String pseudonym : pseudonyms){
            if (verifyPseudonym(pseudonym, allowURLs)){
                bpp.addPseudonym(pseudonym, true, null, DEFAULT_ALIAS);

            }
        }
        return bpp.getPolicy();

    }

    private boolean verifyPseudonym(PresentationPolicyAlternatives ppa, boolean allowURLs) throws UxException {
        int i = 0;
        boolean result = true;
        for (PresentationPolicy pp : ppa.getPresentationPolicy()){
            for (PseudonymInPolicy nym : pp.getPseudonym()){
                if (nym.isExclusive()){
                    String scope = nym.getScope();
                    if (!verifyPseudonym(scope, allowURLs)){
                        result = false;

                    }
                    i++;
                }
            }
        }
        if ((allowURLs && i > 1) || !result){
            throw new UxException(ErrorMessages.UNEXPECTED_PSEUDONYM_REQUEST,
                    "Only one pseudonym is allowed for delegations");

        } else {
            return true;

        }
    }

    private boolean verifyPseudonym(String pseudonym, boolean allowURLs) {
        if (BLACKLIST.contains(pseudonym)){
            return false;

        }
        if (WhiteList.url(pseudonym) && allowURLs){
            return true;

        } else if (pseudonym.contains(":")){
            String[] parts = pseudonym.split(":");
            String expiry = parts[parts.length-1];

            if (WhiteList.isNumbers(expiry)){
                long exp = Long.parseLong(expiry);
                return DateHelper.isTargetInFutureWithinPeriod(
                        Instant.ofEpochMilli(exp), Period.of(0,0,366));

            }
        }
        return false;
    }

    // todo clarify use cases - then protected
    private String proofForPolicy(String policy) throws Exception {
        ExonymOwner owner = exo.getOwner();
        PresentationPolicyAlternatives ppa = WalletUtils.openPPA(policy);
        verifyPresentationPolicyAlternatives(ppa);
        PresentationTokenDescription ptd = owner.canProveClaimFromPolicy(ppa);
        if (ptd!=null){
            PresentationToken proof = owner.proveClaim(ptd, ppa);
            return IdContainer.convertObjectToXml(proof);

        } else {
            return determineUnfulfilled(ptd, ppa);

        }
    }

    private void verifyPresentationPolicyAlternatives(PresentationPolicyAlternatives ppa) {
        // todo
    }

    private String determineUnfulfilled(PresentationTokenDescription ptd, PresentationPolicyAlternatives ppa) throws UxException {
        try {
            exo.getOwner().proveClaim(ptd, ppa);
            return null;

        } catch (UxException e) {
            ArrayList<String> needed = e.getInfo();
            StringBuilder b = new StringBuilder();
            for (String n : needed){
                b.append(n);

            }
            return b.toString();

        } catch (Exception e){
            throw new UxException(ErrorMessages.UNEXPECTED_TOKEN_FOR_THIS_NODE_OR_AUTH_TIMEOUT, e);

        }
    }

    private <T> T jsonToClass(String ssoCJson, Class<?> clazz) throws UxException {
        try {
            return (T) JaxbHelper.jsonToClass(ssoCJson, clazz);

        } catch (Exception e) {
            throw new UxException(ErrorMessages.UNKNOWN_COMMAND, e, "Message class error");

        }
    }

    private String decodeRequest(String challengeB64) throws UxException {
        try {
            byte[] decompressed = WalletUtils.decompress(Base64.decodeBase64(challengeB64));
            return new String(decompressed, StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new UxException(ErrorMessages.UNKNOWN_COMMAND, e, "Garbage request");

        }
    }

    //
    // Accepts both polcies and authentication requests and reports on what's required.
    //
    protected FulfillmentReport authenticationSummaryForULink(String authRequest) throws Exception {
        SsoChallenge c = AuthenticationWrapper.unwrapFromUniversalLink(authRequest, SsoChallenge.class);
        logger.info(c.getChallenge());
        return fulfillmentReport(c);

    }

    protected String walletReport() throws Exception {
        return JaxbHelper.serializeToJson(
                peekInWallet(exo.getId()), WalletReport.class);

    }

    private static WalletReport peekInWallet(AbstractIdContainer x) throws  Exception{
        ArrayList<String> issuers =  x.getOwnerSecretList();
        WalletReport report = new WalletReport();
//        HashMap<String, HashSet<URI>> map = report.getRulebooksToIssuers();

        for (String assetInWallet : issuers){

            if (FileType.isCredential(assetInWallet)){
                UIDHelper h =  new UIDHelper(IdContainer.fileNameToUid(assetInWallet));
                report.add(h.getRulebookUID().toString(), h.getIssuerParameters());

            } else if (FileType.isSftp(assetInWallet)){
                report.getSftpAccess().add(IdContainer.fileNameToUid(assetInWallet));

            }
        }
        return report;
    }

}
