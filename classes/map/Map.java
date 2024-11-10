package classes.map;
import java.util.HashMap;
import classes.station.*;
import classes.helper.*;
import classes.simulation.FilePaths;
import classes.train.TrainVehicle;
import classes.vehicle.Vehicle;
import classes.vehicle.RoadVehicle;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.io.File;
import java.util.Collection;
import java.util.logging.*;

public class Map{
	public static final int LENGTH = 30;
	public static final int WIDTH = 30;
	private Field[][] mapField;
	private HashMap<String, Station> stations;
	private HashMap<String, Roadway> roadways;
	private HashMap<String, CrossingRamp> crossingRamps;
	private HashMap<String, Highway> highways;
	public static Handler handler;
	
	static{
		try {
			handler = new FileHandler(FilePaths.getLoggingFolder() + File.separator + "map.log");
			Logger.getLogger(Map.class.getName()).addHandler(handler);
			Logger.getLogger(Map.class.getName()).setUseParentHandlers(false);
		}
		catch(IOException exception) {
			exception.printStackTrace();
		}
	}
	
	public Field getField(Coordinate other){
		return mapField[other.row][other.column];
	}
	
	public void unpauseCrossingRamps() {
		Collection<CrossingRamp> tempCrossings = crossingRamps.values();
		for(CrossingRamp temp : tempCrossings) {
			temp.unpause();
			synchronized(temp){
				temp.notify();
			}
		}
	}
	
	public void pauseCrossingRamps() {
		Collection<CrossingRamp> tempCrossings = crossingRamps.values();
		for(CrossingRamp temp : tempCrossings)
			temp.pause();
	}
	
	public void pauseStations() {
		Collection<Station> tempStations = stations.values();
		for(Station temp : tempStations)
			temp.pause();
	}
	
	public void unpauseStations() {
		Collection<Station> tempStations = stations.values();
		for(Station temp : tempStations) {
			temp.unpause();
			synchronized(temp) {
				temp.notify();
			}
		}
	}
											
	public void setVehicle(Vehicle vehicle, Coordinate setCoordinate){ //mora biti synchornized zbog Crossing scana, ali ne zakljucavam celu mapu zbog jednog polja
		if(vehicle != null){
			Field field = getField(setCoordinate);
			if(vehicle instanceof TrainVehicle){
				TrainVehicle temp = (TrainVehicle)vehicle;
				if(field instanceof Track) {
					Track track = (Track)field;
					synchronized(track) {
						if(track.vehicle == null)
							track.vehicle = temp;
					}
					
				}
				else if(getField(setCoordinate) instanceof Crossing) {
					Crossing crossing = (Crossing)field;
					synchronized(crossing) {
						if(crossing.vehicle == null)
						crossing.vehicle = temp;
					}
				}
			}
			else if(vehicle instanceof RoadVehicle){
				RoadVehicle temp = (RoadVehicle)vehicle;
				if(field instanceof Road) {
					Road road = (Road)field;
					synchronized(road) {
						if(road.vehicle == null)
						road.vehicle = temp;
					}
					
				}
				else if(field instanceof Crossing) {
					Crossing crossing = (Crossing)field;
					synchronized(crossing) {
						if(crossing.vehicle == null)
							crossing.vehicle = temp;
					}
				}
			}
		}
	}
	
	public void removeTrainVehicle(Coordinate removeCoordinate) {
		Field field;
		if((field = getField(removeCoordinate)) instanceof Track) {
			Track track = (Track)field;
			synchronized(track) {
				track.vehicle = null;
			}
		}
		else if((field = getField(removeCoordinate)) instanceof Crossing) {
			Crossing crossing = (Crossing)field; //??
			synchronized(crossing) {
				crossing.vehicle = null;
			}
		}
		
	}
	
	public synchronized void removeRoadVehicle(Coordinate removeCoordinate) {
		Field field;
		if((field = getField(removeCoordinate)) instanceof Road) {
			Road road = (Road)field;
			synchronized(road) {
				road.vehicle = null;
			}
		}
		else if((field = getField(removeCoordinate)) instanceof Crossing) {
			Crossing crossing = (Crossing)field; //??
			synchronized(crossing) {
				crossing.vehicle = null;
			}
		}
	}
	
	
	
	public void startCrossings() {
		Collection<CrossingRamp> toStart = crossingRamps.values();
		for(CrossingRamp temp : toStart)
			temp.start();
	}
	
	public Highway getHighway(String highwayName) {
		return highways.get(highwayName);
	}
	
