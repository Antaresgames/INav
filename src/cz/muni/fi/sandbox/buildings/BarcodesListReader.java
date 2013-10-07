package cz.muni.fi.sandbox.buildings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import android.os.Environment;
import android.util.Log;
import cz.muni.fi.sandbox.utils.geometric.Line2D;
import cz.muni.fi.sandbox.utils.geometric.Point2D;

public class BarcodesListReader {

	private final String TAG = "PointListReader";

	Map<String, Point2D> barcodesMap;
	
	public Map<String, Point2D> readBarcodes(String filename) {
		
		Log.d(TAG, "readPoints(filename = " + filename + ")");
		File inputFile = new File(filename); 
		if (!inputFile.isAbsolute()) {
			File root = Environment.getExternalStorageDirectory();
			inputFile = new File(root, filename);
		}
		
		barcodesMap = new HashMap<String, Point2D>();
		
		BufferedReader inputStream = null;
		try {
			inputStream = new BufferedReader(new FileReader(inputFile));
			
//			Pattern pattern = Pattern.compile("[ ]");
			String line = null;
			while ((line = inputStream.readLine()) != null) {
				
				System.out.println("reading points "+filename);
				String[] lineSpl = line.split(";");
				String key = lineSpl[0].trim();
				double x = Double.valueOf(lineSpl[1].trim());
				double y = Double.valueOf(lineSpl[2].trim());
				barcodesMap.put(key, new Point2D(x, y));
			}
			
			inputStream.close();

		} catch (IOException ex) {
			ex.printStackTrace();
			
			try {
				if (inputStream != null)
					inputStream.close();
			} catch(IOException ex2) {
				ex2.printStackTrace();
			}
		}
		Log.d(TAG, "SimplePointListReader found " + barcodesMap.size() + " points");
		return barcodesMap;
	}
}
