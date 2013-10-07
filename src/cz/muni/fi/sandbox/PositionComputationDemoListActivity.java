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
import cz.muni.fi.sandbox.service.grid.StochasticGridActivity;
import cz.muni.fi.sandbox.service.particle.ParticleActivity;

public class PositionComputationDemoListActivity extends DemoListActivity {

	@Override
	protected void constructList() {
		intents = new IntentPair[] {

				new IntentPair("Discrete Stochastic Model", new Intent(this,
						StochasticGridActivity.class)),
				new IntentPair("Particle Filter Model", new Intent(this,
						ParticleActivity.class)),

		};
	}
}

