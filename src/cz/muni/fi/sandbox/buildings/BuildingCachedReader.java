package cz.muni.fi.sandbox.buildings;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.os.Environment;
import android.util.Log;

/**
 * Class for reading the xml definition of building areas.
 * 
 * <?xml version="1.0" encoding="utf-8"?> <!DOCTYPE document SYSTEM
 * "building.dtd"> <building> <area name="Basement" originX="598621.6863073101"
 * originY="1159062.0162205047"> <walls format="text" path="walls/FI_P01.txt" />
 * <stairs format="text" path="stairs/FI_P01.txt" /> <transitions format="text"
 * path="transitions/FI_P01.txt" /> <rss format="text" grid="2"
 * path="rss/rss_FI_P01.txt" /> </area> </building>
 */

public class BuildingCachedReader {

	private final String TAG = "BuildingXmlReader";

	private int mAreaIndex = 0;
	private String mAreaName = "Floor";
	private Building mBuilding = null;
	private AreaBuilder mAreaBuilder = null;
	private double mAreaOriginX, mAreaOriginY;
	private double mScale = 1.0f;
	private String mParentDirectory;

	public Building read(String filename) {

		File root = Environment.getExternalStorageDirectory();
		File xml = new File(root, filename);
		mParentDirectory = xml.getParent() + "/";

		InputStream in = null;
		mBuilding = null;

		try {
			in = new BufferedInputStream(new FileInputStream(xml));

			XMLReader myXMLReader = SAXParserFactory.newInstance()
					.newSAXParser().getXMLReader();

			myXMLReader.setContentHandler(new BuildingXmlHandler());

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
		if (mBuilding == null) {
			Log.e(TAG, "returning null building");
		}
		return mBuilding;
	}

	private class BuildingXmlHandler extends DefaultHandler {

		private long startTime = 0;

		@Override
		public void startDocument() throws SAXException {
			Log.d(TAG, "startDocument");
			startTime = System.nanoTime();
		}

		@Override
		public void endDocument() throws SAXException {
			Log.d(TAG, "endDocument: in " + (System.nanoTime() - startTime)
					/ Math.pow(10, 9) + " seconds");
		}

		@Override
		public void startElement(String uri, String localName, String qName,
				Attributes attributes) throws SAXException {

			Log.d(TAG, "startElement: " + localName);
			
			if (localName.equalsIgnoreCase("building")) {
				
				mBuilding = new Building();

			} else if (localName.equalsIgnoreCase("area")) {
				
				// read area name
				// read origin coordinates
				for (int i = 0; i < attributes.getLength(); i++) {

					if (attributes.getLocalName(i).equals("name")) {
						mAreaName = attributes.getValue(i);
					}
					if (attributes.getLocalName(i).equals("originX")) {
						mAreaOriginX = Double.valueOf(attributes.getValue(i));
					}
					if (attributes.getLocalName(i).equals("originY")) {
						mAreaOriginY = Double.valueOf(attributes.getValue(i));
					}
					if (mAreaName == null || mAreaOriginX == Double.NaN
							|| mAreaOriginY == Double.NaN) {
						throw new RuntimeException(
								"required attributes not present (name|originX|originY)");
					}
					mAreaBuilder = AreaBuilder.builder();

				}

			} else if (localName.equalsIgnoreCase("walls")) {
				// read path
				String path = null;
				for (int i = 0; i < attributes.getLength(); i++) {

					if (attributes.getLocalName(i).equals("format")) {
						// ignored, for the time being
					} else if (attributes.getLocalName(i).equals("path")) {
						path = attributes.getValue(i);
					}
				}
				if (path != null) {
					mAreaBuilder.readSimpleTextWalls(mParentDirectory + path);
				} else {
					throw new RuntimeException("required attribute path");
				}
				

			} else if (localName.equalsIgnoreCase("stairs")) {
				// read path
				String path = "";
				for (int i = 0; i < attributes.getLength(); i++) {

					if (attributes.getLocalName(i).equals("format")) {
						// ignored, for the time being
					} else if (attributes.getLocalName(i).equals("path")) {
						path = attributes.getValue(i);
					}
				}
				if (path != null) {
					mAreaBuilder.readSimpleTextStairs(mParentDirectory + path);
				} else {
					throw new RuntimeException("required attribute path");
				}

			} else if (localName.equalsIgnoreCase("transitions")) {
				// read path
				String path = "";
				for (int i = 0; i < attributes.getLength(); i++) {
					if (attributes.getLocalName(i).equals("format")) {
						// ignored, for the time being
					} else if (attributes.getLocalName(i).equals("path")) {
						path = attributes.getValue(i);
					}
				}
				if (path != null) {
					mAreaBuilder.readSimpleTextTransitions(mParentDirectory + path);
				} else {
					throw new RuntimeException("required attribute path");
				}

			} else if (localName.equalsIgnoreCase("rss")) {
				// read grid spacing
				// read path
				String path = "";
				//double gridSpacing = 2;
				for (int i = 0; i < attributes.getLength(); i++) {

					if (attributes.getLocalName(i).equals("path")) {
						path = attributes.getValue(i);
					} else if (attributes.getLocalName(i).equals("grid")) {
						// TODO: apply the grid spacing value
						//gridSpacing = Double.valueOf(attributes.getValue(i));
					}
				}
				if (path != null) {
					mAreaBuilder.readRssFingerprints(mParentDirectory + path);
				} else {
					throw new RuntimeException("required attribute path");
				}				
			}
		}

		@Override
		public void endElement(String uri, String localName, String qName) {

			Log.d(TAG, "endElement: " + localName);
			if (localName.equalsIgnoreCase("area")) {
				if (mBuilding != null && mAreaBuilder != null) {
					mAreaBuilder.scale(mAreaOriginX, mAreaOriginY, mScale, mScale);
					mBuilding.addArea(mAreaIndex, mAreaName, mAreaBuilder.create());
				} else {
					Log.e(TAG, "mBuilding == null || mAreaBuilder == null");
				}
				mAreaName = null;
				mAreaBuilder = null;
				mAreaOriginX = Double.NaN;
				mAreaOriginY = Double.NaN;
				mAreaIndex++;
			}
		}

	}

}
