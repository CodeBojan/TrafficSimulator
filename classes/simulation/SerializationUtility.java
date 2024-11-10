package classes.simulation;
import classes.helper.Coordinate;
import java.util.List;
import java.util.ArrayList;
import java.io.Serializable;
import java.io.File;
public class SerializationUtility implements Serializable{
	private List<Coordinate> coordinatesOfMovement;
	private List<String> stationsVisited;
	private double duration;
	private static int counter = 0;
	
	public SerializationUtility() {
		coordinatesOfMovement = new ArrayList<>();
		stationsVisited = new ArrayList<>();
	}
	
	public void addCoordinatesOfMovement(Coordinate coordinate) {
		coordinatesOfMovement.add(new Coordinate(coordinate));
	}
	
	public List<Coordinate> getCoordinates(){
		return coordinatesOfMovement;
	}
	
	public void addStationsVisited(String station) {
		stationsVisited.add(station);
	}
	
	public void addDuration(double duration) {
		this.duration = duration;
	}
	
	public String createFileName() {
		counter = parseCounter();
		counter++;
		String result = "";
		result += stationsVisited.get(0).toLowerCase();
		result += "#";
		result += stationsVisited.get(stationsVisited.size() - 1).toLowerCase();
		result += "-";
		File file = new File(FilePaths.getSerializationFolder().getPath() + File.separator + result + counter + ".ser");
		while(file.exists()) {
			counter++;
			file = new File(result + counter + ".ser");
		}
		return file.getName();
	}
	
	private static int parseCounter() {
		File folder = FilePaths.getSerializationFolder();
		File[] list = folder.listFiles();
		if(list.length != 0) {
			File update = list[list.length - 1];
			String name = update.getName();
			int i;
			for(i = 0; i < name.length(); i++)
				if(name.charAt(i) == '-') {
					i++;
					break;
				}
			return name.charAt(i) - '0';
		}
		return 0;
	}
	
	@Override
	public String toString() {
		String result = "";
		result += "Coordinates:\n";
		for(Coordinate temp : coordinatesOfMovement)
			result += temp.toString() + ", ";
		result += "\n";
		result += "Stations:\n";
		for(String temp : stationsVisited)
			result += temp + ", ";
		result += "\n";
		result += "Voyage Duration:\n";
		result += (duration / 1000) + " sekundi";
		return result;
		
	}
}
