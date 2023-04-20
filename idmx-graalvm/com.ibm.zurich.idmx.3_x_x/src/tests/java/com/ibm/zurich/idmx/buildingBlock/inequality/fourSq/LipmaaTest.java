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


import java.math.BigInteger;
import java.util.Random;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.ibm.zurich.idmx.buildingBlock.rangeProof.fourSq.LipmaaDecomposition;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.util.bigInt.BigIntFactoryImpl;

public class LipmaaTest {
	
	private static final BigIntFactory bigIntFactory = new BigIntFactoryImpl();
	private static final LipmaaDecomposition lipmaa = new LipmaaDecomposition(bigIntFactory); 
	
  @Test
  public void test1To100000() {
	  for(int i = 1;i<=100000;i++) {
		  BigInt toDecompose = bigIntFactory.valueOf(i);
		  BigInt[] b = lipmaa.decomposeInteger(toDecompose, 100);
		  testResult(b, toDecompose);	  
	  }
  }
	
	
  @Test
  public void testReallyBigNumber() {
	  BigInt toDecompose = bigIntFactory.valueOf(new BigInteger("35215346545212313213864635496846352153465452123132138646354968463521534654521231321386463549684635215346545212313213864635496846352153465452123132138646354968463521"));
	  BigInt[] b = lipmaa.decomposeInteger(toDecompose, 100);
	  testResult(b, toDecompose);
  }
  
  @Test
  @Ignore
  public void testReallyBigPrime() {
	  BigInt toDecompose = bigIntFactory.valueOf(BigInteger.probablePrime(4096, new Random(1337)));
	  BigInt[] b = lipmaa.decomposeInteger(toDecompose, 100);
	  testResult(b, toDecompose);
  }
  
  @Test
  public void testNegativeNumbers() {
	  BigInt toDecompose = bigIntFactory.valueOf(-1);
	  try {
	    lipmaa.decomposeInteger(toDecompose, 100);
		  Assert.fail("A negative number cannot be decomposed.");
	  }
	  catch(IllegalArgumentException ex) {
		  Assert.assertNotNull(ex);
	  }
	  
	  toDecompose = bigIntFactory.valueOf(-1000000000000L);
	  try {
	    lipmaa.decomposeInteger(toDecompose, 100);
		  Assert.fail("A negative number cannot be decomposed.");
	  }
	  catch(IllegalArgumentException ex) {
		  Assert.assertNotNull(ex);
	  }
	  
	  
	  toDecompose = bigIntFactory.valueOf(-2305843009213693951L);
	  try {
	    lipmaa.decomposeInteger(toDecompose, 100);
		  Assert.fail("A negative number cannot be decomposed.");
	  }
	  catch(IllegalArgumentException ex) {
		  Assert.assertNotNull(ex);
	  }
  }
  
  @Test
  public void testZero() {
	  BigInt toDecompose = bigIntFactory.zero();
	  BigInt[] b = lipmaa.decomposeInteger(toDecompose, 100);
	  testResult(b, toDecompose);
  }
  
  
  @Test
  public void testOne() {
	  BigInt toDecompose = bigIntFactory.one();
	  BigInt[] b = lipmaa.decomposeInteger(toDecompose, 100);
	  testResult(b, toDecompose);
	  int amountZeroes = 0;
	  for(int i = 0;i<4;i++) {
		  if(b[i].equals(bigIntFactory.zero())) {
			  amountZeroes++;
		  }
	  }
	  Assert.assertEquals(amountZeroes, 3);
  }
  
  @Test
  public void testThree() {
	  BigInt toDecompose = bigIntFactory.valueOf(3);
	  BigInt[] b = lipmaa.decomposeInteger(toDecompose, 100);
	  testResult(b, toDecompose);
	  int amountZeroes = 0;
	  for(int i = 0;i<4;i++) {
		  if(b[i].equals(bigIntFactory.zero())) {
			  amountZeroes++;
		  }
	  }
	  Assert.assertEquals(amountZeroes, 1);
  }
  
  

  
  @Test
  public void testNonPrime() {
	  BigInt toDecompose = bigIntFactory.valueOf(10000000000L);
	  BigInt[] b = lipmaa.decomposeInteger(toDecompose, 100);
	  testResult(b, toDecompose);
  }
  
