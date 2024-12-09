package io.exonym.lib.actor;


import eu.abc4trust.xml.*;
import io.exonym.lib.helpers.UIDHelper;
import io.exonym.lib.helpers.XmlHelper;
import io.exonym.lib.abc.util.JaxbHelper;
import io.exonym.lib.helpers.UrlHelper;
import io.exonym.lib.exceptions.HubException;
import io.exonym.lib.exceptions.UxException;
import io.exonym.lib.pojo.*;
import io.exonym.lib.standard.AsymStoreKey;
import io.exonym.lib.api.RulebookVerifier;
import io.exonym.lib.helpers.Timing;
import io.exonym.lib.standard.Const;
import io.exonym.lib.standard.WhiteList;
import org.apache.commons.codec.binary.Base64;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

// TODO Node Transfer Protocol
public class NodeVerifier {

	private final static Logger logger = Logger.getLogger(NodeVerifier.class.getName());
	private KeyContainer rawKeys;
	private KeyContainerWrapper keys;

	private  ConcurrentHashMap<String, byte[]> byteContent;
	private  ConcurrentHashMap<String, byte[]> signatureBytes;
	private  ConcurrentHashMap<String, Object> contents;

	private TrustNetwork targetTrustNetwork = null;
	private XKey nodePublicKey = null;
	private String keyCheck = null;
	private PresentationPolicy presentationPolicy = null;
	private CredentialSpecification credentialSpecification = null;
	private InspectorPublicKey inspectorPublicKey = null;
	private Rulebook rulebook = null;

	private final HashMap<String, IssuerParameters> issuerParameterMap = new HashMap<String, IssuerParameters>();
	private final HashMap<String, RevocationAuthorityParameters> revocationAuthorityMap = new HashMap<String, RevocationAuthorityParameters>();
	private final HashMap<String, RevocationInformation> revocationInformationMap = new HashMap<String, RevocationInformation>();
	private final HashMap<String, PresentationToken> presentationTokenMap = new HashMap<String, PresentationToken>();

	private final String networkName;
	private URI nodeUrl;

	private long touched = Timing.currentTime();
	private final boolean amILead;

	private AsymStoreKey publicKey;


	/**
	 * To be used in conjunction with ping(), so the URL has already been established
	 *
	 * @param known
	 * @param isTargetLead, boolean amILead
	 * @return
	 * @throws Exception
	 */
	public static NodeVerifier openNode(URI known, boolean isTargetLead, boolean amILead) throws Exception {
		return new NodeVerifier(known, isTargetLead, amILead);

	}

	private NodeVerifier(URI node, boolean isTargetLead, boolean amILead) throws Exception {
		this.amILead =amILead;

		if (isTargetLead){
			if (!node.toString().contains(Const.LEAD)){
				throw new UxException("URL must be a Lead-URL " + node);

			}
		}
		this.nodeUrl = trainAtFolder(node);
		String[] parts = this.nodeUrl.toString().split("/");
		networkName = parts[parts.length-2];

		byteContent = XmlHelper.openXmlBytesAtUrl(this.nodeUrl);
		signatureBytes = computeBytesThatWereSigned(byteContent);
		contents = XmlHelper.deserializeOpenXml(byteContent);
		// this.ownTrustNetwork = openMyTrustNetwork();
		// flip to network map from openMyTrustNetwork();
		verification();

	}

	private ConcurrentHashMap<String, byte[]> computeBytesThatWereSigned(
			ConcurrentHashMap<String, byte[]> byteContent) throws UnsupportedEncodingException {
		ConcurrentHashMap<String, byte[]> result = new ConcurrentHashMap<>();
		for (String key : byteContent.keySet()){
			String s = new String(byteContent.get(key), "UTF8");
			String t = NodeVerifier.stripStringToSign(s);
			result.put(key, t.getBytes());

		}
		return result;

	}

