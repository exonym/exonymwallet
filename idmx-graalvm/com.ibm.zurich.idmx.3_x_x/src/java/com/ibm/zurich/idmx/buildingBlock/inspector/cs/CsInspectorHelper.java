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
package com.ibm.zurich.idmx.buildingBlock.inspector.cs;

import java.security.NoSuchAlgorithmException;

import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
//import com.ibm.zurich.idmx.interfaces.util.group.GroupElement;
//import com.ibm.zurich.idmx.interfaces.util.group.HiddenOrderGroupElement;
import com.ibm.zurich.idmx.interfaces.util.group.PaillierGroupElement;
import com.ibm.zurich.idmx.util.Hashing;

public class CsInspectorHelper {
  protected BigInt computeHash(final BigInt hk, final PaillierGroupElement u, final PaillierGroupElement e, final byte[] label,
                               final String hashFunction, final BigIntFactory bigIntFactory) throws ConfigurationException {
    final Hashing hash;
    try {
      hash = new Hashing(hashFunction);
    } catch (final NoSuchAlgorithmException exception) {
      throw new ConfigurationException(exception);
    }
    if (hk != null) {
      hash.add(hk.toByteArray());
    }
    hash.add(u.toString().getBytes());
    hash.add(e.toString().getBytes());
    hash.add(label);

    final byte[] hashOutput = hash.digestRaw();
    final BigInt hashOutputAsBigInt = bigIntFactory.unsignedValueOf(hashOutput);
    return hashOutputAsBigInt;
  }
}
