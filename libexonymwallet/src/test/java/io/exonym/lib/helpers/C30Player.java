package io.exonym.lib.helpers;

import io.exonym.lib.pojo.XKey;
import io.exonym.lib.standard.AsymStoreKey;
import io.exonym.lib.standard.CryptoUtils;
import io.exonym.lib.standard.Form;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Logger;

public class C30Player {

    String alpha, beta, epsilon;

    HashMap<String, XKey> gammaToKey = new HashMap<>();
    
    private final static Logger logger = Logger.getLogger(C30Player.class.getName());

    public static C30Player init() {
        C30Player u = new C30Player();
        u.setAlpha(UUID.randomUUID().toString());
        u.setBeta(UUID.randomUUID().toString());
        u.setEpsilon(indexPlayer(u));
        return u;

    }

    public static C30Player init(String alpha) {
        C30Player u = new C30Player();
        u.setAlpha(alpha);
        u.setBeta(UUID.randomUUID().toString());
        return u;

    }

    public void keyForGame(String gamma, String password) throws Exception {
        XKey xk = XKey.createNew(password);
        gammaToKey.put(gamma, xk);
    }


    public String getAlpha() {
        return alpha;
    }

    public void setAlpha(String alpha) {
        this.alpha = alpha;
    }

    public String getBeta() {
        return beta;
    }

    public void setBeta(String beta) {
        this.beta = beta;
    }

    public String getEpsilon() {
        return epsilon;
    }

    public void setEpsilon(String epsilon) {
        this.epsilon = epsilon;
    }

    public HashMap<String, XKey> getGammaToKey() {
        return gammaToKey;
    }

    public void setGammaToKey(HashMap<String, XKey> gammaToKey) {
        this.gammaToKey = gammaToKey;
    }

    public String pathForGamma(String gamma, String nonce, String password) throws Exception {
        XKey key = this.getGammaToKey().get(gamma);
        AsymStoreKey k = XKey.assembleAsym(password, key);
        return pathForGamma(gamma, nonce, k);

    }
    public String pathForGamma(String gamma, String nonce, AsymStoreKey k) throws Exception {
        byte[] sig = k.encryptWithPrivateKey(nonce.getBytes(StandardCharsets.UTF_8));
        String hex = Hex.encodeHexString(sig);
        return this.alpha + "/" +this.beta + "/" + gamma + "/" + hex;

    }

    public static String indexPlayer(C30Player player){
        return CryptoUtils.computeMd5HashAsHex(
                (player.getAlpha() + player.getBeta())
                        .getBytes(StandardCharsets.UTF_8));

    }



}
