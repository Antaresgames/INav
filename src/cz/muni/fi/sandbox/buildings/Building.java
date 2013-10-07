package cz.muni.fi.sandbox.buildings;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import android.util.Log;
import cz.muni.fi.sandbox.buildings.legacy.BuildingBotanicka68aDwg;
import cz.muni.fi.sandbox.buildings.legacy.BuildingFFLibrary;
import cz.muni.fi.sandbox.buildings.legacy.BuildingFlat;
import cz.muni.fi.sandbox.buildings.legacy.BuildingFss;
import cz.muni.fi.sandbox.buildings.legacy.BuildingMediahall;
import cz.muni.fi.sandbox.buildings.legacy.BuildingNo1;

public class Building implements Serializable {
	
	private static final long serialVersionUID = -7550882452628582380L;

	private static final String TAG = "Building";
	
	protected HashMap<Integer, Area> areas;
	protected HashMap<Integer, String> floorNames;
	protected int mCurrentArea;
	
	public Building() {
		mCurrentArea = 0;
		floorNames = new HashMap<Integer, String>();
		areas = new HashMap<Integer, Area>();
		
		floorNames.put(mCurrentArea, "empty");
		areas.put(mCurrentArea, new Area());
	}
	
	/*
	public static Building factory(String buildingName) {
		return factory(buildingName, "indoor-plans");
	}
	*/
	
	public static Building factory(String buildingName, String path) {
		Log.d(TAG, "building factory: "+buildingName+", "+path);
		Building retval = null;
		retval = Building.fromXml(path +"/"+buildingName+".xml");
		if (retval == null)
			retval = new EmptyBuilding();
		
		retval.optimize();
		return retval;
	}
	
	
	
	
	public static Building fromXml(String fileName) {
		Log.d(TAG, "fromXml: "+fileName);
		return new BuildingXmlReader().read(fileName);
	}
	
	
	public static Building fromXmlWithCaching(String filename, String cacheFileName) {
		File cache = new File(cacheFileName);
		Building building = tryToLoadFromCache(cache);
		if (building != null) {
			return building;
		} else {
			return loadFromXmlAndCache(filename, cache);
		}
		
	}
	
	private static Building tryToLoadFromCache(File cache) {
		Building retval = null;
		try {
			if (cache.exists()) {
				long timestamp = System.nanoTime();
				Log.d(TAG, "started loading building from cache");
				retval = Building.fromCache(cache);
				Log.d(TAG, "building loaded from cache in "
						+ (double)(System.nanoTime() - timestamp) / Math.pow(10, 9));
			}
		} catch(IOException e) {
			Log.e(TAG, "failed to load building from cache");
			e.printStackTrace();
		} catch(ClassNotFoundException e) {
			Log.e(TAG, "failed to load building from cache");
			e.printStackTrace();
		}
		return retval;
	}
	
	private static Building loadFromXmlAndCache(String name, File cache) {
		Building retval = Building.fromXml(name);
		retval.areaChanged(1);
		retval.optimize();
		try {
			Building.saveToCache(retval, cache);
		} catch (IOException e) {
			Log.e(TAG, "failed to save building to cache");
			e.printStackTrace();
		}
		return retval;
	}
	
	
	public static Building fromCache(File file) throws IOException, ClassNotFoundException {
		Building building = null;
		ObjectInputStream in = null;
        try {
            in = new ObjectInputStream(new
                    BufferedInputStream(new FileInputStream(file)));
 
            building = (Building) in.readObject();
            
 
        } finally {
            in.close();
        }
		return building;
	}
	
	public static void saveToCache(Building building, File file) throws IOException {
		Log.d(TAG, "saveToCache: file = " + file.getName());
	
		ObjectOutputStream out = null;
		try {
			out = new ObjectOutputStream(new BufferedOutputStream(
					new FileOutputStream(file)));
			out.writeObject(building);
		} finally {
			out.close();
		}
	}
	
	
	public void addArea(int floorIndex, String name, Area area) {
		floorNames.put(floorIndex, name);
		areas.put(floorIndex, area);
	}

	public void setCurrentArea(int areaIndex) {
		mCurrentArea = areaIndex;
	}
	
	public Area getCurrentArea() {
		return areas.get(getCurrentAreaIndex());
	}
	
	
	public AreaLayerModel getWallsModel() {
		return areas.get(getCurrentAreaIndex()).getWallsModel();
	}
	
	public CharSequence[] getFloorLabels() {
		
		ArrayList<Integer> keys = new ArrayList<Integer>();
		keys.addAll(floorNames.keySet());
		Collections.sort(keys);
		CharSequence[] retval = new String[floorNames.size()];
		int index = 0;
		for(Integer key: keys) {
			retval[index] = floorNames.get(key);
			index++;
		}
		Log.d(TAG, "getFloorLabels: retval = " + Arrays.toString(retval));
		return retval; 
	}
	
	public int getFloorIndex(int orderedIndex) {
		
		ArrayList<Integer> keys = new ArrayList<Integer>();
		keys.addAll(floorNames.keySet());
		Collections.sort(keys);
		return keys.get(orderedIndex);
	}
	
	public void areaChanged(int newAreaIndex) {
		Log.d(TAG, "floorChanged: newAreaIndex = " + newAreaIndex);
		mCurrentArea = newAreaIndex;
	}
	
	public int getCurrentAreaIndex() {
		return mCurrentArea;
	}
	
	
	public void optimize() {
		for (Area area: areas.values()) {
			area.optimize();
		}
	}
	
	public void optimize(int grid) {
		for (Area area: areas.values()) {
			area.optimize(grid);
		}
	}
}
