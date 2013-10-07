package cz.muni.fi.sandbox.buildings;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.os.Environment;
import android.util.Log;
import cz.muni.fi.sandbox.utils.geometric.Line2D;

/**
 * AndroidConvertedSvgReader class
 * @author Michal Holcik
 *
 */
public class AndroidConvertedSvgReader {

	private final String TAG = "AndroidConvertedSvgReader";

	ArrayList<Line2D> lines;

	public ArrayList<Line2D> readWalls(String filename) {

		File root = Environment.getExternalStorageDirectory();

		InputStream in = null;

		lines = new ArrayList<Line2D>();

		try {
			in = new BufferedInputStream(new FileInputStream(new File(root,
					filename)));

			XMLReader myXMLReader = SAXParserFactory.newInstance()
					.newSAXParser().getXMLReader();

			myXMLReader.setContentHandler(new SvgHandler());

			myXMLReader.parse(new InputSource(in));

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();

		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		}
		return lines;
	}

	private class SvgHandler extends DefaultHandler {

		int numberPolylines = 0;
		long startTime = 0;

		@Override
		public void startDocument() throws SAXException {
			Log.d(TAG, "startDocument");
			startTime = System.nanoTime();
		}

		@Override
		public void endDocument() throws SAXException {
			Log.d(TAG, "in total " + numberPolylines + " polylines");
			Log.d(TAG, "in total " + lines.size() + " polylines");
			Log.d(TAG,
					"in " + (System.nanoTime() - startTime) / Math.pow(10, 9)
							+ " seconds");
		}

		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {

			if (localName.equalsIgnoreCase("polyline")) {
				// System.out.println("polyline:");
				numberPolylines++;

				for (int i = 0; i < attributes.getLength(); i++) {
					if (attributes.getLocalName(i).equals("points")) {
						String value = attributes.getValue(i);
						Pattern pattern = Pattern.compile("[, ]");
						String[] coords = pattern.split(value);
						// Log.d(TAG, Arrays.toString(coords));

						lines.add(new Line2D(Double.valueOf(coords[0]), Double
								.valueOf(coords[1]), Double.valueOf(coords[2]),
								Double.valueOf(coords[3])));
					}
				}

			}
		}

	}

}
