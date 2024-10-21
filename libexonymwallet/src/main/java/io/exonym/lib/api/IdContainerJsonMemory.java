package io.exonym.lib.api;

import io.exonym.lib.pojo.NetworkMapItemModerator;
import io.exonym.lib.pojo.IdContainerSchema;
import io.exonym.lib.standard.PassStore;
import io.exonym.lib.wallet.ExonymOwner;

import java.util.logging.Logger;

public class IdContainerJsonMemory extends IdContainerJSON {

    private final static Logger logger = Logger.getLogger(IdContainerJsonMemory.class.getName());

    public IdContainerJsonMemory() throws Exception {
        super("blank");
    }

    @Override
    protected void commitSchema() throws Exception {
        logger.fine("In memory container");
    }

    @Override
    protected IdContainerSchema init(boolean create) throws Exception {
        return new IdContainerSchema();
    }

    @Override
    public synchronized void saveLocalResource(Object resource) throws Exception {
        super.saveLocalResource(resource);
    }

    @Override
    public synchronized void saveLocalResource(Object resource, boolean overwrite) throws Exception {
        super.saveLocalResource(resource, overwrite);
    }

    public static void main(String[] args) throws Exception {
        IdContainerJsonMemory x = new IdContainerJsonMemory();
        ExonymOwner owner = new ExonymOwner(x);

        PassStore store = new PassStore("password", false);
        owner.openContainer(store);
        owner.setupContainerSecret(store.getEncrypt(), store.getDecipher());


        NetworkMapMemory m = NetworkMapMemory.getInstance();
        NetworkMapItemModerator nmia = m.nmiForSybilTestNet();

        logger.info("nmia=" + nmia.getNodeUID());

    }
}
