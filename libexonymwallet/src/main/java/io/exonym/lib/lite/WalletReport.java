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
        HashSet<URI> advocateUids = new HashSet<>();
        HashMap<URI,URI> advocateToIssuer = new HashMap<>();

        for (URI issuer : issuers){
            URI aUid = UIDHelper.computeAdvocateUidFromMaterialUID(issuer);
            advocateUids.add(aUid);
            advocateToIssuer.put(aUid, issuer);

        }
        advocateUids.removeAll(required.getAdvocateBlacklist());
        ArrayList<URI> sourceBlacklist = required.getSourceBlacklist();
        HashSet<URI> safeAdvocates = new HashSet<>();
        for (URI advocate : advocateUids){
            URI source = UIDHelper.computeSourceUidFromNodeUid(advocate);
            if (!sourceBlacklist.contains(source)){
                safeAdvocates.add(advocate);
            }
        }
        HashSet<URI> safeIssuers = new HashSet<>();
        for (URI advocate : safeAdvocates){
            safeIssuers.add(advocateToIssuer.get(advocate));
        }
        return safeIssuers;

    }

    public boolean isEmpty(){
        return this.rulebooksToIssuers.isEmpty();
    }

    public boolean hasSybil(){
        HashSet<URI> sybil = rulebooksToIssuers.get(Rulebook.SYBIL_RULEBOOK_ID.toString());
        return (sybil!=null && sybil.size()>0);


    }

    public HashSet<String> getSftpAccess() {
        return sftpAccess;
    }

    public void setSftpAccess(HashSet<String> sftpAccess) {
        this.sftpAccess = sftpAccess;
    }
}
