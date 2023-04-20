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
package com.ibm.zurich.idmx.buildingBlock.rangeProof.fourSq;

import java.security.SecureRandom;
import java.util.Arrays;

import org.junit.Ignore;
import org.junit.Test;

import com.ibm.zurich.idmx.buildingBlock.rangeProof.fourSq.RabinShallitDecomposition;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.util.bigInt.BigIntFactoryImpl;

public class RabinShallitSpeedTest {
  

  /*
Total time:      225.790977765 seconds for 100000 iterations.
Min:             0.569391 ms
25th percentile: 1.16349 ms
median:          1.650019 ms
75th percentile: 2.624219 ms
90th percentile: 4.220644 ms
95th percentile: 5.682893 ms
99th percentile: 9.775064 ms
Max:             47.352252 ms
Average:         2.2579097776499997 ms.
   */
  @Test
  @Ignore
  public void runDecomposition() {
    SecureRandom sr = new SecureRandom();
    int primeProbability = 80;
    BigIntFactory bigIntFactory = new BigIntFactoryImpl();
    RabinShallitDecomposition rabin = new RabinShallitDecomposition(bigIntFactory);
    
    // Warm up
    for(int i=0;i<10;++i) {
      BigInt r = bigIntFactory.random(256, sr);
      rabin.decomposeInteger(r, primeProbability);
    }
    
    int iter = 100000;
    BigInt r[] = new BigInt[iter];
    for(int i=0;i<iter;++i) {
      r[i] = bigIntFactory.random(256, sr);
    }
    
    long times[] = new long[iter];
    long startTime = System.nanoTime();
    for(int i=0;i<iter;++i) {
      long startTime2 = System.nanoTime();
      rabin.decomposeInteger(r[i], primeProbability);
      times[i] = System.nanoTime() - startTime2;
    }
    long time = System.nanoTime() - startTime;
    
    System.out.println("Total time:      " + time / 1e9 + " seconds for " + iter + " iterations.");
    Arrays.sort(times);
    System.out.println("Min:             " + times[0] / 1e6 + " ms");
    System.out.println("25th percentile: " + times[iter/4] / 1e6 + " ms");
    System.out.println("median:          " + times[iter/2] / 1e6 + " ms");
    System.out.println("75th percentile: " + times[iter*3/4] / 1e6 + " ms");
    System.out.println("90th percentile: " + times[iter*9/10] / 1e6 + " ms");
    System.out.println("95th percentile: " + times[iter*19/20] / 1e6 + " ms");
    System.out.println("99th percentile: " + times[iter*99/100] / 1e6 + " ms");
    System.out.println("Max:             " + times[iter-1] / 1e6 + " ms");
    System.out.println("Average:         " + time / 1e6 / iter + " ms.");
  }
}
