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

package com.ibm.zurich.idmx.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.ibm.zurich.idmx.exception.TopologicalSortFailedException;
import com.ibm.zurich.idmx.interfaces.util.ModuleSorter;

/**
 * Implements a module sorter by means of an adapted topological sort algorithm.
 */
public class ModuleSorterImpl<M, D> implements ModuleSorter<M, D> {

  private final Set<M> modules = new HashSet<M>();
  private final Set<M> independentModules = new HashSet<M>(); // set of all modules without dependency
  private final Map<M, Set<D>> demand = new HashMap<M, Set<D>>();
  private final Map<M, Set<D>> supply = new HashMap<M, Set<D>>();

  @Override
  public void registerDemand(final M module, final D dependency) {
    if (module == null || dependency == null) {
      throw new NullPointerException("Arguments must not be null");
    }

    registerModule(module);

    if (!supply.get(module).contains(dependency)) { // self-supplied demand is ignored
      demand.get(module).add(dependency);
      independentModules.remove(module);
    }
  }

  @Override
  public void registerSupply(final M module, final D dependency) {
    if (module == null || dependency == null) {
      throw new NullPointerException("Arguments must not be null");
    }

    registerModule(module);
    supply.get(module).add(dependency);

    // remove self-demand for that dependency (if present) and update independence status
    demand.get(module).remove(dependency);
    if (demand.get(module).isEmpty()) {
      independentModules.add(module);
    }
  }

  @Override
  public void registerModule(final M module) {
    if (!modules.contains(module)) {
      modules.add(module);
      independentModules.add(module);

      demand.put(module, new HashSet<D>());
      supply.put(module, new HashSet<D>());
    }
  }

  @Override
  public List<M> sortModules() throws TopologicalSortFailedException {
    final List<M> naturalOrdering = new ArrayList<M>();

    final Set<M> iMs = new TreeSet<M>(independentModules); // use of TreeSet results in natural ordering
    final Map<M, Set<D>> remainingDemand = new HashMap<M, Set<D>>(demand);

    while (!iMs.isEmpty()) {
      // remove module from set of independent modules
      final M independentModule = iMs.iterator().next();
      iMs.remove(independentModule);

      // insert module into ordering
      naturalOrdering.add(independentModule);

      // remove all demand that is met by supply so far
      for (final D suppliedDependency : supply.get(independentModule)) {
        for (final M consumer : getConsumers(suppliedDependency, remainingDemand)) {
          remainingDemand.get(consumer).remove(suppliedDependency);

          // if current consumer has no more demand, it becomes an independent module
          if (remainingDemand.get(consumer).isEmpty()) {
            iMs.add(consumer);
          }
        }
      }
    }

    // fail if there is still remaining demand
    boolean fail = false;
    final StringBuilder errorMessage = new StringBuilder();
    for (final M module : modules) {
      if (!remainingDemand.get(module).isEmpty()) {
        fail = true;
        errorMessage.append("Remaining demand for module " + module + ":\n");
        for(final D dependency : remainingDemand.get(module)) {
          errorMessage.append("- " + dependency + "\n");
        }
      }
    }
    if(fail) {
      throw new TopologicalSortFailedException(errorMessage.toString());
    }

    return naturalOrdering;
  }

  final List<M> getConsumers(final D dependency, final Map<M, Set<D>> demand) {
	  final List<M> consumers = new ArrayList<M>();
	
	  for (final M module : modules) {
	    if (demand.get(module).contains(dependency)) {
	      consumers.add(module);
	    }
	  }
	  return consumers;
  }
}
