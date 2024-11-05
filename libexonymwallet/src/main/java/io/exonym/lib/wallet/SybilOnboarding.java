package io.exonym.lib.wallet;

import com.beust.jcommander.internal.Nullable;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import eu.abc4trust.xml.*;
import io.exonym.lib.helpers.UIDHelper;
import io.exonym.lib.abc.util.JaxbHelper;
import io.exonym.lib.api.Cache;
import io.exonym.lib.api.IdContainerJSON;
import io.exonym.lib.pojo.*;
import io.exonym.lib.actor.NodeVerifier;
import io.exonym.lib.api.PkiExternalResourceContainer;
import io.exonym.lib.helpers.Parser;
import io.exonym.lib.lite.Http;
import io.exonym.lib.exceptions.ErrorMessages;
import io.exonym.lib.exceptions.UxException;
import io.exonym.lib.standard.AsymStoreKey;
import io.exonym.lib.standard.PassStore;
import org.apache.commons.codec.binary.Hex;

import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public class SybilOnboarding {
    
    private final static Logger logger = Logger.getLogger(SybilOnboarding.class.getName());

    public static final String SYBIL_URL_TEST_NET = "http://localhost:8079/register/";
    public static final String SYBIL_URL_TEST_NET_C30 = "http://localhost:8079/c30-register/";

    public static String testNet(PassStore store, Path rootPath, String sybilUrl, String sybilClass) throws Exception {
        ExonymToolset toolset = new ExonymToolset(store, rootPath);
        URI sybilCSpecUID = URI.create(Rulebook.SYBIL_RULEBOOK_UID_TEST + ":c");

        CredentialSpecification cSpec = toolset.getExternal().openResource(
                IdContainerJSON.uidToXmlFileName(sybilCSpecUID));

        NetworkMap networkMap = toolset.getNetworkMap();

        NetworkMapItemLead sybilLeadTarget = openSybilLead(
                Rulebook.SYBIL_LEAD_UID_TEST.toString(), networkMap);

        NetworkMapItemModerator modSybil = targetSybil(
                sybilLeadTarget, networkMap, false);

        return onboarding(toolset, store, cSpec, modSybil, sybilClass, sybilUrl, null);
    }

    public static String mainNet(PassStore store, Path rootPath, String sybilClass) throws Exception {
        ExonymToolset toolset = new ExonymToolset(store, rootPath);
        URI sybilCSpecUID = URI.create(Rulebook.SYBIL_RULEBOOK_UID_MAIN + ":c");

        CredentialSpecification cSpec = toolset.getExternal().openResource(
                IdContainerJSON.uidToXmlFileName(sybilCSpecUID));

        NetworkMap networkMap = toolset.getNetworkMap();

        NetworkMapItemLead sybilLeadTarget = openSybilLead(
                Rulebook.SYBIL_LEAD_UID_MAIN.toString(), networkMap);

        NetworkMapItemModerator modSybil = targetSybil(
                sybilLeadTarget, networkMap, false);

        String sybilUrl = "https://sybil.exonym.io/register/";

        return onboarding(toolset, store, cSpec, modSybil, sybilClass, sybilUrl, null);
    }

    public static IdContainerSchema c30TestNet(IdContainerSchema schema, Path rootPath,
                                    URI sybilUrl, Http client, AsymStoreKey key,
                                    String alpha, String beta, String gamma) throws Exception {

        String epsilon = schema.getUsername();
        PassStore store = new PassStore(epsilon, false);
        store.setUsername(epsilon);

        ExonymToolset toolset = new ExonymToolset(store, rootPath, schema);
        URI sybilCSpecUID = URI.create(Rulebook.SYBIL_RULEBOOK_UID_TEST + ":c");

        CredentialSpecification cSpec = toolset.getExternal().openResource(
                IdContainerJSON.uidToXmlFileName(sybilCSpecUID));

        NetworkMap networkMap = toolset.getNetworkMap();

        NetworkMapItemLead sybilLeadTarget = openSybilLead(
                Rulebook.SYBIL_LEAD_UID_TEST.toString(), networkMap);

        NetworkMapItemModerator modSybil = targetSybil(
                sybilLeadTarget, networkMap, false);

        String nonce = client.basicGet(SybilOnboarding.SYBIL_URL_TEST_NET_C30);
        byte[] sig = key.encryptWithPrivateKey(nonce.getBytes(StandardCharsets.UTF_8));
        String hexSig = Hex.encodeHexString(sig);
        String pathToGamma = alpha + "/" + beta + "/" + gamma + "/" + hexSig;
        String url = sybilUrl.toString() + "c30-register/" + pathToGamma;
        logger.info(url);

        String response = onboarding(toolset, store, cSpec, modSybil,
                "c30", url, client);

        JsonObject res = JsonParser.parseString(response).getAsJsonObject();

        if (res.has("issuerUid")){
            return C30Utils.c30SchemaFromDisk(rootPath, epsilon);

        } else {
            throw new UxException(response);

        }
    }

    public static String c30MainNet(PassStore store, Path rootPath, Http client) throws Exception {
        ExonymToolset toolset = new ExonymToolset(store, rootPath);
        URI sybilCSpecUID = URI.create(Rulebook.SYBIL_RULEBOOK_UID_MAIN + ":c");

        CredentialSpecification cSpec = toolset.getExternal().openResource(
                IdContainerJSON.uidToXmlFileName(sybilCSpecUID));

        NetworkMap networkMap = toolset.getNetworkMap();

        NetworkMapItemLead sybilLeadTarget = openSybilLead(
                Rulebook.SYBIL_LEAD_UID_MAIN.toString(), networkMap);

        NetworkMapItemModerator modSybil = targetSybil(
                sybilLeadTarget, networkMap, false);

        String sybilUrl = "https://sybil.cyber30.io/c30-register/";

        return onboarding(toolset, store, cSpec, modSybil,
                "c30", sybilUrl, client);

    }



    private static String onboarding(ExonymToolset toolset, PassStore store,
                                     CredentialSpecification cSpec,
                                     NetworkMapItemModerator modSybil,
                                     String sybilClassOfUser,
                                     String sybilUrl, @Nullable Http client) throws Exception {

        NodeVerifier verifiedMod = NodeVerifier.openNode(
                modSybil.getStaticURL0(), false, false);
        UIDHelper helper = verifiedMod.getUidHelperForMostRecentIssuerParameters();
        ExonymOwner owner = toolset.getOwner();
        Cache cache = toolset.getCache();

        owner.addCredentialSpecification(cSpec);

        IssuerParameters ip = verifiedMod.getIssuerParameters(
                helper.getIssuerParametersFileName());

        cache.store(ip);
        owner.addIssuerParameters(ip);

        RevocationAuthorityParameters rap = verifiedMod.getRevocationAuthorityParameters(
                helper.getRevocationAuthorityFileName());
        cache.store(rap);
        owner.addRevocationAuthorityParameters(rap);

        RevocationInformation ri = verifiedMod.getRevocationInformation(
                helper.getRevocationInformationFileName());
        cache.store(ri);
        owner.addRevocationInformation(ri.getRevocationAuthorityParametersUID(), ri);

        IssuanceSigma hello = new IssuanceSigma();
        hello.setTestNet(true);
        hello.setHello(UUID.randomUUID().toString());
        hello.setSybilClass(sybilClassOfUser);

        if (client == null){ client = new Http();}

        logger.info("URL@Hello=" + sybilUrl);

        String response = client.basicPost(sybilUrl,
                JaxbHelper.serializeToJson(hello, IssuanceSigma.class));

        IssuanceSigma in = JaxbHelper.jsonToClass(response, IssuanceSigma.class);
        WalletUtils.rejectOnError(in);

        IssuanceMessageAndBoolean imab = Parser.parseIssuanceMessageAndBoolean(in.getImab());
        System.out.println(IdContainer.convertObjectToXml(imab.getIssuanceMessage()));

        IssuanceMessage message = owner.issuanceStep(imab, store.getEncrypt());
        System.out.println(IdContainer.convertObjectToXml(message));

        IssuanceSigma hello2 = new IssuanceSigma();
        hello2.setTestNet(true);
        hello2.setHello(hello.getHello());
        hello2.setIm(Parser.parseIssuanceMessage(message));
        sybilUrl = sybilUrl.substring(0, sybilUrl.lastIndexOf("/"));

        String responseB = client.basicPost(sybilUrl,
                JaxbHelper.serializeToJson(hello2, IssuanceSigma.class));

        IssuanceSigma in2 = JaxbHelper.jsonToClass(responseB, IssuanceSigma.class);
        WalletUtils.rejectOnError(in2);

        in2.setTestNet(true);
        in2.setSybilClass(sybilClassOfUser);

        IssuanceMessageAndBoolean imab2 = Parser.parseIssuanceMessageAndBoolean(in2.getImab());
        owner.issuanceStep(imab2, store.getEncrypt());

        return Parser.parseIssuanceResult(in2);

    }


    private static NetworkMapItemModerator targetSybil(NetworkMapItemLead sybilLeadTarget,
                                                       NetworkMap map, boolean mainNet) throws Exception {
        List<URI> mods = sybilLeadTarget.getModeratorsForLead();
        for (URI modUid : mods){
            if (mainNet && (modUid.toString().contains("main"))){
                return (NetworkMapItemModerator) map.nmiForNode(modUid);

            } else if ((modUid.toString().contains("test"))) {
                return (NetworkMapItemModerator) map.nmiForNode(modUid);

            }
        }
        throw new UxException(ErrorMessages.SYBIL_WARN + ":main-net=" + mainNet);
    }

    private static NetworkMapItemLead openSybilLead(String rulebookId, NetworkMap map) throws Exception {
        List<String> leads = map.getLeadFileNamesForRulebook(rulebookId);
        return (NetworkMapItemLead) map.nmiForNode(map.fromNmiFilename(leads.get(0)));
    }

    private static NetworkMap openNetworkMap(Path path) throws Exception {
        NetworkMap map = new NetworkMap(path);
        if (!map.networkMapExists()) {
            map.spawn();
        }
        return map;

    }
}
