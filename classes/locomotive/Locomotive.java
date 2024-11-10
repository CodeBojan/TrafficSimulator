package classes.locomotive;
import exceptions.InvalidNumberInputException;
import classes.helper.Coordinate;
import classes.train.TrainVehicle;

public abstract class Locomotive extends TrainVehicle{
	private PowerType type;
	private Double power;
	private String designation;
	
	public Locomotive(Double power, String designation, PowerType type) throws InvalidNumberInputException{
		this.power = power;
		this.type = type;
		this.designation = designation;
	}
	
	public PowerType getPowerType(){
		return type;
	}
	
	public Double getPower() {
		return power;
	}
	
	public String getDesignation() {
		return designation;
	}
	
	@Override
	public boolean equals(Object temp) {
		if(temp == null)
			return false;
		if(temp == this)
			return true;
		if(temp instanceof Locomotive) {
			Locomotive other = (Locomotive)temp;
			if(type.equals(other.type) && power.equals(other.power) && designation.equals(other.designation))
				return true;
		}
		return false;
	}
}