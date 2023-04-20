
package io.exonym.lib.abc.attributeEncoding;

import java.math.BigInteger;
import java.net.URI;


public interface MyAttributeEncoding {
  public BigInteger getIntegerValue();
  public URI getEncoding();
  // Java doesn't allow static methods in interfaces...
  //public static Object recoverValueFromIntegerValue(BigInteger integerValue);
}
