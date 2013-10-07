package cz.muni.fi.sandbox.buildings.legacy;

import cz.muni.fi.sandbox.buildings.AreaBuilder;
import cz.muni.fi.sandbox.buildings.Building;


/**
 * Class BuildingFss represents the building of the faculty of
 * social sciences of the Masaryk University in Brno on Jostova.
 * 
 * @author Michal Holcik
 * 
 */

public class BuildingFss extends Building {

	private static final long serialVersionUID = -3574805674789220277L;

	public BuildingFss() {

		float scale = 1.0f;
		float originX = 598541.9429818317f;
		float originY = 1160414.6582918465f;

		addArea(0,
				"Basement",
				AreaBuilder.builder()
						.readSimpleTextWalls("indoor-plans/fss/walls/FSS_P01.txt")
						.readSimpleTextStairs("indoor-plans/fss/stairs/FSS_P01.txt")
						.readRssFingerprints("indoor-plans/fss/rss/rss_FSS_P01.txt")
						.scale(originX, originY, scale, scale)
						.create());

		addArea(1,
				"Ground Floor",
				AreaBuilder.builder()
						.readSimpleTextWalls("indoor-plans/fss/walls/FSS_N01.txt")
						.readSimpleTextStairs("indoor-plans/fss/stairs/FSS_N01.txt")
						.readRssFingerprints("indoor-plans/fss/rss/rss_FSS_N01.txt")
						.scale(originX, originY, scale, scale)
						.create());

		addArea(2,
				"Floor 2",
				AreaBuilder.builder()
						.readSimpleTextWalls("indoor-plans/fss/walls/FSS_N02.txt")
						.readSimpleTextStairs("indoor-plans/fss/stairs/FSS_N02.txt")
						.readRssFingerprints("indoor-plans/fss/rss/rss_FSS_N02.txt")
						.scale(originX, originY, scale, scale)
						.create());

		addArea(3,
				"Floor 3",
				AreaBuilder.builder()
						.readSimpleTextWalls("indoor-plans/fss/walls/FSS_N03.txt")
						.readSimpleTextStairs("indoor-plans/fss/stairs/FSS_N03.txt")
						.readRssFingerprints("indoor-plans/fss/rss/rss_FSS_N03.txt")
						.scale(originX, originY, scale, scale)
						.create());

		addArea(4,
				"Floor 4",
				AreaBuilder.builder()
						.readSimpleTextWalls("indoor-plans/fss/walls/FSS_N04.txt")
						.readSimpleTextStairs("indoor-plans/fss/stairs/FSS_N04.txt")
						.readRssFingerprints("indoor-plans/fss/rss/rss_FSS_N04.txt")
						.scale(originX, originY, scale, scale)
						.create());

		addArea(5,
				"Floor 5",
				AreaBuilder.builder()
						.readSimpleTextWalls("indoor-plans/fss/walls/FSS_N05.txt")
						.readSimpleTextStairs("indoor-plans/fss/stairs/FSS_N05.txt")
						.readRssFingerprints("indoor-plans/fss/rss/rss_FSS_N05.txt")
						.scale(originX, originY, scale, scale)
						.create());

		addArea(6,
				"Floor 6",
				AreaBuilder.builder()
						.readSimpleTextWalls("indoor-plans/fss/walls/FSS_N06.txt")
						.readSimpleTextStairs("indoor-plans/fss/stairs/FSS_N06.txt")
						.readRssFingerprints("indoor-plans/fss/rss/rss_FSS_N06.txt")
						.scale(originX, originY, scale, scale)
						.create());
	}
}
