package io.exonym.lib.api;

import com.ibm.zurich.idmx.jaxb.JaxbHelperClass;
import eu.abc4trust.smartcard.Base64;
import eu.abc4trust.xml.RevocationInformation;
import io.exonym.lib.abc.util.JaxbHelper;
import io.exonym.lib.actor.NodeVerifier;
import io.exonym.lib.exceptions.UxException;
import io.exonym.lib.helpers.UIDHelper;
import io.exonym.lib.pojo.ExoNotify;
import io.exonym.lib.pojo.RulebookAuth;
import io.exonym.lib.pojo.SsoConfiguration;
import io.exonym.lib.standard.AsymStoreKey;
import io.exonym.lib.standard.CryptoUtils;
import io.exonym.lib.standard.WhiteList;
import io.exonym.lib.wallet.ExonymOwner;
import org.eclipse.paho.client.mqttv3.*;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Logger;

public class NotificationSubscriber {
    
    private final static Logger logger = Logger.getLogger(NotificationSubscriber.class.getName());
    
    private static NotificationSubscriber instance;

    private MqttClient client = null;

    public void subscribe(SsoConfiguration ssoConfig, boolean mod, boolean lead){
        HashMap<String, RulebookAuth> rb = ssoConfig.getHonestUnder();
        for (String ruid : rb.keySet()){
            URI rulebookUid = rb.get(ruid).getRulebookUID();
            String topic = null;
            try {
                topic = UIDHelper.computeRulebookTopicFromUid(rulebookUid);

            } catch (UxException e) {
                logger.info("Error:" + e.getInfo());
            }
            topic += "/#";

            try {
                logger.info("Subscribing to topic=" + topic);
                client.subscribe(topic);

            } catch (MqttException e) {
                logger.info("Failed to subscribe to topic=" + topic);

            }
        }
    }
    
    static {
        instance = new NotificationSubscriber();
        
    }
    
    private NotificationSubscriber(){
        try {
            client = new MqttClient("tcp://host.docker.internal:1883", UUID.randomUUID().toString());

            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);

            client.setCallback(new SubscriberCallback());
            client.connect(options);
            // TODO subscribe to sybil.

        } catch (MqttException e) {
            logger.severe("Unable to connect: " + e.getMessage());
            
        }
    }
    
    public static NotificationSubscriber getInstance(){
        return instance;
    }

    public class SubscriberCallback implements MqttCallback {

        @Override
        public void connectionLost(Throwable throwable) {
            logger.info("*********************************");
            logger.info("* Connection LOST ");
            logger.info("* ");
            logger.info("* ");
            logger.info("* ");

        }


        private LinkedBlockingDeque<String> dedup = new LinkedBlockingDeque<>(15);

        private NetworkPublicKeyManager keys = NetworkPublicKeyManager.getInstance();

        @Override
        public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
            byte[] received = mqttMessage.getPayload();
            String hash = CryptoUtils.computeMd5HashAsHex(received);

            if (!dedup.contains(hash)){
                dedup.put(hash);

                String json = new String(received, StandardCharsets.UTF_8);
                String length = json.substring(0, json.indexOf('{'));

                if (WhiteList.isNumbers(length)) {
                    int l = length.length();
                    int m = Integer.parseInt(length);

                    String obj = json.substring(l, l+m);
                    ExoNotify notify = JaxbHelper.gson.fromJson(obj, ExoNotify.class);
                    processNotification(notify);

                } else {
                    logger.warning("Unknown message received -- ignoring.");

                }
            } else {
                logger.fine("Filtered duplicate " + hash);

            }
        }

        private void processNotification(ExoNotify notify) {
            try {
                String type = notify.getType();
                if (type!=null) {
                    if (type.equals(ExoNotify.TYPE_MOD) || type.equals(ExoNotify.TYPE_LEAD)) {
                        AsymStoreKey key = keys.getKey(notify.getNodeUid());
                        if (type.equals(ExoNotify.TYPE_MOD)) {
                            updateRai(notify, key);

                        } else {
                            updatePp(notify, key);

                        }
                    } else {
                        logger.warning("---------------- Ignoring message of type " + type);
                    }
                } else {
                    logger.warning("sender failed to specify type -- ignoring.");

                }
            } catch (Exception e) {
                logger.info("Failed to find node on network map: " + notify.getNodeUid());

            }
        }

        private void updateRai(ExoNotify notify, AsymStoreKey key) {
            try {
                byte[] sig = Base64.decode(notify.getRaiSigB64());
                byte[] rai = Base64.decode(notify.getRaiB64());
                String raiXml = new String(rai, StandardCharsets.UTF_8);
                byte[] signed = NodeVerifier.stripStringToSign(raiXml).getBytes();
                boolean verified = key.verifySignature(signed, sig);

                if (verified){
                    RevocationInformation ri = (RevocationInformation)
                            JaxbHelperClass.deserialize(raiXml).getValue();
                    ExonymOwner.updateRai(ri);

                } else {
                    logger.info("Failed to process RAI : Bad Signature ");

                }
            } catch (Exception e) {
                logger.info("Failed to process RAI " + e.getMessage());

            }
        }

        private void updatePp(ExoNotify notify, AsymStoreKey key) {

        }


        @Override
        public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

        }
    }
    
}
