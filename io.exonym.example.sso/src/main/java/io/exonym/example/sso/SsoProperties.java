package io.exonym.example.sso;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.ibm.zurich.idmx.exception.SerializationException;
import com.ibm.zurich.idmx.jaxb.JaxbHelperClass;
import eu.abc4trust.xml.SystemParameters;
import io.exonym.lib.api.SsoConfigWrapper;
import io.exonym.lib.exceptions.UxException;
import io.exonym.lib.pojo.SsoConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public class SsoProperties {

    private static SsoProperties instance;

    private SsoConfiguration basic;
    private SsoConfiguration sybil;
    private SsoConfiguration rulebooks;

    private URI myDomain = URI.create("http://exonym-x-03:20001");

    public static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public SsoProperties() {
        // The authentication request will be sent to domain provided.
        SsoConfigWrapper basic = new SsoConfigWrapper(URI.create(myDomain + "/entry"));
        this.basic = basic.getConfig();

        // require the user to have executed one-time onboarding.
        SsoConfigWrapper sybil = new SsoConfigWrapper(URI.create(myDomain + "/no-clones"));
        sybil.requireSybil(true);
        this.sybil = sybil.getConfig();

        // you can require as many rulebooks as you need.
        // all rulebooks require the use to have onboarded to Sybil.
        SsoConfigWrapper rulebooks = new SsoConfigWrapper(URI.create(myDomain + "/accountability-required"));

        rulebooks.requireRulebook(URI.create("urn:rulebook:69bb840695e4fd79a00577de5f0071b311bbd8600430f6d0da8f865c5c459d44"));
        // You can black list specific advocates or sources.
        rulebooks.addAdvocateToBlacklist(URI.create("urn:rulebook:exosources:raised:69bb840695e4fd79a00577de5f0071b311bbd8600430f6d0da8f865c5c459d44"));
        this.rulebooks=rulebooks.getConfig();

    }

    public SsoConfiguration getBasic() {
        return basic;
    }

    public SsoConfiguration getSybil() {
        return sybil;
    }

    public SsoConfiguration getRulebooks() {
        return rulebooks;
    }

    static {
        instance = new SsoProperties();

    }

    public static SsoProperties getInstance() {
        return instance;
    }
    
    private final static Logger logger = Logger.getLogger(SsoProperties.class.getName());

    public static void main(String[] args) throws UxException, SerializationException, IOException {
        try(InputStream stream = ClassLoader.getSystemResourceAsStream("lambda.xml")){
            if (stream!=null){
                byte[] in = new byte[stream.available()];
                stream.read(in);
                SystemParameters p = (SystemParameters) JaxbHelperClass.deserialize(new String(in, StandardCharsets.UTF_8)).getValue();
                logger.info(p.getSystemParametersUID().toString());

            } else {
                throw new UxException("SYSTEM_PARAMETERS_NOT_FOUND_ON_CLASS_PATH",
                        "Unable to load system parameters", "lambda.xml");

            }
        } catch (Exception e){
            throw e;

        }
    }
}