  @Test
  public void testSmallMersenne() {
	  BigInt toDecompose = bigIntFactory.valueOf(2305843009213693951L);
	  BigInt[] b = lipmaa.decomposeInteger(toDecompose, 100);
	  testResult(b, toDecompose);
  }
  
  @Test
  @Ignore
  public void testBigMersenne() {
	  BigInt toDecompose = bigIntFactory.two();
	  toDecompose = toDecompose.pow(2281);
	  toDecompose = toDecompose.subtract(bigIntFactory.one());
	  BigInt[] b = lipmaa.decomposeInteger(toDecompose, 100);
	  testResult(b, toDecompose);
  }
  
  
  
  @Test
  @Ignore
  public void testMod1() {
	  BigInt toDecompose = bigIntFactory.valueOf(BigInteger.probablePrime(2048, new Random(1337)));
	  while(!toDecompose.isProbablePrime(100) || !toDecompose.mod(bigIntFactory.valueOf(4)).equals(bigIntFactory.one())) {
		  toDecompose = toDecompose.add(bigIntFactory.two());
	  }
	  BigInt[] b = lipmaa.decomposeInteger(toDecompose, 100);
	  testResult(b, toDecompose);
	  
	  //An integer mod 1 is _always_ composed out of two prime squares
	  //int amountZeroes = 0;
	  //for(int i = 0;i<4;i++) {
	  //  if(b[i] == BigInt.ZERO) {
	  //	amountZeroes++;
  	  //  }
	  //}
	  //Not the case for Lipmaa...
	  //Assert.assertEquals(amountZeroes, 2);
  }

  @Test
  @Ignore
  public void testMod3() {
	  BigInt toDecompose = bigIntFactory.valueOf(BigInteger.probablePrime(2048, new Random(1337)));
	  while(!toDecompose.isProbablePrime(100) || !toDecompose.mod(bigIntFactory.valueOf(4)).equals(bigIntFactory.valueOf(3))) {
		  toDecompose = toDecompose.add(bigIntFactory.two());
	  }
	  BigInt[] b = lipmaa.decomposeInteger(toDecompose, 100);
	  testResult(b, toDecompose);
	  
	  //Must be at least three non-zeroes - Note: 3 = 1^2 + 1^2 + 1^2 + 0^2
	  int amountZeroes = 0;
	  for(int i = 0;i<4;i++) {
		  if(b[i] == bigIntFactory.zero()) {
			  amountZeroes++;
		  }
	  }
	  Assert.assertTrue(amountZeroes < 2);
  }
  
  @Test
  public void speedTestFail() {
	  BigInt toDecompose = bigIntFactory.valueOf(new BigInteger("521298527675228197231378821556075597380891527370606992188807914537763020107"));
	  BigInt[] b = lipmaa.decomposeInteger(toDecompose, 100);
	  testResult(b, toDecompose);
  }
  
  @Test
  public void testSmallPrime() {
	  BigInt toDecompose = bigIntFactory.valueOf(31337);
	  BigInt[] b = lipmaa.decomposeInteger(toDecompose, 100);
	  testResult(b, toDecompose);
  }
  
  private void testResult(BigInt[] toTest, BigInt input) {
	  //Always four return values
	  Assert.assertTrue(toTest.length == 4);
	  //Check that the result equals the integer to decompose
	  BigInt expected = bigIntFactory.zero();
	  for(int i = 0;i<4;i++) {
		  expected = expected.add(toTest[i].multiply(toTest[i]));
	  }
	  if(!expected.equals(input)) {
		  System.out.println("stop");
	  }
	  Assert.assertEquals(expected, input);
	  
	  //if results need to be ordered - not needed yet
	  //Assert.assertTrue(toTest[0].compareTo(toTest[1]) == 0 || toTest[0].compareTo(toTest[1]) == 1);
	  //Assert.assertTrue(toTest[1].compareTo(toTest[2]) == 0 || toTest[1].compareTo(toTest[2]) == 1);
	  //Assert.assertTrue(toTest[2].compareTo(toTest[3]) == 0 || toTest[2].compareTo(toTest[3]) == 1);
  }
  
}
