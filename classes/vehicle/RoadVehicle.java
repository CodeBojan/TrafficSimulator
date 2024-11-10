package classes.vehicle;
import classes.helper.Coordinate;
import exceptions.InvalidNumberInputException;

import java.io.File;
import java.io.IOException;
import java.util.function.Consumer;
import classes.map.*;
import classes.simulation.*;
import java.util.logging.*;

public abstract class RoadVehicle extends Vehicle{
	protected String brand;
	protected String model;
	protected Integer yearOfProduction;
	protected Double speed;
	protected Double timeout;
	private Coordinate currentCoordinate;
	private Coordinate previousCoordinate;
	public Roadway roadway;
	public static Integer counter = 0;
	private volatile Boolean pause;
	public static Handler handler;
	private static Logger logger;
	static {
		try {
			handler = new FileHandler(FilePaths.getLoggingFolder() + File.separator + "roadVehicle.log");
			logger = Logger.getLogger(RoadVehicle.class.getName());
			logger.addHandler(handler);
			logger.setUseParentHandlers(false);
		}
		catch(IOException exception) {
			exception.printStackTrace();
		}
	}
	
	public RoadVehicle(String brand, String model, Integer yearOfProduction, Double speed, Coordinate startCoordinate, Roadway roadway) throws InvalidNumberInputException{
		if(yearOfProduction < 0)
			throw new InvalidNumberInputException("Broj godista vozila ne sme biti manji od 0!");
		this.timeout = (1 / speed) * 1000;
		this.brand = brand;
		this.model = model;
		this.yearOfProduction = yearOfProduction;
		this.speed = speed;
		this.currentCoordinate = startCoordinate;  //logika za dodeljivanje smera, puta i koordinate je vec odradjena izvan ctora
		this.previousCoordinate = new Coordinate(startCoordinate);
		this.roadway = roadway;
		pause = false;
		
	}
	
	public Coordinate getCurrentCoordinate() {
		return currentCoordinate;
	}
	
	public Coordinate getPreviousCoordinate() {
		return previousCoordinate;
	}
	
	@Override
	public void run(){
		System.out.println("Vehicle timeout is " + timeout + " on roadway " + roadway);
		boolean endOfRoad = false;
		Consumer<Coordinate> iteration;
		while(!endOfRoad){
			iteration = scan();
			if(iteration != null){
				iteration.accept(currentCoordinate);
				Simulation.vehicleInsertImageIntoMap(this, currentCoordinate);
				if(!currentCoordinate.equals(previousCoordinate)) { //u prvom koraku nema smisla upsiati ga pa ispisati odmah
					Simulation.vehicleRemoveImageFromMap(this, previousCoordinate);
				}
				try {
					Thread.sleep(timeout.longValue());
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
			endOfRoad = roadway.checkIfEndOfRoad(currentCoordinate);
		}
		Simulation.map.removeRoadVehicle(currentCoordinate); //izbrisi ga iz mape kad zavrsi kretanje
		Simulation.vehicleRemoveImageFromMap(this, currentCoordinate);
		synchronized(counter) {
			counter++;
		}
		Simulation.vehicleInitializer.removeVehicleFromAllVehicles(this);
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
	
	private void processClosedRamp(Coordinate coordinate) {
		Field field = Simulation.map.getField(currentCoordinate);
		if(!(field instanceof Crossing)) {
			CrossingRamp ramp = Simulation.map.getCrossingRamp(coordinate);
			if(ramp != null) {
				ramp.addVehiclesToQueue(this);
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
	
	public Consumer<Coordinate> scan(){
		Coordinate copy = new Coordinate(currentCoordinate);
		Field temp = null;
		boolean closed;
		Crossing crossing = null;
		copy.moveNorth();
		if((temp = Simulation.map.getField(copy)) instanceof Road && (!previousCoordinate.equals(copy))){ //ovo je najvaznije za vehicle, da nije prethodna koordinata!
			Road road = (Road)temp;
			synchronized(road) {
				if(road.vehicle == null){
					if(roadway.contains(road.getCoordinate())){  //nece skrenuti na drugu stranu ceste
						previousCoordinate = new Coordinate(currentCoordinate);
						return Coordinate::moveNorth;
					}
				}
			}
		}
		else if((temp = Simulation.map.getField(copy)) instanceof Crossing){
			crossing = (Crossing)temp;
			if(!(closed = crossing.check()) && crossing.vehicle == null && roadway.contains(copy) && (!previousCoordinate.equals(copy))) { //ako crossing nije u roadwayu, nemoj se kretati na njega
				previousCoordinate = new Coordinate(currentCoordinate);
				return Coordinate::moveNorth;
			}
		}
		
		copy.set(currentCoordinate);
		copy.moveSouth();
		if((temp = Simulation.map.getField(copy)) instanceof Road && (!previousCoordinate.equals(copy))){
			Road road = (Road)temp;
			synchronized(road) {
				if(road.vehicle == null){
					if(roadway.contains(road.getCoordinate())){
						previousCoordinate = new Coordinate(currentCoordinate);
						return Coordinate::moveSouth;
					}
				}
			}
		}
		else if((temp = Simulation.map.getField(copy)) instanceof Crossing){
			crossing = (Crossing)temp;
			if(!(closed = crossing.check()) && crossing.vehicle == null && roadway.contains(copy)  && (!previousCoordinate.equals(copy))) {
				previousCoordinate = new Coordinate(currentCoordinate);
				return Coordinate::moveSouth;
			}
		}
		
		copy.set(currentCoordinate);
		copy.moveWest();
		if((temp = Simulation.map.getField(copy)) instanceof Road && (!previousCoordinate.equals(copy))){
			Road road = (Road)temp;
			synchronized(road) {
				if(road.vehicle == null){
					if(roadway.contains(road.getCoordinate())){
						previousCoordinate = new Coordinate(currentCoordinate);
						return Coordinate::moveWest;
					}
				}
			}
		}
		else if((temp = Simulation.map.getField(copy)) instanceof Crossing){
			crossing = (Crossing)temp;
			if(!(closed = crossing.check()) && crossing.vehicle == null && roadway.contains(copy)  && (!previousCoordinate.equals(copy))) {
				previousCoordinate = new Coordinate(currentCoordinate);
				return Coordinate::moveWest;
			}
		}
		
		copy.set(currentCoordinate);
		copy.moveEast();
		if((temp = Simulation.map.getField(copy)) instanceof Road && (!previousCoordinate.equals(copy))){
			Road road = (Road)temp;
			synchronized(road) {
				if(road.vehicle == null){ //mozda ovde thread.sleep da se ne vrti bez veze u petlji? ako ima neko ispred njega!
					if(roadway.contains(road.getCoordinate())){
						previousCoordinate = new Coordinate(currentCoordinate);
						return Coordinate::moveEast;
					}
				}
			}
		}
		else if((temp = Simulation.map.getField(copy)) instanceof Crossing){
			crossing = (Crossing)temp;
			if(!(closed = crossing.check()) && crossing.vehicle == null && roadway.contains(copy)  && (!previousCoordinate.equals(copy))) {  
				previousCoordinate = new Coordinate(currentCoordinate);
				return Coordinate::moveEast;
			}
		}
		return null;
	}
	
	
}