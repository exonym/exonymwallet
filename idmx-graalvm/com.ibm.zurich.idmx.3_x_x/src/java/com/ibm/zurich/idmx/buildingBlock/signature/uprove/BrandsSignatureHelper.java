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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import com.ibm.zurich.idmx.buildingBlock.systemParameters.EcryptSystemParametersWrapper;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.TestVectorHelper;
import com.ibm.zurich.idmx.interfaces.util.group.KnownOrderGroupElement;

class BrandsSignatureHelper {

  static BigInt computeXt(final EcryptSystemParametersWrapper sp, final BrandsPublicKeyWrapper pk,
                          final BigInt credentialSpecId, final int numberOfAttributes, final boolean device, final TestVectorHelper testVector)
      throws ConfigurationException {

    final byte[] P = getP(sp, pk, numberOfAttributes, device);
    testVector.checkValue(P, "P");
    
    // xt = Hash(0x01, P, TI) -> Zp
    final BrandsSignatureHashing outerHash = new BrandsSignatureHashing(sp);
    outerHash.addByte((byte) 1);
    outerHash.add(P);
    // We use the credentialSpecId as the token information field
    outerHash.add(credentialSpecId);

    return outerHash.digest();
  }

  private static byte[] getP(final EcryptSystemParametersWrapper sp, final BrandsPublicKeyWrapper pk,
                             final int numberOfAttributes, final boolean device) throws ConfigurationException {
    try {
      final BrandsSignatureHashing hashing = new BrandsSignatureHashing(sp);
  
      // P = hash(UID_P, desc(group), list(g0,..,gn,gr,gt,[gd]), list(e1,..,en, er), S)
      if(pk.hasUidP()) {
        hashing.add(pk.getUidP());
      } else {
        // We use the identifier of the public key as UID_P
        hashing.add(pk.getPublicKeyId().toString().getBytes("UTF-8"));
      }
  
      hashing.addGroupDescription(sp);
  
      final List<BigInt> listG = new ArrayList<BigInt>();
      listG.add(pk.getG0());
      for (int i = 0; i < numberOfAttributes; ++i) {
        listG.add(pk.getGI(i + 1));
      }
      listG.add(pk.getGT());
      if (device) {
        listG.add(pk.getGD());
      }
      hashing.addList(listG);
  
      List<Byte> listE = new ArrayList<Byte>();
      for (int i = 0; i < numberOfAttributes; ++i) {
        final int ei = pk.getEI(i+1);
        listE.add((byte) ei);
      }
      hashing.addList(listE);
  
      if(pk.hasS()) {
        hashing.add(pk.getS());
      } else {
        // We use the identifier of the system parameters as S
        hashing.add(pk.getSystemParametersId().toString().getBytes("UTF-8"));
      }
      
      return hashing.digestRaw();
    } catch(final UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  public static BigInt hashToken(final EcryptSystemParametersWrapper sp, final KnownOrderGroupElement h,
                                 final byte[] proverInformation, final KnownOrderGroupElement sigmaZPrime,
      final KnownOrderGroupElement sigmaAPrime, final KnownOrderGroupElement sigmaBPrime)
      throws ConfigurationException {
    final BrandsSignatureHashing hashing = new BrandsSignatureHashing(sp);
    hashing.add(h);
    hashing.add(proverInformation);
    hashing.add(sigmaZPrime);
    hashing.add(sigmaAPrime);
    hashing.add(sigmaBPrime);
    return hashing.digest();
  }

  public static byte[] getHashContribution(final byte[] tokenId, final byte[] a, final List<Integer> listOfDisclosed,
                                           final List<BigInt> valueOfDisclosed, final BigInt message, final EcryptSystemParametersWrapper sp)
      throws ConfigurationException {
    final BrandsSignatureHashing hashing = new BrandsSignatureHashing(sp);

    hashing.add(tokenId);
    hashing.add(a);
    hashing.addList(listOfDisclosed);
    hashing.addList(valueOfDisclosed);
    hashing.addNull(); // C
    hashing.addNull(); // tildeCi
    hashing.addNull(); // tildeAi
    hashing.addNull(); // p'
    hashing.addNull(); // a_p
    hashing.addNull(); // P_s
    if(message != null) {
      hashing.add(message);
    } else {
      hashing.addNull();
    }

    return hashing.digestRaw();
  }

  public static byte[] getTokenId(final BigInt h, final BigInt sigmaZPrime, final BigInt sigmaCPrime,
                                  final BigInt sigmaRPrime, final EcryptSystemParametersWrapper sp) throws ConfigurationException {
    final BrandsSignatureHashing hashing = new BrandsSignatureHashing(sp);
    hashing.add(h);
    hashing.add(sigmaZPrime);
    hashing.add(sigmaCPrime);
    hashing.add(sigmaRPrime);
    return hashing.digestRaw();
  }

  public static byte[] getTokenId(final KnownOrderGroupElement h, final KnownOrderGroupElement sigmaZPrime,
                                  final BigInt sigmaCPrime, final BigInt sigmaRPrime, final EcryptSystemParametersWrapper sp)
      throws ConfigurationException {
    return getTokenId(h.toBigInt(), sigmaZPrime.toBigInt(), sigmaCPrime, sigmaRPrime, sp);
  }
}