	public Map(){
			mapField = new Field[30][30];
		try{
			roadways = new HashMap<>();
			crossingRamps = new HashMap<>();
			stations = new HashMap<>();
			highways = new HashMap<>();
			MapInitializer.initializeRoadways(roadways);
			MapInitializer.initializeStations(stations);
			MapInitializer.initializeHighways(highways, roadways);
			List<String> lines = Files.readAllLines(FilePaths.getMapFile().toPath());
			int i = 0, j = 0;
			String[][] matrix = new String[30][30];
			CrossingRamp cBC = new CrossingRamp("cBC");
			CrossingRamp cAB = new CrossingRamp("cAB");
			CrossingRamp cCE = new CrossingRamp("cCE");
			for(String line : lines)
				matrix[i++] = line.split(";");
			for(i = 0; i < LENGTH; i++)
				for(j = 0; j < WIDTH; j++) {
					if(matrix[i][j].equals("T"))
						mapField[i][j] = new Track();
					else if(matrix[i][j].equals("R"))
						mapField[i][j] = new Road(new Coordinate(i, j));
					else if(matrix[i][j].equals("SA"))
						mapField[i][j] = stations.get("Station A");
					else if(matrix[i][j].equals("SB"))
						mapField[i][j] = stations.get("Station B");
					else if(matrix[i][j].equals("SC"))
						mapField[i][j] = stations.get("Station C");
					else if(matrix[i][j].equals("SD"))
						mapField[i][j] = stations.get("Station D");
					else if(matrix[i][j].equals("SE"))
						mapField[i][j] = stations.get("Station E");
					else if(matrix[i][j].equals("C")) {
						Crossing newCrossing = new Crossing(new Coordinate(i, j));
						mapField[i][j] = newCrossing;
						if(newCrossing.position.equals(new Coordinate(6, 13)) || newCrossing.position.equals(new Coordinate(6, 14)))
							cBC.addCrossing(newCrossing);
						else if(newCrossing.position.equals(new Coordinate(21, 2)) || newCrossing.position.equals(new Coordinate(20, 2)))
							cAB.addCrossing(newCrossing);
						else if(newCrossing.position.equals(new Coordinate(21, 26)) || newCrossing.position.equals(new Coordinate(20, 26)))
							cCE.addCrossing(newCrossing);
					}
							
				}
			crossingRamps.put("cBC", cBC);
			crossingRamps.put("cAB", cAB);
			crossingRamps.put("cCE", cCE);
		}           
		catch(IOException exception){
			exception.printStackTrace();
			Logger.getLogger(Map.class.getName()).log(Level.WARNING, exception.fillInStackTrace().toString());
			System.exit(-1);
		}
	}
	
	public void print(){
		synchronized(System.out) {
		for(int i = 0; i < LENGTH; i++, System.out.println())
			for(int j = 0; j < WIDTH; j++){
				if(mapField[i][j] == null)
					System.out.print("0");
				if(mapField[i][j] instanceof Station)
					System.out.print("S");
				if(mapField[i][j] instanceof Crossing) {
					Field temp = mapField[i][j];
					Crossing crossing = (Crossing)temp;
					if(crossing.vehicle != null) {
						if(crossing.vehicle instanceof TrainVehicle)
							System.out.print("*");
						else
							System.out.print("#");
					}
					else
						System.out.print("C");
				}
				if(mapField[i][j] instanceof Road) {
					Field temp = mapField[i][j];
					Road road = (Road)temp;
					if(road.vehicle != null)
						System.out.print("#");
					else
						System.out.print("-");
				}
				if(mapField[i][j] instanceof Track) {
					Field temp = mapField[i][j];
					Track track = (Track)temp;
					if(track.vehicle != null)
						System.out.print("*");
					else
						System.out.print("T");
				}
			}
		}
	}
	
	public void startAllStations() {
		Collection<Station> tempCollection = stations.values();
		for(Station temp : tempCollection)
			temp.start();
	}
	
	public CrossingRamp getCrossingRamp(Coordinate coordinate) {
		Collection<CrossingRamp> collection = crossingRamps.values();
		for(CrossingRamp temp : collection)
			if(temp.containsCoordinate(coordinate))
				return temp;
		return null;
	}
	
	public Coordinate locateCrossingRampOnRoadway(Roadway roadway) { //ovom metodom lociram na osnovu pozicije automobila gde se nalazi ispred njega crossing
		for(Coordinate potentialLocation : roadway.roadList) {
			Field field = getField(potentialLocation);
			if(field instanceof Crossing)
					return potentialLocation;
		}
		return null;
	}
	
	public CrossingRamp getCrossing(String name) {
		return crossingRamps.get(name);
	}
	
	public Station getStation(String name){
		return stations.get(name);
	}
	
	public Roadway getRoadway(String name) {
		return roadways.get(name);
	}
	
	public Collection<Station> getAllStations(){
		return stations.values();
	}
}
