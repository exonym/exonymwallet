/*
 * Copyright (c) 2023. All Rights Reserved. Exonym GmbH
 */

package io.exonym.lib.abc.attributeType;

import io.exonym.lib.abc.util.Constants;

import java.net.URI;
import java.util.List;
import java.util.StringTokenizer;



public class MyAttributeValueFactory {


  public static MyAttributeValue parseValue(URI dataType, Object attributeValue, /*Nullable*/ EnumAllowedValues av) {
    StringTokenizer st = new StringTokenizer(dataType.toString(), ":");
    String type = "";
    while(st.hasMoreTokens()) {
      // ignore namespace
      type = st.nextToken();
    }

    if (type.equals(Constants.STRING_TYPE)) {
      return new MyAttributeValueString(attributeValue, av);
    } else if (type.equals(Constants.BOOLEAN_TYPE)) {
      return new MyAttributeValueBoolean(attributeValue, av);
    } else if (type.equals(Constants.INTEGER_TYPE) || type.equals("int") || type.equals("long")) {
      return new MyAttributeValueInteger(attributeValue, av);
    } else if (type.equals(Constants.DATE_TYPE)) {
      return new MyAttributeValueDate(attributeValue, av);
    } else if (type.equals(Constants.TIME_TYPE)) {
      return new MyAttributeValueTime(attributeValue, av);
    } else if (type.equals(Constants.DATETIME_TYPE)) {
      return new MyAttributeValueDateTime(attributeValue, av);
    } else if (type.equals(Constants.URI_TYPE)) {
      return new MyAttributeValueUri(attributeValue, av);
   } else {
      throw new RuntimeException("Cannot parse attribute data type: '" + type + "'");
    }
  }

  public static Constants.OperationType operationTypeOfFunction(URI functionAsUri) {
    String function = functionAsUri.toString();
    if (function.equals("urn:oasis:names:tc:xacml:1.0:function:string-equal")) {
      return Constants.OperationType.EQUAL;
    } else if (function.equals("urn:oasis:names:tc:xacml:1.0:function:boolean-equal")) {
      return Constants.OperationType.EQUAL;
    } else if (function.equals("urn:oasis:names:tc:xacml:1.0:function:integer-equal")) {
      return Constants.OperationType.EQUAL;
    } else if (function.equals("urn:oasis:names:tc:xacml:1.0:function:date-equal")) {
      return Constants.OperationType.EQUAL;
    } else if (function.equals("urn:oasis:names:tc:xacml:1.0:function:time-equal")) {
      return Constants.OperationType.EQUAL;
    } else if (function.equals("urn:oasis:names:tc:xacml:1.0:function:dateTime-equal")) {
      return Constants.OperationType.EQUAL;
    } else if (function.equals("urn:oasis:names:tc:xacml:1.0:function:anyURI-equal")) {
      return Constants.OperationType.EQUAL;
    } else if (function.equals("urn:oasis:names:tc:xacml:1.0:function:integer-greater-than")) {
      return Constants.OperationType.GREATER;
    } else if (function
        .equals("urn:oasis:names:tc:xacml:1.0:function:integer-greater-than-or-equal")) {
      return Constants.OperationType.GREATEREQ;
    } else if (function.equals("urn:oasis:names:tc:xacml:1.0:function:integer-less-than")) {
      return Constants.OperationType.LESS;
    } else if (function.equals("urn:oasis:names:tc:xacml:1.0:function:integer-less-than-or-equal")) {
      return Constants.OperationType.LESSEQ;
    } else if (function.equals("urn:oasis:names:tc:xacml:1.0:function:date-greater-than")) {
      return Constants.OperationType.GREATER;
    } else if (function.equals("urn:oasis:names:tc:xacml:1.0:function:date-greater-than-or-equal")) {
      return Constants.OperationType.GREATEREQ;
    } else if (function.equals("urn:oasis:names:tc:xacml:1.0:function:date-less-than")) {
      return Constants.OperationType.LESS;
    } else if (function.equals("urn:oasis:names:tc:xacml:1.0:function:date-less-than-or-equal")) {
      return Constants.OperationType.LESSEQ;
    } else if (function.equals("urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than")) {
      return Constants.OperationType.GREATER;
    } else if (function.equals("urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than-or-equal")) {
      return Constants.OperationType.GREATEREQ;
    } else if (function.equals("urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than")) {
      return Constants.OperationType.LESS;
    } else if (function.equals("urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than-or-equal")) {
      return Constants.OperationType.LESSEQ;
    } else if (function.equals("urn:abc4trust:1.0:function:string-not-equal")) {
      return Constants.OperationType.NOTEQUAL;
    } else if (function.equals("urn:abc4trust:1.0:function:boolean-not-equal")) {
      return Constants.OperationType.NOTEQUAL;
    } else if (function.equals("urn:abc4trust:1.0:function:integer-not-equal")) {
      return Constants.OperationType.NOTEQUAL;
    } else if (function.equals("urn:abc4trust:1.0:function:date-not-equal")) {
      return Constants.OperationType.NOTEQUAL;
    } else if (function.equals("urn:abc4trust:1.0:function:time-not-equal")) {
      return Constants.OperationType.NOTEQUAL;
    } else if (function.equals("urn:abc4trust:1.0:function:dateTime-not-equal")) {
      return Constants.OperationType.NOTEQUAL;
    } else if (function.equals("urn:abc4trust:1.0:function:anyURI-not-equal")) {
      return Constants.OperationType.NOTEQUAL;
    } else if (function.equals("urn:abc4trust:1.0:function:string-equal-oneof")) {
      return Constants.OperationType.EQUALONEOF;
    } else if (function.equals("urn:abc4trust:1.0:function:boolean-equal-oneof")) {
      return Constants.OperationType.EQUALONEOF;
    } else if (function.equals("urn:abc4trust:1.0:function:integer-equal-oneof")) {
      return Constants.OperationType.EQUALONEOF;
    } else if (function.equals("urn:abc4trust:1.0:function:date-equal-oneof")) {
      return Constants.OperationType.EQUALONEOF;
    } else if (function.equals("urn:abc4trust:1.0:function:time-equal-oneof")) {
      return Constants.OperationType.EQUALONEOF;
    } else if (function.equals("urn:abc4trust:1.0:function:dateTime-equal-oneof")) {
      return Constants.OperationType.EQUALONEOF;
    } else if (function.equals("urn:abc4trust:1.0:function:anyURI-equal-oneof")) {
      return Constants.OperationType.EQUALONEOF;
    } else {
      throw new RuntimeException("Cannot parse function name: '" + function + "'");
    }
  }

