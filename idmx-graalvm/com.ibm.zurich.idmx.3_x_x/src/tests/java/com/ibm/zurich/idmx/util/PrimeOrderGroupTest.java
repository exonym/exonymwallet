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
package com.ibm.zurich.idmx.util;

import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.RandomGeneration;
import com.ibm.zurich.idmx.interfaces.util.group.KnownOrderGroup;
import com.ibm.zurich.idmx.util.bigInt.BigIntFactoryImpl;
import com.ibm.zurich.idmx.util.group.GroupFactoryImpl;

public class PrimeOrderGroupTest {

  public void testPrimeOrderGroup() {
    BigIntFactory bigIntFactory = new BigIntFactoryImpl();
    RandomGeneration rg = new RandomGenerationImpl(bigIntFactory);
    
    BigInt mod;
    BigInt subgroupOrd;
    
    mod = rg.generateRandomPrime(200, 80);
    subgroupOrd = rg.generateRandomPrime(100, 80);
    
    KnownOrderGroup group = new GroupFactoryImpl().createPrimeOrderGroup(mod, subgroupOrd);
    
    
  }
  
}
