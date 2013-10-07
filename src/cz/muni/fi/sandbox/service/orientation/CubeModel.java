package cz.muni.fi.sandbox.service.orientation;

import cz.muni.fi.sandbox.utils.linear.Matrix;
import cz.muni.fi.sandbox.utils.linear.VectorBase;

public class CubeModel extends WireframeObject {

	protected VectorBase mBase;
	protected final float LENGTH = 100.0f;
	
	public CubeModel(VectorBase base) {
		
		super(base);
				
		mPoints = new Matrix(8,3, new double[] {
						-LENGTH, -LENGTH, -LENGTH,
						-LENGTH, LENGTH, -LENGTH,
						LENGTH, LENGTH, -LENGTH,
						LENGTH, -LENGTH, -LENGTH,
						-LENGTH, -LENGTH, LENGTH,
						-LENGTH, LENGTH, LENGTH,
						LENGTH, LENGTH, LENGTH,
						LENGTH, -LENGTH, LENGTH }
		);
		mPointsInBase = mPoints;
		
		mLines = new int[][] {
					{ 0, 1 },
					{ 0, 3 },
					{ 0, 4 },
					{ 1, 2 },
					{ 1, 5 },
					{ 2, 3 },
					{ 2, 6 },
					{ 3, 7 },
					{ 4, 5 },
					{ 4, 7 },
					{ 5, 6 },
					{ 6, 7 }};
		
		mProjectedPoints = new float[8][2];
	}
	
	
}
