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
package com.ibm.zurich.idmx.device;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.ibm.zurich.idmx.interfaces.device.ExternalSecretsHelper;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;

import javax.inject.Inject;

public class ExternalSecretsHelperImpl implements ExternalSecretsHelper {

  private final BigIntFactory bigIntFactory;

  @Inject
  public ExternalSecretsHelperImpl(final BigIntFactory bigIntFactory) {
    this.bigIntFactory = bigIntFactory;
  }

  @Override
  public BigInteger getBaseForScopeExclusivePseudonym(final URI scope, final BigInteger modulus,
                                                      final BigInteger subgroupOrder) {
    return getBaseForScopeExclusivePseudonym(bigIntFactory, scope, bigIntFactory.valueOf(modulus),
        bigIntFactory.valueOf(subgroupOrder)).getValue();
  }

  private final static String HASH_ALGORITHM = "SHA-256";
  private final static String ENCODING = "UTF-8";

  public static BigInt getBaseForScopeExclusivePseudonym(final BigIntFactory bigIntFactory, final URI scope,
                                                         final BigInt modulus, final BigInt subgroupOrder) {
    try {
      final byte[] encodedScope = scope.toString().getBytes(ENCODING);
      final MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
      digest.update(encodedScope);
      final byte[] hashedScopeAsBytes = digest.digest();
      final BigInt hasedScope = bigIntFactory.unsignedValueOf(hashedScopeAsBytes);

      // cofactor = (modulus-1)/subgroupOrder
      final BigInt cofactor = modulus.subtract(bigIntFactory.one()).divide(subgroupOrder);
      // base = hash(scope) ^ cofactor
      final BigInt base = hasedScope.modPow(cofactor, modulus);

      return base;
    } catch (UnsupportedEncodingException|NoSuchAlgorithmException e) {
      throw new RuntimeException(e);
    }
  }
}
