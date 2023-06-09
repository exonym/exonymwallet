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
package com.ibm.zurich.idmx.buildingBlock.revocation.cl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A helper class storing a list of accumulator events for XML serialization.
 */
public class AccumulatorHistory implements Iterable<AccumulatorEvent> {
  private final List<AccumulatorEvent> events;
  
  public AccumulatorHistory() {
    events = new ArrayList<AccumulatorEvent>();
  }

  @Override
  public Iterator<AccumulatorEvent> iterator() {
    return events.iterator();
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((events == null) ? 0 : events.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    final AccumulatorHistory other = (AccumulatorHistory) obj;
    if (events == null) {
      if (other.events != null) return false;
    } else if (!events.equals(other.events)) return false;
    return true;
  }

  /**
   * Add an event to this history.
   * The event epochs must be sequential (it is OK to start with an epoch larger than 0).
   * @param event
   */
  public void addEvent(final AccumulatorEvent event) {
    if (events.isEmpty()) {
      events.add(event);
    } else if (events.get(events.size()-1).getNewEpoch() + 1 == event.getNewEpoch()) {
      events.add(event);
    } else {
      throw new UnsupportedOperationException("Cannot add this event to history: non sequential epochs");
    }
  }
  
}
