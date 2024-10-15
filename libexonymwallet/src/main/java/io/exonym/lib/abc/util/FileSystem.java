/*
 * Copyright (c) 2023. All Rights Reserved. Exonym GmbH
 */

package io.exonym.lib.abc.util;

import com.ibm.zurich.idmix.abc4trust.XmlUtils;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import java.io.*;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Deprecated // This is awful and it needs to be rewritten.
public class FileSystem {
static Logger log = Logger.getLogger(FileSystem.class.getName());

public static File getFile(String filename, boolean wipe_existing_file) throws IOException {
  log.fine("getFile : " + filename + " - wipe : " + wipe_existing_file);

  URL file = FileSystem.class.getResource(filename);

  File f = null;
  if (file != null) {
    f = new File(file.getFile());
  } else {
    f = new File(filename);
  }
  if (f.exists() && !wipe_existing_file) {
    log.fine("getFile : " + filename + " - exists!");
    return f;
  } else {
    if (f.exists() && wipe_existing_file) {
      log.fine("file exits - wipe it! " + filename);
      f.delete();
    }

    File folder = f.getParentFile();
    if ((f.getParentFile() != null) && !folder.exists()) {
      log.fine("create folders : " + folder);
      folder.mkdirs();
    }
    log.fine("create new file : " + filename);
    boolean created = f.createNewFile();
    if (!created) {
      throw new IOException("Could not create new file : " + filename);
    }
    return f;
  }
}

public static void storeObjectInFile(Object object, String prefix, String name)
    throws IOException {
  File file = getFile(prefix + name, false);// new File(prefix + name);
  log.fine("Store Object " + object + " - in file " + file.getAbsolutePath());

  FileOutputStream fos = new FileOutputStream(file);
  ObjectOutputStream oos = new ObjectOutputStream(fos);

  oos.writeObject(object);
  fos.close();
}

public static void storeObjectInFile(Object object, String resourceName) throws IOException {
  File file = getFile(resourceName, false);
  log.fine("store Object " + object + " - in file " + file.getAbsolutePath());

  FileOutputStream fos = new FileOutputStream(file);
  ObjectOutputStream oos = new ObjectOutputStream(fos);

  oos.writeObject(object);
  fos.close();
}

public static void storeObjectAsXMLInFile(JAXBElement<?> element, String prefix,
    String resourceName) throws Exception {
  storeObjectAsXMLInFile(element, prefix + resourceName);
}

public static void storeObjectAsXMLInFile(JAXBElement<?> element, String resource)
    throws Exception {

  File file = new File(resource);
  log.fine("store Object: " + element + " - as XML in file " + file.getAbsolutePath());
  String normalizedXml = XmlUtils.toXml(element);
  FileOutputStream fos = new FileOutputStream(file);
  fos.write(normalizedXml.getBytes(Charset.forName("UTF-8")));
  fos.flush();
  fos.close();
}

public static InputStream getInputStream(String resource) throws IOException {
  InputStream is = FileSystem.class.getResourceAsStream(resource);
  if (is == null) {
    File f = new File(resource);
    if (!f.exists()) {
      throw new IOException("Resource not found :  " + resource);
    }

    is = new FileInputStream(f);
    
  }
  return is;
}

@SuppressWarnings("unchecked")
public static <T> T loadObjectFromResource(String name) throws IOException,
    ClassNotFoundException {
  log.fine("Load Object from Resource : " + name);
  InputStream is = getInputStream(name);
  ObjectInputStream ois = new ObjectInputStream(is);

  Object object = ois.readObject();
  ois.close();
  is.close();

  return (T) object;
}

@SuppressWarnings("unchecked")
public static <T> T loadXmlFromResource(String name) throws IOException, JAXBException,
    SAXException {
  log.info("Load Object from Resource : " + name);
  InputStream is = getInputStream(name);
  if (is != null) {
    JAXBElement<?> object = XmlUtils.getJaxbElementFromXml(is, true);

    return (T) object.getValue();
  } else {
    return null;
  }
}

@SuppressWarnings("unchecked")
public static <T> List<T> loadXmlListFromResources(String[] nameList) throws IOException,
    JAXBException, SAXException {
  List<T> list = new ArrayList<T>();
  for (String name : nameList) {
    T obj = loadXmlFromResource(name);
    list.add(obj);
  }
  return list;
}

//private static <T> T[] toArray(List<T> list) {
//  if(list.size()==0) {
//    return null;
//  }
//  T[] toR = (T[]) java.lang.reflect.Array.newInstance(list.get(0).getClass(), list.size());
//  for (int i = 0; i < list.size(); i++) {
//    toR[i] = list.get(i);
//  }
//  return toR;
//}

public static <T> List<T> findAndLoadXmlResourcesInDir(String folderName, final String... filter)
    throws IOException, JAXBException, SAXException {
  URL url = FileSystem.class.getResource(folderName);
  File folder = null;
  if (url != null) {
    folder = new File(url.getFile());
  } else {
    folder = new File(folderName);
  }
  return findAndLoadXmlResoucesInDir(folder, filter);
}

public static <T> List<T> findAndLoadXmlResoucesInDir(File folder, final String... filter)
    throws IOException, JAXBException, SAXException {
  String[] resources = localFindFilesInDir(folder, filter, ".xml");
  return loadXmlListFromResources(resources);
}

public static String[] findFilesInDir(String folderName, final String... filter) {
  URL url = FileSystem.class.getResource(folderName);
  File folder = null;
  if (url != null) {
    folder = new File(url.getFile());
  } else {
    folder = new File(folderName);
  }
  return findFilesInDir(folder, filter);
}

public static String[] findFilesInDir(File folder, final String... filter) {
  return localFindFilesInDir(folder, filter, null);
}

private static String[] localFindFilesInDir(File folder, final String[] filter,
    final String suffix) {

  String[] resourceList;
  File[] fileList = folder.listFiles(new FilenameFilter() {
    @Override
    public boolean accept(File arg0, String filename) {
      for (String f : filter) {
        if (filename.indexOf(f) == -1) {
          return false;
        }
        if (suffix != null && !filename.endsWith(suffix)) {
          return false;
        }
      }
      return true;
    }
  });
  if (fileList == null) {
    log.warning("Folder " + folder.getName()
        + " does not exist! \n Trying to continue without these resources");
    return new String[0];
  }

  resourceList = new String[fileList.length];
  for (int i = 0; i < fileList.length; i++) {
    resourceList[i] = fileList[i].getAbsolutePath();
  }
  return resourceList;
}

}
