package classes.map;
import classes.train.TrainVehicle;

public class Track implements Field{
	public TrainVehicle vehicle;
	private Boolean isCharged;
	
	public Track(){
		isCharged = false;
	}
	
	public synchronized void charge(){
		synchronized(isCharged) {
			isCharged = true;
		}
	}
	
	public synchronized void discharge(){
		synchronized(isCharged) {
			isCharged = false;
		}
	}
}