package classes.wagon;
import exceptions.InvalidNumberInputException;
import classes.train.TrainVehicle;


public abstract class Wagon extends TrainVehicle{
	private Double length;
	private String designation;
	
	public Wagon(Double length, String designation) throws InvalidNumberInputException{
		this.length = length;
		this.designation = designation;
	}
	
	public String getDesignation() {
		return designation;
	}
	
	public Double getLength() {
		return length;
	}
	
	@Override
	public boolean equals(Object temp) {
		if(temp == null)
			return false;
		if(temp == this)
			return true;
		if(temp instanceof Wagon) {
			Wagon other = (Wagon)temp;
			if(designation.equals(other.designation) && length.equals(other.length))
				return true;
		}
		return false;
	}
}

