package cz.muni.fi.sandbox.buildings.legacy;

import java.util.ArrayList;
import java.util.Collection;

import cz.muni.fi.sandbox.buildings.Area;
import cz.muni.fi.sandbox.buildings.Building;
import cz.muni.fi.sandbox.utils.geometric.Line2D;

public class BuildingNo1 extends Building {
	
	private static final long serialVersionUID = -1828656800278694642L;

	private class AreaNo1 extends Area {
		
		private static final long serialVersionUID = 4176282859192469447L;

		public AreaNo1() {
			super();
			
			// all dimensions must be in meters
			// correction of origin
			float wallOriginX = -567f;
			float wallOriginY = 34f;
			float scale = 0.13f * 1.42f;

			
			Collection<Line2D> walls = new ArrayList<Line2D>();
			walls.add(new Line2D(865.195, 295.5, 865.195, 295.5));
			walls.add(new Line2D(865.195, 295.5, 865.195, 295.5));
			walls.add(new Line2D(850, 377, 851, 439));
			walls.add(new Line2D(1014, 338.75, 980.016, 337.516));
			walls.add(new Line2D(899.016, 373.016, 900.016, 435.016));
			walls.add(new Line2D(773.016, 438.516, 773, 495.25));
			walls.add(new Line2D(622, 361.25, 622, 493.484));
			walls.add(new Line2D(621, 31.25, 623, 331.984));
			walls.add(new Line2D(807, 31.25, 621.007, 33.4909));
			walls.add(new Line2D(805, 130.25, 805.012, 32.0029));
			walls.add(new Line2D(804, 175.25, 804.012, 152.75));
			walls.add(new Line2D(802, 234.25, 803.013, 174.751));
			walls.add(new Line2D(867, 231.25, 804.03, 231.767));
			walls.add(new Line2D(953, 243.25, 869.008, 243.275));
			walls.add(new Line2D(1078, 228.25, 969, 228.775));
			walls.add(new Line2D(1077, 467.75, 1076, 227.28));
			walls.add(new Line2D(1076, 465.255, 977, 465.75));
			walls.add(new Line2D(980.009, 437.255, 898, 435.75));
			walls.add(new Line2D(852.028, 434.773, 772, 435.75));
			walls.add(new Line2D(752, 311.75, 621.012, 311.239));
			walls.add(new Line2D(771.992, 354.765, 753, 354.75));
			walls.add(new Line2D(736.993, 354.266, 675, 354.75));
			walls.add(new Line2D(772, 331.75, 772, 434.484));
			walls.add(new Line2D(980, 320.75, 978, 465.984));
			walls.add(new Line2D(955.013, 242.264, 954, 318.75));
			walls.add(new Line2D(970, 225.75, 953.013, 242.749));
			walls.add(new Line2D(1076.02, 339.52, 1042.04, 338.286));
			walls.add(new Line2D(674.016, 311.516, 676, 355));
			walls.add(new Line2D(668, 31.5, 668, 74));
			walls.add(new Line2D(718, 176, 621.012, 175.753));
			walls.add(new Line2D(717.991, 73.7497, 657, 75));
			walls.add(new Line2D(637.011, 74.5203, 620, 75));
			walls.add(new Line2D(717, 146, 718.012, 63.5001));
			walls.add(new Line2D(744, 175, 745.024, 155.512));
			walls.add(new Line2D(804, 176, 744, 176));
			walls.add(new Line2D(804, 111.5, 744, 111.5));
			walls.add(new Line2D(743.051, 131.039, 744.075, 111.551));
			walls.add(new Line2D(718.051, 46.0389, 719.075, 26.5511));
			walls.add(new Line2D(868.027, 231.513, 867, 317));
			
			
			for (Line2D wall: scaleWalls(walls, wallOriginX, wallOriginY, scale, scale)) {
				mWallsModel.addWall(wall);
			}
			
		}

	}
	
	public BuildingNo1() {
		
		addArea(0, "Ground Floor", 
				new AreaNo1());
		optimize();
	}
}
