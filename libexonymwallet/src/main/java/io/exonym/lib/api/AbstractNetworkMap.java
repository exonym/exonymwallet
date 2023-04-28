package io.exonym.lib.api;

import io.exonym.lib.abc.util.JaxbHelper;
import io.exonym.lib.actor.NodeVerifier;
import io.exonym.lib.actor.TrustNetworkWrapper;
import io.exonym.lib.exceptions.ErrorMessages;
import io.exonym.lib.exceptions.HubException;
import io.exonym.lib.exceptions.UxException;
import io.exonym.lib.helpers.UIDHelper;
import io.exonym.lib.helpers.UrlHelper;
import io.exonym.lib.pojo.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractNetworkMap {

    private Path rootPath = null;
    private final CacheContainer cache;

    public AbstractNetworkMap(Path rootPath) throws Exception {
        this.rootPath = rootPath;
        this.cache = instantiateCache(this.rootPath);

    }

    public abstract void delete() throws IOException;

    public AbstractNetworkMap() throws Exception {
        this.rootPath = defineRootPath();
        this.cache = instantiateCache(this.rootPath);

    }

    public void spawnIfDoesNotExist() throws Exception {
        if (!this.networkMapExists()){
            this.spawn();

        }
    }

    /**
     *
     * @return the default path if it is a file system.  Null if you are using a database.
     */
    protected abstract Path defineRootPath();

    /**
     *
     * @return the appropriate Cache back for the environment.
     */
    protected abstract CacheContainer instantiateCache(Path root) throws Exception;

    /**
     * Write the NMIS and the NMIAs to your chosen repository
     *
     * @param rulebookId
     * @param source
     * @param nmis
     * @param advocatesForSource
     * @throws Exception
     */
    protected abstract void writeVerifiedSource(String rulebookId, String source, NetworkMapItemSource nmis,
                                       ArrayList<NetworkMapItemAdvocate> advocatesForSource) throws Exception;


    protected Path pathToRootPath() {
        if (rootPath==null){
            throw new RuntimeException("You have not defined a root path in the implementation");
        }
        return rootPath;

    }

    protected Path pathToSourcePath(String rulebook, String source) {
        return rootPath.resolve(rulebook).resolve(source);
    }

    protected Path pathToRulebookPath(String rulebook) {
        return rootPath.resolve(rulebook);
    }

    public void spawn() throws Exception {
        cleanupExisting();
        TrustNetworkWrapper tnw = new TrustNetworkWrapper(openSourceSet());
        Collection<NetworkParticipant> allSources = tnw.getAllParticipants();
        for (NetworkParticipant source : allSources){
            buildMapForSource(source);

        }
    }

    private void buildMapForSource(NetworkParticipant source) throws Exception {
        NetworkMapItemSource nmis = new NetworkMapItemSource();
        buildBasisNMI(nmis, source);
        nmis.setSourceUID(source.getNodeUid());
        ArrayList<URI> advocateListForSource = new ArrayList<>();
        ArrayList<NetworkMapItemAdvocate> advocatesForSource = verifySource(advocateListForSource, source);
        nmis.setAdvocatesForSource(advocateListForSource);
        String[] parts = source.getNodeUid().toString().split(":");
        writeVerifiedSource(parts[3], parts[2], nmis, advocatesForSource);

    }


    public String toNmiFilename(URI advocate) {
        return advocate.toString()
//                .replaceAll(":" + rulebookId, "") // overly complex to recompute UID
                .replaceAll(":", ".") + ".nmi";

    }

    public URI fromNmiFilename(String filename) {
        if (filename==null){
            throw new NullPointerException();
        }
        return URI.create(filename.replaceAll(".nmi", "").replaceAll("\\.", ":"));
    }


    public abstract boolean networkMapExists() throws Exception;

    protected void cleanupExisting() {
    }

    private ArrayList<NetworkMapItemAdvocate> verifySource(ArrayList<URI> advocateListForSource, NetworkParticipant source) throws Exception {
        NodeVerifier verifier = openNodeVerifier(source.getStaticNodeUrl0(),
                source.getStaticNodeUrl1(), true);

        Rulebook rulebook = verifier.getRulebook();
        cache.store(rulebook);
        cache.store(verifier.getPresentationPolicy());
        cache.store(verifier.getCredentialSpecification());

        TrustNetworkWrapper tnw = new TrustNetworkWrapper(verifier.getTargetTrustNetwork());
        Collection<NetworkParticipant> allAdvocates = tnw.getAllParticipants();
        ArrayList<NetworkMapItemAdvocate> advocatesForSource = new ArrayList<>();
        for (NetworkParticipant advocate : allAdvocates){
            URI sourceUid = source.getNodeUid();
            advocateListForSource.add(advocate.getNodeUid());
            advocatesForSource.add(buildAdvocateNMIA(sourceUid, advocate));

        }
        return advocatesForSource;

    }

    protected abstract NodeVerifier openNodeVerifier(URL staticNodeUrl0,
                                                     URL staticNodeUrl1,
                                                     boolean isTargetSource) throws Exception;

    private NetworkMapItemAdvocate buildAdvocateNMIA(URI sourceUid, NetworkParticipant participant) throws Exception {
        NetworkMapItemAdvocate nmia = new NetworkMapItemAdvocate();
        buildBasisNMI(nmia, participant);
        nmia.setSourceUID(sourceUid);
        return nmia;

    }

    private void buildBasisNMI(NetworkMapItem nmi, NetworkParticipant participant) throws Exception {
        nmi.setNodeUID(participant.getNodeUid());
        nmi.setPublicKeyB64(participant.getPublicKey().getPublicKey());
        nmi.setStaticURL0(participant.getStaticNodeUrl0());
        nmi.setStaticURL1(participant.getStaticNodeUrl1());
        nmi.setLastUpdated(participant.getLastUpdateTime());
        nmi.setBroadcastAddress(participant.getBroadcastAddress());
        nmi.setRulebookNodeURL(participant.getRulebookNodeUrl());
        nmi.setRegion(participant.getRegion());
        URI lastUid = participant.getLastIssuerUID();
        if (lastUid!=null){
            UIDHelper helper = new UIDHelper(lastUid);
            nmi.setLastIssuerUID(lastUid);
            nmi.setSourceName(helper.getSourceName());
            nmi.setAdvocateName(helper.getAdvocateName());

        } else {
            nmi.setSourceName(
                    UIDHelper.computeSourceNameFromAdvocateOrSourceUid(
                    nmi.getNodeUID()));

        }
    }

    private TrustNetwork openSourceSet() throws Exception {
        try {
            String sources = "https://trust.exonym.io/sources.xml";
            byte[] s = UrlHelper.readXml(new URL(sources));
            return JaxbHelper.xmlToClass(s, TrustNetwork.class);

        } catch (Exception e) {
            throw e;

        }
    }


    public NetworkMapItem findNetworkMapItem(URI sourceOrAdvocate) throws Exception {
        if (sourceOrAdvocate==null){
            throw new HubException("Null URL - Programming Error");
        }
        URI sourceUid = UIDHelper.computeSourceUidFromNodeUid(sourceOrAdvocate);
        String sourceName = UIDHelper.computeSourceNameFromAdvocateOrSourceUid(sourceUid);
        String rulebookId = UIDHelper.computeRulebookHashFromSourceUid(sourceUid);
        Path path = null;
        if (UIDHelper.isAdvocateUid(sourceOrAdvocate)){
            path = pathToSourcePath(rulebookId, sourceName)
                    .resolve(toNmiFilename(sourceOrAdvocate));

            if (Files.exists(path)){
                return JaxbHelper.jsonFileToClass(path, NetworkMapItemAdvocate.class);

            } else {
                throw new UxException(ErrorMessages.FILE_NOT_FOUND,
                        path.toAbsolutePath().toString());

            }
        } else if (UIDHelper.isSourceUid(sourceOrAdvocate)){
            path = pathToRulebookPath(rulebookId).resolve(toNmiFilename(sourceOrAdvocate));

            if (Files.exists(path)){
                return JaxbHelper.jsonFileToClass(path, NetworkMapItemSource.class);

            } else {
                throw new UxException(ErrorMessages.FILE_NOT_FOUND,
                        path.toAbsolutePath().toString());

            }
        } else {
            throw new UxException(ErrorMessages.INCORRECT_PARAMETERS, sourceOrAdvocate.toString());

        }
    }

    public List<String> getSourceFilenamesForRulebook(String rulebookId) throws UxException {
        if (rulebookId==null){
            throw new NullPointerException();

        } if (rulebookId.startsWith(Namespace.URN_PREFIX_COLON)){
            rulebookId = rulebookId.replaceAll(Namespace.URN_PREFIX_COLON, "");

        }
        Path path = pathToRulebookPath(rulebookId);
        if (Files.exists(path)){
            return Stream.of(new File(path.toString()).listFiles())
                    .filter(file -> !file.isDirectory())
                    .map(File::getName)
                    .collect(Collectors.toList());

        } else {
            throw new UxException(ErrorMessages.FILE_NOT_FOUND,
                    "No such rulebook", path.toString());

        }
    }

    public List<String> listRulebooks() throws UxException {
        if (Files.exists(rootPath)){
            return Stream.of(new File(rootPath.toString()).listFiles())
                    .filter(file -> file.isDirectory())
                    .map(File::getName)
                    .collect(Collectors.toList());

        } else {
            throw new UxException(ErrorMessages.FILE_NOT_FOUND,
                    "No such rulebook", rootPath.toString());

        }
    }

    public NetworkMapItem nmiForNode(URI uid) throws Exception {
        if (uid==null){
            throw new HubException("Null Node UID - Programming Error");

        }
        String fileName = toNmiFilename(uid);

        if (UIDHelper.isSourceUid(uid)){
            String rulebookId = UIDHelper.computeRulebookHashFromSourceUid(uid);
            Path nmiPath = pathToRulebookPath(rulebookId).resolve(fileName);
            return JaxbHelper.jsonFileToClass(nmiPath, NetworkMapItemSource.class);

        } else if (UIDHelper.isAdvocateUid(uid)){
            String rulebookId = UIDHelper.computeRulebookHashFromAdvocateUid(uid);
            String sourceName = UIDHelper.computeSourceNameFromAdvocateOrSourceUid(uid);
            Path nmiPath = pathToSourcePath(rulebookId, sourceName).resolve(fileName);
            return JaxbHelper.jsonFileToClass(nmiPath, NetworkMapItemAdvocate.class);

        } else {
            throw new UxException(ErrorMessages.FILE_NOT_FOUND + ":" + uid.toString());

        }
    }

    public NetworkMapItemSource nmiForSybilSource() throws Exception {
        return (NetworkMapItemSource) nmiForNode(Rulebook.SYBIL_SOURCE_UID);
    }

    public NetworkMapItemAdvocate nmiForSybilTestNet() throws Exception {
        return (NetworkMapItemAdvocate) nmiForNode(Rulebook.SYBIL_TEST_NET_UID);
    }

    public NetworkMapItemAdvocate nmiForSybilMainNet() throws Exception {
        return (NetworkMapItemAdvocate) nmiForNode(Rulebook.SYBIL_MAIN_NET_UID);
    }

    public NetworkMapItemSource nmiForMyNodesSource() throws Exception{
        throw new UxException(ErrorMessages.INCORRECT_PARAMETERS, "Wallets do not have sources");
    }

    public NetworkMapItemAdvocate nmiForMyNodesAdvocate() throws Exception{
        throw new UxException(ErrorMessages.INCORRECT_PARAMETERS, "Wallets do not have advocates");
    }

    protected NetworkMapItem findRandomAdvocateForSource(String source) throws Exception {
        List<NetworkMapItem> hosts = findAdvocatesForSource(source);
        int size = hosts.size();
        int target = (int)(Math.random() * 1000000) % size;
        return hosts.get(target);

    }

    protected List<NetworkMapItem> findAdvocatesForSource(String source) throws Exception {
        return null;

    }

    public CacheContainer getCache() {
        return cache;
    }
}