	private NodeVerifier(URL url, KeyContainer keys, boolean amISource) throws Exception {
		try {
			networkName = null;

			this.amILead = amISource;
			byteContent = readLocalBytes(url, keys);
			signatureBytes = computeBytesThatWereSigned(byteContent);
			contents = XmlHelper.deserializeOpenXml(byteContent);
			// this.ownTrustNetwork = openMyTrustNetwork();
			// flip to network map from openMyTrustNetwork();

			verification();

		} catch (FileNotFoundException e){
			throw new HubException("The URL was likely incorrect " + url, e);

		} catch (Exception e){
			throw e;

		}
	}

	private ConcurrentHashMap<String, byte[]> readLocalBytes(URL url, KeyContainer keys) throws Exception {
		ConcurrentHashMap<String, byte[]> result = new ConcurrentHashMap<>();
		String sigXml = JaxbHelper.serializeToXml(keys, KeyContainer.class);
		result.put("signatures.xml", sigXml.getBytes());

		for (XKey key : keys.getKeyPairs()){
			if (key.getKeyUid().toString().startsWith(Namespace.URN_PREFIX_COLON)){
				String fn = IdContainer.uidToXmlFileName(key.getKeyUid());
				byte[] b = UrlHelper.read(new URL(url.toString() + "/" + fn));
				result.put(fn, b);

			} else {
				logger.fine("Opening materials and ignoring " + key.getKeyUid());

			}
		}
		return result;

	}

	public UIDHelper getUidHelperForMostRecentIssuerParameters() throws Exception {
		TrustNetworkWrapper tnw = new TrustNetworkWrapper(this.getTargetTrustNetwork());
		return new UIDHelper(tnw.getMostRecentIssuerParameters());

	}



	private void verification() throws Exception {
		try {
			rawKeys = (KeyContainer) contents.get("signatures.xml");
			keys = new KeyContainerWrapper(rawKeys);
			logger.info("Found keys and verifying PublicKey signature.");

			XKey x = keys.getKey(KeyContainerWrapper.TN_ROOT_KEY);

			this.keyCheck = Base64.encodeBase64String(x.getPublicKey());

			this.nodePublicKey = x;

			publicKey = AsymStoreKey.blank();
			publicKey.assembleKey(x.getPublicKey());

			// Verify Signature on Public Key
			verifySignature(x.getPublicKey(), publicKey, x.getSignature());
			verifyMaterialSignatures(keys.getKeyRingUids());

			updateObjects();
			verifyChecksum(keys.getKeyRingUids());

			verifyPublicKey(this.targetTrustNetwork.getNodeInformation().getNodeUid());
			byteContent.clear();
			contents.clear();

		} catch (Exception e) {
			throw e;

		}
	}

	private boolean verifyPublicKey(URI nodeUid) throws Exception {
		// if network map item exists, use that -- 9/3/23
		// otherwise try the source, if and only if the NMIS exists
		// otherwise build NMI

//		if (this.ownTrustNetwork!=null) {
//			NetworkParticipant ownRecord = this.ownTrustNetwork.getParticipant(nodeUid);
//			if (ownRecord!=null) {
//				String k = Base64.encodeBase64String(ownRecord.getPublicKey().getPublicKey());
//				if (k.equals(keyCheck)) {
//					logger.info("Verified there was no change in Public Key");
//					return true;
//
//				} else {
//					throw new SecurityException("XNode invalid - Public Key has changed");
//
//				}
//			} else {
//				logger.warn("Participant is unknown - this should only be seen during the addition of Nodes to the Network");
//				return false;
//
//			}
//		} else {
//			logger.info("Node is being established - no public keys are known");
//			return false;
//
//		}
		return false;
	}


