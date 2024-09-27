package io.exonym.lib.wallet;

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
import io.exonym.lib.standard.PassStore;

import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public class SybilOnboarding {
    
    private final static Logger logger = Logger.getLogger(SybilOnboarding.class.getName());

    private final static String sybilUrl = "http://exonym-x-03:8079/";
//    private final static String sybilUrl = "https://t0.sybil.exonym.io/";

    public static String testNet(PassStore store, Path rootPath, String sybilClass) throws Exception {

        Cache cache = new Cache(rootPath);
        NetworkMap map = openNetworkMap(rootPath.resolve("network-map"));
        PkiExternalResourceContainer external = PkiExternalResourceContainer.getInstance();
        external.setNetworkMapAndCache(map, cache);

        URI sybilRulebookID = Rulebook.SYBIL_RULEBOOK_UID_TEST;
        URI sybilCSpecUID = URI.create(Rulebook.SYBIL_RULEBOOK_UID_TEST + ":c");
        CredentialSpecification cSpec = external.openResource(IdContainerJSON.uidToXmlFileName(sybilCSpecUID));

        NetworkMapItemLead sybilLeadTarget = openSybilLead(sybilRulebookID.toString(), map);
        NetworkMapItemModerator testNetTarget = targetSybil(sybilLeadTarget, map, false);
        NodeVerifier verifiedAdvocate = NodeVerifier.openNode(testNetTarget.getStaticURL0(), false, false);
        UIDHelper helper = verifiedAdvocate.getUidHelperForMostRecentIssuerParameters();

        IdContainerJSON x = new IdContainerJSON(ExonymToolset.pathToContainers(rootPath),
                store.getUsername());

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
//        String target = (testNetTarget.getRulebookNodeURL().toString() + "register")
//                .replaceAll("node.", "");

        String target = (sybilUrl + "register");

        logger.info(">>>>>>>>> " + target);

        String response = client.basicPost(target,
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

        String responseB = client.basicPost(target, JaxbHelper.serializeToJson(hello2, IssuanceSigma.class));

        IssuanceSigma in2 = JaxbHelper.jsonToClass(responseB, IssuanceSigma.class);
        WalletUtils.rejectOnError(in2);

        in2.setTestNet(true);
        in2.setSybilClass(sybilClass);

        IssuanceMessageAndBoolean imab2 = Parser.parseIssuanceMessageAndBoolean(in2.getImab());
        owner.issuanceStep(imab2, store.getEncrypt());

        return Parser.parseIssuanceResult(in2);

    }


    private static NetworkMapItemModerator targetSybil(NetworkMapItemLead sybilLeadTarget, NetworkMap map, boolean mainNet) throws Exception {
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
