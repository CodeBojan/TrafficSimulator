package classes.map;
import classes.helper.Coordinate;
import classes.simulation.FilePaths;
import classes.station.Station;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapInitializer {
	public static void initializeStations(HashMap<String, Station> stations) {
		Station sA = new Station("Station A"); 
		Station sB = new Station("Station B");
		Station sC = new Station("Station C");
		Station sD = new Station("Station D");
		Station sE = new Station("Station E");
		stations.put("Station A", sA);
		stations.put("Station B", sB);
		stations.put("Station C", sC);
		stations.put("Station D", sD);
		stations.put("Station E", sE);
	}
	
	private static Double processSpeedLimits(String line){
		String[] splitted = line.split(";");
		return Double.parseDouble(splitted[1]);
	}
	
	private static List<Coordinate> processRoadwayLines(String line){
		String[] splitted = line.split(";");
		List<Coordinate> result = new ArrayList<>();
		Coordinate coordinate;
		for(int i = 1; i < splitted.length; i++){
			coordinate = parseCoordinate(splitted[i]);
			result.add(coordinate);
		}
		return result;
	}
	
	public static void initializeRoadways(HashMap<String, Roadway> roadways) throws IOException{
		Roadway rALeft = null;
		Roadway rARight = null;
		Roadway rBLeft = null;
		Roadway rBRight = null;
		Roadway rCLeft = null;
		Roadway rCRight = null;
		List<String> roadwayInfo = Files.readAllLines(FilePaths.getRoadwaysFile().toPath());
		List<String> speedInfo = Files.readAllLines(FilePaths.getMainConfigurationFile().toPath());
		List<Coordinate> coordinates;
		Double speed = 0.0;
		int counter = 0;
		for(int i = 0; i < roadwayInfo.size(); i++){
			coordinates = processRoadwayLines(roadwayInfo.get(i));
			if(i % 2 == 0) {
				speed = processSpeedLimits(speedInfo.get(counter));
				counter++;
			}
			switch(i){
				case 0:
					rALeft = new Roadway("rALeft", speed, coordinates);
					break;
				case 1:
					rARight = new Roadway("rARight", speed, coordinates);
					break;
				case 2:
					rBLeft = new Roadway("rBLeft", speed, coordinates);
					break;
				case 3:
					rBRight = new Roadway("rBRight", speed, coordinates);
					break;
				case 4:
					rCLeft = new Roadway("rCLeft", speed, coordinates);
					break;
				case 5:
					rCRight = new Roadway("rCRight", speed, coordinates);
					break;
			}
		}
		roadways.put("rALeft", rALeft);
		roadways.put("rARight", rARight);
		roadways.put("rBLeft", rBLeft);
		roadways.put("rBRight", rBRight);
		roadways.put("rCLeft", rCLeft);
		roadways.put("rCRight", rCRight);
	}
	
	private static Coordinate parseCoordinate(String coordinate) {
		String[] splitted = coordinate.split("-");
		return new Coordinate(Integer.parseInt(splitted[0]), Integer.parseInt(splitted[1]));
	}
	
	public static void initializeHighways(HashMap<String, Highway> highways, HashMap<String, Roadway> roadways) {
		Highway hA = new Highway(roadways.get("rALeft"), roadways.get("rARight"));
		Highway hB = new Highway(roadways.get("rBLeft"), roadways.get("rBRight"));
		Highway hC = new Highway(roadways.get("rCLeft"), roadways.get("rCRight"));
		highways.put("hA", hA);
		highways.put("hB", hB);
		highways.put("hC", hC);
	}	
}
