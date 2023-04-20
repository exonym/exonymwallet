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

package com.ibm.zurich.idmix.abc4trust.facades;

import java.net.URI;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBIntrospector;

import com.ibm.zurich.idmx.configuration.ErrorMessages;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.SerializationException;
import com.ibm.zurich.idmx.interfaces.configuration.Constants;
import com.ibm.zurich.idmx.jaxb.JaxbHelperClass;

import eu.abc4trust.xml.CredentialDescription;
import eu.abc4trust.xml.ObjectFactory;

/**
 * 
 */
public class CredentialDescriptionFacade implements Constants {

  // Delegatee
  private final JAXBElement<CredentialDescription> credentialDescription;



  public CredentialDescriptionFacade() {
    this(new ObjectFactory().createCredentialDescription());
  }

  public CredentialDescriptionFacade(final CredentialDescription credentialDescription) {
    this.credentialDescription =
        new ObjectFactory().createCredentialDescription(credentialDescription);
  }

  private CredentialDescriptionFacade(final JAXBElement<?> jaxbElement) throws SerializationException {
    this.credentialDescription = verifyTypeOfJaxbElement(jaxbElement);
  }


  public URI getCredentialSpecificicationUID() {
    return getCredentialDescription().getCredentialSpecificationUID();
  }

  public URI getIssuerParametersUID() {
    return getCredentialDescription().getIssuerParametersUID();
  }


  public void setSecretReference(final URI secretUid) {
    getCredentialDescription().setSecretReference(secretUid);
  }

  public void setCredentialUID(final URI credentialUID) {
    getCredentialDescription().setCredentialUID(credentialUID);
  }



  public CredentialDescription getCredentialDescription() {
    return (CredentialDescription) JAXBIntrospector.getValue(credentialDescription);
  }

  /**
   * @param jaxbElement
   * @throws SerializationException
   */
  @SuppressWarnings("unchecked")
  private static JAXBElement<CredentialDescription> verifyTypeOfJaxbElement(
    final JAXBElement<?> jaxbElement) throws SerializationException {
    final Class<?> delegateeClass = CredentialDescription.class;
    if (delegateeClass.isAssignableFrom(jaxbElement.getDeclaredType())) {
      return (JAXBElement<CredentialDescription>) jaxbElement;
    } else {
      throw new SerializationException(ErrorMessages.malformedClass(delegateeClass.getSimpleName()));
    }
  }


  public static CredentialDescriptionFacade deserialize(final String credentialDescription)
      throws SerializationException, ConfigurationException {

    final JAXBElement<?> jaxbElement = JaxbHelperClass.deserialize(credentialDescription);

    return new CredentialDescriptionFacade(jaxbElement);
  }

  public String serialize() throws SerializationException {
    return JaxbHelperClass.serialize(credentialDescription);
  }



}
