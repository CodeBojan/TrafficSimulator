package classes.simulation;
import java.io.File;
import java.util.List;
import java.util.ArrayList;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.io.FileInputStream;
import javafx.event.EventHandler;
import javafx.event.ActionEvent;
import javafx.scene.text.Text;
import javafx.scene.layout.BorderPane;
import java.util.logging.*;

import classes.map.CrossingRamp;

public class SerializationMethods {
	private static int counter = 0;
	public static Handler handler;
	private static Logger logger;
	static {
		try {
			handler = new FileHandler(FilePaths.getLoggingFolder() + File.separator + "serializationMethods.log");
			logger = Logger.getLogger(SerializationMethods.class.getName());
			logger.addHandler(handler);
			logger.setUseParentHandlers(false);
		}
		catch(IOException exception) {
			exception.printStackTrace();
		}
	}
	public static void serialize(SerializationUtility object) {
		try(ObjectOutputStream saver = new ObjectOutputStream(new FileOutputStream(FilePaths.getSerializationFolder().getPath() + File.separator + object.createFileName()))){
			saver.writeObject(object);
		}
		catch(IOException exception) {
			exception.printStackTrace();
			logger.log(Level.WARNING, exception.fillInStackTrace().toString());
		}
	}
	
	public static SerializationUtility deserialize(File file) {
		SerializationUtility get = null;
		try(ObjectInputStream loader = new ObjectInputStream(new FileInputStream(file))) {
			get = (SerializationUtility)loader.readObject();
		}
		catch(ClassNotFoundException | IOException exception) {
			exception.printStackTrace();
			logger.log(Level.WARNING, exception.fillInStackTrace().toString());
		}
		return get;
	}
	
	public static List<Button> getButtons(){
		List<Button> buttons = new ArrayList<>();
		File folder = FilePaths.getSerializationFolder();
		File[] list = folder.listFiles();
		Button tempButton;
		for(File temp : list) {
			String name = temp.getName();
			tempButton = new Button(name);
			tempButton.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					File[] list = getSerializedFiles();
					BorderPane pane = new BorderPane();
					Text info = new Text(getInfo());
					pane.setTop(info);
					Text serializationInfo = new Text(deserialize(list[counter - 1]).toString());
					serializationInfo.setWrappingWidth(500);
					pane.setBottom(serializationInfo);
					Scene newScene = new Scene(pane);
					Stage newStage = new Stage();
					newStage.setScene(newScene);
					newStage.setTitle(list[counter - 1].getName());
					newStage.setResizable(false);
					newStage.show();
				}
			});
			counter++;
			buttons.add(tempButton);
		}
		return buttons;	
	}
	
	private static File[] getSerializedFiles() {
		File folder = FilePaths.getSerializationFolder();
		return folder.listFiles();
	}
	
	private static String getInfo() {
		return "Coordinates: x-y | Stations : 'stationName' | Duration: 'time in seconds'"; 
	}
}
