package classes.vehicle;

public abstract class Vehicle extends Thread{
	private String overridenImageInFront;
	private String overridenImagePrevious;
	
	public void setOverridenImage(String imageKey) {
		overridenImagePrevious = overridenImageInFront;
		overridenImageInFront = imageKey;
	}
	
	public String getOverridenImagePrevious() {
		return overridenImagePrevious;
	}
	
	public String getOverridenImageInFront() {
		return overridenImageInFront;
	}
}