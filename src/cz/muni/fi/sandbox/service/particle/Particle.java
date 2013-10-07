package cz.muni.fi.sandbox.service.particle;

import java.util.Arrays;

import thirdparty.distr.NormalDistribution;


public class Particle implements Cloneable {

	double[] state; // x, y, north, stride length
	double x, y, north, step;
	int areaIndex;

	/*
	public Particle(double posX, double posY, double heading, double stepLength) {
		this.state = new double[] { posX, posY, heading, stepLength };
		this.areaIndex = 0;
	}
	*/
	
	public Particle(double posX, double posY, double heading, double stepLength, int areaIndex) {
		this.state = new double[] { posX, posY, heading, stepLength };
		this.areaIndex = areaIndex;
	}

	// polar version
	public static Particle polarNormalDistr(double meanX, double meanY,
			double sigma, double headingDeflection, double headingSpread,
			double stepLength, double stepSpread, int areaIdx) {

		double angle = 2 * Math.PI * Math.random();
		double distance = sigma * NormalDistribution.inverse(Math.random());
		
		double x = meanX + (distance * Math.cos(angle));
		double y = meanY + (distance * Math.sin(angle));
		
		double randomHeading = headingDeflection + headingSpread * NormalDistribution.inverse(Math.random());
		double randomStepLength = stepLength + stepSpread * NormalDistribution.inverse(Math.random());
		return new Particle(x, y, randomHeading, randomStepLength, areaIdx);
	}

	public static Particle evenSpread(float meanX, float meanY, float sizeX, float sizeY,
			double headingDeflection, double headingSpread,
			double stepLength, double stepSpread, int areaIdx) {
		
		
		double x = meanX + ((Math.random() - 0.5) *  sizeX);
		double y = meanY + ((Math.random() - 0.5) * sizeY);
		
		double randomHeading = headingDeflection + headingSpread * NormalDistribution.inverse(Math.random());
		double randomStepLength = stepLength + stepSpread * NormalDistribution.inverse(Math.random());
		
		return new Particle(x, y, randomHeading, randomStepLength, areaIdx);
	}

	public double[] getState() {
		return state;
	}
	public int getAreaIndex() {
		return areaIndex;
	}

	public String toString() {
		return "particle(" + Arrays.toString(state) + ", " + areaIndex + ")";
	}

	public Particle copy() {
		return new Particle(state[0], state[1], state[2], state[3], areaIndex);
	}

}
