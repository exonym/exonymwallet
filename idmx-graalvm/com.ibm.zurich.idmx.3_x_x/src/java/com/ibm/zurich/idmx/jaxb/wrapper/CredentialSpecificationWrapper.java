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

package com.ibm.zurich.idmx.jaxb.wrapper;

import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.SerializationException;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.jaxb.JaxbHelperClass;

import eu.abc4trust.xml.AttributeDescription;
import eu.abc4trust.xml.CredentialSpecification;
import eu.abc4trust.xml.ObjectFactory;

public class CredentialSpecificationWrapper {

  private final BigIntFactory bigIntFactory;
  private final CredentialSpecification credSpec;

  // TODO add credential specification element names (cf. issuer public key)


  public CredentialSpecificationWrapper(final CredentialSpecification credSpec,
                                        final BigIntFactory bigIntFactory) {
    this.credSpec = credSpec;
    this.bigIntFactory = bigIntFactory;
  }



  public BigInt getCredSpecId(final String hashFunction) throws ConfigurationException {
    if (credSpec.getNumericalId() != null) {
      return bigIntFactory.valueOf(credSpec.getNumericalId());
    } else {
      final byte[] hash = getHash(hashFunction);
      return bigIntFactory.unsignedValueOf(hash);
    }
  }


  // TODO: add getters for the key elements (cf. issuer public key)


  /**
   * Returns the number of attributes the credential contains.
   */
  public int getNumberOfAttributes() {
    return credSpec.getAttributeDescriptions().getAttributeDescription().size();
  }


  /**
   * @return
   */
  public CredentialSpecification getCredentialSpecification() {
    return credSpec;
  }


  public boolean isKeyBinding() {
    return credSpec.isKeyBinding();
  }

  public boolean isRevocable() {
    return credSpec.isRevocable();
  }

  public AttributeDescription getRevocationHandleAttributeDescription() {
    for (final AttributeDescription attDesc : credSpec.getAttributeDescriptions()
        .getAttributeDescription()) {
      if (attDesc.getType().equals(
          URI.create("http://abc4trust.eu/wp2/abcschemav1.0/revocationhandle"))) {
        return attDesc;
      }
    }
    throw new RuntimeException("Revocation handle is not present.");
  }

  public int getRevocationHandleAttributeIndex(AttributeDescription attDesc) {
    return credSpec.getAttributeDescriptions().getAttributeDescription().indexOf(attDesc);
  }


  protected String createParameterUriBasedOnParameterName(String parameterName) {
    // return ParameterBaseName.credentialSpecificationParameterName(parameterName);
    throw new RuntimeException("not implemented");
  }

  public String serialize() throws SerializationException {
    return JaxbHelperClass.serialize(new ObjectFactory().createCredentialSpecification(credSpec));
  }

  /**
   * Computes a cryptographic hash of the public key
   * 
   * @throws ConfigurationException
   */
  public byte[] getHash(final String hashAlgorithm) throws ConfigurationException {
    try {
      final byte[] toHash =
          JaxbHelperClass.canonicalXml(new ObjectFactory().createCredentialSpecification(credSpec));
      final MessageDigest md = MessageDigest.getInstance(hashAlgorithm);
      md.update(toHash);
      return md.digest();
    } catch (final SerializationException e) {
      throw new RuntimeException(e);
    } catch (final NoSuchAlgorithmException e) {
      throw new ConfigurationException(e);
    }
  }
}
