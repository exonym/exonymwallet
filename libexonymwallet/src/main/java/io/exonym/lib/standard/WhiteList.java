package io.exonym.lib.standard;

import io.exonym.lib.pojo.Namespace;

import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WhiteList {

	public static boolean isHex(String value){
		return value.matches("(0[xX])?[0-9a-fA-F]+");
	}
	
	public static boolean numbers(int length, String value){
		if (value==null){ 
			return false; 
			
		} 
		if (!value.matches("\\d{" + length + "}")){ 
			return false; 
			
		}
		return true; 
	}

	public static boolean isRulebookUid(URI uid){
		return isRulebookUid(uid.toString());

	}

	public static boolean isRulebookUid(String uid){
		if (uid==null){
			return false;

		}
		return uid.toString().matches(Namespace.URN_PREFIX_COLON + "[0-9a-f]{64}");

	}

	public static boolean isAdvocateUid(URI uid){
		return isAdvocateUid(uid.toString());
	}
	public static boolean isAdvocateUid(String uid){
		return uid!=null && uid.toString().matches(Namespace.URN_PREFIX_COLON + "[\\w-]*[:][\\w-]*[:][0-9a-f]{64}");

	}

	public static boolean isSourceUid(URI uid){
		return isSourceUid(uid.toString());
	}

	public static boolean isSourceUid(String uid){
		if (uid==null){
			return false;

		}
		return uid.toString().matches(Namespace.URN_PREFIX_COLON + "[\\w-]*[:][0-9a-f]{64}");

	}

	public static boolean numbers(String value){
		if (value==null){ 
			return false; 
			
		} 
		if (!value.matches("\\d")){ 
			return false; 
			
		}
		return true; 
	}

	
	public static boolean telNumbers(String value){
		if (value==null){ 
			return false; 
			
		} 
		if (!value.matches("[+0-9 ()]{9,}")){ 
			return false; 
			
		}
		return true; 
	}	
	
	public static boolean databaseUrl(String url){
		return (url==null ? null : url.matches("jdbc:mysql://[a-z_]*:[\\d]{4}/[a-z_]*"));
	}
	
	public static boolean url(String url){
		return (url==null ? null : url.matches("^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]"));
		
	}

	public static boolean path(String path){
		Pattern p = Pattern.compile("[A-Z:\\.]*[\\\\/][\\\\/\\w\\.-]*");
		Matcher m = p.matcher(path);
		return m.matches();
	}
	
	public static boolean email(String email){		
		// Note that this was true if the email was null.  That seems like a bug to me, so I changed it.
		return (email != null && email.matches("[\\w\\-\\._\\+]*@[\\w\\-\\._]*\\.[a-zA-Z\\.]*"));
		
	}
	

	public static boolean isIpV4(String ipPort) {
		return (ipPort != null && ipPort.matches("([01]?[0-9]?[0-9]|2[0-4][0-9]|25[0-5])\\."
				+ "([01]?[0-9]?[0-9]|2[0-4][0-9]|25[0-5])\\."
				+ "([01]?[0-9]?[0-9]|2[0-4][0-9]|25[0-5])\\."
				+ "([01]?[0-9]?[0-9]|2[0-4][0-9]|25[0-5])"));
		
	}
	
	public static boolean utcTime(String time){
		return time.matches("[0-9]{4}\\-[0-9]{2}\\-[0-9]{2}T[0-2][0-9]:[0-5][0-9]:[0-5][0-9][\\.0-9]*Z");
		
	}

	public static boolean isUuid(String uuid){
		return uuid.matches("[a-f0-9]{8}[-][a-f0-9]{4}[-][a-f0-9]{4}[-][a-f0-9]{4}[-][a-f0-9]{12}");

	}
	public static boolean username(String username){
		return (username==null ? null : username.matches("[\\w]{2,32}"));
		
	}
	
  	//returns true if the string contains of the required number of letters
	public static boolean isLetters(String s, int length){
		return (s==null ? false : s.matches("[a-zA-Z]{"+length+"}"));
		
	}
	
	public static boolean isMinLetters(String s, int length){
		return (s==null ? false : s.matches("[a-zA-Z]{"+length+",}"));
		
	}
	
	public static boolean isMinLettersAllowsNumbers(String s, int length){
		return (s==null ? false : s.matches("[\\w]{"+length+",}"));
		
	}	

	public static boolean isMinLettersAllowsNumbersAndHyphens(String s, int length){
		return (s==null ? false : s.matches("[\\w-\\/]{"+length+",}"));
		
	}	
	
	public static boolean isMinLettersAllowNumbersAllowSpaces(String s, int length){
		return (s==null ? false : s.matches("[\\w ]{"+length+",}"));
		
	}		
	
	public static boolean isMinLettersAllowSpaces(String s, int length){
		return (s==null ? false : s.matches("[a-zA-Z ]{"+length+",}"));
		
	}	
	
	public static boolean isMinMaxLetters(String s, int min, int max){
		return (s==null ? false : s.matches("[a-zA-Z]{" + min + ", " + max + "}"));
		
	}	
	
  	// returns true if the string matches exactly "true" or "True"
	// or "yes" or "Yes"
  	public static boolean isTrueOrYes(String s){
		return (s==null ? false : s.matches("[tT]rue|[yY]es"));
		
  	}

	  // returns true if the string contains exactly "true"
	public static boolean containsTrue(String s){
		  return (s==null ? false : s.matches(".*true.*"));
		  
	}
	
	public static boolean containsLetters(String s){
		  return (s==null ? false : s.matches(".*[a-zA-Z].*"));
		  
	}
	
	public static boolean containsUpperCaseLetters(String s){
		  return (s==null ? false : s.matches(".*[A-Z].*"));
		  
	}
	
	public static boolean containsLowerCaseLetters(String s){
		  return (s==null ? false : s.matches(".*[a-z].*"));
		  
	}

	public static boolean containsNumbers(String s){
		return (s==null ? false : s.matches(".*[\\d].*"));

	}

	public static boolean isNumbers(String s){
		return (s==null ? false : s.matches("[\\d].*"));

	}

  	// returns true if the string does not have a number at the beginning
	public static boolean isNoNumberAtBeginning(String s){
		return (s==null ? false : s.matches("^[^\\d].*"));
		
	}
}