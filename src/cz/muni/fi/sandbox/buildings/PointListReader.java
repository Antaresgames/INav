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

public class PointListReader {

	private final String TAG = "PointListReader";

	Map<Point2D, String> pointsMap;
	
	public Map<Point2D, String> readPoints(String filename) {
		
		Log.d(TAG, "readPoints(filename = " + filename + ")");
		File inputFile = new File(filename); 
		if (!inputFile.isAbsolute()) {
			File root = Environment.getExternalStorageDirectory();
			inputFile = new File(root, filename);
		}
		
		pointsMap = new HashMap<Point2D, String>();
		
		BufferedReader inputStream = null;
		try {
			inputStream = new BufferedReader(new FileReader(inputFile));
			
//			Pattern pattern = Pattern.compile("[ ]");
			String line = null;
			System.out.println("reading points "+filename);
			while ((line = inputStream.readLine()) != null) {
//				String[] coords = pattern.split(line);
//				if (coords.length == 2) {
//					double x1 = Double.valueOf(coords[0]);
//					double y1 = Double.valueOf(coords[1]);
//					pointsMap.add(new Point2D(x1, y1));
//				}
				System.out.println("line "+line);
				String[] lineSpl = line.split(";");
				double x = Double.valueOf(lineSpl[0].trim());
				double y = Double.valueOf(lineSpl[1].trim());
				Point2D key = new Point2D(x, y);
				pointsMap.put(key, lineSpl[2].trim());
				System.out.println("read point "+x+", "+y+": "+lineSpl[2].trim());
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
		Log.d(TAG, "SimplePointListReader found " + pointsMap.size() + " points");
		return pointsMap;
	}
}
