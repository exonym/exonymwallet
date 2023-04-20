/*
 * Copyright (c) 2023. All Rights Reserved. Exonym GmbH
 */
package io.exonym.lib.abc.util;

import java.io.*;

public class ByteSerializer {
  public static Object readFromBytes(byte[] item) {
    if (item == null) {
      return null;
    }
    try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(item);) {
      try (ObjectInputStream objectInput = new ObjectInputStream(byteArrayInputStream);) {
        return objectInput.readObject();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  public static  byte[] writeAsBytes(Object o) {
    try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();) {
      try (ObjectOutputStream objectOutput = new ObjectOutputStream(byteArrayOutputStream);) {
        objectOutput.writeObject(o);
        return byteArrayOutputStream.toByteArray();
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
