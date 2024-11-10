package classes.locomotive;
import exceptions.InvalidNumberInputException;

public class PassengerLocomotive extends Locomotive{
	
	public PassengerLocomotive(Double power, String designation, PowerType type) throws InvalidNumberInputException{
		super(power, designation, type);
	}
	
	
}