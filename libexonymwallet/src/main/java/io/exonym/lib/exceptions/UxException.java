package io.exonym.lib.exceptions;


import java.util.ArrayList;

public class UxException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private ArrayList<String> info;

	public UxException(String msg) {
		super(msg);
	}

	public UxException(String msg, String... required) {
		super(msg);
		populateRequired(required);

	}

	public UxException(String msg, Throwable e, String... required) {
		super(msg, e);
		populateRequired(required);
	}

	private void populateRequired(String[] required) {
		info = new ArrayList<>();
		for (String a : required){
			info.add(a);
		}
	}

	public UxException(String msg, Throwable e){
		super(msg, e);
		
	}


	public boolean hasCause(){
		return this.getCause()!=null;
		
	}

	public ArrayList<String> getInfo() {
		return info;
	}

	@Override
	public synchronized Throwable initCause(Throwable arg0) {
		return super.initCause(arg0);
	}
	
	public static ArrayList<String> getStackAsString(Exception e) {
		ArrayList<String> result = new ArrayList<>();
		result.add(e.getMessage());
		StackTraceElement[] stes = e.getStackTrace();
		for (StackTraceElement ste : stes){
			result.add(ste.toString());

		}
		if (e.getCause()!=null){
			Throwable t = addCause(e.getCause(), result);
			if (t!=null){
				addCause(t, result);

			}
		}
		return result;

	}

	private static Throwable addCause(Throwable t, ArrayList<String> result) {
		result.add(t.getMessage());
		StackTraceElement[] stes = t.getStackTrace();
		int i=0;
		for (StackTraceElement ste : stes){
			result.add(ste.toString());
			i++;
			if (i>4){
				break;

			}
		}
		return t.getCause();

	}
}
