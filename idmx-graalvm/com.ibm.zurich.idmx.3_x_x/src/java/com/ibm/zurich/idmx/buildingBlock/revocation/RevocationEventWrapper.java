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
import eu.abc4trust.xml.RevocationEvent;


/**
 * 
 */
public class RevocationEventWrapper extends ParametersHelper {


  // Delegatee element names
  private static final String NEW_EPOCH = "newEpoch";
  private static final String EVENT_DATE = "eventDate";
  private static final String EVENT_ID = "eventId";

  // Delegatee
  private final JAXBElement<RevocationEvent> delegatee;

  public RevocationEventWrapper() {
    this(new ObjectFactory().createRevocationEvent());
  }

  public RevocationEventWrapper(final RevocationEvent revocationEvent) {
    this.delegatee = new ObjectFactory().createRevocationEvent(revocationEvent);
    this.parameterListHelper = new ParameterListHelper(getDelegateeValue().getParameter());
  }



  @SuppressWarnings("unchecked")
  private RevocationEventWrapper(final JAXBElement<?> jaxbElement) throws SerializationException {

    final Class<?> delegateeClass = NreUpdateMessage.class;
    if (delegateeClass.isAssignableFrom(jaxbElement.getDeclaredType())) {
      this.delegatee = (JAXBElement<RevocationEvent>) jaxbElement;
      this.parameterListHelper = new ParameterListHelper(getDelegateeValue().getParameter());
    } else {
      throw new SerializationException(ErrorMessages.malformedClass(delegateeClass.getSimpleName()));
    }
  }

  /**
   * Returns the issuer parameters template (JAXB object).
   */
  public RevocationEvent getDelegateeValue() {
    return (RevocationEvent) JAXBIntrospector.getValue(delegatee);
  }


  @Override
  protected String createParameterUriBasedOnParameterName(final String parameterName) {
    // return ParameterBaseName.NreUpdateResponseParameterName(parameterName);
    return parameterName;
  }



  public int getNewEpoch() throws ConfigurationException {
    return (Integer) getParameter(NEW_EPOCH);
  }

  public void setNewEpoch(final int newEpoch) {
    setParameter(NEW_EPOCH, newEpoch);
  }

  public URI getEventDate() throws ConfigurationException {
    return (URI) getParameter(EVENT_DATE);
  }

  public void setEventDate(final URI eventDate) {
    setParameter(EVENT_DATE, eventDate);
  }

  public URI getEventId() throws ConfigurationException {
    return (URI) getParameter(EVENT_ID);
  }

  public void setEventId(final URI eventId) {
    setParameter(EVENT_ID, eventId);
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
  public static RevocationEventWrapper deserialize(final String nreUpdateRequest)
      throws SerializationException {
    return new RevocationEventWrapper(JaxbHelperClass.deserialize(nreUpdateRequest));
  }
}
