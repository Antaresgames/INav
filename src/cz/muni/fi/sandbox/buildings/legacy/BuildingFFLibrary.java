package cz.muni.fi.sandbox.buildings.legacy;

import cz.muni.fi.sandbox.buildings.AreaBuilder;
import cz.muni.fi.sandbox.buildings.Building;

public class BuildingFFLibrary extends Building {

	private static final long serialVersionUID = 4294474621858455762L;

	public BuildingFFLibrary() {

		float scale = 1.0f;

		float originX = 598868.2919039582f;
		float originY = 1160053.6946345752f;

		addArea(0,
				"Basement",
				AreaBuilder.builder()
						.readSimpleTextWalls("indoor-plans/fflib/walls/FF_P01.txt")
						.scale(originX, originY, scale, scale).create());

		addArea(1,
				"Ground Floor",
				AreaBuilder.builder()
						.readSimpleTextWalls("indoor-plans/fflib/walls/FF_N01.txt")
						.scale(originX, originY, scale, scale).create());

		addArea(2,
				"Floor 2",
				AreaBuilder.builder()
						.readSimpleTextWalls("indoor-plans/fflib/walls/FF_N02.txt")
						.scale(originX, originY, scale, scale).create());

		addArea(3,
				"Floor 3",
				AreaBuilder.builder()
						.readSimpleTextWalls("indoor-plans/fflib/walls/FF_N03.txt")
						.scale(originX, originY, scale, scale).create());

		addArea(4,
				"Floor 4",
				AreaBuilder.builder()
						.readSimpleTextWalls("indoor-plans/fflib/walls/FF_N04.txt")
						.scale(originX, originY, scale, scale).create());

	}
}
