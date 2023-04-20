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

// TODO extend SRSAGroup (also for GroupElement, etc.)
public class SignedQuadraticResiduesGroupImpl implements HiddenOrderGroup {

  private final BigInt modulus;
  private final BigIntFactory bigIntFactory;

  /*
   * (n-1)/2
   */
  private final BigInt modMinus1By2;

  public SignedQuadraticResiduesGroupImpl(final BigInt modulus) {
    this.modulus = modulus;
    this.bigIntFactory = modulus.getFactory();
    this.modMinus1By2 = (modulus.subtract(bigIntFactory.one())).divide(bigIntFactory.two());
  }

  @Override
  public String toString() {
    return "SignedQuadraticResiduesGroup [modulus=" + modulus + "]";
  }

  @Override
  public HiddenOrderGroupElement neutralElement() {
    return new SignedQuadraticResiduesGroupElementImpl(this, bigIntFactory.one());
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
    return new SignedQuadraticResiduesGroupElementImpl(this, mapIntoGroup(qr));
  }

  @Override
  public BigInt createRandomIterationcounter(final RandomGeneration rg, final int statisticalInd) {
    return rg.generateRandomNumber(modulus.shiftRight(2), statisticalInd);
  }

  public boolean checkGroupMembership(final BigInt val) {
    boolean result = false;

    if (val.compareTo(bigIntFactory.zero()) > 0 && val.compareTo(modMinus1By2) <= 0
        && jacobiSymbol(val, modulus) == 1) {
      result = true;
    }

    return result;
  }

  public BigInt mapIntoGroup(final BigInt val) {
    BigInt ge;

    // TODO remove assertion after extensive testing due to computation time
    assert (jacobiSymbol(val, modulus) == 1);

    if (val.compareTo(modMinus1By2) == 1 && val.compareTo(modulus) == -1) {
      ge = val.subtract(modulus);
      ge = ge.abs();
    } else {
      ge = val;
    }

    // TODO remove assertion after extensive testing due to computation time
    assert (jacobiSymbol(val, modulus) == 1);

    return ge;

  }

  private int jacobiSymbol(final BigInt value, final BigInt modulus) {
    int jsym = 1;
    BigInt aLoc = value;
    BigInt nLoc = modulus;

    BigInt aPrime;
    int h;

    final BigIntFactory bif = this.bigIntFactory;

    do {
      aLoc = aLoc.mod(nLoc);

      if (aLoc.compareTo(bif.zero()) == 0) {
        if (nLoc.compareTo(bif.one()) == 0) {
          return jsym;
        } else {
          return 0;
        }
      }

      h = aLoc.getLowestSetBit();
      aPrime = aLoc.shiftRight(h);

      // int nLocInt = nLoc.intValue();

      if (!(h % 2 == 0) && !(nLoc.mod(bif.valueOf(8)).compareTo(bif.valueOf(1)) == 0)
          && !(nLoc.mod(bif.valueOf(8)).compareTo(bif.valueOf(7)) == 0)) {
        jsym = jsym * -1;
      }

      if (!(aPrime.mod(bif.valueOf(4)).compareTo(bif.one()) == 0)
          && !(nLoc.mod(bif.valueOf(4)).compareTo(bif.one()) == 0)) {
        jsym = jsym * -1;
      }

      aLoc = nLoc;
      nLoc = aPrime;

    } while (true);

  }

  @Override
  public SignedQuadraticResiduesGroupElementImpl unserializeElement(final byte[] b) {
    // TODO Auto-generated method stub
    throw new RuntimeException("not yet implemented");
  }

  @Override
  public byte[] getGroupDescription() {
    return this.toString().getBytes();
  }

  @Override
  public SignedQuadraticResiduesGroupElementImpl valueOf(final BigInt val) {
    BigInt valInGroup = mapIntoGroup(val.mod(modulus)); // TODO: do we really want this here?
    SignedQuadraticResiduesGroupElementImpl ge =
        valueOfNoCheck(valInGroup);
    if (checkGroupMembership(valInGroup) == true) {
      return ge;
    } else {
      throw new ArithmeticException(ErrorMessages.illegalGroupElement());
    }
  }

  @Override
  public SignedQuadraticResiduesGroupElementImpl valueOf(final byte[] val) {
	final SignedQuadraticResiduesGroupElementImpl ge =
        valueOfNoCheck(val);
    if (checkGroupMembership(ge.toBigInt()) == true) {
      return ge;
    } else {
      System.err.println(ge.toBigInt());
      throw new ArithmeticException(ErrorMessages.illegalGroupElement());
    }
  }

  @Override
  public SignedQuadraticResiduesGroupElementImpl valueOfNoCheck(final BigInt val) {
    return new SignedQuadraticResiduesGroupElementImpl(this, val);
  }

  @Override
  public SignedQuadraticResiduesGroupElementImpl valueOfNoCheck(final byte[] val) {
	final BigInt bi = bigIntFactory.unsignedValueOf(val);
    return new SignedQuadraticResiduesGroupElementImpl(this, bi);
  }

  @Override
  public HiddenOrderGroupMultOpSequence initializeSequence() {
    return new SignedQuadraticResiduesGroupMOS(this, bigIntFactory);
  }


  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((modulus == null) ? 0 : modulus.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    final SignedQuadraticResiduesGroupImpl other = (SignedQuadraticResiduesGroupImpl) obj;
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
