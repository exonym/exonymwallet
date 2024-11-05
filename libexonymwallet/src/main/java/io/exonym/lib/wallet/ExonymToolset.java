package io.exonym.lib.wallet;

import eu.abc4trust.xml.CredentialInToken;
import eu.abc4trust.xml.PresentationTokenDescription;
import io.exonym.lib.api.Cache;
import io.exonym.lib.api.PkiExternalResourceContainer;
import io.exonym.lib.api.IdContainerJSON;
import io.exonym.lib.exceptions.ErrorMessages;
import io.exonym.lib.exceptions.UxException;
import io.exonym.lib.helpers.UIDHelper;
import io.exonym.lib.pojo.IdContainerSchema;
import io.exonym.lib.standard.PassStore;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Logger;

public class ExonymToolset {

    private final static Logger logger = Logger.getLogger(ExonymToolset.class.getName());
    private NetworkMap networkMap;
    private Cache cache;
    private ExonymOwner owner;
    private IdContainerJSON id;
    private IdContainerSchema schema;
    private PassStore store;

    private PkiExternalResourceContainer external;


    public ExonymToolset(PassStore store, Path rootPath, IdContainerSchema schema) throws Exception {
        if (store.getUsername()==null){
            throw new UxException(ErrorMessages.INCORRECT_PARAMETERS, "no username");
        }
        this.schema=schema;
        Path container = pathToContainers(rootPath);

        this.id = new IdContainerJSON(container, schema);
        this.networkMap = new NetworkMap(pathToNetworkMap(rootPath));
        this.cache = new Cache(rootPath);
        this.external = PkiExternalResourceContainer.getInstance();
        this.external.setNetworkMapAndCache(networkMap, cache);

        owner = new ExonymOwner(id);
        owner.openContainer(store);
        if (owner.getContainer().getOwnerSecretList().isEmpty()){
            owner.setupContainerSecret(store.getEncrypt(), store.getDecipher());
        }
        this.store = store;

    }
    public ExonymToolset(PassStore store, Path rootPath) throws Exception {
        if (store.getUsername()==null){
            throw new UxException(ErrorMessages.INCORRECT_PARAMETERS, "no username");

        }
        Path container = pathToContainers(rootPath);
        id = new IdContainerJSON(container, store.getUsername());

        networkMap = new NetworkMap(pathToNetworkMap(rootPath));
        cache = new Cache(rootPath);
        this.external = PkiExternalResourceContainer.getInstance();
        this.external.setNetworkMapAndCache(networkMap, cache);

        owner = new ExonymOwner(id);
        owner.openContainer(store);
        this.store = store;

    }

    public static NetworkMap basicInit(Path rootPath) throws Exception {
        NetworkMap networkMap = new NetworkMap(pathToNetworkMap(rootPath));
        Cache cache = new Cache(rootPath);
        PkiExternalResourceContainer external = PkiExternalResourceContainer.getInstance();
        external.setNetworkMapAndCache(networkMap, cache);
        return networkMap;

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

    protected void reopen(PresentationTokenDescription ptd){
        List<CredentialInToken> creds = ptd.getCredential();
        for (CredentialInToken cred : creds) {
            URI iuid = cred.getIssuerParametersUID();
            try {
                UIDHelper helper = new UIDHelper(iuid);
                owner.openResourceIfNotLoaded(helper.getRevocationAuthority());
                owner.openResourceIfNotLoaded(helper.getRevocationInfoParams());
                owner.openResourceIfNotLoaded(helper.getInspectorParams());
                owner.openResourceIfNotLoaded(helper.getIssuerParameters());

            } catch (Exception e) {
                logger.warning("Unexpected Error " + e.getMessage());

            }
        }
        this.owner.openContainer(store);

    }

    public PkiExternalResourceContainer getExternal() {
        return external;
    }

    protected IdContainerJSON getId() {
        return id;
    }

    public IdContainerSchema getSchema() {
        return schema;
    }
}
