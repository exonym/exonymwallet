//* Licensed Materials - Property of IBM                                     *
//* com.ibm.zurich.idmx.3_x_x                                                *
//* (C) Copyright IBM Corp. 2015. All Rights Reserved.                       *
//* US Government Users Restricted Rights - Use, duplication or              *
//* disclosure restricted by GSA ADP Schedule Contract with IBM Corp.        *
//*                                                                          *
//* The contents of this file are subject to the terms of either the         *
//* International License Agreement for Identity Mixer Version 1.2 or the    *
//* Apache License Version 2.0.                                              *
//*                                                                          *
//* The license terms can be found in the file LICENSE.txt that is provided  *
//* together with this software.                                             *
//*/**/***********************************************************************

package io.exonym.lib.abc.util;

import com.ibm.zurich.idmx.jaxb.JaxbHelperClass;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBIntrospector;
import java.io.*;
import java.net.URI;
import java.nio.file.FileAlreadyExistsException;
import java.util.Scanner;
import java.util.logging.Logger;

/**
 * 
 */
public class IoMngt {

	private final static Logger logger = Logger.getLogger(IoMngt.class.getName());
	// Non-instantiable class
  private IoMngt() {}

  public static void print(String filename) throws IOException {
    // FileInputStream fis = null;
    // try {
    // fis = new FileInputStream(filename);
    // } catch (FileNotFoundException e) {
    // // TODO Auto-generated catch block
    // e.printStackTrace();
    // }
    //
    // StringBuilder text = new StringBuilder();
    String newLine = System.getProperty("line.separator");
    // Scanner scanner = new Scanner(fis, "UTF-8");
    // try {
    // while (scanner.hasNextLine()) {
    // text.append(scanner.nextLine() + newLine);
    // }
    // } finally {
    // scanner.close();
    // }

    String text = loadFromFile(filename);
    logger.info("Contents of file (" + filename + "): " + newLine + text);
  }

  public static void saveToFile(String object, String filename, boolean overwrite) throws Exception {
	logger.info("saving:  " + filename);
    File file = new File(URI.create(filename));
    if (!overwrite){
    	if (file.exists()){
          	throw new FileAlreadyExistsException("The file already exists " + filename);
          	
        }
    }
    if (!file.getParentFile().exists()) {
        file.getParentFile().mkdirs();
        
    } 
    OutputStream outputStream;
    outputStream = new FileOutputStream(file);
    OutputStreamWriter out = new OutputStreamWriter(outputStream, "UTF-8");
    out.write(object);
    out.flush();
    out.close();
    
  }

  public static String loadFromFile(String filename) throws IOException {
	  File file = new File(URI.create(filename));

	  FileInputStream fis = null;
	  try {
		  fis = new FileInputStream(file);
      
	  } catch (FileNotFoundException e) {
		  return null;
      
	  }
	  StringBuilder text = new StringBuilder();
	  String newLine = System.getProperty("line.separator");
    
	  try (Scanner scanner = new Scanner(fis, "UTF-8")){
		  while (scanner.hasNextLine()) {
			  text.append(scanner.nextLine() + newLine);
        
		  }
	  } catch (Exception e){
		  throw e; 
		  
	  }
	  return text.toString();
    
  }

	@SuppressWarnings("unchecked")
	public static <T> T getResource(String fileName, ClassLoader classLoader) throws Exception {
		InputStream resource = classLoader.getResourceAsStream(fileName);

		if (resource!=null){
		    JAXBElement<?> resourceAsJaxbElement = JaxbHelperClass.deserialize(resource, true);
		    return (T)JAXBIntrospector.getValue(resourceAsJaxbElement);
			
		} else {
			throw new Exception("Resource not found " + fileName);
			
		}
	}	
	
	@SuppressWarnings("unchecked")
	public static <T> T getResource(String fileName, Class<?> clazz) throws Exception {
		try (FileInputStream fis = new FileInputStream(new File(fileName))){
			JAXBElement<?> resourceAsJaxbElement = JaxbHelperClass.deserialize(fis, true);
			return (T)JAXBIntrospector.getValue(resourceAsJaxbElement);
			
		} catch (Exception e) {
			throw e; 
			
		}
	}	
	
	public static JAXBElement<?> getResourceAsJaxbElement(String fileName, Class<?> clazz) throws Exception {
		try (FileInputStream fis = new FileInputStream(new File(fileName))){
			return JaxbHelperClass.deserialize(fis, true);
			
		} catch (Exception e) {
			throw e; 
			
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T getResource(InputStream  resource) throws Exception {

		if (resource!=null){
		    JAXBElement<?> resourceAsJaxbElement = JaxbHelperClass.deserialize(resource, true);
		    return (T)JAXBIntrospector.getValue(resourceAsJaxbElement);
			
		} else {
			throw new Exception("Resource not found " );
			
		}
	}

}
