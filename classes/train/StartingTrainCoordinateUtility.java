package classes.train;

import classes.helper.Coordinate;

public class StartingTrainCoordinateUtility {
	public static Coordinate getStart(String takeOffStationName, String destination){
		if(takeOffStationName.equals("Station A") && destination.equals("Station B"))
			return new Coordinate(27, 2);
		else if(takeOffStationName.equals("Station B") && destination.equals("Station A"))
			return new Coordinate(6, 6);
		else if(takeOffStationName.equals("Station B") && destination.equals("Station C"))
			return new Coordinate(6, 7);
		else if(takeOffStationName.equals("Station C") && destination.equals("Station B"))
			return new Coordinate(12, 19);
		else if(takeOffStationName.equals("Station C") && destination.equals("Station D"))
			return new Coordinate(12, 20);
		else if(takeOffStationName.equals("Station D") && destination.equals("Station C"))
			return new Coordinate(1, 26);
		else if(takeOffStationName.equals("Station C") && destination.equals("Station E"))
			return new Coordinate(13, 20);
		return new Coordinate(25, 26); //Station E prema C - jedini preostali slucaj
	}
}
