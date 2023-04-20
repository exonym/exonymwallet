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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.ibm.zurich.idmx.buildingBlock.revocation.cl.AccumulatorEvent;
import com.ibm.zurich.idmx.buildingBlock.revocation.cl.AccumulatorHistory;
import com.ibm.zurich.idmx.buildingBlock.revocation.cl.AccumulatorState;
import com.ibm.zurich.idmx.buildingBlock.revocation.cl.AccumulatorWitness;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.SerializationException;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.group.GroupFactory;
import com.ibm.zurich.idmx.tests.TestUtils;
import com.ibm.zurich.idmx.util.bigInt.BigIntFactoryImpl;
import com.ibm.zurich.idmx.util.group.GroupFactoryImpl;

import eu.abc4trust.keyManager.KeyManagerException;
import eu.abc4trust.xml.KeyPair;
import eu.abc4trust.xml.PrivateKey;
import eu.abc4trust.xml.PublicKey;
import eu.abc4trust.xml.SystemParameters;

public class TestRevocation {

  private SystemParameters systemParameters;
  private KeyPair keyPair;
  private PublicKey publicKey;
  private PrivateKey secretKey;
  private GroupFactory groupFactory;
  private BigIntFactory bigIntFactory;

  @Before
  public void init() throws SerializationException, ConfigurationException {
    systemParameters = TestUtils.getResource("sp_default.xml", SystemParameters.class, this);
    keyPair = TestUtils.getResource("ra_cl.xml", KeyPair.class, this);
    publicKey = keyPair.getPublicKey();
    secretKey = keyPair.getPrivateKey();
    groupFactory = new GroupFactoryImpl();
    bigIntFactory = new BigIntFactoryImpl();
  }

  @Test
  public void testAccumulator() throws ConfigurationException, KeyManagerException {
    // Create an empty accumulator
    AccumulatorState state = AccumulatorState.getEmptyAccumulator(systemParameters, publicKey, groupFactory);

    List<BigInt> history = new ArrayList<BigInt>();
    BigInt lastPrime = null;
    final int initialSize = 20;
    // Add a few primes automatically
    for (int i = 0; i < initialSize / 2 - 1; ++i) {
      lastPrime = state.getNextPrime(lastPrime);
      history.add(lastPrime);
    }
    // Add a few primes automatically
    for (int i = initialSize / 2 - 1; i < initialSize - 1; ++i) {
      lastPrime = state.getRandomPrime();
      history.add(lastPrime);
    }
    // Add a prime manually
    {
      history.add(bigIntFactory.valueOf(1009));
    }

    // Try removing events individually & check that it works
    int[] indexes = {0, 1, 5, initialSize - 2, initialSize - 1};
    for (int i : indexes) {
      AccumulatorEvent e1 = AccumulatorEvent.removePrime(state, history.get(i), null, secretKey);
      assertEquals(e1.getNewEpoch(), 1);
      // Ignore return value
      AccumulatorState.applyEvent(state, e1, true);
    }

    // Try removing everything
    AccumulatorState s = state;
    for (int i = 0; i < initialSize; ++i) {
      AccumulatorEvent e = AccumulatorEvent.removePrime(s, history.get(i), null, secretKey);
      s = AccumulatorState.applyEvent(s, e, true);
    }
    // Remove inverse order
    s = state;
    for (int i = initialSize - 1; i >= 0; --i) {
      AccumulatorEvent e = AccumulatorEvent.removePrime(s, history.get(i), null, secretKey);
      s = AccumulatorState.applyEvent(s, e, true);
    }
  }

  @Test
  public void testNextPrime() throws ConfigurationException, KeyManagerException {
    AccumulatorState state = AccumulatorState.getEmptyAccumulator(systemParameters, publicKey, groupFactory);
    AccumulatorHistory history = new AccumulatorHistory();
    BigInt lastPrime = null;
    List<BigInt> primes = new ArrayList<BigInt>();
    Set<BigInt> primeSet = new HashSet<BigInt>();

    final int events = 50;

    for (int i = 0; i < events; ++i) {

      if (i == 11) {
        // make sure the nextPrime also works when adding primes manually
        lastPrime = state.getNextPrime(lastPrime);
        primes.add(lastPrime);
        // assert uniqueness
        assertTrue(primeSet.add(lastPrime));
      } else if (i % 3 == 1) {
        AccumulatorEvent e;
        e = AccumulatorEvent.removePrime(state, primes.get(i / 2), null, secretKey);
        state = AccumulatorState.applyEvent(state, e, true);
        history.addEvent(e);
      } else {
        lastPrime = state.getNextPrime(lastPrime);
        primes.add(lastPrime);
        // assert uniqueness
        assertTrue(primeSet.add(lastPrime));
      }
    }
  }

  @Test
  public void updateWitnesses() throws Exception {
    // Create an empty accumulator
    AccumulatorState state = AccumulatorState.getEmptyAccumulator(systemParameters, publicKey, groupFactory);
    AccumulatorHistory accHist = new AccumulatorHistory();

    // Add 20 events
    List<BigInt> history = new ArrayList<BigInt>();
    BigInt lastPrime = null;
    final int initialSize = 20;
    for (int i = 0; i < initialSize; ++i) {
      lastPrime = state.getNextPrime(lastPrime);
      history.add(lastPrime);
    }

    List<AccumulatorWitness> witnesses = new ArrayList<AccumulatorWitness>();
    // Extract witnesses
    for (int i = 0; i < initialSize; ++i) {
      AccumulatorWitness w1 = AccumulatorWitness.calculateWitness(state, history.get(i), secretKey);
      assertTrue(w1.isConsistent());
      witnesses.add(w1);
    }

    // Add 5 more primes
    final int additionalSize = 5;
    for (int i = 0; i < additionalSize; ++i) {
      lastPrime = state.getNextPrime(lastPrime);
      history.add(lastPrime);
    }

    // Remove additional primes again & update witness
    for (int i = initialSize; i < initialSize + additionalSize; ++i) {
      AccumulatorEvent e = AccumulatorEvent.removePrime(state, history.get(i), null, secretKey);
      state = AccumulatorState.applyEvent(state, e, true);
      accHist.addEvent(e);

      for (int j = 0; j < witnesses.size(); ++j) {
        witnesses.set(j, AccumulatorWitness.updateWitness(witnesses.get(j), e, true));
        assertTrue(witnesses.get(j).isConsistent());
      }
    }
    for (AccumulatorWitness w : witnesses) {
      assertEquals(w.getState().getAccumulatorValue(), state.getAccumulatorValue());
    }
    
    
  }
}
