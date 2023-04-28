package io.exonym.lib.wallet;

import com.ibm.zurich.idmx.exception.SerializationException;
import com.ibm.zurich.idmx.jaxb.JaxbHelperClass;
import eu.abc4trust.xml.PresentationPolicy;
import eu.abc4trust.xml.PresentationPolicyAlternatives;
import eu.abc4trust.xml.PresentationToken;
import eu.abc4trust.xml.PseudonymInToken;
import io.exonym.lib.helpers.UIDHelper;
import io.exonym.lib.pojo.IssuanceSigma;
import io.exonym.lib.pojo.Namespace;
import io.exonym.lib.pojo.NetworkMapItemAdvocate;
import io.exonym.lib.helpers.UrlHelper;
import io.exonym.lib.exceptions.ErrorMessages;
import io.exonym.lib.exceptions.UxException;
import io.exonym.lib.standard.CryptoUtils;
import io.exonym.lib.standard.Form;
import io.exonym.lib.standard.Morph;
import org.apache.commons.codec.binary.Base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterOutputStream;

public class WalletUtils {
    
    private final static Logger logger = Logger.getLogger(WalletUtils.class.getName());

    protected static <T> T deserialize(String object) throws UxException {
        if (UrlHelper.isXml(object.getBytes(StandardCharsets.UTF_8))){
            try {
                return (T) JaxbHelperClass.deserialize(object).getValue();

            } catch (SerializationException e) {
                throw new UxException(ErrorMessages.TOKEN_INVALID, e,
                        "The presentation policy was XML, but it was not an Issuance Policy");

            }
        } else { // base64
            try {
                byte[] decodedB64 = Base64.decodeBase64(object);
                String xml = new String(decodedB64, StandardCharsets.UTF_8);
                return (T) JaxbHelperClass.deserialize(xml).getValue();

            } catch (Exception e) {
                throw new UxException(ErrorMessages.TOKEN_INVALID, e,
                        "The Issuance Policy was neither XML nor Base64 encoded XML");

            }
        }
    }

    protected static void rejectOnError(IssuanceSigma in) throws UxException {
        if (in.getError()!=null){
            String[] errors = new String[in.getInfo().size()];
            for (int i=0; i<errors.length; i++){
                errors[i] = in.getInfo().get(i);

            }
            throw new UxException(in.getError(), errors);

        }
    }


    protected static NetworkMapItemAdvocate determinedSearchForAdvocate(Path path, URI advocateUID) throws Exception {
        NetworkMap map = new NetworkMap(path.resolve("network-map"));
        return determinedSearchForAdvocate(map, advocateUID);

    }

    protected static NetworkMapItemAdvocate determinedSearchForAdvocate(NetworkMap map, URI advocateUID) throws Exception {
        try {
            return (NetworkMapItemAdvocate) map.nmiForNode(advocateUID);

        } catch (Exception e) {
            try {
                map.spawn();
                return (NetworkMapItemAdvocate) map.nmiForNode(advocateUID);

            } catch (Exception ex) {
                throw new UxException(ErrorMessages.ADVOCATE_NOT_FOUND_ON_NETWORK_MAP, ex,
                        "Also attempted real-time refresh - the advocate is unknown");

            }
        }
    }

    public static PresentationPolicyAlternatives openPPA(PresentationPolicy policy) throws UxException {
        PresentationPolicyAlternatives ppa = new PresentationPolicyAlternatives();
        ppa.getPresentationPolicy().add(policy);
        return ppa;

    }

    public static String decodeCompressedB64(String b64) throws IOException {
        byte[] decom = decompress(
                Base64.decodeBase64(
                b64.getBytes(StandardCharsets.UTF_8)));
        return new String(decom, StandardCharsets.UTF_8);
    }



    public static String decodeUncompressedB64(String b64) throws IOException {
        return new String(Base64.decodeBase64(b64), StandardCharsets.UTF_8);
    }

