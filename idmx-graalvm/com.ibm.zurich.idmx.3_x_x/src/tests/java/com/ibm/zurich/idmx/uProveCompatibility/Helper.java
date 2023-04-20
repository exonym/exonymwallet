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
package com.ibm.zurich.idmx.uProveCompatibility;

import com.ibm.zurich.idmx.buildingBlock.signature.uprove.BrandsKeyPairWrapper;
import com.ibm.zurich.idmx.buildingBlock.signature.uprove.BrandsPublicKeyWrapper;
import com.ibm.zurich.idmx.buildingBlock.signature.uprove.BrandsSecretKeyWrapper;
import com.ibm.zurich.idmx.buildingBlock.systemParameters.EcryptSystemParametersWrapper;
import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.SerializationException;
import com.ibm.zurich.idmx.interfaces.configuration.Constants;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.TestVectorHelper;

import java.net.URI;

import static org.junit.Assert.assertTrue;

class Helper {
  public static EcryptSystemParametersWrapper generateSystemParameters(TestVectorHelper tvHelper,
      BigIntFactory bigIntFactory) throws ConfigurationException, SerializationException {
    EcryptSystemParametersWrapper sp = new EcryptSystemParametersWrapper();
    sp.setSystemParametersId(URI.create("uprove-subgroup-2048-256"));
    sp.setSecurityLevel(113);
    sp.setHashFunction(tvHelper.getValueAsString("UIDh"));
    sp.setPrimeProbability(113);
    sp.setImplementationVersion(Constants.IMPLEMENTATION_VERSION);
    sp.setAttributeLength(255);
    sp.setStatisticalZeroKnowledge(80);
    BigInt p = tvHelper.getValueAsBigInt("p");
    BigInt q = tvHelper.getValueAsBigInt("q");
    BigInt g = tvHelper.getValueAsBigInt("g");
    BigInt h = tvHelper.getValueAsBigInt("g11");
    sp.setDHModulus(p);
    sp.setDHModulusLength(p.bitLength());
    sp.setDHSubgroupOrder(q);
    sp.setDHSubgroupLength(q.bitLength());
    sp.setDHGenerator1(g);
    sp.setDHGenerator2(h);

    BigInt one = bigIntFactory.one();
    assertTrue(p.isProbablePrime(sp.getPrimeProbability()));
    assertTrue(q.isProbablePrime(sp.getPrimeProbability()));
    assertTrue(p.mod(q).equals(one));
    assertTrue(g.modPow(q, p).equals(one));
    assertTrue(h.modPow(q, p).equals(one));

    return sp;
  }

  public static BrandsKeyPairWrapper generateKeyPair(EcryptSystemParametersWrapper sp,
      TestVectorHelper tvHelper, BigIntFactory bigIntFactory) throws ConfigurationException,
      SerializationException {
    BigInt one = bigIntFactory.one();
    BigInt p = sp.getDHModulus();
    BigInt q = sp.getDHSubgroupOrder();

    BrandsKeyPairWrapper kp = new BrandsKeyPairWrapper();

    // Determine number of attributes
    int numAttributes = 0;
    while (true) {
      if (tvHelper.valueExists("e" + (numAttributes + 1))) {
        numAttributes++;
      } else {
        break;
      }
    };

    BrandsSecretKeyWrapper sk = kp.getUProvePrivateKeyWrapper();
    sk.setPublicKeyId(URI.create("uprovecompatibility:ip"));
    BigInt y0 = tvHelper.getValueAsBigInt("y0");
    sk.setY0(y0);

    BrandsPublicKeyWrapper pk = kp.getUProvePublicKeyWrapper();
    pk.setPublicKeyTechnology(URI.create("urn:idmx:3.0.0:block:sig:uprove"));
    pk.setSystemParametersId(sp.getSystemParametersId());
    pk.setPublicKeyId(sk.getPublicKeyId());
    pk.setMaximalNumberOfAttributes(numAttributes);
    pk.setNumberOfUProveTokens(1);
    BigInt g0 = sp.getDHGenerator1().modPow(y0, sp.getDHModulus());
    tvHelper.checkValue(g0, "g0");
    pk.setG0(g0);
    for (int i = 1; i <= numAttributes; ++i) {
      BigInt gi = tvHelper.getValueAsBigInt("g" + i);
      pk.setGI(gi, i);
      assertTrue(gi.modPow(q, p).equals(one));
      BigInt ei = tvHelper.getValueAsBigInt("e" + i);
      pk.setEI(ei.intValue(), i);
    }
    BigInt gt = tvHelper.getValueAsBigInt("gt");
    pk.setGT(gt);
    assertTrue(gt.modPow(q, p).equals(one));
    BigInt gd = tvHelper.getValueAsBigInt("gd");
    pk.setGExt(gd);
    assertTrue(gd.modPow(q, p).equals(one));
    BigInt S = tvHelper.getValueAsBigInt("S");
    pk.setS(S);
    BigInt uidP = tvHelper.getValueAsBigInt("UIDp");
    pk.setUidP(uidP);

    return kp;
  }
}
