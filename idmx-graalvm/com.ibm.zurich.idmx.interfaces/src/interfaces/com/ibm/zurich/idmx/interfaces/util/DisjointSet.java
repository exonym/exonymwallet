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

/**
 * A disjoint-set data structure. It keeps track of a set of elements that is partitioned into a
 * number of disjoint subsets.
 */
public interface DisjointSet<T> {

  /**
   * Returns the representative of the subset that contains the given element.<br>
   * <br>
   * If the given element is not contained in a subset yet, a new set containing (only) the given
   * element is added to the list of disjoint subsets and the representative of this newly created
   * set is returned.<br>
   * <br>
   * For two elements that are contained in the same subset, the returned set representatives are
   * equal according to the == operator.
   * 
   * @param element The element whose set representative shall be returned.
   * @return the set representative of the given element. A set representative is not necessarily a
   *         member element of the respective set.
   * @throws NullPointerException if the given element is null.
   */
  public Object find(T element);

  /**
   * Merges two subsets into a single one. The two subsets to merge are determined by using the
   * {@link #find(Object) find} operation on the two given elements.<br>
   * <br>
   * If the two elements already belong to the same subset, then a merge has no effect.
   * 
   * @param element1 A member of the first subset to merge.
   * @param element2 A member of the second subset to merge.
   * @throws NullPointerException if at least one of the given elements is null.
   */
  public void merge(T element1, T element2);
}
