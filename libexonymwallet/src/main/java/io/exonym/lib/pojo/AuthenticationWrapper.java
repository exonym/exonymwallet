package io.exonym.lib.pojo;

import io.exonym.lib.abc.util.JaxbHelper;
import io.exonym.lib.standard.QrCode;
import io.exonym.lib.wallet.WalletUtils;
import org.apache.commons.codec.binary.Base64;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public class AuthenticationWrapper<T> {

    private T challenge;
    private String qrPngB64;
    private String link;

    public T getChallenge() {
        return challenge;
    }

    public void setChallenge(T challenge) {
        this.challenge = challenge;
    }

    public String getQrPngB64() {
        return qrPngB64;
    }

    public void setQrPngB64(String qrPngB64) {
        this.qrPngB64 = qrPngB64;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public static <Q extends ExonymChallenge> String  wrap(Q challenge, int qrPixels, Class<?> clazz) throws Exception {
        AuthenticationWrapper wrapper = new AuthenticationWrapper<Q>();
        wrapper.setChallenge(challenge);
        wrapper.setLink(computeUniversalLink(challenge, clazz));
        wrapper.setQrPngB64(QrCode.computeQrCodeAsPngB64(wrapper.getLink(), qrPixels));
        return JaxbHelper.gson.toJson(wrapper, AuthenticationWrapper.class);

    }

    public static <Q extends ExonymChallenge> AuthenticationWrapper<Q>  wrapToWrapper(Q challenge, int qrPixels, Class<?> clazz) throws Exception {
        AuthenticationWrapper wrapper = new AuthenticationWrapper<Q>();
        wrapper.setChallenge(challenge);
        wrapper.setLink(computeUniversalLink(challenge, clazz));
        wrapper.setQrPngB64(QrCode.computeQrCodeAsPngB64(wrapper.getLink(), qrPixels));
        return wrapper;

    }

    private static <Q extends ExonymChallenge> String computeUniversalLink(Q challenge, Class<?> clazz) throws IOException {
        byte[] preCompressed = JaxbHelper.gson.toJson(challenge, clazz)
                .getBytes(StandardCharsets.UTF_8);
        byte[] compressed = WalletUtils.compress(preCompressed);
        String linkContent = Base64.encodeBase64String(compressed);
        return challenge.universalLinkPrefix() + linkContent;


    }
    
    private final static Logger logger = Logger.getLogger(AuthenticationWrapper.class.getName());

    public static <Q extends ExonymChallenge> Q unwrapFromUniversalLink(String ulink, Class<Q> clazz) throws Exception {
        String str = WalletUtils.isolateUniversalLinkContent(ulink);
        byte[] b64 = Base64.decodeBase64(str);
        byte[] decompressed = WalletUtils.decompress(b64);
        String json = new String(decompressed, StandardCharsets.UTF_8);
        return JaxbHelper.gson.fromJson(json, clazz);

    }

}
