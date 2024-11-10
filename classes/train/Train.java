package classes.train;
import exceptions.*;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Queue;
import java.util.function.Consumer;
import classes.map.*;
import classes.simulation.*;
import classes.station.Station;
import classes.station.*;
import classes.helper.*;
import classes.locomotive.*;
import java.util.logging.*;

public class Train extends Thread{
	private static int trainCounter = 0;
	private Integer orderedTrainNumber;
	private List<TrainVehicle> trainComposition;
	private Double timeout;
	private String lineDestinationStationName;
	private String lineTakeOffStationName;	
	private String takeOffStationName;
	private String nextStationName; 
	private Integer trainVehiclesArrivedNextStation;
	public static int trainsFinished = 0;
	private volatile Boolean pause;
	private SerializationUtility info;
	public static Handler handler;
	private static Logger logger;
	static {
		try {
			handler = new FileHandler(FilePaths.getLoggingFolder() + File.separator + "train.log");
			logger = Logger.getLogger(Train.class.getName());
			logger.addHandler(handler);
			logger.setUseParentHandlers(false);
		}
		catch(IOException exception) {
			exception.printStackTrace();
		}
	}
	
	public Double getTimeout(){
		return timeout;
	}
	
	private void synchronizeTakenOppositeDirection(Line line, Station thisStation) {
			Boolean taken = line.getTaken(takeOffStationName);
			synchronized(this){
				while(taken.equals(true)){
					try{
						if(line.isStationLeft(thisStation.getStationName()))
							line.addToWaitingOppositeLineLeft(this);
						else
							line.addToWaitingOppositeLineRight(this);
						wait();
						taken = line.getTaken(takeOffStationName);
					}
					catch(InterruptedException exception){
						exception.printStackTrace();
						logger.log(Level.WARNING, exception.fillInStackTrace().toString());
					}
			}
			line.setTaken(takeOffStationName, true);
		}
	}
	
	private void synchronizeCanMoveAfter(Line line, Station thisStation) {
		Boolean canMoveAfter = line.getCanMoveAfter(takeOffStationName);
		synchronized(this){
			while(canMoveAfter.equals(false)){
				try{
					if(line.isStationLeft(thisStation.getStationName()))
						line.addToWaitingForTailingLeft(this);
					else
						line.addToWaitingForTailingRight(this);
					wait();
					canMoveAfter = line.getCanMoveAfter(takeOffStationName);
				}
				catch(InterruptedException exception){
					exception.printStackTrace();
					logger.log(Level.WARNING, exception.fillInStackTrace().toString());
				}
			}	
		}
		line.setCanMoveAfter(takeOffStationName, false);
		System.out.println("canMoveAfter is false - locked");
	}
	
	public void adjustTimeout(Line line) {
		Queue<Train> inFrontTrains;
		inFrontTrains = line.getTrainsOnTheWay(takeOffStationName);
		Object[] array = inFrontTrains.toArray();
		synchronized(inFrontTrains) {
			if(array.length > 0) {
				Train inFront = (Train)array[array.length - 1];
				if(inFront != null) {
					Double timeout;
					if((timeout = inFront.getTimeout()) > this.timeout) 
						this.timeout = timeout;
				}
			}
		}
	}
	
