/*
 * Copyright (c) 2023. All Rights Reserved. Exonym GmbH
 */

package io.exonym.lib.abc.util;

public class FileType {
	
	public static boolean isXmlDocument(String fileName){
		return fileName.matches("\\p{ASCII}*\\.xml"); 
		
	}

	public static boolean isIssuerParameters(String fileName){
		return fileName.matches("\\p{ASCII}*\\.i\\.xml");

	}
	public static boolean isSftp(String fileName){
		return fileName.matches("\\p{ASCII}*\\.sftp\\.xml");

	}

	public static boolean isRulebook(String fileName){
		return fileName.matches("\\p{ASCII}*\\.json");

	}

	public static boolean isIssuanceLog(String fileName){
		return fileName.matches("\\p{ASCII}*\\.il\\.xml"); 
		
	}
	public static boolean isIssuancePolicy(String fileName){
		return fileName.matches("\\p{ASCII}*.ip\\.xml"); 
		
	}
	public static boolean isIssuerSecret(String fileName){
		return fileName.matches("\\p{ASCII}*\\.is\\.xml"); 
		
	}
	public static boolean isRegistrationParams(String fileName){
		return fileName.matches("\\p{ASCII}*\\.rp\\.xml"); 
		
	}
	public static boolean isConnectKeys(String fileName){
		return fileName.matches("\\p{ASCII}*\\.kc\\.xml"); 
		
	}
	public static boolean isKeys(String fileName){
		return fileName.matches("keys.xml"); 
		
	}
	public static boolean isDevices(String fileName){
		return fileName.matches("devices.xml"); 
		
	}
	public static boolean isAnonCredentialParameters(String fileName){
		return fileName.matches("\\p{ASCII}*\\.acp\\.xml"); 
		
	}
	public static boolean isMintedAnonCredential(String fileName){
		return fileName.matches("\\p{ASCII}*\\.mac\\.xml"); 
		
	}
	public static boolean isOwnerSecret(String fileName){
		return fileName.matches("\\p{ASCII}*\\.ss\\.xml"); 
		
	}
	public static boolean isGroup(String fileName){
		return fileName.matches("\\p{ASCII}*\\.gp\\.xml"); 
		
	}
	public static boolean isPresentationPolicy(String fileName){
		return fileName.matches("\\p{ASCII}*\\.pp\\.xml"); 
		
	}
	public static boolean isPresentationPolicyAlternatives(String fileName){
		return fileName.matches("\\p{ASCII}*\\.ppa\\.xml"); 
		
	}
	public static boolean isSignature(String fileName){
		return fileName.matches("\\p{ASCII}*signatures\\.xml"); 
		
	}
	public static boolean isProofToken(String fileName){
		return fileName.matches("\\p{ASCII}*\\.t\\.xml");
		
	}
	public static boolean isCredentialSpecification(String fileName){
		return fileName.matches("\\p{ASCII}*\\.c\\.xml"); 
		
	}
	public static boolean isCredential(String fileName){
		return fileName.matches("\\p{ASCII}*\\.ic\\.xml"); 
		
	}
	public static boolean isInspectorPublicKey(String fileName){
		return fileName.matches("\\p{ASCII}*\\.ins\\.xml"); 
		
	}
	public static boolean isInspectorPrivateKey(String fileName){
		return fileName.matches("\\p{ASCII}*\\.inss\\.xml"); 
		
	}
	public static boolean isRevocationAuthority(String fileName){
		return fileName.matches("\\p{ASCII}*\\.ra\\.xml"); 
		
	}
	public static boolean isRevocationInformation(String fileName){
		return fileName.matches("\\p{ASCII}*\\.rai\\.xml"); 
		
	}
	public static boolean isSystemParameters(String fileName){
		return fileName.matches("lambda.xml");
		
	}

	public static boolean isRevocationAuthorityPrivateKey(String fileName) {
		return fileName.matches("\\p{ASCII}*\\.ras\\.xml");
		
	}
	public static boolean isRevocationHistory(String fileName) {
		return fileName.matches("\\p{ASCII}*\\.rh\\.xml");
		
	}
}
