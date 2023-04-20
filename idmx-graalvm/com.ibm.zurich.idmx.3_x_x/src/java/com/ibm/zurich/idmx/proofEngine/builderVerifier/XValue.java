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
package com.ibm.zurich.idmx.proofEngine.builderVerifier;

import java.util.Arrays;

import com.ibm.zurich.idmx.exception.ProofException;
import com.ibm.zurich.idmx.interfaces.util.BigInt;
import com.ibm.zurich.idmx.interfaces.util.BigIntFactory;
import com.ibm.zurich.idmx.interfaces.util.group.Group;
import com.ibm.zurich.idmx.interfaces.util.group.GroupElement;

import eu.abc4trust.xml.ValueInZkProof;
import eu.abc4trust.xml.ValueWithHashInZkProof;

class XValue implements Comparable<XValue> {

  private final String name;
  private final String type;
  private final byte[] valueAsByte;
  private final byte[] hashContribution;

  public static final String BIG_INT_TYPE = "integer";
  public static final String GROUP_ELEMENT_TYPE = "groupElement";
  public static final String OCTET_STREAM_TYPE = "octetStream";

  public XValue(final String name, final BigInt value) {
    this.name = name;
    this.type = BIG_INT_TYPE;
    this.valueAsByte = value.toByteArray();
    this.hashContribution = value.toByteArray();
  };

  public XValue(final String name, final GroupElement<?, ?, ?> value) {
    this.name = name;
    this.type = GROUP_ELEMENT_TYPE;
    this.valueAsByte = value.serialize();
    this.hashContribution = value.serialize();
  };

  public XValue(final String name, final byte[] value) {
    this.name = name;
    this.type = OCTET_STREAM_TYPE;
    this.valueAsByte = value;
    this.hashContribution = value;
  };

  private XValue(final String name, final String type, final byte[] value,
		  final byte[] hashContribution) {
    this.name = name;
    this.type = type;
    this.valueAsByte = value;
    this.hashContribution = hashContribution;
  };

  public static XValue parse(final ValueInZkProof value) throws ProofException {
    return parse(value.getName(), value.getType(), value.getValue(), null);
  }

  public static XValue parse(final ValueWithHashInZkProof value) throws ProofException {
    return parse(value.getName(), value.getType(), value.getValue(), value.getHashContribution());
  }

  public static XValue parse(final String name, final String type, final byte[] value,
		  byte[] hashContribution)
      throws ProofException {
    if (hashContribution == null) {
      hashContribution = value;
    }
    boolean needIdenticalHashContribution = false;

    if (type.equals(BIG_INT_TYPE)) {
      needIdenticalHashContribution = true;
    } else if (type.equals(GROUP_ELEMENT_TYPE)) {
      needIdenticalHashContribution = true;
    } else if (type.equals(OCTET_STREAM_TYPE)) {
      // No check
    } else {
      throw new ProofException("Cannot parse value: " + name + ", unknown type: " + type);
    }

    if (needIdenticalHashContribution) {
      if (!Arrays.equals(value, hashContribution)) {
        throw new ProofException("Type " + type + " requires identical value and hash contribution");
      }
    }
    return new XValue(name, type, value, hashContribution);
  }

  public BigInt getValueAsInt(final BigIntFactory factory) throws ProofException {
    if (!type.equals(BIG_INT_TYPE)) {
      throw new ProofException("Value " + name + " is not an integer but a " + type + ".");
    }
    return factory.signedValueOf(valueAsByte);
  }

  public <GE extends GroupElement<?, GE, ?>> GE getValueAsGroupElement(final Group<?, GE, ?> group)
      throws ProofException {
    if (!type.equals(GROUP_ELEMENT_TYPE)) {
      throw new ProofException("Value " + name + " is not a group element but a " + type + ".");
    }
    return group.valueOf(valueAsByte);
  }

  public byte[] getValueAsByteArray() throws ProofException {
    if (!type.equals(OCTET_STREAM_TYPE)) {
      throw new ProofException("Value " + name + " is not a byte array but a " + type + ".");
    }
    return valueAsByte;
  }

  public byte[] getHashContribution() {
    return hashContribution;
  }

  public boolean isSame(final BigInt lhs) throws ProofException {
    if (!type.equals(BIG_INT_TYPE)) {
      return false;
    }
    return getValueAsInt(lhs.getFactory()).equals(lhs);
  }

  public boolean isSame(final GroupElement<?, ?, ?> lhs) throws ProofException {
    if (!type.equals(GROUP_ELEMENT_TYPE)) {
      return false;
    }
    return getValueAsGroupElement(lhs.getGroup()).equals(lhs);
  }

  public boolean isSame(final byte[] lhs) {
    if (!type.equals(OCTET_STREAM_TYPE)) {
      return false;
    }
    return Arrays.equals(valueAsByte, lhs) && Arrays.equals(hashContribution, lhs);
  }

  public boolean isSame(final byte[] lhs, final byte[] lhsHash) {
    if (!type.equals(OCTET_STREAM_TYPE)) {
      return false;
    }
    return Arrays.equals(valueAsByte, lhs) && Arrays.equals(hashContribution, lhsHash);
  }

  @Override
  public int compareTo(final XValue o) {
    return name.compareTo(o.name);
  }

  public ValueWithHashInZkProof serializeWithHash() {
	final ValueWithHashInZkProof ret = new ValueWithHashInZkProof();
    ret.setName(name);
    ret.setValue(valueAsByte);
    if (!Arrays.equals(valueAsByte, hashContribution)) {
      ret.setHashContribution(hashContribution);
    }
    ret.setType(type);
    return ret;
  }

  public boolean hasCustomHashContribution() {
    return !Arrays.equals(valueAsByte, hashContribution);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(hashContribution);
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((type == null) ? 0 : type.hashCode());
    result = prime * result + Arrays.hashCode(valueAsByte);
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    XValue other = (XValue) obj;
    if (!Arrays.equals(hashContribution, other.hashContribution)) return false;
    if (name == null) {
      if (other.name != null) return false;
    } else if (!name.equals(other.name)) return false;
    if (type == null) {
      if (other.type != null) return false;
    } else if (!type.equals(other.type)) return false;
    if (!Arrays.equals(valueAsByte, other.valueAsByte)) return false;
    return true;
  }

  public String getName() {
    return name;
  }
}
