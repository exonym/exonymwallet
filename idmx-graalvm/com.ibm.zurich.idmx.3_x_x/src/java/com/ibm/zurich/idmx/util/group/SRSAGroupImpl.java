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
import com.ibm.zurich.idmx.interfaces.util.group.HiddenOrderGroup;
import com.ibm.zurich.idmx.interfaces.util.group.HiddenOrderGroupElement;
import com.ibm.zurich.idmx.interfaces.util.group.HiddenOrderGroupMultOpSequence;

public class SRSAGroupImpl implements HiddenOrderGroup {

  private final BigInt modulus;
  private final BigIntFactory bigIntFactory;

  public SRSAGroupImpl(final BigInt modulus) {
    this.modulus = modulus;
    this.bigIntFactory = modulus.getFactory();
  }

  @Override
  public String toString() {
    return "SRSAGroup [modulus=" + modulus + "]";
  }

  @Override
  public HiddenOrderGroupElement neutralElement() {
    return new SRSAGroupElementImpl(this, bigIntFactory.one());
  }

  public BigInt getModulus() {
    return modulus;
  }

  @Override
  public HiddenOrderGroupElement createRandomElement(final RandomGeneration rg) {
    return createRandomGenerator(rg);
  }

  /**
   * Compute a generator of the group of quadratic residue modulo <tt>n</tt>. The generator will not
   * be part of the subgroup of size 2.
   * 
   * @return group generator of group of quadratic residues modulo <tt>n</tt>.
   */
  @Override
  public HiddenOrderGroupElement createRandomGenerator(final RandomGeneration rg) {
    // TODO check algorithm
    BigInt qr;
    do {
      qr = rg.generateRandomNumber(modulus);
      // verify that the qr is a generator but not of the subgroup of size 2
      qr = qr.modPow(bigIntFactory.two(), modulus);
    } while (qr.equals(bigIntFactory.one())
        || !modulus.gcd(qr.subtract(bigIntFactory.one())).equals(bigIntFactory.one()));
    return new SRSAGroupElementImpl(this, qr);
  }

  @Override
  public BigInt createRandomIterationcounter(final RandomGeneration rg, final int statisticalInd) {
    return rg.generateRandomNumber(modulus.shiftRight(2), statisticalInd);
  }

  public boolean checkGroupMembership(BigInt val) {
	// TODO implement properly
    //boolean result = false;
    //if (val.compareTo(bigIntFactory.zero()) > 0 && val.compareTo(modulus) < 0) {
    //  result = true;
    //}

    throw new RuntimeException("Not yet implemented");
    //return result;
  }

  @Override
  public SRSAGroupElementImpl unserializeElement(final byte[] b) {
    // TODO Auto-generated method stub
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public byte[] getGroupDescription() {
    return this.toString().getBytes();
  }

  @Override
  public SRSAGroupElementImpl valueOf(final BigInt val) {
    // val = mapIntoGroup(val.mod(modulus)); // TODO: do we really want this here?
    final SRSAGroupElementImpl ge = valueOfNoCheck(val);
    if (checkGroupMembership(val) == true) {
      return ge;
    } else {
      throw new ArithmeticException(ErrorMessages.illegalGroupElement());
    }
  }

  @Override
  public SRSAGroupElementImpl valueOf(final byte[] val) {
    final SRSAGroupElementImpl ge = valueOfNoCheck(val);
    if (checkGroupMembership(ge.toBigInt()) == true) {
      return ge;
    } else {
      throw new ArithmeticException(ErrorMessages.illegalGroupElement());
    }
  }

  @Override
  public SRSAGroupElementImpl valueOfNoCheck(final BigInt val) {
    return new SRSAGroupElementImpl(this, val);
  }

  @Override
  public SRSAGroupElementImpl valueOfNoCheck(final byte[] val) {
    final BigInt bi = bigIntFactory.unsignedValueOf(val);
    return new SRSAGroupElementImpl(this, bi);
  }

  @Override
  public HiddenOrderGroupMultOpSequence initializeSequence() {
    return new SRSAGroupMOS(this, bigIntFactory);
  }


  @Override
  public int hashCode() {
    final int prime = 31;
    int result = prime + ((modulus == null) ? 0 : modulus.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    final SRSAGroupImpl other = (SRSAGroupImpl) obj;
    if (modulus == null) {
      if (other.modulus != null) return false;
    } else if (!modulus.equals(other.modulus)) return false;
    return true;
  }

  // Specific for Hidden-Order Groups
  @Override
  public BigInt getBoundOnOrder() {
    return getModulus();
  }

  @Override
  public Integer getRandomIterationcounterLength(final int statisticalInd) {
    return getBoundOnOrder().bitLength() + 1 + statisticalInd;
  }
}
