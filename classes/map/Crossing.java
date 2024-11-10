package classes.map;
import classes.helper.Coordinate;
import classes.vehicle.Vehicle;

public class Crossing implements Field{
	public Vehicle vehicle;
	private boolean closed;
	public Coordinate position;
	private Boolean charged;
	
	public Crossing(Coordinate position){
		closed = false;
		this.position = position;
		charged = false;
	}
	
	public void charge() {
		synchronized(charged) {
			charged = true;
		}
	}
	
	public void discharge() {
		synchronized(charged){
			charged = false;
		}
	}
	
	public boolean check(){
		return closed;
	}
	
	public void lock() {
		closed = true;
	}
	
	public void unlock() {
		closed = false;
	}
}