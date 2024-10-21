package io.exonym.lib.wallet;

import eu.abc4trust.xml.*;
import io.exonym.lib.helpers.UIDHelper;
import io.exonym.lib.abc.util.JaxbHelper;
import io.exonym.lib.api.IdContainerJSON;
import io.exonym.lib.pojo.*;
import io.exonym.lib.lite.Http;
import io.exonym.lib.exceptions.ErrorMessages;
import io.exonym.lib.exceptions.UxException;
import io.exonym.lib.standard.Const;
import io.exonym.lib.standard.PassStore;
import io.exonym.lib.standard.ExtractObject;
import org.apache.commons.codec.binary.Base64;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public  class RulebookOnboarding {
    
    private final static Logger logger = Logger.getLogger(RulebookOnboarding.class.getName());

    public static String onboardRulebook(PassStore store, Path path, URI modUid) throws Exception {
        NetworkMapItemModerator modNmi = WalletUtils.determinedSearchForModerator(path, modUid);
        Http client = new Http();
        String json = client.basicGet(modNmi.getRulebookNodeURL() + Const.ENDPOINT_JOIN);
        Rulebook rulebook = JaxbHelper.jsonToClass(json, Rulebook.class);
        if (rulebook.getLink()!=null){
            return onboardRulebook(store, path, rulebook.getLink());

        } else {
            throw new UxException(ErrorMessages.MODERATOR_DOES_NOT_ACCEPT_OPEN_JOIN_REQUESTS);

        }
    }

    public static String onboardRulebook(PassStore store, Path path, String issuancePolicy) throws Exception {
        issuancePolicy = WalletUtils.isolateUniversalLinkContent(issuancePolicy);
        String decoded = WalletUtils.decodeCompressedB64(issuancePolicy);
        Pattern pattern = Pattern.compile("<abc:Nonce>([^<]+)</abc:Nonce>");
        Matcher matcher = pattern.matcher(decoded);
        boolean appeal = false;

        if (matcher.find()) {
            String nonceValue = matcher.group(1);
            byte[] nonce = Base64.decodeBase64(nonceValue);
            appeal = nonce.length==16;
            logger.info("isAppeal=" + appeal);

        }

        ExonymToolset exo = new ExonymToolset(store, path);
        IssuanceMessageAndBoolean imab = WalletUtils.deserialize(decoded);

        NetworkMapItemModerator modNmi = discoverMod(imab, exo.getNetworkMap());

        ExonymOwner owner = exo.getOwner();
        IssuanceMessage im = owner.issuanceStep(imab, store.getEncrypt());

        String endPoint = modNmi.getRulebookNodeURL() +
                (appeal ? Const.ENDPOINT_APPEAL : Const.ENDPOINT_JOIN);

        logger.info("Targeting endpoint: " + endPoint);

        Http client = new Http();
        String response = client.basicPost(endPoint, IdContainerJSON.convertObjectToXml(im));

        if (response.startsWith("{")){
            RejoinCriteria criteria = JaxbHelper.gson.fromJson(response, RejoinCriteria.class);

            if (criteria.getError()!=null){
                return criteria.getError();

            } else {
                return evalRejoinCriteria(criteria, exo, store);

            }
        }
        IssuanceMessageAndBoolean completionToken = processIssuanceMessage(response);
        owner.issuanceStep(completionToken, store.getEncrypt());

        return prepareResponse(completionToken, modNmi.getLastIssuerUID());

    }

    protected static NetworkMapItemModerator discoverMod(
            IssuanceMessageAndBoolean imab, NetworkMap map) throws Exception {

        IssuancePolicy policy = ExtractObject.extract(
                imab.getIssuanceMessage().getContent(), IssuancePolicy.class);

        assert policy != null;

        CredentialTemplate template = policy.getCredentialTemplate();
        UIDHelper helper = new UIDHelper(template.getIssuerParametersUID());
        return WalletUtils.determinedSearchForModerator(map, helper.getModeratorUid());

    }

    // TODO - the node will communicate re-issuance criteria
    // either report back the conditions, or join if can be met.
    // {"canRejoin": true, revokedModerators : { modUid_0 .... , modUid_1, .... } }
    // if can rejoin, then delete the list of revoked credentials, but only after successful reissue.
    private static String evalRejoinCriteria(RejoinCriteria criteria, ExonymToolset exo,
                                             PassStore store) throws UxException {
        try {
            if (criteria.isCanRejoin()){
                removeRevokedCredentials(exo.getOwner(),
                        criteria.getRevokedModerators(), store);

                IssuanceMessageAndBoolean imab = WalletUtils.deserialize(
                        criteria.getImabFinalB64());

                exo.getOwner().issuanceStep(
                        imab, store.getEncrypt());
                criteria.setImabFinalB64(null);

                return JaxbHelper.gson.toJson(criteria);

            } else {
                return JaxbHelper.gson.toJson(criteria);

            }
        } catch (Exception e) {
            throw new UxException(ErrorMessages.TOKEN_INVALID + " " + e.getMessage());

        }
    }

    private static void removeRevokedCredentials(ExonymOwner owner, ArrayList<URI> revokedModerators, PassStore store) throws Exception {
        ArrayList<String> got = owner.getContainer().getOwnerSecretList();
        HashMap<String, String> modToFile = new HashMap<>();
        for (String ic : got){
            String mod = ic.replaceAll("\\.[0-9a-fA-F]+\\.ic\\.xml$", "");
            logger.info("Got Index for Credential= " + mod + ", " + ic);
            modToFile.put(mod, ic);

        }
        ArrayList<String> toRemove = new ArrayList<>();
        for (URI mod : revokedModerators){
            try {
                String f = IdContainerJSON.uidToFileName(mod);
                String remove = modToFile.get(f);
                toRemove.add(remove);

            } catch (Exception e) {
                logger.info("Error processing mod: " + mod);

            }
        }
        IdContainerJSON id = (IdContainerJSON)owner.getContainer();
        for (String delete : toRemove){
            id.deleteCredential(delete, store);

        }
    }


    private static IssuanceMessageAndBoolean processIssuanceMessage(String response) throws Exception {
        if (response==null){
            throw new UxException(ErrorMessages.NO_SESSION_MAINTAINED,
                    "Failed to collect response from request");
        }
        if (response.startsWith("{")){
            IssuanceSigma errorSigma = JaxbHelper.jsonToClass(response, IssuanceSigma.class);
            handleError(errorSigma);

        }
        return WalletUtils.deserialize(response);

    }

    public static void handleError(SerialErrorHandling errorSigma) throws UxException {
        String[] info = new String[errorSigma.getInfo().size()];
        int i = 0;
        for (String e : errorSigma.getInfo()){
            info[i] = e;
            i++;
        }
        throw new UxException(errorSigma.getError(), info);
    }

    private static String prepareResponse(IssuanceMessageAndBoolean completionToken, URI lastIssuerUID) throws Exception {
        HashMap<String, String> result = new HashMap<String, String>();
        result.put("issuerUid", lastIssuerUID.toString());
        result.put("issued", "true");
        return JaxbHelper.serializeToJson(result, HashMap.class);
    }
}