	private boolean verifyChecksum(Set<URI> keyRingUids) throws Exception {
		String check = this.nodeUrl.toString();
		ArrayList<URI> tmp = new ArrayList<URI>(keyRingUids);
		Collections.sort(tmp);
		tmp.remove(KeyContainerWrapper.TN_ROOT_KEY);
		tmp.remove(KeyContainerWrapper.SIG_CHECKSUM);
		for (URI uid : tmp) {
			check += uid;

		}
		check = check.replaceAll("/", "");
		logger.fine("CHECK(VERIFY)\n\t\t " + check);
		XKey signature = this.keys.getKey(KeyContainerWrapper.SIG_CHECKSUM);
		if (publicKey.verifySignature(check.getBytes(), signature.getSignature())) {
			logger.info("Checksum Verified");
			return true;

		} else {
			throw new UxException("The XNode is invalid - These files do not belong on this domain, or a file was added or removed");

		}
	}

	private void updateObjects() throws Exception {
		for (String f : contents.keySet()){
			Object o = contents.get(f);
			if (o instanceof PresentationPolicy){
				presentationPolicy = ((PresentationPolicy) o);

			} else if (o instanceof CredentialSpecification){
				credentialSpecification = (CredentialSpecification) o;

			} else if (o instanceof InspectorPublicKey){
				inspectorPublicKey = (InspectorPublicKey) o;

			} else if (o instanceof IssuerParameters){
				this.issuerParameterMap.put(f, (IssuerParameters) o);

			} else if (o instanceof RevocationAuthorityParameters){
				this.revocationAuthorityMap.put(f, (RevocationAuthorityParameters) o);

			} else if (o instanceof RevocationInformation){
				this.revocationInformationMap.put(f, (RevocationInformation) o);

			} else if (o instanceof TrustNetwork){
				targetTrustNetwork = (TrustNetwork) o;
				this.nodeUrl = targetTrustNetwork.getNodeInformation().getStaticNodeUrl0();

			} else if (o instanceof PresentationToken){
				this.presentationTokenMap.put(f, (PresentationToken) o);

			} else if (o instanceof Rulebook){
				this.rulebook = new RulebookVerifier((Rulebook) o).getRulebook();

			} else if (o instanceof KeyContainer){
				// do nothing

			} else {
				throw new Exception("Unimplemented object type " + o);

			}
		}
	}

	private void verifyMaterialSignatures(Set<URI> keyRingUids) throws Exception {
		HashMap<XKey, byte[]> signatures = new HashMap<>();

		for (URI uid : keyRingUids){
			if (!uid.equals(KeyContainerWrapper.TN_ROOT_KEY) &&
					!uid.equals(KeyContainerWrapper.SIG_CHECKSUM)) {
				XKey sig = keys.getKey(uid);
				String fn = IdContainer.uidToXmlFileName(uid);
				byte[] b = signatureBytes.get(fn);
				signatures.put(sig, b);

			}
		}
		checkSignatures(publicKey, signatures);

	}

	public static void checkSignatures(AsymStoreKey key, HashMap<XKey, byte[]> signatures) throws Exception{
		if (signatures==null || key==null){
			throw new Exception("A required attribute was null key " + key + " signatures " + signatures);

		}
		for (XKey sig : signatures.keySet()){
			URI uid = sig.getKeyUid();
			logger.info("Verifying Signature of Resource " + uid);

			if (uid==null){
				throw new Exception("KeyUID was null");

			} if (sig.getSignature()==null){
				throw new Exception("Signature was null for KeyUid " + uid);

			}
			byte[] baf = signatures.get(sig);
			if (baf==null || baf==null){
				throw new Exception("Null raw data for file " + uid);

			}
			if (!verifySignature(baf, key, sig.getSignature())){
				throw new Exception("Signature was invalid for UID " + uid);

			} else {
				logger.info("Signature Verified for " + uid);

			}
		}
	}

	public static String stripStringToSign(String xml){
		return xml.replaceAll("\t", "")
				.replaceAll("\n", "")
				.replaceAll(" ", "")
				.replaceAll("\"", "");
	}

