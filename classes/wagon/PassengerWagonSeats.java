package classes.wagon;
import exceptions.InvalidNumberInputException;

public class PassengerWagonSeats extends PassengerWagon{
	private Integer seats;
	
	public PassengerWagonSeats(Double length, String designation, Integer seats) throws InvalidNumberInputException{
		super(length, designation);
		if(seats <= 0)
			throw new InvalidNumberInputException("Broj mesta u putnickom vagonu ne moze biti manji niti jednak 0!");
		this.seats = seats;
	}
}