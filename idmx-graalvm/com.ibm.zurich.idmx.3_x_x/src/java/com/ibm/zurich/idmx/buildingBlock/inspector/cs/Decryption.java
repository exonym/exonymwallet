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

import com.ibm.zurich.idmx.exception.ConfigurationException;
import com.ibm.zurich.idmx.exception.ProofException;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
//import com.ibm.zurich.idmx.interfaces.util.group.GroupElement;
import com.ibm.zurich.idmx.interfaces.util.group.GroupFactory;
//import com.ibm.zurich.idmx.interfaces.util.group.HiddenOrderGroupElement;
import com.ibm.zurich.idmx.interfaces.util.group.PaillierGroup;
import com.ibm.zurich.idmx.interfaces.util.group.PaillierGroupElement;

import eu.abc4trust.xml.ModuleInZkProof;
import eu.abc4trust.xml.PrivateKey;
import eu.abc4trust.xml.ValueWithHashInZkProof;
import eu.abc4trust.xml.ZkProof;

public class Decryption {
  private static BigIntFactory bigIntFactory;

  public Decryption(final BigIntFactory bigIntFactory) {
    Decryption.bigIntFactory = bigIntFactory;
  }

  public final BigInt decrypt(final PaillierGroupElement[] ciphertext, final byte[] label, /* Inspector */
                              final PrivateKey isk) throws ProofException, ConfigurationException {
    final PaillierGroupElement u = ciphertext[0];
    final PaillierGroupElement e = ciphertext[1];
    final PaillierGroupElement v = ciphertext[2];
    final CsSecretKeyWrapper iskWrapper = new CsSecretKeyWrapper(isk);
    final BigInt n = iskWrapper.getModulus();
    final BigInt n2 = iskWrapper.getN2();
    final BigInt hk = iskWrapper.getHashKey();

    if (!(v.toBigInt()).equals(abs(v.toBigInt(), n2))) {
      throw new ProofException("Decryption failed.");
    }

    final BigInt x1 = iskWrapper.getX1();
    final BigInt x2 = iskWrapper.getX2();
    final BigInt x3 = iskWrapper.getX3();

    // exp = 2*(x2 + Hash(hk,u,e,L)*x3)
    final CsInspectorHelper CsHelper = new CsInspectorHelper();
    BigInt exp = CsHelper.computeHash(hk, u, e, label, iskWrapper.getHashFunction(), bigIntFactory);
    exp = exp.multiply(x3);
    exp = exp.add(x2);
    exp = exp.shiftLeft(1);
    if (!u.multOp(exp).equals(v.op(v))) {
      throw new ProofException("Decryption failed.");
    }

    // t = 2^{-1} mod n
    final BigInt t = bigIntFactory.two().modInverse(n);

    // mHat = (e/u^x1)^2t
    PaillierGroupElement mHat = u;
    mHat = mHat.multOp(x1);
    mHat = mHat.invert();
    mHat = mHat.op(e);
    mHat = mHat.multOp(t.shiftLeft(1));

    BigInt mHatInt = mHat.toBigInt();
    // check if mHat = 1+an mod n^2 for some a, i.e., mHat = 1 mod n
    if (!mHatInt.mod(n).equals(bigIntFactory.one())) {
      throw new ProofException("Decryption failed.");
    }

    // message is given by (mHat - 1) mod n mod n^2
    mHatInt = mHatInt.mod(n2);
    mHatInt = mHatInt.subtract(bigIntFactory.one());
    mHatInt = mHatInt.divide(n);
    return mHatInt;
  }

  public final BigInt decrypt(final ZkProof proof, final String identifierOfModule, final byte[] label,/* Inspector */
                              final PrivateKey isk, final GroupFactory groupFactory) throws ProofException, ConfigurationException {
    // find the correct module that contains the ciphertext to decrypt
    ModuleInZkProof module = null;
    for (final ModuleInZkProof m : proof.getModule()) {
      if (m.getName().equals(identifierOfModule)) {
        module = m;
        break;
      }
    }

    if (module == null) {
      throw new ProofException("Decryption failed.");
    }

    final CsSecretKeyWrapper iskWrapper = new CsSecretKeyWrapper(isk);
    final BigInt n = iskWrapper.getModulus();
    final PaillierGroup group = groupFactory.createPaillierGroup(n);
    // find the ciphertext components in the DValues of the module
    byte[] uByte = null;
    byte[] eByte = null;
    byte[] vByte = null;
    for (final ValueWithHashInZkProof valueWithHash : module.getDValue()) {
      if (valueWithHash.getName().equals(identifierOfModule + ":ciphertext:u")) {
        uByte = valueWithHash.getValue();
      } else if (valueWithHash.getName().equals(identifierOfModule + ":ciphertext:e")) {
        eByte = valueWithHash.getValue();
      } else if (valueWithHash.getName().equals(identifierOfModule + ":ciphertext:v")) {
        vByte = valueWithHash.getValue();
      }
    }

    if (uByte == null || eByte == null || vByte == null) {
      throw new ProofException("Decryption failed.");
    }

    final PaillierGroupElement[] ciphertext = new PaillierGroupElement[3];
    ciphertext[0] = group.valueOf(uByte);
    ciphertext[1] = group.valueOf(eByte);
    ciphertext[2] = group.valueOf(vByte);

    return decrypt(ciphertext, label, isk);
  }

  //TODO(ksa) i see this multiple times - refactor?
  private static BigInt abs(final BigInt a, final BigInt n2) {
    final BigInt n2Half = n2.shiftRight(1);
    if (a.compareTo(n2Half) > 0) {
      return (n2.subtract(a).mod(n2));
    } else {
      return a.mod(n2);
    }
  }
}
