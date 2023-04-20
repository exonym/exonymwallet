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
package com.ibm.zurich.idmx.buildingBlock.signature.uprove;

import java.security.NoSuchAlgorithmException;
import java.util.List;

import com.ibm.zurich.idmx.buildingBlock.systemParameters.EcryptSystemParametersWrapper;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.group.KnownOrderGroupElement;
import com.ibm.zurich.idmx.util.Hashing;

class BrandsSignatureHashing extends Hashing {

  private final BigInt subgroupOrder;

  public BrandsSignatureHashing(final EcryptSystemParametersWrapper sp) throws ConfigurationException {
    super(sp);
    this.subgroupOrder = sp.getDHSubgroupOrder();
  }

  public BrandsSignatureHashing(final String hashAlgorithm, final BigInt subgroupOrder)
      throws NoSuchAlgorithmException {
    super(hashAlgorithm);
    this.subgroupOrder = subgroupOrder;
  }

  public void add(final KnownOrderGroupElement e) {
    add(e.toBigInt());
  }

  public void add(final BigInt b) {
    add(b.toByteArrayUnsigned());
  }

  public void addGroupDescription(final EcryptSystemParametersWrapper sp) throws ConfigurationException {
    add(sp.getDHModulus());
    add(sp.getDHSubgroupOrder());
    add(sp.getDHGenerator1());
  }

  public void addList(List<? extends Object> list) {
    addInteger(list.size());
    for (final Object o : list) {
      if (o == null) {
        addNull();
      } else if (o instanceof byte[]) {
        add((byte[]) o);
      } else if (o instanceof BigInt) {
        add((BigInt) o);
      } else if (o instanceof KnownOrderGroupElement) {
        add((KnownOrderGroupElement) o);
      } else if (o instanceof Integer) {
        addInteger((Integer) o);
      } else if (o instanceof Byte) {
        addByte((Byte) o);
      } else {
        throw new RuntimeException("Unknown type in UProveHash: " + o.getClass().toString());
      }
    }
  }

  public BigInt digest() {
    final byte[] hashResult = digestRaw();
    final BigInt result = subgroupOrder.getFactory().unsignedValueOf(hashResult);
    return result.mod(subgroupOrder);
  }
}
