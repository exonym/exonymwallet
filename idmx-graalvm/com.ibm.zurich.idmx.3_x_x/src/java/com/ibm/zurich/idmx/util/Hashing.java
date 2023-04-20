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
package com.ibm.zurich.idmx.util;

import java.math.BigInteger;
//import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import com.ibm.zurich.idmx.buildingBlock.systemParameters.EcryptSystemParametersWrapper;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.interfaces.util.BigInt;

public class Hashing {

  private final MessageDigest md;

  public Hashing(final EcryptSystemParametersWrapper sp) throws ConfigurationException {
    try {
      md = MessageDigest.getInstance(sp.getHashFunction());
    } catch (final NoSuchAlgorithmException e) {
      throw new ConfigurationException(e);
    }
  }

  public Hashing(final String hashAlgorithm) throws NoSuchAlgorithmException {
    md = MessageDigest.getInstance(hashAlgorithm);
  }

  public void addInteger(final int a) {
    if (a < 0) {
      throw new ArithmeticException("cannot call hashInteger with negative number");
    }
    final BigInteger bi = BigInteger.valueOf(a);
    final byte[] bytes = bi.toByteArray();
    final byte[] toHash = new byte[4];
    //bytes = ByteBuffer.allocate(4).putInt(a).array();
    System.arraycopy(bytes, 0, toHash, 4 - bytes.length, bytes.length);

    md.update(toHash);
  }
  
  public void addByte(final byte b) {
    md.update(b);
  }

  public void add(final byte[] str) {
    if (str == null) {
      addNull();
    } else {
      addInteger(str.length);
      md.update(str);
    }
  }

  public void addNull() {
    addInteger(0);
  }

  public void addListBytes(final List<byte[]> list) {
    addInteger(list.size());
    for (final byte[] o : list) {
    	add(o);
    }
  }
  
  public void addListSignedIntegers(final List<BigInt> list) {
    addInteger(list.size());
    for (final BigInt o : list) {
      if (o == null) {
        addNull();
      } else {
        add(o.toByteArray());
      }
    }
  }

  public byte[] digestRaw() {
    return md.digest();
  }
}
