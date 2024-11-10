package classes.map;

public class Highway {
	private Roadway left;
	private Roadway right;
	
	public Highway(Roadway left, Roadway right) {
		this.left = left;
		this.right = right;
	}
	
	public Roadway getLeft() {
		return left;
	}
	
	public Roadway getRight() {
		return right;
	}
}
