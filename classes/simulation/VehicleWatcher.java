package classes.simulation;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.io.File;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import exceptions.InvalidNumberInputException;
import java.util.logging.*;

import classes.map.CrossingRamp;

public class VehicleWatcher extends Thread{
	private VehicleInitializer initializer;
	public static Handler handler;
	private static Logger logger;
	static {
		try {
			handler = new FileHandler(FilePaths.getLoggingFolder() + File.separator + "vehicleWatcher.log");
			logger = Logger.getLogger(VehicleWatcher.class.getName());
			logger.addHandler(handler);
			logger.setUseParentHandlers(false);
		}
		catch(IOException exception) {
			exception.printStackTrace();
		}
	}
	
	public VehicleWatcher(VehicleInitializer initializer) {
		this.initializer = initializer;
		setDaemon(true);
	}
	
	@Override
	public void run() {
		try {
			WatchService watcher = FileSystems.getDefault().newWatchService();
			Path directory = FilePaths.getConfigurationFolder().toPath();
			directory.register(watcher, ENTRY_MODIFY);
			while(true) {
				WatchKey key = null;
				try {
					key = watcher.take();
				}
				catch(InterruptedException exception) {
					exception.printStackTrace();
					logger.log(Level.WARNING, exception.fillInStackTrace().toString());
					interrupt();  //ukoliko ne bude radio, ugasi thread
				}
				
				for(WatchEvent<?> event : key.pollEvents()) {
					WatchEvent.Kind<?> kind = event.kind();
					WatchEvent<Path> ev = (WatchEvent<Path>)event;
					Path fileName = ev.context();
					if(fileName.toString().equals("mainConfigurationFile.txt") && kind.equals(ENTRY_MODIFY)) { 
						synchronized(initializer.canRead) {  //ukoliko bi initializer u ovom trenutku nekog inicijalizovao, updatovanje bi poremetilo sve!
							if(initializer.canRead.equals(false)) {
								try {
									wait();
								}
								catch(InterruptedException exception) {
									exception.printStackTrace();
									logger.log(Level.WARNING, exception.fillInStackTrace().toString());
								}
							}
							initializer.readConfigFile();
						}
						synchronized(initializer) {
							initializer.notify();
						}
					}
				}
				boolean validControl = key.reset();
				if(!validControl)
					break;
				try {
					Thread.sleep(1000);
				}
				catch(InterruptedException exception) {
					exception.printStackTrace();
					logger.log(Level.WARNING, exception.fillInStackTrace().toString());
				}
				if(initializer.checkPause()) {
					synchronized(this) {
						try {
							wait();
						}
						catch(InterruptedException exception) {
							exception.printStackTrace();
							logger.log(Level.WARNING, exception.fillInStackTrace().toString());
						}
					}
				}
			}
		}
		catch(IOException | InvalidNumberInputException exception) {
			exception.printStackTrace();
			logger.log(Level.WARNING, exception.fillInStackTrace().toString());
		}
			
	}
}
