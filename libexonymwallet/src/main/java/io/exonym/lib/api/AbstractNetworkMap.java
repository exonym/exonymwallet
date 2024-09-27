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
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractNetworkMap {
    
    private final static Logger logger = Logger.getLogger(AbstractNetworkMap.class.getName());

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
     * @throws Exception
     */
    protected abstract void writeVerifiedLead(URI leadUid, NetworkMapItemLead nmis,
                                              ArrayList<NetworkMapItemModerator> modForLead) throws Exception;


    protected Path pathToRootPath() {
        if (rootPath==null){
            throw new RuntimeException("You have not defined a root path in the implementation");
        }
        return rootPath;

    }

    protected Path pathToLeadPath(URI leadUid) {
        Path path = pathToRulebookPath(leadUid);
        String name = UIDHelper.computeLeadNameFromModOrLeadUid(leadUid);
        return path.resolve(name);
    }

    protected Path pathToRulebookPath(URI leadOrRulebookUid) {
        if (leadOrRulebookUid==null){
            throw new NullPointerException();
        }
        String[] parts = UIDHelper.computeRulebookIdFromLeadUid(leadOrRulebookUid).toString().split(":");
        if (parts.length==4){
            return rootPath.resolve(parts[2]).resolve(parts[3]);
        } else {
            return rootPath.resolve(parts[2]).resolve(parts[4]);
        }
    }

    public void spawn() throws Exception {
        cleanupExisting();
        TrustNetworkWrapper tnw = new TrustNetworkWrapper(openLeadSet());
        Collection<NetworkParticipant> allLeads = tnw.getAllParticipants();
        for (NetworkParticipant lead : allLeads){
            buildMapForLead(lead);

        }
    }

    private void buildMapForLead(NetworkParticipant lead) throws Exception {
        NetworkMapItemLead nmis = new NetworkMapItemLead();
        buildBasisNMI(nmis, lead);
        nmis.setLeadUID(lead.getNodeUid());
        ArrayList<URI> modListForLead = new ArrayList<>();
        ArrayList<NetworkMapItemModerator> modsForLead = verifyLead(modListForLead, lead);
        nmis.setModeratorsForLead(modListForLead);
        writeVerifiedLead(lead.getNodeUid(), nmis, modsForLead);

    }


    public String toNmiFilename(URI moderator) {
        return moderator.toString()
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

    private ArrayList<NetworkMapItemModerator> verifyLead(ArrayList<URI> modListForLead, NetworkParticipant lead) throws Exception {
        NodeVerifier verifier = openNodeVerifier(lead.getStaticNodeUrl0(), true);

        Rulebook rulebook = verifier.getRulebook();
        cache.store(rulebook);
        cache.store(verifier.getPresentationPolicy());
        cache.store(verifier.getCredentialSpecification());

        TrustNetworkWrapper tnw = new TrustNetworkWrapper(verifier.getTargetTrustNetwork());
        Collection<NetworkParticipant> allMods = tnw.getAllParticipants();
        ArrayList<NetworkMapItemModerator> modsForLead = new ArrayList<>();
        for (NetworkParticipant mod : allMods){
            URI leadUid = lead.getNodeUid();
            modListForLead.add(mod.getNodeUid());
            modsForLead.add(buildModNMIA(leadUid, mod));

        }
        return modsForLead;

    }

    protected abstract NodeVerifier openNodeVerifier(URI staticNodeUrl0,
                                                     boolean isTargetLead) throws Exception;

    private NetworkMapItemModerator buildModNMIA(URI leadUid, NetworkParticipant participant) throws Exception {
        NetworkMapItemModerator nmia = new NetworkMapItemModerator();
        buildBasisNMI(nmia, participant);
        nmia.setLeadUID(leadUid);
        return nmia;

    }

    private void buildBasisNMI(NetworkMapItem nmi, NetworkParticipant participant) throws Exception {
        nmi.setNodeUID(participant.getNodeUid());
        nmi.setPublicKeyB64(participant.getPublicKey().getPublicKey());
        nmi.setStaticURL0(participant.getStaticNodeUrl0());
        nmi.setLastUpdated(participant.getLastUpdateTime());
        nmi.setBroadcastAddress(participant.getBroadcastAddress());
        nmi.setRulebookNodeURL(participant.getRulebookNodeUrl());
        nmi.setRegion(participant.getRegion());
        URI lastUid = participant.getLastIssuerUID();
        if (lastUid!=null){
            UIDHelper helper = new UIDHelper(lastUid);
            nmi.setLastIssuerUID(lastUid);
            nmi.setLeadName(helper.getLeadName());
            nmi.setModeratorName(helper.getModeratorName());

        } else {
            nmi.setLeadName(UIDHelper
                    .computeLeadNameFromModOrLeadUid(
                            nmi.getNodeUID()));

        }
    }

    private TrustNetwork openLeadSet() throws Exception {
        try {
            String leads = "https://trust.exonym.io/leads.xml";
            byte[] s = UrlHelper.readXml(new URL(leads));
            return JaxbHelper.xmlToClass(s, TrustNetwork.class);

        } catch (Exception e) {
            throw e;

        }
    }


    public NetworkMapItem findNetworkMapItem(URI leadOrMod) throws Exception {
        if (leadOrMod==null){
            throw new HubException("Null URL - Programming Error");
        }
        URI leadUid = UIDHelper.computeLeadUidFromModUid(leadOrMod);
        String leadName = UIDHelper.computeLeadNameFromModOrLeadUid(leadUid);
        String rulebookId = UIDHelper.computeRulebookHashUid(leadUid);
        Path path = null;
        if (UIDHelper.isModeratorUid(leadOrMod)){
            path = pathToLeadPath(leadUid)
                    .resolve(toNmiFilename(leadOrMod));

            if (Files.exists(path)){
                return JaxbHelper.jsonFileToClass(path, NetworkMapItemModerator.class);

            } else {
                throw new UxException(ErrorMessages.FILE_NOT_FOUND,
                        path.toAbsolutePath().toString());

            }
        } else if (UIDHelper.isLeadUid(leadOrMod)){
            path = pathToRulebookPath(leadUid).resolve(toNmiFilename(leadOrMod));

            if (Files.exists(path)){
                return JaxbHelper.jsonFileToClass(path, NetworkMapItemLead.class);

            } else {
                throw new UxException(ErrorMessages.FILE_NOT_FOUND,
                        path.toAbsolutePath().toString());

            }
        } else {
            throw new UxException(ErrorMessages.INCORRECT_PARAMETERS, leadOrMod.toString());

        }
    }

    public List<String> getLeadNamesForRulebook(String rulebookId) throws UxException {
        if (rulebookId==null){
            throw new NullPointerException();

        }
        Path path = pathToRulebookPath(URI.create(rulebookId));

        if (Files.exists(path)){
            return Stream.of(new File(path.toString()).listFiles())
                    .filter(file -> file.isDirectory())
                    .map(File::getName)
                    .collect(Collectors.toList());

        } else {
            logger.info("Searching for path " + path);
            throw new UxException(ErrorMessages.FILE_NOT_FOUND,
                    "No such rulebook", path.toString());

        }
    }

    public List<String> getLeadFileNamesForRulebook(String rulebookId) throws UxException {
        if (rulebookId==null){
            throw new NullPointerException();

        }
        Path path = pathToRulebookPath(URI.create(rulebookId));

        if (Files.exists(path)){
            return Stream.of(new File(path.toString()).listFiles())
                    .filter(file -> !file.isDirectory())
                    .map(File::getName)
                    .collect(Collectors.toList());

        } else {
            logger.info("Searching for path " + path);
            throw new UxException(ErrorMessages.FILE_NOT_FOUND,
                    "No such rulebook", path.toString());

        }
    }

    public List<String> listRulebooks() throws UxException {
        if (Files.exists(rootPath)) {
            try (Stream<Path> walk = Files.walk(rootPath)) {
                return walk.filter(Files::isDirectory)
                        .filter(this::isTargetDirectory)
                        .map(this::createUrnFromPath)
                        .collect(Collectors.toList());
            } catch (IOException e) {
                throw new UxException(ErrorMessages.FILE_NOT_FOUND,
                        "Error accessing files", rootPath.toString());
            }
        } else {
            throw new UxException(ErrorMessages.FILE_NOT_FOUND,
                    "No such rulebook", rootPath.toString());
        }
    }

    private boolean isTargetDirectory(Path path) {
        String fileName = path.getFileName().toString();
        return fileName.matches("[a-fA-F0-9]{64}");
    }

    private String createUrnFromPath(Path path) {
        String type = path.getParent().getFileName().toString();
        String id = path.getFileName().toString();
        return "urn:rulebook:" + type + ":" + id;
    }

//    public List<String> listRulebooks() throws UxException {
//        if (Files.exists(rootPath)){
//            return Stream.of(new File(rootPath.toString()).listFiles())
//                    .filter(file -> file.isDirectory())
//                    .map(File::getName)
//                    .collect(Collectors.toList());
//
//        } else {
//            throw new UxException(ErrorMessages.FILE_NOT_FOUND,
//                    "No such rulebook", rootPath.toString());
//
//        }
//    }

    public NetworkMapItem nmiForNode(URI uid) throws Exception {
        if (uid==null){
            throw new HubException("Null Node UID - Programming Error");

        }
        String fileName = toNmiFilename(uid);

        if (UIDHelper.isLeadUid(uid)){
            Path nmiPath = pathToLeadPath(uid).getParent().resolve(fileName);
            return JaxbHelper.jsonFileToClass(nmiPath, NetworkMapItemLead.class);

        } else if (UIDHelper.isModeratorUid(uid)){
            URI leadUid = UIDHelper.computeLeadUidFromModUid(uid);
            Path nmiPath = pathToLeadPath(leadUid).resolve(fileName);
            return JaxbHelper.jsonFileToClass(nmiPath, NetworkMapItemModerator.class);

        } else {
            throw new UxException(ErrorMessages.FILE_NOT_FOUND + ":" + uid);

        }
    }

    public NetworkMapItemLead nmiForSybilLead() throws Exception {
        return (NetworkMapItemLead) nmiForNode(Rulebook.SYBIL_LEAD_UID_TEST);
    }

    public NetworkMapItemModerator nmiForSybilTestNet() throws Exception {
        return (NetworkMapItemModerator) nmiForNode(Rulebook.SYBIL_MOD_UID_TEST);
    }

    public NetworkMapItemModerator nmiForSybilMainNet() throws Exception {
        return (NetworkMapItemModerator) nmiForNode(Rulebook.SYBIL_MOD_UID_MAIN);
    }

    public NetworkMapItemLead nmiForMyNodesLead() throws Exception{
        throw new UxException(ErrorMessages.INCORRECT_PARAMETERS, "Wallets do not have leads");
    }

    public NetworkMapItemModerator nmiForMyNodesMod() throws Exception{
        throw new UxException(ErrorMessages.INCORRECT_PARAMETERS, "Wallets do not have moderators");
    }

    protected NetworkMapItem findRandomModForLead(String lead) throws Exception {
        List<NetworkMapItem> hosts = findModeratorsForLead(lead);
        int size = hosts.size();
        int target = (int)(Math.random() * 1000000) % size;
        return hosts.get(target);

    }

    protected List<NetworkMapItem> findModeratorsForLead(String lead) throws Exception {
        return null;

    }

    public CacheContainer getCache() {
        return cache;
    }
}
