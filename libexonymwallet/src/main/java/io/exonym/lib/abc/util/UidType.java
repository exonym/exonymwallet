/*
 * Copyright (c) 2023. All Rights Reserved. Exonym GmbH
 */

package io.exonym.lib.abc.util;

import java.net.URI;

public class UidType {
	
	public static boolean isIssuerParameters(URI uid){
		return uid.toString().matches("\\p{ASCII}*:i"); 
		
	}
	public static boolean isIssuanceLog(URI uid){
		return uid.toString().matches("\\p{ASCII}*:il"); 
		
	}
	public static boolean isIssuancePolicy(URI uid){
		return uid.toString().matches("\\p{ASCII}*:ip"); 
		
	}
	public static boolean isIssuerSecret(URI uid){
		return uid.toString().matches("\\p{ASCII}*:is"); 
		
	}
	public static boolean isRegistrationParams(URI uid){
		return uid.toString().matches("\\p{ASCII}*:rp"); 
		
	}
	public static boolean isOwnerSecret(URI uid){
		return uid.toString().matches("\\p{ASCII}*:ss"); 
		
	}
	public static boolean isPresentationPolicy(URI uid){
		return uid.toString().matches("\\p{ASCII}*:pp"); 
		
	}
	public static boolean isPresentationPolicyAlternatives(URI uid){
		return uid.toString().matches("\\p{ASCII}*:ppa"); 
		
	}
	public static boolean isProofToken(URI uid){
		return uid.toString().matches("\\p{ASCII}*:t");
		
	}
	public static boolean isCredentialSpecification(URI uid){
		return uid.toString().matches("\\p{ASCII}*:c"); 
		
	}
	public static boolean isCredential(URI uid){
		return uid.toString().matches("\\p{ASCII}*:ic"); 
		
	}
	public static boolean isInspectorPublicKey(URI uid){
		return uid.toString().matches("\\p{ASCII}*:ins"); 
		
	}
	public static boolean isInspectorPrivateKey(URI uid){
		return uid.toString().matches("\\p{ASCII}*:inss"); 
		
	}
	public static boolean isRevocationAuthority(URI uid){
		return uid.toString().matches("\\p{ASCII}*:ra"); 
		
	}
	public static boolean isRevocationInformation(URI uid){
		return uid.toString().matches("\\p{ASCII}*:rai"); 
		
	}
	public static boolean isRevocationAuthorityPrivateKey(URI uid) {
		return uid.toString().matches("\\p{ASCII}*:ras");
		
	}
	public static boolean isRevocationHistory(URI uid) {
		return uid.toString().matches("\\p{ASCII}*:rh");
		
	}
}
