package cz.muni.fi.sandbox.buildings.legacy;

import cz.muni.fi.sandbox.buildings.AreaBuilder;
import cz.muni.fi.sandbox.buildings.Building;

public class BuildingFlat extends Building {

	private static final long serialVersionUID = 6426603378734284189L;

	public BuildingFlat() {

		float scale = 2 * 5.82f / 198.0f;
		float originX = -340f;
		float originY = -136f;

		addArea(0,
				"Floor",
				AreaBuilder.builder()
						.readDiaExportedSvgArea("indoor/plans/flat/walls/flat.svg")
						.scale(originX, originY, scale, -scale)
						.readRssFingerprints("indoor/plans/flat/rss/rss_fingerprints.txt")
						.create());
	}
}
