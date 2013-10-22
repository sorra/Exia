package github.exia.util;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import junit.framework.Assert;

public class CommonUtils {
	private static MyLogger logger = MyLogger.getLogger(CommonUtils.class);
	
    public static void runInModules(String command) throws InterruptedException {
	  Map<String,Process> mappings = new HashMap<String,Process>();
	  
	  for (File module : /*listModules()*/new ArrayList<File>()) {
	    Process p = runCommand(command, module);
	    mappings.put(module.getName(),p);
	  }
	  for (Map.Entry<String,Process> entry : mappings.entrySet()) {
	    String modName = entry.getKey();
	    Process p = entry.getValue();
	    if (p.waitFor() != 0) {
	      logger.log("abnormal termination of process for module name " + modName);
	    }
        int charCount = 0;
	    charCount += printStream(p.getInputStream());
        charCount += printStream(p.getErrorStream());
        if(charCount > 0) System.out.println();
	  }
	}

    private static Process runCommand(String command, File workdir) {
      System.out.println("Run \""+command+"\" in "+workdir);
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
          sb.append((char)in.read());
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      System.out.print(sb);
      return sb.length();
    }
	
	public static String mapDebugString(Map<?,?> map) {
		StringBuilder sb = new StringBuilder();
		sb.append("map.size = ");
		sb.append(map.size());
		sb.append('\n');
		for (Entry<?, ?> entry : map.entrySet()) {
			sb.append(entry);
			sb.append('\n');
		}
		return sb.toString();
	}
	
	public static String combineLines(List<String> lines) {
		StringBuilder sb = new StringBuilder();
		for (String each : lines) {
			sb.append(each).append('\n');
		}
		return sb.toString();
	}
	
	/**
	 * Readin a list from string, and "null" is regarded as null
	 * @param string a string representation of list, elements mustn't contain ','!
	 * @return the desired list
	 */
	public static List<String> readListRegardNull(String string) {
	  Assert.assertTrue(string.startsWith("["));
	  Assert.assertTrue(string.endsWith("]"));
	  String eleStr = string.substring(1, string.length()-1);
	  String[] elements = eleStr.split(", ");
	  
	  List<String> list = new ArrayList<String>();
	  for (String e : elements) {
	    Assert.assertTrue(e.indexOf(' ') == -1);
	    if (e.equals("null")) {
	      e = null;
	    }
	    list.add(e);
	  }
	  Assert.assertEquals(elements.length, list.size());
	  return list;
	}
	
	public static String getLastName(String longName) {
	  String octName = longName.replace('.', '/');
	  return octName.substring(octName.lastIndexOf('/')+1, octName.length());
	}
}
