package io.exonym.lib.wallet;

import eu.abc4trust.xml.*;
import io.exonym.lib.helpers.UIDHelper;
import io.exonym.lib.abc.util.JaxbHelper;
import io.exonym.lib.api.IdContainerJSON;
import io.exonym.lib.pojo.NetworkMapItemModerator;
import io.exonym.lib.lite.Http;
import io.exonym.lib.exceptions.ErrorMessages;
import io.exonym.lib.exceptions.UxException;
import io.exonym.lib.pojo.Rulebook;
import io.exonym.lib.pojo.SerialErrorHandling;
import io.exonym.lib.standard.Const;
import io.exonym.lib.standard.PassStore;
import io.exonym.lib.pojo.IssuanceSigma;
import io.exonym.lib.standard.ExtractObject;

import java.net.URI;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.logging.Logger;

public  class RulebookOnboarding {
    
    private final static Logger logger = Logger.getLogger(RulebookOnboarding.class.getName());

    public static String onboardRulebook(PassStore store, Path path, URI advocateUID) throws Exception {
        NetworkMapItemModerator advocateNmia = WalletUtils.determinedSearchForAdvocate(path, advocateUID);
        Http client = new Http();
        String json = client.basicGet(advocateNmia.getRulebookNodeURL() + Const.ENDPOINT_JOIN);
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

        ExonymToolset exo = new ExonymToolset(store, path);
        IssuanceMessageAndBoolean imab = WalletUtils.deserialize(decoded);
        NetworkMapItemModerator advocateNmi = discoverAdvocate(imab, exo.getNetworkMap());

        ExonymOwner owner = exo.getOwner();
        IssuanceMessage im = owner.issuanceStep(imab, store.getEncrypt());

        Http client = new Http();
        String response = client.basicPost(advocateNmi.getRulebookNodeURL() + Const.ENDPOINT_JOIN,
                IdContainerJSON.convertObjectToXml(im));
        if (response.startsWith("{")){
            return response;
        }
        IssuanceMessageAndBoolean completionToken = processIssuanceMessage(response);
        owner.issuanceStep(completionToken, store.getEncrypt());

        return prepareResponse(completionToken, advocateNmi.getLastIssuerUID());

    }

    protected static NetworkMapItemModerator discoverAdvocate(IssuanceMessageAndBoolean imab, NetworkMap map) throws Exception {
        IssuancePolicy policy = ExtractObject.extract(imab.getIssuanceMessage().getContent(), IssuancePolicy.class);
        assert policy != null;
        CredentialTemplate template = policy.getCredentialTemplate();
        UIDHelper helper = new UIDHelper(template.getIssuerParametersUID());
        return WalletUtils.determinedSearchForAdvocate(map, helper.getModeratorUid());

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
