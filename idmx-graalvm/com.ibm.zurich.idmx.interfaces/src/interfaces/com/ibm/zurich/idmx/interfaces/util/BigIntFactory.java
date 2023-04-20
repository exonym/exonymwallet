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

package com.ibm.zurich.idmx.interfaces.util;

import java.math.BigInteger;
import java.util.Random;

public interface BigIntFactory {
  public BigInt zero();

  public BigInt one();

  public BigInt two();

  public BigInt valueOf(int a);

  public BigInt valueOf(long a);

  public BigInt valueOf(BigInteger a);

  public BigInt signedValueOf(byte[] bytes);

  public BigInt unsignedValueOf(byte[] bytes);

  public BigInt random(int bitLength, Random r);

  public BigInt randomPrime(int bitLength, int certainty, Random r);
}
