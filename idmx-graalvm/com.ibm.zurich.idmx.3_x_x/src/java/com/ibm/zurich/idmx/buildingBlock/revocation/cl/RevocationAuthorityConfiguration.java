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

import java.util.Calendar;
import java.util.GregorianCalendar;

import com.ibm.zurich.idmx.configuration.ErrorMessages;
import com.ibm.zurich.idmx.exception.ConfigurationException;

/**
 * 
 */
public class RevocationAuthorityConfiguration {

  /**
   * Returns the granularity of the creation date that is used in the non revocation evidence and
   * the revocation information.
   */
  public static int getCreationDateGranularity() {
    return Calendar.DAY_OF_MONTH;
  }

  /**
   * Returns the time that a revocation handle value (thus a credential) will be valid.
   */
  public static int getRevocationHandleTimeToLive() {
    return Calendar.MONTH;
  }

  /**
   * Returns the time that revocation information will be valid.
   */
  public static int getRevocationInformationTimeToLive() {
    return Calendar.DAY_OF_MONTH;
  }

  /**
   * Creates a calendar using the given granularity (e.g., Calendar.MONTH, Calendar.DAY_OF_MONTH)
   * with the current date.
   * 
   * @throws ConfigurationException
   */
  public static Calendar now(final int granularity) throws ConfigurationException {
    final Calendar now = new GregorianCalendar();
    switch (granularity) {
      case Calendar.DAY_OF_MONTH:
        return new GregorianCalendar(now.get(Calendar.YEAR), now.get(Calendar.MONTH),
            now.get(Calendar.DAY_OF_MONTH));
      case Calendar.MONTH:
        return new GregorianCalendar(now.get(Calendar.YEAR), now.get(Calendar.MONTH), 1);
      case Calendar.YEAR:
        return new GregorianCalendar(now.get(Calendar.YEAR), Calendar.JANUARY, 1);
      default:
        throw new ConfigurationException(
            ErrorMessages
                .wrongUsage("granularity of the creation date of a non revocation evidence is unknown."));
    }
  }

  /**
   * @return Initial epoch of the revocation authority - this epoch is set when the revocation is
   *         initialised but no events have yet occured.
   */
  public static int initialEpoch() {
    return 0;
  }

}
