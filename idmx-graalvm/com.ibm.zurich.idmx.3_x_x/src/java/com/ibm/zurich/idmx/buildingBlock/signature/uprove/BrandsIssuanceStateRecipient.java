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

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import com.ibm.zurich.idmx.interfaces.state.IssuanceStateRecipient;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.group.KnownOrderGroup;
import com.ibm.zurich.idmx.interfaces.util.group.KnownOrderGroupElement;

//TODO(ksa) not immutable - refactor

class BrandsIssuanceStateRecipient implements IssuanceStateRecipient {

  private static final long serialVersionUID = 3500169869822934803L;

  private BigInteger modulus;
  private BigInteger q;
  private byte[] g;
  private byte[] g0;
  private List<TokenState> tokenStates;
  private List<BigInteger> attributes;
  private BigInteger randomizer;
  
  public BrandsIssuanceStateRecipient() {
    tokenStates = new ArrayList<>();
  }

  public BigInt getQ(BigIntFactory bf) {
    return bf.valueOf(q);
  }

  public void setQ(BigInt q) {
    this.q = q.getValue();
  }
  
  public BigInt getModulus(BigIntFactory bf) {
    return bf.valueOf(modulus);
  }

  public void setModulus(BigInt modulus) {
    this.modulus = modulus.getValue();
  }

  public KnownOrderGroupElement getG(KnownOrderGroup group) {
    return group.valueOfNoCheck(g);
  }

  public void setG(KnownOrderGroupElement g) {
    this.g = g.serialize();
  }

  public KnownOrderGroupElement getG0(KnownOrderGroup group) {
    return group.valueOfNoCheck(g0);
  }

  public void setG0(KnownOrderGroupElement g0) {
    this.g0 = g0.serialize();
  }

  public List<TokenState> getTokenStates() {
    return tokenStates;
  }

  public void setTokenStates(List<TokenState> tokenStates) {
    this.tokenStates = tokenStates;
  }

  public List<BigInt> getAttributes(BigIntFactory bf) {
    List<BigInt> ret = new ArrayList<>();
    for(BigInteger a: attributes) {
      ret.add(bf.valueOf(a));
    }
    return ret;
  }

  public void setAttributes(List<BigInt> attributes) {
    this.attributes = new ArrayList<>();
    for(BigInt a: attributes) {
      this.attributes.add(a.getValue());
    }
  }

  public BigInt getRandomizer(BigIntFactory bf) {
    return bf.valueOf(randomizer);
  }

  public void setRandomizer(BigInt randomizer) {
    this.randomizer = randomizer.getValue();
  }

  public void addTokenState(TokenState tokenState) {
    tokenStates.add(tokenState);
  }
  
  public class TokenState implements Serializable {
    private static final long serialVersionUID = -5866890619938857645L;
    
    private BigInteger alphaInverse;
    private BigInteger beta2;
    private byte[] h;
    private byte[] sigmaAPrime;
    private byte[] sigmaBPrime;
    private BigInteger sigmaC;
    private BigInteger sigmaCPrime;
    private byte[] sigmaZPrime;
    
    private byte[] proverInformation;
    
    public BigInt getAlphaInverse(BigIntFactory bf) {
      return bf.valueOf(alphaInverse);
    }
    public void setAlphaInverse(BigInt alphaInverse) {
      this.alphaInverse = alphaInverse.getValue();
    }
    public BigInt getBeta2(BigIntFactory bf) {
      return bf.valueOf(beta2);
    }
    public void setBeta2(BigInt beta2) {
      this.beta2 = beta2.getValue();
    }
    public KnownOrderGroupElement getH(KnownOrderGroup group) {
      return group.valueOfNoCheck(h);
    }
    public void setH(KnownOrderGroupElement h) {
      this.h = h.serialize();
    }
    public KnownOrderGroupElement getSigmaAPrime(KnownOrderGroup group) {
      return group.valueOfNoCheck(sigmaAPrime);
    }
    public void setSigmaAPrime(KnownOrderGroupElement sigmaAPrime) {
      this.sigmaAPrime = sigmaAPrime.serialize();
    }
    public KnownOrderGroupElement getSigmaBPrime(KnownOrderGroup group) {
      return group.valueOfNoCheck(sigmaBPrime);
    }
    public void setSigmaBPrime(KnownOrderGroupElement sigmaBPrime) {
      this.sigmaBPrime = sigmaBPrime.serialize();
    }
    public BigInt getSigmaC(BigIntFactory bf) {
      return bf.valueOf(sigmaC);
    }
    public void setSigmaC(BigInt sigmaC) {
      this.sigmaC = sigmaC.getValue();
    }
    public BigInt getSigmaCPrime(BigIntFactory bf) {
      return bf.valueOf(sigmaCPrime);
    }
    public void setSigmaCPrime(BigInt sigmaCPrime) {
      this.sigmaCPrime = sigmaCPrime.getValue();
    }
    public KnownOrderGroupElement getSigmaZPrime(KnownOrderGroup group) {
      return group.valueOfNoCheck(sigmaZPrime);
    }
    public void setSigmaZPrime(KnownOrderGroupElement sigmaZPrime) {
      this.sigmaZPrime = sigmaZPrime.serialize();
    }
    public byte[] getProverInformation() {
      return proverInformation;
    }
    public void setProverInformation(byte[] proverInformation) {
      this.proverInformation = proverInformation;
    }
  }

}
