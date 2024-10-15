package io.exonym.lib.wallet;

import io.exonym.lib.abc.util.JaxbHelper;
import io.exonym.lib.api.AbstractNetworkMap;
import io.exonym.lib.api.Cache;
import io.exonym.lib.exceptions.UxException;
import io.exonym.lib.helpers.UIDHelper;
import io.exonym.lib.pojo.NetworkMapItemModerator;
import io.exonym.lib.pojo.NetworkMapItemLead;
import io.exonym.lib.actor.NodeVerifier;
import io.exonym.lib.api.CacheContainer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Logger;

public class NetworkMap extends AbstractNetworkMap {

    private final static Logger logger = Logger.getLogger(NetworkMap.class.getName());
    private final Path root;

    public NetworkMap(Path rootToNetworkMapParent) throws Exception {
        super(rootToNetworkMapParent);
        if (rootToNetworkMapParent.getParent()==null){
            throw new UxException("network map must exist at least one level below the root");
        }
        this.root=rootToNetworkMapParent;
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
    protected void writeVerifiedLead(URI leadUid, NetworkMapItemLead nmis,
                                     ArrayList<NetworkMapItemModerator> modForLead) throws Exception {

        Path pathLead = pathToLeadPath(leadUid);
        Files.createDirectories(pathLead);
        Path pathLeadNMI = pathLead.getParent().resolve(
                toNmiFilename(nmis.getLeadUID()));

        try (BufferedWriter bw = Files.newBufferedWriter(pathLeadNMI)) {
            bw.write(JaxbHelper.serializeToJson(nmis, NetworkMapItemLead.class));
            bw.flush();

        } catch (Exception e) {
            throw e;

        }
        for (NetworkMapItemModerator advocate : modForLead){
            String advocateFileName = toNmiFilename(advocate.getNodeUID());
            Path path = pathLead.resolve(advocateFileName);
            try (BufferedWriter bw = Files.newBufferedWriter(path)) {
                bw.write(JaxbHelper.serializeToJson(advocate, NetworkMapItemModerator.class));
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
    protected NodeVerifier openNodeVerifier(URI staticNodeUrl0, boolean isTargetLead) throws Exception {
        return NodeVerifier.openNode(staticNodeUrl0, isTargetLead, false);
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