	public static boolean verifySignature(byte[] data, AsymStoreKey key, byte[] signature) throws Exception {
		if (data==null){
			throw new UxException("Data was null");

		} if (key==null){
			throw new UxException("Public key was null");

		} if (signature==null){
			throw new UxException("Signature was null");

		}
		if (key.verifySignature(data, signature)){
			logger.info("PublicKey Signature Verified");
			return true;

		} else {
			throw new UxException("Public Key Signature Verification Failed - the XNode is invalid");

		}
	}

	public static URI trainAtFolder(URI node) throws UxException {
		String f = node.toString();
		if (f.endsWith(Const.MODERATOR + "/") || f.endsWith(Const.LEAD + "/")){
			return node;

		} else if (f.endsWith(Const.MODERATOR) || f.endsWith(Const.LEAD)){
			return URI.create(node + "/");

		} else if (f.contains(Const.MODERATOR)){
			return URI.create(f.substring(0, f.indexOf(Const.MODERATOR)) + (Const.MODERATOR + "/"));

		} else if (f.contains(Const.LEAD)){
			return URI.create(f.substring(0, f.indexOf(Const.LEAD)) + Const.LEAD + "/");

		} else {
			throw new UxException("Node Verification Error.  An inspectable url ends with either `moderator` or `lead` (" + node + ")");

		}
	}

	public PresentationPolicyAlternatives getPresentationPolicyAlternatives() throws HubException {
		throw new HubException("You should not be using Presentation Policy Alternatives.  " +
				"Deprecated.  Moved to Presentation Policy :pp suffix files - check materials");

	}

	public PresentationPolicy getPresentationPolicy() {
		return presentationPolicy;
	}


	public CredentialSpecification getCredentialSpecification() throws HubException {
		if (credentialSpecification==null){
			throw new HubException("Credential Specification null");

		}
		return credentialSpecification;

	}

	public InspectorPublicKey getInspectorPublicKey() throws HubException {
		if (inspectorPublicKey==null){
			throw new HubException("InspectorPublicKey null");

		}
		return inspectorPublicKey;

	}

	public IssuerParameters getIssuerParameters(String fileName) throws HubException {
		return issuerParameterMap.get(fileName);

	}


	public Set<String> getIssuerParameterFileNames(){
		return issuerParameterMap.keySet();

	}


	public KeyContainer getRawKeys() {
		return rawKeys;
	}

	public RevocationAuthorityParameters getRevocationAuthorityParameters(String fileName) throws HubException {
		return revocationAuthorityMap.get(fileName);

	}

	public ArrayList<PresentationToken> getPresentationTokens() {
		return (ArrayList<PresentationToken>) this.presentationTokenMap.values();

	}

	public Set<String> getAllRevocationAuthorityFileNames(){
		return revocationAuthorityMap.keySet();

	}

	public RevocationInformation getRevocationInformation(String fileName) throws HubException {
		return revocationInformationMap.get(fileName);
	}

	public Set<String> getAllRevocationInformationFileNames(){
		return revocationInformationMap.keySet();

	}


	public TrustNetwork getTargetTrustNetwork() throws HubException {
		if (targetTrustNetwork ==null){
			throw new HubException("TrustNetwork is null");

		}
		return targetTrustNetwork;

	}

	public XKey getPublicKey() throws HubException {
		if (nodePublicKey==null){
			throw new HubException("PublicKey is null");

		}
		return nodePublicKey;

	}

	public String getNodeName() {
		return networkName;
	}

	public long getTouched() {
		return touched;
	}

	public void setTouched(long touched) {
		this.touched = touched;

	}

	public Rulebook getRulebook() {
		return rulebook;
	}

	public static void main(String[] args) throws Exception {
		logger.info("Hello " + WhiteList.isRulebookUid("800a5e641a0f9e6f2ba77c8a31384df2d034a969ccd565b8b9206fd4f8126296"));

	}

}
