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
package com.ibm.zurich.idmx.interfaces.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

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
