package cz.muni.fi.sandbox.service.grid;

import java.util.Collection;

import cz.muni.fi.sandbox.buildings.Building;
import cz.muni.fi.sandbox.utils.geometric.Line2D;
import cz.muni.fi.sandbox.utils.geometric.Point2D;
import cz.muni.fi.sandbox.utils.geometric.Rectangle;

public class StochasticGridPosition2d {

	public enum InitialProbability {
		UNIFORM, POINT
	};

	protected String TAG = "StochasticPosition2d";

	protected IStochasticView view;

	protected final int SIZE;
	protected final double ALPHA;
	protected final double CUTOFF_PROBABILITY = Math.pow(10, -6);

	protected double[][] probability;
	protected double[][][] tmp;
	protected int current;

	protected int STEP_PROBABILITY_WINDOW_SIZE;
	//protected double[][] mStepProbability;
	protected StepProbabilityMap mStepProbability;
	protected Building mBuilding;

	private int boundingBoxMinX, boundingBoxMaxX, boundingBoxMinY,
			boundingBoxMaxY;
	private Rectangle mComputingBox;
	private final Rectangle SCREEN_BOX;

	public StochasticGridPosition2d(double alpha, int size, Building building,
			InitialProbability initProb,
			StepProbabilityMap stepProbability) {

		SIZE = size;
		ALPHA = alpha;
		current = 0;
		tmp = new double[2][SIZE][SIZE];
		probability = tmp[current];
		mBuilding = building;
		mStepProbability = stepProbability;
		STEP_PROBABILITY_WINDOW_SIZE = stepProbability.getSize();
		SCREEN_BOX = new Rectangle(0,0, SIZE - 1, SIZE - 1);
		

		switch (initProb) {

		case UNIFORM:
			for (int i = 0; i < SIZE * SIZE; i++) {
				probability[i / SIZE][i % SIZE] = 1.0 / SIZE * SIZE;
			}
			break;
			
		default:
			probability[SIZE / 2 - 10][SIZE / 2] = 1.0;
			break;

		}

		mComputingBox = new Rectangle(0, 0, SIZE - 1, SIZE - 1);
		
		boundingBoxMinX = SIZE;
		boundingBoxMaxX = 0;
		boundingBoxMinY = SIZE;
		boundingBoxMaxY = 0;
		
		// probability cutoff
		for (int i = 0; i < SIZE; i++) {
			for (int j = 0; j < SIZE; j++) {

				if (probability[i][j] > 0.0) {
					if (boundingBoxMinX > i)
						boundingBoxMinX = i;
					if (boundingBoxMaxX < i)
						boundingBoxMaxX = i;
					if (boundingBoxMinY > j)
						boundingBoxMinY = j;
					if (boundingBoxMaxY < j)
						boundingBoxMaxY = j;
				}
			}
		}

	}

	public double getMaxProb() {
		return getMaxProb(probability);
	}

	protected double getMaxProb(double[][] probs) {
		double maxProb = 0.0;
		for (int i = 0; i < probs.length; i++) {
			for (int j = 0; j < probs[0].length; j++) {
				if (maxProb < probs[i][j])
					maxProb = probs[i][j];
			}
		}
		return maxProb;
	}

	public double getSum(double[][] probs) {
		double sum = 0.0;
		for (int i = 0; i < probs.length; i++) {
			for (int j = 0; j < probs[0].length; j++) {
				sum += probs[i][j];
			}
		}
		return sum;
	}
	
	public Rectangle getBox() {
		return new Rectangle(boundingBoxMinX, boundingBoxMinY, boundingBoxMaxX, boundingBoxMaxY);
	}
	
	public Rectangle getComputingBox() {
		return mComputingBox;
	}

	protected boolean outOfBounds(int x, int y, int size) {
		if (x < 0 || x >= size || y < 0 || y >= size)
			return true;
		return false;
	}

	protected double getStepProbability(int newX, int newY, int deltaX,
			int deltaY, Collection<Point2D> hits) {

		assert (mStepProbability.getMap().length == STEP_PROBABILITY_WINDOW_SIZE);

		// System.out.println("getStepProbability(x=" + newX + ", y=" + newY
		// + ", dx=" + deltaX + ", dy=" + deltaY);

		int originalX = newX - deltaX;
		int originalY = newY - deltaY;

		boolean stupidChange = true;
		stupidChange = !stupidChange;

		double retval = 0.0;
		int locX, locY;
		Line2D trajectory = new Line2D();

		for (int i = 0; i < STEP_PROBABILITY_WINDOW_SIZE; i++) {
			for (int j = 0; j < STEP_PROBABILITY_WINDOW_SIZE; j++) {
				locX = i - STEP_PROBABILITY_WINDOW_SIZE / 2;
				locY = j - STEP_PROBABILITY_WINDOW_SIZE / 2;
				
				// TODO: optimize: trajectory object == stress on the gc
				trajectory.setCoords(originalX + locX, originalY + locY, newX,
						newY);
				
				boolean collisionDetected = !hits.contains(new Point2D(i, j));
				collisionDetected &= detectCollision(trajectory);
				if (!outOfBounds(originalX + locX, originalY + locY, SIZE)
						&& !collisionDetected)
					retval += mStepProbability.getMap()[i][j]
							* probability[originalX + locX][originalY + locY];
			}
		}

		return retval;
	}

