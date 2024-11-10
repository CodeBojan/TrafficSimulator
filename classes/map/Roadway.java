package classes.map;
import java.util.List;
import java.util.ArrayList;
import classes.helper.Coordinate;

public class Roadway{ //objekat koji reprezentuje celu deonicu puta
	public String name;
	public List<Coordinate> roadList;
	public Double speedLimit;
	
	public Roadway(String name, Double speedLimit, List<Coordinate> coordinates){ //da li moze nesto na kraju?
		this.name = name;
		roadList = new ArrayList<Coordinate>(coordinates);
		this.speedLimit = speedLimit;
	}
	//podrazumeva se da idu po redu coordinates
	public boolean checkIfEndOfRoad(Coordinate coordinate){
		if(roadList.get(roadList.size() - 1).equals(coordinate))
					return true;
		return false;
	}
	
	public void setSpeedLimit(Double newSpeedLimit) {
		this.speedLimit = newSpeedLimit;
	}
	public Double getSpeedLimit() {
		return speedLimit;
	}
	
	public void addRoad(Coordinate road){
		roadList.add(road);
	}
	
	public boolean contains(Coordinate road){
		return roadList.contains(road);
	}
	
	public Coordinate endOfRoad() {
		return roadList.get(roadList.size() - 1);
	}
	
	public Coordinate startOfRoad() {
		return roadList.get(0);
	}
	
	@Override
	public String toString() {
		return name;
	}
}