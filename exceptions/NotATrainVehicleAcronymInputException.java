package exceptions;

public class NotATrainVehicleAcronymInputException extends Exception{
	public NotATrainVehicleAcronymInputException() {
		super();
	}
	public NotATrainVehicleAcronymInputException(String message) {
		super(message);
	}
}
