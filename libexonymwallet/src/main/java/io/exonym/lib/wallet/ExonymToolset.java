package io.exonym.lib.wallet;

import io.exonym.lib.api.Cache;
import io.exonym.lib.api.PkiExternalResourceContainer;
import io.exonym.lib.api.XContainerJSON;
import io.exonym.lib.exceptions.ErrorMessages;
import io.exonym.lib.exceptions.UxException;
import io.exonym.lib.standard.PassStore;

import java.nio.file.Path;

public class ExonymToolset {

    private NetworkMap networkMap;
    private Cache cache;
    private ExonymOwner owner;
    private XContainerJSON x;


    public ExonymToolset(PassStore store, Path rootPath) throws Exception {
        if (store.getUsername()==null){
            throw new UxException(ErrorMessages.INCORRECT_PARAMETERS, "no username");

        }
        networkMap = new NetworkMap(pathToNetworkMap(rootPath));
        cache = new Cache(rootPath);
        PkiExternalResourceContainer external = PkiExternalResourceContainer.getInstance();
        external.setNetworkMapAndCache(networkMap, cache);
        x = new XContainerJSON(pathToContainers(rootPath), store.getUsername());
        owner = new ExonymOwner(x);
        owner.openContainer(store);

    }

    public static Path pathToNetworkMap(Path root){
        return root.resolve("network-map");
    }

    public static Path pathToContainers(Path root){
        return root.resolve("containers");
    }


    protected NetworkMap getNetworkMap() {
        return networkMap;
    }

    protected Cache getCache() {
        return cache;
    }

    protected ExonymOwner getOwner() {
        return owner;
    }

    protected XContainerJSON getX() {
        return x;
    }
}
