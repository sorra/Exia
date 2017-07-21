package com.iostate.exia.util;

import org.apache.commons.lang3.StringUtils;

public class StringUtil {
  public static boolean isCapital(String s) {
    if (s == null || s.isEmpty()) return false;
    return Character.isUpperCase(s.charAt(0));
  }

  public static boolean isNotCapital(String s) {
    return !isCapital(s);
  }

  public static String simpleName(String name) {
    int idxDot = name.lastIndexOf('.');
    if (idxDot < 0) {
      return name;
    }
    return name.substring(idxDot + 1);
  }

  public static String qualifier(String name) {
    int idxDot = name.lastIndexOf('.');
    if (idxDot < 0) {
      return "";
    }
    return name.substring(0, idxDot);
  }

  public static String asVarName(String qname) {
    return StringUtils.uncapitalize(simpleName(qname));
  }
}
