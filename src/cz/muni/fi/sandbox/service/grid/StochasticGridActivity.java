package cz.muni.fi.sandbox.service.grid;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import cz.muni.fi.sandbox.buildings.Building;
import cz.muni.fi.sandbox.buildings.legacy.BuildingNo1;
import cz.muni.fi.sandbox.service.motion.MotionProvider;

public class StochasticGridActivity extends Activity {
	
	private static final String TAG = "StochasticActivity";

	private StochasticGridView mView;
	private StochasticGridPosition2d mStochPosition;
	private MotionProvider mPositionProvider;
	private Building mBuilding;
	
	private static final double ALPHA = 0.8;
	private static final int SIZE = 100;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// Be sure to call the super class.
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");
		
		mBuilding = new BuildingNo1();
		
		final int STEP_PROBABILITY_SIZE = 3;
		mStochPosition = new StochasticGridPosition2d(ALPHA, SIZE, mBuilding,
				StochasticGridPosition2d.InitialProbability.POINT,
				new StepProbabilityMap(new NormalBivariateDistribution(
						new double[] { .0, .0 }, new double[] { 0.5, 0.5 }, .0),
						STEP_PROBABILITY_SIZE));
		
		mView = new StochasticGridView(this, mStochPosition, mBuilding);
		
		mPositionProvider = MotionProvider.providerFactory("sensor", this);
		mPositionProvider.register(mView);
		mStochPosition.registerView(mView);
		setContentView(mView);
		
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "onResume");
		//mPositionProvider.resume();
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.d(TAG, "onStop");
		//mPositionProvider.stop();
	}
}
