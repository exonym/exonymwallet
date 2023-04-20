package io.exonym.lib.exceptions;

import java.util.ArrayList;

public class ExceptionCollection extends Exception {

	private static final long serialVersionUID = 44874248L;
	
	private final ArrayList<Exception> exceptions = new ArrayList<>();
	
	public void addException(Exception e){
		exceptions.add(e);
		
	}

	public ArrayList<Exception> getExceptions() {
		return exceptions;
	
	}

	public boolean isEmpty(){
		return exceptions.isEmpty();
		
	}
	
	public void addCause(Exception e){
		this.initCause(e);
		
	}
	
	public String getErrorReport(){
		if (exceptions.size()>1){
			String result = "\nThe following errors are present:\n";
			for (Exception e : exceptions){
				result += "\t" + e.getMessage() + "\n";
				
			}
			return result; 
			
		} else {
			return exceptions.get(0).getMessage(); 
			
		}
	}
}
