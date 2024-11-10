package classes.station;
import classes.train.Train;
import java.util.Queue;
import java.util.ArrayDeque;

public class Line{
	private Boolean takenLeft;
	private Boolean takenRight;
	private Boolean canMoveAfterLeft;
	private Boolean canMoveAfterRight;
	private String stationLeftName;
	private String stationRightName;
	private Queue<Train> trainsWaitingForOppositeLineLeft;
	private Queue<Train> trainsWaitingForOppositeLineRight;
	private Queue<Train> trainsWaitingForTailingLeft;
	private Queue<Train> trainsWaitingForTailingRight;
	private Queue<Train> trainsOnTheWayFromLeft;
	private Queue<Train> trainsOnTheWayFromRight;
	
	public Line(String stationLeftName, String stationRightName){
		takenLeft = false;
		takenRight = false;
		canMoveAfterLeft = true;
		canMoveAfterRight = true;
		this.stationLeftName = stationLeftName;
		this.stationRightName = stationRightName;
		trainsWaitingForOppositeLineLeft = new ArrayDeque<>();
		trainsWaitingForOppositeLineRight = new ArrayDeque<>();
		trainsWaitingForTailingLeft = new ArrayDeque<>();
		trainsWaitingForTailingRight = new ArrayDeque<>();
		trainsOnTheWayFromLeft = new ArrayDeque<>();
		trainsOnTheWayFromRight = new ArrayDeque<>();
	}
	
	public Queue<Train> getTrainsOnTheWay(String takeOffStationName){
		if(isStationLeft(takeOffStationName)) {
				synchronized(trainsOnTheWayFromLeft){
					return trainsOnTheWayFromLeft;
			}
		}
		else {
			synchronized(trainsOnTheWayFromRight) {
				return trainsOnTheWayFromRight;
			}
		}
	}
	
	public boolean checkIfTrainsOnTheWay(String stationName) {
		if(isStationLeft(stationName))
			return checkIfTrainsOnTheWayFromLeft();
		return checkIfTrainsOnTheWayFromLeft();
	}
	
	public void removeFromTrainsOnTheWay(String takeOffStationName) {
		if(isStationLeft(takeOffStationName))
			removeFromTrainsOnTheWayFromLeft();
		else
			removeFromTrainsOnTheWayFromRight();
	}
	
	public void addToTrainsOnTheWay(Train train, String takeOffStationName) {
		if(isStationLeft(takeOffStationName))
			addToTrainsOnTheWayFromLeft(train);
		else
			addToTrainsOnTheWayFromRight(train);
	}
	
	public boolean checkIfTrainsOnTheWayFromLeft() {
		synchronized(trainsOnTheWayFromLeft) {
			if(trainsOnTheWayFromLeft.size() > 0)
				return true;
			return false;
		}
	}
	
	public boolean checkIfTrainsOnTheWayFromRight() {
		synchronized(trainsOnTheWayFromRight) {
			if(trainsOnTheWayFromRight.size() > 0)
				return true;
			return false;
		}
	}
	
	public void removeFromTrainsOnTheWayFromLeft() {
		synchronized(trainsOnTheWayFromLeft) {
			trainsOnTheWayFromLeft.poll();
		}
	}
	
	public void removeFromTrainsOnTheWayFromRight() {
		synchronized(trainsOnTheWayFromRight) {
			trainsOnTheWayFromRight.poll();
		}
	}
	
	public void addToTrainsOnTheWayFromRight(Train train) {
		synchronized(trainsOnTheWayFromRight) {
			trainsOnTheWayFromRight.offer(train);
		}
	}
	
	public void addToTrainsOnTheWayFromLeft(Train train) {
		synchronized(trainsOnTheWayFromLeft) {
			trainsOnTheWayFromLeft.offer(train);
		}
	}
	
	public Train removeFromWaitingTailingLeft() {
		synchronized(trainsWaitingForTailingLeft) {
			return trainsWaitingForTailingLeft.poll();
		}
	}
	
	public Train removeFromWaitingTailingRight() {
		synchronized(trainsWaitingForTailingRight) {
			return trainsWaitingForTailingRight.poll();
		}
	}
	
