package io.exonym.lib.api;

import java.net.URI;

public class CacheInMemory implements CacheContainer {
    
    private static CacheInMemory instance;

    private IdContainerJsonMemory x;
    
    static {
        try {
            instance = new CacheInMemory();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private CacheInMemory() throws Exception {
        this.x = new IdContainerJsonMemory();

    }
    
    public static CacheInMemory getInstance(){
        return instance;
    }

    @Override
    public <T> T open(URI material) throws Exception {
        return x.openResource(material);
    }

    @Override
    public <T> T open(String filename) throws Exception {
        return x.openResource(filename);
    }

    @Override
    public void store(Object material) throws Exception {
        x.saveLocalResource(material, true);
    }

    @Override
    public void clear() throws Exception {

    }

    @Override
    public AbstractIdContainer getContainer() throws Exception {
        return x;
    }
}
