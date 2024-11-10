package classes.train;
import classes.helper.Coordinate;
import java.util.function.Consumer;
import classes.vehicle.Vehicle;
import classes.map.*;
import classes.station.*;
import classes.simulation.*;

public abstract class TrainVehicle extends Vehicle{
	protected Coordinate currentCoordinate;
	protected Coordinate previousCoordinate;
	
	public void setCurrentCoordinate(Coordinate other){
		currentCoordinate = other;
	}
	
	public void setPreviousCoordinate(Coordinate other){
		previousCoordinate = other;
	}
	
	public Coordinate getPreviousCoordinate(){
		return previousCoordinate;
	}
	
	public Coordinate getCurrentCoordinate(){
		return currentCoordinate;
	}
	
	public Consumer<Coordinate> scan(){
		Coordinate copy = new Coordinate(currentCoordinate);
		Field temp;
		Field tempCrossing;
		copy.moveNorth();
		if((temp = Simulation.map.getField(copy)) instanceof Track){
			Track track = (Track)temp;
			if(track.vehicle == null){
				previousCoordinate = new Coordinate(currentCoordinate);
				return Coordinate::moveNorth;
			}
		}
		else if((tempCrossing = Simulation.map.getField(copy)) instanceof Crossing){
			Crossing crossing = (Crossing)tempCrossing;
			if(crossing.vehicle == null) {
				previousCoordinate = new Coordinate(currentCoordinate);
				return Coordinate::moveNorth;
			}
		}
		
		copy.set(currentCoordinate);
		copy.moveSouth();
		if((temp = Simulation.map.getField(copy)) instanceof Track){
			Track track = (Track)temp;
			if(track.vehicle == null){
				previousCoordinate = new Coordinate(currentCoordinate);
				return Coordinate::moveSouth;
			}
		}
		else if((tempCrossing = Simulation.map.getField(copy)) instanceof Crossing){
			Crossing crossing = (Crossing)tempCrossing;
			if(crossing.vehicle == null) {
				previousCoordinate = new Coordinate(currentCoordinate);
				return Coordinate::moveSouth;
			}
		}
		copy.set(currentCoordinate);
		copy.moveWest();
		if((temp = Simulation.map.getField(copy)) instanceof Track){
			Track track = (Track)temp;
			if(track.vehicle == null){
				previousCoordinate = new Coordinate(currentCoordinate);
				return Coordinate::moveWest;
			}
		}
		else if((tempCrossing = Simulation.map.getField(copy)) instanceof Crossing){
			Crossing crossing = (Crossing)tempCrossing;
			if(crossing.vehicle == null) {
				previousCoordinate = new Coordinate(currentCoordinate);
				return Coordinate::moveWest;
			}
		}
		copy.set(currentCoordinate);
		copy.moveEast();
		if((temp = Simulation.map.getField(copy)) instanceof Track){
			Track track = (Track)temp;
			if(track.vehicle == null){
				previousCoordinate = new Coordinate(currentCoordinate);
				return Coordinate::moveEast;
			}
		}
		else if((tempCrossing = Simulation.map.getField(copy)) instanceof Crossing){
			Crossing crossing = (Crossing)tempCrossing;
			if(crossing.vehicle == null) {
				previousCoordinate = new Coordinate(currentCoordinate);
				return Coordinate::moveEast;
			}
		}
		copy.set(currentCoordinate);
		copy.moveNorth();
		if(Simulation.map.getField(copy) instanceof Station) {
			return null;
		}
		copy.set(currentCoordinate);
		copy.moveSouth();
		if(Simulation.map.getField(copy) instanceof Station) {
			return null;
		}
		copy.set(currentCoordinate);
		copy.moveWest();
		if(Simulation.map.getField(copy) instanceof Station) {
			return null;
		}
		copy.set(currentCoordinate);
		copy.moveEast();
		if(Simulation.map.getField(copy) instanceof Station) {
			return null;
		}
		return null;
	}
}