	public void addToWaitingForTailingLeft(Train train) {
		synchronized(trainsWaitingForTailingLeft) {
			System.out.println(trainsWaitingForTailingLeft.offer(train));
		}
	}
	
	public void addToWaitingForTailingRight(Train train) {
		synchronized(trainsWaitingForTailingRight) {
			System.out.println(trainsWaitingForTailingRight.offer(train));
		}
	}
	
	public boolean checkIfWaitingTailingLeft() {
		synchronized(trainsWaitingForTailingLeft) {
			if(trainsWaitingForTailingLeft.size() == 0)
				return false;
			return true;
		}
	}
	
	public boolean checkIfWaitingTailingRight() {
		synchronized(trainsWaitingForTailingRight) {
			if(trainsWaitingForTailingRight.size() == 0)
				return false;
			return true;
		}
	}
	
	public boolean isStationLeft(String name) {
		if(stationLeftName.equals(name))
			return true;
		return false;
	}
	
	public boolean checkIfWaitingOppositeLineLeft() {
		synchronized(trainsWaitingForOppositeLineLeft) {
			if(trainsWaitingForOppositeLineLeft.size() == 0)
				return false;
			return true;
		}
	}
	
	public boolean checkIfWaitingOppositeLineRight() {
		synchronized(trainsWaitingForOppositeLineRight) { 
			if(trainsWaitingForOppositeLineRight.size() == 0)
				return false;
			return true;
		}
	}
	
	public void addToWaitingOppositeLineLeft(Train train) {
		synchronized(trainsWaitingForOppositeLineLeft) {
			System.out.println(trainsWaitingForOppositeLineLeft.offer(train));
		}
	}
	
	public void addToWaitingOppositeLineRight(Train train) {
		synchronized(trainsWaitingForOppositeLineRight) {
			System.out.println(trainsWaitingForOppositeLineRight.offer(train));
		}
	}
	
	public Train removeFromWaitingOppositeLineLeft() {
		synchronized(trainsWaitingForOppositeLineLeft) {
			return trainsWaitingForOppositeLineLeft.poll();
		}
	}
	
	public Train removeFromWaitingOppositeLineRight() {
		synchronized(trainsWaitingForOppositeLineRight) {
			return trainsWaitingForOppositeLineRight.poll();
		}
	}
	
	public Boolean getTaken(String stationName){
		if(stationName.equals(stationLeftName)) {
			synchronized(takenRight) {
				return takenRight;  //treba mu informacija o suprotnom smeru
			}
		}
		synchronized(takenLeft) {
			return takenLeft;
		}
	}
	
	public void setTaken(String stationName, boolean set){ //vise vozova moze upisati istovremeno - mora biti synchronized
		if(stationName.equals(stationLeftName)) {
			synchronized(takenLeft) {
				takenLeft = set;
			}
		}
			
		else if(stationName.equals(stationRightName)) {
			synchronized(takenRight) {
				takenRight = set;
			}
		}
	}
	
	public boolean checkIfTailing(String stationName) {
		if(isStationLeft(stationName))
			return checkIfWaitingTailingLeft();
		return checkIfWaitingTailingRight();
	}
	
	public boolean checkIfWaiting(String stationName) {
		if(isStationLeft(stationName))
			return checkIfWaitingOppositeLineLeft();
		return checkIfWaitingOppositeLineRight();    
	}
	
	
	public Boolean getCanMoveAfter(String stationName){
		if(stationName.equals(stationLeftName)) {
			synchronized(canMoveAfterLeft) {
				return canMoveAfterLeft;
			}
		}
		synchronized(canMoveAfterRight) {
			return canMoveAfterRight;
		}
	}
	
	public void setCanMoveAfter(String stationName, boolean set){
		if(stationName.equals(stationLeftName)) {
			synchronized(canMoveAfterLeft) {
				canMoveAfterLeft = set;
			}
		}
		else if(stationName.equals(stationRightName)) {
			synchronized(canMoveAfterRight) {
				canMoveAfterRight = set;
			}
		}
	}
	
	@Override
	public String toString() {
		return "Line: " + stationLeftName + "-" + stationRightName;
	}
}