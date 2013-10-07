package cz.muni.fi.sandbox.utils.geographical;

/**
 * Class representing geographical location in S-JTSK coordinate system using
 * Krovak's projection
 * 
 * Conversion algorithm from WGS-84 (convert() method) taken over from [1].
 * 
 * Bibliography: 1. Hrdina, Z.; Transformace souradnic ze systemu WGS-84 do
 * systemu S-JTSK, CVUT v Praze, Praha, 1997.
 */

public class SjtskLocation {
	private double[] coords;

	public SjtskLocation(double x, double y) {
		coords = new double[2];
		coords[0] = x;
		coords[1] = y;
	}
	
	public SjtskLocation(double[] coords) {
		assert(coords.length == 2);
		this.coords = new double[2];
		System.arraycopy(coords, 0, this.coords, 0, 2);
	}

	
	public double getX() {
		return coords[0];
	}

	public double getY() {
		return coords[1];
	}

	@Override
	public String toString() {
		return "Y:" + getX() + " X:" + getY();
	}

	/**
	 * converts WGS-84 to S-JTSK
	 * 
	 * @param wgs84
	 *            WGS-84 location
	 * @return S-JTSK location coordinates
	 */
	public static SjtskLocation convert(Wgs84Location wgs84) {

		final double B = wgs84.getLatitude() * Math.PI / 180;
		final double L = wgs84.getLongitude() * Math.PI / 180;
		final double H = wgs84.getAltitude();
		
		double[] xyz =  blhToXyz(new double[] { B, L, H });
		double[] xyz2 = transform(xyz);
		double[] sjtskBlh = xyzToBlh(xyz2);
		double[] planarXY = blhToPlanarXy(sjtskBlh);
		return new SjtskLocation(planarXY);
	}
	
	
	public static double[] transform(double[] orthoWgs) {

		double dx = -570.69;
		double dy = -85.69;
		double dz = -462.84;
		double wx = 4.99821 / 3600 * Math.PI / 180;
		double wy = 1.58676 / 3600 * Math.PI / 180;
		double wz = 5.2611 / 3600 * Math.PI / 180;
		double m = -3.543e-6;

		double x2 = dx + (1 + m)
				* (orthoWgs[0] + wz * orthoWgs[1] - wy * orthoWgs[2]);
		double y2 = dy + (1 + m)
				* (-wz * orthoWgs[0] + orthoWgs[1] + wx * orthoWgs[2]);
		double z2 = dz + (1 + m)
				* (wy * orthoWgs[0] - wx * orthoWgs[1] + orthoWgs[2]);

		return new double[] { x2, y2, z2 };
	}

	public static double[] blhToXyz(double[] blh) {

		double B = blh[0];
		double L = blh[1];
		double H = blh[2];

		double a = 6378137.0;
		double f1 = 298.257223563;

		double e2 = 1 - Math.pow(1 - 1 / f1, 2);
		double rho = a / Math.sqrt(1 - e2 * Math.pow(Math.sin(B), 2));
		double x1 = (rho + H) * Math.cos(B) * Math.cos(L);
		double y1 = (rho + H) * Math.cos(B) * Math.sin(L);
		double z1 = ((1 - e2) * rho + H) * Math.sin(B);

		return new double[] { x1, y1, z1 };
	}

	public static double[] xyzToBlh(double[] xyz) {

		double x2 = xyz[0];
		double y2 = xyz[1];
		double z2 = xyz[2];

		double a = 6377397.15508;
		double f1 = 299.152812853;
		double ab = f1 / (f1 - 1);
		double p = Math.sqrt(Math.pow(x2, 2) + Math.pow(y2, 2));
		double e2 = 1 - Math.pow(1 - 1 / f1, 2);
		double th = Math.atan(z2 * ab / p);
		double st = Math.sin(th);
		double ct = Math.cos(th);
		double t = (z2 + e2 * ab * a * (st * st * st))
				/ (p - e2 * a * (ct * ct * ct));

		double B = Math.atan(t);
		double H = Math.sqrt(1 + t * t)
				* (p - a / Math.sqrt(1 + (1 - e2) * t * t));
		double L = 2 * Math.atan(y2 / (p + x2));

		return new double[] { B, L, H };
	}
	
	private static double sqr(double x) {
		return x * x;
	}

	public static double[] blhToPlanarXy(double[] blh) {
		
		double B = blh[0];
		double L = blh[1];
		//double H = blh[2];
		
		//double a = 6377397.15508;
		double e = 0.081696831215303;
		double n = 0.97992470462083;
		double rho0 = 12310230.12797036;
		double sinUQ = 0.863499969506341;
		double cosUQ = 0.504348889819882;
		double sinVQ = 0.420215144586493;
		double cosVQ = 0.907424504992097;
		double alpha = 1.000597498371542;
		double k2 = 1.00685001861538;

		double sinB = Math.sin(B);
		double t = (1 - e * sinB) / (1 + e * sinB);
		t = sqr(1 + sinB) / (1 - sqr(sinB))
				* Math.exp(e * Math.log(t));
		t = k2 * Math.exp(alpha * Math.log(t));

		double sinU = (t - 1) / (t + 1);
		double cosU = Math.sqrt(1 - sinU * sinU);
		double V = alpha * L;
		double sinV = Math.sin(V);
		double cosV = Math.cos(V);
		double cosDV = cosVQ * cosV + sinVQ * sinV;
		double sinDV = sinVQ * cosV - cosVQ * sinV;
		double sinS = sinUQ * sinU + cosUQ * cosU * cosDV;
		double cosS = Math.sqrt(1 - sinS * sinS);
		double sinD = sinDV * cosU / cosS;
		double cosD = Math.sqrt(1 - sinD * sinD);

		double eps = n * Math.atan(sinD / cosD);
		double rho = rho0 * Math.exp(-n * Math.log((1 + sinS) / cosS));

		return new double[] {-rho * Math.sin(eps), -rho * Math.cos(eps)};
	}
}
