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

import java.util.HashSet;
import java.util.Set;

import com.ibm.zurich.idmx.interfaces.util.DisjointSet;

/**
 * @deprecated Use {@link DisjointSetImpl} instead because it is more efficient. <br>
 *             This class is functional, however, it is deprecated because the implementation is not
 *             optimized. In particular, this class has complexity O(n) while
 *             {@link DisjointSetImpl} has constant complexity in all practical cases.
 */
@Deprecated
public class DisjointSetImpl_old<T> implements DisjointSet<T> {

  final Set<Set<T>> disjointSets;

  public DisjointSetImpl_old() {
    disjointSets = new HashSet<Set<T>>();
  }

  private Set<T> makeSet(final T element) {
	final Set<T> newSubset = new HashSet<T>();
    newSubset.add(element);

    disjointSets.add(newSubset);
    return newSubset;
  }


  @Override
  public Set<T> find(final T element) {
    if (element == null) {
      throw new NullPointerException("Argument must not be null");
    }

    for (final Set<T> subset : disjointSets) {
      if (subset.contains(element)) {
        return subset;
      }
    }

    // Given element is not contained in a subset yet
    return makeSet(element);
  }


  @Override
  public void merge(final T element1, final T element2) {
    if (element1 == null || element2 == null) {
      throw new NullPointerException("Arguments must not be null");
    }

    final Set<T> subset1 = find(element1);
    final Set<T> subset2 = find(element2);

    if (subset1 == subset2) { // == is the right operator here, even though .equals may also work
      return;
    }
    
    disjointSets.remove(subset1);
    disjointSets.remove(subset2);

    subset1.addAll(subset2);
    disjointSets.add(subset1);
  }
}