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

import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBIntrospector;

import com.ibm.zurich.idmx.configuration.ErrorMessages;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.SerializationException;
import com.ibm.zurich.idmx.jaxb.JaxbHelperClass;

import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PresentationTokenDescription;

/**
 * 
 */
public class PresentationTokenDescriptionWrapper {

  // Delegatee
  private final JAXBElement<PresentationTokenDescription> delegatee;

  public PresentationTokenDescriptionWrapper() {
    this(new ObjectFactory().createPresentationTokenDescription());
  }

  public PresentationTokenDescriptionWrapper(
      final PresentationTokenDescription presentationTokenDescription) {
    this.delegatee =
        new ObjectFactory().createPresentationTokenDescription(presentationTokenDescription);
  }

  @SuppressWarnings("unchecked")
  private PresentationTokenDescriptionWrapper(JAXBElement<?> jaxbElement)
      throws SerializationException {

    final Class<?> delegateeClass = PresentationTokenDescription.class;
    if (delegateeClass.isAssignableFrom(jaxbElement.getDeclaredType())) {
      this.delegatee = (JAXBElement<PresentationTokenDescription>) jaxbElement;
    } else {
      throw new SerializationException(ErrorMessages.malformedClass(delegateeClass.getSimpleName()));
    }
  }



  /**
   * Computes a cryptographic hash of the system parameters
   * 
   * @throws ConfigurationException
   */
  public byte[] getHash(final String hashAlgorithm) throws ConfigurationException {
    try {
      final byte[] toHash = JaxbHelperClass.canonicalXml(delegatee);
      final MessageDigest md = MessageDigest.getInstance(hashAlgorithm);
      md.update(toHash);
      return md.digest();
    } catch (final SerializationException e) {
      throw new RuntimeException(e);
    } catch (final NoSuchAlgorithmException e) {
      throw new ConfigurationException(e);
    }
  }

  /**
   * @return
   */
  public PresentationTokenDescription getSystemParameters() {
    return (PresentationTokenDescription) JAXBIntrospector.getValue(delegatee);
  }

  public String serialize() throws SerializationException {
    return JaxbHelperClass.serialize(delegatee);
  }

  public static PresentationTokenDescriptionWrapper deserialize(final String delegatee)
      throws SerializationException {
    return new PresentationTokenDescriptionWrapper(JaxbHelperClass.deserialize(delegatee));
  }

  public static PresentationTokenDescriptionWrapper deserialize(final InputStream inputStream)
      throws SerializationException {
    return new PresentationTokenDescriptionWrapper(JaxbHelperClass.deserialize(inputStream));
  }
}
