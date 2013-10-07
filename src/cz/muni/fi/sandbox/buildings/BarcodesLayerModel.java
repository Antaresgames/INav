package cz.muni.fi.sandbox.buildings;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import android.util.Log;
import cz.muni.fi.sandbox.utils.geometric.Grid;
import cz.muni.fi.sandbox.utils.geometric.Line2D;
import cz.muni.fi.sandbox.utils.geometric.Point2D;
import cz.muni.fi.sandbox.utils.geometric.Polygon;
import cz.muni.fi.sandbox.utils.geometric.Rectangle;
import cz.muni.fi.sandbox.utils.geometric.RectangleGrid;

public class BarcodesLayerModel implements Serializable {

	private static final long serialVersionUID = 5841671515887301278L;
	
	
	private static final String TAG = "BarcodesLayerModel";
	private Map<String, Point2D> completeSet;

	public BarcodesLayerModel() {
		completeSet = new HashMap<String, Point2D>();
	}

	public boolean addBarcode(String code,Point2D poi) {
		return completeSet.put(code, poi)==null;
	}


	public boolean addBarcode(String name, double x1, double y1) {
		return completeSet.put(name, new Point2D(x1, y1))==null;
	}

	public Point2D getPoint(String barcode) {
		return completeSet.get(barcode);
	}

}
