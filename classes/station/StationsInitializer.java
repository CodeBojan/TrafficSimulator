package classes.station;
import classes.helper.Coordinate;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;

public class StationsInitializer{
	public static void initialize(String name, List<Coordinate> coordinates, HashMap<String, Line> thisStationRailwayLines, List<String> neighbours, HashMap<String, Line> lines){
		switch(name){
			case "Station A":
				coordinates = Arrays.asList(new Coordinate(27, 1), new Coordinate(27, 2), new Coordinate(28, 1), new Coordinate(28, 2));
				thisStationRailwayLines.put("Station A-Station B", lines.get("Station A-Station B"));
				neighbours.add("Station B");
				break;
			case "Station B":
				coordinates = Arrays.asList(new Coordinate(5, 6), new Coordinate(5, 7), new Coordinate(6, 6), new Coordinate(6, 7));
				neighbours.add("Station A");
				neighbours.add("Station C");
				thisStationRailwayLines.put("Station A-Station B", lines.get("Station A-Station B"));
				thisStationRailwayLines.put("Station B-Station C", lines.get("Station B-Station C"));
				break;
			case "Station C":
				coordinates = Arrays.asList(new Coordinate(12, 19), new Coordinate(12, 20), new Coordinate(13, 19), new Coordinate(13, 20));
				neighbours.add("Station B");
				neighbours.add("Station D");
				neighbours.add("Station E");
				thisStationRailwayLines.put("Station B-Station C", lines.get("Station B-Station C"));
				thisStationRailwayLines.put("Station C-Station D", lines.get("Station C-Station D"));
				thisStationRailwayLines.put("Station C-Station E", lines.get("Station C-Station E"));
				break;
			case "Station D":
				coordinates = Arrays.asList(new Coordinate(1, 26), new Coordinate(1, 27), new Coordinate(2, 26), new Coordinate(2, 27));
				neighbours.add("Station C");
				thisStationRailwayLines.put("Station C-Station D", lines.get("Station C-Station D"));
				break;
			case "Station E":
				coordinates = Arrays.asList(new Coordinate(25, 25), new Coordinate(25, 26), new Coordinate(26, 25), new Coordinate(26, 26));
				neighbours.add("Station C");
				thisStationRailwayLines.put("Station C-Station E", lines.get("Station C-Station E"));
				break;
		}
	}
}