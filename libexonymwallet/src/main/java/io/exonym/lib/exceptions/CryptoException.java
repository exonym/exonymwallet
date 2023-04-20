package io.exonym.lib.exceptions;

import com.ibm.zurich.idmx.interfaces.util.BigInt;

import java.util.HashMap;

public class CryptoException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private HashMap<String, BigInt> parameterValueMap = new HashMap<>();
	
	public String getLoggerOutput(){
		String r = "";
		for (String param: parameterValueMap.keySet()){
			r += "\n" + param + "\t=\t" + parameterValueMap.get(param);
			
		}
		return r;
		
	}

	public HashMap<String, BigInt> getParameterValueMap() {
		return parameterValueMap;
	}
	
}

