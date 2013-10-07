package cz.muni.fi.sandbox.navigation;

import java.util.ArrayList;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import cz.muni.fi.sandbox.R;
import cz.muni.fi.sandbox.service.particle.ParticlePosition;
import cz.muni.fi.sandbox.utils.Writer;
import cz.muni.fi.sandbox.utils.geometric.Point2D;

/**
 * 
 * @author Michal Holcik
 *
 */
public class PrecisionEvaluationActivity extends PedestrianNavigationPrototype {


	private static final String TAG = "PrecisionEvaluationActivity";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate new text");

		super.onCreate(savedInstanceState);
		
	}


	@Override
	protected void onResume() {
		Log.d(TAG, "onResume");
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		Log.d(TAG, "onResume");
		super.onPause();
		
	}
	
	@Override
	protected void onStop() {
		Log.d(TAG, "onStop");
		super.onStop();
		
	}
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.eval_menu, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {

		case R.id.log_attempt:
			logAttempt();
			return true;
		
		case R.id.pick_next:
			pickNext();
			return true;

		case R.id.set_destination:
			setDestination();
			return true;
			
			
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	
	private void logAttempt() {
		Log.d(TAG, "logAttempt()");
		if (mDest == null) {
			Log.e(TAG, "mDest == null");
			return;
		}
		Point2D loc = new Point2D(((ParticlePosition)mPosition).getCoordinates()[0],
		((ParticlePosition)mPosition).getCoordinates()[1]);
		double deltax = loc.getX() - mDest.getX();
		double deltay = loc.getY() - mDest.getY();
		double distance = Math.sqrt(deltax * deltax + deltay * deltay);
		Log.d(TAG, "distance from destination = " + distance);
		
		evaluationWriter.openAppend();
		evaluationWriter.writeln(System.currentTimeMillis() +  " " + mDest.toString() + " " + distance);
		evaluationWriter.close();
	}
	
	Point2D mDest = null;
	
	private void pickNext() {
		Log.d(TAG, "pickNext()");
		// fss
		// Point2D dest = new Point2D(-40 + 70 * Math.random(), 5-60 * Math.random());
		// ff
		Point2D dest = new Point2D(-5 + 20 * Math.random(), -40 * Math.random());
		mDest = dest;
		mNaviView.setDestination(dest);
		mNaviView.invalidate();
	}
	

	private Writer destinationWriter = new Writer("destinations.txt");
	private Writer evaluationWriter = new Writer("evalutation.txt");
	
	private ArrayList<Point2D> destinationList = new ArrayList<Point2D>();
	
	private void setDestination() {
		Log.d(TAG, "setDestination()");
		
		mNaviView.requestSetPosition(new SetPositionListener() {
			
			@Override
			public void setPosition(float x, float y, int area) {
				Point2D dest = new Point2D(x, y);
				destinationList.add(dest);
				destinationWriter.openAppend();
				destinationWriter.writeln(dest.toString());
				destinationWriter.close();
				mDest = dest;
				mNaviView.setDestination(dest);
				mNaviView.invalidate();
			}
		});	
	}
}
