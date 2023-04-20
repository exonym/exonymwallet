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


/**
 * Interface for retrieving values from a test vector, and checking values against it.
 */
public interface TestVectorHelper {
  
  /**
   * Returns true if a test is currently in progress, and false otherwise.
   * If this method returns false, then you MUST NOT call any of the get methods of this interface.
   * @return
   */
  boolean isActive();
  
  /**
   * Returns true if a presentation is currently in progress, and false otherwise.
   * @return
   */
  boolean isPresentation();
  
  /**
   * Recover a value from the test vectors (as an integer).
   * @param valueName
   * @return
   */
  BigInt getValueAsBigInt(String valueName);
  
  /**
   * Recover a value from the test vectors (as a byte array).
   * @param valueName
   * @return
   */
  byte[] getValueAsBytes(String valueName);
  
  /**
   * Check a value against the test vectors.
   * If the value doesn't match, print an error message to standard error; the finalizeTest
   * function will then return false.
   * @param value
   * @param valueName
   */
  void checkValue(BigInt value, String valueName);
  
  /**
   * Check a value against the test vectors.
   * If the value doesn't match, print an error message to standard error; the finalizeTest
   * function will then return false.
   * @param value
   * @param valueName
   */ 
  void checkValue(byte[] value, String valueName);
  
  /**
   * Return true if all checks have passed.
   * @return
   */
  boolean finalizeTest();

  /**
   * Recover a value from the test vectors (as a string).
   * @param valueName
   * @return
   */
  String getValueAsString(String valueName);

  /**
   * Returns true if the given value is in the test vector.
   * @param valueName
   * @return
   */
  boolean valueExists(String valueName);
}
