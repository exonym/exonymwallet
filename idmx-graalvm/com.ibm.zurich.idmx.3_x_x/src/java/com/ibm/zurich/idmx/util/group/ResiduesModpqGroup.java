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

import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.RandomGeneration;
import com.ibm.zurich.idmx.interfaces.util.group.KnownOrderGroup;
import com.ibm.zurich.idmx.interfaces.util.group.KnownOrderGroupElement;
import com.ibm.zurich.idmx.interfaces.util.group.KnownOrderGroupMultOpSequence;

/**
 * Group of residue classes mod pq with known order, where modulus n is pq.
 */
public class ResiduesModpqGroup implements KnownOrderGroup {
  private final BigIntFactory bigIntFactory;
  @SuppressWarnings("unused")
  private final BigInt p; // prime factor of modulus
  @SuppressWarnings("unused")
  private final BigInt q; // prime factor of modulus
  private final BigInt mod; // modulus
  private final BigInt order; // order
  private final ResiduesModpqGroupElement neutral;


  public ResiduesModpqGroup(final BigIntFactory bigIntFactory, final BigInt p, final BigInt q) {
    this.bigIntFactory = bigIntFactory;
    this.p = p;
    this.q = q;
    this.mod = p.multiply(q);
    this.order = (p.subtract(bigIntFactory.one())).multiply(q.subtract(bigIntFactory.one()));
    this.neutral = new ResiduesModpqGroupElement(this, bigIntFactory.one());
  }

  @Override
  public KnownOrderGroupElement neutralElement() {
    return neutral;
  }

  @Override
  public KnownOrderGroupElement createRandomElement(final RandomGeneration rg) {
    // TODO replace arg 80 - externalize
    // 0 < ge < modulus
	final int certainty = 80;
    BigInt ge;
    do {
      ge = rg.generateRandomNumber(bigIntFactory.one(), mod.subtract(bigIntFactory.one()), certainty);
    } while (!(ge.gcd(mod).equals(bigIntFactory.one())));
    // loop will be executed exactly once with overwhelming probability; check could be removed

    return new ResiduesModpqGroupElement(this, ge);
  }

  @Override
  public KnownOrderGroupElement createRandomGenerator(final RandomGeneration rg) {
    // TODO check algorithm
    BigInt ge;
    do {
      ge = rg.generateRandomNumber(mod);
      ge = ge.modPow(bigIntFactory.two(), mod);
      // verify that the ge is a generator but not of the subgroup of size 2
    } while (ge.equals(bigIntFactory.one())
        || !mod.gcd(ge.subtract(bigIntFactory.one())).equals(bigIntFactory.one()));
    return new ResiduesModpqGroupElement(this, ge);
  }

  @Override
  public BigInt createRandomIterationcounter(final RandomGeneration rg, final int statisticalInd) {
    return rg.generateRandomNumber(mod.shiftRight(2), statisticalInd);
  }

  @Override
  public KnownOrderGroupElement unserializeElement(final byte[] b) {
    throw new RuntimeException("Not implemented.");
  }

  @Override
  public byte[] getGroupDescription() {
    throw new RuntimeException("Not implemented.");
  }

  @Override
  public KnownOrderGroupElement valueOf(final BigInt val) {
    throw new RuntimeException("Not implemented.");
  }

  @Override
  public KnownOrderGroupElement valueOf(final byte[] val) {
    throw new RuntimeException("Not implemented.");
  }

  @Override
  public KnownOrderGroupElement valueOfNoCheck(final BigInt val) {
    return new ResiduesModpqGroupElement(this, val);
  }

  @Override
  public KnownOrderGroupElement valueOfNoCheck(final byte[] val) {
    throw new RuntimeException("Not implemented.");
  }

  @Override
  public KnownOrderGroupMultOpSequence initializeSequence() {
    throw new ArithmeticException(
        "Multi-operation sequences currently not implemented for this group.");
  }

  @Override
  public BigInt getOrder() {
    return this.order;
  }

  @Override
  public BigInt invertIterationcounter(final BigInt iterationcounter) {
    return iterationcounter.modInverse(order);
  }

  public BigInt getModulus() {
    return this.mod;
  }

}
