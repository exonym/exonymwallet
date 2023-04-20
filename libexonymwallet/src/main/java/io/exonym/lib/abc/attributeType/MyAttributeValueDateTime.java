/*
 * Copyright (c) 2023. All Rights Reserved. Exonym GmbH
 */

package io.exonym.lib.abc.attributeType;

import org.w3c.dom.Element;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

public class MyAttributeValueDateTime extends MyAttributeValue {

  private XMLGregorianCalendar value;
  
  public MyAttributeValueDateTime(Object attributeValue, /*IsNull*/ EnumAllowedValues allowedValues) {
    super(allowedValues);
    try {
      if (attributeValue instanceof XMLGregorianCalendar) {
        value = ((XMLGregorianCalendar) attributeValue);
      } else if (attributeValue instanceof Element) {
        String svalue = ((Element) attributeValue).getTextContent();
        value = DatatypeFactory.newInstance().newXMLGregorianCalendar(svalue);
      } else if (attributeValue instanceof String) {
        String svalue = (String) attributeValue;
        value = DatatypeFactory.newInstance().newXMLGregorianCalendar(svalue);
      } else {
        throw new RuntimeException(
            "Cannot parse attribute value as dateTime (XMLGregorianCalendar)");
      }
      if (value.getTimezone() == DatatypeConstants.FIELD_UNDEFINED) {
        // DateTimes without timezone are interpreted as UTC
        value.setTimezone(0);
      }
    } catch (DatatypeConfigurationException e) {
      throw new RuntimeException("Cannot parse attribute value as dateTime (XMLGregorianCalendar)");
    }
  }

  @Override
  public boolean isEquals(MyAttributeValue lhs) {
    if(lhs instanceof MyAttributeValueDateTime) {
      XMLGregorianCalendar lhsDateTime = ((MyAttributeValueDateTime)lhs).value;
      return (value.compare(lhsDateTime) == DatatypeConstants.EQUAL);
    } else {
      return false;
    }
  }
  
  @Override
  public boolean isLess(MyAttributeValue lhs) {
    if(lhs instanceof MyAttributeValueDateTime) {
      XMLGregorianCalendar lhsTime = ((MyAttributeValueDateTime)lhs).value;
      return (value.compare(lhsTime) == DatatypeConstants.LESSER);
    } else {
      return false;
    }
  }
  
  protected XMLGregorianCalendar getValue() {
    return value;
  }
  
  @Override
  public Object getValueAsObject() {
    return getValue();
  }

}
