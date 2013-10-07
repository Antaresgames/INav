package cz.muni.fi.sandbox.toys;

import thirdparty.delaunay.DelaunayView;
import android.app.Activity;
import android.os.Bundle;

public class DelaunayTriangulationDemo extends Activity {
	
	private static final String TAG = "DelaunayTriangulationDemo";

	private DelaunayView mView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// Be sure to call the super class.
		super.onCreate(savedInstanceState);

		mView = new DelaunayView(this);
		setContentView(mView);
		
	}

	@Override
	protected void onResume() {
		super.onResume();


	}

	@Override
	protected void onStop() {
		super.onStop();
	}
}
