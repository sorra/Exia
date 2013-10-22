package github.exia.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;


public class FileMaker {
    private static MyLogger logger = MyLogger.getLogger(FileMaker.class);
	
	public static boolean createDir(String destDirName) {
		File dir = new File(destDirName);
		if (dir.exists()) {
			System.out.println("Create Dir " + destDirName
					+ " Failed, Dir has already existed!");
			return false;
		}
		if (!destDirName.endsWith(File.separator))
			destDirName = destDirName + File.separator;

		if (dir.mkdirs()) {
			System.out.println("Create Dir " + destDirName + " Success!");
			return true;
		} else {
			System.out.println("Create Dir " + destDirName + " Success!");
			return false;
		}
	}

	public static boolean createFile(String destFileName) {
		File file = new File(destFileName);

		if (file.exists()) {
			// System.out.println(" File already exists! Overwrite it!");
			file.delete();
		}

		if (destFileName.endsWith(File.separator)) {
			System.out.println("Create File " + destFileName
					+ " Failed, Can not be a Dir");
			return false;
		}

		if (!file.getParentFile().exists()) {
			System.out.println("Dir did not exist，creating。。。");
			if (!file.getParentFile().mkdirs()) {

				System.out.println("Create Dir failed！");
				return false;
			}
		}

		// Create File
		try {
			if (file.createNewFile()) {
//				System.out.println("Create " + destFileName + " Success!");
				return true;
			} else {
				System.out.println("Create " + destFileName + " Failed!");
				return false;
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Create " + destFileName + " Failed!");
			return false;
		}
	}

	public static boolean writePlainFile(String fileName, String content, String charsetName) {
		File file = new File(fileName);
		try {
			if(!createFile(fileName)){
				return false;
			}
			OutputStreamWriter os = null;
			if (charsetName == null || charsetName.length() == 0) {
				os = new OutputStreamWriter(new FileOutputStream(file));
			} else {
				os = new OutputStreamWriter(new FileOutputStream(file), charsetName);
			}
			os.write(content);
			os.close();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;

	}

}
