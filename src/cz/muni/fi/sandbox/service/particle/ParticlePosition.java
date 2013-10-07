package cz.muni.fi.sandbox.service.particle;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import thirdparty.distr.NormalDistribution;
import android.content.SharedPreferences;
import android.util.Log;
import cz.muni.fi.sandbox.buildings.AreaLayerModel;
import cz.muni.fi.sandbox.buildings.Building;
import cz.muni.fi.sandbox.buildings.EmptyBuilding;
import cz.muni.fi.sandbox.navigation.PositionModel;
import cz.muni.fi.sandbox.navigation.PositionModelUpdateListener;
import cz.muni.fi.sandbox.navigation.PositionRenderer;
import cz.muni.fi.sandbox.service.motion.MotionProvider;
import cz.muni.fi.sandbox.service.wifi.ProbabilityMap;
import cz.muni.fi.sandbox.service.wifi.WifiPositionUpdateListener;
import cz.muni.fi.sandbox.utils.MathUtils;
import cz.muni.fi.sandbox.utils.geometric.GridPoint2D;
import cz.muni.fi.sandbox.utils.geometric.Line2D;
import cz.muni.fi.sandbox.utils.geometric.Point2D;
import cz.muni.fi.sandbox.utils.geometric.Rectangle;

/**
 * ParticlePosition class. Represents a model of position computation by means
 * of a particle filter.
 * 
 * @author Michal Holcik
 * 
 */
