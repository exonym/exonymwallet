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

package com.ibm.zurich.idmix.abc4trust.util;

import java.net.URI;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import com.ibm.zurich.idmx.configuration.ErrorMessages;

/**
 * 
 */
public class EncodeDecode {

  private static final String CONCATENATOR = "::";

  public EncodeDecode() {
    throw new AssertionError(ErrorMessages.nonInstantiationErrorMessage());
  }

  public static URI removeRandomSuffix(final URI idmxUID) {
    return URI.create(idmxUID.toString().substring(0, idmxUID.toString().lastIndexOf(":")));
  }

  public static String encodeDateAsString(final XMLGregorianCalendar lastChangeDate) {
    final String eventDateString = lastChangeDate.getYear() + CONCATENATOR + //
        lastChangeDate.getMonth() + CONCATENATOR + //
        lastChangeDate.getDay() + CONCATENATOR + //
        lastChangeDate.getHour() + CONCATENATOR + //
        lastChangeDate.getMinute();
    return eventDateString;
  }

  public static XMLGregorianCalendar decodeDateFromString(final String dateString) {
    final String[] splitDate = dateString.split(CONCATENATOR);
    final XMLGregorianCalendar eventDate;
    try {
      eventDate =
          DatatypeFactory.newInstance().newXMLGregorianCalendar(
              new GregorianCalendar(Integer.valueOf(splitDate[0]), Integer.valueOf(splitDate[1]),
                  Integer.valueOf(splitDate[2]), Integer.valueOf(splitDate[3]), Integer
                      .valueOf(splitDate[4])));
    } catch (final DatatypeConfigurationException e) {
      throw new RuntimeException(e);
    }
    return eventDate;
  }


}
