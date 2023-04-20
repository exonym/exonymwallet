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
package com.ibm.zurich.idmx.buildingBlock.inequality.fourSq;

import java.util.Arrays;

import org.junit.Ignore;
import org.junit.Test;

import com.ibm.zurich.idmx.buildingBlock.rangeProof.fourSq.LipmaaDecomposition;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.RandomGeneration;
import com.ibm.zurich.idmx.util.RandomGenerationImpl;
import com.ibm.zurich.idmx.util.bigInt.BigIntFactoryImpl;

public class LipmaaSpeedTest {
  /*
	Total time:      1543.98989881 seconds for 100000 iterations.
	Min:             2.012256 ms
	25th percentile: 6.170183 ms
	median:          11.174977 ms
	75th percentile: 19.965198 ms
	90th percentile: 32.397929 ms
	95th percentile: 42.265738 ms
	99th percentile: 66.178667 ms
	Max:             195.763924 ms
	Average:         15.4398989881 ms.
   */
	
	private static final BigIntFactory bigIntFactory = new BigIntFactoryImpl();
	private static final RandomGeneration rg = new RandomGenerationImpl(bigIntFactory);
	private static final LipmaaDecomposition li = new LipmaaDecomposition(bigIntFactory);
  @Test
  @Ignore
  public void runDecomposition() {
    int primeProbability = 80;
    
    // Warm up
    for(int i=0;i<100;++i) {
      BigInt r = rg.generateRandomNumber(256);
      li.decomposeInteger(r, primeProbability);
    }
    
    int iter = 100000;
    BigInt r[] = new BigInt[iter];
    for(int i=0;i<iter;++i) {
      r[i] = rg.generateRandomNumber(256);
    }
    
    long times[] = new long[iter];
    long startTime = System.nanoTime();
    for(int i=0;i<iter;++i) {
      long startTime2 = System.nanoTime();
      li.decomposeInteger(r[i], primeProbability);
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
