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

import com.ibm.zurich.idmx.interfaces.configuration.Constants;
import com.ibm.zurich.idmx.util.UriUtils;

/**
 * 
 */
public class ParameterBaseName implements Constants {

  private ParameterBaseName() {
    throw new AssertionError(ErrorMessages.nonInstantiationErrorMessage());
  }

  private static String extendName(final String base, final String extension) {
    return UriUtils.concat(base, extension).toString();
  }

  public static String implementationBaseName() {
    return extendName(BASE_URI, IMPLEMENTATION_VERSION);
  }

  public static String implementationElementName(final String extension) {
    return extendName(implementationBaseName(), extension);
  }

  // System parameters
  public static String systemParametersTemplateBaseName() {
    return implementationElementName("systemParametersTemplate");
  }

  public static String systemParametersTemplateParameterName(final String parameterName) {
    return extendName(systemParametersTemplateBaseName(), parameterName);
  }

  public static String systemParametersBaseName() {
    return implementationElementName("systemParameters");
  }

  public static String systemParametersParameterName(final String parameterName) {
    return extendName(systemParametersBaseName(), parameterName);
  }

  // Verifier parameters
  public static String verifierParametersTemplateBaseName() {
    return implementationElementName("verifierParametersTemplate");
  }

  public static String verifierParametersTemplateParameterName(final String parameterName) {
    return extendName(verifierParametersTemplateBaseName(), parameterName);
  }

  public static String verifierParametersBaseName() {
    return implementationElementName("verifierParameters");
  }

  public static String verifierParametersParameterName(final String parameterName) {
    return extendName(verifierParametersBaseName(), parameterName);
  }

  // Issuer Key Pair
  public static String issuerParametersTemplateBaseName() {
    return implementationElementName("issuerParametersTemplate");
  }

  public static String issuerPublicKeyTemplateParameterName(final String parameterName) {
    return extendName(issuerParametersTemplateBaseName(), parameterName);
  }

  public static String issuerParametersBaseName() {
    return implementationElementName("issuerParameters");
  }

  public static String issuerParametersParameterName(final String parameterName) {
    return extendName(issuerParametersBaseName(), parameterName);
  }

  public static String issuerPublicKeyBaseName() {
    return implementationElementName(extendName("issuer", "publicKey"));
  }

  public static String issuerPublicKeyParameterName(final String parameterName) {
    return extendName(issuerPublicKeyBaseName(), parameterName);
  }

  public static String issuerPrivateKeyBaseName() {
    return implementationElementName(extendName("issuer", "privateKey"));
  }

  public static String issuerPrivateKeyParameterName(final String parameterName) {
    return extendName(issuerPrivateKeyBaseName(), parameterName);
  }

  // Inspector Key Pair
  public static String inspectorPublicKeyTemplateBaseName() {
    return implementationElementName("inspectorPublicKeyTemplate");
  }

  public static String inspectorPublicKeyTemplateParameterName(final String parameterName) {
    return extendName(inspectorPublicKeyTemplateBaseName(), parameterName);
  }

  public static String inspectorPublicKeyBaseName() {
    return implementationElementName(extendName("inspector", "publicKey"));
  }

  public static String inspectorPublicKeyParameterName(final String parameterName) {
    return extendName(inspectorPublicKeyBaseName(), parameterName);
  }

  public static String inspectorPrivateKeyBaseName() {
    return implementationElementName(extendName("inspector", "privateKey"));
  }

  public static String inspectorPrivateKeyParameterName(final String parameterName) {
    return extendName(inspectorPrivateKeyBaseName(), parameterName);
  }



  // Revocation Authority Key Pair
  public static String revocationAuthorityPublicKeyTemplateBaseName() {
    return implementationElementName("revocationAuthorityPublicKeyTemplate");
  }

  public static String revocationAuthorityPublicKeyTemplateParameterName(final String parameterName) {
    return extendName(revocationAuthorityPublicKeyTemplateBaseName(), parameterName);
  }

  public static String revocationAuthorityPublicKeyBaseName() {
    return implementationElementName(extendName("revocationAuthority", "publicKey"));
  }

  public static String revocationAuthorityPublicKeyParameterName(final String parameterName) {
    return extendName(revocationAuthorityPublicKeyBaseName(), parameterName);
  }

  public static String revocationAuthorityPrivateKeyBaseName() {
    return implementationElementName(extendName("revocationAuthority", "privateKey"));
  }

  public static String revocationAuthorityPrivateKeyParameterName(final String parameterName) {
    return extendName(revocationAuthorityPrivateKeyBaseName(), parameterName);
  }
}
