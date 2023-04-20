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

package com.ibm.zurich.idmx.interfaces.util;

import java.util.List;

import com.ibm.zurich.idmx.exception.TopologicalSortFailedException;

/**
 * Sorts interdependent modules such that mutual dependencies are resolved.
 */
public interface ModuleSorter<M, D> {

  /**
   * Registers a module's demand for a given dependency.
   * 
   * @param module The module that demands a dependency.
   * @param dependency The dependency that the module demands.
   * @throws NullPointerException if any of the arguments is null.
   */
  void registerDemand(M module, D dependency);

  /**
   * Registers a module's (unlimited) supply of a given dependency.
   * 
   * @param module The module that supplies a dependency.
   * @param dependency The dependency that the module supplies.
   * @throws NullPointerException if any of the arguments is null.
   */
  void registerSupply(M module, D dependency);

  /**
   * Registers a module. Has no effect if the module is already registered. Two modules are
   * considered equal if they are equal according to the {@link Object#equals(Object) equals}
   * method.<br>
   * <br>
   * When registering a module's {@link #registerSupply(Object, Object) supply} or
   * {@link #registerDemand(Object, Object) demand}, the corresponding module is automatically
   * registered. Therefore, explicitly calling this method is only needed in scenarios where a
   * module has neither supply nor demand, but still this module shall be included in the ordering
   * returned by {@link #sortModules()}.
   * 
   * @param module The module that shall be registered.
   */
  void registerModule(M module);

  /**
   * Returns a module ordering such that it holds for all demanded dependencies of all modules that
   * there is module appearing earlier in the ordering that supplies this dependency, unless the
   * dependency is supplied by a module itself. Throws an exception if such an ordering does not
   * exist.<br>
   * <br>
   * More formally, this method returns an ordering of all registered modules such that the
   * following holds for all modules <i>m</i> and all dependencies <i>d</i>: if <i>m</i> demands
   * <i>d</i> and <i>m</i> does not supply <i>d</i> itself, then there appears a module <i>n</i>
   * before <i>m</i> in the ordering that supplies <i>d</i>.<br>
   * <br>
   * The method returns the first possible {@link Comparable natural ordering}.<br>
   * <br>
   * Matching supply and demand of a dependency is determined with the {@link Object#equals(Object)
   * equals} method.
   * 
   * @throws TopologicalSortFailedException if the modules cannot be ordered such that all demand is
   *         supplied.
   */
  List<M> sortModules() throws TopologicalSortFailedException;
}
