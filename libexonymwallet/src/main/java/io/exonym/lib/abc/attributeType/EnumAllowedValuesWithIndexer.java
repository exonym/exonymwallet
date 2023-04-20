/*
 * Copyright (c) 2023. All Rights Reserved. Exonym GmbH
 */

package io.exonym.lib.abc.attributeType;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class EnumAllowedValuesWithIndexer extends EnumAllowedValues {
  private final EnumIndexer indexer;
  
  public EnumAllowedValuesWithIndexer(EnumIndexer indexer, List<String> allowedValues) {
    super(allowedValues);
    this.indexer = indexer;
  }
  
  public Map<String, BigInteger> getEncodingForEachAllowedValue() {
    Map<String, BigInteger> res = new HashMap<String, BigInteger>();
    List<String> list = getAllowedValues();
    for(int i=0;i<list.size();++i) {
      res.put(list.get(i), indexer.getRepresentationOfIndex(i));
    }
    return res;
  }
}
