package cz.muni.fi.sandbox;

import android.content.Intent;
import cz.muni.fi.sandbox.service.orientation.CompassDeflectionDemo;
import cz.muni.fi.sandbox.service.orientation.GyroCompassDemo;
import cz.muni.fi.sandbox.service.orientation.GyroMagneticCompassDemo;
import cz.muni.fi.sandbox.service.orientation.MagneticCompassDemo;
import cz.muni.fi.sandbox.service.stepdetector.StepDetectionDemo;
import cz.muni.fi.sandbox.service.stepdetector.StepDetectionDemoGravity;
import cz.muni.fi.sandbox.service.stepdetector.StepDetectionDemoLinear;
import cz.muni.fi.sandbox.service.stepdetector.StepDetectionFFT;

public class DeviceOrientationAndMotionDemoListActivity extends
		DemoListActivity {

	@Override
	protected void constructList() {
		intents = new IntentPair[] {

				new IntentPair("Step Detection - Accelerometer", new Intent(this,
						StepDetectionDemo.class)),
						
				new IntentPair("Step Detection - Linear Acceleration", new Intent(this,
								StepDetectionDemoLinear.class)),
						
				new IntentPair("Step Detection - Gravity", new Intent(this,
						StepDetectionDemoGravity.class)),

				new IntentPair("Acceleration FFT", new Intent(this,
						StepDetectionFFT.class)),

				new IntentPair("Magnetic Compass Demo", new Intent(this,
						MagneticCompassDemo.class)),

				new IntentPair("Gyro Compass", new Intent(this,
						GyroCompassDemo.class)),

				new IntentPair("Combined Gyro&Magnetic Compass", new Intent(
						this, GyroMagneticCompassDemo.class)),

				new IntentPair("Magnetic Compass Deflection Demo", new Intent(
						this, CompassDeflectionDemo.class)),
				
		};
	}
}
