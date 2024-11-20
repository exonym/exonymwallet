package io.exonym.lib.api;

import io.exonym.lib.actor.NodeVerifier;
import io.exonym.lib.exceptions.ErrorMessages;
import io.exonym.lib.exceptions.HubException;
import io.exonym.lib.exceptions.UxException;
import io.exonym.lib.helpers.UIDHelper;
import io.exonym.lib.pojo.NetworkMapItem;
import io.exonym.lib.pojo.NetworkMapItemModerator;
import io.exonym.lib.pojo.NetworkMapItemLead;
import io.exonym.lib.standard.WhiteList;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

// TODO In principle we can discovery only the information that's needed for this node
// A lightweight network map based on SsoConfigurations is desirable.
// This is not that.
public class NetworkMapMemory extends AbstractNetworkMap {
    
    private final static Logger logger = Logger.getLogger(NetworkMapMemory.class.getName());
    
    private final HashSet<String> rulebookIds = new HashSet<>();
    private final ConcurrentHashMap<URI, NetworkMapItemLead> leadMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<URI, NetworkMapItemModerator> modMap = new ConcurrentHashMap<>();

    private NetworkMapMemory() throws Exception {
        this.spawn();
    }

    @Override
    protected void writeVerifiedLead(URI leadUid, NetworkMapItemLead nmis,
                                     ArrayList<NetworkMapItemModerator> modForLead) throws Exception {
        URI rulebookId = UIDHelper.computeRulebookIdFromLeadUid(leadUid);
        rulebookIds.add(rulebookId.toString());
        leadMap.put(nmis.getLeadUID(), nmis);
        for (NetworkMapItemModerator nmia : modForLead){
            modMap.put(nmia.getNodeUID(), nmia);

        }
    }

    @Override
    public NetworkMapItemLead nmiForSybilLead() throws Exception {
        return super.nmiForSybilLead();
    }

    @Override
    public NetworkMapItemModerator nmiForSybilTestNet() throws Exception {
        return super.nmiForSybilTestNet();
    }

    @Override
    public NetworkMapItemModerator nmiForSybilMainNet() throws Exception {
        return super.nmiForSybilMainNet();
    }

    @Override
    public NetworkMapItem nmiForNode(URI uid) throws Exception {
        logger.info("Searching for: " + uid);
        if (WhiteList.isModeratorUid(uid)){
            NetworkMapItem item = modMap.get(uid);
            if (item==null){
                throw new HubException(ErrorMessages.MODERATOR_NOT_FOUND_ON_NETWORK_MAP + ":" + uid);

            }else{
                return item;

            }
        } else if (WhiteList.isLeadUid(uid)){
            NetworkMapItem item = leadMap.get(uid);
            if (item==null){
                throw new HubException(ErrorMessages.MODERATOR_NOT_FOUND_ON_NETWORK_MAP + ":" + uid);

            }else{
                return item;

            }
        } else {
            throw new UxException(ErrorMessages.INVALID_UID);

        }
    }

    @Override
    protected NodeVerifier openNodeVerifier(URI staticNodeUrl0, boolean isTargetLead) throws Exception {
        return NodeVerifier.openNode(staticNodeUrl0, isTargetLead, false);
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
