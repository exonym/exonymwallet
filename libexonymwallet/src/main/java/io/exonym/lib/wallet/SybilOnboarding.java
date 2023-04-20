package io.exonym.lib.wallet;

import eu.abc4trust.xml.*;
import io.exonym.lib.helpers.UIDHelper;
import io.exonym.lib.abc.util.JaxbHelper;
import io.exonym.lib.api.Cache;
import io.exonym.lib.api.XContainerJSON;
import io.exonym.lib.pojo.NetworkMapItemAdvocate;
import io.exonym.lib.pojo.NetworkMapItemSource;
import io.exonym.lib.actor.NodeVerifier;
import io.exonym.lib.api.PkiExternalResourceContainer;
import io.exonym.lib.helpers.Parser;
import io.exonym.lib.lite.Http;
import io.exonym.lib.exceptions.ErrorMessages;
import io.exonym.lib.exceptions.UxException;
import io.exonym.lib.pojo.Namespace;
import io.exonym.lib.pojo.Rulebook;
import io.exonym.lib.standard.PassStore;
import io.exonym.lib.pojo.IssuanceSigma;

import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

public class SybilOnboarding {

     private final static String sybilUrl = "http://exonym-x-03:8080/";

    public static String testNet(PassStore store, Path rootPath, String sybilClass) throws Exception {
        URL registerUrl = new URL(sybilUrl + "register");
        Cache cache = new Cache(rootPath);
        NetworkMap map = openNetworkMap(rootPath.resolve("network-map"));
        PkiExternalResourceContainer external = PkiExternalResourceContainer.getInstance();
        external.setNetworkMapAndCache(map, cache);

        URI sybilRulebookID = URI.create(Namespace.URN_PREFIX_COLON + Rulebook.SYBIL_RULEBOOK_HASH);
        URI sybilCSpecUID = URI.create(Namespace.URN_PREFIX_COLON + Rulebook.SYBIL_RULEBOOK_HASH + ":c");
        CredentialSpecification cSpec = external.openResource(XContainerJSON.uidToXmlFileName(sybilCSpecUID));

        NetworkMapItemSource sybilSourceTarget = openSybilSource(sybilRulebookID.toString(), map);
        NetworkMapItemAdvocate testNetTarget = targetSybil(sybilSourceTarget, map, false);
        NodeVerifier verifiedAdvocate = NodeVerifier.openNode(testNetTarget.getStaticURL0().toURI(), false);
        UIDHelper helper = verifiedAdvocate.getUidHelperForMostRecentIssuerParameters();

        XContainerJSON x = new XContainerJSON(ExonymToolset.pathToContainers(rootPath), store.getUsername());
        ExonymOwner owner = new ExonymOwner(x);
        owner.openContainer(store);
        owner.addCredentialSpecification(cSpec);

        IssuerParameters ip = verifiedAdvocate.getIssuerParameters(helper.getIssuerParametersFileName());
        cache.store(ip);
        owner.addIssuerParameters(ip);

        RevocationAuthorityParameters rap = verifiedAdvocate.getRevocationAuthorityParameters(
                helper.getRevocationAuthorityFileName());
        cache.store(rap);
        owner.addRevocationAuthorityParameters(rap);

        RevocationInformation ri = verifiedAdvocate.getRevocationInformation(
                helper.getRevocationInformationFileName());
        cache.store(ri);
        owner.addRevocationInformation(ri.getRevocationAuthorityParametersUID(), ri);

        IssuanceSigma hello = new IssuanceSigma();
        hello.setTestNet(true);
        hello.setHello(UUID.randomUUID().toString());
        hello.setSybilClass(sybilClass);

        Http client = new Http();
        String response = client.basicPost(
                registerUrl.toString(), JaxbHelper.serializeToJson(hello, IssuanceSigma.class));
        IssuanceSigma in = JaxbHelper.jsonToClass(response, IssuanceSigma.class);

        IssuanceMessageAndBoolean imab = Parser.parseIssuanceMessageAndBoolean(in.getImab());

        IssuanceMessage message = owner.issuanceStep(imab, store.getEncrypt());

        IssuanceSigma hello2 = new IssuanceSigma();
        hello2.setTestNet(true);
        hello2.setHello(hello.getHello());
        hello2.setIm(Parser.parseIssuanceMessage(message));

        response = client.basicPost(registerUrl.toString(), JaxbHelper.serializeToJson(hello2, IssuanceSigma.class));
        IssuanceSigma in2 = JaxbHelper.jsonToClass(response, IssuanceSigma.class);
        in2.setTestNet(true);
        in2.setSybilClass(sybilClass);

        IssuanceMessageAndBoolean imab2 = Parser.parseIssuanceMessageAndBoolean(in2.getImab());
        owner.issuanceStep(imab2, store.getEncrypt());

        return Parser.parseIssuanceResult(in2);

    }

    private static NetworkMapItemAdvocate targetSybil(NetworkMapItemSource sybilSourceTarget, NetworkMap map, boolean mainNet) throws Exception {
        List<URI> advocates = sybilSourceTarget.getAdvocatesForSource();
        for (URI advocateUid : advocates){
            if (mainNet && (advocateUid.toString().contains("main"))){
                return (NetworkMapItemAdvocate) map.nmiForNode(advocateUid);

            } else if ((advocateUid.toString().contains("test"))) {
                return (NetworkMapItemAdvocate) map.nmiForNode(advocateUid);

            }
        }
        throw new UxException(ErrorMessages.SYBIL_WARN + ":main-net=" + mainNet);
    }

    private static NetworkMapItemSource openSybilSource(String rulebookId, NetworkMap map) throws Exception {
        List<String> sources = map.getSourceFilenamesForRulebook(rulebookId);
        return (NetworkMapItemSource) map.nmiForNode(map.fromNmiFilename(sources.get(0)));
    }

    private static NetworkMap openNetworkMap(Path path) throws Exception {
        NetworkMap map = new NetworkMap(path);
        if (!map.networkMapExists()) {
            map.spawn();
        }
        return map;

    }
}
