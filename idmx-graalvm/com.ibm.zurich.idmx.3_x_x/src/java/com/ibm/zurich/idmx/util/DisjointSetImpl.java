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
import java.util.Map;

import com.ibm.zurich.idmx.interfaces.util.DisjointSet;

public class DisjointSetImpl<T> implements DisjointSet<T> {
  
  private final Map<T, Integer> elementIndexes;
  private final ArrayList<Integer> tree;
  private final ArrayList<Integer> ranks;
  
  public DisjointSetImpl() {
    this.elementIndexes = new HashMap<T, Integer>();
    this.tree = new ArrayList<Integer>();
    this.ranks = new ArrayList<Integer>();
  }
  
  private Integer find_internal(final Integer x) {
    if(tree.get(x) != x) {
      tree.set(x, find_internal(tree.get(x)));
    }
    return tree.get(x);
  }
  
  @Override
  public Integer find(final T element) {
    if(element == null) {
      throw new NullPointerException();
    }
    
    if(!elementIndexes.containsKey(element)) {
      final int value = tree.size();
      elementIndexes.put(element, value);
      tree.add(value);
      ranks.add(0);
    }
    final int x = elementIndexes.get(element);
    return find_internal(x);
  }
  
  @Override
  public void merge(final T element1, final T element2) {
	final int xRoot = find(element1);
    final int yRoot = find(element2);
    
    if(xRoot != yRoot) {
      final int rankX = ranks.get(xRoot);
      final int rankY = ranks.get(yRoot);
      if(rankX > rankY) {
        tree.set(yRoot, xRoot);
      } else if(rankX < rankY) {
        tree.set(xRoot, yRoot);
      } else {
        tree.set(yRoot, xRoot);
        ranks.set(xRoot, rankX+1);
      }
    }
  }
  
}
