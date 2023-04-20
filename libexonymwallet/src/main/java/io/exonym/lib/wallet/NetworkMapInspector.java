package io.exonym.lib.wallet;

import io.exonym.lib.exceptions.ErrorMessages;
import io.exonym.lib.exceptions.UxException;
import io.exonym.lib.helpers.UIDHelper;
import io.exonym.lib.pojo.*;
import io.exonym.lib.standard.WhiteList;
import io.exonym.lib.abc.util.JaxbHelper;
import io.exonym.lib.api.AbstractNetworkMap;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class NetworkMapInspector {

    private static Logger logger = Logger.getLogger(NetworkMapInspector.class.getName());
    private final AbstractNetworkMap map;


    NetworkMapInspector(AbstractNetworkMap map){
        this.map=map;

    }

    public String spawn() throws UxException {
        try {
            if (map.networkMapExists()){
                map.delete();

            }
            map.spawn();
            return "NETWORK_MAP_DELETED_AND_RESPAWNED";

        } catch (UxException e) {
            throw e;

        } catch (Exception e) {
            throw new UxException(ErrorMessages.SERVER_SIDE_PROGRAMMING_ERROR, e);

        }
    }

    public String viewActor(String uidString) throws UxException {
        try {
            URI uid= URI.create(uidString);

            if (WhiteList.isRulebookUid(uidString)){
                return viewBaseRulebook(uidString);

            } else if (WhiteList.isAdvocateUid(uidString)){
                return view(uid);

            } else if (WhiteList.isSourceUid(uidString)){
                return view(uid);

            } else {
                throw new UxException(ErrorMessages.INVALID_UID);

            }
        } catch (UxException e) {
            throw e;

        } catch (Exception e) {
            throw new UxException(ErrorMessages.SERVER_SIDE_PROGRAMMING_ERROR, e);

        }
    }


    private String viewBaseRulebook(String uid) throws Exception {
        try {
            Rulebook rulebook = this.map.getCache().open(XContainer.uidToFileName(uid) + ".json");
            rulebook.setPenalties(null);
            rulebook.setRuleExtensions(new ArrayList<>());

            ArrayList<String> sources = computeSourcesForRulebook(rulebook.getRulebookId());
            ActorOverview result = new ActorOverview();
            result.setRulebook(rulebook);
            result.setSourcesForRulebook(sources);
            return serializeResult(result);

        } catch (Exception e) {
            throw new UxException(ErrorMessages.FILE_NOT_FOUND, e);

        }
    }


    private ArrayList<String> computeSourcesForRulebook(String rulebookId) throws UxException {
        List<String> sources = map.getSourceFilenamesForRulebook(rulebookId);
        ArrayList<String> sourceUIDs = new ArrayList<>();
        for (String f : sources){
            sourceUIDs.add(map.fromNmiFilename(f).toString());

        }
        return sourceUIDs;

    }

    private String view(URI uid) throws Exception {
        NetworkMapItem item = map.nmiForNode(uid);
        String rulebookUID = UIDHelper.computeRulebookIdFromSourceUid(item.getSourceUID());
        ArrayList<String> sources = computeSourcesForRulebook(rulebookUID);

        Rulebook rulebook = map.getCache().open(rulebookUID);
        rulebook.setPenalties(null);
        rulebook.setRuleExtensions(new ArrayList<>());

        ActorOverview result = new ActorOverview();
        result.setRulebook(rulebook);
        result.setSourcesForRulebook(sources);
        result.setActor(item);
        return serializeResult(result);

    }


    private String serializeResult(ActorOverview result) throws Exception {
        return JaxbHelper.gson.toJson(result, ActorOverview.class);

    }

    public String listActors(String uidString) throws UxException {
        try {
            if (uidString==null){
                return listRulebooks();

            } else {
                URI uid= URI.create(uidString);
                return list(uid);

            }
        } catch (UxException e) {
            throw e;

        } catch (Exception e) {
            throw new UxException(ErrorMessages.SERVER_SIDE_PROGRAMMING_ERROR, e);

        }
    }

    private String listRulebooks() throws Exception {
        List<String> rulebooks = map.listRulebooks();
        List<String> result = new ArrayList<>();
        for (String r : rulebooks){
            result.add(Namespace.URN_PREFIX_COLON + r);
        }
        return JaxbHelper.gson.toJson(result, List.class);

    }

    private String list(URI uid) throws Exception {
        URI source = null;
        if (WhiteList.isSourceUid(uid)){
            source = uid;

        } else if (WhiteList.isAdvocateUid(uid)) {
            source = UIDHelper.computeSourceUidFromNodeUid(uid);

        } else if (WhiteList.isRulebookUid(uid)) {
            return JaxbHelper.gson.toJson(
                    computeSourcesForRulebook(uid.toString()));

        } else {
            throw new UxException(ErrorMessages.INCORRECT_PARAMETERS, "Not an Advocate or a Source UID");

        }
        NetworkMapItemSource s = (NetworkMapItemSource) map.nmiForNode(source);
        return JaxbHelper.gson.toJson(s, NetworkMapItemSource.class);

    }

    private String nmiForNode(String uid) throws Exception {
        if (WhiteList.isAdvocateUid(uid)){
            return JaxbHelper.serializeToJson(map.nmiForNode(URI.create(uid)), NetworkMapItemSource.class);

        } else if (WhiteList.isSourceUid(uid)){
            return JaxbHelper.serializeToJson(map.nmiForNode(URI.create(uid)), NetworkMapItemAdvocate.class);

        } else {
            throw new UxException(ErrorMessages.INVALID_UID, uid);

        }
    }

}
