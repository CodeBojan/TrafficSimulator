package exceptions;

public class InvalidWagonTypeException extends Exception{
	public InvalidWagonTypeException(){
		super("Nevalidan tip vagona!");
	}
	public InvalidWagonTypeException(String message){
		super(message);
	}
}