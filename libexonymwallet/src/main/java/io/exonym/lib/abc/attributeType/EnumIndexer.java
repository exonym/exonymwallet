/*
 * Copyright (c) 2023. All Rights Reserved. Exonym GmbH
 */

package io.exonym.lib.abc.attributeType;

import java.math.BigInteger;

public interface EnumIndexer {
  public BigInteger getRepresentationOfIndex(int index);
  public Integer getIndexFromRepresentation(BigInteger repr);
}
