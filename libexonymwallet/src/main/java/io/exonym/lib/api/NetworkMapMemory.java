package io.exonym.lib.api;

import io.exonym.lib.actor.NodeVerifier;
import io.exonym.lib.exceptions.ErrorMessages;
import io.exonym.lib.exceptions.UxException;
import io.exonym.lib.pojo.NetworkMapItem;
import io.exonym.lib.pojo.NetworkMapItemAdvocate;
import io.exonym.lib.pojo.NetworkMapItemSource;
import io.exonym.lib.standard.WhiteList;
import org.graalvm.collections.EconomicMap;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

// TODO In principle we can discovery only the information that's needed for this node
// A lightweight network map based on SsoConfigurations is desirable.
// This is not that.
public class NetworkMapMemory extends AbstractNetworkMap {
    
    private final static Logger logger = Logger.getLogger(NetworkMapMemory.class.getName());
    
    private final HashSet<String> rulebookIds = new HashSet<>();
    private final ConcurrentHashMap<URI, NetworkMapItemSource> sourceMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<URI, NetworkMapItemAdvocate> advocateMap = new ConcurrentHashMap<>();

    private NetworkMapMemory() throws Exception {
        this.spawn();
    }

    @Override
    protected void writeVerifiedSource(String rulebookId, String source, NetworkMapItemSource nmis,
                                       ArrayList<NetworkMapItemAdvocate> advocatesForSource) throws Exception {
        rulebookIds.add(rulebookId);
        sourceMap.put(nmis.getSourceUID(), nmis);
        for (NetworkMapItemAdvocate nmia : advocatesForSource){
            advocateMap.put(nmia.getNodeUID(), nmia);

        }
    }

    @Override
    public NetworkMapItemSource nmiForSybilSource() throws Exception {
        return super.nmiForSybilSource();
    }

    @Override
    public NetworkMapItem nmiForNode(URI uid) throws Exception {
        if (WhiteList.isAdvocateUid(uid)){
            return advocateMap.get(uid);


        } else if (WhiteList.isSourceUid(uid)){
            return sourceMap.get(uid);

        } else {
            throw new UxException(ErrorMessages.INVALID_UID);

        }
    }

    @Override
    protected NodeVerifier openNodeVerifier(URL staticNodeUrl0, URL staticNodeUrl1, boolean isTargetSource) throws Exception {
        return NodeVerifier.tryNode(
                staticNodeUrl0.toString(), staticNodeUrl1.toString(),
                isTargetSource, false);
    }

    @Override
    protected CacheContainer instantiateCache(Path root) throws Exception {
        return CacheInMemory.getInstance();

    }

    @Override
    public boolean networkMapExists() throws Exception {
        return true;
    }

    @Override
    public void delete() throws IOException {
        // nothing to delete
    }

    @Override
    protected Path defineRootPath() {
        // no path to define
        return null;
    }


    private static NetworkMapMemory instance;

    static {
        try {
            instance = new NetworkMapMemory();
        } catch (Exception e) {
            logger.severe("NETWORK MAP (MEMORY FAILED TO INSTATIATE)");
            throw new RuntimeException(e);
        }

    }

    public static NetworkMapMemory getInstance(){
        return instance;
    }

}
