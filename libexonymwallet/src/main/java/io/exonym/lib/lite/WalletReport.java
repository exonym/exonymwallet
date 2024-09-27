package io.exonym.lib.lite;

import io.exonym.lib.helpers.UIDHelper;
import io.exonym.lib.pojo.Rulebook;
import io.exonym.lib.pojo.RulebookAuth;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

public class WalletReport {

    private final HashMap<String, HashSet<URI>> rulebooksToIssuers = new HashMap<>();
    private HashSet<String> sftpAccess = new HashSet<>();

    public HashMap<String, HashSet<URI>> getRulebooksToIssuers() {
        return rulebooksToIssuers;
    }


    public void add(String rulebookId, URI issuerUID){
        HashSet<URI> issuerList = rulebooksToIssuers.get(rulebookId);
        if (issuerList==null){
            issuerList = new HashSet<>();
            rulebooksToIssuers.put(rulebookId, issuerList);
        }
        issuerList.add(issuerUID);

    }

    public void add(String rulebookId, Collection<URI> issuerUID){
        HashSet<URI> issuerList = rulebooksToIssuers.get(rulebookId);
        if (issuerList==null){
            issuerList = new HashSet<>();
            rulebooksToIssuers.put(rulebookId, issuerList);
        }
        issuerList.addAll(issuerUID);

    }

    public static HashSet<URI> safeList(HashSet<URI> issuers, RulebookAuth required) throws Exception {
        HashSet<URI> mods = new HashSet<>();
        HashMap<URI,URI> modToIssuer = new HashMap<>();

        for (URI issuer : issuers){
            URI aUid = UIDHelper.computeModUidFromMaterialUID(issuer);
            mods.add(aUid);
            modToIssuer.put(aUid, issuer);

        }
        mods.removeAll(required.getModBlacklist());
        ArrayList<URI> leadBlacklist = required.getLeadBlacklist();
        HashSet<URI> safeMods = new HashSet<>();
        for (URI mod : mods){
            URI lead = UIDHelper.computeLeadUidFromModUid(mod);
            if (!leadBlacklist.contains(lead)){
                safeMods.add(mod);
            }
        }
        HashSet<URI> safeIssuers = new HashSet<>();
        for (URI mod : safeMods){
            safeIssuers.add(modToIssuer.get(mod));
        }
        return safeIssuers;

    }

    public boolean isEmpty(){
        return this.rulebooksToIssuers.isEmpty();
    }

    public boolean hasSybil(){
        HashSet<URI> sybil = rulebooksToIssuers.get(Rulebook.SYBIL_RULEBOOK_UID_TEST.toString());
        return (sybil!=null && sybil.size()>0);


    }

    public HashSet<String> getSftpAccess() {
        return sftpAccess;
    }

    public void setSftpAccess(HashSet<String> sftpAccess) {
        this.sftpAccess = sftpAccess;
    }
}
