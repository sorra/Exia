package com.iostate.exia.util;

/*
 * Consider an expression: A equals B
 * subject is left side
 * object is right side
 */
public abstract class StringMatcher {

  // interface
  public abstract boolean matches(String subject);

  public static StringMatcher equals(String object) {

    return new Equals(object);
  }

  public static StringMatcher startsWith(String object) {
    return new StartsWith(object);
  }

  public static StringMatcher endsWith(String object) {
    return new EndsWith(object);
  }

  public static StringMatcher longerEndsWith(String object) {
    return new LongerEndsWith(object);
  }

  // concrete classes
  static class Equals extends StringMatcher {
    private String object;

    Equals(String object) {
      this.object = object;
    }

    @Override
    public boolean matches(String subject) {
      return subject.equals(object);
    }
  }

  static class StartsWith extends StringMatcher {
    private String object;

    StartsWith(String object) {
      this.object = object;
    }

    @Override
    public boolean matches(String subject) {
      return subject.startsWith(object);
    }
  }

  static class EndsWith extends StringMatcher {
    private String object;

    EndsWith(String object) {
      this.object = object;
    }

    @Override
    public boolean matches(String subject) {
      return subject.endsWith(object);
    }
  }

  static class LongerEndsWith extends StringMatcher {
    private String object;

    LongerEndsWith(String object) {
      this.object = object;
    }

    @Override
    public boolean matches(String subject) {
      return subject.length() > object.length() && subject.endsWith(object);
    }
  }
}