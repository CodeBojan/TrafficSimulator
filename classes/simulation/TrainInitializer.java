package classes.simulation;
import classes.station.Station;
import java.util.logging.*;
import classes.train.*;
import classes.locomotive.*;
import classes.map.CrossingRamp;
import classes.wagon.*;
import java.util.Queue;
import java.util.ArrayDeque;
import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.nio.file.Files;
import java.io.IOException;
import exceptions.InputFailedException;
import exceptions.InvalidPowerTypeInputException;
import exceptions.InvalidMultipleTypeLocomotivesException;
import exceptions.InvalidNumberInputException;
import exceptions.InvalidWagonTypeException;
import exceptions.NotATrainVehicleAcronymInputException;

public class TrainInitializer extends Thread{
	private Queue<File> newFiles;
	private volatile Boolean pause;
	private List<Train> allTrains;
	private TrainWatcher watcher;
	public static Handler handler;
	private static Logger logger;
	static {	
		try {
			handler = new FileHandler(FilePaths.getLoggingFolder() + File.separator + "trainInitializer.log");
			logger = Logger.getLogger(TrainInitializer.class.getName());
			logger.addHandler(handler);
			logger.setUseParentHandlers(false);
		}
		catch(IOException exception) {
			exception.printStackTrace();
		}
	}
	
	public TrainInitializer() {
		newFiles = new ArrayDeque<>();
		pause = false;
		allTrains = new ArrayList<>();
		watcher = new TrainWatcher(this);
		setDaemon(true);
	}
	
	public void pauseAllTrains() {
		for(Train train : allTrains) 
			train.pause();
	}
	
	public void unpauseAllTrains() {
		for(Train train : allTrains) {
			train.unpause();
			synchronized(train) {
				train.notify();
			}
		}
	}
	
	public void addNewFile(File file) {
		synchronized(newFiles) {
			newFiles.offer(new File(FilePaths.getTrainsFolder().getPath() + File.separator + file.getName()));
		}
	}
	
	public File takeFile() {
		synchronized(newFiles) {
			return newFiles.poll();
		}
	}
	
