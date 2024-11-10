package classes.simulation;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.logging.*;

import classes.map.CrossingRamp;

public class TrainWatcher extends Thread{
	private TrainInitializer initializer;
	public static Handler handler;
	private static Logger logger;
	static {
		try {
			handler = new FileHandler(FilePaths.getLoggingFolder() + File.separator + "trainWatcher.log");
			logger = Logger.getLogger(TrainWatcher.class.getName());
			logger.addHandler(handler);
			logger.setUseParentHandlers(false);
		}
		catch(IOException exception) {
			exception.printStackTrace();
		}
	
	}
	
	public TrainWatcher(TrainInitializer initializer) {
		this.initializer = initializer;
		setDaemon(true);
		}
	
	@Override
	public void run() {
		try {
			WatchService watcher = FileSystems.getDefault().newWatchService();
			Path directory = FilePaths.getTrainsFolder().toPath();
			directory.register(watcher, ENTRY_CREATE);
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
					if(fileName.toString().trim().endsWith(".txt") && kind.equals(ENTRY_CREATE)) { 
							initializer.addNewFile(fileName.toFile());
							synchronized(initializer) {
								initializer.notify();
							}
						}
					}
				
				boolean validControl = key.reset();
				if(!validControl)
					break;
				if(initializer.checkPause()) {
					synchronized(this) {
						try {
							System.out.println("TRAIN WATCHER PAUSED!");
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
		catch(IOException exception) {
			exception.printStackTrace();
			logger.log(Level.WARNING, exception.fillInStackTrace().toString());
		}
			
	}
}
