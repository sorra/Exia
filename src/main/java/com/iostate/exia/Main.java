package com.iostate.exia;

import java.util.Arrays;

public class Main {

  public static void main(String[] args) throws Exception {
    String className = args[0];
    String[] paths = Arrays.copyOfRange(args, 1, args.length);

    String basePackageName = "com.iostate.exia.samples";
    Class<?> clazz = Class.forName(basePackageName + "." + className);
    clazz.getDeclaredMethod("main", String[].class).invoke(null, (Object) paths);
  }
}
