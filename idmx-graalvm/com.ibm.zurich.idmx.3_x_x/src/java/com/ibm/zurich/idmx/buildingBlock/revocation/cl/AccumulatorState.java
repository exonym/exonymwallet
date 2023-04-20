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

import javax.xml.datatype.XMLGregorianCalendar;

import com.ibm.zurich.idmx.annotations.Nullable;
import com.ibm.zurich.idmx.buildingBlock.systemParameters.EcryptSystemParametersWrapper;
import com.ibm.zurich.idmx.configuration.ErrorMessages;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.RandomGeneration;
import com.ibm.zurich.idmx.interfaces.util.group.GroupFactory;
import com.ibm.zurich.idmx.interfaces.util.group.HiddenOrderGroup;
import com.ibm.zurich.idmx.interfaces.util.group.HiddenOrderGroupElement;
import com.ibm.zurich.idmx.util.RandomGenerationImpl;

import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.xml.PublicKey;
import eu.abc4trust.xml.SystemParameters;

/**
 * Class representing the current state of the accumulator. Note that this class is immutable.
 */
public class AccumulatorState implements Serializable {

  private static final long serialVersionUID = 277395457449121800L;

  private final ClRevocationAuthorityPublicKeyWrapper pkWrapper;
  private final int epoch;
  private final HiddenOrderGroupElement accumulatorValue;
  private final XMLGregorianCalendar lastChange;

  // TODO remove after the methods for getting the primes are removed
  private final EcryptSystemParametersWrapper spWrapper;

  /**
   * Construct the state based on XML.
   * 
   * @throws ConfigurationException
   */
  public AccumulatorState(final SystemParameters systemParameters, final PublicKey pk, final int epoch,
                          final HiddenOrderGroupElement accumulatorValue, final @Nullable XMLGregorianCalendar lastChange)
      throws ConfigurationException {

    this.spWrapper = new EcryptSystemParametersWrapper(systemParameters);
    this.pkWrapper = new ClRevocationAuthorityPublicKeyWrapper(pk);

    if (!spWrapper.getSystemParametersId().equals(pkWrapper.getSystemParametersId())) {
      throw new ConfigurationException(
          ErrorMessages
              .parameterWrong("system parmeters id in public key and in key manager do not match."));
    }

    this.epoch = epoch;
    this.accumulatorValue = accumulatorValue;
    this.lastChange = lastChange;
  }

  // /**
  // * Construct the state based on XML. Load the public key from storage.
  // */
  // public AccumulatorState(URI publicKeyUri, int epoch, BigInteger accumulatorValue, /* nullable
  // */
  // XMLGregorianCalendar lastChange) {
  // this.pkWrapper = new ClPublicKeyWrapper(StructureStore.getInstance().get(publicKeyUri);
  // this.epoch = epoch;
  // this.accumulatorValue = accumulatorValue;
  // this.lastChange = lastChange;
  // }

  /**
   * Generate an empty accumulator.
   * 
   * @throws ConfigurationException
   */
  public static AccumulatorState getEmptyAccumulator(final SystemParameters systemParameters, final PublicKey pk,
                                                     final GroupFactory gf)
      throws ConfigurationException {
    final ClRevocationAuthorityPublicKeyWrapper pkWrapper = new ClRevocationAuthorityPublicKeyWrapper(pk);
    final HiddenOrderGroup group = pkWrapper.getGroup(gf);
    final HiddenOrderGroupElement base = group.valueOfNoCheck(pkWrapper.getBase(0));
    return new AccumulatorState(systemParameters, pkWrapper.getPublicKey(), 0, base, null);
  }

