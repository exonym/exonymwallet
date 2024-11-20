package io.exonym.lib.wallet;

import com.google.gson.JsonObject;
import io.exonym.lib.abc.util.JaxbHelper;
import io.exonym.lib.exceptions.ErrorMessages;
import io.exonym.lib.exceptions.UxException;
import io.exonym.lib.helpers.Timing;
import io.exonym.lib.lite.Http;
import io.exonym.lib.pojo.IdContainerSchema;
import io.exonym.lib.pojo.NetworkMapItemModerator;
import io.exonym.lib.pojo.XKey;
import io.exonym.lib.standard.AsymStoreKey;
import io.exonym.lib.standard.CryptoUtils;
import io.exonym.lib.standard.Form;
import io.exonym.lib.standard.PassStore;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.logging.Logger;

public class C30Utils {
    
    private final static Logger logger = Logger.getLogger(C30Utils.class.getName());

    public static final int STOPPER_NONE = 0;
    public static final int STOPPER_SYBIL = 1;
    public static final int STOPPER_RULEBOOK = 2;

    protected static String joinToAuthProtocol(String alpha, String beta, String gamma,
                                               Path rootPath, boolean test) throws Exception {
        return joinToAuthProtocol(alpha, beta, gamma, rootPath, test, 0);

    }

    // returns a challenge.
    protected static String joinToAuthProtocol(String alpha, String beta, String gamma,
                                               Path rootPath, boolean test, int stopper) throws Exception {
        String epsilon = computeEpsilon(alpha, beta);
        if (hasPlayerKeyForGame(alpha, beta, rootPath)){
            Http client = new Http();
            XKey keyXml = openKey(alpha, beta, rootPath);
            AsymStoreKey playerKeyForGame = XKey.assembleAsym(epsilon, keyXml);
            // TODO
            String c30Host = test ? "https://t1.sybil.cyber30.io/" : "https://t1.sybil.cyber30.io/" ;
            IdContainerSchema schema = c30Get(alpha, beta, gamma, c30Host, client, playerKeyForGame);
            HashMap<String, String> ss = schema.getOwnerSecretStore();

            PassStore store = new PassStore(epsilon, false);
            store.setUsername(epsilon);
            ExonymToolset toolset = new ExonymToolset(store, rootPath, schema);

            if (ss.size() > 2){ // prove
                return gameOnboarding(alpha, beta, gamma, rootPath,
                        c30Host, schema, client, playerKeyForGame, toolset);

            } else if (ss.size()==2){ // join rulebook -> prove
                return rulebookOnboarding(alpha, beta, gamma, rootPath, test,
                        c30Host, client, playerKeyForGame, toolset);

            } else { // join sybil --> join rulebook --> prove
                return fullOnboarding(alpha, beta, gamma, rootPath, test,
                        c30Host, schema, client, playerKeyForGame, toolset, stopper);

            }
        } else {
            throw new UxException("NO_PLAYER_KEY_FOR_GAME");

        }
    }

    private static String fullOnboarding(String alpha, String beta, String gamma, Path rootPath, boolean test,
                                         String c30Host, IdContainerSchema schema, Http client,
                                         AsymStoreKey key, ExonymToolset toolset, int stopper) throws Exception {
        URI host = URI.create(c30Host);

        if (test){
            schema = SybilOnboarding.c30TestNet(schema, rootPath, host, client,
                    key, alpha, beta, gamma);

        } else {
            schema = SybilOnboarding.c30MainNet(schema, rootPath, host, client,
                    key, alpha, beta, gamma);

        }

        String modUid = c30Update(alpha, beta, gamma, host, client, schema, key);
        if (stopper==STOPPER_SYBIL){
            return "stopped after sybil";
        }

        URI mod = URI.create(modUid);
        PassStore store = toolset.getStore();
        RulebookOnboarding.onboardRulebook(
                store, rootPath, mod, client);

        schema = c30SchemaFromDisk(rootPath, store.getUsername());

        c30Update(alpha, beta, gamma, host, client, schema, key);

        if (stopper==STOPPER_RULEBOOK){
            return "stopped after rulebook";
        }

        NetworkMap map = toolset.getNetworkMap();
        NetworkMapItemModerator nmim = (NetworkMapItemModerator) map.nmiForNode(mod);

        String link = getChallengeLinkToFill(alpha, beta, key, nmim, client);

        return proveAnon(alpha, beta, gamma, link, rootPath, schema, nmim, client);

    }

