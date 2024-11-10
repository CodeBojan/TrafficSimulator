package classes.map;
import classes.vehicle.RoadVehicle;
import classes.helper.Coordinate;

public class Road implements Field{
	public RoadVehicle vehicle;
	private Coordinate coordinate;
	
	public Road(Coordinate coordinate){  //popravi u mapi inicijalizaciju!
		this.coordinate = coordinate;
	}
	
	public Coordinate getCoordinate(){
		return coordinate;
	}
	
	@Override
	public boolean equals(Object other){
		if(other == null)
			return false;
		if(other == this)
			return true;
		if(other instanceof Road){
			Road temp = (Road)other;
			if(coordinate.equals(temp.coordinate))
				return true;
		}
		return false;
	}
	
	@Override
	public int hashCode(){
		int hash = 1;
		hash = 3 * hash * coordinate.hashCode();
		return hash;
	}
}