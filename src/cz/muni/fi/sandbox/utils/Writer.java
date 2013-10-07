package cz.muni.fi.sandbox.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.util.Log;

/** 
 * Writer class is a wrapper for file output stream.
 * @author Michal Holcik
 *
 */
public class Writer {

	
	private final String TAG = "Writer";
	protected FileOutputStream outputStream;
	protected File file;
	protected String eol;
	
	public Writer(String fileName) {
		Log.d(TAG, "constructor");
		eol = System.getProperty("line.separator");
		file = AndroidFileUtils.getFileFromPath(fileName);
	}
	
	public File getFile() {
		return file;
	}
	
	public void writeln(String line) {
		try {
			if (outputStream != null)
			outputStream.write((line + eol).getBytes());
		} catch (IOException e) {
			Log.d(TAG, "exception: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void openAppend() {
		Log.d(TAG, "openAppend");
		open(true);
	}
	
	public void open(boolean append) {
		Log.d(TAG, "open");
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			outputStream = new FileOutputStream(file, append);
		} catch (IOException e) {
			Log.e(TAG, "exception: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void close() {
		Log.d(TAG, "close");
		try {
			outputStream.close();
		} catch (IOException e) {
			Log.e(TAG, "exception: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void delete() {
		Log.d(TAG, "delete");
		file.delete();
	}
}