    private static String rulebookOnboarding(String alpha, String beta, String gamma, Path rootPath, boolean test,
                                             String c30Host, Http client,
                                             AsymStoreKey key, ExonymToolset toolset) throws Exception {
        String target = c30Host + "c30/" + gamma;
        String modUid = client.basicGet(target);
        URI mod = URI.create(modUid);
        logger.info("Found moderator:" + mod);
        PassStore store = toolset.getStore();
        String result = RulebookOnboarding.onboardRulebook(store, rootPath, mod, client);
        logger.info(result);
        IdContainerSchema schema = c30SchemaFromDisk(rootPath, store.getUsername());

        c30Update(alpha, beta, gamma, URI.create(c30Host), client, schema, key);
        logger.info(modUid);

        NetworkMap map = toolset.getNetworkMap();
        NetworkMapItemModerator nmim = (NetworkMapItemModerator) map.nmiForNode(mod);

        String link = getChallengeLinkToFill(alpha, beta, key, nmim, client);

        return proveAnon(alpha, beta, gamma, link, rootPath, schema, nmim, client);


    }

    public static String gameOnboarding(String alpha, String beta, String gamma, Path rootPath,
                                         String c30Host, IdContainerSchema schema, Http client,
                                         AsymStoreKey key, ExonymToolset toolset) throws Exception {
        // todo MOVE TO OWN FUNCTION
        String target = c30Host + "c30/" + gamma;
        String modUid = client.basicGet(target);
        logger.info(modUid);

        NetworkMap map = toolset.getNetworkMap();
        NetworkMapItemModerator nmim = (NetworkMapItemModerator) map.nmiForNode(URI.create(modUid));

        String link = getChallengeLinkToFill(alpha, beta, key, nmim, client);

        return proveAnon(alpha, beta, gamma, link, rootPath, schema, nmim, client);

    }


    protected static String proveAnon(String alpha, String beta, String gamma,
                                      String link, Path rootPath,
                                      IdContainerSchema schema,
                                      NetworkMapItemModerator mod,
                                      Http client) throws Exception {

        String epsilon = CryptoUtils.computeMd5HashAsHex(
                (alpha + beta).getBytes(StandardCharsets.UTF_8));

        PassStore store = new PassStore(epsilon, false);
        store.setUsername(epsilon);

        Prove prove = new Prove(store, rootPath, schema);
        String xml = prove.proofForRulebookSSOAnon(link);

        if (xml.startsWith("{")){
            return xml;

        } else {
            StringBuilder url = new StringBuilder();
            url.append(mod.getRulebookNodeURL());
            url.append("auth-status/");
            url.append(epsilon);
            return client.basicPost(url.toString(), xml);

        }
    }

    public static XKey openKey(String alpha, String beta, Path rootPath) throws Exception {
        String epsilon = computeEpsilon(alpha, beta);
        Path pathKey = pathToKey(rootPath, epsilon);
        String keyJson = Files.readString(pathKey);
        XKey key = JaxbHelper.gson.fromJson(keyJson, XKey.class);
        return key;

    }

    public static String getChallengeLinkToFill(String alpha, String beta, AsymStoreKey playerKey,
                                            NetworkMapItemModerator nmim,
                                            Http client) throws IOException {
        String t0 = "" + Timing.currentTime();
        String sig = Hex.encodeHexString(playerKey.encryptWithPrivateKey(
                t0.getBytes(StandardCharsets.UTF_8)));

        StringBuilder url = new StringBuilder();
        url.append(nmim.getRulebookNodeURL());
        url.append("auth-status/");
        url.append(CryptoUtils.computeMd5HashAsHex((alpha + beta)
                .getBytes(StandardCharsets.UTF_8)));
        url.append("/");
        url.append(sig);
        return client.basicGet(url.toString());
    }

    public static IdContainerSchema c30SchemaFromHost(String alpha, String beta, String gamma,
                                              AsymStoreKey playerKey, Http client, URI c30Sybil){

        return null;
    }

