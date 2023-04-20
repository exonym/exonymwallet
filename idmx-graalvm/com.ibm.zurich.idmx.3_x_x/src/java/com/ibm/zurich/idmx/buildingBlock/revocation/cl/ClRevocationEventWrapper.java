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

package com.ibm.zurich.idmx.buildingBlock.revocation.cl;


import java.net.URI;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBIntrospector;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import com.ibm.zurich.idmix.abc4trust.facades.RevocationLogEntryFacade;
import com.ibm.zurich.idmx.annotations.Nullable;
import com.ibm.zurich.idmx.configuration.Configuration;
import com.ibm.zurich.idmx.configuration.ErrorMessages;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.SerializationException;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
//import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.group.GroupFactory;
import com.ibm.zurich.idmx.interfaces.util.group.HiddenOrderGroup;
import com.ibm.zurich.idmx.interfaces.util.group.HiddenOrderGroupElement;
import com.ibm.zurich.idmx.jaxb.JaxbHelperClass;
import com.ibm.zurich.idmx.jaxb.ParameterListHelper;
import com.ibm.zurich.idmx.jaxb.ParametersHelper;

import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PrivateKey;
import eu.abc4trust.xml.PublicKey;
import eu.abc4trust.xml.RevocationEvent;
import eu.abc4trust.xml.RevocationState;

// TODO make this extend the RevocationEventWrapper
public class ClRevocationEventWrapper extends ParametersHelper {

  // Delegatee element names
  private static final String NEW_EPOCH = "newEpoch";
  private static final String NEW_ACCUMULATOR = "newAccumulatorValue";
  private static final String REVOCATION_HANDLE = "removedPrime";
  private static final String EVENT_DATE = "eventDate";
  private static final String EVENT_ID = "eventId";

  private static final String CONCATENATOR = "::";

  //public static BigInt test;
  //public static HiddenOrderGroupElement test2;

  // Delegatee
  private final JAXBElement<RevocationEvent> delegatee;

  public ClRevocationEventWrapper() {
    this(new ObjectFactory().createRevocationEvent());
  }

  public ClRevocationEventWrapper(RevocationEvent delegatee) {
    this.delegatee = new ObjectFactory().createRevocationEvent(delegatee);
    this.parameterListHelper = new ParameterListHelper(getDelegateeValue().getParameter());
  }

  public ClRevocationEventWrapper(final URI publicKeyId, final int newEpoch, final BigInt removedPrime,
                                  final Calendar eventDate, final BigInt accumulatorValue) {
    this();
    // general fields
    setNewEpoch(newEpoch);
    setEventDate(eventDate);
    setEventId(RevocationLogEntryFacade.getRevocationLogEntryUID(publicKeyId,
        String.valueOf(newEpoch)));

    // specific to CL revocation
    setRevocationHandle(removedPrime);
    setAccumulatorValue(accumulatorValue);
  }


  @SuppressWarnings("unchecked")
  private ClRevocationEventWrapper(final JAXBElement<?> jaxbElement) throws SerializationException {

    final Class<?> delegateeClass = RevocationEvent.class;
    if (delegateeClass.isAssignableFrom(jaxbElement.getDeclaredType())) {
      this.delegatee = (JAXBElement<RevocationEvent>) jaxbElement;
      this.parameterListHelper = new ParameterListHelper(getDelegateeValue().getParameter());
    } else {
      throw new SerializationException(ErrorMessages.malformedClass(delegateeClass.getSimpleName()));
    }
  }

  /**
   * Returns the delegatee (JAXB object).
   */
  public RevocationEvent getDelegateeValue() {
    return (RevocationEvent) JAXBIntrospector.getValue(delegatee);
  }


  @Override
  protected String createParameterUriBasedOnParameterName(final String parameterName) {
    // return ParameterBaseName.NreUpdateResponseParameterName(parameterName);
    return parameterName;
  }



  public Integer getNewEpoch() throws ConfigurationException {
    return (Integer) getParameter(NEW_EPOCH);
  }

  public void setNewEpoch(int newEpoch) {
    setParameter(NEW_EPOCH, newEpoch);
  }

  public BigInt getAccumulatorValue() throws ConfigurationException {
    return (BigInt) getParameter(NEW_ACCUMULATOR);
  }

  public void setAccumulatorValue(BigInt accumulatorValue) {
    setParameter(NEW_ACCUMULATOR, accumulatorValue);
  }

  public BigInt getRevocationHandle() throws ConfigurationException {
    return (BigInt) getParameter(REVOCATION_HANDLE);
  }

  public void setRevocationHandle(final BigInt revocationHandle) {
    setParameter(REVOCATION_HANDLE, revocationHandle);
  }

