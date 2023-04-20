/*
 * Copyright (c) 2023. All Rights Reserved. Exonym GmbH
 */

package io.exonym.lib.abc.attributeType;

import eu.abc4trust.xml.AttributeDescription;

import java.util.*;

public class EnumAllowedValues {
  private final List<String> allowedValues;
  private final Map<String, Integer> positions;
  
  public EnumAllowedValues() {
    allowedValues = new ArrayList<String>();
    positions = new HashMap<String, Integer>();
  }
  
  public EnumAllowedValues(AttributeDescription desc) {
    this();
    if(desc.getAllowedValue() != null) {
      for(Object o: desc.getAllowedValue()) {
        addAllowedValue(o.toString());
      }
    }
  }
  
  public EnumAllowedValues(List<String> allowedValues) {
    this();
    for(String s: allowedValues) {
      addAllowedValue(s);
    }
  }
  
  public void addAllowedValue(String value) {
    if(positions.containsKey(value)) {
      throw new RuntimeException("Duplicate enum value");
    }
    positions.put(value, allowedValues.size());
    allowedValues.add(value);
  }
  
  public boolean isAllowedValue(String value) {
    return positions.containsKey(value);
  }
  
  public int getPosition(String value) {
    return positions.get(value);
  }
  
  public List<String> getAllowedValues() {
    return Collections.unmodifiableList(allowedValues);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((allowedValues == null) ? 0 : allowedValues.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    EnumAllowedValues other = (EnumAllowedValues) obj;
    if (allowedValues == null) {
      if (other.allowedValues != null) return false;
    } else if (!allowedValues.equals(other.allowedValues)) return false;
    return true;
  }
}
