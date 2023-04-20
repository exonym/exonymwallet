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
package com.ibm.zurich.idmx.util.group;

import com.ibm.zurich.idmx.configuration.ErrorMessages;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.RandomGeneration;
import com.ibm.zurich.idmx.interfaces.util.group.GroupElement;
import com.ibm.zurich.idmx.interfaces.util.group.PaillierGroup;
import com.ibm.zurich.idmx.interfaces.util.group.PaillierGroupElement;
import com.ibm.zurich.idmx.interfaces.util.group.PaillierGroupMultOpSequence;

public class PaillierGroupImpl implements PaillierGroup {


  private final BigInt n; /* n= p*q, p, q safe primes. */
  private final BigInt modulus; /* n^2 */
  private final BigIntFactory bigIntFactory;
  private final PaillierGroupElement neutralElement;
  private final PaillierGroupElement h; /* order n element */

  /**
   * Creates a the group from n= p*q with p and q safe primes, that is, p= 2p'+1 and q= 2q'+1. l(p')
   * = l(q'). The group operation is multiplication modulo n^2 and the resulting group Z_{n^2}^*.
   * Relevant operations are performed in the non-trivial subgroups of the group.
   * 
   * @param modulus
   */
  public PaillierGroupImpl(final BigInt n) {
    this.n = n;
    this.modulus = n.multiply(n);
    this.bigIntFactory = modulus.getFactory();
    this.neutralElement = this.valueOf(bigIntFactory.one());

    // TODO check correctness
    // TODO mod probably not required as it is guaranteed to be in the group already
    this.h = valueOf(n.add(bigIntFactory.one().mod(modulus)));
  }

  @Override
  public PaillierGroupElement neutralElement() {
    return neutralElement;
  }

  /**
   * Creates a random element of the full group Z_{n^2} with a probability distribution
   * statistically close to uniform.
   */
  @Override
  public PaillierGroupElementImpl createRandomElement(final RandomGeneration rg) {
    // TODO replace arg 80
    // 0 < ge < modulus
    BigInt ge;
    do {
      ge = rg.generateRandomNumber(bigIntFactory.one(), modulus.subtract(bigIntFactory.one()), 80);
    } while (!(ge.gcd(modulus).compareTo(bigIntFactory.one()) == 0));
    // loop will be executed exactly once with overwhelming probability; check could be removed

    return new PaillierGroupElementImpl(this, ge);
  }

  /**
   * Create a random generator of the subgroup G_{n'}G_2G_T by killing elements of G_n. TODO What
   * about G_2? maybe details here
   */
  @Override
  public PaillierGroupElementImpl createRandomGenerator(final RandomGeneration rg) {
    final GroupElement<PaillierGroup, PaillierGroupElement, PaillierGroupMultOpSequence> ge = createRandomElement(rg);
    final BigInt g = ge.toBigInt().modPow(n.multiply(bigIntFactory.two()), modulus);

    return new PaillierGroupElementImpl(this, g);
  }

  @Override
  public BigInt createRandomIterationcounter(final RandomGeneration rg, final int statisticalInd) {
    final BigInt exp =
        rg.generateRandomNumber(bigIntFactory.one(), modulus.shiftRight(2), statisticalInd);
    // TODO check: group order ~ modulus / 4; sufficiently close estimation of order
    return exp;
  }

  @Override
  public PaillierGroupElementImpl unserializeElement(final byte[] b) {
    // TODO check unsigned
    return new PaillierGroupElementImpl(this, bigIntFactory.unsignedValueOf(b));
  }

  @Override
  public String toString() {
    return "PaillierGroup [modulus = " + this.n + "^2]";
  }

  @Override
  public byte[] getGroupDescription() {
    return this.toString().getBytes();
  }

  @Override
  public PaillierGroupElementImpl valueOf(final BigInt val) {
    final PaillierGroupElementImpl ge = valueOfNoCheck(val);
    final BigInt gcd = ge.toBigInt().gcd(modulus);
    if (gcd.compareTo(bigIntFactory.one()) != 0) {
      throw new ArithmeticException(ErrorMessages.illegalGroupElement());
    }
    return ge;
  }

  @Override
  public PaillierGroupElementImpl valueOf(final byte[] val) {
    final PaillierGroupElementImpl ge = valueOfNoCheck(val);
    final BigInt gcd = ge.toBigInt().gcd(modulus);
    if (gcd.compareTo(bigIntFactory.one()) != 0) {
      throw new ArithmeticException(ErrorMessages.illegalGroupElement());
    }
    return ge;
  }

  @Override
  public PaillierGroupElementImpl valueOfNoCheck(final BigInt val) {
    return new PaillierGroupElementImpl(this, val);
  }

  @Override
  public PaillierGroupElementImpl valueOfNoCheck(final byte[] val) {
    BigInt bi = bigIntFactory.unsignedValueOf(val);
    return new PaillierGroupElementImpl(this, bi);
  }

  @Override
  public PaillierGroupMultOpSequence initializeSequence() {
    return new PaillierGroupMOS(this, bigIntFactory);
  }

  // TODO Which subgroup?
  @Override
  public BigInt getOrder() {
    // TODO Auto-generated method stub
    return null;
  }

  public PaillierGroupElement getH() {
    return h;
  }

  @Override
  public BigInt invertIterationcounter(final BigInt iterationcounter) {
    throw new ArithmeticException(
        "This operation is not possible in this group as the order is not known.");
  }

  public BigInt getModulus() {
    return modulus;
  }

  public BigInt getN() {
    return n;
  }

  public BigIntFactory getBigIntFactory() {
    return bigIntFactory;
  }

  @Override
  public int getRandomIterationcounterLength(final int statisticalInd) {
    return modulus.bitLength() - 2 + statisticalInd;
  }

  @Override
  public BigInt createRandomIterationcounterForSubgroupOfOrderPhiN(final RandomGeneration rg,
		  final int statisticalInd) {
    final BigInt exp = rg.generateRandomNumber(bigIntFactory.one(), n.shiftRight(2), statisticalInd);
    // TODO check: group order ~ modulus / 4; sufficiently close estimation of order
    return exp;
  }

  @Override
  public int getRandomIterationcounterLengthForSubgroupOfOrderPhiN(final int statisticalInd) {
    return n.bitLength() - 2 + statisticalInd;
  }

}
