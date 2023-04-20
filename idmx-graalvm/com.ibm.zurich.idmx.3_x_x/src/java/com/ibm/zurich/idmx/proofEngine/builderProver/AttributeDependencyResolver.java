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
package com.ibm.zurich.idmx.proofEngine.builderProver;

import java.util.ArrayList;
import java.util.List;

import com.ibm.zurich.idmx.exception.TopologicalSortFailedException;
import com.ibm.zurich.idmx.interfaces.util.ModuleSorter;

class AttributeDependencyResolver {
   
  private final ModuleSorter<String, String> ms;
  private final List<String> modules;
  private final List<StringPair> demand;
  private final List<StringPair> supply;
  private boolean done;
  
  AttributeDependencyResolver(final ModuleSorter<String, String> ms) {
    this.ms = ms;
    this.modules = new ArrayList<String>();
    this.demand = new ArrayList<StringPair>();
    this.supply = new ArrayList<StringPair>();
    this.done = false;
  }
  
  /*
   * You do not have to call registerModule for a given module name
   * if you call registerDemand or registerSupply.
   */
  public void registerModule(final String moduleName) {
    modules.add(moduleName);
  }
  
  public void registerDemand(final String moduleName, final String attributeName) {
    demand.add(new StringPair(moduleName, attributeName));
  }
  
  public void registerSupply(final String moduleName, final String attributeName) {
    supply.add(new StringPair(moduleName, attributeName));
  }
  
  public List<String> sortModulesOrThrow(final AttributeSet as) throws TopologicalSortFailedException {
    if(done) {
      throw new RuntimeException("Cannot sort modules twice.");
    }
    done = true;
    
    for(final String moduleName: modules) {
      ms.registerModule(moduleName);
    }
    
    for(final StringPair s: supply) {
      String representative = as.getProperty(s.attribute).representativeName;
      ms.registerSupply(s.module, representative);
    }
    
    // Skip demand if we already know the attribute value
    for(final StringPair d: demand) {
    	final AttributeProperty ap = as.getProperty(d.attribute);
      if(ap == null) {
        throw new NullPointerException("Could not get property of attribute " + d.attribute + " in module " + d.module);
      }
      if(ap.value == null) {
    	  final String representative = ap.representativeName;
        ms.registerDemand(d.module, representative);
      }
    }
    
    final List<String> sortedList = ms.sortModules();
    return sortedList;
  }
  
  private class StringPair {
    public final String module;
    public final String attribute;
    
    public StringPair(final String module, final String attribute) {
      this.module = module;
      this.attribute = attribute;
    }
  }
}
