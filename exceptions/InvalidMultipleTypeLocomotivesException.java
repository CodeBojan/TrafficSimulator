package exceptions;

public class InvalidMultipleTypeLocomotivesException extends Exception{
	public InvalidMultipleTypeLocomotivesException(){
		super();
	}
	
	public InvalidMultipleTypeLocomotivesException(String message){
		super(message);
	}
}