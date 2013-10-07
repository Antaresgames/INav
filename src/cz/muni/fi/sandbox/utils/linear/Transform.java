package cz.muni.fi.sandbox.utils.linear;

public class Transform {

	public static double[] getRotationMatrixX(double omega) {
		double[] result = new double[9];
		result[0] = 1.0f;
		result[4] = Math.cos(omega);
		result[5] = -Math.sin(omega);
		result[7] = Math.sin(omega);
		result[8] = Math.cos(omega);
		return result;
	}

	public static double[] getRotationMatrixY(double omega) {
		double[] result = new double[9];
		result[0] = Math.cos(omega);
		result[2] = Math.sin(omega);
		result[4] = 1.0f;
		result[6] = -Math.sin(omega);
		result[8] = Math.cos(omega);
		return result;
	}

	public static double[] getRotationMatrixZ(double omega) {
		double[] result = new double[9];
		result[0] = Math.cos(omega);
		result[1] = -Math.sin(omega);
		result[3] = Math.sin(omega);
		result[4] = Math.cos(omega);
		result[8] = 1.0f;
		return result;
	}


	


	public static double[] multiplyMatrixAndMatrix(double[] m1, double[] m2) {
		double[] result = new double[9];
		final int size = 3;
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				for (int k = 0; k < size; k++) {
					
				}
			}
		}
		return result;
	}

	public static float[] multiplyMatrixAndVector(float[] matrix, float[] vector) {
		assert (vector.length == 3);
		assert (matrix.length == 9);
		float[] result = new float[3];

		for (int i = 0; i < 3; i++) {
			result[i] = 0.0f;
			for (int j = 0; j < 3; j++) {
				result[i] += vector[j] * matrix[i * 3 + j];
			}
		}
		return result;
	}
	
	
	/**
	 * 
	 * A simultaneous orthogonal rotations angle (SORA) is a vector representing
	 * angular orientation of a rigid-body relative to some reference frame. The
	 * components of this vector are equal to the angles of three simultaneous
	 * rotations around the rigid body's intrinsic coordinate system axes,
	 * initially aligned with the axes of the reference frame, needed to move
	 * the rigid body to its current angular orientation.
	 * 
	 * @param sora
	 *            vector = (phi_x, phi_y, phi_z)
	 * @returns rotation matrix
	 */
	public static double[] getRotationMatrixSORA(double[] phi) {
		
		
		assert (phi.length == 3);
		//System.out.println(phi[0] + " " + phi[1] + " " + phi[2]);
		double phiSize = Math.sqrt(phi[0] * phi[0]
				+ phi[1] * phi[1] + phi[2] * phi[2]);
		double cosPhi = Math.cos(phiSize);
		double oneMinusCosPhi = 1 - cosPhi;
		double sinPhi = Math.sin(phiSize);
		
		double[] v = { phi[0] / phiSize, phi[1] / phiSize, phi[2] / phiSize };
		double v0v1 = v[0] * v[1];
		double v1v2 = v[1] * v[2];
		double v0v2 = v[0] * v[2];
		
		double[] result = new double[9];
		
		result[0] = cosPhi + v[0] * v[0] * oneMinusCosPhi;
		result[1] = v0v1 * oneMinusCosPhi - v[2] * sinPhi;
		result[2] = v0v2 * oneMinusCosPhi + v[1] * sinPhi;
		
		result[3] = v0v1 * oneMinusCosPhi + v[2] * sinPhi;
		result[4] = cosPhi + v[1] * v[1] * oneMinusCosPhi;
		result[5] = v1v2 * oneMinusCosPhi - v[0] * sinPhi;
		
		result[6] = v0v2 * oneMinusCosPhi - v[1] * sinPhi;
		result[7] = v1v2 * oneMinusCosPhi + v[0] * sinPhi;
		result[8] = cosPhi + v[2] * v[2] * oneMinusCosPhi;
		
		return result;
	}
	
	public static double[] getRotationMatrixSORA(double x, double y, double z) {
		return getRotationMatrixSORA(new double[] {x, y, z});
	}

}
