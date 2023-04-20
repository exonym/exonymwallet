package io.exonym.lib.wallet;

import io.exonym.lib.abc.util.JaxbHelper;
import io.exonym.lib.api.AbstractNetworkMap;
import io.exonym.lib.api.Cache;
import io.exonym.lib.exceptions.UxException;
import io.exonym.lib.pojo.NetworkMapItemAdvocate;
import io.exonym.lib.pojo.NetworkMapItemSource;
import io.exonym.lib.actor.NodeVerifier;
import io.exonym.lib.api.CacheContainer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Logger;

public class NetworkMap extends AbstractNetworkMap {

    private final static Logger logger = Logger.getLogger(NetworkMap.class.getName());
    private final Path root;

    public NetworkMap(Path root) throws Exception {
        super(root);
        if (root.getParent()==null){
            throw new UxException("network map must exist at least one level below the root");
        }
        this.root=root;
    }

    /**
     * @return the default path if it is a file system.  Null if you are using a database.
     */
    @Override
    protected Path defineRootPath() {
        return this.root;
    }

    /**
     * @return the appropriate Cache back for the environment.
     */
    @Override
    protected CacheContainer instantiateCache(Path path) throws Exception {
        return new Cache(path.getParent());
    }

    @Override
    protected void writeVerifiedSource(String rulebookId, String source, NetworkMapItemSource nmis,
                                     ArrayList<NetworkMapItemAdvocate> advocatesForSource) throws Exception {

        Path pathSource = pathToSourcePath(rulebookId, source);
        Files.createDirectories(pathSource);
        Path pathSourceNMI = pathToRootPath()
                .resolve(rulebookId)
                .resolve(toNmiFilename(nmis.getSourceUID()));

        try (BufferedWriter bw = Files.newBufferedWriter(pathSourceNMI)) {
            bw.write(JaxbHelper.serializeToJson(nmis, NetworkMapItemSource.class));
            bw.flush();

        } catch (Exception e) {
            throw e;

        }
        for (NetworkMapItemAdvocate advocate : advocatesForSource){
            String advocateFileName = toNmiFilename(advocate.getNodeUID());
            Path path = pathSource.resolve(advocateFileName);
            try (BufferedWriter bw = Files.newBufferedWriter(path)) {
                bw.write(JaxbHelper.serializeToJson(advocate, NetworkMapItemAdvocate.class));
                bw.flush();

            } catch (Exception e) {
                throw e;

            }
        }
    }

    @Override
    public boolean networkMapExists(){
        return Files.exists(pathToRootPath());

    }

    @Override
    protected NodeVerifier openNodeVerifier(URL staticNodeUrl0, URL staticNodeUrl1, boolean isTargetSource) throws Exception {
        return NodeVerifier.tryNode(staticNodeUrl0.toString(), staticNodeUrl1.toString(), isTargetSource, false);

    }

    @Override
    public void delete() throws IOException {
        Path path = defineRootPath();
        File file = path.toFile();
        deleteDirectory(file);

    }

    private void deleteDirectory(File file) throws IOException {
        if (file.isDirectory()) {
            File[] entries = file.listFiles();
            if (entries != null) {
                for (File entry : entries) {
                    deleteDirectory(entry);
                }
            }
        }
        if (!file.delete()) {
            throw new IOException("Failed to delete " + file);

        }
    }
}
