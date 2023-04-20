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

import com.ibm.zurich.idmx.configuration.ErrorMessages;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.RevocationException;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.Pair;
import com.ibm.zurich.idmx.interfaces.util.group.HiddenOrderGroupElement;
import com.ibm.zurich.idmx.util.Arithmetic;

import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.xml.PrivateKey;

/**
 * This class represents a witness/value pair, used for proving that a value was indeed accumulated
 * in the accumulator.
 */
public class AccumulatorWitness implements Serializable {

  private static final long serialVersionUID = -5822719108582322362L;

  private final AccumulatorState state;
  private final BigInt value;
  private final HiddenOrderGroupElement witness;

  /**
   * Construct witness based on XML.
   */
  public AccumulatorWitness(final AccumulatorState state, final BigInt value, final HiddenOrderGroupElement witness) {
    this.state = state;
    this.value = value;
    this.witness = witness;
  }

  /**
   * Compute witness using private key.
   * 
   * @throws ConfigurationException
   */
  public static AccumulatorWitness calculateWitness(final AccumulatorState state, final BigInt value,
                                                    final PrivateKey sk) throws ConfigurationException {

    final ClRevocationSecretKeyWrapper skWrapper = new ClRevocationSecretKeyWrapper(sk);

    if (!skWrapper.getModulus().equals(state.getPublicKeyWrapper().getModulus())) {
      throw new RuntimeException("Using invalid private key in AccumulatorEvent:removePrime");
    }

    //TODO(ksa) group exponent may be faster
    //skWrapper.getSophieGermainPrimeP();
    final BigIntFactory bf = skWrapper.getSafePrimeP().getFactory();
    final BigInt order = skWrapper.getSafePrimeP().subtract(bf.one()).multiply(skWrapper.getSafePrimeQ().subtract(bf.one()));

    final BigInt valueInv = value.modInverse(order);
    final HiddenOrderGroupElement witness = state.getAccumulatorValue().multOp(valueInv);
    return new AccumulatorWitness(state, value, witness);
  }

  /**
   * Update witness based on a new event.
   * 
   * @throws ConfigurationException
   * @throws KeyManagerException
   */
  public static AccumulatorWitness updateWitness(
      final AccumulatorWitness previous, final AccumulatorEvent event, final boolean check)
      throws RevocationException, ConfigurationException, KeyManagerException {
    final BigInt n = previous.state.getPublicKeyWrapper().getModulus();

    final BigIntFactory bigIntFactory = n.getFactory();

    final AccumulatorState newState = AccumulatorState.applyEvent(previous.state, event, check);
    final HiddenOrderGroupElement newWitness;
    {
      if (!previous.value.gcd(event.getAccumulatedPrime()).equals(bigIntFactory.one())) {
        throw new RevocationException(ErrorMessages.valueHasBeenRevoked());
      }
      // find a, b st. a*value + b*prime = 1
      final Pair<BigInt, BigInt> euclid =
          Arithmetic.extendedEuclid(previous.value, event.getAccumulatedPrime());
      // newWit = oldWit^b * newAcc^a (mod n)
      final HiddenOrderGroupElement term1 = previous.witness.multOp(euclid.second);
      final HiddenOrderGroupElement term2 = event.getFinalAccumulatorValue().multOp(euclid.first);
      newWitness = term1.op(term2);
    }

    final AccumulatorWitness newAw = new AccumulatorWitness(newState, previous.value, newWitness);
    if (check) {
      if (!newAw.isConsistent()) {
        throw new RuntimeException("Witness update failed in Accumulator");
      }
    }
    return newAw;
  }

  /**
   * Check if the witness/value pair is consistent with the current state.
   * 
   * @throws ConfigurationException
   */
  public boolean isConsistent() throws ConfigurationException {
    final HiddenOrderGroupElement acc = witness.multOp(value);
    return acc.equals(state.getAccumulatorValue());
  }

  /**
   * Return accumulator state this witness/value pair is valid for
   * 
   * @return
   */
  public AccumulatorState getState() {
    return state;
  }

  /**
   * Return the value that was accumulated in the accumulator.
   * 
   * @return
   */
  public BigInt getValue() {
    return value;
  }

  /**
   * Return the witness proving that this value was accumulated in the accumulator.
   * 
   * @return
   */
  public HiddenOrderGroupElement getWitness() {
    return witness;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((state == null) ? 0 : state.hashCode());
    result = prime * result + ((value == null) ? 0 : value.hashCode());
    result = prime * result + ((witness == null) ? 0 : witness.hashCode());
    return result;
  }

  @Override
  public String toString() {
    return "AccumulatorWitness [state=" + state + ", value=" + value + ", witness=" + witness + "]";
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    final AccumulatorWitness other = (AccumulatorWitness) obj;
    if (state == null) {
      if (other.state != null) return false;
    } else if (!state.equals(other.state)) return false;
    if (value == null) {
      if (other.value != null) return false;
    } else if (!value.equals(other.value)) return false;
    if (witness == null) {
      if (other.witness != null) return false;
    } else if (!witness.equals(other.witness)) return false;
    return true;
  }
}
