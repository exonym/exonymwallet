package io.exonym.lib.wallet;

import eu.abc4trust.xml.*;
import io.exonym.lib.helpers.UIDHelper;
import io.exonym.lib.abc.util.JaxbHelper;
import io.exonym.lib.api.XContainerJSON;
import io.exonym.lib.pojo.NetworkMapItemAdvocate;
import io.exonym.lib.lite.Http;
import io.exonym.lib.exceptions.ErrorMessages;
import io.exonym.lib.exceptions.UxException;
import io.exonym.lib.pojo.Rulebook;
import io.exonym.lib.pojo.SerialErrorHandling;
import io.exonym.lib.standard.PassStore;
import io.exonym.lib.pojo.IssuanceSigma;
import io.exonym.lib.standard.ExtractObject;

import java.net.URI;
import java.nio.file.Path;
import java.util.HashMap;

public  class RulebookOnboarding {

    public static String onboardRulebook(PassStore store, Path path, URI advocateUID) throws Exception {
        NetworkMapItemAdvocate advocateNmia = WalletUtils.determinedSearchForAdvocate(path, advocateUID);
        Http client = new Http();
        String json = client.basicGet(advocateNmia.getRulebookNodeURL() + "/subscribe");
        Rulebook rulebook = JaxbHelper.jsonToClass(json, Rulebook.class);
        return onboardRulebook(store, path, rulebook.getChallengeB64());

    }

    public static String onboardRulebook(PassStore store, Path path, String issuancePolicy) throws Exception {
        ExonymToolset exo = new ExonymToolset(store, path);
        IssuanceMessageAndBoolean imab = WalletUtils.deserialize(issuancePolicy);
        NetworkMapItemAdvocate advocateNmi = discoverAdvocate(imab, exo.getNetworkMap());

        ExonymOwner owner = exo.getOwner();
        IssuanceMessage im = owner.issuanceStep(imab, store.getEncrypt());

        Http client = new Http();
        String response = client.basicPost(advocateNmi.getRulebookNodeURL() + "/subscribe",
                XContainerJSON.convertObjectToXml(im));

        IssuanceMessageAndBoolean completionToken = processIssuanceMessage(response);
        owner.issuanceStep(completionToken, store.getEncrypt());

        return prepareResponse(completionToken, advocateNmi.getLastIssuerUID());

    }

    protected static NetworkMapItemAdvocate discoverAdvocate(IssuanceMessageAndBoolean imab, NetworkMap map) throws Exception {
        IssuancePolicy policy = ExtractObject.extract(imab.getIssuanceMessage().getContent(), IssuancePolicy.class);
        assert policy != null;
        CredentialTemplate template = policy.getCredentialTemplate();
        UIDHelper helper = new UIDHelper(template.getIssuerParametersUID());
        return WalletUtils.determinedSearchForAdvocate(map, helper.getNodeUid());

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
