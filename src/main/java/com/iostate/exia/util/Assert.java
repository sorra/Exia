package com.iostate.exia.util;

import java.util.Objects;

public class Assert {
  public static void assertNotNull(Object o) {
    if (o == null) {
      throw new RuntimeException("It should not be null!");
    }
  }

  public static void assertEquals(Object expected, Object actual) {
    if (!Objects.equals(expected, actual)) {
      throw new RuntimeException("Not equal! expected: " + expected + ", actual: " + actual);
    }
  }

  public static void assertTrue(boolean condition) {
    if (!condition) {
      throw new RuntimeException("It's not true!");
    }
  }
}
