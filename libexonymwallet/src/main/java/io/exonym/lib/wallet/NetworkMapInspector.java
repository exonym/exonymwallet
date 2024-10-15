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

            } else if (WhiteList.isModeratorUid(uidString)){
                return view(uid);

            } else if (WhiteList.isLeadUid(uidString)){
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
        String file = IdContainer.uidToFileName(uid) + ".json";
        try {
            Rulebook rulebook = this.map.getCache().open(file);
            rulebook.setPenalties(null);
            rulebook.setRuleExtensions(new ArrayList<>());

            ArrayList<String> sources = computeLeadsForRulebook(rulebook.getRulebookId());
            ActorOverview result = new ActorOverview();
            result.setRulebook(rulebook);
            result.setLeadsForRulebook(sources);
            return serializeResult(result);

        } catch (Exception e) {
            throw new UxException(ErrorMessages.FILE_NOT_FOUND + " " + file , e);

        }
    }


    private ArrayList<String> computeLeadsForRulebook(String rulebookId) throws UxException {
        List<String> sources = map.getLeadFileNamesForRulebook(rulebookId);
        ArrayList<String> leadsUids = new ArrayList<>();
        for (String f : sources){
            leadsUids.add(map.fromNmiFilename(f).toString());

        }
        return leadsUids;

    }

    private String view(URI uid) throws Exception {
        NetworkMapItem item = map.nmiForNode(uid);
        URI rulebookUid = UIDHelper.computeRulebookUidFromNodeUid(uid);
        ArrayList<String> leads = computeLeadsForRulebook(rulebookUid.toString());

        Rulebook rulebook = map.getCache().open(rulebookUid);

        rulebook.setPenalties(null);
        rulebook.setRuleExtensions(new ArrayList<>());

        ActorOverview result = new ActorOverview();
        result.setRulebook(rulebook);
        result.setLeadsForRulebook(leads);
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
        return JaxbHelper.gson.toJson(rulebooks, List.class);

    }

    private String list(URI uid) throws Exception {
        URI lead = null;
        if (WhiteList.isLeadUid(uid)){
            lead = uid;

        } else if (WhiteList.isModeratorUid(uid)) {
            lead = UIDHelper.computeLeadUidFromModUid(uid);

        } else if (WhiteList.isRulebookUid(uid)) {
            ArrayList<String> list = computeLeadsForRulebook(uid.toString());
            return JaxbHelper.gson.toJson(list);

        } else {
            throw new UxException(ErrorMessages.INCORRECT_PARAMETERS, "Neither a Moderator nor a Lead UID");

        }
        NetworkMapItemLead s = (NetworkMapItemLead) map.nmiForNode(lead);
        return JaxbHelper.gson.toJson(s, NetworkMapItemLead.class);

    }

    private String nmiForNode(String uid) throws Exception {
        if (WhiteList.isModeratorUid(uid)){
            return JaxbHelper.serializeToJson(map.nmiForNode(URI.create(uid)), NetworkMapItemLead.class);

        } else if (WhiteList.isLeadUid(uid)){
            return JaxbHelper.serializeToJson(map.nmiForNode(URI.create(uid)), NetworkMapItemModerator.class);

        } else {
            throw new UxException(ErrorMessages.INVALID_UID, uid);

        }
    }

}
