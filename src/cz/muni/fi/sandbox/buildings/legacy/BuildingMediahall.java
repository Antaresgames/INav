package cz.muni.fi.sandbox.buildings.legacy;

import cz.muni.fi.sandbox.buildings.AreaBuilder;
import cz.muni.fi.sandbox.buildings.Building;

public class BuildingMediahall extends Building {

	private static final long serialVersionUID = -8880343290723861240L;

	public BuildingMediahall(String path) {

		float scale = 2 * 15.5f / 585f;
		float wallOriginX = -1643f;
		float wallOriginY = -630f;

		addArea(0, "Ground Floor", AreaBuilder.builder()
				.readDiaExportedSvgArea(path + "/mediahall/walls/mediahall_n00.svg")
				.readRssFingerprints(path + "/mediahall/rss/rss_N00.txt")
				.scale(wallOriginX, wallOriginY, scale, -scale).create());
		
		addArea(1,
				"1st Floor",
				AreaBuilder
						.builder()
						.readDiaExportedSvgArea(path + "/mediahall/walls/mediahall_n01.svg")
						.readRssFingerprints(path + "/mediahall/rss/rss_N01.txt")
						.scale(wallOriginX, wallOriginY, scale, -scale)
						.create());
		addArea(2,
				"2nd Floor",
				AreaBuilder
						.builder()
						.readDiaExportedSvgArea(path + "/mediahall/walls/mediahall_n02.svg")
						.readRssFingerprints(path + "/mediahall/rss/rss_N02.txt")
						.scale(wallOriginX, wallOriginY, scale, -scale)
						.create());

	}

}
