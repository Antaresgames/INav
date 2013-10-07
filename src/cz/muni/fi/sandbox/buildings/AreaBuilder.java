package cz.muni.fi.sandbox.buildings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import cz.muni.fi.sandbox.service.wifi.WifiLayerModel;
import cz.muni.fi.sandbox.service.wifi.RssFingerprintWriter;
import cz.muni.fi.sandbox.utils.geometric.Line2D;
import cz.muni.fi.sandbox.utils.geometric.Point2D;

/**
 * AreaBuilder class. Responsible for incremental construction area parts and
 * creation of a new Area instance.
 * 
 * @author Michal Holcik
 * 
 */
public class AreaBuilder {

	private Area area;
	private Collection<Line2D> mWalls;
	private Map<Point2D, String> pointsMap;
	private Map<String, Point2D> barcodesMap;
	private Collection<Line2D> mStairs;
	
	
	
	private WifiLayerModel mFingerprints;
	private RssFingerprintWriter mRssWriter;
	private double originX, originY;

	/**
	 * AreaBuilder constructor
	 * 
	 */
	public AreaBuilder() {
		area = null;
		mWalls = new ArrayList<Line2D>();
		pointsMap = new HashMap<Point2D, String>();
		barcodesMap = new HashMap<String, Point2D>();
		mStairs = new ArrayList<Line2D>();
		mFingerprints = null;
		mRssWriter = null;
	}

	/**
	 * Creates a new instance of the AreaBuilder
	 * 
	 * @return new instance of the AreaBuilder
	 */
	public static AreaBuilder builder() {

		return new AreaBuilder();
	}

	/**
	 * Creates an instance of the constructed area.
	 * 
	 * @return an instance of the created area.
	 */
	public Area create() {
		area = new Area();
		area.originX = this.originX;
		area.originY = this.originY;
		
		if (mWalls != null)
			area.setWalls(mWalls);
		if (pointsMap != null)
			area.setPoints(pointsMap);
		if (barcodesMap != null)
			area.setBarcodes(barcodesMap);
		if (mStairs != null)
			area.setStairs(mStairs);
		if (mFingerprints != null)
			area.setRssFingerprints(mFingerprints);
		if (mRssWriter != null)
			area.setRssFingerprintWriter(mRssWriter);
		// area.optimize();
		Area retval = area;
		area = null;
		return retval;
	}

	/**
	 * Translates and scales imported stair and wall lines according to given
	 * parameters.
	 * 
	 * @param originX
	 *            x coordinate of the new origin.
	 * @param originY
	 *            y coordinate of the new origin.
	 * @param scaleX
	 *            x axis scale factor.
	 * @param scaleY
	 *            y axis scale factor.
	 * @return this AreaBuilder instance.
	 */
	public AreaBuilder scale(double originX, double originY, double scaleX,
			double scaleY) {
		this.originX = originX;
		this.originY = originY;
		
		scaleLines(mStairs, originX, originY, scaleX, scaleY);
		scaleLines(mWalls, originX, originY, scaleX, scaleY);
		scalePoints(pointsMap, originX, originY, scaleX, scaleY);
		scaleBarcodes(barcodesMap, originX, originY, scaleX, scaleY);
		return this;
	}

	/**
	 * Generic method to scale a collection of lines according to given
	 * parameters. Lines are scaled in place. The input set is used for output.
	 * 
	 * @param lines
	 *            the set of lines to be scaled.
	 * @param originX
	 * @param originY
	 * @param scaleX
	 * @param scaleY
	 */
	private void scaleLines(Collection<Line2D> lines, double originX,
			double originY, double scaleX, double scaleY) {
		ArrayList<Line2D> otherLines = new ArrayList<Line2D>();
		for (Line2D wall : lines) {
			double x1 = scaleX * ((double) wall.getX1() + originX);
			double y1 = scaleY * ((double) wall.getY1() + originY);
			double x2 = scaleX * ((double) wall.getX2() + originX);
			double y2 = scaleY * ((double) wall.getY2() + originY);
			otherLines.add(new Line2D(x1, y1, x2, y2));
		}
		lines.clear();
		lines.addAll(otherLines);
	}
	
