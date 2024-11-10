package classes.locomotive;
import exceptions.InvalidNumberInputException;

public class UniversalLocomotive extends Locomotive{
	
	public UniversalLocomotive(Double power, String designation, PowerType type) throws InvalidNumberInputException{
		super(power, designation, type);
	}
	
}