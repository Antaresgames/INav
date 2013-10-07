package cz.muni.fi.sandbox.buildings.legacy;

import cz.muni.fi.sandbox.buildings.AreaBuilder;
import cz.muni.fi.sandbox.buildings.Building;

public class BuildingBotanicka68a extends Building {

	private static final long serialVersionUID = -6926639533321076963L;

	public BuildingBotanicka68a() {

		float scaleB = 0.95f / 17.4f;
		float scale0 = 1.4740f / 24.07f;
		float scale1 = 0.6560f / 8.16f;
		float scale2 = 0.95f / 23.89f;
		float scale3 = 1.076f / 21.43f;
		float scale4 = 1.076f / 17.4f;

		float originX = -4000f;
		float originY = -3000f;

		addArea(0,
				"Basement",
				AreaBuilder.builder()
						.readConvertedSvgArea("indoor-plans/P01-Model.svg")
						.scale(originX, originY, scaleB, -scaleB).create());

		addArea(1,
				"Ground Floor",
				AreaBuilder.builder()
						.readConvertedSvgArea("indoor-plans/N01-Model.svg")
						.scale(originX, originY, scale0, -scale0).create());

		addArea(2,
				"Floor 2",
				AreaBuilder
						.builder()
						.readConvertedSvgArea("indoor-plans/N02-Model.svg")
						.scale(originX + 94.7f / 20.0f / scale1,
								originY - 379.2f / 20.0f / scale1, scale1,
								-scale1).create());

		addArea(3,
				"Floor 3",
				AreaBuilder
						.builder()
						.readConvertedSvgArea("indoor-plans/N03-Model.svg")
						.scale(originX + (144 + 94.7f) / 20.0f / scale2,
								originY + (250.4f - 379.2f) / 20.0f / scale2,
								scale2, -scale2).create());

		addArea(4,
				"Floor 4",
				AreaBuilder.builder()
						.readConvertedSvgArea("indoor-plans/N04-Model.svg")
						.scale(originX, originY, scale3, -scale3).create());

		addArea(5,
				"Floor 5",
				AreaBuilder.builder()
						.readConvertedSvgArea("indoor-plans/N05-Model.svg")
						.scale(originX, originY, scale4, -scale4).create());

	}
}
