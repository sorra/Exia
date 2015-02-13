package github.exia.util;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileBufferedReader {
	public static String readToString(String filename) throws IOException {
		List<String> lines = readToLines(filename);
		StringBuilder sb = new StringBuilder();
		for (String line : lines) {
			sb.append(line);
			sb.append("\n");
		}
		return sb.toString();
	}
	
	public static List<String> readToLines(String filename) throws IOException {
		if (!new File(filename).exists()) {
			throw new FileNotFoundException(filename);
		}

		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(filename));

			List<String> lines = new ArrayList<String>();

			while (true) {
				String line = reader.readLine();
				if (line == null) {
					break;
				}
				lines.add(line);
			}

//			System.out.println("Reading complete. " + filename + ": " + lines.size() + " lines");

			return lines;
		} finally {
			if (reader != null) reader.close();
		}
	}
}