  public static String returnTypeOfFunction(URI functionAsUri) {
    String function = functionAsUri.toString();
    if (function.equals("urn:oasis:names:tc:xacml:1.0:function:string-equal")) {
      return Constants.STRING_TYPE;
    } else if (function.equals("urn:oasis:names:tc:xacml:1.0:function:boolean-equal")) {
      return Constants.BOOLEAN_TYPE;
    } else if (function.equals("urn:oasis:names:tc:xacml:1.0:function:integer-equal")) {
      return Constants.INTEGER_TYPE;
    } else if (function.equals("urn:oasis:names:tc:xacml:1.0:function:date-equal")) {
      return Constants.DATE_TYPE;
    } else if (function.equals("urn:oasis:names:tc:xacml:1.0:function:time-equal")) {
      return Constants.TIME_TYPE;
    } else if (function.equals("urn:oasis:names:tc:xacml:1.0:function:dateTime-equal")) {
      return Constants.DATETIME_TYPE;
    } else if (function.equals("urn:oasis:names:tc:xacml:1.0:function:anyURI-equal")) {
      return Constants.URI_TYPE;
    } else if (function.equals("urn:oasis:names:tc:xacml:1.0:function:integer-greater-than")) {
      return Constants.INTEGER_TYPE;
    } else if (function
        .equals("urn:oasis:names:tc:xacml:1.0:function:integer-greater-than-or-equal")) {
      return Constants.INTEGER_TYPE;
    } else if (function.equals("urn:oasis:names:tc:xacml:1.0:function:integer-less-than")) {
      return Constants.INTEGER_TYPE;
    } else if (function.equals("urn:oasis:names:tc:xacml:1.0:function:integer-less-than-or-equal")) {
      return Constants.INTEGER_TYPE;
    } else if (function.equals("urn:oasis:names:tc:xacml:1.0:function:date-greater-than")) {
      return Constants.DATE_TYPE;
    } else if (function.equals("urn:oasis:names:tc:xacml:1.0:function:date-greater-than-or-equal")) {
      return Constants.DATE_TYPE;
    } else if (function.equals("urn:oasis:names:tc:xacml:1.0:function:date-less-than")) {
      return Constants.DATE_TYPE;
    } else if (function.equals("urn:oasis:names:tc:xacml:1.0:function:date-less-than-or-equal")) {
      return Constants.DATE_TYPE;
    } else if (function.equals("urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than")) {
      return Constants.DATETIME_TYPE;
    } else if (function.equals("urn:oasis:names:tc:xacml:1.0:function:dateTime-greater-than-or-equal")) {
      return Constants.DATETIME_TYPE;
    } else if (function.equals("urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than")) {
      return Constants.DATETIME_TYPE;
    } else if (function.equals("urn:oasis:names:tc:xacml:1.0:function:dateTime-less-than-or-equal")) {
      return Constants.DATETIME_TYPE;
    } else if (function.equals("urn:abc4trust:1.0:function:string-not-equal")) {
      return Constants.STRING_TYPE;
    } else if (function.equals("urn:abc4trust:1.0:function:boolean-not-equal")) {
      return Constants.BOOLEAN_TYPE;
    } else if (function.equals("urn:abc4trust:1.0:function:integer-not-equal")) {
      return Constants.INTEGER_TYPE;
    } else if (function.equals("urn:abc4trust:1.0:function:date-not-equal")) {
      return Constants.DATE_TYPE;
    } else if (function.equals("urn:abc4trust:1.0:function:time-not-equal")) {
      return Constants.TIME_TYPE;
    } else if (function.equals("urn:abc4trust:1.0:function:dateTime-not-equal")) {
      return Constants.DATETIME_TYPE;
    } else if (function.equals("urn:abc4trust:1.0:function:anyURI-not-equal")) {
      return Constants.URI_TYPE;
    } else if (function.equals("urn:abc4trust:1.0:function:string-equal-oneof")) {
      return Constants.STRING_TYPE;
    } else if (function.equals("urn:abc4trust:1.0:function:boolean-equal-oneof")) {
      return Constants.BOOLEAN_TYPE;
    } else if (function.equals("urn:abc4trust:1.0:function:integer-equal-oneof")) {
      return Constants.INTEGER_TYPE;
    } else if (function.equals("urn:abc4trust:1.0:function:date-equal-oneof")) {
      return Constants.DATE_TYPE;
    } else if (function.equals("urn:abc4trust:1.0:function:time-equal-oneof")) {
      return Constants.TIME_TYPE;
    } else if (function.equals("urn:abc4trust:1.0:function:dateTime-equal-oneof")) {
      return Constants.DATETIME_TYPE;
    } else if (function.equals("urn:abc4trust:1.0:function:anyURI-equal-oneof")) {
      return Constants.URI_TYPE;
    } else {
      throw new RuntimeException("Cannot parse function name: '" + function + "'");
    }
  }

