package io.exonym.lib.api;

import io.exonym.lib.abc.util.FileType;
import io.exonym.lib.actor.NodeVerifier;
import io.exonym.lib.actor.TrustNetworkWrapper;
import io.exonym.lib.exceptions.ErrorMessages;
import io.exonym.lib.exceptions.UxException;
import io.exonym.lib.helpers.BuildCredentialSpecification;
import io.exonym.lib.helpers.UIDHelper;
import io.exonym.lib.pojo.ExternalResourceContainer;
import io.exonym.lib.pojo.NetworkMapItem;
import io.exonym.lib.pojo.Rulebook;

import java.net.URI;
import java.net.URL;
import java.util.logging.Logger;

public final class PkiExternalResourceContainer extends ExternalResourceContainer {
	private final static Logger logger = Logger.getLogger(PkiExternalResourceContainer.class.getName());
	private static PkiExternalResourceContainer instance = null;
	private CacheContainer cache = null;
	private AbstractNetworkMap networkMap;

	static {
		if (instance==null){
			instance = new PkiExternalResourceContainer();
			logger.info("External Resource Manager Instantiated");

		}
	}

	public synchronized static PkiExternalResourceContainer getInstance(){
		return instance;

	}

	public void close() throws Exception {
		instance = null;

	}

	public synchronized <T> T openResource(String fileName) throws Exception {
		return openResource(fileName, false);
	}

	public synchronized <T> T openResource(String fileName, boolean overrideCache) throws Exception {
		if (fileName == null) {
			throw new NullPointerException("File Name");

		}
		T t = null;
		if (!overrideCache) {
			try {
				t = this.getCache().open(fileName);

			} catch (Exception e) {
				logger.throwing("PkiExternalResourceContainer.class", "openResource()", e);
				logger.info("Could not find the file on the local cache");

			}
		}
		if (t == null) {
			if (FileType.isPresentationPolicy(fileName) ||
					FileType.isCredentialSpecification(fileName)) {
				return verifySource(fileName);

			} else if (FileType.isInspectorPublicKey(fileName) ||
					FileType.isRevocationInformation(fileName) ||
					FileType.isRevocationAuthority(fileName) ||
					FileType.isIssuerParameters(fileName)) {
				return verifyAdvocate(fileName);

			} else if (FileType.isRulebook(fileName)){
				throw new UxException("CANNOT_OPEN_RULEBOOK_EXTERNALLY");

			} else {
				throw new UxException(ErrorMessages.INCORRECT_PARAMETERS,
						"No such file",
						fileName
				);
			}
		}
		return t;
	}

	private <T> T verifySource(String fileName) throws Exception {
		if (FileType.isCredentialSpecification(fileName)){
			if (Rulebook.isSybilMain(fileName)){
				RulebookVerifier verifier = new RulebookVerifier(new URL(Rulebook.SYBIL_URL_MAIN));
				return (T) BuildCredentialSpecification.buildSybilCredentialSpecification(verifier);

			} else if (Rulebook.isSybilTest(fileName)){
				RulebookVerifier verifier = new RulebookVerifier(new URL(Rulebook.SYBIL_URL_TEST));
				return (T) BuildCredentialSpecification.buildSybilCredentialSpecification(verifier);

			} else {
				return (T) new BuildCredentialSpecification(
						UIDHelper.fileNameToUid(fileName), true)
						.getCredentialSpecification();
			}
		} else {
			URI sourceUID = UIDHelper.computeLeadUidFromModUid(UIDHelper.fileNameToUid(fileName));
			NetworkMapItem nmi = getNetworkMap().nmiForNode(sourceUID);
			NodeVerifier sourceVerifier = NodeVerifier.openNode(nmi.getStaticURL0(), true, false);
			CacheContainer cache = this.getCache();
			cache.store(sourceVerifier.getPresentationPolicy());
			cache.store(sourceVerifier.getCredentialSpecification());
			cache.store(sourceVerifier.getRulebook());
			if (FileType.isPresentationPolicy(fileName)) {
				return (T) sourceVerifier.getPresentationPolicy();
			} else if (FileType.isRulebook(fileName)){
				return (T) sourceVerifier.getRulebook();
			} else {
				throw new UxException(ErrorMessages.FILE_NOT_FOUND, fileName);

			}
		}
	}

	public CacheContainer getCache() {
		if (cache==null){
			throw new RuntimeException("You should set the cache and the network map on start of the node");

		}
		return cache;
	}

	public AbstractNetworkMap getNetworkMap() {
		if (networkMap==null){
			throw new RuntimeException("You should set the cache and the network map on start of the node");

		}
		return networkMap;
	}

	private <T> T verifyAdvocate(String fileName) throws Exception {
		URI searchingFor = UIDHelper.fileNameToUid(fileName);
		URI advocateUID = UIDHelper.computeModUidFromMaterialUID(searchingFor);
		NetworkMapItem nmi = getNetworkMap().nmiForNode(advocateUID);
		NodeVerifier advocateVerifier = NodeVerifier.openNode(nmi.getStaticURL0(), false, false);

		CacheContainer cache = this.getCache();
		TrustNetworkWrapper tnw = new TrustNetworkWrapper(advocateVerifier.getTargetTrustNetwork());
		URI issuerUID = tnw.getMostRecentIssuerParameters();
		UIDHelper helper = new UIDHelper(issuerUID);
		cache.store(advocateVerifier.getIssuerParameters(helper.getIssuerParametersFileName()));
		cache.store(advocateVerifier.getInspectorPublicKey());
		cache.store(advocateVerifier.getRevocationAuthorityParameters(helper.getRevocationAuthorityFileName()));
		cache.store(advocateVerifier.getRevocationInformation(helper.getRevocationInformationFileName()));
		return cache.open(fileName);

	}

	public void setNetworkMapAndCache(AbstractNetworkMap nmi, CacheContainer cache){
		this.networkMap = nmi;
		this.cache = cache;

	}

}