	/**
	 * Generic method to scale a collection of points according to given
	 * parameters. Points are scaled in place. The input set is used for output.
	 * 
	 * @param pointsModel
	 *            the set of points to be scaled.
	 * @param originX
	 * @param originY
	 * @param scaleX
	 * @param scaleY
	 */
	private void scalePoints(Map<Point2D, String> lines, double originX,
			double originY, double scaleX, double scaleY) {
		Map<Point2D, String> otherPoints = new HashMap<Point2D, String>();
		for (Point2D point : lines.keySet()) {
			double x1 = scaleX * ((double) point.getX() + originX);
			double y1 = scaleY * ((double) point.getY() + originY);
			otherPoints.put(new Point2D(x1, y1), lines.get(point));
		}
		lines.clear();
		lines.putAll(otherPoints);
	}
	
	private void scaleBarcodes(Map<String, Point2D> barcodes, double originX,
			double originY, double scaleX, double scaleY) {
		Map<String, Point2D> otherBarcodes = new HashMap<String, Point2D>();
		for (String key : barcodes.keySet()) {
			Point2D point = barcodes.get(key);
			double x1 = scaleX * ((double) point.getX() + originX);
			double y1 = scaleY * ((double) point.getY() + originY);
			otherBarcodes.put(key, new Point2D(x1, y1));
		}
		barcodes.clear();
		barcodes.putAll(otherBarcodes);
	}


	/**
	 * Imports wall lines from the file in svg format as it is exported from the
	 * opensource and free Dia tool.
	 * 
	 * @param svgFileName
	 *            name of the svg file.
	 * @return this AreaBuilder instance.
	 */
	public AreaBuilder readDiaExportedSvgArea(String svgFileName) {
		AndroidDiagExportedSvgReader reader = new AndroidDiagExportedSvgReader();
		mWalls = reader.readWalls(svgFileName);
		return this;
	}

	/**
	 * Imports wall lines from the file in svg format is it is exported from
	 * some shareware tool I downloaded from the internet... whose name I
	 * forgot...
	 * 
	 * @param svgFileName
	 * @return this AreaBuilder instance.
	 */
	public AreaBuilder readConvertedSvgArea(String svgFileName) {
		AndroidConvertedSvgReader reader = new AndroidConvertedSvgReader();
		mWalls = reader.readWalls(svgFileName);
		return this;
	}

	/**
	 * Imports wall lines from the file in simple text format. Lines coordinates
	 * are white-space separated, lines are by separated newlines. I.e. 4
	 * coordinates per line.
	 * 
	 * @param fileName
	 *            the name of the file to be read.
	 * @return this AreaBuilder instance.
	 */
	public AreaBuilder readSimpleTextWalls(String fileName) {
		SimpleLineListReader reader = new SimpleLineListReader();
		mWalls = reader.readWalls(fileName);
		return this;
	}
	
	/**
	 * Imports points from the file in simple text format. Point coordinates
	 * are white-space separated, lines are by separated newlines. I.e. 2
	 * coordinates per line.
	 * 
	 * @param fileName
	 *            the name of the file to be read.
	 * @return this AreaBuilder instance.
	 */
	public AreaBuilder readTextPoints(String fileName) {
		PointListReader reader = new PointListReader();
		pointsMap = reader.readPoints(fileName);
		return this;
	}

	/**
	 * Imports stair lines from the file in simple text format. Lines
	 * coordinates are white-space separated, lines are by separated newlines.
	 * I.e. 4 coordinates per line.
	 * 
	 * @param fileName
	 *            of the file to be read.
	 * @return this AreaBuilder instance.
	 */
	public AreaBuilder readSimpleTextStairs(String fileName) {
		SimpleLineListReader reader = new SimpleLineListReader();
		mStairs = reader.readWalls(fileName);
		return this;
	}

	/**
	 * Reads rss fingerprints from the file.
	 * 
	 * @param rssFileName
	 *            relative to sdcard mount point directory
	 * @return this AreaBuilder instance.
	 */
	public AreaBuilder readRssFingerprints(String rssFileName) {
		mFingerprints = new WifiLayerModel(rssFileName);
		mRssWriter = new RssFingerprintWriter(rssFileName);
		return this;
	}

	public AreaBuilder readSimpleTextTransitions(String path) {
		// TODO Auto-generated method stub
		return this;
	}

	public AreaBuilder readBarcodes(String fileName) {
		BarcodesListReader reader = new BarcodesListReader();
		barcodesMap = reader.readBarcodes(fileName);
		return this;
	}
}
