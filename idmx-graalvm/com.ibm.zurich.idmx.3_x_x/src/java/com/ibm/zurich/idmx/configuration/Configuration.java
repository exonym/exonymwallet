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

import java.net.URI;

import com.ibm.zurich.idmx.buildingBlock.revocation.cl.ClRevocationBuildingBlock;
import com.ibm.zurich.idmx.buildingBlock.signature.cl.ClSignatureBuildingBlock;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.interfaces.configuration.Constants;

/**
 * 
 */
public class Configuration implements Constants {

  public static final String PROOF_SYSTEM = "fiat-shamir";
  public static final long TEST_TIMEOUT = 5 * 60 * 1000;

  // System parameter template configuration values
  private final static String SYSTEM_PARAMETERS_VERSION = "1.0.0";
  private final static String DEFAULT_HASH_FUNCTION = "sha-256";

  // Issuer parameters template configuration values
  private final static String ISSUER_PARAMETERS_VERSION = "1.0.0";

  // Issuer storage configuration
  private final static int MAX_CONCURRENT_ISSUANCE_CONTEXTS = 1000;

  // Activates (console) output
  private final static boolean VERBOSE_PROOF_XML = true;
  private final static boolean PRINT_STACK_TRACES = true;

  // Activates all checks that are implemented
  private final static boolean DEBUG_MODE = true;
  // Individual control over the checks
  private final static boolean CHECK_GROUP_ORDERS = true;
  private final static boolean CHECK_PRIMALITY_OF_MODULI = true;
  private final static boolean RE_COMPUTE_SIGNATURE = true;
  private final static boolean VERBOSE_CANONICAL_XML = false;

  // Default values that serve as a suggestion for the user of the library
  private final static boolean SAVE_LAST_TOKEN = false;
  private final static int DEFAULT_NUMBER_TOKENS = 5;
  
  // Security level of 103 will result in an RSA modulus size of 2048 bits.
  private final static int DEFAULT_SECURITY_LEVEL = 103;
  // Statistical indistinguishability parameter
  private final static int DEFAULT_STATISTICAL_IND = 80;
  // Attribute length of 256 matches the hash length when using SHA-256
  private final static int DEFAULT_ATTRIBUTE_LENGTH = 256;
  private final static int DEFAULT_MAXIMAL_NUMBER_OF_ATTRIBUTES = 8;
  private final static URI DEFAULT_ISSUER_PUBLIC_KEY_PREFIX = URI.create("urn" + URI_DELIMITER
      + "tld.organisation.department" + URI_DELIMITER + "issuance" + URI_DELIMITER + "technology"
      + URI_DELIMITER + "version" + URI_DELIMITER + "publicKey");
  private final static URI DEFAULT_INSPECTOR_PUBLIC_KEY_PREFIX = URI.create("urn" + URI_DELIMITER
      + "tld.organisation.department" + URI_DELIMITER + "inspector" + URI_DELIMITER + "technology"
      + URI_DELIMITER + "version" + URI_DELIMITER + "publicKey");
  private final static URI DEFAULT_REVOCATION_AUTHORITY = URI.create("urn" + URI_DELIMITER
      + "tld.organisation.department" + URI_DELIMITER + "revocation" + URI_DELIMITER + "technology"
      + URI_DELIMITER + "version" + URI_DELIMITER + "publicKey");


  private Configuration() {
    throw new AssertionError(ErrorMessages.nonInstantiationErrorMessage());
  }

  public static String systemParametersVersion() {
    return SYSTEM_PARAMETERS_VERSION;
  }

  public static String defaultHashFunction() {
    return DEFAULT_HASH_FUNCTION;
  }

  public static String issuerParametersVersion() {
    return ISSUER_PARAMETERS_VERSION;
  }


  // Issuer storage configuration
  public static int maximumNumberOfIssuanceStates() {
    return MAX_CONCURRENT_ISSUANCE_CONTEXTS;
  }



  public static URI defaultSignatureTechnology() throws ConfigurationException {
    return new ClSignatureBuildingBlock(null, null, null, null, null, null).getImplementationId();
  }

  public static URI defaultRevocationTechnology() throws ConfigurationException {
    return new ClRevocationBuildingBlock(null, null, null, null).getImplementationId();
  }

  public static int defaultMaximalNumberOfAttributes() {
    return DEFAULT_MAXIMAL_NUMBER_OF_ATTRIBUTES;
  }

  public static URI defaultIssuerPublicKeyPrefix() {
    return DEFAULT_ISSUER_PUBLIC_KEY_PREFIX;
  }

  public static URI defaultInspectorPublicKeyPrefix() {
    return DEFAULT_INSPECTOR_PUBLIC_KEY_PREFIX;
  }

  public static URI defaultRevocationAuthorityId() {
    return DEFAULT_REVOCATION_AUTHORITY;
  }

  public static URI defaultRevocationAuthorityPublicKeyPrefix() {
    return DEFAULT_REVOCATION_AUTHORITY;
  }

  public static int defaultSecurityLevel() {
    return DEFAULT_SECURITY_LEVEL;
  }

  public static int defaultStatisticalInd() {
    return DEFAULT_STATISTICAL_IND;
  }

  public static int defaultAttributeLength() {
    return DEFAULT_ATTRIBUTE_LENGTH;
  }


  // Activates additional (console) output
  public static boolean verboseProofXml() {
    return VERBOSE_PROOF_XML;
  }

  public static boolean printStackTraces() {
    return PRINT_STACK_TRACES;
  }


  // Activates checks that provide more debug output
  public static boolean debug() {
    return DEBUG_MODE;
  }

  @SuppressWarnings("unused")
  public static boolean reComputeSignature() {
    return DEBUG_MODE || RE_COMPUTE_SIGNATURE;
  }

  @SuppressWarnings("unused")
  public static boolean checkGroupOrders() {
    return DEBUG_MODE || CHECK_GROUP_ORDERS;
  }

  @SuppressWarnings("unused")
  public static boolean checkPrimalityOfModuli() {
    return DEBUG_MODE || CHECK_PRIMALITY_OF_MODULI;
  }

  public static boolean saveLastSignatureTokenForReIssuance() {
    return SAVE_LAST_TOKEN;
  }

  public static int defaultNumberOfSignatureTokens() {
    return DEFAULT_NUMBER_TOKENS;
  }

  public static boolean verboseCanonicalXml() {
    return DEBUG_MODE && VERBOSE_CANONICAL_XML;
  }

}
