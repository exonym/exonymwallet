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

package com.ibm.zurich.idmx.buildingBlock.revocation;


import java.net.URI;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBIntrospector;

import com.ibm.zurich.idmx.configuration.ErrorMessages;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.SerializationException;
import com.ibm.zurich.idmx.jaxb.JaxbHelperClass;
import com.ibm.zurich.idmx.jaxb.ParameterListHelper;
import com.ibm.zurich.idmx.jaxb.ParametersHelper;

import eu.abc4trust.xml.NreUpdateMessage;
import eu.abc4trust.xml.ObjectFactory;


/**
 * 
 */
public class NreUpdateRequestWrapper extends ParametersHelper {


  // Delegatee element names
  private static final String NRE_UPDATE_CONTEXT = "nreUpdate";
  private static final String RA_PUBLIC_KEY = "raPublicKey";
  private static final String IMPLEMENTATION_ID = "implementationId";
  private static final String NRE_VERSION = "nreVersion";
  private static final String REQUESTED_NRE_VERSION = "requestedNreVersion";

  // Delegatee
  private JAXBElement<NreUpdateMessage> delegatee;

  public NreUpdateRequestWrapper() {
    this(new ObjectFactory().createNreUpdateMessage());
  }

  public NreUpdateRequestWrapper(NreUpdateMessage nreUpdateMessage) {
    this.delegatee = new ObjectFactory().createNreUpdateMessage(nreUpdateMessage);
    this.parameterListHelper = new ParameterListHelper(getNreUpdateMessage().getParameter());
  }



  @SuppressWarnings("unchecked")
  private NreUpdateRequestWrapper(final JAXBElement<?> jaxbElement) throws SerializationException {

    final Class<?> delegateeClass = NreUpdateMessage.class;
    if (delegateeClass.isAssignableFrom(jaxbElement.getDeclaredType())) {
      this.delegatee = (JAXBElement<NreUpdateMessage>) jaxbElement;
      this.parameterListHelper = new ParameterListHelper(getNreUpdateMessage().getParameter());
    } else {
      throw new SerializationException(ErrorMessages.malformedClass(delegateeClass.getSimpleName()));
    }
  }

  /**
   * Returns the issuer parameters template (JAXB object).
   */
  public NreUpdateMessage getNreUpdateMessage() {
    return (NreUpdateMessage) JAXBIntrospector.getValue(delegatee);
  }


  @Override
  protected String createParameterUriBasedOnParameterName(final String parameterName) {
    // return ParameterBaseName.NreUpdateResponseParameterName(parameterName);
    return parameterName;
  }



  public URI getNreUpdateContext() throws ConfigurationException {
    return (URI) getParameter(NRE_UPDATE_CONTEXT);
  }

  public void setNreUpdateContext(final URI nreUpdateContext) {
    setParameter(NRE_UPDATE_CONTEXT, nreUpdateContext);
  }

  public URI getRaPublicKey() throws ConfigurationException {
    return (URI) getParameter(RA_PUBLIC_KEY);
  }

  public void setRaPublicKey(final URI raPublicKey) {
    setParameter(RA_PUBLIC_KEY, raPublicKey);
  }

  public URI getImplementationId() throws ConfigurationException {
    return (URI) getParameter(IMPLEMENTATION_ID);
  }

  public void setImplementationId(final URI implementationId) {
    setParameter(IMPLEMENTATION_ID, implementationId);
  }

  public URI getNreVersion() throws ConfigurationException {
    return (URI) getParameter(NRE_VERSION);
  }

  public void setNreVersion(final URI nreVersion) {
    setParameter(NRE_VERSION, nreVersion);
  }

  public URI getRequestedNreVersion() throws ConfigurationException {
    return (URI) getParameter(REQUESTED_NRE_VERSION);
  }

  public void setRequestedNreVersion(final URI nreVersion) {
    setParameter(REQUESTED_NRE_VERSION, nreVersion);
  }


  /**
   * Serializes the stored template using JAXB.
   * 
   * @param filename
   * @throws SerializationException
   */
  public String serialize() throws SerializationException {
    return JaxbHelperClass.serialize(delegatee);
  }

  /**
   * De-serializes an issuer public key template formatted as a string into a JAXB object.
   * 
   * @throws SerializationException
   */
  public static NreUpdateRequestWrapper deserialize(final String nreUpdateRequest)
      throws SerializationException {
    return new NreUpdateRequestWrapper(JaxbHelperClass.deserialize(nreUpdateRequest));
  }
}
