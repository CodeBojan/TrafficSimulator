package classes.wagon;
import exceptions.InvalidNumberInputException;

public class PassengerWagonRestaurant extends PassengerWagon{
	private String description;
	
	public PassengerWagonRestaurant(Double length, String designation, String description) throws InvalidNumberInputException{
		super(length, designation);
		this.description = description;
	}
}