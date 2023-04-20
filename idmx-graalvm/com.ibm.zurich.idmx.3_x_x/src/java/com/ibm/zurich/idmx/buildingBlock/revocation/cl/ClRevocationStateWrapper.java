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
import java.util.GregorianCalendar;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBIntrospector;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import com.ibm.zurich.idmix.abc4trust.util.EncodeDecode;
import com.ibm.zurich.idmx.annotations.Nullable;
import com.ibm.zurich.idmx.buildingBlock.signature.cl.ClPublicKeyWrapper;
import com.ibm.zurich.idmx.buildingBlock.systemParameters.EcryptSystemParametersWrapper;
import com.ibm.zurich.idmx.configuration.Configuration;
import com.ibm.zurich.idmx.configuration.ErrorMessages;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.SerializationException;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.RandomGeneration;
import com.ibm.zurich.idmx.interfaces.util.group.GroupFactory;
import com.ibm.zurich.idmx.interfaces.util.group.HiddenOrderGroup;
import com.ibm.zurich.idmx.interfaces.util.group.HiddenOrderGroupElement;
import com.ibm.zurich.idmx.jaxb.JaxbHelperClass;
import com.ibm.zurich.idmx.jaxb.ParameterListHelper;
import com.ibm.zurich.idmx.jaxb.ParametersHelper;
import com.ibm.zurich.idmx.keypair.ra.RevocationAuthorityPublicKeyWrapper;

import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.xml.ObjectFactory;
import eu.abc4trust.xml.PublicKey;
import eu.abc4trust.xml.RevocationState;
import eu.abc4trust.xml.SystemParameters;

public class ClRevocationStateWrapper extends ParametersHelper {

  // // Runtime wrappers
  // private ClRevocationAuthorityPublicKeyWrapper pkWrapper;

  // Delegatee element names
  private static final String RA_PUBLIC_KEY_ID = "raPublicKeyId";
  private static final String EPOCH = "epoch";
  private static final String ACCUMULATOR_VALUE = "accumulatorValue";
  private static final String LAST_CHANGE_DATE = "lastChangeDate";

  // Delegatee
  private final JAXBElement<RevocationState> delegatee;

  public ClRevocationStateWrapper() {
    this(new ObjectFactory().createRevocationState());
  }

  public ClRevocationStateWrapper(final RevocationState delegatee) {
    this.delegatee = new ObjectFactory().createRevocationState(delegatee);
    this.parameterListHelper = new ParameterListHelper(getRevocationState().getParameter());
  }

  public ClRevocationStateWrapper(final SystemParameters systemParameters, final PublicKey pk, final int epoch,
                                  final HiddenOrderGroupElement accumulatorValue, @Nullable XMLGregorianCalendar lastChangeDate)
      throws ConfigurationException {
    this();

    // this.spWrapper = new EcryptSystemParametersWrapper(systemParameters);
    RevocationAuthorityPublicKeyWrapper pkWrapper = new RevocationAuthorityPublicKeyWrapper(pk);

    // if (!spWrapper.getSystemParametersId().equals(pkWrapper.getSystemParametersId())) {
    // throw new ConfigurationException(
    // ErrorMessages
    // .parameterWrong("system parmeters id in public key and in key manager do not match."));
    // }

    setRaPublicKeyId(pkWrapper.getPublicKeyId());
    setEpoch(epoch);
    setAccumulatorValue(accumulatorValue);
    if (lastChangeDate == null) {
      lastChangeDate = now();
    }
    setLastChangeDate(lastChangeDate);
  }


  @SuppressWarnings("unchecked")
  private ClRevocationStateWrapper(final JAXBElement<?> jaxbElement) throws SerializationException {

    final Class<?> delegateeClass = RevocationState.class;
    if (delegateeClass.isAssignableFrom(jaxbElement.getDeclaredType())) {
      this.delegatee = (JAXBElement<RevocationState>) jaxbElement;
      this.parameterListHelper = new ParameterListHelper(getRevocationState().getParameter());
    } else {
      throw new SerializationException(ErrorMessages.malformedClass(delegateeClass.getSimpleName()));
    }
  }

  /**
   * Returns the delegatee (JAXB object).
   */
  public RevocationState getRevocationState() {
    return (RevocationState) JAXBIntrospector.getValue(delegatee);
  }


