package exceptions;

public class InvalidNumberInputException extends Exception{
	public InvalidNumberInputException(){
		super("Uneseni broj nije validan!");
	}
	public InvalidNumberInputException(String message){
		super(message);
	}
}