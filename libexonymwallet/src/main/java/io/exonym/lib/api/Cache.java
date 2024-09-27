package io.exonym.lib.api;

import io.exonym.lib.pojo.IdContainer;
import io.exonym.lib.standard.WhiteList;

import java.io.FileNotFoundException;
import java.net.URI;
import java.nio.file.Path;

public class Cache implements CacheContainer {

    private IdContainerJSON x;
    private final Path path;

    public Cache(Path path) throws Exception {
        this.path = path;
        try {
            x = new IdContainerJSON(path, "cache", true);

        } catch (Exception e) {
            x = new IdContainerJSON(path, "cache", false);

        }
    }

    public <T> T open(URI material) throws Exception {
        try {
            return x.openResource(material);

        } catch (FileNotFoundException e) {
            return null;

        }
    }

    public <T> T open(String filename) throws Exception {
        try {
            if (!(filename.endsWith(".json") || filename.endsWith(".xml"))){
                if (WhiteList.isRulebookUid(filename)){
                    filename = IdContainer.uidToFileName(filename) + ".json";

                } else {
                    filename = IdContainer.uidToXmlFileName(filename);

                }
            }
            return x.openResource(filename);

        } catch (FileNotFoundException e) {
            return null;

        }
    }

    public void store(Object material) throws Exception {
        x.saveLocalResource(material, true);

    }

    public void clear() throws Exception {
        x.delete();
        x = new IdContainerJSON(this.path, "cache", true);

    }

    @Override
    public AbstractIdContainer getContainer() throws Exception {
        return x;
    }
}
