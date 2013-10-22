package github.exia.ast.util;

import java.util.HashMap;
import java.util.Map;

public class PrimitiveTypeBoxer {
	private static Map<String, String> map = new HashMap<String, String>();

	static {
		map.put("byte", "Byte");
		map.put("short", "Short");
		map.put("char", "Character");
		map.put("int", "Integer");
		map.put("long", "Long");
		map.put("float", "Float");
		map.put("double", "Double");
		map.put("boolean", "Boolean");
		map.put("void", "Void");
	}
	
	public static String box(String typename) {
		return map.get(typename);
	}
}
