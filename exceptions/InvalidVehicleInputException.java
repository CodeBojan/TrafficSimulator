package exceptions;

public class InvalidVehicleInputException extends Exception{
	public InvalidVehicleInputException(){
		super("Uneseni objekat nije vozilo!");
	}
	public InvalidVehicleInputException(String message){
		super(message);
	}
}