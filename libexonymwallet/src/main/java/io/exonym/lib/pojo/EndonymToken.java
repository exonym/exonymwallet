package io.exonym.lib.pojo;

import eu.abc4trust.xml.PresentationToken;
import io.exonym.lib.exceptions.ErrorMessages;
import io.exonym.lib.exceptions.UxException;
import io.exonym.lib.helpers.UIDHelper;
import io.exonym.lib.standard.CryptoUtils;
import io.exonym.lib.standard.Form;
import io.exonym.lib.wallet.WalletUtils;

import java.net.URI;
import java.nio.charset.StandardCharsets;

public class EndonymToken {

    private URI endonym;
    private byte[] compressedPresentationToken;
    private URI moderatorUid;

    private String error;

    private boolean timeout = false;


    public static EndonymToken build(URI endonym, PresentationToken pt) throws UxException {
        try {
            EndonymToken result = new EndonymToken();
            result.setCompressedPresentationToken(pt);
            result.setEndonym(endonym);
            return result;

        } catch (Exception e) {
            throw new UxException(ErrorMessages.TOKEN_INVALID + ":Forward Serialization Error");

        }
    }

    public URI getEndonym() {
        return endonym;
    }

    public void setEndonym(URI endonym) {
        this.endonym = endonym;
    }

    public byte[] getCompressedPresentationToken() {
        return compressedPresentationToken;
    }

    public void setCompressedPresentationToken(PresentationToken pt) {
        try {
            String xml = IdContainer.convertObjectToXml(pt);
            this.compressedPresentationToken = WalletUtils.compress(
                    xml.getBytes(StandardCharsets.UTF_8));

        } catch (Exception e) {
            throw new RuntimeException(e);

        }
    }

    public URI getModeratorUid() {
        return moderatorUid;
    }

    public void setModeratorUid(URI moderatorUid) {
        this.moderatorUid = moderatorUid;
    }

    public boolean isTimeout() {
        return timeout;
    }

    public void setTimeout(boolean timeout) {
        this.timeout = timeout;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public boolean hasError(){
        return error!=null;
    }

    public String computeIndex() throws Exception {
        if (moderatorUid!=null){
            String r = UIDHelper.computeShortRulebookHashUid(moderatorUid);
            String fn = IdContainer.uidToFileName(endonym);

            String modName = UIDHelper.computeModNameFromModUid(moderatorUid);
            String leadName = UIDHelper.computeLeadNameFromModOrLeadUid(moderatorUid);

            fn = fn.replace("urn", leadName);
            fn = fn.replace("endonym", modName);

            StringBuilder builder = new StringBuilder();
            builder.append(r)
                    .append(".")
                    .append(fn);
            return builder.toString();

        } else {
            return IdContainer.uidToFileName(endonym).replace("urn.", "");

        }
    }


    public static URI endonymForm(String scope, byte[] fullValue) throws UxException {
        if (scope==null || fullValue==null){
            throw new UxException(ErrorMessages.UNEXPECTED_PSEUDONYM_REQUEST);

        }
        // urn:exonym:<scope-representation>:<nibble6>-<pseudonym-hash>
        String n6 = Form.toHex(fullValue).substring(0,6);
        String shortValue = CryptoUtils.computeSha256HashAsHex(fullValue);
        String prefix = CryptoUtils.computeSha256HashAsHex(scope);
        URI result = URI.create(Namespace.ENDONYM_PREFIX +
                prefix.substring(32) + ":" +
                n6 + "-" +
                shortValue);
        return result;

    }

}
