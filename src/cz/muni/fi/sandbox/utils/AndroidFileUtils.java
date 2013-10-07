package cz.muni.fi.sandbox.utils;

import java.io.File;

import android.os.Environment;

public class AndroidFileUtils {
	/**
	 * Return file to the filename if absolute path is given. If filename is
	 * relative path, external storage directory is used as the parent
	 * directory.
	 * 
	 * @param filename name of the file to be opened.
	 * @return file from the filename
	 */
	public static File getFileFromPath(String filename) {
		File file = new File(filename);
		if (!file.isAbsolute()) {
			File root = Environment.getExternalStorageDirectory();
			file = new File(root, filename);
		}
		return file;
	}
}
