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
package com.ibm.zurich.idmx.interfaces.zkModule.state;

import java.util.Collections;
import java.util.TreeSet;
import java.util.Set;

import com.ibm.zurich.idmx.interfaces.util.BigInt;

// This class is almost an enum (not quite as residue class other allows multiple values)
// TODO(enr): Not yet fully tested
public class ResidueClass {
  /**
   * Attribute is an integer and is guaranteed to be in the range 0--maxAttributeValue.
   */
  public static final ResidueClass INTEGER_IN_RANGE = new ResidueClass("IntegerInRange", false,
      false, null);
  /**
   * Attribute is an integer, but not necessarily in the correct range
   */
  public static final ResidueClass ANY_INTEGER = new ResidueClass("Integer", true, false, null);
  /**
   * Attribute is a residue class modulo the prime order specified in system parameters
   */
  public static final ResidueClass RESIDUE_CLASS_MOD_Q = new ResidueClass("ResidueClass", true,
      true, null);

  /**
   * Attribute is a residue class of a different prime order.
   */
  public static ResidueClass RESIDUE_CLASS(BigInt order) {
    return new ResidueClass("ResidueClass", true, false, Collections.singleton(order));
  }

  /**
   * It is not (yet) known if the attribute is a residue class or an integer. This is the default
   * value. It is overridden by any other value.
   */
  public static final ResidueClass UNSPECIFIED = new ResidueClass("?", true, false, null);


  // -END OF ENUM-----------------------------------------------------------------------------------
  private final boolean needsRangeCheck;
  private final boolean includesOrderQ;
  private final Set<BigInt> orders = new TreeSet<>();
  private final String type;

  private ResidueClass(final String type, final boolean needsRangeCheck, final boolean includesOrderQ,
                       final Set<BigInt> orders) {
    this.type = type;
    this.needsRangeCheck = needsRangeCheck;
    this.includesOrderQ = includesOrderQ;
    if (orders != null) {
      this.orders.addAll(orders);
    }
  }

  /**
   * Is it necessary to check if attribute is between 0 and maxAttributeSize ?
   * 
   * @return
   */
  public boolean needsRangeCheck() {
    return needsRangeCheck;
  }

  public boolean hasOrderQ() {
    return includesOrderQ;
  }

  public Set<BigInt> getOrdersWithoutQ() {
    return Collections.unmodifiableSet(orders);
  }

  public Set<BigInt> getOrdersWithQ(final BigInt q) {
    final TreeSet<BigInt> ret = new TreeSet<>(getOrdersWithoutQ());
    if (includesOrderQ) {
      ret.add(q);
    }
    return ret;
  }
  
  public boolean hasMultipleOrders() {
    if(orders.size() >= 2) {
      return true;
    }
    if(orders.size() == 1 && includesOrderQ) {
      return true;
    }
    return false;
  }

  public ResidueClass merge(final ResidueClass lhs) {
    // Unspecified is always overridden
    if (this == UNSPECIFIED) {
      return lhs;
    }
    if (lhs == UNSPECIFIED) {
      return this;
    }
    // Integer in range overrides any integer & residue class
    if (this == INTEGER_IN_RANGE || lhs == INTEGER_IN_RANGE) {
      return INTEGER_IN_RANGE;
    }
    // Any integer overrides residue class
    if (this == ANY_INTEGER || lhs == ANY_INTEGER) {
      return ANY_INTEGER;
    }
    // If both are residue classes mod q, use the predefined constant
    if (this == RESIDUE_CLASS_MOD_Q && lhs == RESIDUE_CLASS_MOD_Q) {
      return RESIDUE_CLASS_MOD_Q;
    }
    // Otherwise, merge manually
    final boolean newIncludesOrderQ = this.includesOrderQ || lhs.includesOrderQ;
    final Set<BigInt> newOrders = new TreeSet<>(this.orders);
    newOrders.addAll(lhs.orders);
    return new ResidueClass("ResidueClass", true, newIncludesOrderQ, newOrders);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (includesOrderQ ? 1231 : 1237);
    result = prime * result + ((type == null) ? 0 : type.hashCode());
    result = prime * result + (needsRangeCheck ? 1231 : 1237);
    result = prime * result + ((orders == null) ? 0 : orders.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    final ResidueClass other = (ResidueClass) obj;
    if (includesOrderQ != other.includesOrderQ) return false;
    if (type == null) {
      if (other.type != null) return false;
    } else if (!type.equals(other.type)) return false;
    if (needsRangeCheck != other.needsRangeCheck) return false;
    if (orders == null) {
      if (other.orders != null) return false;
    } else if (!orders.equals(other.orders)) return false;
    return true;
  }

  @Override
  public String toString() {
    if(this == INTEGER_IN_RANGE || this == ANY_INTEGER) {
      return type;
    }
    final StringBuilder sb = new StringBuilder();
    boolean comma = false;
    sb.append(type + " [orders={");
    if (this.includesOrderQ) {
      sb.append("Q");
      comma = true;
    }
    for (final BigInt b : orders) {
      if (comma) {
        sb.append(", ");
      }
      sb.append(b.toHumanReadableString());
      comma = true;
    }
    sb.append("}]");
    return sb.toString();
  }


}
