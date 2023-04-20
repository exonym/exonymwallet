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

import java.io.Serializable;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import com.ibm.zurich.idmx.annotations.Nullable;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.group.HiddenOrderGroupElement;

import eu.abc4trust.xml.PrivateKey;

/**
 * This class represents an event, i.e., the removal of a value from the accumulator. The
 * accumulator value is not changed when "adding" an element to it, and this there is no event for
 * that; instead, one simply extracts the witness of the value to "add".
 */
public class AccumulatorEvent implements Serializable {

  private static final long serialVersionUID = -6290889092486960875L;

  private final int newEpoch;
  private final BigInt removedPrime;
  //TODO(ksa) why is this a XML calendar
  private final XMLGregorianCalendar eventDate;
  private final HiddenOrderGroupElement finalAccumulatorValue;

  /**
   * Construct an AccumulatorEvent from XML
   */
  public AccumulatorEvent(final int newEpoch, final BigInt accumulatedPrime, final XMLGregorianCalendar eventDate,
                          final HiddenOrderGroupElement finalAccumulatorValue) {
    super();
    this.newEpoch = newEpoch;
    this.removedPrime = accumulatedPrime;
    this.eventDate = eventDate;
    this.finalAccumulatorValue = finalAccumulatorValue;
  }

  /**
   * Construct an AccumulatorEvent to remove a prime from the accumulator given the secret key of
   * the accumulator. This method is faster than the one taking the whole history.
   * 
   * @throws ConfigurationException
   */
  //TODO(ksa) this should be unknown order group as well?
  public static AccumulatorEvent removePrime(final AccumulatorState currentState,
                                             final BigInt primeToRemove, @Nullable XMLGregorianCalendar date, final PrivateKey sk)
      throws ConfigurationException {

    final ClRevocationSecretKeyWrapper skWrapper = new ClRevocationSecretKeyWrapper(sk);

    if (!skWrapper.getModulus().equals(currentState.getPublicKeyWrapper().getModulus())) {
      throw new RuntimeException("Using invalid private key in AccumulatorEvent:removePrime");
    }
    final int newEpoch = currentState.getEpoch() + 1;
    if (date == null) {
      date = now();
    }
    // acc = acc^( prime^-1 mod phi ) mod n

    // TODO make this into group operations!
    final BigIntFactory bf = skWrapper.getSafePrimeP().getFactory();
    final BigInt order = skWrapper.getSafePrimeP().subtract(bf.one()).multiply(skWrapper.getSafePrimeQ().subtract(bf.one()));
    
    final BigInt inv = primeToRemove.modInverse(order);
    final HiddenOrderGroupElement acc = currentState.getAccumulatorValue().multOp(inv);   
    
    return new AccumulatorEvent(newEpoch, primeToRemove, date, acc);
  }

  private static XMLGregorianCalendar now() {
    try {
      return DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar());
    } catch (final DatatypeConfigurationException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * The epoch of the accumulator after this event has been applied.
   */
  public int getNewEpoch() {
    return newEpoch;
  }

  /**
   * The value that was added/removed from the accumulator
   */
  public BigInt getAccumulatedPrime() {
    return removedPrime;
  }

  /**
   * The date at which the event was created.
   */
  public XMLGregorianCalendar getEventDate() {
    return eventDate;
  }

  /**
   * The value of the accumulator after applying the event.
   */
  public HiddenOrderGroupElement getFinalAccumulatorValue() {
    return finalAccumulatorValue;
  }


  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result =
        prime * result + ((finalAccumulatorValue == null) ? 0 : finalAccumulatorValue.hashCode());
    result = prime * result + newEpoch;
    result = prime * result + ((removedPrime == null) ? 0 : removedPrime.hashCode());
    return result;
  }


  @Override
  public boolean equals(final Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    final AccumulatorEvent other = (AccumulatorEvent) obj;
    if (finalAccumulatorValue == null) {
      if (other.finalAccumulatorValue != null) return false;
    } else if (!finalAccumulatorValue.equals(other.finalAccumulatorValue)) return false;
    if (newEpoch != other.newEpoch) return false;
    if (removedPrime == null) {
      if (other.removedPrime != null) return false;
    } else if (!removedPrime.equals(other.removedPrime)) return false;
    return true;
  }


  @Override
  public String toString() {
    return "AccumulatorEvent [newEpoch=" + newEpoch + ", removedPrime=" + removedPrime
        + ", eventDate=" + eventDate + ", finalAccumulatorValue=" + finalAccumulatorValue + "]";
  }

}