public class ParticlePosition implements PositionModel,
		WifiPositionUpdateListener {

	private final String TAG = "ParticlePositionClass";
	private Set<Particle> particles;
	private Building mBuilding;
	private PositionModelUpdateListener mPositionListener;
	private MotionProvider mProvider;
	private double[] mCloudAverageState;
	private ProbabilityMap mWifiProbabilityMap;

	private static final int DEFAULT_PARTICLE_COUNT = 1000;
	private static final float DEFAULT_ALPHA = .99f;
	private static final float HEADING_SIGMA = (float) (Math.PI * 10f / 180f);
	private static final float DEFAULT_STEP_LENGTH = .70f;
	private static final float DEFAULT_STEP_LENGTH_SPREAD = .1f * DEFAULT_STEP_LENGTH;
	private static final float HEADING_DEFLECTION = 0.0f;
	private static final float DEFAULT_HEADING_SPREAD = 10f / 180f * (float) Math.PI;
	private static final double NANO = Math.pow(10, 9);

	private float mPositionSigma = 1.0f;
	private float mHeading; // compass heading
	private double[] mCoords;
	private Rectangle mBox;

	private int mNumberOfParticles;
	private float mStepProbability;
	private float mStepLength;
	private float mStepLengthSpread;
	private float mHeadingSpread;
	private boolean mCheckWallsCollisions;
	private boolean mCheckStairsCollisions;
	private boolean mCheckTransitionEdgeCollisions;

	private ParticleGenerationMode mParticleGeneration = ParticleGenerationMode.GAUSSIAN;

	// private ParticleGenerationMode mParticleGeneration =
	// ParticleGenerationMode.UNIFORM;

	public enum ParticleGenerationMode {
		GAUSSIAN, UNIFORM
	}

	public ParticlePosition(PositionModelUpdateListener positionListener,
			SharedPreferences prefs) {

		updatePreferences(prefs);

		int numberOfParticles = mNumberOfParticles;
		particles = new HashSet<Particle>(numberOfParticles);
		while (numberOfParticles > 0) {
			particles.add(Particle.polarNormalDistr(0.0f, 0.0f, 1.0f,
					HEADING_DEFLECTION, mHeadingSpread, mStepLength,
					mStepLengthSpread, 0));
			numberOfParticles--;
		}
		for (Particle p : particles) {
			Log.d(TAG, p.toString());
		}
		mBuilding = new EmptyBuilding();
		mPositionListener = positionListener;

		mCoords = new double[4];
		mCloudAverageState = new double[4];
	}

	/**
	 * ParticlePosition model constructor.
	 * 
	 * @param numberOfParticles
	 * @param posX
	 *            x coordinate of the position
	 * @param posY
	 *            y coordinate of the position
	 * @param sigma
	 *            standard deviation of the distance from the posX, posY
	 * @param positionListener
	 * @param prefs
	 *            preferences object with configuration data.
	 */
	public ParticlePosition(float posX, float posY, float sigma, int area,
			PositionModelUpdateListener positionListener) {
		this(posX, posY, sigma, area, positionListener, null);
	}

	/**
	 * ParticlePosition model constructor.
	 * 
	 * @param numberOfParticles
	 * @param posX
	 *            x coordinate of the position
	 * @param posY
	 *            y coordinate of the position
	 * @param sigma
	 *            standard deviation of the distance from the posX, posY
	 * @param positionListener
	 * @param prefs
	 *            preferences object with configuration data.
	 */
	public ParticlePosition(float posX, float posY, float sigma, int area,
			PositionModelUpdateListener positionListener,
			SharedPreferences prefs) {
		this(positionListener, prefs);
		mPositionSigma = sigma;
		setPosition(posX, posY, area);
	}

	public ParticlePosition(float posX, float posY, float sigma, int area,
			PositionModelUpdateListener positionListener,
			SharedPreferences prefs, ParticleGenerationMode particleGeneration) {
		this(positionListener, prefs);
		mPositionSigma = sigma;
		mParticleGeneration = particleGeneration;
		setPosition(posX, posY, area);
	}

	/**
	 * Sets the building.
	 * 
	 * @param building
	 *            the building to be set
	 */
	public void setBuilding(Building building) {
		mBuilding = building;
	}

	/**
	 * Get building.
	 * 
	 * @return building
	 */
	public Building getBuilding() {
		return mBuilding;
	}

	@Override
	public void setPosition(float posX, float posY, int area) {
		if (mParticleGeneration == ParticleGenerationMode.GAUSSIAN) {
			setPositionNormDistr(posX, posY, mPositionSigma, area);
		} else {
			setPositionEvenlySpread(posX, posY, 2 * mPositionSigma,
					2 * mPositionSigma, area);
		}
	}

	public void setPositionNormDistr(float posX, float posY, float sigma,
			int area) {
		int number = mNumberOfParticles;
		particles = new HashSet<Particle>(mNumberOfParticles);
		while (number > 0) {
			particles.add(Particle.polarNormalDistr(posX, posY, sigma,
					HEADING_DEFLECTION, mHeadingSpread, mStepLength,
					mStepLengthSpread, area));
			number--;
		}
		computeCloudAverageState();
	}

	public void setPositionEvenlySpread(float posX, float posY, float spreadX,
			float spreadY, int area) {
		int number = mNumberOfParticles;
		particles = new HashSet<Particle>(mNumberOfParticles);
		while (number > 0) {
			particles.add(Particle.evenSpread(posX, posY, spreadX, spreadY,
					HEADING_DEFLECTION, mHeadingSpread, mStepLength,
					mStepLengthSpread, area));
			number--;
		}
		computeCloudAverageState();
	}

	private void setPositionBasedOnProbabilityMap(ProbabilityMap map, int area) {

		particles = new HashSet<Particle>(mNumberOfParticles);
		int gridSpacing = map.getGridSize();
		GridPoint2D origin = map.getOrigin();
		GridPoint2D size = map.getSize();
		double total = 0.0;
		for (int x = origin.x; x < origin.x + size.x; x++) {
			for (int y = origin.y; y < origin.y + size.y; y++) {
				total += map.getProbability(x, y);
			}
		}
		Log.d(TAG, "total probability sum = " + total);
		for (int x = origin.x; x < origin.x + size.x; x++) {
			for (int y = origin.y; y < origin.y + size.y; y++) {
				Log.d(TAG,
						"setPositionBasedOnProbabilityMap: setting position on "
								+ x + " " + y);
				float prob = map.getProbability(x, y);
				int number = (int) (prob * mNumberOfParticles / total);
				if (prob > 0 && number == 0)
					number = 1;

				while (number > 0) {
					particles.add(Particle.evenSpread(gridSpacing * x, gridSpacing * y, gridSpacing,
							gridSpacing, HEADING_DEFLECTION, mHeadingSpread,
							mStepLength, mStepLengthSpread, area));
					number--;
				}

			}
		}
		computeCloudAverageState();
	}

	@Override
	public PositionRenderer getRenderer() {
		return new ParticlePositionRenderer(this);
	}

	/**
	 * Box Rectangle of the bounding box of all particles.
	 * 
	 * @return
	 */
	public Rectangle getBox() {
		return mBox;
	}

	/**
	 * Get the WallsModel instance for the current area in the building.
	 * 
	 * @return
	 */
	public AreaLayerModel getWallsModel() {
		return mBuilding.getCurrentArea().getWallsModel();
	}

	private void adjustStepLengthDistribution() {
		/*
		 * for (Particle particle: particles) {
		 * 
		 * }
		 */
	}

	private void adjustHeadingDistribution() {

		double stdDeviation = 0.0;
		for (Particle particle : particles) {
			stdDeviation += particle.state[2] - mCoords[2];
		}

		for (Particle particle : particles) {
			particle.state[2] = mCoords[2] + mHeadingSpread
					* NormalDistribution.inverse(Math.random());
		}
	}

	/**
	 * Compute new position of the particle cloud based on a step event.
	 * 
	 * @param hdg
	 * @param length
	 */
	public void onStep(double hdg, double length) {

		Log.d(TAG, "onStep(hdg: " + hdg + ", length: " + length);
		HashSet<Particle> living = new HashSet<Particle>(particles.size());
		for (Particle particle : particles) {
			boolean particleDied = updateParticle(particle, hdg, 1.0);
			if (!particleDied) {
				living.add(particle);
			}
		}
		particles = living;
		Log.d(TAG, "no. particles = " + particles.size());

		computeCloudAverageState();

		// adjustStepLengthDistribution();
		// adjustHeadingDistribution();
	}

	/**
	 * Compute new position of the particle cloud based on a step event.
	 * 
	 * @param alpha
	 * @param hdg
	 * @param hdgSpread
	 * @param length
	 * @param lengthSpread
	 */
	public void onStep(double alpha, double hdg, double hdgSpread,
			double length, double lengthSpread) {

		Log.d(TAG, "onStep(hdg: " + hdg + ", length: " + length
				+ ", hdgSpread: " + hdgSpread + ", lengthSpread: "
				+ lengthSpread);
		HashSet<Particle> living = new HashSet<Particle>(particles.size());
		for (Particle particle : particles) {
			if (Math.random() > alpha) {
				living.add(particle);
				continue;
			}
			double lengthFactor = 1.0 + lengthSpread * NormalDistribution.inverse(Math.random()) / length;
			boolean particleDied = updateParticle(particle, hdg + hdgSpread
					* NormalDistribution.inverse(Math.random()), lengthFactor);
			if (!particleDied) {
				living.add(particle);
			}
		}
		particles = living;

		if (particles.size() < 0.75 * mNumberOfParticles) {
			refill();
		}

		Log.d(TAG, "no. particles = " + particles.size());

		computeCloudAverageState();

	}

	/**
	 * Update state of a single particle
	 * 
	 * @param particle
	 *            updated particle
	 * @param hdg
	 *            heading in which the particle moves
	 * @param lengthModifier
	 *            distance of the particle travel
	 * @return true if the particle was eliminated during the update step
	 */
	private boolean updateParticle(Particle particle, double hdg,
			double lengthModifier) {

		// Log.d(TAG, "updateParticle(): hdg = " + hdg + ", length = " +
		// length);
		double[] state = particle.getState();
		double deltaX = (lengthModifier * state[3] * Math.sin(hdg + state[2]));
		double deltaY = (lengthModifier * state[3] * Math.cos(hdg + state[2]));
		Line2D trajectory = new Line2D(state[0], state[1], state[0] + deltaX,
				state[1] + deltaY);

		if (mBuilding != null) {

			// step collision
			if (mCheckStairsCollisions) {
				Point2D intersection = new Point2D(0.0, 0.0);
				Collection<Line2D> stairs = mBuilding.getCurrentArea()
						.getStairsLayer().getWorkingSet(state[0], state[1]);
				for (Line2D step : stairs) {
					if (trajectory.intersect(step, intersection)) {
						deltaX = 1.01 * (intersection.getX() - state[0]);
						deltaY = 1.01 * (intersection.getY() - state[1]);
						break;
					}
				}
			}

			// transition edge collision
			if (mCheckTransitionEdgeCollisions) {
				for (Line2D edge : mBuilding.getCurrentArea()
						.getTransitionEdgeLayer()
						.getWorkingSet(state[0], state[1])) {
					if (trajectory.intersect(edge)) {
						// TODO: move the particle to respective area
					}
				}
			}

			// wall collision
			if (mCheckWallsCollisions) {
				for (Line2D wall : mBuilding.getWallsModel().getWorkingSet(
						state[0], state[1])) {
					if (trajectory.intersect(wall)) {
						return true;
					}
				}
			}
		}

		state[0] += deltaX;
		state[1] += deltaY;

		return false;
	}

	/**
	 * 
	 * @param particle
	 * @return
	 */
	public double getSurvivalProbability(Particle particle) {

		return mWifiProbabilityMap.getProbability(particle.state[0], particle.state[1]);

	}

	/**
	 * Update particle cloud based on a new RSS measurement.
	 */
	public void onRssMeasurementUpdate() {

		Log.d(TAG, "onRssMeasurement()");
		HashSet<Particle> living = new HashSet<Particle>(particles.size());

		if (particles.isEmpty()) {
			Log.d(TAG,
					"onRssMeasurementUpdate: no particles, resetting position from probability map");
			// distribute particles
			setPositionBasedOnProbabilityMap(mWifiProbabilityMap,
					mBuilding.getCurrentAreaIndex());

		} else {

			for (Particle particle : particles) {
				boolean particleDied = Math.random() > getSurvivalProbability(particle);
				if (!particleDied) {
					living.add(particle);
				}
			}
			particles = living;
		}


		if (particles.size() < 0.75 * mNumberOfParticles) {
			refill();
		}

		
		Log.d(TAG, "no. particles = " + particles.size());

		computeCloudAverageState();
	}

	/**
	 * Computes average state values of the particle cloud.
	 */
	private void computeCloudAverageState() {

		mCloudAverageState[0] = mCloudAverageState[1] = mCloudAverageState[2] = mCloudAverageState[3] = 0.0;
		double[] max = new double[] { Double.NEGATIVE_INFINITY,
				Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY,
				Double.NEGATIVE_INFINITY };
		double[] min = new double[] { Double.POSITIVE_INFINITY,
				Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY,
				Double.POSITIVE_INFINITY };
		for (Particle particle : particles) {
			for (int i = 0; i < particle.getState().length; i++) {
				mCloudAverageState[i] += particle.getState()[i];
				min[i] = Math.min(min[i], particle.getState()[i]);
				max[i] = Math.max(max[i], particle.getState()[i]);
			}
		}
		for (int i = 0; i < 4; i++) {
			mCloudAverageState[i] /= particles.size();
		}

		Log.d(TAG,
				"computeCloudAverageState: "
						+ Arrays.toString(mCloudAverageState));

		mBox = new Rectangle(min[0] - 2 * DEFAULT_STEP_LENGTH, min[1] - 2
				* DEFAULT_STEP_LENGTH, max[0] + 2 * DEFAULT_STEP_LENGTH, max[1]
				+ 2 * DEFAULT_STEP_LENGTH);

		long timestamp = System.nanoTime();
		if (mBuilding != null) {
			mBuilding.getCurrentArea().setBoundingBox(mBox);
		}
		Log.d(TAG, "computeCloudAverageState: mBox = " + mBox);
		Log.d(TAG, "setBoundingBox took " + (System.nanoTime() - timestamp)
				/ NANO);
		Log.d(TAG, "working set has "
				+ mBuilding.getWallsModel().getWorkingSet().size() + " walls");

	}

	/**
	 * Recreates the particles eliminated by the constraint filter to the full
	 * count.
	 */
	private void refill() {

		int count = mNumberOfParticles - particles.size();
		Set<Particle> refillSet = new HashSet<Particle>();
		for (Particle particle : particles) {
			if (count > 0) {
				refillSet.add(particle.copy());
				count--;
			} else {
				break;
			}
		}
		particles.addAll(refillSet);
	}

	/**
	 * Retrieve the collection of particles.
	 * 
	 * @return the collection of particles.
	 */
	public Collection<Particle> getParticles() {
		return particles;
	}

	/*
	 * public float getWidth() { return mWidth; }
	 * 
	 * public float getHeight() { return mHeight; }
	 */

	@Override
	public String toString() {
		return "particle(count: " + particles.size() + ", hdg:"
				+ String.format("%.2f", 180 * mHeading / Math.PI) + ", x:"
				+ String.format("%.2f", mCoords[0]) + ", y:"
				+ String.format("%.2f", mCoords[1]) + ", hdg:"
				+ String.format("%.2f", mCoords[2]) + ", step:"
				+ String.format("%.2f", mCoords[3]);
	}

	@Override
	public void setPositionProvider(MotionProvider provider) {
		if (mProvider != null) {
			mProvider.unregister(this);
		}
		this.mProvider = provider;
		mProvider.register(this);
	}

	private float mLastNotifiedHeading;
	private static final float HEADING_DIFF_THRESHOLD = (float) (5f / 180f * Math.PI);

	@Override
	public void positionChanged(float heading, float x, float y) {
		mHeading = heading;

		if (x * x + y * y != 0.0) {
			long timestamp = System.nanoTime();
			onStep(mStepProbability, heading, HEADING_SIGMA,
					DEFAULT_STEP_LENGTH, DEFAULT_STEP_LENGTH_SPREAD);
			Log.d(TAG, "onStep took " + (System.nanoTime() - timestamp) / NANO);
			if (mPositionListener != null)
				mPositionListener.updatePosition(this);
		} else {
			float delta = Math.abs(mHeading - mLastNotifiedHeading);
			if (delta > HEADING_DIFF_THRESHOLD && mPositionListener != null) {
				mLastNotifiedHeading = mHeading;
				mPositionListener.updateHeading(this);
			}
		}
	}

	@Override
	public void probabilityMapChanged(ProbabilityMap map) {

		mWifiProbabilityMap = map;
		onRssMeasurementUpdate();
	}

	@Override
	public void wifiPositionChanged(float heading, float x, float y) {
		// do nothing
	}

	public ProbabilityMap getProbabilityMap() {
		return mWifiProbabilityMap;
	}

	public double[] getCoordinates() {
		mCoords[0] = mCloudAverageState[0];
		mCoords[1] = mCloudAverageState[1];
		mCoords[2] = mCloudAverageState[2];
		mCoords[3] = mCloudAverageState[3];
		return mCoords;
	}
	
	public double getPrecision() {
		double sdX = 0.0;
		double sdY = 0.0;
		for (Particle particle: particles) {
			sdX += MathUtils.sqr(particle.state[0] - mCoords[0]);
			sdY += MathUtils.sqr(particle.state[1] - mCoords[1]);
		}
		sdX = Math.sqrt(sdX / particles.size());
		sdY = Math.sqrt(sdY / particles.size());
		return Math.max(sdX, sdY);
	}

	public float getHeading() {
		return mHeading;
	}

	@Override
	public void floorChanged(int floor) {
		Log.d(TAG, "Swiching floor to: " + floor);

	}

	@Override
	public void updatePreferences(SharedPreferences prefs) {

		if (prefs == null) {
			mNumberOfParticles = DEFAULT_PARTICLE_COUNT;
			mStepProbability = DEFAULT_ALPHA;
			mStepLength = DEFAULT_STEP_LENGTH;
			mStepLengthSpread = DEFAULT_STEP_LENGTH_SPREAD;
			mHeadingSpread = DEFAULT_HEADING_SPREAD;
			mCheckWallsCollisions = true;
			mCheckStairsCollisions = true;
			mCheckTransitionEdgeCollisions = true;
		} else {

			try {
			mNumberOfParticles = Integer.valueOf(prefs.getString(
					"particle_filter_number_of_particles_preference",
					String.valueOf(DEFAULT_PARTICLE_COUNT)));
			} catch (NumberFormatException e) {
				e.printStackTrace();
				mNumberOfParticles = DEFAULT_PARTICLE_COUNT;
			}

			try {
			mStepProbability = Float.valueOf(prefs.getString(
					"step_probability_preference",
					String.valueOf(DEFAULT_ALPHA)));
			} catch (NumberFormatException e) {
				e.printStackTrace();
				mStepProbability = DEFAULT_ALPHA;
			}

			try {
			mStepLength = Float.valueOf(prefs.getString(
					"step_length_preference",
					String.valueOf(DEFAULT_STEP_LENGTH)));
			} catch (NumberFormatException e) {
				e.printStackTrace();
				mStepLength = DEFAULT_STEP_LENGTH;
			}

			try {
			mStepLengthSpread = Float.valueOf(prefs.getString(
					"step_length_spread_preference",
					String.valueOf(DEFAULT_STEP_LENGTH_SPREAD)));
			} catch (NumberFormatException e) {
				e.printStackTrace();
				mStepLengthSpread = DEFAULT_STEP_LENGTH_SPREAD;
			}

			try {
			mHeadingSpread = Float.valueOf(prefs.getString(
					"heading_spread_preference",
					String.valueOf(DEFAULT_HEADING_SPREAD)));
			} catch (NumberFormatException e) {
				e.printStackTrace();
				mHeadingSpread = DEFAULT_HEADING_SPREAD;
			}
			
			mCheckWallsCollisions = prefs.getBoolean(
					"check_walls_collisions_preference", true);

			mCheckStairsCollisions = prefs.getBoolean(
					"check_stairs_collisions_preference", true);

			mCheckTransitionEdgeCollisions = prefs.getBoolean(
					"check_transition_edge_collisions_preference", true);
		}

	}

	@Override
	public double getX() {
		return getCoordinates()[0];
	}

	@Override
	public double getY() {
		return getCoordinates()[1];
	}

}
