package classes.simulation;
import classes.vehicle.*;
import exceptions.InvalidNumberInputException;
import classes.map.*;
import classes.helper.Coordinate;
import java.nio.file.Files;
import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.Set;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.logging.*;

public class VehicleInitializer extends Thread{
	private static List<RoadVehicle> queuedVehicles;
	private List<RoadVehicle> allVehicles;
	private Random generator = new Random();
	private static HashMap<String, Integer> initialized = new HashMap<>();
	private static HashMap<String, Integer> toInitialize;
	private static HashMap<String, Double> maxSpeeds;
	private static final double minSpeed = 0.4; 
	private List<Integer> yearsOfProduction;
	private List<Integer> numberOfDoors;
	private List<String> brands;
	private List<String> models;
	public Boolean canRead;
	private volatile Boolean pause;
	private VehicleWatcher watcher;
	public static Handler handler;
	private static Logger logger;
	
	static {
		initialized.put("hA", 0);
		initialized.put("hB", 0);
		initialized.put("hC", 0);
		try {
			handler = new FileHandler(FilePaths.getLoggingFolder() + File.separator + "vehicleInitializer.log");
			logger = Logger.getLogger(VehicleInitializer.class.getName());
			logger.addHandler(handler);
			logger.setUseParentHandlers(false);
		}
		catch(IOException exception) {
			exception.printStackTrace();
		}
	}
	public VehicleInitializer() {
		queuedVehicles = new ArrayList<>();
		yearsOfProduction = new ArrayList<>();
		numberOfDoors = new ArrayList<>();
		brands = new ArrayList<>();
		models = new ArrayList<>();
		allVehicles = new ArrayList<>();
		canRead = true;
		pause = false;
		watcher = new VehicleWatcher(this);
		setDaemon(true);
	}
	
	public boolean checkPause() {
		synchronized(pause) {
			return pause;
		}
	}
	
	public void readContentFile() throws IOException{
		String[] splitted;
		List<String> content = Files.readAllLines(FilePaths.getVehiclesContentFile().toPath());
		for(int i = 0; i < content.size(); i++) {
			splitted = content.get(i).split(";");
			switch(i) {
			case 0:
				for(String temp : splitted)
					brands.add(temp);
				break;
			case 1:
				for(String temp : splitted)
					models.add(temp);
				break;
			case 2:
				for(String temp : splitted)
					yearsOfProduction.add(Integer.parseInt(temp));
				break;
			case 3:
				for(String temp : splitted)
					numberOfDoors.add(Integer.parseInt(temp));
				break;
			}
		}
	}
	
	public void removeVehicleFromAllVehicles(RoadVehicle vehicle) {
		synchronized(allVehicles) {
			allVehicles.remove(vehicle);
		}
	}
	
	
	public void readConfigFile() throws IOException, InvalidNumberInputException{
		Path mainConfigContentPath = FilePaths.getMainConfigurationFile().toPath();
		List<String> mainConfigContent = Files.readAllLines(mainConfigContentPath);
		maxSpeeds = parseSpeeds(mainConfigContent);
		updateRoadwaySpeeds(maxSpeeds);
		toInitialize = parseIterations(mainConfigContent);
	}
	
	public void pauseAllVehicles() {
		for(RoadVehicle vehicle : allVehicles)
			vehicle.pause();
	}
	
	public void unpauseAllVehicles() {
		for(RoadVehicle vehicle : allVehicles) {
			vehicle.unpause();
			synchronized(vehicle) {
				vehicle.notify();
			}
		}
	}
	
