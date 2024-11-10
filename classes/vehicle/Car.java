package classes.vehicle;
import exceptions.InvalidNumberInputException;
import classes.helper.Coordinate;
import classes.map.Roadway;

public class Car extends RoadVehicle{
	private Integer numberOfDoors;
	
	public Car(String brand, String model, Integer yearOfProduction, Integer numberOfDoors, Double speed, Coordinate startCoordinate, Roadway roadway) throws InvalidNumberInputException{
		super(brand, model, yearOfProduction, speed, startCoordinate, roadway);
		if(numberOfDoors < 0)
			throw new InvalidNumberInputException("Ne moze se unositi broj vrata automobila koji je manji od 0!");
		this.numberOfDoors = numberOfDoors;
	}
	
	@Override
	public String toString() {
		return "\nCar brand: " + brand + "\nModel: " + model + "\nYear of production: " + yearOfProduction + "\nSpeed: " + speed + "\nNumber of doors: " + numberOfDoors;
	}
	
}