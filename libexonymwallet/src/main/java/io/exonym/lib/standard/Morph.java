/*
 * Copyright (c) 2023. All Rights Reserved. Exonym GmbH
 */

package io.exonym.lib.standard;


import java.io.*;
import java.util.logging.Logger;

public class Morph<T> {

	private final static Logger logger = Logger.getLogger(Morph.class.getName());

	public byte[] toByteArray(T t) throws Exception {
		if (t==null){
			throw new Exception("Null object for byte conversion");
			
		}
		ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
		ObjectOutput out = null;
		
		try{ 
			out = new ObjectOutputStream(byteOut);
			out.writeObject(t);
			return byteOut.toByteArray();
			
		} catch (Exception e){
			logger.throwing("Morph.class", "toByteArray()", e);
			return null;
			
		} finally {
			try {
				if (out!=null) {
					out.close();
					
				}
			} catch (Exception e){
				logger.throwing("Morph.class", "toByteArray()", e);
				
			}
			try {
				if (byteOut!=null) {
					byteOut.close();
					
				}
				
			} catch (Exception e2) {
				logger.throwing("Morph.class", "toByteArray()", e2);
			}
		}	
	}

	@SuppressWarnings("unchecked")
	public T construct(byte[] byteArray){
		if (byteArray==null){
			return null;
			
		}
		ByteArrayInputStream bis = new ByteArrayInputStream(byteArray);
		ObjectInput in = null;
		
		try {
			in = new ObjectInputStream(bis);
			return (T) in.readObject();

		}catch (Exception e){
			logger.throwing("Morph.class", "construct()", e);
			return null; 
			  
		} finally {
			try {
				if (bis!=null){ bis.close(); }
			} catch (Exception ex) {
				logger.throwing("Morph.class", "construct()", ex);
				
			}
			
			try {
				if (in!=null){ in.close(); }
			} catch (Exception ex) {
				logger.throwing("Morph.class", "construct()", ex);
				
			}
		}
	}

	
}
