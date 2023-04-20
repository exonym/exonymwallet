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

package com.ibm.zurich.idmx.tests;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.util.Scanner;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBIntrospector;

import com.ibm.zurich.idmx.exception.SerializationException;
import com.ibm.zurich.idmx.jaxb.JaxbHelperClass;

/**
 * 
 */
public class TestUtils {

  // Non-instantiable class
  private TestUtils() {}

  public static void deleteFilesInFolder(final File folder, final String inclusionPattern) {
    
    // Create the folders for the temporary files
    if (!folder.exists()) {
      folder.mkdirs();
    }

    // Get contained files exclusive those matching the pattern
    File[] files = folder.listFiles(new FilenameFilter() {
      @Override
      public boolean accept(File dir, String name) {
        return (inclusionPattern != null ? name.contains(inclusionPattern) : true);
      }
    });

    // Delete the files
    for (File f : files) {
      System.err.println("Delete: " + f.toURI());
      f.delete();
    }
  }

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
    System.out.println("Contents of file (" + filename + "): " + newLine + text);
  }

  public static void saveToFile(String object, String filename) throws IOException {

    File file = new File(URI.create(filename));
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
    //
    // InputStream inputStream;
    // inputStream = new FileInputStream(file);
    // Reader reader = new InputStreamReader(inputStream, "UTF-8");
    // StringBuilder stringBuilder = new StringBuilder();
    // BufferedReader bufferedReader = new BufferedReader(reader);
    // String string = bufferedReader.readLine();
    //
    // while (string != null) {
    // stringBuilder.append(string);
    // string = bufferedReader.readLine();
    // }
    // bufferedReader.close();
    //
    // return stringBuilder.toString();

    FileInputStream fis = null;
    try {
      fis = new FileInputStream(file);
    } catch (FileNotFoundException e) {
      return null;
    }

    StringBuilder text = new StringBuilder();
    String newLine = System.getProperty("line.separator");
    Scanner scanner = new Scanner(fis, "UTF-8");
    try {
      while (scanner.hasNextLine()) {
        text.append(scanner.nextLine() + newLine);
      }
    } finally {
      scanner.close();
    }
    return text.toString();
  }



  // TODO - replace the saveToFile method???
  // public static <T> boolean storeResource(String name, JAXBElement<T> resource,
  // Class<?> testClass, boolean validate) throws SerializationException {
  // Package pack = testClass.getPackage();
  // String packagePath = pack.getName().replaceAll("\\.", "/");
  // String path = "/" + packagePath + "/" + name;
  // // InputStream resource = classOfResource.getResourceAsStream(path);
  // JAXBElement<?> resourceAsJaxbElement = JaxbHelperClass.serialize(resource, validate);
  // Object resourceAsObject = resourceAsJaxbElement.getValue();
  // return (T) resourceAsObject;
  // }


  @SuppressWarnings("unchecked")
  public static <T> T getResource(String name, Class<T> classOfResource, Class<?> testClass,
      boolean validate) throws SerializationException {
    Package pack = testClass.getPackage();
    String packagePath = pack.getName().replaceAll("\\.", "/");
    String path = "/" + packagePath + "/" + name;
    InputStream resource = classOfResource.getResourceAsStream(path);
    JAXBElement<?> resourceAsJaxbElement = JaxbHelperClass.deserialize(resource, validate);
    Object resourceAsObject = JAXBIntrospector.getValue(resourceAsJaxbElement);
    return (T) resourceAsObject;
  }

  public static <T> T getResource(String name, Class<T> classOfResource, Class<?> testClass)
      throws SerializationException {
    return getResource(name, classOfResource, testClass, false);
  }

  public static <T> T getResource(String name, Class<T> classOfResource, Object testClassInstance)
      throws SerializationException {
    return getResource(name, classOfResource, testClassInstance, false);
  }

  public static <T> T getResource(String name, Class<T> classOfResource, Object testClassInstance,
      boolean validate) throws SerializationException {
    return getResource(name, classOfResource, testClassInstance.getClass(), validate);
  }
}
