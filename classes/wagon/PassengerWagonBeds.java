package classes.wagon;
import exceptions.InvalidNumberInputException;

public class PassengerWagonBeds extends PassengerWagon{
	private Integer beds;
	
	public PassengerWagonBeds(Double length, String designation, Integer beds) throws InvalidNumberInputException{
		super(length, designation);
		if(beds <= 0)
			throw new InvalidNumberInputException("broj lezajeva u vagonu sa lezajevima ne sme biti jednak niti manji od 0!");
	}
}