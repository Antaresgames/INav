package cz.muni.fi.sandbox.service.particle;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import cz.muni.fi.sandbox.buildings.Building;
import cz.muni.fi.sandbox.buildings.legacy.BuildingNo2;
import cz.muni.fi.sandbox.service.motion.MotionProvider;
import cz.muni.fi.sandbox.service.particle.ParticlePosition.ParticleGenerationMode;

public class ParticleActivity extends Activity {
	
	private static final String TAG = "ParticleActivity";

	private ParticleView mView;
	private ParticlePosition mPosition;
	private MotionProvider mPositionProvider;
	private Building mBuilding;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// Be sure to call the super class.
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");
		
		mBuilding = new BuildingNo2();
		mPosition = new ParticlePosition(10, 10, 1, 0, mView, null, ParticleGenerationMode.GAUSSIAN);
		mPosition.setBuilding(mBuilding);
		
		mView = new ParticleView(this, mPosition, mBuilding);
		
		mPositionProvider = MotionProvider.providerFactory("sensor", this);
		mPositionProvider.register(mView);
		//mPosition.registerView(mView);
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