	@Override
	public void run(){
		long startTime = System.currentTimeMillis();
		while(!takeOffStationName.equals(lineDestinationStationName)){
			info.addStationsVisited(takeOffStationName);
			Station currentStation = Simulation.map.getStation(takeOffStationName);
			setNextStationName();
			Line line = currentStation.getLine(takeOffStationName + "-" + nextStationName);
			if(line == null)
				line = currentStation.getLine(nextStationName + "-" + takeOffStationName); //moze biti da idem od B ka A!
			Station nextStation = Simulation.map.getStation(nextStationName);
			synchronizeTakenOppositeDirection(line, currentStation); //glavne sinhronizacije
			synchronizeCanMoveAfter(line, currentStation);   //glavne sinhronizacije
				//proveravanje brzina
			Double regularTimeout = this.timeout;
				adjustTimeout(line);
				boolean electric = false;
				Coordinate start = StartingTrainCoordinateUtility.getStart(takeOffStationName, nextStationName);
				for(TrainVehicle temp : trainComposition){
					temp.setCurrentCoordinate(new Coordinate(start)); //setujem pri dobijenoj start poziciji iz stanice ceo voz na koordinatu. Nece biti problem sa GUIjem jer je mapa ta koja se prikazuje
					if(temp instanceof Locomotive){
						Locomotive l = (Locomotive)temp;
						if(l.getPowerType().equals(PowerType.ELECTRIC))
							electric = true;
					}
				}
				boolean freeLine = false;
				int counterLeftStation = 0;
				this.trainVehiclesArrivedNextStation = 0;
				line.addToTrainsOnTheWay(this, takeOffStationName);  //JAKO VAZNA LINIJA, STAVLJANJE DA JE NA PUTU
				currentStation.leaveStation(this);

//=====================================================//GLAVNA PETLJA
				while(true){                    
					counterLeftStation++;
					move(counterLeftStation, electric, info);
					if(trainVehiclesArrivedNextStation == 0)
						info.addCoordinatesOfMovement(trainComposition.get(0).getCurrentCoordinate());
					if(counterLeftStation > trainComposition.size() + 3) {
						if(!freeLine) {
							line.setCanMoveAfter(takeOffStationName, true);
							freeLine = true;
						}
					if(trainVehiclesArrivedNextStation == trainComposition.size())
						break;
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
				trainVehiclesArrivedNextStation = 0;
				
				nextStation.trainArrived(this, false);
				if(line.checkIfTrainsOnTheWay(takeOffStationName))
					line.setTaken(takeOffStationName, false);
				line.removeFromTrainsOnTheWay(takeOffStationName);
				if(regularTimeout != this.timeout)
					this.timeout = regularTimeout;
				takeOffStationName = nextStationName;
				

		}
		trainsFinished++;
		info.addStationsVisited(lineDestinationStationName);
		Simulation.trainInitializer.removeTrainFromAllTrains(this);
		long stopTime = System.currentTimeMillis();
		info.addDuration(stopTime - startTime);
		Station destination = Simulation.map.getStation(lineDestinationStationName);
		destination.trainArrived(this, true);
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
	
	public SerializationUtility getSerializationInfo() {
		return info;
	}
	
	private void removeIfNotStation(TrainVehicle toMoveTrainVehicle) {
		if(!(Simulation.map.getField(toMoveTrainVehicle.getPreviousCoordinate()) instanceof Station)) { //brisi ga iz prethodnog polja ako nije stanica
			Simulation.vehicleRemoveImageFromMap(toMoveTrainVehicle, toMoveTrainVehicle.getPreviousCoordinate());
		}
	}
	
	private void chargeTrack(TrainVehicle toMoveTrainVehicle, Consumer<Coordinate> iteration) {
		Coordinate copy = new Coordinate(toMoveTrainVehicle.getCurrentCoordinate());
		iteration.accept(copy);
		Field field = Simulation.map.getField(copy);
		if(field instanceof Track) {
			Track track = (Track)field;
			track.charge();
			//System.out.println("CHARGE");
		}
		else if(field instanceof Crossing) {
			Crossing crossing = (Crossing)field;
			crossing.charge();
			//System.out.println("CHARGED CROSSING");
		}
	}
	
	private void dischargeTrack(TrainVehicle toMoveTrainVehicle) {
		Field field = Simulation.map.getField(toMoveTrainVehicle.getPreviousCoordinate());
		if(field instanceof Track) {
			Track track = (Track)field;
			//System.out.println("DISCHARGE");
			track.discharge();
		}
		else if(field instanceof Crossing) {
			Crossing crossing = (Crossing)field;
			crossing.discharge();
			//System.out.println("DISCHARGED CROSSING");
		}
	}
	
	public void move(int toMove, boolean electric, SerializationUtility info){
		int i = 0; //broji koliko njih treba da izvrsi kretanje u odnosu na kretanje iz stanice
		TrainVehicle toMoveTrainVehicle;
		int iterator;
		for(iterator = trainVehiclesArrivedNextStation; iterator < trainComposition.size(); iterator++){ //kad krenu ulaziti u stanicu. petlja ne treba ici za one koji su vec usli
			toMoveTrainVehicle = trainComposition.get(iterator);
			if(i == toMove)
				break;
			Consumer<Coordinate> iteration;
			if(trainComposition.indexOf(toMoveTrainVehicle) == 0) {  //glavna if grana
				iteration = toMoveTrainVehicle.scan();
				if(iteration != null){
					chargeTrack(toMoveTrainVehicle, iteration);	
					iteration.accept(toMoveTrainVehicle.getCurrentCoordinate());
					removeIfNotStation(toMoveTrainVehicle);
					Simulation.vehicleInsertImageIntoMap(toMoveTrainVehicle, toMoveTrainVehicle.getCurrentCoordinate());
				}
				else if(iteration == null){
					toMoveTrainVehicle.setPreviousCoordinate(toMoveTrainVehicle.getCurrentCoordinate());
					 //brisem ga iz mape ispred stanice
					Simulation.vehicleRemoveImageFromMap(toMoveTrainVehicle, toMoveTrainVehicle.getCurrentCoordinate());
					trainVehiclesArrivedNextStation++;
				}
			}
			else {
				int index = trainComposition.indexOf(toMoveTrainVehicle);
				toMoveTrainVehicle.setPreviousCoordinate(toMoveTrainVehicle.getCurrentCoordinate());
				Coordinate nextCoordinate = new Coordinate(trainComposition.get(index - 1).getPreviousCoordinate()); //razlikuje se dodeljivanje lokomotivi na celu i ostalima previousCoordinate
				if(trainComposition.indexOf(toMoveTrainVehicle) == trainComposition.size() - 1)
					dischargeTrack(toMoveTrainVehicle);
				if(!nextCoordinate.equals(toMoveTrainVehicle.getCurrentCoordinate())) { //ako je sadasnja koju je nabavio jednaka prosloj, ispred stanice je -> u else granu
					toMoveTrainVehicle.setCurrentCoordinate(nextCoordinate);
					removeIfNotStation(toMoveTrainVehicle);
					Simulation.vehicleInsertImageIntoMap(toMoveTrainVehicle, toMoveTrainVehicle.getCurrentCoordinate());
				}
				else{
					//brisem ga iz mape ispred stanice
					Simulation.vehicleRemoveImageFromMap(toMoveTrainVehicle, toMoveTrainVehicle.getCurrentCoordinate());
					trainVehiclesArrivedNextStation++;
				}
			}
			i++;
			try{
				Thread.sleep((timeout.longValue()));
			}
			catch(InterruptedException exception){
				exception.printStackTrace();
				logger.log(Level.WARNING, exception.fillInStackTrace().toString());
			}
		}
	}
	
	
	
	public void setNextStationName(){
		Station currentStation = Simulation.map.getStation(takeOffStationName);
		List<String> startingPointRoutes = currentStation.neighbours;  //PITANJE ZASTO KAD U STATIONSINITIALIZER PROSLEDIM NEINICIJALIZOVANU REFRENCU ONA SE BRISE? TJ AKO JE TAMO INICIJALIZUJEM neighbours == null
		for(String temp : startingPointRoutes)
			if(temp.equals(lineDestinationStationName))
				nextStationName = lineDestinationStationName;
		if(!nextStationName.equals(lineDestinationStationName)){		
			if(startingPointRoutes.size() == 1)
				nextStationName = startingPointRoutes.get(0);
			else{
				Station destination = Simulation.map.getStation(lineDestinationStationName);
				List<String> destinationRoutes = destination.neighbours;
				//List<String> container = startingPointRoutes.stream().filter(destinationRoutes::contains).collect(toList()); 
				destinationRoutes.retainAll(startingPointRoutes);
				if(destinationRoutes.size() == 1)   
					nextStationName = destinationRoutes.get(0); //maksimalno jedna stanica moze da bude zajednicka za ovu mapu u ovoj else grani
			}
		}			
	}
	
	
	public Train(List<TrainVehicle> composition, String startingPoint, String destination, Double speed) throws InvalidNumberInputException, InvalidWagonTypeException, InvalidMultipleTypeLocomotivesException, InputFailedException{
		this.orderedTrainNumber = ++trainCounter;
		this.trainComposition = composition;
		if(startingPoint == null)
			throw new InputFailedException("Ime polazne stanice nije ucitano!");
		this.takeOffStationName = startingPoint;
		this.lineTakeOffStationName = startingPoint;
		if(destination == null)
			throw new InputFailedException("Ime odredisne stanice nije ucitano!");
		this.lineDestinationStationName = destination;
		this.nextStationName = "";
		if(speed < 0.5)
			throw new InvalidNumberInputException("Minimalna brzina vozova ne moze biti manja od 0.5!");
		this.timeout = (1 / speed) * 1000;
		pause = false;
		info = new SerializationUtility();
		setDaemon(true);
		
	}
	
	public void setTakeOffStationName(String takeOffStationName){
		this.takeOffStationName = takeOffStationName;
	}
	
	public String getNextStationName(){
		return nextStationName;
	}
	
	public String getLineDestinationStationName(){
		return lineDestinationStationName;
	}
	
	public void setLineDestinationStationName(String name){
		lineDestinationStationName = name;
	}
	
	public void setLineTakeOffStationName(String name){
		lineTakeOffStationName = name;
	}
	
	public String getLineTakeOffStationName(){
		return lineTakeOffStationName;
	}
	
	public Integer getOrderedTrainNumber(){
		return orderedTrainNumber;
	}
	

	
}