package classes.locomotive;
import exceptions.InvalidNumberInputException;

public class MaintenanceLocomotive extends Locomotive{
	
	public MaintenanceLocomotive(Double power, String designation, PowerType type) throws InvalidNumberInputException{
		super(power, designation, type);
	}
}