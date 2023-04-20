/*
 * Copyright (c) 2023. All Rights Reserved. Exonym GmbH
 */

package io.exonym.lib.abc.util;

public class Constants {
	
	  public static final String STRING_TYPE = "string";
	  public static final String BOOLEAN_TYPE = "boolean";
	  public static final String INTEGER_TYPE = "integer";
	  public static final String DATE_TYPE = "date";
	  public static final String TIME_TYPE = "time";
	  public static final String DATETIME_TYPE = "dateTime";
	  public static final String URI_TYPE = "anyURI";
	  
	  //predefined predicates for spesific attribute types
	  public static final String EXPIRES = "Expires";
	  public static final String DATEOFBIRTH = "DateOfBirth";
	  
	  public enum PredefinedAttrTypesForPredicates {
		    EXPIRES, DATEOFBIRTH
	  }

	  public enum OperationType {
	    EQUAL, NOTEQUAL, EQUALONEOF, LESS, LESSEQ, GREATER, GREATEREQ
	  }
	  
	  public static final String CONSTANT = "constant";

}
