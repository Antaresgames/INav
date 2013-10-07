package cz.muni.fi.sandbox.service.wifi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.os.Environment;
import android.util.Log;
import cz.muni.fi.sandbox.utils.geometric.Line2D;
import cz.muni.fi.sandbox.utils.geometric.Point2D;

public class RssFingerprintReader {

	private static final String TAG = "RssFingerprintReader";

	ArrayList<Line2D> mLines;
	
	public static void readFingerprints(String filename, Set<RssFingerprint> set) {
		
		Log.d(TAG, "readFingerprints(filename = " + filename + ")");
		int records = 0;
		RssFingerprint fingerprintRecord = null;
		
		
		File inputFile = new File(filename); 
		if (!inputFile.isAbsolute()) {
			File root = Environment.getExternalStorageDirectory();
			inputFile = new File(root, filename);
		}
		
		BufferedReader inputStream = null;
		try {
			inputStream = new BufferedReader(new FileReader(inputFile));
			
			// fingerprint: loc: (2.0, -2.0)
			Pattern fingerprintPattern = Pattern.compile("fingerprint: loc: \\((\\S+), (\\S+)\\)");
			// entry: d8:5d:4c:f5:43:de -41.75
			Pattern entryPattern = Pattern.compile("entry: (\\S+) (\\S+)");

			String line = null;
			while ((line = inputStream.readLine()) != null) {
				
				if (fingerprintPattern.matcher(line).matches()) {
					Matcher m = fingerprintPattern.matcher(line);
					m.matches();
					Log.d(TAG, "line matches fingerprint: " + line);
					records++;
					
					if (m.groupCount() == 2) {
						double x1 = Double.valueOf(m.group(1));
						double y1 = Double.valueOf(m.group(2));
						fingerprintRecord = new RssFingerprint();
						fingerprintRecord.setLocation(new Point2D(x1, y1));
						set.add(fingerprintRecord);
					} else {
						fingerprintRecord = null;
					}
					
				} else if (entryPattern.matcher(line).matches()) {
					Log.d(TAG, "line matches event");
					Matcher m2 = entryPattern.matcher(line);
					m2.matches();
					if (m2.groupCount() == 2) {
						String accessPointId = m2.group(1);
						double rss = Double.valueOf(m2.group(2));
						if (fingerprintRecord != null) {
							fingerprintRecord.add(accessPointId, rss);
						}
					}
				}
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
		Log.d(TAG, "RssFingerprintReader found " + records + " rss fingerprint record(s)");
	}
}
