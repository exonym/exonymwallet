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

package com.ibm.zurich.idmx.configuration;



/**
 * 
 */
public class ErrorMessages {

  private ErrorMessages() {
    throw new AssertionError(nonInstantiationErrorMessage());
  }

  private static String getIdmxIdentifier() {
    return "Idmx: ";
  }

  /**
   * Should be used for classes that are not meant to be instantiated (e.g., static classes).
   * 
   * @return
   */
  public static String nonInstantiationErrorMessage() {
    return getIdmxIdentifier() + "Class must not be instantiated.";
  }

  /**
   * Should be used when an element cannot be cast as it should be (usually checked with instanceof
   * or isAssignableFrom).
   * 
   * @param nonAssignableClass
   * @return
   */
  public static String nonAssignableErrorMessage(final Class<?> nonAssignableClass) {
    return getIdmxIdentifier() + "Current class cannot be assigned from "
        + nonAssignableClass.getSimpleName() + ".";
  }

  /**
   * Should be used when verifying that the element ids match (e.g., the system parameters ids in
   * two different files).
   */
  public static String elementIdsMismatch(final String elementName) {
    return getIdmxIdentifier() + "There is a mismatch of the \"" + elementName
        + "\" elements. The system cannot function with these different identifiers.";
  }

  /**
   * Should be used when a parameter with the given name cannot be found in the list of parameters.
   * 
   * @param parameterName
   * @return
   */
  public static String parameterNotFound(final String parameterName) {
    return getIdmxIdentifier() + "Parameter \"" + parameterName + "\" cannot be found "
        + "in the list of parameters. This may be caused by loading a wrong element "
        + "(e.g., a private key as a public key).";
  }

  /**
   * Should be used when a parameter does not meet the required conditions (e.g., is not prime).
   */
  public static String parameterWrong(final String message) {
    return getIdmxIdentifier() + "Parameter does not meet a requirement: " + message;
  }

  /**
   * @return
   */
  public static String technologyNotSupported() {
    return getIdmxIdentifier() + "Technology for creating issuer parameters " + "is not supported.";
  }

  /**
   * @return
   */
  public static String defaultSystemParametersGeneratorNotConfigured() {
    return getIdmxIdentifier() + "The default system parameters generation class is "
        + "not properly assigned.";
  }

  /**
   * Should be used when a class is cast but the expected class is not assignable to the actual
   * class.
   * 
   * @param expectedSuperClass
   * @param actualClass
   * @return
   */
  public static String classCastMessage(final Class<?> expectedSuperClass, final Class<?> actualClass) {
    return getIdmxIdentifier() + "Element with class \"" + actualClass.getSimpleName()
        + "\" cannot be cast to class \"" + expectedSuperClass.getSimpleName() + "\".";
  }

  /**
   * @return
   */
  public static String wrongContext() {

    return getIdmxIdentifier() + "Context does not have an associated state or the context of a "
        + "new issuance collides with a previously used context value.";
  }

  public static String hiddenOrderGroup() {
    return getIdmxIdentifier() + "This group does not support computing inverses of exponents as "
        + "it is a group of hidden order.";
  }

  public static String illegalGroupElement() {
    return getIdmxIdentifier() + "The argument is not a group element of the given instance of "
        + "the signed quadratic residues group.";
  }

  public static String malformedClass(final String className) {
    return getIdmxIdentifier() + "Class " + className + " is malformed.";
  }

  public static String tooManyAttributes() {
    return getIdmxIdentifier() + "The given issuer public key does not support as many attributes "
        + "as were provided.";
  }

  /**
   * This message should be used, for example, when a party calls the extra issuance round for a
   * protocol which does not need any extra steps or if a party calls the extra round too many
   * times.
   */
  public static String numberOfMaximalRoundsReached(final String buildingBlockName) {
    return getIdmxIdentifier() + "Maximal number of rounds for the building block: "
        + buildingBlockName + " has been reached.";
  }

  /**
   * This message should be used to indicate that the list of attributes and a helper list (e.g.,
   * list of indications whether the attribute should be revealed) are of equal length. The simple
   * name of the class is taken as parameter.
   * 
   * @param className Simple name of the class throwing the error.
   */
  public static String attributeNumberMismatch(final String className) {
    return getIdmxIdentifier() + "The number of list elements (e.g., attributes) does not "
        + "match with a corresponding helper list (e.g., list of booleans that indicate whether "
        + "an attribute is revealed or not) in class: " + className;
  }

  public static String verificationFailed() {
    return getIdmxIdentifier() + "Verification of the proof failed.";
  }

  public static String valueHasBeenRevoked() {
    return getIdmxIdentifier() + "Revocation handle has been revoked.";
  }

  /**
   * Should be used to indicate that an element is used wrongly - the message may be used to specify
   * details.
   */
  public static String wrongUsage(final String message) {
    return getIdmxIdentifier() + "Wrong usage of an element: " + message;
  }

  /**
   * Should be used when an element cannot be found in the appropriate storage (e.g., key manager or
   * credential manager).
   */
  public static String missingElement(final String missingElementId, final String storageId) {
    return getIdmxIdentifier() + "The elment " + missingElementId + " is mising in the storage: "
        + storageId;
  }



}