  @Override
  protected String createParameterUriBasedOnParameterName(final String parameterName) {
    // return ParameterBaseName.NreUpdateResponseParameterName(parameterName);
    return parameterName;
  }



  public URI getRaPublicKeyId() throws ConfigurationException {
    return (URI) getParameter(RA_PUBLIC_KEY_ID);
  }

  private void setRaPublicKeyId(final URI raPublicKeyId) {
    setParameter(RA_PUBLIC_KEY_ID, raPublicKeyId);
  }

  public Integer getEpoch() throws ConfigurationException {
    return (Integer) getParameter(EPOCH);
  }

  private void setEpoch(int epoch) {
    setParameter(EPOCH, epoch);
  }

  public BigInt getAccumulatorValue() throws ConfigurationException {
    return (BigInt) getParameter(ACCUMULATOR_VALUE);
  }

  public void setAccumulatorValue(final HiddenOrderGroupElement accumulatorValue) {
    setParameter(ACCUMULATOR_VALUE, accumulatorValue.toBigInt());
  }

  public XMLGregorianCalendar getLastChangeDate() throws ConfigurationException {
    final String eventDateString = (String) getParameter(LAST_CHANGE_DATE);
    return EncodeDecode.decodeDateFromString(eventDateString);
  }

  public void setLastChangeDate(final XMLGregorianCalendar lastChangeDate) {

    final String eventDateString = EncodeDecode.encodeDateAsString(lastChangeDate);

    setParameter(LAST_CHANGE_DATE, eventDateString);
  }

  public BigInt generateRevocationHandle(final SystemParameters systemParameters, final PublicKey publicKey,
                                         final RandomGeneration randomGeneration) throws ConfigurationException {

    final ClPublicKeyWrapper rapkWrapper = new ClPublicKeyWrapper(publicKey);
    if (!getRaPublicKeyId().equals(rapkWrapper.getPublicKeyId())) {
      throw new ConfigurationException(
          ErrorMessages.elementIdsMismatch("revocation authority public key."));
    }

    final BigInt revocationHandle =
        getRandomPrime(systemParameters, rapkWrapper.getPublicKey(), randomGeneration);
    // TODO would be nice to have the list of revocation handles again somewhere
    // while (revocationHandles.contains(revocationHandle)) {
    // revocationHandle = getRandomPrime(rapkWrapper.getPublicKey());
    // }
    // revocationHandles.add(revocationHandle);

    return revocationHandle;
  }

  /**
   * Generate a random prime number suitable for adding to the accumulator.
   * 
   * @throws ConfigurationException
   * @throws KeyManagerException
   */
  private BigInt getRandomPrime(final SystemParameters systemParameters, final PublicKey publicKey,
                                final RandomGeneration randomGeneration) throws ConfigurationException {

    final EcryptSystemParametersWrapper spWrapper = new EcryptSystemParametersWrapper(systemParameters);
    final int messageLength = spWrapper.getAttributeLength();
    final int primeProbability = spWrapper.getPrimeProbability();
    return randomGeneration.generateRandomPrime(messageLength, primeProbability);
  }



  /**
   * Generate an empty accumulator.
   * 
   * @throws ConfigurationException
   */
  public static ClRevocationStateWrapper getEmptyAccumulator(final SystemParameters systemParameters,
                                                             final PublicKey pk, final GroupFactory gf) throws ConfigurationException {
    final ClRevocationAuthorityPublicKeyWrapper pkWrapper = new ClRevocationAuthorityPublicKeyWrapper(pk);
    final HiddenOrderGroup group = pkWrapper.getGroup(gf);
    final HiddenOrderGroupElement base = group.valueOfNoCheck(pkWrapper.getBase(0));
    return new ClRevocationStateWrapper(systemParameters, pkWrapper.getPublicKey(),
        RevocationAuthorityConfiguration.initialEpoch(), base, null);
  }


  /**
   * Extract the latest state of the accumulator from an event. It is recommended to use
   * applyEvent() instead.
   * 
   * @throws ConfigurationException
   */
  public static ClRevocationStateWrapper getStateFromLastEvent(final SystemParameters systemParameters,
                                                               final PublicKey publicKey, final AccumulatorEvent lastEvent) throws KeyManagerException,
      ConfigurationException {
    return new ClRevocationStateWrapper(systemParameters, publicKey, lastEvent.getNewEpoch(),
        lastEvent.getFinalAccumulatorValue(), lastEvent.getEventDate());
  }

