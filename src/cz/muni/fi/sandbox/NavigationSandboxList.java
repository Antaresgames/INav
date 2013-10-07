/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cz.muni.fi.sandbox;

import android.content.Intent;
import cz.muni.fi.sandbox.navigation.PedestrianNavigationPrototype;
import cz.muni.fi.sandbox.navigation.PedestrianNavigationPrototypeGM;
import cz.muni.fi.sandbox.navigation.PrecisionEvaluationActivity;

public class NavigationSandboxList extends DemoListActivity {

	@Override
	public void constructList() {

		intents = new IntentPair[] {

				new IntentPair("Pedestrian Navigation", new Intent(this,
						PedestrianNavigationPrototype.class)),
						
				new IntentPair("Pedestrian Navigation with Google Maps", new Intent(this,
						PedestrianNavigationPrototypeGM.class)),
				
				new IntentPair("Precision Evaluation Activity", new Intent(this,
						PrecisionEvaluationActivity.class)),
						
				new IntentPair("Device Orientation & Motion", new Intent(this,
						DeviceOrientationAndMotionDemoListActivity.class)),

				new IntentPair("Radio Positioning Methods", new Intent(this,
						RadioPositioningDemoListActivity.class)),

				new IntentPair("Position Computation Methods", new Intent(this,
						PositionComputationDemoListActivity.class)),
				
				new IntentPair("Preferences", new Intent(this,
						Preferences.class)),
		};

	}

}
