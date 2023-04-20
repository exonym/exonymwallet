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
import com.ibm.zurich.idmx.interfaces.util.group.GroupFactory;
import com.ibm.zurich.idmx.interfaces.util.group.HiddenOrderGroup;
import com.ibm.zurich.idmx.interfaces.util.group.KnownOrderGroup;
import com.ibm.zurich.idmx.interfaces.util.group.PaillierGroup;

public class GroupFactoryImpl implements GroupFactory {

  @Override
  public KnownOrderGroup createPrimeOrderGroup(final BigInt modulus, final BigInt subgroupOrder) {
    return new PrimeOrderGroupImpl(modulus, subgroupOrder);
  }

  @Override
  public HiddenOrderGroup createSignedQuadraticResiduesGroup(final BigInt modulus) {
    return new SignedQuadraticResiduesGroupImpl(modulus);
  }

  @Override
  public HiddenOrderGroup createSRSAGroup(final BigInt modulus) {
    return new SRSAGroupImpl(modulus);
  }

  @Override
  public PaillierGroup createPaillierGroup(final BigInt n) {
    return new PaillierGroupImpl(n);
  }

  @Override
  public KnownOrderGroup createResiduesModPQGroup(final BigInt p, final BigInt q) {
    return new ResiduesModpqGroup(p.getFactory(), p, q);
  }

}