  public static MyAttributeValue parseValueFromFunction(URI function, Object param) {
    String returnType = "";
    returnType = returnTypeOfFunction(function);
    return parseValue(URI.create(returnType), param, null);
  }

  public static boolean evaulateFunction(URI function, List<MyAttributeValue> arguments) {
    String returnType = returnTypeOfFunction(function);
    if(!checkTypes(arguments, returnType)) {
      throw new RuntimeException("Incorrect type of arguments, when evaluating function: '"
        + function + "'");
    }
    Constants.OperationType op = operationTypeOfFunction(function);
    if (!isArgumentNumberCorrect(op, arguments.size())) {
      throw new RuntimeException("Incorrect number of arguments when evaluating function: '"
          + function + "'");
    }
    switch (op) {
      case EQUAL:
        return arguments.get(0).isCompatibleAndEquals(arguments.get(1));
      case NOTEQUAL:
        return arguments.get(0).isCompatibleAndNotEquals(arguments.get(1));
      case EQUALONEOF: {
        MyAttributeValue first = arguments.get(0);
        for (int i = 1; i < arguments.size(); ++i) {
          if (first.isCompatibleAndEquals(arguments.get(i))) {
            return true;
          }
        }
        return false;
      }
      case LESS:
        return arguments.get(0).isCompatibleAndLess(arguments.get(1));
      case LESSEQ:
        return arguments.get(0).isCompatibleAndLessOrEqual(arguments.get(1));
      case GREATER:
        return arguments.get(1).isCompatibleAndLess(arguments.get(0));
      case GREATEREQ:
        return arguments.get(1).isCompatibleAndLessOrEqual(arguments.get(0));
      default:
        throw new RuntimeException("Problem with evaluating function: '" + function + "'");
    }
  }

  private static boolean checkTypes(List<MyAttributeValue> arguments, String returnType) {
    Class<?> expectedClass = getClassOfType(returnType);
    for(MyAttributeValue argument:arguments) {
      if ( ! expectedClass.isInstance(argument)) {
        return false;
      }
    }
    return true;
  }
  
  private static Class<?> getClassOfType(String type) {
    if (type.equals(Constants.STRING_TYPE)) {
      return MyAttributeValueString.class;
    } else if (type.equals(Constants.BOOLEAN_TYPE)) {
      return MyAttributeValueBoolean.class;
    } else if (type.equals(Constants.INTEGER_TYPE)) {
      return MyAttributeValueInteger.class;
    } else if (type.equals(Constants.DATE_TYPE)) {
      return MyAttributeValueDate.class;
    } else if (type.equals(Constants.TIME_TYPE)) {
      return MyAttributeValueTime.class;
    } else if (type.equals(Constants.DATETIME_TYPE)) {
      return MyAttributeValueDateTime.class;
    } else if (type.equals(Constants.URI_TYPE)) {
      return MyAttributeValueUri.class;
    } else {
      throw new RuntimeException("Cannot parse attribute data type: '" + type + "'");
    }
  }

  private static boolean isArgumentNumberCorrect(Constants.OperationType op, int size) {
    switch (op) {
      case EQUAL:
        return (size == 2);
      case NOTEQUAL:
        return (size == 2);
      case EQUALONEOF:
        return (size >= 2);
      case LESS:
        return (size == 2);
      case LESSEQ:
        return (size == 2);
      case GREATER:
        return (size == 2);
      case GREATEREQ:
        return (size == 2);
      default:
        throw new RuntimeException("Problem with evaluating operation: '" + op + "'");
    }
  }

}
