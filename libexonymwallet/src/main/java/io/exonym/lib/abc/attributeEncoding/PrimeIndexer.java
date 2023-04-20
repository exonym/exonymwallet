/*
 * Copyright (c) 2023. All Rights Reserved. Exonym GmbH
 */

package io.exonym.lib.abc.attributeEncoding;

import io.exonym.lib.abc.attributeType.EnumIndexer;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PrimeIndexer implements EnumIndexer {
  private List<BigInteger> primes;
  private Map<BigInteger, Integer> primePosition;
  
  public PrimeIndexer() {
    primes = new ArrayList<BigInteger>();
    primePosition = new HashMap<BigInteger, Integer>();
    generatePrimesToUp(23);
  }
  
  public BigInteger getNthPrime(int n) {
    if (n<0) {
      throw new RuntimeException("getNthPrime argument must be > 0");
    } else if (n < primes.size()) {
      return primes.get(n);
    } else {
      generatePrimesToUp(n);
      return primes.get(n);
    }
  }
  
  public Integer getIndexOfPrime(BigInteger p) {
    while (primes.get(primes.size()-1).compareTo(p) <= 0) {
      generatePrimesToUp(primes.size());
    }
    return primePosition.get(p);
  }
  
  private void addToPrimes(BigInteger p) {
    primePosition.put(p, primes.size());
    primes.add(p);
  }

  private void generatePrimesToUp(int n) {
    if(primes.size() == 0) {
      // We add 2 here so we never have to worry about it again
      addToPrimes(BigInteger.valueOf(2));
      addToPrimes(BigInteger.valueOf(3));
    }
    
    for(BigInteger next = primes.get(primes.size()-1).add(BigInteger.valueOf(2))
        ; primes.size() <= n; next = next.add(BigInteger.valueOf(2)) ) {
      // Test 'next' for primality; invariant: next is odd
      boolean ok = true;
      // Try all known primes up to the square root of 'next'  (skip 2, we know that next is odd)
      for(int i=1; primes.get(i).multiply(primes.get(i)).compareTo(next) <= 0; ++i) {
        if (next.mod(primes.get(i)).equals(BigInteger.ZERO)) {
          ok=false;
          break;
        }
      }
      if(ok) {
        addToPrimes(next);
      }
    }
  }

  @Override
  public BigInteger getRepresentationOfIndex(int index) {
    return getNthPrime(index);
  }

  @Override
  public Integer getIndexFromRepresentation(BigInteger repr) {
    return getIndexOfPrime(repr);
  }
}
