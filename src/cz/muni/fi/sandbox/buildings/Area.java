package cz.muni.fi.sandbox.buildings;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import cz.muni.fi.sandbox.service.wifi.InterpolatedFingerprintModel;
import cz.muni.fi.sandbox.service.wifi.RssFingerprintWriter;
import cz.muni.fi.sandbox.service.wifi.WifiLayerModel;
import cz.muni.fi.sandbox.utils.geometric.Line2D;
import cz.muni.fi.sandbox.utils.geometric.Point2D;
import cz.muni.fi.sandbox.utils.geometric.Rectangle;

public class Area implements Serializable {
	
	private static final long serialVersionUID = -2066515773234136384L;
	
	protected AreaLayerModel mWallsModel;
	protected PointsLayerModel pointsModel;
	protected BarcodesLayerModel barcodesModel;
	transient protected WifiLayerModel mWifiLayerModel;
	protected AreaLayerModel mTransitionEdges;
	protected AreaLayerModel mStairs;
	transient protected RssFingerprintWriter mRssWriter;
	public double originX;

	public double originY;
	
	private final float GRID_SIZE = 2.0f;
	
	public Area() {
		mWallsModel = new AreaLayerModel();
		pointsModel = new  PointsLayerModel();
		barcodesModel = new  BarcodesLayerModel();
		mStairs = new AreaLayerModel();
		mTransitionEdges = new AreaLayerModel();
		
		mWifiLayerModel = new InterpolatedFingerprintModel();
		mRssWriter = new RssFingerprintWriter("rss_fingerprints.txt");
	}
	
	public void optimize() {
		optimize(GRID_SIZE);
	}
	
	public void optimize(float grid) {
		mWallsModel.computeBuckets(grid);
		mStairs.computeBuckets(grid);
//		pointsModel.computeBuckets(grid);
	}
	
	public void setWalls(Collection<Line2D> walls) {
		for (Line2D line: walls) {
			mWallsModel.addWall(line);
		}
	}
	
	public void setPoints(Map<Point2D, String> points) {
		for (Point2D point : points.keySet()) {
			String poiName = points.get(point);
			pointsModel.addPoint(point, poiName);
		}
	}
	
	public void setBarcodes(Map<String, Point2D> barcodes) {
		for (String key : barcodes.keySet()) {
			barcodesModel.addBarcode(key, barcodes.get(key));
		}
	}
	
	public void setStairs(Collection<Line2D> stairs) {
		for (Line2D line: stairs) {
			mStairs.addWall(line);
		}
	}
		
	public AreaLayerModel getWallsModel() {
		return mWallsModel;
	}
	
	public PointsLayerModel getPointsModel() {
		return pointsModel;
	}
	
	public BarcodesLayerModel getBarcodesModel() {
		return barcodesModel;
	}
	
	public WifiLayerModel getRssFingerprints() {
		return mWifiLayerModel;
	}
	
	public void setRssFingerprints(WifiLayerModel set) {
		mWifiLayerModel.copyFrom(set);
	}
	
	public void setDisplayCropBox(Rectangle box) {
		mWallsModel.setDisplayCropBox(box);
		pointsModel.setDisplayCropBox(box);
//		barcodesModel.setDisplayCropBox(box);
		mStairs.setDisplayCropBox(box);
	}

	public void setBoundingBox(Rectangle box) {
		mWallsModel.setBoundingBox(box);
//		pointsMap.setBoundingBox(box);
		mStairs.setBoundingBox(box);
	}
	
	public AreaLayerModel getTransitionEdgeLayer() {
		return mTransitionEdges;
	}
	
	public void setTransitionEdgeLayer(AreaLayerModel transitionEdges) {
		mTransitionEdges = transitionEdges;
	}
	
	
	public AreaLayerModel getStairsLayer() {
		return mStairs;
	}
	
	public void setSteps(AreaLayerModel stairsLayer) {
		mStairs = stairsLayer;
	}
	
	public RssFingerprintWriter getRssFingerprintWriter() {
		return mRssWriter;
	}

	public void setRssFingerprintWriter(RssFingerprintWriter rssWriter) {
		this.mRssWriter = rssWriter;
	}
	
	protected static Collection<Line2D> scaleWalls(Collection<Line2D> otherWalls, float originX, float originY, float scaleX,
			float scaleY) {
		ArrayList<Line2D> walls = new ArrayList<Line2D>();
		for (Line2D wall : otherWalls) {
			double x1 = scaleX * ((double) wall.getX1() + originX);
			double y1 = scaleY * ((double) wall.getY1() + originY);
			double x2 = scaleX * ((double) wall.getX2() + originX);
			double y2 = scaleY * ((double) wall.getY2() + originY);
			walls.add(new Line2D(x1, y1, x2, y2));
		}
		return walls;
	}
}
