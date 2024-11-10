package classes.map;
import classes.vehicle.RoadVehicle;
import java.util.logging.*;
import classes.helper.Coordinate;
import classes.simulation.Simulation;
import classes.simulation.FilePaths;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.io.File;
import java.io.IOException;
import java.util.ArrayDeque;
import classes.station.Line;
public class CrossingRamp extends Thread{
	private List<Track> scanArea;
	private List<Crossing> crossings;
	private Queue<RoadVehicle> queuedVehicles;
	private volatile Boolean pause;
	private String crossingName;
	public static Handler handler;
	private static Logger logger;
	
	static{
		try {
			handler = new FileHandler(FilePaths.getLoggingFolder() + File.separator + "crossingRamp.log");
			logger = Logger.getLogger(CrossingRamp.class.getName());
			logger.addHandler(handler);
			logger.setUseParentHandlers(false);
		}
		catch(IOException exception) {
			exception.printStackTrace();
		}
	}
	
	public CrossingRamp(String name) {
		pause = false;
		crossingName = name;
		this.scanArea = new ArrayList<>();
		this.crossings = new ArrayList<>();
		queuedVehicles = new ArrayDeque<>();
		setDaemon(true);
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
	
	public void addCrossing(Crossing other) {
		crossings.add(other);
	}
	
	public void addScanSpot(Track other) {
		scanArea.add(other);
	}
	
	public void addVehiclesToQueue(RoadVehicle vehicle) {
		synchronized(queuedVehicles){
			queuedVehicles.offer(vehicle);
		}
	}
	
	public boolean containsCoordinate(Coordinate coordinate) {
		for(Crossing temp : crossings) 
			if(temp.position.equals(coordinate))
				return true;	
		return false;
	}
	
	private Line getLine() {
		if(crossingName.equals("cBC"))
			return Simulation.map.getStation("Station B").getLine("Station B-Station C");
		else if(crossingName.equals("cAB"))
			return Simulation.map.getStation("Station A").getLine("Station A-Station B");
		return Simulation.map.getStation("Station C").getLine("Station C-Station E");
	}
	
	@Override
	public void run(){
		boolean checkIncomingTrain;
		Line line = getLine();
		while(true){
			checkIncomingTrain = scan(line);
			if(checkIncomingTrain) {
				for(Crossing temp : crossings) 
						temp.lock();
			}
			else{
				for(Crossing temp : crossings)
					temp.unlock();
				//synchronized(queuedVehicles){
				//	while(!queuedVehicles.isEmpty()) {
				//		RoadVehicle vehicle = queuedVehicles.poll();
				//		synchronized(vehicle) {
				//			System.out.println("Unpaused vehicle!!!!!!!!!!");
				//			vehicle.notify();
				//		}
				//	}
				//}
			}
			try {
				Thread.sleep(500); //ovo je min kretanje voza, pa ga mogu za toliko uspavati
			}
			catch(InterruptedException exception){
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
	}
	
	public boolean checkLock() {
		for(Crossing temp : crossings)
			if(!temp.check())
				return false;
		return true;
	}
	
	public boolean scan(Line line){
		if(line.checkIfTrainsOnTheWayFromLeft())
			return true;
		else if(line.checkIfTrainsOnTheWayFromRight())
			return true;
	//	else if(line.checkIfWaitingOppositeLineLeft())
	//		return true;
	//	else if(line.checkIfWaitingOppositeLineRight())
	//		return true;
	//	else if(line.checkIfWaitingTailingLeft())
	//		return true;
	//	else if(line.checkIfWaitingTailingRight())
	//		return true;
		return false;
	}
}
