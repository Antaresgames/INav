package cz.muni.fi.sandbox.buildings;

import cz.muni.fi.sandbox.utils.geometric.Line2D;

public class AreaDummy extends Area {
	
	private static final long serialVersionUID = 5044618952475968010L;

	public AreaDummy() {
		super();
		
		// load walls
		mWallsModel.addWall(new Line2D(20.0, 20.0, 80.0, 20.0));
		mWallsModel.addWall(new Line2D(20.0, 20.0, 20.0, 80.0));
		mWallsModel.addWall(new Line2D(20.0, 80.0, 80.0, 80.0));
		
		mWallsModel.addWall(new Line2D(80.0, 20.0, 80.0, 40.0));
		mWallsModel.addWall(new Line2D(80.0, 60.0, 80.0, 80.0));
	}
	
}
