package classes.wagon;
import exceptions.InvalidNumberInputException;

public class LoadWagon extends Wagon{
	private Double maxLoadCapacity;
	
	public LoadWagon(Double length, String designation, Double maxLoadCapacity) throws InvalidNumberInputException{
		super(length, designation);
		if(maxLoadCapacity < 0)
			throw new InvalidNumberInputException("Maksimalna nosivnost vagona ne sme biti negativna!");
		this.maxLoadCapacity = maxLoadCapacity;
	}		
}