  /**
   * Extract the latest state of the accumulator from an event. It is recommended to use
   * applyEvent() instead.
   * 
   * @throws ConfigurationException
   */
  public static AccumulatorState getStateFromLastEvent(final SystemParameters systemParameters,
                                                       final PublicKey publicKey, final AccumulatorEvent lastEvent) throws KeyManagerException,
      ConfigurationException {
    return new AccumulatorState(systemParameters, publicKey, lastEvent.getNewEpoch(),
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
  public static AccumulatorState applyEvent(final AccumulatorState previous, final AccumulatorEvent event,
                                            final boolean check) throws ConfigurationException {

    // Check that the event's epoch matches the state's epoch
    if (previous.getEpoch() + 1 != event.getNewEpoch()) {
      throw new RuntimeException("Incompatible state and event in AccumulatorState:applyEvent");
    }

    if (check) {
      final HiddenOrderGroupElement oldAcc =
          event.getFinalAccumulatorValue().multOp(event.getAccumulatedPrime());
      if (!oldAcc.equals(previous.accumulatorValue)) {
        throw new RuntimeException("Incorrect final accumulator value when applying event (del). "
          + "Actual: " + oldAcc + " New: " + previous.accumulatorValue);
      }
    }

    return new AccumulatorState(previous.getSystemParameters(), previous.getPublicKey(),
        event.getNewEpoch(), event.getFinalAccumulatorValue(), event.getEventDate());
  }



  // TODO move to appropriate class
  private BigIntFactory bigIntFactory = null;

  /**
   * Generate a prime number suitable for adding to the accumulator. This function assigns the prime
   * numbers sequentially.
   * 
   * @param lastPrime The last value that was output by this function, or null if this is the first
   *        event.
   * @return The next prime after lastPrime, or 3 if lastPrime is null
   * @throws ConfigurationException
   */
  //TODO(ksa) why isnt this static? Why is argument not stored?
  public BigInt getNextPrime(final @Nullable BigInt lastPrime) throws ConfigurationException {

    if (bigIntFactory == null) {
      bigIntFactory = pkWrapper.getModulus().getFactory();
    }

    if (lastPrime == null) {
      return bigIntFactory.valueOf(3);
    } else if (!lastPrime.isProbablePrime(spWrapper.getPrimeProbability())) {
      throw new RuntimeException("LastPrime is a not a prime!");
    } else {
      BigInt TWO = bigIntFactory.two();
      BigInt counter = lastPrime;
      do {
        counter = counter.add(TWO);
      } while (!counter.isProbablePrime(spWrapper.getPrimeProbability()));
      return counter;
    }
  }

  // TODO move to appropriate class
  /**
   * Generate a random prime number suitable for adding to the accumulator.
   * 
   * @throws ConfigurationException
   */
  public BigInt getRandomPrime() throws ConfigurationException {
    final RandomGeneration randomGeneration = new RandomGenerationImpl(bigIntFactory);
    final int messageLength = spWrapper.getAttributeLength();
    final int primeProbability = spWrapper.getPrimeProbability();
    return randomGeneration.generateRandomPrime(messageLength, primeProbability);
  }

  // TODO move to appropriate class
  public SystemParameters getSystemParameters() {
    return spWrapper.getSystemParameters();
  }


  /**
   * The epoch (number of events that have been applied to this accumulator) of this accumulator.
   */
  public int getEpoch() {
    return epoch;
  }

  /**
   * The current value of the accumulator
   */
  public HiddenOrderGroupElement getAccumulatorValue() {
    return accumulatorValue;
  }

  /**
   * The public key wrapper of this accumulator.
   */
  ClRevocationAuthorityPublicKeyWrapper getPublicKeyWrapper() {
    return pkWrapper;
  }

  /**
   * The public key of this accumulator
   */
  public PublicKey getPublicKey() {
    return pkWrapper.getPublicKey();
  }

  /**
   * Returns the date of the last time the accumulator was changed, or null if the accumulator has
   * never been updated
   */
  public XMLGregorianCalendar getLastChange() {
    return lastChange;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((accumulatorValue == null) ? 0 : accumulatorValue.hashCode());
    result = prime * result + epoch;
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    final AccumulatorState other = (AccumulatorState) obj;
    if (accumulatorValue == null) {
      if (other.accumulatorValue != null) return false;
    } else if (!accumulatorValue.equals(other.accumulatorValue)) return false;
    if (epoch != other.epoch) return false;
    return true;
  }

  @Override
  public String toString() {
    return "AccumulatorState [pk=" + pkWrapper.getPublicKeyId().toString() + ", epoch=" + epoch
        + ", accumulatorValue=" + accumulatorValue + ", lastChange=" + lastChange + "]";
  }
}
