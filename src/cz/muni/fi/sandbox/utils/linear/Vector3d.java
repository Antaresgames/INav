package cz.muni.fi.sandbox.utils.linear;

public class Vector3d {
	
	private double[] data;
	
	public Vector3d() {
		data = new double[3];
	}
	
	public Vector3d(double x, double y, double z) {
		data = new double[] {x, y, z};
	}
	
	public double[] getValues() {
		return data;
	}
	
	public String toString() {
		return "[" + data[0] + ", " + data[1] + ", " + data[2] + "]";
	}

	public Vector3d crossProduct(Vector3d other) {
		return new Vector3d(
			this.data[1] * other.data[2] - this.data[2] * other.data[1],
			- (this.data[0] * other.data[2] - this.data[2] * other.data[0]),
			this.data[0] * other.data[1] - this.data[1] * other.data[0]
		);
	}
	
	public double size() {
		return Math.sqrt(data[0] * data[0] + data[1] * data[1] + data[2] * data[2]);
	}
	
	public Vector3d normalize() {
		Vector3d result = new Vector3d();
		double vectorSize = size();
		for (int i = 0; i < 3; i++)
			result.data[i] = data[i] / vectorSize;
		return result;
	}
	
	public Vector3d scalarMultiple(double a) {
		data[0] *= a;
		data[1] *= a;
		data[2] *= a;
		return this;
	}
	
	public static double[] crossProduct3d(double[] a, double[] b) {
		double[] result = new double[3];
		result[0] = a[1] * b[2] - a[2] * b[1];
		result[1] = - (a[0] * b[2] - a[2] * b[0]);
		result[2] = a[0] * b[1] - a[1] * b[0];
		return result;
	}
	
	public static double vectorSize3d(double[] a) {
		return Math.sqrt(a[0] * a[0] + a[1] * a[1] + a[2] * a[2]);
	}
	
	public static double[] normalizeVector3d(double[] a) {
		double vectorSize = vectorSize3d(a);
		for (int i = 0; i < 3; i++)
			a[i] /= vectorSize;
		return a;
	}	
}
