/*
 * Copyright (c) 2023. All Rights Reserved. Exonym GmbH
 */

package io.exonym.lib.abc.util;

import eu.abc4trust.util.ContextGenerator;

import java.math.BigInteger;
import java.net.URI;

public class ContextGeneratorSequential implements ContextGenerator{
  private int counter;
  
  public ContextGeneratorSequential() {
    counter = 0;
    System.out.println("*** Using sequential ContextGenerator *** DO NOT USE IN PRODUCTION");
  }
  
  @Override
  public URI getUniqueContext(URI prefix) {
    counter++;
    return URI.create(prefix + "/" + counter);
  }

  @Override
  public BigInteger getRandomNumber(long bits) {
    counter++;
    // return 2^bits - counter
    return BigInteger.valueOf(2).pow((int) bits).add(BigInteger.valueOf(-counter));
  }
}
