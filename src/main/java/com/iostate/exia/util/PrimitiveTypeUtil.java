package com.iostate.exia.util;

import java.util.HashMap;
import java.util.Map;

public class PrimitiveTypeUtil {
  public static boolean isPrimitive(String typename) {
    return prim2Box.containsKey(typename);
  }

  public static String box(String primTypename) {
    String box = prim2Box.get(primTypename);
    return box != null ? box : primTypename;
  }

  private static final Map<String, String> prim2Box = new HashMap<>();

  static {
    prim2Box.put("void", "Void");
    prim2Box.put("boolean", "Boolean");
    prim2Box.put("char", "Character");
    prim2Box.put("byte", "Byte");
    prim2Box.put("short", "Short");
    prim2Box.put("int", "Integer");
    prim2Box.put("long", "Long");
    prim2Box.put("float", "Float");
    prim2Box.put("double", "Double");
  }
}
