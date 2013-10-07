package cz.muni.fi.sandbox.utils.linear;

public class VectorBase {
	
	private Matrix baseVectors;
	private int mDimension;
	public VectorBase(int dimension) {
		mDimension = dimension;
		baseVectors = Matrix.identity(dimension, dimension);
	}
	
	
	public Vector3d getAxis(int axis) {
		if (mDimension != 3) {
			throw new RuntimeException("not applicable for dimensions != 3");
		}
		return new Vector3d(
				baseVectors.getValue(0, axis),
				baseVectors.getValue(1, axis),
				baseVectors.getValue(2, axis)
			);
	}
	
	/*
	public align(double[] vector, int axisIndex) {
		
		double rotationVector[] = new double[3];
		
		vectorProduct = 
		
		rotationVector[0] = -(values[0] + lastValues[0]);
		rotationVector[1] = -(values[1] + lastValues[1]) / 2 * deltaT
				* TO_RADIANS;
		rotationVector[2] = -(values[2] + lastValues[2]) / 2 * deltaT
				* TO_RADIANS;
		double[] rotationMatrix = Transform
				.getRotationMatrixSORA(rotationVector);

		//double x = -(values[0]) * deltaT * SLOW_DOWN;
		//double y = (values[1]) * deltaT * SLOW_DOWN;
		//double z = (values[2]) * deltaT * SLOW_DOWN;
		//double[] rotationMatrix2 = Transform.getRotationMatrixSORA(x, y, z);

		// System.out.println("rotationMatrix:\n" + rotationMatrix[0]+" " +
		// rotationMatrix[1] + " " + rotationMatrix[2] + "\n"
		// + rotationMatrix[3] + " " + rotationMatrix[4] + " " +
		// rotationMatrix[5]+ "\n"
		// + rotationMatrix[6] + " " + rotationMatrix[7] + " " +
		// rotationMatrix[8]);

		transform(new Matrix(3, 3, rotationMatrix));
	}
	*/
	
	public void setBase(int dimension) {
		baseVectors = Matrix.identity(dimension, dimension);
	}
	
	public void transform(Matrix transformation) {
		baseVectors = baseVectors.times(transformation.transpose());
	}
	
	public Matrix from(Matrix vectors) {
		//return baseVectors.transpose().times(vectors.transpose()).transpose();
		return vectors.times(baseVectors);
	}
	
	public Matrix to(Matrix vectors) {
		return vectors.times(baseVectors.transpose());
	}
	
	public String toString() {
		return "base = " + baseVectors;
	}
}
