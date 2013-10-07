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
import cz.muni.fi.sandbox.service.wifi.WifiLocalizationActivity;
import cz.muni.fi.sandbox.service.wifi.WifiRssScannerActivity;
import cz.muni.fi.sandbox.toys.DelaunayTriangulationDemo;
import cz.muni.fi.sandbox.toys.LocationDemo;

public class RadioPositioningDemoListActivity extends DemoListActivity {

	@Override
	protected void constructList() {
		intents = new IntentPair[] {
				
				new IntentPair("Wifi RSS Map", new Intent(this,
						WifiLocalizationActivity.class)),
				new IntentPair("Wifi RSS Scanner", new Intent(this,
						WifiRssScannerActivity.class)),
				new IntentPair("Delaunay Triangulation Demo", new Intent(this,
						DelaunayTriangulationDemo.class)),
				new IntentPair("Location Provider Demo", new Intent(this,
						LocationDemo.class)), };
	}

}
