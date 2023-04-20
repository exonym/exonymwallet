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

import java.util.logging.Logger;
import com.ibm.zurich.idmx.interfaces.util.Timing;

/**
 * 
 */
public class TimingImpl implements Timing {

  private final Logger logger;
  private long startingTime;

  private static final String LINE_SEPARATOR = System.getProperty("line.separator");

  public TimingImpl() {
    this.logger = Logger.getGlobal();
  }

  @Override
  public void startTiming() {
    startingTime = System.nanoTime();
  }

  @Override
  public long endTiming() {
    return endTiming("");
  }

  @Override
  public long endTiming(final String message) {
    final long executionTime = System.nanoTime() - startingTime;
    final double executionTimeInSeconds = executionTime / 1e9;
    final double executionTimeInMilli = executionTime / 1e6;
    final String executionTimeSec = String.format("%.3f", executionTimeInSeconds);
    final String executionTimeMilli = String.format("%.3f", executionTimeInMilli);

    final String toOutput;
    
    if (message == null) {
    	toOutput = "";
    } else if (!message.isEmpty()) {
    	toOutput = "Idmix: " + message;
    } else {
    	toOutput = message;
    }
    logger.info(toOutput + LINE_SEPARATOR + "Execution time: " + executionTimeMilli
        + " milliseconds (" + executionTimeSec + " seconds).");

    return executionTime;
  }
}
