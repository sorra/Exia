package com.iostate.exia.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class CommandProperties {
  private Properties properties = new Properties();

  public CommandProperties(String filename) throws IOException {
    properties.load(new FileInputStream(filename));
  }

  public String getProperty(String key) {
    return getProperty(key, null);
  }

  public String getProperty(String key, String deft) {
    String sysProp = System.getProperty(key);
    if (sysProp != null) {
      return sysProp;
    } else {
      String fileProp = properties.getProperty(key);
      if (fileProp != null) {
        return fileProp;
      } else if (deft != null) {
        return deft;
      } else {
        throw new IllegalArgumentException("Require property: " + key);
      }
    }
  }
}
