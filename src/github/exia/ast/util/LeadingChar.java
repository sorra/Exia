package github.exia.ast.util;

public class LeadingChar {
	public static String shift(String word) {
		char[] chars = word.toCharArray();
		chars[0] = Character.toUpperCase(chars[0]);
		return new String(chars);
	}
	
	public static String unshift(String word) {
		char[] chars = word.toCharArray();
		chars[0] = Character.toLowerCase(chars[0]);
		return new String(chars);
	}
}
