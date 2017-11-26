package com.iostate.exia.module;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.iostate.exia.util.MyLogger;

public class ModuleCommander {
  private static MyLogger logger = MyLogger.getLogger(ModuleCommander.class);

  private static List<File> modules = new ArrayList<>();

  public static void setModules(List<File> modules) {
    ModuleCommander.modules = modules;
  }

  public static void runInModules(String command) throws InterruptedException {
    Map<String, Process> mappings = new HashMap<>();

    for (File module : /*listModules()*/ modules) {
      Process p = runCommand(command, module);
      mappings.put(module.getName(), p);
    }
    for (Map.Entry<String, Process> entry : mappings.entrySet()) {
      String modName = entry.getKey();
      Process p = entry.getValue();
      if (p.waitFor() != 0) {
        logger.log("abnormal termination of process for module name " + modName);
      }
      int charCount = 0;
      charCount += printStream(p.getInputStream());
      charCount += printStream(p.getErrorStream());
      if (charCount > 0) System.out.println();
    }
  }

  private static Process runCommand(String command, File workdir) {
    System.out.println("Run \"" + command + "\" in " + workdir);
    try {
      String[] cmd = {"/bin/sh", "-c", command};
      return Runtime.getRuntime().exec(cmd, null, workdir);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static int printStream(InputStream in) {
    StringBuilder sb = new StringBuilder();
    try {
      while (in.available() > 0) {
        sb.append((char) in.read());
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    System.out.print(sb);
    return sb.length();
  }
}
