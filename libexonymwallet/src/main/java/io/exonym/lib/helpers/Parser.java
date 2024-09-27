package io.exonym.lib.helpers;

import com.ibm.zurich.idmx.exception.SerializationException;
import com.ibm.zurich.idmx.jaxb.JaxbHelperClass;

import eu.abc4trust.xml.*;
import io.exonym.lib.exceptions.ErrorMessages;
import io.exonym.lib.exceptions.UxException;
import io.exonym.lib.pojo.IdContainer;
import io.exonym.lib.pojo.IssuanceSigma;
import io.exonym.lib.abc.util.JaxbHelper;
import org.apache.commons.codec.binary.Base64;
import org.graalvm.collections.EconomicMap;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Parser {

    public static IssuanceMessageAndBoolean parseIssuanceMessageAndBoolean(String imabB64) throws SerializationException, IOException {
        String imabString = new String(Base64.decodeBase64(imabB64));
        return (IssuanceMessageAndBoolean) JaxbHelperClass.deserialize(imabString).getValue();

    }

    public static PresentationToken parsePresentationTokenFromXml(String ptXml) throws Exception {
        if (isXml(ptXml)){
            return (PresentationToken) JaxbHelperClass.deserialize(ptXml).getValue();

        } else {
            throw new UxException(ErrorMessages.TOKEN_INVALID, "The token was not XML");

        }
    }

    private static boolean isXml(String xml) {
        return xml.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
    }


    public static PresentationToken parsePresentationToken(String ptB64) throws Exception {
        String ptString = new String(Base64.decodeBase64(ptB64));
        return parsePresentationTokenFromXml(ptString);

    }

    public static String parseIssuanceMessage(IssuanceMessage im) throws Exception {
        String xmlResponse = IdContainer.convertObjectToXml(im);
        return Base64.encodeBase64String(xmlResponse.getBytes(StandardCharsets.UTF_8));

    }

    public static String parseIssuanceResult(IssuanceSigma issuanceResult) throws Exception {
        issuanceResult.setHello(null);
        issuanceResult.setIm(null);
        issuanceResult.setImab(null);
        return JaxbHelper.gson.toJson(issuanceResult, IssuanceSigma.class);

    }

    public static PseudonymInPolicy nymInTokenToPolicy(PseudonymInToken pit){
        PseudonymInPolicy pip = new PseudonymInPolicy();
        pip.setExclusive(pit.isExclusive());
        pip.setScope(pit.getScope());
        pip.setAlias(pit.getAlias());
        pip.setSameKeyBindingAs(pit.getSameKeyBindingAs());
        pip.setPseudonymValue(pit.getPseudonymValue());
        return pip;

    }

    // todo review
    public static CredentialInPolicy credentialInTokenToPolicy(CredentialInToken cit){
        CredentialInPolicy cip = new CredentialInPolicy();
        cip.setAlias(cit.getAlias());
        cip.setSameKeyBindingAs(cit.getSameKeyBindingAs());
        CredentialInPolicy.CredentialSpecAlternatives ca = new CredentialInPolicy.CredentialSpecAlternatives();
        ca.getCredentialSpecUID().add(cit.getCredentialSpecUID());
        cip.setCredentialSpecAlternatives(ca);

        CredentialInPolicy.IssuerAlternatives ia = new CredentialInPolicy.IssuerAlternatives();
        cip.setIssuerAlternatives(ia);
        CredentialInPolicy.IssuerAlternatives.IssuerParametersUID ip =
                UIDHelper.computeIssuerParametersUID(cit.getIssuerParametersUID());
        ia.getIssuerParametersUID().add(ip);
        return cip;

    }
}
