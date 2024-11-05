package io.exonym.lib.wallet;

import com.google.gson.JsonObject;
import io.exonym.lib.abc.util.JaxbHelper;
import io.exonym.lib.exceptions.ErrorMessages;
import io.exonym.lib.exceptions.UxException;
import io.exonym.lib.lite.Http;
import io.exonym.lib.pojo.IdContainerSchema;
import io.exonym.lib.pojo.XKey;
import io.exonym.lib.standard.AsymStoreKey;
import io.exonym.lib.standard.CryptoUtils;
import io.exonym.lib.standard.Form;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

public class C30Utils {
    
    private final static Logger logger = Logger.getLogger(C30Utils.class.getName());

    // returns a challenge.
    protected static String getChallenge(String alpha, String beta, String gamma, Path rootPath) throws Exception {
        String epsilon = computeEpsilon(alpha, beta);
        // hasSybil=false --> join
        // hasMod=false --> join
        // getChallenge from game

        return null;

    }

    protected static String proveAnon(String alpha, String beta, String gamma, Path rootPath) throws Exception {
        return null;
    }

    public static XKey getPlayerKey(Path rootPath, String alpha, String beta) throws Exception {
        String epsilon = computeEpsilon(alpha, beta);
        Path pathKey = pathToKey(rootPath, epsilon);
        XKey key = null;
        if (!Files.exists(pathKey)){ // If the key doesn't exist, set one up.
            key = generateKeyForGamma(pathKey, epsilon);

        } else {
            String keyJson = Files.readString(pathKey);
            key = JaxbHelper.gson.fromJson(keyJson, XKey.class);

        }
        return key;

    }

    public static String getPlayerPublicKey(Path rootPath, String alpha, String beta) throws Exception {
        String epsilon = computeEpsilon(alpha, beta);
        Path pathKey = pathToKey(rootPath, epsilon);
        XKey key = null;
        if (!Files.exists(pathKey)){ // If the key doesn't exist, set one up.
            key = generateKeyForGamma(pathKey, epsilon);

        } else {
            String keyJson = Files.readString(pathKey);
            key = JaxbHelper.gson.fromJson(keyJson, XKey.class);

        }
        return Form.toHex(key.getPublicKey());

    }


    protected static Path pathToKey(Path rootPath, String epsilon){
        return Path.of(rootPath.toString(), "containers", epsilon + "-key.json");

    }

    protected static String computeEpsilon(String alpha, String beta) {
        return CryptoUtils.computeMd5HashAsHex((alpha + beta).getBytes(StandardCharsets.UTF_8));
    }

    protected static XKey generateKeyForGamma(Path pathToKey, String epsilon) throws Exception {
        XKey key = XKey.createNew(epsilon);
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

    protected static boolean c30Update(String alpha, String beta, String gamma, URI c30Host,
                                       Http client, IdContainerSchema id, AsymStoreKey key) throws UxException {
        try {
            // c30/epsilon?container+gamma+sig(hash(container))
            String container = JaxbHelper.gson.toJson(id);
            String endpoint = c30Host + "c30/";
            String epsilon = CryptoUtils.computeMd5HashAsHex((alpha + beta)
                    .getBytes(StandardCharsets.UTF_8));

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("gamma", gamma);
            jsonObject.addProperty("container", container);
            jsonObject.addProperty("sig", Base64.encodeBase64String(
                    key.sign(CryptoUtils.computeSha256HashAsBytes(
                            container.getBytes(StandardCharsets.UTF_8)))));

            String target = endpoint + epsilon;
            logger.info("TargetUpdate=" + target);
            String response = client.basicPost(target, jsonObject.toString());
            return response.equals("{\"success\":true}");

        } catch (Exception e) {

            throw new UxException(ErrorMessages.FAILED_TO_AUTHORIZE, e);

        }
    }

    protected static IdContainerSchema c30Get(String alpha, String beta,
                                              String gamma, URI c30Host,
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
            throw new RuntimeException(e);

        }
    }

    private static String pathForGamma(String alpha,String beta,String gamma, String nonce, AsymStoreKey k) throws Exception {
        byte[] sig = k.encryptWithPrivateKey(nonce.getBytes(StandardCharsets.UTF_8));
        String hex = Hex.encodeHexString(sig);
        return alpha + "/" + beta + "/" + gamma + "/" + hex;

    }

}