	@Override
	public void run(){
		watcher.start();
		try {
			readContentFile();
			readConfigFile();
			Set<String> highways = maxSpeeds.keySet();
			while(true) {
				for(String highwayName : highways) {
					if(queuedVehicles.size() > 0)
						placeQueuedVehicles(highwayName);   //oni koji su bili postavljeni u red cekanja, sad pokusaj da ispises
					while(initialized.get(highwayName).intValue() < toInitialize.get(highwayName).intValue()) {  //nova inicijalizacija
						RoadVehicle vehicle = initialize(highwayName, watcher);
						if(vehicle != null) {
							Road road = getMapRoad(vehicle);   //nabavljam put iz mape na koji treba da stavim vozilo
							if(road.vehicle == null) { 
								allVehicles.add(vehicle);
								putVehicle(vehicle);
							}
							else {
								queuedVehicles.add(vehicle);
							}
							updateInitialized(highwayName);
						}
					}
				}
				synchronized(RoadVehicle.counter) {
					System.out.println("KRETANJE JE ZAVRSILO " + RoadVehicle.counter);
				}
				try {
					Thread.sleep(2000);
				}
				catch(InterruptedException exception) {
					exception.printStackTrace();
					logger.log(Level.WARNING, exception.fillInStackTrace().toString());
				}
				if(queuedVehicles.size() == 0) {
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
		catch(IOException | InvalidNumberInputException exception) {
			exception.printStackTrace();
			logger.log(Level.WARNING, exception.fillInStackTrace().toString());
		}
	}
	
	public void pause() {
		synchronized(pause){
			pause = true;
		}
		pauseAllVehicles();
	}
	
	public void unpause() {
		synchronized(pause){
			pause = false;
			synchronized(this) {
				notify();
			}
			synchronized(watcher) {
				watcher.notify();
			}
		}
		unpauseAllVehicles();
		
	}
	
	private void updateInitialized(String highwayName) {
		int increment = initialized.get(highwayName);
		increment++;
		initialized.put(highwayName, Integer.valueOf(increment));
	}
	
	private Road getMapRoad(RoadVehicle vehicle) {
		Coordinate start = vehicle.getCurrentCoordinate();
		Field field = Simulation.map.getField(start);
		Road road = (Road)field;
		return road;
	}
	
	private void placeQueuedVehicles(String roadwayName) {
		for(int i = 0; i < queuedVehicles.size(); i++) {
			RoadVehicle vehicle = queuedVehicles.get(i);
			Road road = getMapRoad(vehicle);
			if(road.vehicle == null) {
				allVehicles.add(vehicle);
				putVehicle(vehicle);
				queuedVehicles.remove(i);
			}
		}
	}
	
	public void putVehicle(RoadVehicle vehicle) {
		Coordinate currentCoordinate = vehicle.getCurrentCoordinate();
		Simulation.map.setVehicle(vehicle, currentCoordinate);
		Simulation.vehicleInsertImageIntoMap(vehicle, currentCoordinate);
		vehicle.start();
	}
	//updateovanje brzina kod roadway objekata u mapi (tj glavnoj bazi podataka za roadways!)
	private void updateRoadwaySpeeds(HashMap<String, Double> maxSpeeds) {
		Set<String> highwayNames = maxSpeeds.keySet();
		for(String highwayName : highwayNames) {
			Highway highway = Simulation.map.getHighway(highwayName);
			Double newSpeed = maxSpeeds.get(highwayName);
			highway.getLeft().setSpeedLimit(newSpeed);
			highway.getRight().setSpeedLimit(newSpeed);
		}
	}
	
	private HashMap<String, Double> parseSpeeds(List<String> mainConfigContent){
		HashMap<String, Double> speeds = new HashMap<>();
		String[] splitted;
		for(int i = 0; i < 3; i++) {
			splitted = mainConfigContent.get(i).split(";");
			speeds.put(splitted[0], Double.parseDouble(splitted[1]));
		}
		return speeds;
	}
	//objedini u jednu metodu!
	private HashMap<String, Integer> parseIterations(List<String> mainConfigContent) throws InvalidNumberInputException{
		HashMap<String, Integer> iterations = new HashMap<>();
		String[] splitted;
		for(int i = 0; i < 3; i++) {
			splitted = mainConfigContent.get(i).split(";");
			int number = Integer.parseInt(splitted[2]);
			if(number < 0)
				throw new InvalidNumberInputException("Broj vozila za inicijalizaciju ne moze biti manji od 0!");
			iterations.put(splitted[0], number);
		}
		return iterations;
	}
	
	public RoadVehicle initialize(String highwayName, VehicleWatcher watcher){
			canRead = false; //watcher ne bi trebalo da updateuje sada
			Car car = null;
			Truck truck = null;
			Coordinate start;
			Highway highway = Simulation.map.getHighway(highwayName);
			int choice = generator.nextInt(2);
			Roadway roadway;
			boolean side;
					switch(choice) {
					case 0:
						side = generator.nextBoolean();
						if(side) {
							roadway = highway.getLeft();
							start = new Coordinate(roadway.startOfRoad());
						}
						else {
							roadway = highway.getRight();
							start = new Coordinate(roadway.startOfRoad());
						}
						try {
							car = new Car(brands.get(generator.nextInt(brands.size())), models.get(generator.nextInt(models.size())), yearsOfProduction.get(generator.nextInt(yearsOfProduction.size())), numberOfDoors.get(generator.nextInt(numberOfDoors.size())), generateSpeed(highway), start, roadway);
						}
						catch(InvalidNumberInputException exception) {
							exception.printStackTrace();
							logger.log(Level.WARNING, exception.fillInStackTrace().toString());
							return null;
						}
						break;
					case 1:
						side = generator.nextBoolean();
						if(side) {
							roadway = highway.getLeft();
							start = new Coordinate(roadway.startOfRoad());
						}
						else {
							roadway = highway.getRight();
							start = new Coordinate(roadway.startOfRoad());
						}
						try {	
							truck = new Truck(brands.get(generator.nextInt(brands.size())), models.get(generator.nextInt(models.size())), yearsOfProduction.get(generator.nextInt(yearsOfProduction.size())), (double)(generator.nextInt(5) * 1000), generateSpeed(highway), start, roadway);
						}
						catch(InvalidNumberInputException exception) {
							exception.printStackTrace();
							logger.log(Level.WARNING, exception.fillInStackTrace().toString());
							return null;
						}
					}
		synchronized(watcher) { //treba synchronized jer thread treba da ownuje monitor da bi uradio notify!
			canRead = true;
			watcher.notify();  //da li mora direktno ili moze samo notify?
		}
		if(truck != null) 
			return truck;
		return car;
	}
	
	private Double generateSpeed(Highway highway) {  //GENERISANJE RANDOM BRZINE
		double maxSpeed = highway.getLeft().getSpeedLimit();
		return minSpeed + (maxSpeed - minSpeed) * generator.nextDouble();
	}
}
