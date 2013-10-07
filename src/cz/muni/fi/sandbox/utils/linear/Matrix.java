package cz.muni.fi.sandbox.utils.linear;

import java.util.Arrays;

public class Matrix {
	double[] data;
	int rows, columns;
	
	final int PRECISION = 6;

	public class MatrixException extends RuntimeException {
		
		private static final long serialVersionUID = -3058550887955836266L;
		
		String reason;
		public MatrixException(String reason) {
			this.reason = reason;
		}
	}
	
	public Matrix(int rows, int columns) {
		this.rows = rows;
		this.columns = columns;
		data = new double[rows * columns];
	}

	public Matrix(int rows, int columns, double[] data) {
		this(rows, columns);
		assert (data.length == rows * columns);
		System.arraycopy(data, 0, this.data, 0, rows * columns);
	}

	public void setRandom() {
		for (int i = 0; i < rows * columns; i++) {
			data[i] = 10.0 * Math.random() - 5.0;
		}
	}

	public static Matrix identity(int rows, int columns) {
		Matrix identity = new Matrix(rows, columns);
		for (int j = 0; j < rows; j++)
			for (int i = 0; i < columns; i++) {
				if (i == j)
					identity.data[i + j * columns] = 1.0;
				else
					identity.data[i + j * columns] = 0.0;
			}
		return identity;
	}

	public void setIdentity() {
		for (int j = 0; j < rows; j++)
			for (int i = 0; i < columns; i++) {
				if (i == j)
					data[i + j * columns] = 1.0;
				else
					data[i + j * columns] = 0.0;
			}
	}

	public static Matrix getRotationMatrix(double[] phi) {

		assert (phi.length == 3);

		double phiSize = Math.sqrt(phi[0] * phi[0] + phi[1] * phi[1] + phi[2]
				* phi[2]);
		double cosPhi = Math.cos(phiSize);
		double oneMinusCosPhi = 1 - cosPhi;
		double sinPhi = Math.sin(phiSize);

		double[] v = { phi[0] / phiSize, phi[1] / phiSize, phi[2] / phiSize };
		double v0v1 = v[0] * v[1];
		double v1v2 = v[1] * v[2];
		double v0v2 = v[0] * v[2];

		Matrix result = new Matrix(3, 3);

		result.data[0] = cosPhi + v[0] * v[0] * oneMinusCosPhi;
		result.data[1] = v0v1 * oneMinusCosPhi - v[2] * sinPhi;
		result.data[2] = v0v2 * oneMinusCosPhi + v[1] * sinPhi;

		result.data[3] = v0v1 * oneMinusCosPhi + v[2] * sinPhi;
		result.data[4] = cosPhi + v[1] * v[1] * oneMinusCosPhi;
		result.data[5] = v1v2 * oneMinusCosPhi - v[0] * sinPhi;

		result.data[6] = v0v2 * oneMinusCosPhi - v[1] * sinPhi;
		result.data[7] = v1v2 * oneMinusCosPhi + v[0] * sinPhi;
		result.data[8] = cosPhi + v[2] * v[2] * oneMinusCosPhi;

		return result;
	}

	public Matrix plus(Matrix other) {
		assert (other.rows == rows && other.columns == columns);

		Matrix result = new Matrix(rows, columns);
		for (int i = 0; i < rows; i++)
			for (int j = 0; j < columns; j++)
				result.data[j + i * 3] = data[j + i * columns]
						+ other.data[j + i * columns];
		return result;

	}

	public Matrix transpose() {

		Matrix result = new Matrix(columns, rows);
		for (int i = 0; i < result.columns; i++)
			for (int j = 0; j < result.rows; j++)
				result.data[i + j * result.columns] = data[j + i * columns];
		return result;

	}

	public Matrix times(Matrix other) {
		
		if (columns != other.rows) {
			throw new MatrixException("incompatible matrices");
		}
		assert (columns == other.rows);

		Matrix result = new Matrix(rows, other.columns);

		for (int i = 0; i < result.columns; i++) {
			for (int j = 0; j < result.rows; j++) {
				double value = 0.0;
				for (int n = 0; n < other.rows; n++) {
					value += this.data[n + columns * j]
							* other.data[i + other.columns * n];
				}
				result.data[i + result.columns * j] = value;
			}
		}

		return result;

	}
	
	public double getValue(int i, int j) {
		return data[i + columns * j];
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + columns;
		result = prime * result + Arrays.hashCode(data);
		result = prime * result + rows;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		return equals(PRECISION, obj);
	}

	public boolean equals(int precision, Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof Matrix)) {
			return false;
		}
		Matrix other = (Matrix) obj;
		if (columns != other.columns) {
			return false;
		}
		if (rows != other.rows) {
			return false;
		}
		
		//if (!Arrays.equals(data, other.data)) {
		//	return false;
		//}

		long precisionFactor = (long) Math.pow(10, precision);
		for (int i = 0; i < data.length; i++) {
			long v1 = (long) (Math.round(precisionFactor * data[i]));
			long v2 = (long) (Math.round(precisionFactor * other.data[i]));
			if (v1 != v2) {
				return false;
			}
		}

		return true;
	}

	public String toString() {
		String result = "\n[";
		for (int i = 0; i < rows; i++) {
			result = result + "[";
			for (int j = 0; j < columns; j++) {
				result += String.format("%." + PRECISION + "f", data[j + i * columns])
						+ ((j == columns - 1) ? "" : " ");
			}
			result = result + "]" + ((i == rows - 1) ? "" : ",\n");
		}
		result = result + "]";
		return result;
	}
}
