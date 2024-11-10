package classes.simulation;
import java.io.File;
import java.util.logging.*;

import classes.map.CrossingRamp;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
public class FilePaths {
	public static Handler handler;
	private static Logger logger;
	
	static {
		try {
			handler = new FileHandler(FilePaths.getLoggingFolder() + File.separator + "filePaths.log");
			logger = Logger.getLogger(FilePaths.class.getName());
			logger.addHandler(handler);
			logger.setUseParentHandlers(false);
		}
		catch(IOException exception) {
			exception.printStackTrace();
		}
		try {
		File[] files = parseConfigFileForPaths();
		configurationFolder = new File(files[0].getPath());
		serializationFolder = new File(files[1].getPath());
		trainsFolder = new File(files[2].getPath());
		picturesFolder = new File(files[3].getPath());
		loggingFolder = new File(files[4].getPath());
		}
		catch(IOException exception) {
			exception.printStackTrace();
			logger.log(Level.WARNING, exception.fillInStackTrace().toString());
		}
	}
	private static File configurationFolder;
	private static File serializationFolder;
	private static File trainsFolder;
	private static File picturesFolder;
	private static File loggingFolder;
	
	private static File[] parseConfigFileForPaths() throws IOException{
		File configFile = new File("." + File.separator + "configuration" + File.separator + "mainConfigurationFile.txt");
		File[] files = new File[5];
		File temp;
		List<String> lines = Files.readAllLines(configFile.toPath());
		int fileIndex = 3;
		for(int i = 0; i < files.length; i++, fileIndex++) {
			String path = parsePath(lines.get(fileIndex));
			temp = new File(path);
			files[i] = temp;
		}
		return files;
	}
	
	private static String parsePath(String input) {
		String[] splitted = input.split("/");
		String result = "";
		for(int i = 0; i < splitted.length; i++)
			result += splitted[i] + File.separator;
		return result;
	}
	
	public static File getLoggingFolder() {
		return loggingFolder;
	}
	
	public static File getSerializationFolder() {
		return serializationFolder;
	}
	
	public static File getTrainsFolder() {
		return trainsFolder;
	}
	
	public static File getMainConfigurationFile() {
		return new File(configurationFolder.getPath() + File.separator + "mainConfigurationFile.txt");
	}
	
	public static File getPicturesFolder() {
		return picturesFolder;
	}
	
	public static File getConfigurationFolder() {
		return configurationFolder;
	}
	
	public static File getVehiclesContentFile() {
		return new File(configurationFolder.getPath() + File.separator + "vehicles.txt");
	}
	
	public static File getRoadwaysFile() {
		return new File(configurationFolder.getPath() + File.separator + "roadways.txt");
	}
	
	public static File getMapFile() {
		return new File(configurationFolder.getPath() + File.separator + "map.txt");
	}
}