    public static String getPlayerPublicKeyAsString(Path rootPath, String alpha, String beta) throws Exception {
        String epsilon = computeEpsilon(alpha, beta);
        Path pathKey = pathToKey(rootPath, epsilon);
        String keyJson = Files.readString(pathKey);
        XKey key = JaxbHelper.gson.fromJson(keyJson, XKey.class);
        return Form.toHex(key.getPublicKey());

    }

    public static boolean hasPlayerKeyForGame(String alpha, String beta, Path rootPath) throws Exception {
        String epsilon = computeEpsilon(alpha, beta);
        Path pathKey = pathToKey(rootPath, epsilon);
        return Files.exists(pathKey);

    }

    protected static Path pathToKey(Path rootPath, String epsilon){
        return Path.of(rootPath.toString(), "containers", epsilon + "-key.json");

    }

    protected static String computeEpsilon(String alpha, String beta) {
        return CryptoUtils.computeMd5HashAsHex((alpha + beta).getBytes(StandardCharsets.UTF_8));
    }

    public static XKey generateNewPlayerKeyForGamma(Path rootPath, String alpha, String beta) throws Exception {
        String epsilon = computeEpsilon(alpha, beta);
        Path pathToKey = pathToKey(rootPath, epsilon);
        return generatePlayerKeyForGamma(pathToKey, epsilon);

    }

    private static XKey generatePlayerKeyForGamma(Path pathToKey, String epsilon) throws Exception {
        XKey key = XKey.createNew(epsilon);
        Files.createDirectories(pathToKey.getParent());
        String keyJson = JaxbHelper.gson.toJson(key);
        Files.writeString(pathToKey, keyJson);
        return key;

    }

    protected static IdContainerSchema c30SchemaFromDisk(Path pathRoot, String epsilon) throws IOException {
        Path sch = Path.of(pathRoot.toString(),
                "containers", epsilon,
                epsilon + ".json");
        String idJson = Files.readString(sch);
        return JaxbHelper.gson.fromJson(idJson, IdContainerSchema.class);

    }

    protected static String c30Update(String alpha, String beta, String gamma, URI c30Host,
                                       Http client, IdContainerSchema id, AsymStoreKey key) throws UxException {
        try {
            // c30/epsilon?container+gamma+sig(hash(container))
            String container = JaxbHelper.gson.toJson(id);
            String endpoint = c30Host + "c30/";
            String epsilon = computeEpsilon(alpha, beta);

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("gamma", gamma);
            jsonObject.addProperty("container", container);
            jsonObject.addProperty("sig", Base64.encodeBase64String(
                    key.sign(CryptoUtils.computeSha256HashAsBytes(
                            container.getBytes(StandardCharsets.UTF_8)))));

            String target = endpoint + epsilon;
            logger.info("TargetUpdate=" + target);
            String response = client.basicPost(target, jsonObject.toString());
            return response;

        } catch (Exception e) {

            throw new UxException(ErrorMessages.FAILED_TO_AUTHORIZE, e);

        }
    }

    protected static IdContainerSchema c30Get(String alpha, String beta,
                                              String gamma, String c30Host,
                                              Http client, AsymStoreKey key) throws UxException {
        try {
            String nonce = client.basicGet(c30Host + "c30/nonce");
            String path = pathForGamma(alpha, beta, gamma, nonce, key);
            String target = c30Host + "c30/"+ path;
            String container = client.basicGet(target);
            IdContainerSchema schema = JaxbHelper.gson.fromJson(container, IdContainerSchema.class);
            if (schema.getUsername()==null){
                throw new UxException(container);

            } else {
                return schema;

            }
        } catch (UxException e) {
            throw e;

        } catch (Exception e) {
            throw new UxException(e.getMessage());

        }
    }

    private static String pathForGamma(String alpha,String beta,String gamma, String nonce, AsymStoreKey k) throws Exception {
        byte[] sig = k.encryptWithPrivateKey(nonce.getBytes(StandardCharsets.UTF_8));
        String hex = Hex.encodeHexString(sig);
        return alpha + "/" + beta + "/" + gamma + "/" + hex;

    }


}
