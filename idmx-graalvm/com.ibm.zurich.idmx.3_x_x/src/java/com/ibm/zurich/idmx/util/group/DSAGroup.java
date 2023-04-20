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
///*~~~~
// * Copyright notice IBM
// ~~~~*/
//
//package com.ibm.zurich.idmix.group;
//
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.security.MessageDigest;
//import java.security.NoSuchAlgorithmException;
//
//import com.ibm.zurich.idmx.interfaces.util.BigInt;
//import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
//import com.ibm.zurich.idmx.interfaces.util.RandomGeneration;
//import com.ibm.zurich.idmx.util.NumberComparison;
//
///**
// * 
// */
//public class DSAGroup {
//
//	private static int[][] acceptableBitlengths;
//	private RandomGeneration randomGeneration;
//	private MessageDigest messageDigest;
//	private int primeProbability;
//	private BigInt p;
//	private BigInt q;
//	private byte[] domainParameterSeed;
//
//	public DSAGroup() {
//		acceptableBitlengths = initAcceptableBitlengths();
//
//		// TODO: the user should be able to select one of the FIPS 180-4 algorithms
//		try {
//			messageDigest = MessageDigest.getInstance("SHA-256");
//		} catch (NoSuchAlgorithmException e) {
//			e.printStackTrace();
//		}
//		// TODO: the user should be allowed to set the prime probability
//		primeProbability = 80;
//
//		BigInt[] pq = generateGroupDescription(2048, 256, 256);
//		p = pq[0];
//		q = pq[1];
//	}
//
//	/**
//	 * Initialises the possible modulus length and group order combinations according to the FIPS
//	 * 186-3 standard.
//	 * 
//	 * @return
//	 */
//	private int[][] initAcceptableBitlengths() {
//
//		int[][] allowedBitlengths = { { 1024, 160 }, { 2048, 224 }, { 2048, 256 }, { 3072, 256 } };
//		return allowedBitlengths;
//	}
//
//	private BigInt[] generateGroupDescription(int modulusBitlength, int subgroupOrderBitlength,
//			int seedLength) {
//
//		int L = modulusBitlength;
//		int N = subgroupOrderBitlength;
//		int outlen = N;
//
//		// step 1: verify that modulus length and subgroup order build an acceptable pair according
//		if (!checkGroupAndSoubgroupOrderBitlength(L, N)) {
//			return null;
//		}
//
//		// step 2: verify that the seed is of length larger than the bit length of the group order
//		if (seedLength < N) {
//			return null;
//		}
//
//		// generate q the prime divisor of (p-1)
//		BigInt q, p = null;
//		byte[] seed;
//		// step 10
//		int offset = 1;
//		do {
//			do {
//				// step 5
//				seed = getDomainParameterSeed(seedLength);
//
//				BigInt twoToNMinusOne = BigIntImpl.TWO.pow(N - 1);
//				// step 6
//				BigInt U = new BigIntImpl(messageDigest.digest(seed)).mod(twoToNMinusOne);
//				// step 7
//				q = twoToNMinusOne.add(U).add(BigIntImpl.ONE).subtract(U.mod(BigIntImpl.TWO));
//			}
//			// step 8 (make this comply with FIPS 186-3 Appendix C.3)
//			while (!q.isProbablePrime(primeProbability));
//
//			// computation of p
//			// step 3
//			int n = (int) Math.ceil(L / (double) outlen) - 1;
//			// step 4
//			int b = L - 1 - (n * outlen);
//
//			// step 11
//			for (int counter = 0; counter < (4 * L - 1); counter++) {
//				BigInt V;
//				BigInt W = BigIntImpl.ZERO;
//
//				// step step 11.1
//				for (int j = 0; j < n; j++) {
//					// step 11.2
//					BigInt argument = new BigIntImpl(seed).add(BigIntImpl.valueOf(offset + j).mod(
//							BigIntImpl.TWO.pow(seedLength)));
//					V = new BigIntImpl(messageDigest.digest(argument.toByteArray()));
//
//					// step 11.2
//					W = W.add(V.multiply(BigIntImpl.TWO.pow(j * outlen)));
//				}
//				// iteration for j = n
//				BigInt argument = new BigIntImpl(seed).add(BigIntImpl.valueOf(offset + n).mod(
//						BigIntImpl.TWO.pow(seedLength)));
//				V = new BigIntImpl(messageDigest.digest(argument.toByteArray()));
//				W = W.add(V.mod(BigIntImpl.TWO.pow(b)).multiply(BigIntImpl.TWO.pow(n * outlen)));
//
//				// step 11.3
//				BigInt X = W.add(BigIntImpl.TWO.pow(L - 1));
//				// step 11.4
//				BigInt c = X.mod(q.shiftLeft(1));
//				// step 11.5
//				p = X.subtract(c.subtract(BigIntImpl.ONE));
//			}
//			// step 11.9
//			offset = offset + n + 1;
//		}
//		// step 11.6
//		// step 11.7 (make comply with FIPS-186-3 Appendix C.3)
//		while (p.bitLength() < (L - 1) || !p.isProbablePrime(primeProbability));
//
//		// step 11.8
//		BigInt[] pq = new BigInt[2];
//		pq[0] = p;
//		pq[1] = q;
//
//		return pq;
//	}
//
//	private byte[] getDomainParameterSeed(int seedLength) {
//		if (domainParameterSeed == null) {
//			domainParameterSeed = randomGeneration.generateRandomNumber(seedLength).toByteArray();
//		}
//		return domainParameterSeed;
//	}
//
//	/**
//	 * Verifies that the given bit lengths for the prime order group and the subgroup are a pair
//	 * that is acceptable to the standard FIPS 186-3. Returns false in case the given bit lengths do
//	 * comply with the standard.
//	 * 
//	 * @param groupOrderBitlength
//	 * @param subgroupOrderBitlength
//	 */
//	private boolean checkGroupAndSoubgroupOrderBitlength(int groupOrderBitlength,
//			int subgroupOrderBitlength) {
//		for (int i = 0; i < acceptableBitlengths.length; i++) {
//			if (groupOrderBitlength == acceptableBitlengths[i][1]
//					&& subgroupOrderBitlength == acceptableBitlengths[i][2]) {
//				return true;
//			}
//		}
//		return false;
//	}
//
//	private BigInt computeGeneratorNondeterministically(BigInt p, BigInt q) {
//		BigInt g = null;
//
//		BigInt order = p.subtract(BigIntImpl.ONE);
//		// step 1
//		BigInt e = order.divide(q);
//		do {
//			// step 2
//			BigInt h = randomGeneration.generateRandomNumber(order);
//			// step 3
//			g = h.modPow(e, p);
//		}
//		// step 4
//		while (g.equals(BigIntImpl.ONE));
//
//		return g;
//	}
//
//	public boolean basicGeneratorValidation(BigInt p, BigInt q, BigInt g) {
//		// step 1
//		if (!NumberComparison.isInInterval(g, BigIntImpl.TWO, p.subtract(BigIntImpl.ONE))) {
//			return false;
//		}
//		// step 2
//		if (g.modPow(q, p).equals(BigIntImpl.ONE)) {
//			return true;
//		}
//		// step 3
//		return false;
//	}
//
//	public BigInt computeGeneratorDeterministically(BigInt p, BigInt q) {
//		BigInt g = null;
//
//		// step 1
//		// omitting the index for now
//
//		// step 2
//		int N = q.bitLength();
//
//		int seedLength = N;
//
//		// step 3
//		BigInt e = p.subtract(BigIntImpl.ONE).divide(q);
//		// step 4
//		int count = 0;
//		do {
//			// step 5
//			count += 1;
//
//			// step 7
//			byte[] U = getByteArrayForGenerator(seedLength, count);
//
//			// step 8
//			BigInt W = new BigIntImpl(messageDigest.digest(U));
//			// step 9
//			g = W.modPow(e, p);
//		}
//		// step 6
//		// step 10
//		while (count != 0 || g.compareTo(BigIntImpl.TWO) < 0);
//
//		return g;
//	}
//
//	private byte[] getByteArrayForGenerator(int seedLength, Integer count) {
//		ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
//		try {
//			byteArray.write(getDomainParameterSeed(seedLength));
//			byteArray.write("ggen".getBytes());
//			// TODO: add index here
//			byteArray.write(count.byteValue());
//		} catch (IOException e1) {
//			e1.printStackTrace();
//		}
//		return byteArray.toByteArray();
//	}
//
//	public boolean completeGeneratorValidation(BigInt p, BigInt q, BigInt g) {
//
//		BigInt computedG = null;
//		// step 1
//		// TODO: verify the index
//
//		// step 2
//		// step 3
//		if (!basicGeneratorValidation(p, q, g)) {
//			return false;
//		}
//
//		// steps 4 to 12
//		computedG = computeGeneratorDeterministically(p, q);
//
//		// step 13
//		if (computedG.equals(g)) {
//			return true;
//		} else {
//			return false;
//		}
//	}
//}
