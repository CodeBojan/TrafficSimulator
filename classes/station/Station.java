package classes.station;
import classes.helper.Coordinate;
import java.util.logging.*;
import java.util.List;
import java.util.Queue;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.HashMap;
import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import classes.map.*;
import classes.simulation.FilePaths;
import classes.simulation.SerializationMethods;
import classes.train.*;
import java.util.ArrayList;
import java.util.Collection;
public class Station extends Thread implements Field{
	private static Line lineAB;
	private static Line lineBC;
	private static Line lineCE;
	private static Line lineCD;
	private List<Coordinate> coordinates;
	private String stationName;
	private static HashMap<String, Line> railwayLines = new HashMap<>(); //sve linije na mapi, pomocna struktura
	private HashMap<String, Line> thisStationRailwayLines;  //glavna struktura
	public List<String> neighbours;
	private Queue<Train> trains; //u stanici //MORACE DA SE MENJA, JER AKO JEDAN CEKA, DRUGI NE MOGU IZACI IAKO SU U RAZLICITIM PRAVCIMA
	volatile private Boolean pause;
	public static Handler handler;
	private static Logger logger;
	static{
		lineAB = new Line("Station A", "Station B");
		lineBC = new Line("Station B", "Station C");
		lineCE = new Line("Station C", "Station E");
		lineCD = new Line("Station C", "Station D");
		railwayLines.put("Station A-Station B", lineAB);
		railwayLines.put("Station B-Station C",lineBC);
		railwayLines.put("Station C-Station E", lineCE);
		railwayLines.put("Station C-Station D", lineCD);
		try {
			handler = new FileHandler(FilePaths.getLoggingFolder() + File.separator + "station.log");
			logger = Logger.getLogger(Station.class.getName());
			logger.addHandler(handler);
			logger.setUseParentHandlers(false);
		}
		catch(IOException exception) {
			exception.printStackTrace();
		}
		
	}
	
	public void pause() {
		synchronized(pause) {
			pause = true;
		}
	}
	
	public void unpause() {
		synchronized(pause) {
			pause = false;
		}
	}
	
	public String getStationName(){
		return stationName;
	}
	
	public List<Coordinate> getCoordinates(){
		return coordinates;
	}
	
	public Line getLine(String line){
		return thisStationRailwayLines.get(line);
	}
	
	public Station(String name){
		pause = false;
		neighbours = new ArrayList<>();
		thisStationRailwayLines = new HashMap<>();
		StationsInitializer.initialize(name, this.coordinates, thisStationRailwayLines, this.neighbours, railwayLines);
		trains = new ArrayDeque<>();
		this.stationName = name;
		setDaemon(true);
	}
	
	@Override
	public void run() {
		Train train;
		Collection<Line> lines = thisStationRailwayLines.values();
		do {
			for(Line line : lines) {
				boolean isLeft;
				Boolean taken = line.getTaken(stationName);
				Boolean canMoveAfter = line.getCanMoveAfter(stationName);
				synchronized(taken) {
				if((isLeft = line.isStationLeft(stationName))) {
					if(line.checkIfWaitingOppositeLineLeft()) {
						if(taken.equals(false)) {
								if(!line.checkIfTrainsOnTheWayFromRight()) {
										train = line.removeFromWaitingOppositeLineLeft();
									synchronized(train) {
										train.notify();
									}
								}
						}
					}
				}
				else {
					if(line.checkIfWaitingOppositeLineRight()) {
						if(taken.equals(false)) {
							if(!line.checkIfTrainsOnTheWayFromLeft()) {
								train = line.removeFromWaitingOppositeLineRight();
								synchronized(train) {
									train.notify();
								}
							}
						}
					}
				}
				}
				synchronized(canMoveAfter) {
				if(isLeft) {
					if(line.checkIfWaitingTailingLeft()) {
						if(canMoveAfter.equals(true)) {
							train = line.removeFromWaitingTailingLeft();
							synchronized(train) {
								train.notify();
							}
						}
					}
				}
				else
					if(line.checkIfWaitingTailingRight()) {
						if(canMoveAfter.equals(true)) {
							train = line.removeFromWaitingTailingRight();
							synchronized(train) {
								train.notify();
							}
						}
					}
			}
			}
			try {
				Thread.sleep(1000);
			}
			catch(InterruptedException exception) {
				exception.printStackTrace();
				logger.log(Level.WARNING, exception.fillInStackTrace().toString());
			}
			if(pause.equals(true)) {
				synchronized(this) {
					try {
						wait();
					}
					catch(InterruptedException exception) {
						exception.printStackTrace();
						logger.log(Level.WARNING, exception.fillInStackTrace().toString());
					}
				}
			}
		}
		while(true);
	}
	
	public void leaveStation(Train train) {
		synchronized(trains) {
			trains.remove(train);
		}
	
	}
	public void trainArrived(Train train, boolean serialize){
		if(!serialize) {
			synchronized(trains) {
				trains.add(train);
			}
		}
		else
			SerializationMethods.serialize(train.getSerializationInfo());
	}
}