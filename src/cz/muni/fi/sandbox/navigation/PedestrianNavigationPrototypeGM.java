package cz.muni.fi.sandbox.navigation;
/*
import java.io.File;
import java.util.Collections;
import java.util.Map;

import thirdparty.delaunay.Point;
import thirdparty.delaunay.Triangle;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;
import cz.muni.fi.sandbox.Preferences;
import cz.muni.fi.sandbox.R;
import cz.muni.fi.sandbox.buildings.Building;
import cz.muni.fi.sandbox.service.PedestrianLocation;
import cz.muni.fi.sandbox.service.PedestrianLocationListener;
import cz.muni.fi.sandbox.service.PedestrianLocationManager;
import cz.muni.fi.sandbox.service.wifi.BuildingFingerprintAdaptor;
import cz.muni.fi.sandbox.service.wifi.InterpolatedFingerprintModel;
import cz.muni.fi.sandbox.service.wifi.RssFingerprint;
import cz.muni.fi.sandbox.service.wifi.RssFingerprintController;
import cz.muni.fi.sandbox.service.wifi.RssFingerprintControllerStateChangeListener;
import cz.muni.fi.sandbox.service.wifi.WifiLayerModel;
import cz.muni.fi.sandbox.service.wifi.RssFingerprintController.State;
import cz.muni.fi.sandbox.utils.AndroidFileUtils;
import cz.muni.fi.sandbox.utils.ColorRamping;
import cz.muni.fi.sandbox.utils.geographical.SjtskLocation;
import cz.muni.fi.sandbox.utils.geographical.Wgs84Location;
import cz.muni.fi.sandbox.utils.geometric.Line2D;
import cz.muni.fi.sandbox.utils.geometric.Point2D; */


