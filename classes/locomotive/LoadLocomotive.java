package classes.locomotive;
import exceptions.InvalidNumberInputException;

public class LoadLocomotive extends Locomotive{
	
	public LoadLocomotive(Double power, String designation, PowerType type) throws InvalidNumberInputException{
		super(power, designation, type);
	}

}