    public HashMap<String, UIDHelper> populateHelpers(ArrayList<String> issuerUids) throws Exception {
        HashMap<String, UIDHelper> helpers = new HashMap<>();
        for (String issuer : issuerUids){
            helpers.put(issuer, new UIDHelper(issuer));

        }
        return helpers;
    }


    public static PresentationPolicyAlternatives openPPA(String policy) throws Exception {
        Object obj = idmxFromUniversalLink(policy);
        if (obj instanceof PresentationPolicyAlternatives){
            return (PresentationPolicyAlternatives) obj;

        } else if (obj instanceof PresentationPolicy){
            PresentationPolicyAlternatives ppa = new PresentationPolicyAlternatives();
            ppa.getPresentationPolicy().add((PresentationPolicy) obj);
            return ppa;

        } else {
            throw new UxException(ErrorMessages.TOKEN_INVALID, "The request from the service was malformed");

        }
    }

    public static String isolateUniversalLinkContent(String uLink) throws Exception {
        if (uLink!=null){
            String[] parts = uLink.split("\\?");
            if (parts.length==2){
                return parts[1];

            } else if (parts.length==1){
                return parts[0];

            }
        }
        throw new UxException(ErrorMessages.UNEXPECTED_PSEUDONYM_REQUEST,
                "Blind-sided by the universal link");

    }


    public static <T> ArrayList<T> wrapInList(T item){
        ArrayList<T> list = new ArrayList<>();
        list.add(item);
        return list;
    }

    public static <T> ArrayList<T> emptyList(){
        return new ArrayList<>();
    }

    public static <T> String idmxToUniversalLink(String U_LINK_PREFIX_FROM_NAMESPACE, T t) throws Exception {
        assert t!=null;
        Morph<T> morph = new Morph<>();
        byte[] s0 = morph.toByteArray(t);
        byte[] s1 = WalletUtils.compress(s0);
        String b64 = Base64.encodeBase64String(s1);
        return U_LINK_PREFIX_FROM_NAMESPACE  + b64;

//        String xml0 = XContainer.convertObjectToXml(o);
//        xml0 = xml0.replaceAll("\\n", "");
//        xml0 = xml0.replaceAll(">\\s*<", "><");
//        byte[] xml = xml0.getBytes(StandardCharsets.UTF_8);
//        byte[] compressed = compress(xml);

    }

    public static ArrayList<URI> extractPseudonyms(PresentationToken verifiedToken) throws UxException {
        List<PseudonymInToken> nyms = verifiedToken.getPresentationTokenDescription().getPseudonym();
        ArrayList<URI> result = new ArrayList<>();
        for (PseudonymInToken nym : nyms){
            if (nym.isExclusive()){
                URI endonym = endonymForm(nym.getScope(), nym.getPseudonymValue());
                result.add(endonym);

            }
        }
        return result;
    }

    public static URI endonymForm(String scope, byte[] fullValue) throws UxException {
        if (scope==null || fullValue==null){
            throw new UxException(ErrorMessages.UNEXPECTED_PSEUDONYM_REQUEST);

        }
        String n6 = Form.toHex(fullValue).substring(0,6);
        String shortValue = CryptoUtils.computeSha256HashAsHex(fullValue);
        String prefix = CryptoUtils.computeSha256HashAsHex(scope);
        URI result = URI.create(Namespace.ENDONYM_PREFIX +
                prefix.substring(32) + ":" +
                n6 + "-" +
                shortValue);
        logger.info(result.toString());
        return result;

    }

    public static <T> T idmxFromUniversalLink(String b64) throws IOException {
        Morph<T> morph = new Morph<>();
        byte[] decoded = Base64.decodeBase64(b64);
        byte[] decompressed = decompress(decoded);
        return morph.construct(decompressed);

    }

    public static byte[] compress(byte[] in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DeflaterOutputStream defl = new DeflaterOutputStream(out);
        defl.write(in);
        defl.finish();
        defl.flush();
        defl.close();
        return out.toByteArray();

    }

    public static byte[] decompress(byte[] in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        InflaterOutputStream infl = new InflaterOutputStream(out);
        infl.write(in);
        infl.flush();
        infl.close();

        return out.toByteArray();

    }

}

