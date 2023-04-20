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
package com.ibm.zurich.idmx.proofEngine;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.ibm.zurich.idmx.buildingBlock.systemParameters.EcryptSystemParametersWrapper;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.interfaces.util.TestVectorHelper;
import com.ibm.zurich.idmx.util.Hashing;

import javax.inject.Inject;

public class HashComputationForChallenge {
  
  private final TestVectorHelper tv;
  
  @Inject
  public HashComputationForChallenge(final TestVectorHelper tv) {
    this.tv = tv;
  }


  /**
   * Computes a two-level hash from the list of hash contributions, as follows: hash(<h_1,
   * hash(<h_2, h_3, ...>)>).
   * 
   * @param hashContributions
   * @param sp
   * @return
   * @throws ConfigurationException
   */
  public byte[] getHashContributionForChallenge(final List<byte[]> hashContributions,
		  final EcryptSystemParametersWrapper sp)
      throws ConfigurationException {

    final List<byte[]> outerContribution = new ArrayList<byte[]>();
    final List<byte[]> innerContributions = new LinkedList<byte[]>(hashContributions);

    if (hashContributions.size() != 0) {
      byte[] firstContribution = innerContributions.remove(0);
      if(tv.isActive() && tv.isPresentation()) {
        tv.checkValue(firstContribution, "cp");
      }
      outerContribution.add(firstContribution);
    } else {
      outerContribution.add(new byte[0]);
    }

    final Hashing innerHashing = new Hashing(sp);
    innerHashing.addListBytes(innerContributions);
    byte[] innerHash = innerHashing.digestRaw();
    if (tv.isActive() && tv.isPresentation()) {
      innerHash = tv.getValueAsBytes("md");
      System.out
          .println("!!! Inner challenge hash overridden. This should happen only during tests.");
    }
    outerContribution.add(innerHash);

    final Hashing outerHashing = new Hashing(sp);
    outerHashing.addListBytes(outerContribution);
    return outerHashing.digestRaw();
  }
}
