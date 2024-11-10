package classes.vehicle;
import exceptions.InvalidNumberInputException;
import classes.helper.Coordinate;
import classes.map.Roadway;


public class Truck extends RoadVehicle{
	private Double loadCapacity;
	
	public Truck(String brand, String model, Integer yearOfProduction, Double loadCapacity, Double speed, Coordinate startCoordinate, Roadway roadway) throws InvalidNumberInputException{
		super(brand, model, yearOfProduction, speed, startCoordinate, roadway);	
		if(loadCapacity < 0)
			throw new InvalidNumberInputException("Ne moze se unositi nosivost kamiona koja je manja od 0!");
		this.loadCapacity = loadCapacity;
	}
	@Override
	public String toString() {
		return "\nTruck brand: " + brand + "\nModel: " + model + "\nYear of production: " + yearOfProduction + "\nSpeed: " + speed + "\nLoad Capacity: " + loadCapacity;
	}
}