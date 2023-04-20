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
package com.ibm.zurich.idmx.orchestration.presentation;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class PredicateDecoder {
  static enum PredicateType {
    EQUALITY,
    INEQUALITY,
    GREATER_THAN,
    LESS_THAN,
    LESS_EQUAL,
    GREATER_EQUAL,
    ONEOF
  }
  
  private final static Map<String, PredicateType> knownPredicates;
  static {
    knownPredicates = new HashMap<String, PredicateDecoder.PredicateType>();
    knownPredicates.put("urn:oasis:names:tc:xacml:1.0:function:string-equal", PredicateType.EQUALITY);
    knownPredicates.put("urn:oasis:names:tc:xacml:1.0:function:boolean-equal", PredicateType.EQUALITY);
    knownPredicates.put("urn:oasis:names:tc:xacml:1.0:function:integer-equal", PredicateType.EQUALITY);
    knownPredicates.put("urn:oasis:names:tc:xacml:1.0:function:date-equal", PredicateType.EQUALITY);
    knownPredicates.put("urn:oasis:names:tc:xacml:1.0:function:time-equal", PredicateType.EQUALITY);
    knownPredicates.put("urn:oasis:names:tc:xacml:1.0:function:dateTime-equal", PredicateType.EQUALITY);
    knownPredicates.put("urn:oasis:names:tc:xacml:1.0:function:anyURI-equal", PredicateType.EQUALITY);
    
    knownPredicates.put("urn:oasis:names:tc:xacml:1.0:function:integer-greater-than", PredicateType.GREATER_THAN);
    knownPredicates.put("urn:oasis:names:tc:xacml:1.0:function:integer-greater-than-or-equal", PredicateType.GREATER_EQUAL);
    knownPredicates.put("urn:oasis:names:tc:xacml:1.0:function:integer-less-than", PredicateType.LESS_THAN);
    knownPredicates.put("urn:oasis:names:tc:xacml:1.0:function:integer-less-than-or-equal", PredicateType.LESS_EQUAL);
    knownPredicates.put("urn:oasis:names:tc:xacml:1.0:function:date-greater-than", PredicateType.GREATER_THAN);
    knownPredicates.put("urn:oasis:names:tc:xacml:1.0:function:date-greater-than-or-equal", PredicateType.GREATER_EQUAL);
    knownPredicates.put("urn:oasis:names:tc:xacml:1.0:function:date-less-than", PredicateType.LESS_THAN);
    knownPredicates.put("urn:oasis:names:tc:xacml:1.0:function:date-less-than-or-equal", PredicateType.LESS_EQUAL);
    knownPredicates.put("urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than", PredicateType.GREATER_THAN);
    knownPredicates.put("urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than-or-equal", PredicateType.GREATER_EQUAL);
    knownPredicates.put("urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than", PredicateType.LESS_THAN);
    knownPredicates.put("urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than-or-equal", PredicateType.LESS_EQUAL);
    
    knownPredicates.put("urn:abc4trust:1.0:function:string-not-equal", PredicateType.INEQUALITY);
    knownPredicates.put("urn:abc4trust:1.0:function:boolean-not-equal", PredicateType.INEQUALITY);
    knownPredicates.put("urn:abc4trust:1.0:function:integer-not-equal", PredicateType.INEQUALITY);
    knownPredicates.put("urn:abc4trust:1.0:function:date-not-equal", PredicateType.INEQUALITY);
    knownPredicates.put("urn:abc4trust:1.0:function:time-not-equal", PredicateType.INEQUALITY);
    knownPredicates.put("urn:abc4trust:1.0:function:dateTime-not-equal", PredicateType.INEQUALITY);
    knownPredicates.put("urn:abc4trust:1.0:function:anyURI-not-equal", PredicateType.INEQUALITY);
    
    knownPredicates.put("urn:abc4trust:1.0:function:string-equal-oneof", PredicateType.ONEOF);
    knownPredicates.put("urn:abc4trust:1.0:function:boolean-equal-oneof", PredicateType.ONEOF);
    knownPredicates.put("urn:abc4trust:1.0:function:integer-equal-oneof", PredicateType.ONEOF);
    knownPredicates.put("urn:abc4trust:1.0:function:date-equal-oneof", PredicateType.ONEOF);
    knownPredicates.put("urn:abc4trust:1.0:function:time-equal-oneof", PredicateType.ONEOF);
    knownPredicates.put("urn:abc4trust:1.0:function:dateTime-equal-oneof", PredicateType.ONEOF);
    knownPredicates.put("urn:abc4trust:1.0:function:anyURI-equal-oneof", PredicateType.ONEOF);
  }
  
  static PredicateType getPredicateType(final URI function) {
    return knownPredicates.get(function.toString());
  }
}