  /**
   * Apply an event to an accumulator, yielding a new state. This method is the preferred way of
   * updating the state.
   * 
   * @param previous The current state of the accumulator
   * @param event The event to apply
   * @param check Perform a consistency check?
   * @return The state of the accumulator after having applied the event.
   * 
   * @throws ConfigurationException
   */
  public static ClRevocationStateWrapper applyEvent(ClRevocationStateWrapper previous,
      ClRevocationEventWrapper event, RevocationAuthorityPublicKeyWrapper rapkWrapper,
      GroupFactory gf) throws ConfigurationException {

    // Check that the event's epoch matches the state's epoch
    if (previous.getEpoch() + 1 != event.getNewEpoch()) {
      throw new RuntimeException("Incompatible state and event in AccumulatorState:applyEvent");
    }

    final BigInt modulus = ((ClRevocationAuthorityPublicKeyWrapper) rapkWrapper).getModulus();
    final HiddenOrderGroup group = ((ClRevocationAuthorityPublicKeyWrapper) rapkWrapper).getGroup(gf);
    final HiddenOrderGroupElement eventAccValue = group.valueOf(event.getAccumulatorValue());
    if (Configuration.debug()) {
      if (!rapkWrapper.getPublicKeyId().equals(previous.getRaPublicKeyId())) {
        throw new RuntimeException(
            "Public key supplied to revocation state wrapper does not have the same id as the public key used for the previous event.");
      }

      final HiddenOrderGroupElement oldAcc = eventAccValue.multOp(event.getRevocationHandle());

      if (Configuration.debug()) {
        System.out.println();
        System.err.println("Applying event");
        System.err.println("    newEpoch          : " + event.getNewEpoch());
        System.err.println("    revocation handle : " + event.getRevocationHandle());
        System.err.println("    modulus           : " + modulus);
        System.err.println("    accumulator (pre) : " + oldAcc);
        System.err.println("    accumulator (post): " + event.getAccumulatorValue());
      }

      if (!oldAcc.equals(group.valueOf(previous.getAccumulatorValue()))) {
        throw new RuntimeException("Incorrect final accumulator value when applying event (del)");
      }
    }

    return new ClRevocationStateWrapper(null, rapkWrapper.getPublicKey(), event.getNewEpoch(),
        eventAccValue, event.getEventDate());
  }



  // // TODO move to appropriate class
  // private BigIntFactory bigIntFactory = null;
  // private EcryptSystemParametersWrapper spWrapper;
  //
  // // TODO move to appropriate class
  // /**
  // * Generate a prime number suitable for adding to the accumulator. This function assigns the
  // prime
  // * numbers sequentially.
  // *
  // * @param lastPrime The last value that was output by this function, or null if this is the
  // first
  // * event.
  // * @return The next prime after lastPrime, or 3 if lastPrime is null
  // * @throws ConfigurationException
  // */
  // public BigInt getNextPrime(@Nullable BigInt lastPrime) throws ConfigurationException {
  //
  // if (bigIntFactory == null) {
  // bigIntFactory = pkWrapper.getModulus().getFactory();
  // }
  //
  // if (lastPrime == null) {
  // return bigIntFactory.valueOf(3);
  // } else if (lastPrime.mod(bigIntFactory.valueOf(2)).longValue() == 0) {
  // throw new RuntimeException("LastPrime is an even number; epected an odd number.");
  // } else {
  // BigInt TWO = bigIntFactory.two();
  // do {
  // lastPrime = lastPrime.add(TWO);
  // } while (!lastPrime.isProbablePrime(spWrapper.getPrimeProbability()));
  // return lastPrime;
  // }
  // }



  // // TODO move to appropriate class
  // public SystemParameters getSystemParameters() {
  // if (spWrapper == null) {
  // return null;
  // }
  // return spWrapper.getSystemParameters();
  // }


  // TODO same code in ClRevocationStateWrapper and RevocationInformationFacade
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
  public static ClRevocationStateWrapper deserialize(final String nreUpdateRequest)
      throws SerializationException {
    return new ClRevocationStateWrapper(JaxbHelperClass.deserialize(nreUpdateRequest));
  }
}
