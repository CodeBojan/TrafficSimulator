package classes.wagon;
import exceptions.InvalidNumberInputException;

public class SpecialWagon extends Wagon{
	
	public SpecialWagon(Double length, String designation) throws InvalidNumberInputException{
		super(length, designation);
	}
}