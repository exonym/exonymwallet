/*
 * Copyright (c) 2023. All Rights Reserved. Exonym GmbH
 */

package io.exonym.lib.abc.util;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;
import io.exonym.lib.pojo.Namespace;


public class AbcNamespace extends NamespacePrefixMapper {

  @Override
  public String getPreferredPrefix(String base, String other, boolean arg2) {

      if (base.startsWith("http://abc4trust.eu/wp2")) {
          return "abc";
      } else if(base.equals("http://www.w3.org/2001/XMLSchema")) {
          return "xs";
      } else if(base.equals("http://www.w3.org/2001/XMLSchema-instance")) {
          return "xsi";
      } else if(base.equals(Namespace.EX)) {
          return "exonym";
      } else {
          return other;
      }
  }
}
