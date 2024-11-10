package classes.wagon;
import exceptions.InvalidNumberInputException;

public abstract class PassengerWagon extends Wagon{
	
	public PassengerWagon(Double length, String designation) throws InvalidNumberInputException{
		super(length, designation);
	}
}