package cz.muni.fi.sandbox.buildings.legacy;

import java.util.ArrayList;
import java.util.Collection;

import cz.muni.fi.sandbox.buildings.Area;
import cz.muni.fi.sandbox.utils.geometric.Line2D;

public class AreaMediaHall1St extends Area {

	private static final long serialVersionUID = -7844672270361416183L;

	public AreaMediaHall1St() {

		float scale = 2 * 15.5f / 585f;
		float wallOriginX = -1643f;
		float wallOriginY = -630f;
		
		Collection<Line2D> walls = new ArrayList<Line2D>();
		walls.add(new Line2D(1160.77, 888.432, 1160.77, 888.432));
		walls.add(new Line2D(1446.88, 603.168, 1545.42, 699.843));
		walls.add(new Line2D(1665, 538.055, 1666.05, 651.151));
		walls.add(new Line2D(1694.58, 539.624, 1520.8, 534.436));
		walls.add(new Line2D(1524.82, 534.753, 1472.98, 583.903));
		walls.add(new Line2D(1456.3, 600.661, 1412.87, 644.006));
		walls.add(new Line2D(1395.56, 660.058, 1201.45, 855.43));
		walls.add(new Line2D(1588.94, 645.244, 1292.03, 943.641));
		walls.add(new Line2D(1212.93, 736.536, 1146.37, 802.008));
		walls.add(new Line2D(1186.88, 659.881, 1095.03, 748.763));
		walls.add(new Line2D(1294.36, 944.348, 986.065, 641.001));
		walls.add(new Line2D(1291.6, 765.946, 1273.15, 749.895));
		walls.add(new Line2D(1257.67, 731.997, 1176.98, 653.199));
		walls.add(new Line2D(1160.29, 634.49, 1061.02, 537.234));
		walls.add(new Line2D(1079.4, 554.522, 987.55, 643.405));
		walls.add(new Line2D(1292.95, 706.231, 1169.98, 583.691));
		walls.add(new Line2D(1415.28, 583.196, 1366.49, 536.527));
		walls.add(new Line2D(1415.98, 582.489, 1290.12, 706.231));
		walls.add(new Line2D(1216.58, 536.527, 1170.69, 583.69));
		walls.add(new Line2D(1267.49, 559.154, 1215.24, 538.224));
		walls.add(new Line2D(1367.79, 537.332, 1326.89, 556.326));
		walls.add(new Line2D(1328.24, 555.815, 1259.71, 558.447));
		walls.add(new Line2D(1061.72, 540.77, 1062.43, 488.444));
		walls.add(new Line2D(1061.39, 491.367, 1249.11, 489.151));
		walls.add(new Line2D(1271.73, 505.414, 1244.82, 487.411));
		walls.add(new Line2D(1310.62, 504.707, 1265.06, 504.629));
		walls.add(new Line2D(1333.83, 488.146, 1303.52, 504.345));
		walls.add(new Line2D(1330.09, 490.045, 1462.7, 489.869));
		walls.add(new Line2D(1388.29, 384.769, 2189.4, 391.571));
		walls.add(new Line2D(1390.5, 383.52, 1372.94, 410.663));
		walls.add(new Line2D(1373.6, 409.035, 1351.02, 424.805));
		walls.add(new Line2D(1292.07, 382.213, 1246.82, 395.844));
		walls.add(new Line2D(1353.14, 425.832, 1322.61, 390.119));
		walls.add(new Line2D(1225.9, 425.459, 1226.72, 491.387));
		walls.add(new Line2D(1882.53, 610.744, 1877.99, 1405.7));
		walls.add(new Line2D(1883.17, 650.768, 1586.64, 646.416));
		walls.add(new Line2D(2003.78, 612.689, 1879.74, 612.875));
		walls.add(new Line2D(2003.85, 677.155, 2003.87, 692.19));
		walls.add(new Line2D(2005.05, 716.594, 1881, 716.78));
		walls.add(new Line2D(2181.86, 1406.83, 1877.11, 1405.72));
		walls.add(new Line2D(2181.63, 681.908, 2178.62, 1408.13));
		walls.add(new Line2D(2183.77, 688.712, 2059.72, 688.898));
		walls.add(new Line2D(2061.9, 591.291, 2061.25, 690.497));
		walls.add(new Line2D(1959.45, 540.715, 1959.23, 614.796));
		walls.add(new Line2D(1765.57, 540.877, 1714.15, 539.766));
		walls.add(new Line2D(1841.43, 541.517, 1790, 540.407));
		walls.add(new Line2D(1960.75, 541.525, 1862.04, 540.861));
		walls.add(new Line2D(1960.02, 495.019, 1861.31, 494.355));
		walls.add(new Line2D(1959.6, 388.21, 1960.52, 495.162));
		walls.add(new Line2D(2032.07, 389.796, 2032.26, 487.542));
		walls.add(new Line2D(2188.11, 390.604, 2188.3, 488.349));
		walls.add(new Line2D(2188.11, 482.679, 2148.14, 522.073));
		walls.add(new Line2D(2188.94, 483.091, 2058.66, 485.113));
		walls.add(new Line2D(1811.38, 389.309, 1811.8, 493.378));
		walls.add(new Line2D(1738.44, 387.924, 1738.46, 496.264));
		walls.add(new Line2D(1589.04, 384.206, 1588.97, 492.081));
		walls.add(new Line2D(1738.67, 538.245, 1739.72, 651.342));
		walls.add(new Line2D(1813.09, 538.666, 1814.14, 651.762));
		walls.add(new Line2D(2063.13, 484.281, 2062.54, 549.765));
		walls.add(new Line2D(1962.42, 681.652, 1962.81, 717.893));
		walls.add(new Line2D(2005.1, 681.77, 1962.03, 681.652));
		walls.add(new Line2D(1905.15, 632.263, 1905.09, 683.399));
		walls.add(new Line2D(1882.16, 654.877, 1943.5, 654.263));
		walls.add(new Line2D(1961.78, 653.42, 2003.25, 653.074));
		walls.add(new Line2D(2003.85, 611.749, 2003.69, 659.346));
		walls.add(new Line2D(1970.85, 612.344, 1971.38, 653.966));
		walls.add(new Line2D(1943.2, 684.067, 1923.28, 683.77));
		walls.add(new Line2D(1924.17, 652.851, 1924.17, 689.716));
		walls.add(new Line2D(1924.77, 704.58, 1924.92, 718.879));
		walls.add(new Line2D(1941.12, 642.148, 1941.85, 683.789));
		walls.add(new Line2D(1940.67, 612.447, 1940.82, 626.746));
		walls.add(new Line2D(1779.22, 492.527, 1622.25, 490.786));
		walls.add(new Line2D(1835.96, 492.845, 1803.08, 491.712));
		walls.add(new Line2D(2003.44, 710.06, 2000.69, 1408.29));
		walls.add(new Line2D(2061.77, 688.82, 2057.96, 1338.47));
		walls.add(new Line2D(2057.84, 1374.23, 2057.96, 1408.48));
		walls.add(new Line2D(2057.37, 1305.62, 2181.04, 1306.8));
		walls.add(new Line2D(2090.06, 1335.15, 2154.24, 1333.86));
		walls.add(new Line2D(2091.12, 1379.81, 2181.29, 1378.88));
		walls.add(new Line2D(2092.16, 1379.72, 2092.16, 1335.99));
		walls.add(new Line2D(2154.59, 1378.49, 2154.59, 1334.76));
		walls.add(new Line2D(1588.88, 590.605, 1588.28, 645.846));
		walls.add(new Line2D(1644.75, 417.75, 1645.15, 494.898));
		walls.add(new Line2D(1740.71, 444.658, 1640.86, 444.01));
		walls.add(new Line2D(1713.15, 417.75, 1644.1, 418.398));
		walls.add(new Line2D(1482.87, 490.786, 1603.22, 491.192));
		walls.add(new Line2D(1327.24, 392.027, 1285.8, 382.213));
		walls.add(new Line2D(1253.09, 392.027, 1225.83, 427.467));
		
		for (Line2D wall: scaleWalls(walls, wallOriginX, wallOriginY, scale, -scale)) {
			mWallsModel.addWall(wall);
		}
	}
}
