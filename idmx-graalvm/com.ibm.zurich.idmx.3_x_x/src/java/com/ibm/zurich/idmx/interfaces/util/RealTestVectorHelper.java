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

import javax.inject.Inject;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;


public class RealTestVectorHelper implements TestVectorHelper {

  private final BigIntFactory bigIntFactory;
  private final List<String> errors;
  private final Set<String> seen;
  private final Map<String, BigInt> numericalValues;
  private final Map<String, String> stringValues;
  private boolean isPresentation;

  @Inject
  public RealTestVectorHelper(BigIntFactory bigIntFactory) {
    this.bigIntFactory = bigIntFactory;
    this.errors = new ArrayList<String>();
    this.seen = new HashSet<String>();
    this.numericalValues = new HashMap<String, BigInt>();
    this.stringValues = new HashMap<String, String>();
  }

  /**
   * Loads a test vector from a resource file. You may call this method multiple times, the test
   * vector then consists of the concatenation of the two files. The file must consist of lines of
   * the form "name = value" (otherwise the line is ignored), where value is a number written in
   * base 16.
   * 
   * @param testClass
   */
  public void loadResource(final String resourceName, final Class<?> testClass) {
    Package pack = testClass.getPackage();
    String packagePath = pack.getName().replaceAll("\\.", "/");
    String path = "/" + packagePath + "/" + resourceName;
    InputStream resource = testClass.getResourceAsStream(path);
    int lineNumber = 0;
    try (Scanner sc = new Scanner(resource)) {
      while (sc.hasNext()) {
        lineNumber++;
        final String line = sc.nextLine();
        final StringTokenizer st = new StringTokenizer(line);
        try {
          final String name = st.nextToken();
          final String equal = st.nextToken();
          String value = null;
          if(st.hasMoreTokens()) {
            value = st.nextToken();
          }
          if (!equal.equals("=")) {
            System.err.println("Line " + lineNumber + " was ignored.");
            continue;
          }
          if (numericalValues.containsKey(name)) {
            System.err.println("Duplicate variable named: " + name + " at line " + lineNumber
                + ". Ignored.");
            continue;
          }
  
          if ( value!= null && value.matches("[0-9a-f]*") ) {
            final int basis = 16;
            final BigInteger value_bi = new BigInteger(value, basis);
            final BigInt value_bigInt = bigIntFactory.valueOf(value_bi);
            numericalValues.put(name, value_bigInt);
          } else {
            stringValues.put(name, value);
          }
        } catch (final NoSuchElementException e) {
          System.err.println("Line " + lineNumber + " was ignored.");
          continue;
        }
      }
      sc.close();
    }
    catch(Exception ex) {
      
    }
    
  }
  
  public void ignoreValuesRegex(final String regex) {
    final Set<String> names = new TreeSet<String>(numericalValues.keySet());
    names.addAll(stringValues.keySet());
    for(final String name: names) {
      if(name.matches(regex)) {
        System.out.println("Ignoring " + name + ".");
        seen.add(name);
      }
    }
  }

  @Override
  public boolean valueExists(final String valueName) {
    return numericalValues.containsKey(valueName);
  }

  @Override
  public boolean isActive() {
    return !numericalValues.isEmpty();
  }

  @Override
  public BigInt getValueAsBigInt(final String valueName) {
    if(!isActive()) {
      throw new RuntimeException("Test Vector is not active");
    }
    BigInt value = numericalValues.get(valueName);
    seen.add(valueName);
    return value;
  }

  @Override
  public String getValueAsString(final String valueName) {
    if(!isActive()) {
      throw new RuntimeException("Test Vector is not active");
    }
    final String value = stringValues.get(valueName);
    seen.add(valueName);
    return value;
  }

  @Override
  public byte[] getValueAsBytes(final String valueName) {
    if(!isActive()) {
      throw new RuntimeException("Test Vector is not active");
    }
    return getValueAsBigInt(valueName).toByteArrayUnsigned();
  }

  @Override
  public void checkValue(final BigInt value, final String valueName) {
    if(!isActive()) {
      return;
    }
    final BigInt expected = getValueAsBigInt(valueName);
    if (!expected.equals(value)) {
      String error =
          "Value " + valueName + " was re-computed incorrectly. Expected " + expected
              + " actual = " + value + ".";
      System.err.println(error);
      errors.add(error);
    }
  }

  @Override
  public void checkValue(final byte[] value, final String valueName) {
    if(!isActive()) {
      return;
    }
    final byte[] expected = getValueAsBytes(valueName);
    if (!Arrays.equals(expected, value)) {
      final String error =
          "Value " + valueName + " was re-computed incorrectly. Expected "
              + Arrays.toString(expected) + " actual = " + Arrays.toString(value) + ".";
      System.err.println(error);
      errors.add(error);
    }
  }

  @Override
  public boolean finalizeTest() {
    boolean ok = true;
    if (!errors.isEmpty()) {
      ok = false;
      System.err.println("Errors in test vectors:");
      for (final String error : errors) {
        System.err.println("- " + error);
      }
    }

    if (seen.size() != numericalValues.size() + stringValues.size()) {
      ok = false;
      final Set<String> notSeen = new TreeSet<String>(numericalValues.keySet());
      notSeen.addAll(stringValues.keySet());
      notSeen.removeAll(seen);
      System.err.println("Values that where not touched in test vector:");
      for (final String s : notSeen) {
        System.err.println("- " + s);
      }
      System.err.println("Values seen but not in the test vector:");
      notSeen.clear();
      notSeen.addAll(seen);
      notSeen.removeAll(numericalValues.keySet());
      notSeen.removeAll(stringValues.keySet());
      for (final String s : notSeen) {
        System.err.println("- " + s);
      }
    }

    return ok;
  }

  @Override
  public boolean isPresentation() {
    return isPresentation;
  }
  
  public void setPresentation(final boolean v) {
    isPresentation = v;
  }

}