	@Override
	public void run() {
			watcher.start();
			File folder = FilePaths.getTrainsFolder();
			File[] listFiles = folder.listFiles();
			for(File temp : listFiles) 
				placeTrainOnMapStation(temp);		//postavljanje u neku polaznu stanicu
		while(true) {
			if(newFiles.size() > 0) 
				placeTrainOnMapStation(takeFile());
			else {
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
				synchronized(this){
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
	
	public boolean checkPause() {
		synchronized(pause) {
			return pause;
		}
	}
	
	public void pause() {
		synchronized(pause){
			pause = true;
		}
		pauseAllTrains();
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
		unpauseAllTrains();
	}
	
	public void placeTrainOnMapStation(File file) {
		try {
			Train train = parseTrain(file);
			allTrains.add(train);
			Station station = Simulation.map.getStation(train.getLineTakeOffStationName());
			station.trainArrived(train, false);
			train.start();
		}
		catch(NotATrainVehicleAcronymInputException | InvalidNumberInputException | InvalidMultipleTypeLocomotivesException | InvalidWagonTypeException | InputFailedException | InvalidPowerTypeInputException | IOException exception) {
			exception.printStackTrace();
			logger.log(Level.WARNING, exception.fillInStackTrace().toString());
		}
	}
	
	public void removeTrainFromAllTrains(Train train) {
		synchronized(allTrains) {
			allTrains.remove(train);
		}
	}
	
	public Train parseTrain(File newFile) throws NotATrainVehicleAcronymInputException, InvalidNumberInputException, InvalidMultipleTypeLocomotivesException, InvalidWagonTypeException, InputFailedException, IOException, InvalidPowerTypeInputException{
		Train train = null;
		String lineDestinationStationName = null;
		String lineTakeOffStationName = null;
		List<TrainVehicle> composition = new ArrayList<>();
		List<Locomotive> locomotives = new ArrayList<>();
		List<Wagon> wagons = new ArrayList<>();
		List<String> content = Files.readAllLines(newFile.toPath());
		String line = content.get(0);
		String[] tempSplitted;
		Double speed = null;
		TrainVehicle vehicle = null;
		String[] splitted = line.split(";");
		for(int i = 0; i < splitted.length; i++) {
			switch(i) {
				case 0:
			tempSplitted = splitted[i].split("-");
			lineDestinationStationName = tempSplitted[1];
			lineTakeOffStationName = tempSplitted[0];
				break;
			case 1:
				tempSplitted = splitted[i].split("-");
				for(String temp : tempSplitted) {
					vehicle = parseTrainVehicle(temp);
					if(vehicle == null)
						throw new NotATrainVehicleAcronymInputException("Akronim u fajlu ne obelezava tip vozila voza!");
					composition.add(vehicle);   //finalna kompozicija koja ce biti uneta u voz ako bude validna
					if(vehicle instanceof Wagon) {
						Wagon wagon = (Wagon)vehicle;
						wagons.add(wagon);
					}										//dodavanje u posebne liste za proveru validnosti kompozicije
					else {
						Locomotive locomotive = (Locomotive)vehicle;
						locomotives.add(locomotive);
					}
				}
				break;
			case 2:
				speed = Double.parseDouble(splitted[i]);
				break;
			}
		}
		testCompositionValidity(locomotives, wagons);
		train = new Train(composition, lineTakeOffStationName, lineDestinationStationName, speed);
		return train;
}
	
	private static void testCompositionValidity(List<Locomotive> locomotives, List<Wagon> wagons) throws InvalidMultipleTypeLocomotivesException, InvalidWagonTypeException, InvalidNumberInputException{
		//Provera validnosti lokomotiva
		if(locomotives.size() < 1)
			throw new InvalidNumberInputException("Broj lokomotiva ne moze biti manji od 1!");
		
		
		Locomotive first = locomotives.get(0);
		if(first instanceof PassengerLocomotive){
			for(int i = 1; i < locomotives.size(); i++){
				Locomotive temp = locomotives.get(i);
				if(temp instanceof LoadLocomotive)
					throw new InvalidMultipleTypeLocomotivesException("U jednoj kompoziciji ne smeju se naci manevarska i putnicka lokomotiva!");
				if(temp instanceof MaintenanceLocomotive)
					throw new InvalidMultipleTypeLocomotivesException("U jednoj kompoziciji ne smeju se naci teretna i putnicka lokomotiva!");
			}
		}
		else if(first instanceof LoadLocomotive){
			for(int i = 1; i < locomotives.size(); i++){
				Locomotive temp = locomotives.get(i);
				if(temp instanceof PassengerLocomotive)
					throw new InvalidMultipleTypeLocomotivesException("U jednoj kompoziciji ne smeju se naci teretna i putnicka lokomotiva!");
				if(temp instanceof MaintenanceLocomotive)
					throw new InvalidMultipleTypeLocomotivesException("U jednoj kompoziciji ne smeju se naci manevarska i putnicka lokomotiva!");
			}
		}
		else if(first instanceof MaintenanceLocomotive){
			for(int i = 1; i < locomotives.size(); i++){
				Locomotive temp = locomotives.get(i);
				if(temp instanceof PassengerLocomotive)
					throw new InvalidMultipleTypeLocomotivesException("U jednoj kompoziciji ne smeju se naci manevarska i putnicka lokomotiva!");
				if(temp instanceof LoadLocomotive)
					throw new InvalidMultipleTypeLocomotivesException("U jednoj kompoziciji ne smeju se naci teretna i manevarska lokomotiva!");
				if(temp instanceof UniversalLocomotive)
					throw new InvalidMultipleTypeLocomotivesException("U jednoj kompoziciji ne smeju se naci univerzalna i manevarska lokomotiva!");
			}
		}
		else if(first instanceof UniversalLocomotive){
			for(int i = 1; i < locomotives.size(); i++){
				Locomotive temp = locomotives.get(i);
				if(temp instanceof MaintenanceLocomotive)
					throw new InvalidMultipleTypeLocomotivesException("U jednoj kompoziciji ne smeju se naci univerzalna i manevarska lokomotiva!");
			}
		}
		//Provera validnosti vagona
		for(Locomotive tempLocomotive : locomotives){	
			if(tempLocomotive instanceof PassengerLocomotive){
				for(Wagon tempWagon : wagons){
					if(!(tempWagon instanceof PassengerWagon))
						throw new InvalidWagonTypeException("Putnicka lokomotiva moze da vuce samo putnicke vagone!");
				}
			}
			else if(tempLocomotive instanceof LoadLocomotive){
				for(Wagon tempWagon : wagons){
					if(!(tempWagon instanceof LoadWagon))
						throw new InvalidWagonTypeException("Teretna lokomotiva moze da vuce samo teretne vagone!");
				}
			}
			else if(tempLocomotive instanceof UniversalLocomotive){
				for(Wagon tempWagon : wagons){
					if(!(tempWagon instanceof LoadWagon) && !(tempWagon instanceof PassengerWagon))
						throw new InvalidWagonTypeException("Univerzalna lokomotiva moze da vuce samo teretne i putnicke vagone!");
				}
			}
			else if(tempLocomotive instanceof MaintenanceLocomotive){
				for(Wagon tempWagon : wagons){
					if(!(tempWagon instanceof SpecialWagon))
						throw new InvalidWagonTypeException("Manevarska lokomotiva moze da vuce samo specijalne vagone!");
					}
			}
		}
	}
	
	private PowerType getPowerType(String type) throws InvalidPowerTypeInputException{
		if(type.equals("Diesel"))
			return PowerType.DIESEL;
		else if(type.equals("Electric"))
			return PowerType.ELECTRIC;
		if(type.equals("Steam"))
			return PowerType.STEAM;
		throw new InvalidPowerTypeInputException("Uneti tip pogona nije validan!");
	}
	
	private TrainVehicle parseTrainVehicle(String vehicle) throws InvalidNumberInputException, InvalidPowerTypeInputException{
		String[] splitted = vehicle.split("#");
 		String type = splitted[0];
		String parameters = splitted[1];
		String[] tempSplitted = parameters.split(",");
		Double power;
		PowerType powerType = null;
		String designation;
		Double loadCapacity;
		Integer temp;
		String description;
		power = Double.parseDouble(tempSplitted[0]);
		designation = tempSplitted[1];
		if(type.length() == 2 && type.charAt(1) == 'L') //onda je sigurno akronim za lokomotivu!
			powerType = getPowerType(tempSplitted[2]);
		switch(type) {
			case "LL":
				return new LoadLocomotive(power, designation, powerType);
			case "ML":
				return new MaintenanceLocomotive(power, designation, powerType);
			case "PL":
				return new PassengerLocomotive(power, designation, powerType);
			case "UL":
				return new UniversalLocomotive(power, designation, powerType);
			case "LW":
				loadCapacity = Double.parseDouble(tempSplitted[2]);
				return new LoadWagon(power, designation, loadCapacity);
			case "PWB":
				temp = Integer.parseInt(tempSplitted[2]);
				return new PassengerWagonBeds(power, designation, temp);
			case "PWR":
				description = tempSplitted[2];
				return new PassengerWagonRestaurant(power, designation, description);
			case "PWS":
				temp = Integer.parseInt(tempSplitted[2]);
				return new PassengerWagonSeats(power, designation, temp);
			case "PWSl":
				return new PassengerWagonSleep(power, designation);
			case "SW":
				return new SpecialWagon(power, designation);
		}
		return null;		
	}
	
	
}