  public XMLGregorianCalendar getEventDate() throws ConfigurationException {
    final String eventDateString = (String) getParameter(EVENT_DATE);
    final String[] splitDate = eventDateString.split(CONCATENATOR);
    final XMLGregorianCalendar eventDate;
    try {
      eventDate =
          DatatypeFactory.newInstance().newXMLGregorianCalendar(
              new GregorianCalendar(Integer.valueOf(splitDate[0]), Integer.valueOf(splitDate[1]),
                  Integer.valueOf(splitDate[2]), Integer.valueOf(splitDate[3]), Integer
                      .valueOf(splitDate[4])));
    } catch (final DatatypeConfigurationException e) {
      throw new RuntimeException(e);
    }

    return eventDate;
  }

  public void setEventDate(Calendar eventDate) {

    final XMLGregorianCalendar eventDateXml;
    final Date creationDate = eventDate.getTime();
    final GregorianCalendar calendar = new GregorianCalendar();
    calendar.setTime(creationDate);
    try {
      eventDateXml = DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);
    } catch (final DatatypeConfigurationException e) {
      throw new RuntimeException(e);
    }

    setEventDate(eventDateXml);
  }

  public void setEventDate(XMLGregorianCalendar eventDate) {

    final String eventDateString = eventDate.getYear() + CONCATENATOR + //
        eventDate.getMonth() + CONCATENATOR + //
        eventDate.getDay() + CONCATENATOR + //
        eventDate.getHour() + CONCATENATOR + //
        eventDate.getMinute();

    setParameter(EVENT_DATE, eventDateString);
  }

  public URI getEventId() throws ConfigurationException {
    return (URI) getParameter(EVENT_ID);
  }

  public void setEventId(URI eventId) {
    setParameter(EVENT_ID, eventId);
  }



  /**
   * Construct an AccumulatorEvent to remove a prime from the accumulator given the secret key of
   * the accumulator. This method is faster than the one taking the whole history.
   * 
   * @throws ConfigurationException
   */
  public static ClRevocationEventWrapper removePrime(final RevocationState currentState,
                                                     final BigInt accumulatedPrime, @Nullable XMLGregorianCalendar date, final PublicKey pk, final PrivateKey sk,
      final GroupFactory gf) throws ConfigurationException {

    final ClRevocationAuthorityPublicKeyWrapper pkWrapper = new ClRevocationAuthorityPublicKeyWrapper(pk);
    final ClRevocationSecretKeyWrapper skWrapper = new ClRevocationSecretKeyWrapper(sk);
    final ClRevocationStateWrapper currentStateWrapper = new ClRevocationStateWrapper(currentState);

    if (Configuration.debug()) {
      // if (!skWrapper.getModulus().equals(currentState.getPublicKeyWrapper().getModulus())) {
      // throw new RuntimeException("Using invalid private key in AccumulatorEvent:removePrime");
      // }
    }
    final int newEpoch = currentStateWrapper.getEpoch() + 1;
    if (date == null) {
      date = now();
    }
    // acc = acc^( prime^-1 mod phi ) mod n

    
    //BEFORE
    HiddenOrderGroup group = pkWrapper.getGroup(gf);// ORDER PRIME!!!
    BigInt qPrime = skWrapper.getSophieGermainPrimeQ();
    BigInt pPrime = skWrapper.getSophieGermainPrimeP();
    BigInt order = pPrime.multiply(qPrime);// !!!!
    BigInt inv = accumulatedPrime.modInverse(order);
    HiddenOrderGroupElement oldAcc = group.valueOf(currentStateWrapper.getAccumulatorValue());
    HiddenOrderGroupElement newAcc = oldAcc.multOp(inv);
    
    //TODO(ksa)
//    final HiddenOrderGroup group = pkWrapper.getGroup(gf);
//    final BigInt q = skWrapper.getSafePrimeQ();
//    final BigInt p = skWrapper.getSafePrimeP();
//      
//    final BigIntFactory b = p.getFactory(); 
//    final BigInt order = p.subtract(b.one()).multiply(q.subtract(b.one()));
//        
//    final BigInt inv = accumulatedPrime.modInverse(order);
//    final HiddenOrderGroupElement oldAcc = group.valueOf(currentStateWrapper.getAccumulatorValue());
//    final HiddenOrderGroupElement newAcc = oldAcc.multOp(inv);
    //test2 = oldAcc;
    
    if (Configuration.debug()) {
      final HiddenOrderGroupElement calculatedPreviousAccumulator = newAcc.multOp(accumulatedPrime);
      if (!calculatedPreviousAccumulator.equals(oldAcc)) {
        throw new RuntimeException(
            "Re-calculation of the previous accumulator value fails on the RA side.");
      }
    }

    return new ClRevocationEventWrapper(pk.getPublicKeyId(), newEpoch, accumulatedPrime,
        new GregorianCalendar(), newAcc.toBigInt());
  }

  private static XMLGregorianCalendar now() {
    try {
      return DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar());
    } catch (final DatatypeConfigurationException e) {
      throw new RuntimeException(e);
    }
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
  public static ClRevocationEventWrapper deserialize(final String nreUpdateRequest)
      throws SerializationException {
    return new ClRevocationEventWrapper(JaxbHelperClass.deserialize(nreUpdateRequest));
  }
}
