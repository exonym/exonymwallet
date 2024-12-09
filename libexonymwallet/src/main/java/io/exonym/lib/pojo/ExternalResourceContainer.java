package io.exonym.lib.pojo;

import eu.abc4trust.xml.*;
import io.exonym.lib.helpers.NamespaceMngt;
import io.exonym.lib.abc.util.FileType;
import io.exonym.lib.abc.util.IoMngt;
import io.exonym.lib.abc.util.JaxbHelper;
import io.exonym.lib.adapters.PresentationPolicyAlternativesAdapter;

import java.net.URI;
import java.nio.file.FileSystemException;
import java.nio.file.Paths;
import java.util.logging.Logger;

public abstract class ExternalResourceContainer {

	private final static Logger logger = Logger.getLogger(ExternalResourceContainer.class.getName());
	protected final URI ledger = NamespaceMngt.LEDGER;
	// private final String ledgerResource = "idmx/ledger/"; // "\\resource\\idmx\\ledger\\" 
	
	protected ExternalResourceContainer(){}

	@SuppressWarnings("unchecked")
	public synchronized <T> T openResource(String fileName) throws Exception{
		logger.info("Searching trust network for " + fileName);
		
		if (FileType.isIssuerParameters(fileName)){
			return (T) IoMngt.getResource(ledger.resolve(fileName).getPath(), IssuerParameters.class);
			
		} else if (FileType.isSystemParameters(fileName)){
			return (T) IoMngt.getResource(ledger.resolve(fileName).getPath(), SystemParameters.class);

		} else if (FileType.isPresentationPolicyAlternatives(fileName)){
			return (T) JaxbHelper.xmlFileToClass(Paths.get(ledger.resolve(fileName).getPath()), PresentationPolicyAlternativesAdapter.class);

		} else if (FileType.isCredentialSpecification(fileName)){
			return (T) IoMngt.getResource(ledger.resolve(fileName).getPath(), CredentialSpecification.class);

		} else if (FileType.isIssuancePolicy(fileName)){
			return (T) IoMngt.getResource(ledger.resolve(fileName).getPath(), IssuancePolicy.class);
			
		} else if (FileType.isProofToken(fileName)){ // Local
			return (T) IoMngt.getResource(ledger.resolve(fileName).getPath(), PresentationToken.class);
			
		} else if (FileType.isInspectorPublicKey(fileName)){
			return (T) IoMngt.getResource(ledger.resolve(fileName).getPath(), InspectorPublicKey.class);
			
		} else if (FileType.isRevocationAuthority(fileName)){
			return (T) IoMngt.getResource(ledger.resolve(fileName).getPath(), RevocationAuthorityParameters.class);

		} else if (FileType.isRevocationInformation(fileName)){
			return (T) IoMngt.getResource(ledger.resolve(fileName).getPath(), RevocationInformation.class);

		} else {
			throw new FileSystemException("File type not recognized " + fileName);
			
		}		
	}
	
	/**
	 * This is a stub that will eventually take, the XML, the UID and 
	 * the publication location written on the ledger to publish the materials.
	 * 
	 * @param xml
	 * @param fileName
	 * @throws Exception
	 * 
	 */
	public synchronized void publish(String xml, String fileName) throws Exception {
		IoMngt.saveToFile(xml, ledger.toString() + fileName, true);
		
	}
	
	protected String fileNameFromUid(URI groupUid) throws Exception {
		return IdContainer.uidToFileName(groupUid) + ".gp.xml";
		
	}
	
}