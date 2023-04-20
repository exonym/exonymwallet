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

package com.ibm.zurich.idmx.buildingBlock.signature.cl;

import java.util.List;

import com.ibm.zurich.idmx.configuration.ErrorMessages;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.Pair;
import com.ibm.zurich.idmx.interfaces.util.RandomGeneration;
//import com.ibm.zurich.idmx.interfaces.util.group.Group;
//import com.ibm.zurich.idmx.interfaces.util.group.GroupElement;
import com.ibm.zurich.idmx.interfaces.util.group.GroupFactory;
import com.ibm.zurich.idmx.interfaces.util.group.HiddenOrderGroup;
import com.ibm.zurich.idmx.interfaces.util.group.HiddenOrderGroupElement;

/**
 * 
 */
public class ClHelper {

  private ClHelper() {
    throw new AssertionError(ErrorMessages.nonInstantiationErrorMessage());
  }

  static HiddenOrderGroupElement computeQ(GroupFactory groupFactory, ClPublicKeyWrapper pk,
                                          HiddenOrderGroupElement commitment, List<BigInt> attributes, BigInt credSpecId, BigInt randomizerExponent)
      throws ConfigurationException {

    HiddenOrderGroup group = groupFactory.createSignedQuadraticResiduesGroup(pk.getModulus());

    HiddenOrderGroupElement capU = commitment;
    HiddenOrderGroupElement capZ = group.valueOfNoCheck(pk.getZ());
    HiddenOrderGroupElement capS = group.valueOfNoCheck(pk.getS());
    HiddenOrderGroupElement capRt = group.valueOfNoCheck(pk.getRt());

    HiddenOrderGroupElement capQ = capU.opMultOp(capS, randomizerExponent);
    capQ = capQ.opMultOp(capRt, credSpecId);
    for (int i = 0; i < attributes.size(); i++) {
      BigInt attributeValue = attributes.get(i);
      if (attributeValue != null) {
        HiddenOrderGroupElement capR = group.valueOfNoCheck(pk.getBase(i));
        capQ = capQ.opMultOp(capR, attributeValue);
      }
    }
    capQ = capQ.invert();
    capQ = capZ.op(capQ);
    return capQ;
  }


  public static void generateSecretKey(RandomGeneration randomGeneration, BigIntFactory bigIntFactory,
      ClSecretKeyWrapper sk, int rsaModulusLength, int primeProbability)
      throws ConfigurationException {

    Pair<BigInt, BigInt> pq = ClHelper.getPQ(randomGeneration, rsaModulusLength, primeProbability);

    BigInt p = pq.first;
    BigInt q = pq.second;
    BigInt n = p.multiply(q);

    // p = 2*p' + 1, q = 2*q' - 1 <-> p' = (p - 1)/2, q' = (q - 1)/2
    BigInt pPrime = p.subtract(bigIntFactory.one()).shiftRight(1);
    BigInt qPrime = q.subtract(bigIntFactory.one()).shiftRight(1);

    // add values to the key
    sk.setModulus(n);
    sk.setSafePrimeP(p);
    sk.setSafePrimeQ(q);
    sk.setSophieGermainPrimeP(pPrime);
    sk.setSophieGermainPrimeQ(qPrime);
  }

  /**
   * To return a pair of p, q such that n = p*q and p = 2p' + 1, q = 2q' + 1 with p, q, p', q'
   * prime.
   * 
   * @param modulusLength Length of modulus.
   * @param primeCertainty Probability for prime testing.
   * 
   */
  static final Pair<BigInt, BigInt> getPQ(final RandomGeneration randomGeneration,
      final int modulusLength, final int primeCertainty) {

    BigInt _n;
    BigInt _p;
    BigInt _q;
    do {

      _p = randomGeneration.generateRandomSafePrime(modulusLength / 2, primeCertainty);
      do {
        _q =
            randomGeneration.generateRandomSafePrime(modulusLength - (modulusLength / 2),
                primeCertainty);
        // make sure p and q are unequal
      } while (_p.equals(_q));

      _n = _p.multiply(_q);

    } while (_n.bitLength() != modulusLength);

    return new Pair<BigInt, BigInt>(_p, _q);
  }



}
