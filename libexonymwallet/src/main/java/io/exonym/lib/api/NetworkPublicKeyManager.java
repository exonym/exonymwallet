package io.exonym.lib.api;



import io.exonym.lib.pojo.NetworkMapItem;
import io.exonym.lib.standard.AsymStoreKey;

import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class NetworkPublicKeyManager {
    private final static Logger logger = Logger.getLogger(NetworkPublicKeyManager.class.getName());
    private static NetworkPublicKeyManager instance;
    
    static {
        instance = new NetworkPublicKeyManager();
    }

    protected static NetworkPublicKeyManager getInstance(){
        return instance;
    }
    private final ConcurrentHashMap<URI, AsymStoreKey> keys = new ConcurrentHashMap<>();

    private final NetworkMapMemory networkMap = NetworkMapMemory.getInstance();

    private NetworkPublicKeyManager(){
    }

    protected synchronized AsymStoreKey getKey(URI nodeUid) throws Exception {
        if (keys.containsKey(nodeUid)){
            return keys.get(nodeUid);

        } else {
            logger.fine("Opening Key " + nodeUid);
            try {
                NetworkMapItem nmi = networkMap.nmiForNode(nodeUid);
                byte[] keyBytes = nmi.getPublicKeyB64();
                AsymStoreKey key = AsymStoreKey.blank();
                key.assembleKey(keyBytes);
                this.keys.put(nodeUid, key);
                return key;

            } catch (Exception e) {
                logger.warning("Unable to find key for " + nodeUid);
                throw e;

            }
        }
    }
}




