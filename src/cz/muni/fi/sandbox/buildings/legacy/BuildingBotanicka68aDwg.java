package cz.muni.fi.sandbox.buildings.legacy;

import cz.muni.fi.sandbox.buildings.AreaBuilder;
import cz.muni.fi.sandbox.buildings.Building;

/**
 * Class BuildingBotanicka68bDwg represents the building of the faculty of
 * informatics of the Masaryk University in Brno on Botanicka 68b.
 * 
 * @author Michal Holcik
 * 
 */
public class BuildingBotanicka68aDwg extends Building {

	private static final long serialVersionUID = -5002949429345933664L;

	public BuildingBotanicka68aDwg() {

		float scale = 1.0f;
		float originX = 598621.6863073101f;
		float originY = 1159062.0162205047f;

		addArea(0,
				"Basement",
				AreaBuilder
						.builder()
						.readSimpleTextWalls("indoor-plans/fi/walls/FI_P01.txt")
						.scale(originX, originY, scale, scale)
						.readRssFingerprints(
								"indoor-plans/fi/rss/rss_FI_P01.txt").create());

		addArea(1,
				"Ground Floor",
				AreaBuilder
						.builder()
						.readSimpleTextWalls("indoor-plans/fi/walls/FI_N01.txt")
						.scale(originX, originY, scale, scale)
						.readRssFingerprints(
								"indoor-plans/fi/rss/rss_FI_N01.txt").create());

		addArea(2,
				"Floor 2",
				AreaBuilder
						.builder()
						.readSimpleTextWalls("indoor-plans/fi/walls/FI_N02.txt")
						.scale(originX, originY, scale, scale)
						.readRssFingerprints(
								"indoor-plans/fi/rss/rss_FI_N02.txt").create());

		addArea(3,
				"Floor 3",
				AreaBuilder
						.builder()
						.readSimpleTextWalls("indoor-plans/fi/walls/FI_N03.txt")
						.scale(originX, originY, scale, scale)
						.readRssFingerprints(
								"indoor-plans/fi/rss/rss_FI_N03.txt").create());

		addArea(4,
				"Floor 4",
				AreaBuilder
						.builder()
						.readSimpleTextWalls("indoor-plans/fi/walls/FI_N04.txt")
						.scale(originX, originY, scale, scale)
						.readRssFingerprints(
								"indoor-plans/fi/rss/rss_FI_N04.txt").create());

		addArea(5,
				"Floor 5",
				AreaBuilder
						.builder()
						.readSimpleTextWalls("indoor-plans/fi/walls/FI_N05.txt")
						.scale(originX, originY, scale, scale)
						.readRssFingerprints(
								"indoor-plans/fi/rss/rss_FI_N05.txt").create());

	}
}