public class PedestrianNavigationPrototypeGM {}/*extends SherlockFragmentActivity implements PedestrianLocationListener, LocationAssignmentAuthority {
	
	private final String TAG = "PedestrianNavigationPrototype2";
	protected SharedPreferences mSharedPrefs;
	protected PedestrianLocationManager mLocationManager;

	protected PositionModel mPosition;
	protected Building mBuilding;
	protected RssFingerprintController mWifiModel;
	
	private SupportMapFragment mapFragment;
	
    private Runnable mStopMeasurementRunnable;
    
	private class StopMeasurementRunnable implements Runnable {
		private final int mTick; // in milliseconds
		private int mCount; // in ticks
		private final String mTitle;
		private final MenuItem mItem;
		private final RssFingerprintController mController;

		public StopMeasurementRunnable(RssFingerprintController controller, MenuItem item, String title, int count, int tick) {
			mController = controller;
			mTick = tick;
			mCount = count;
			mTitle = title;
			mItem = item;
		}
		
		public void run() {
			
			if (mCount > 0) {
				if (mItem != null)
					mItem.setTitle(mTitle + " (" + mCount + ")");
				mCount--;
				
				// call itself again after one tick
	            mHandler.postDelayed(this, mTick);
			} else {
				mItem.setTitle(mTitle);
				mController.measure();	
			}
		}
	} */

	/**
	 * 
	 *//*
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate new text");
		
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_pedestrian_navigation);
		
		mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

		mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

		
		createTask = new AsyncTask<Void, Void, Void>(){

			ProgressDialog dialog = new ProgressDialog(PedestrianNavigationPrototypeGM.this);
			
	        @Override
	        protected void onPreExecute() {
	            // what to do before background task
	            dialog.setTitle("Loading...");
	            dialog.setMessage("Please wait.");
	            dialog.setIndeterminate(true);
	            dialog.setCancelable(false);
	            dialog.show();
	        }
			
			@Override
			protected Void doInBackground(Void... params) {
				mLocationManager = new PedestrianLocationManager(PedestrianNavigationPrototypeGM.this);
				mPosition = mLocationManager.getPositionModel();
				mLocationManager.requestLocationUpdates("", 0, 0, PedestrianNavigationPrototypeGM.this);
				mWifiModel = mLocationManager.getWifiModel();
				
				mWifiModel
				.setStateChangeListener(new RssFingerprintControllerStateChangeListener() {
					@Override
					public void stateChange(State mState) {
						if (mMenu != null) {
							updateMeasurement(mMenu.findItem(R.id.measurement));
						}
						
						if (mWifiModel.getState() == RssFingerprintController.State.IDLE) {
							if (mStopMeasurementRunnable != null) {
								mHandler.removeCallbacks(mStopMeasurementRunnable);
								mStopMeasurementRunnable = null;
							}
						} else if (mWifiModel.getState() == RssFingerprintController.State.MEASURING) {
							if (mStopMeasurementRunnable == null) {
								mStopMeasurementRunnable = new StopMeasurementRunnable(mWifiModel, mMenu.findItem(R.id.measurement), "Stop measuring", 20, 1000);
								mStopMeasurementRunnable.run();
							}
						} else if (mWifiModel.getState() == RssFingerprintController.State.FINDING) {
							
						}
					}
				});
		 mWifiModel.setLocationAssignment(PedestrianNavigationPrototypeGM.this);
				
	    		mLocationManager.recachePreferences(mSharedPrefs);
				return null;
			}
			
	        @Override
	        protected void onPostExecute(Void result) {
	        	mWifiModel.onResume();
	        	mLocationManager.start();
	        	System.out.println("Location manager start");
	            dialog.dismiss();
	        };

	        @Override
	        protected void onCancelled() {
	            dialog.dismiss();
	            super.onCancelled();
	        }
			
		};
		
		createTask.execute(new Void[]{});

	}
	AsyncTask<Void, Void, Void> createTask;

	@Override
	protected void onResume() {
		Log.d(TAG, "onResume");
		super.onResume();
		if (mWifiModel!=null) {
			mWifiModel.onResume();
		}
//		mNaviView.recachePreferences();
		if (mLocationManager!=null && createTask.getStatus() == Status.FINISHED) {
			mLocationManager.recachePreferences(mSharedPrefs);
			mLocationManager.start();
			System.out.println("Location manager start");
		}

	}

	@Override
	protected void onPause() {
		Log.d(TAG, "onPause");
		super.onPause();
		if (mWifiModel!=null) {
		mWifiModel.onPause();
		}
		
		if (mLocationManager!=null && createTask.getStatus() == Status.FINISHED) {
			mLocationManager.stop();
			System.out.println("Location manager stop");
		}

	}

	@Override
	protected void onStop() {
		Log.d(TAG, "onStop");
		super.onStop();

		// setBuilding(null);

	}
	
	private GroundOverlay positionOverlay = null;

	private void setBuilding(Building building) {
		mBuilding = building;
		mPosition.setBuilding(building);
		
		BuildingFingerprintAdaptor fingerprintsAdaptor = new BuildingFingerprintAdaptor();
		fingerprintsAdaptor.setBuilding(mBuilding);
		mWifiModel.setFingerprintAdaptor(fingerprintsAdaptor);
		mWifiModel.setFingerprintWriter(mBuilding.getCurrentArea().getRssFingerprintWriter());
		
		fillMap();		
	}
	
	private void fillMap() {
		
		final LatLngBounds.Builder boundsBuilder = LatLngBounds.builder();
		
		for (Line2D line : mBuilding.getCurrentArea().getWallsModel().getCompleteSet()) {
			
			double[] p1 = getLatLng(-line.getY1()+mBuilding.getCurrentArea().originY, -line.getX1()+mBuilding.getCurrentArea().originX);
			double[] p2 = getLatLng(-line.getY2()+mBuilding.getCurrentArea().originY, -line.getX2()+mBuilding.getCurrentArea().originX);
			
			boundsBuilder.include(new LatLng(p1[0], p1[1]));
			boundsBuilder.include(new LatLng(p2[0], p2[1]));
			
			final PolylineOptions lOpt = new PolylineOptions();
			lOpt.add(new LatLng(p1[0], p1[1]));
			lOpt.add(new LatLng(p2[0], p2[1]));
			lOpt.width(2.0f);
			lOpt.color(Color.BLACK);
			
			
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mapFragment.getMap().addPolyline(lOpt);
				}
			});
			
		}
		
		for (Line2D line : mBuilding.getCurrentArea().getStairsLayer().getCompleteSet()) {
			
			double[] p1 = getLatLng(-line.getY1()+mBuilding.getCurrentArea().originY, -line.getX1()+mBuilding.getCurrentArea().originX);
			double[] p2 = getLatLng(-line.getY2()+mBuilding.getCurrentArea().originY, -line.getX2()+mBuilding.getCurrentArea().originX);
			
			boundsBuilder.include(new LatLng(p1[0], p1[1]));
			boundsBuilder.include(new LatLng(p2[0], p2[1]));
			
			final PolylineOptions lOpt = new PolylineOptions();
			lOpt.add(new LatLng(p1[0], p1[1]));
			lOpt.add(new LatLng(p2[0], p2[1]));
			lOpt.color(Color.BLUE);
			lOpt.width(1.0f);
			
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mapFragment.getMap().addPolyline(lOpt);
				}
			});
		}
		
		Map<Point2D, String> poisMap = mBuilding.getCurrentArea().getPointsModel().getCompleteSet();
		for (Point2D poi : poisMap.keySet()) {
			final String title = poisMap.get(poi);
			final double[] latLng = getLatLng(-poi.getY()+mBuilding.getCurrentArea().originY, -poi.getX()+mBuilding.getCurrentArea().originX);
			
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					mapFragment.getMap().addMarker(
							new MarkerOptions().title(title)
							.position(new LatLng(latLng[0], latLng[1]))
						);
				}
			});
		}
		
		fillAccessPoints();
		
		fillDelaunay();
		
		final double[] latLng = getLatLng(-mPosition.getY()+mBuilding.getCurrentArea().originY, -mPosition.getX()+mBuilding.getCurrentArea().originX);
		
		if (positionOverlay!=null) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					positionOverlay.remove();
					positionOverlay = mapFragment.getMap().addGroundOverlay(
							new GroundOverlayOptions()
							.image(BitmapDescriptorFactory.fromResource(R.drawable.ic_nav))
							.position(new LatLng(latLng[0], latLng[1]), 2.0f)
							.zIndex(100.0f)
							);
				}
			});
		} else {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					positionOverlay = mapFragment.getMap().addGroundOverlay(
							new GroundOverlayOptions()
							.image(BitmapDescriptorFactory.fromResource(R.drawable.ic_nav))
							.position(new LatLng(latLng[0], latLng[1]), 2.0f)
							.zIndex(100.0f)
							);
				}
			});
			
		}
		
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mapFragment.getMap().moveCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 50));
			}
		});
	}
	
	private static final double DEFAULT_GRID_SPACING = 2.0;
	private String bssid = "maximum";
	
	private void fillAccessPoints() {
		
		if (mBuilding == null)
			return;
		
		double gridSpacing = DEFAULT_GRID_SPACING;
		
		WifiLayerModel fingerprints = mBuilding.getCurrentArea().getRssFingerprints();
		
		if (fingerprints != null) {
			gridSpacing = fingerprints.getGridSpacing();
		}

//		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
//		paint.setStyle(Paint.Style.FILL);

		if (fingerprints != null) {
			for (RssFingerprint fingerprint : fingerprints.getFingerprints()) {

				boolean max = bssid.equals("maximum");

				if ((max && !fingerprint.getVector().isEmpty())
						|| fingerprint.getVector().containsKey(bssid)) {

					Point2D loc = fingerprint.getLocation();
					
					double x = -loc.getX()+mBuilding.getCurrentArea().originX;
					double y = -loc.getY()+mBuilding.getCurrentArea().originY;
					
					System.out.println("FINGERPRINT LOCATION: "+x+", "+y);

					double rss = 0.0;
					if (max) {
						// Log.d(TAG,
						// "fingerprint.getVector().values().isEmpty == " +
						// fingerprint.getVector().values().isEmpty());
						rss = Collections.max(fingerprint.getVector().values())
								.getRss();
					} else {
						rss = fingerprint.getVector().get(bssid).getRss();
					}

					Log.d(TAG, "rss = " + rss + ", loc = " + loc);
					double temperatureScale = 1 + rss / 100.0;
//					paint.setColor(ColorRamping.blueToRedRamp(temperatureScale));
					float left = (float) (x - gridSpacing / 2);
					float top = (float) (x - gridSpacing / 2);
					float right = (float) (x + gridSpacing / 2);
					float bottom = (float) (x + gridSpacing / 2);
					
					final double[] topLeft = getLatLng((y - gridSpacing / 2),(x - gridSpacing / 2));
					double[] topRight = getLatLng((y - gridSpacing / 2), (x + gridSpacing / 2));
					double[] bottomLeft = getLatLng( (y + gridSpacing / 2), (x - gridSpacing / 2));
					double[] bottomRight = getLatLng((y + gridSpacing / 2), (x + gridSpacing / 2));

					final PolygonOptions polygon = new PolygonOptions();
					polygon.add(new LatLng(topLeft[0],topLeft[1]));
					polygon.add(new LatLng(topRight[0],topRight[1]));
					polygon.add(new LatLng(bottomRight[0],bottomRight[1]));
					polygon.add(new LatLng(bottomLeft[0],bottomLeft[1]));
					polygon.strokeColor(ColorRamping.blueToRedRamp(temperatureScale));
					polygon.fillColor(ColorRamping.blueToRedRamp(temperatureScale));
					polygon.strokeWidth(2);
					
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							mapFragment.getMap().addPolygon(polygon);
						}
					});
//					canvas.drawRect(scaleX * left, -scaleY * bottom, scaleX
//							* right, -scaleY * top, paint);
//
//					if (scaleX >= 20) {
//						paint.setColor(Color.BLACK);
//						canvas.drawText(Integer.toString((int) rss),
//								(float) (scaleX * loc.getX()),
//								(float) (-scaleY * loc.getY()), paint);
//					}
				}

			}
		}

	}
	
	private void fillDelaunay() {
		if (mBuilding == null)
			return;
		WifiLayerModel fingerprints = mBuilding.getCurrentArea().getRssFingerprints();
		
		if (fingerprints != null
				&& fingerprints instanceof InterpolatedFingerprintModel) {
			for (Triangle triangle : ((InterpolatedFingerprintModel)fingerprints).getTriangulation()) {
				Log.d(TAG, "drawDelaunay: triangle");
				Point[] vertices = triangle.toArray(new Point[0]);
				final PolygonOptions polygon = new PolygonOptions();
				final double[] latLngA = getLatLng(
						-vertices[0].coord(1)+mBuilding.getCurrentArea().originY, 
						-vertices[0].coord(0)+mBuilding.getCurrentArea().originX
						);
				LatLng a = new LatLng(latLngA[0], latLngA[1]);
				
				final double[] latLngB = getLatLng(
						-vertices[1].coord(1)+mBuilding.getCurrentArea().originY, 
						-vertices[1].coord(0)+mBuilding.getCurrentArea().originX
						);
				LatLng b = new LatLng(latLngB[0], latLngB[1]);
				
				final double[] latLngC = getLatLng(
						-vertices[2].coord(1)+mBuilding.getCurrentArea().originY, 
						-vertices[2].coord(0)+mBuilding.getCurrentArea().originX
						);
				LatLng c = new LatLng(latLngC[0], latLngC[1]);
				
				polygon.add(a);
				polygon.add(b);
				polygon.add(c);
				
				polygon.strokeColor(Color.BLACK);
				polygon.strokeWidth(2);
				
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						mapFragment.getMap().addPolygon(polygon);
					}
				});
			}
		}
	}

	
	 private Menu mMenu;
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.menu, menu);

		 mMenu = menu;

		updatePauseMenuItem(menu.findItem(R.id.pause));
		 updateMeasurement(menu.findItem(R.id.measurement));

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		
		case R.id.scan_code:
			startScanActivity();
			return true;
		
		case R.id.measurement:
			mWifiModel.measure();
			return true;
			
		case R.id.settings:
			showSettings();
			return true;

		case R.id.select_floor:
			levelSelect();
			return true;

		case R.id.pause:
			pause();
			updatePauseMenuItem(item);
			return true;

		case R.id.reset_compass:
			resetCompass();
			return true;

		case R.id.set_position:
			setPosition();
			return true;

		case R.id.help:
			showHelp();
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void updateMeasurement(MenuItem measurementItem) {

		if (mWifiModel == null) return;
		
		if (mWifiModel.getState() == RssFingerprintController.State.IDLE) {
			measurementItem.setEnabled(true);
			measurementItem.setTitle("Add fingerprint");
		} else if (mWifiModel.getState() == RssFingerprintController.State.MEASURING) {
			measurementItem.setTitle("Stop measuring");
		} else if (mWifiModel.getState() == RssFingerprintController.State.FINDING) {
			measurementItem.setEnabled(false);
		}
	}
	
	public static final int PEDESTRIAN_NAVIGATION_SCAN_BARCODE = 0;
	public static final String SCAN_RESULT = "SCAN_RESULT";
	private void startScanActivity() {
//		Intent intent = new Intent(PedestrianNavigationPrototype2.this, ScanCodeActivity.class);
//		startActivityForResult(intent, PEDESTRIAN_NAVIGATION_SCAN_BARCODE);
		
		Intent intent = new Intent("com.google.zxing.client.android.SCAN");
		startActivityForResult(intent, PEDESTRIAN_NAVIGATION_SCAN_BARCODE);
	}

	private void showSettings() {
		Log.d(TAG, "showSettings()");
		startActivity(new Intent(this, Preferences.class));
	}

	private static final int LEVEL_SELECT = 45;

	private void levelSelect() {
		Log.d(TAG, "levelSelect()");
		showDialog(LEVEL_SELECT);
	}

	@Override
	protected Dialog onCreateDialog(int id) {

		switch (id) {
		case LEVEL_SELECT:
			return new AlertDialog.Builder(PedestrianNavigationPrototypeGM.this)
					.setTitle("Select level")
					.setOnCancelListener(new OnCancelListener() {
						@Override
						public void onCancel(DialogInterface dialog) {
							PedestrianNavigationPrototypeGM.this
									.removeDialog(LEVEL_SELECT);
						}
					})
					.setItems(mBuilding.getFloorLabels(),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									mBuilding.areaChanged(mBuilding
											.getFloorIndex(which));
									mWifiModel.setFingerprintWriter(mBuilding.getCurrentArea().getRssFingerprintWriter());
//									mNaviView.cropWalls();
//									mNaviView.invalidateBackground();
//									mNaviView.invalidate();
									PedestrianNavigationPrototypeGM.this
											.removeDialog(LEVEL_SELECT);
									
									AsyncTask<Void, Void, Void> fillTask = new AsyncTask<Void, Void, Void>(){

										ProgressDialog dialog = new ProgressDialog(PedestrianNavigationPrototypeGM.this);
										
								        @Override
								        protected void onPreExecute() {
								            // what to do before background task
								            dialog.setTitle("Preparing map...");
								            dialog.setMessage("Please wait.");
								            dialog.setIndeterminate(true);
								            dialog.setCancelable(false);
								            dialog.show();
								            
								            mapFragment.getMap().clear();
								        }
										
										@Override
										protected Void doInBackground(Void... params) {
											fillMap();
											return null;
										}
										
								        @Override
								        protected void onPostExecute(Void result) {
								            dialog.dismiss();
								        };

								        @Override
								        protected void onCancelled() {
								            dialog.dismiss();
								            super.onCancelled();
								        }
										
									};
									
									fillTask.execute(new Void[]{});
									
								}
							}).create();
		}
		Log.e(TAG, "Unsupported dialog id");
		return null;
	}

	private boolean mPaused = false;

	private void pause() {
		Log.d(TAG, "pause()");
		mPaused = !mPaused;
		mLocationManager.setPaused(mPaused);

	}

	private void resetCompass() {
		Log.d(TAG, "resetCompass()");
		mLocationManager.resetCompass();

	}

	private void setPosition() {
		Log.d(TAG, "setPosition()");
		pause();
//		mNaviView.requestSetPosition(new PositionModelSetPositionListener(
//				mPosition));
		mapFragment.getMap().setOnMapClickListener(new OnMapClickListener() {
			
			@Override
			public void onMapClick(LatLng point) {
				float x,y;
				SjtskLocation loc = SjtskLocation.convert(
						new Wgs84Location(point.latitude, point.longitude)
						);
				x = (float)(-(loc.getX()+mBuilding.getCurrentArea().originX));
				y = (float)(-(loc.getY()+mBuilding.getCurrentArea().originY));
				
				x = -x;
				y = -y;
				
				System.out.println("MAP CLICK "+(x)+", "+(y));
				mPosition.setPosition(x, y, mBuilding.getCurrentAreaIndex());
				mapFragment.getMap().setOnMapClickListener(null);
				pause();
				mLocationManager.updatePosition(mPosition);
			}
		});
	}

	private void updatePauseMenuItem(MenuItem pauseItem) {
		if (!mPaused) {
			pauseItem.setTitle(R.string.pause);
		} else {
			pauseItem.setTitle(R.string.resume);
		}
	}

	ProgressDialog dialog;
	private Handler mHandler = new Handler();

	private class SetNewBuildingRunnable implements Runnable {
		Building building;

		SetNewBuildingRunnable(Building building) {
			this.building = building;
		}

		public void run() {
			setBuilding(building);
		}
	}

	public void postSetBuilding(Building building) {
		mHandler.post(new SetNewBuildingRunnable(building));
	}

	private void showHelp() {
		Log.d(TAG, "showHelp method");

		File readmeFile = AndroidFileUtils.getFileFromPath(mSharedPrefs
				.getString("data_root_dir_preference", "")
				+ "readme.txt");
		Intent i = new Intent();
		i.setAction(android.content.Intent.ACTION_VIEW);
		i.setDataAndType(Uri.fromFile(readmeFile), "text/plain");
		startActivity(i);

	}
	
	

	@Override
	public void onLocationChanged(PedestrianLocation location) {
		float head = (float) (180.0f * location.getPosition().getHeading() / Math.PI);
		positionOverlay.setBearing(head);
		double[] latLng = getLatLng(-location.getPosition().getY()+mBuilding.getCurrentArea().originY, -location.getPosition().getX()+mBuilding.getCurrentArea().originX);
		positionOverlay.setPosition(
				new LatLng(latLng[0], latLng[1])
				);
		System.out.println("loc.changed: "+(location.getPosition().getX()+mBuilding.getCurrentArea().originX)+", "+(location.getPosition().getY()+mBuilding.getCurrentArea().originY));
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
//		mNaviView.invalidate();
		mLocationManager.updatePosition(mPosition);
	}

	@Override
	public void onBuildingChanged(Building building) {
		Log.d(TAG, "onBuildingChanged");
		postSetBuilding(building);
	}

	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
			case PEDESTRIAN_NAVIGATION_SCAN_BARCODE:
				String code = data.getStringExtra(SCAN_RESULT);
				Toast.makeText(PedestrianNavigationPrototypeGM.this, "Scanned code: "+code, Toast.LENGTH_LONG).show();
				
				Point2D point = mBuilding.getCurrentArea().getBarcodesModel().getPoint(code);
				
				if (point!=null) {
					mPosition.setPosition((float)(point.getX()),(float)(point.getY()), mBuilding.getCurrentAreaIndex());
//					mLocationManager.updatePosition(mPosition);
					
					System.out.println("desired position: "+(point.getX())+", "+(point.getY()));
					System.out.println("desired position: "+(point.getX()+mBuilding.getCurrentArea().originX)+", "+(point.getY()+mBuilding.getCurrentArea().originY));
					
				} else {
					Toast.makeText(PedestrianNavigationPrototypeGM.this, "Barcode not recognized", Toast.LENGTH_SHORT).show();
				}
				
				break;

			default:
				break;
			}
		}
	}
	
	private double[] getLatLng(double x, double y) {
		double[] latLng = new double[2];
//		
		double H = 0; */
	       /*Vypocet zemepisnych souradnic z rovinnych souradnic*/ /*
        double a=6377397.15508; 
        double e=0.081696831215303;
        double n=0.97992470462083; 
        double konst_u_ro=12310230.12797036;
        double sinUQ=0.863499969506341; 
        double cosUQ=0.504348889819882;
        double sinVQ=0.420215144586493; 
        double cosVQ=0.907424504992097;
        double alfa=1.000597498371542; 
        double k=1.003419163966575;
        double ro=Math.sqrt(x*x+y*y);
        double epsilon=2*Math.atan(y/(ro+x));
        double D=epsilon/n; 
        double S=2*Math.atan(Math.exp(1/n*Math.log(konst_u_ro/ro)))-(Math.PI/2);
        double sinS=Math.sin(S);
        double cosS=Math.cos(S);
        double sinU=sinUQ*sinS-cosUQ*cosS*Math.cos(D);
        double cosU=Math.sqrt(1-sinU*sinU);
        double sinDV=Math.sin(D)*cosS/cosU; 
        double cosDV=Math.sqrt(1-sinDV*sinDV);
        double sinV=sinVQ*cosDV-cosVQ*sinDV; 
        double cosV=cosVQ*cosDV+sinVQ*sinDV;
        double Ljtsk=2*Math.atan(sinV/(1+cosV))/alfa;
        double t=Math.exp(2/alfa*Math.log((1+sinU)/cosU/k));
        double pom=(t-1)/(t+1);
        double sinB;
        do {
            sinB=pom;
            pom=t*Math.exp(e*Math.log((1+e*sinB)/(1-e*sinB))); 
            pom=(pom-1)/(pom+1);
        } 
        while (Math.abs(pom-sinB)>0.000000000000001);
        double Bjtsk=Math.atan(pom/Math.sqrt(1-pom*pom)); */
    
        /* Pravoúhlé souøadnice ve S-JTSK */ /*
        double f_1=299.152812853;
        double e2=1-(1-1/f_1)*(1-1/f_1); 
        ro=a/Math.sqrt(1-e2*Math.sin(Bjtsk)*Math.sin(Bjtsk));
        x=(ro+H)*Math.cos(Bjtsk)*Math.cos(Ljtsk);  
        y=(ro+H)*Math.cos(Bjtsk)*Math.sin(Ljtsk);  
        double z=((1-e2)*ro+H)*Math.sin(Bjtsk); */
        
        /* Pravoúhlé souøadnice v WGS-84*/ /*
        double dx=570.69; 
        double dy=85.69; 
        double dz=462.84; 
        double wz=-5.2611/3600*Math.PI/180;
        double wy=-1.58676/3600*Math.PI/180;
        double wx=-4.99821/3600*Math.PI/180; 
        double m=3.543*Math.pow(10,-6); 
        double xn=dx+(1+m)*(x+wz*y-wy*z); 
        double yn=dy+(1+m)*(-wz*x+y+wx*z); 
        double zn=dz+(1+m)*(wy*x-wx*y+z); */
       
        /* Geodetické souøadnice v systému WGS-84*/ /*
        a=6378137.0; 
        f_1=298.257223563;
        double a_b=f_1/(f_1-1); 
        double p=Math.sqrt(xn*xn+yn*yn); 
        e2=1-(1-1/f_1)*(1-1/f_1);
        double theta=Math.atan(zn*a_b/p); 
        double st=Math.sin(theta); 
        double ct=Math.cos(theta);
        t=(zn+e2*a_b*a*st*st*st)/(p-e2*a*ct*ct*ct);
        double B=Math.atan(t);  
        double L=2*Math.atan(yn/(p+xn));
        
        latLng[0] = B/Math.PI*180;
        latLng[1] = L/Math.PI*180;
        return latLng;
	}

	@Override
	public void requestSetPosition(final SetPositionListener spl) {
		
		mapFragment.getMap().setOnMapClickListener(new OnMapClickListener() {
			
			@Override
			public void onMapClick(LatLng point) {
				float x,y;
				SjtskLocation loc = SjtskLocation.convert(
						new Wgs84Location(point.latitude, point.longitude)
						);
				
				x = (float)(-(loc.getX()+mBuilding.getCurrentArea().originX));
				y = (float)(-(loc.getY()+mBuilding.getCurrentArea().originY));
				
				x = -x;
				y = -y;
				
				System.out.println("MAP CLICK "+(x)+", "+(y));
				mapFragment.getMap().setOnMapClickListener(null);
				
				spl.setPosition(x, y, mBuilding.getCurrentAreaIndex());
			}
		});
		
	}
} */
