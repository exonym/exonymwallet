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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.ibm.zurich.idmx.interfaces.util.DisjointSet;

public class DisjointSetTest {

  @Test
  public void testFindNotNull() {
    DisjointSet<String> d = new DisjointSetImpl<String>();
    String a = "a";

    assertNotNull(d.find(a));
  }

  @Test
  public void testFindTwiceOnSameElement() {
    DisjointSet<String> d = new DisjointSetImpl<String>();
    String a = "a";

    assertTrue(d.find(a) == d.find(a));
  }

  @Test
  public void testFindOnDifferentElements() {
    DisjointSet<String> d = new DisjointSetImpl<String>();
    String a = "a";
    String b = "b";

    assertTrue(d.find(a) != d.find(b));
  }

  @Test
  public void testMerge() {
    DisjointSet<String> d = new DisjointSetImpl<String>();
    String a = "a";
    String b = "b";

    d.merge(a, b);
    assertTrue(d.find(a) == d.find(b));

    assertNotNull(d.find(a));
    assertNotNull(d.find(b));
  }
  
  @Test
  public void testMerge2() {
    DisjointSet<String> d = new DisjointSetImpl<String>();
    d.merge("a", "b");
    d.merge("c", "a");

    assertTrue(d.find("a") == d.find("b"));
    assertTrue(d.find("a") == d.find("c"));
  }

  @Test
  public void testMergeWithItself() {
    DisjointSet<String> d = new DisjointSetImpl<String>();
    String a = "a";
    String b = "b";

    d.merge(a, a);
    assertTrue(d.find(a) == d.find(a));
    assertTrue(d.find(a) != d.find(b));
  }

  @Test
  public void testMergeOfThreeElements() {
    DisjointSet<String> d = new DisjointSetImpl<String>();
    String a = "a";
    String b = "b";
    String c = "c";

    d.merge(a, b);
    d.merge(b, c);

    assertTrue(d.find(a) == d.find(c));
    assertTrue(d.find(a) == d.find(b));
    assertTrue(d.find(b) == d.find(c));
  }

  @Test
  public void testMergeOfFourElements() {
    DisjointSet<String> x = new DisjointSetImpl<String>();
    String a = "a";
    String b = "b";
    String c = "c";
    String d = "d";

    x.merge(a, b);
    x.merge(c, d);
    x.merge(a, c);

    assertTrue(x.find(b) == x.find(d));

    assertTrue(x.find(a) == x.find(b));
    assertTrue(x.find(c) == x.find(d));
    assertTrue(x.find(a) == x.find(c));
  }

  @Test
  public void testMergeDoubleOperation() {
    DisjointSet<String> d = new DisjointSetImpl<String>();
    String a = "a";
    String b = "b";

    d.merge(a, b);
    d.merge(a, b);

    assertTrue(d.find(a) == d.find(b));
  }

  @Test
  public void testMergeDoubleOperation2() {
    DisjointSet<String> x = new DisjointSetImpl<String>();
    String a = "a";
    String b = "b";
    String c = "c";
    String d = "d";

    x.merge(a, b);
    x.merge(c, d);
    x.merge(a, c);
    x.merge(b, d);
    x.merge(a, b);

    assertTrue(x.find(a) == x.find(b));
    assertTrue(x.find(c) == x.find(d));
    assertTrue(x.find(a) == x.find(c));
    assertTrue(x.find(b) == x.find(d));
  }

  @Test
  public void testMergeDoubleOperationOnDisjointSets() {
    DisjointSet<String> x = new DisjointSetImpl<String>();
    String a = "a";
    String b = "b";
    String c = "c";
    String d = "d";

    x.merge(a, b);
    x.merge(c, d);
    x.merge(a, b);
    x.merge(c, d);

    assertTrue(x.find(a) == x.find(b));
    assertTrue(x.find(c) == x.find(d));
    assertTrue(x.find(a) != x.find(c));
    assertTrue(x.find(b) != x.find(d));
  }

  @Test(expected = NullPointerException.class)
  public void testFindNullArgument() {
    DisjointSet<String> x = new DisjointSetImpl<String>();

    x.find(null);
  }

  @Test(expected = NullPointerException.class)
  public void testMergeOneNullArgument1() {
    DisjointSet<String> x = new DisjointSetImpl<String>();

    x.merge(null, "a");
  }

  @Test(expected = NullPointerException.class)
  public void testMergeOneNullArgument2() {
    DisjointSet<String> x = new DisjointSetImpl<String>();

    x.merge("a", null);
  }

  @Test(expected = NullPointerException.class)
  public void testMergeOneNullArguments() {
    DisjointSet<String> x = new DisjointSetImpl<String>();

    x.merge(null, null);
  }
}
