package classes.helper;
import java.io.Serializable;

public class Coordinate implements Serializable{
	public Integer row;
	public Integer column;
	
	public Coordinate(Integer x, Integer y){
		this.row = x;
		this.column = y;
	}
	
	public Coordinate(Coordinate other){
		this.row = other.row;
		this.column = other.column;
	}
	
	public void set(int x, int y) {
		row = x;
		column = y;
	}
	
	public void set(Coordinate other){
		this.row = other.row;
		this.column = other.column;
	}
	//veoma vazno uslovi za ako je vozilo na ivici mape, da ne moze izaci! i napraviti indexoutofboundexception
	public void moveNorth(){
		if(row != 0)
			row--;
	}
	
	public void moveSouth(){
		if(row != 29)
		row++;
	}
	
	public void moveWest(){
		if(column != 0)
			column--;
	}
	
	public void moveEast(){
		if(column != 29)
			column++;
	}
	
	@Override
	public String toString() {
		return row + "-" + column;
	}
	
	@Override
	public boolean equals(Object other){
		if(other == null)
			return false;
		if(other instanceof Coordinate){
			Coordinate temp = (Coordinate)other;
			if(row.equals(temp.row) && column.equals(temp.column))
				return true;
		}
		return false;
	}
	
	@Override 
	public int hashCode(){
		int hash = 1;
		hash = 3 * hash * row.hashCode();
		hash = 3 * hash * column.hashCode();
		return hash;
	}
}