	protected boolean detectCollision(Line2D trajectory) {

		for (Line2D wall : mBuilding.getCurrentArea().getWallsModel().getCompleteSet()) {
			if (trajectory.intersect(wall)) {
				return true;
			}
		}
		return false;
	}

	public void onStep(double heading, double length) {

		int next = (current + 1) % 2;

		System.out.println("Stepping in hdg = " + heading);

		int deltaX = (int) Math.round(length
				* Math.sin(Math.PI * heading / 180.0));
		int deltaY = (int) Math.round(-length
				* Math.cos(Math.PI * heading / 180.0));

		
		System.out.println("dX = " + deltaX + " dY = " + deltaY);

		final int MAX_STEP_LENGTH = 5;

		mComputingBox.set(boundingBoxMinX, boundingBoxMinY, boundingBoxMaxX, boundingBoxMaxY);
		mComputingBox.enlarge(STEP_PROBABILITY_WINDOW_SIZE + MAX_STEP_LENGTH);
		mComputingBox.intersect(SCREEN_BOX);
		
		System.out.println("computing box = " + mComputingBox);
		
		mBuilding.getCurrentArea().getWallsModel().setMotionVector(deltaX, deltaY, STEP_PROBABILITY_WINDOW_SIZE);
		mBuilding.getCurrentArea().getWallsModel().setBoundingBox(mComputingBox);
		mBuilding.getCurrentArea().getWallsModel().computeCollisionSet();
		
		Collection<Point2D> points = mBuilding.getCurrentArea().getWallsModel().getCollisionSet();		
		
		for (int i = (int)mComputingBox.left; i <= (int)mComputingBox.right; i++) {
			for (int j = (int)mComputingBox.top; j <= (int)mComputingBox.bottom; j++) {
				tmp[next][i][j] = ALPHA
						* getStepProbability(i, j, deltaX, deltaY, points)
						+ (1 - ALPHA) * tmp[current][i][j];
			}
		}

		// zero edges
		for (int i = 0; i < SIZE; i++) {
			tmp[next][i][0] = 0.0;
			tmp[next][i][SIZE - 1] = 0.0;
			tmp[next][0][i] = 0.0;
			tmp[next][SIZE - 1][i] = 0.0;
		}

		boundingBoxMinX = SIZE;
		boundingBoxMaxX = 0;
		boundingBoxMinY = SIZE;
		boundingBoxMaxY = 0;

		// probability cutoff
		for (int i = 0; i < SIZE; i++) {
			for (int j = 0; j < SIZE; j++) {
				if (tmp[next][i][j] < CUTOFF_PROBABILITY) {
					tmp[next][i][j] = 0.0;
				}

				if (tmp[next][i][j] > 0.0) {
					if (boundingBoxMinX > i)
						boundingBoxMinX = i;
					if (boundingBoxMaxX < i)
						boundingBoxMaxX = i;
					if (boundingBoxMinY > j)
						boundingBoxMinY = j;
					if (boundingBoxMaxY < j)
						boundingBoxMaxY = j;
				}
			}
		}

		System.out.println("new box = " + boundingBoxMinX + ", "
				+ boundingBoxMaxX + ", " + boundingBoxMinY + ", "
				+ boundingBoxMaxY);

		double sum = getSum(tmp[next]);
		System.out.println("sum = " + sum);

		// normalize
		for (int i = 0; i < SIZE; i++) {
			for (int j = 0; j < SIZE; j++) {
				if (sum != 0)
					tmp[next][i][j] /= sum;
			}
		}

		current = next;
		probability = tmp[current];

		System.out.println("sum = " + getSum(probability));
		/*
		 * System.out.print("["); for(int i = 0; i < len; i++) {
		 * //System.out.print(probability[i]+","); System.out.format("%.03f; ",
		 * probability[i]); } System.out.print("]");
		 */

		// notify view panel
		view.positionChanged(this);
	}

	public double[][] getData() {
		return probability;
	}

	public void registerView(IStochasticView view) {
		this.view = view;
	}

}
