package classes.simulation;
import classes.train.TrainVehicle;
import java.util.logging.*;
import classes.locomotive.Locomotive;
import classes.wagon.Wagon;
import javafx.geometry.Orientation;
import classes.vehicle.*;
import java.util.List;
import classes.map.*;
import classes.station.Station;
import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.layout.BorderPane;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.event.EventHandler;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.image.Image;
import java.io.File;
import java.io.IOException;

import javafx.scene.layout.StackPane;
import javafx.scene.layout.*;
import javafx.scene.layout.FlowPane;
import javafx.geometry.Pos;
import classes.helper.Coordinate;
import java.util.HashMap;
import javafx.scene.image.ImageView;
public class Simulation extends Application{
	public static Map map = new Map();
	private static HashMap<String, Image> images;
	private static GridPane mapGrid;
	public static VehicleInitializer vehicleInitializer = new VehicleInitializer();
	public static TrainInitializer trainInitializer = new TrainInitializer();
	private static Boolean paused = false;
	private static Boolean started = false;
	public static Handler handler;
	private static Logger logger;
	static {
		initializeImages();
		try {
			handler = new FileHandler(FilePaths.getLoggingFolder() + File.separator + "simulation.log");
			logger = Logger.getLogger(Simulation.class.getName());
			logger.addHandler(handler);
			logger.setUseParentHandlers(false);
		}
		catch(IOException exception) {
			exception.printStackTrace();
		}
	}
	
	public static void main(String[] args){
		try {
		launch();
		}
		catch(Exception exception) {
			exception.printStackTrace();
			logger.log(Level.WARNING, exception.fillInStackTrace().toString());
		}
	}
	
	@Override
	public void init() throws Exception{
		super.init();
	}
	
	@Override
	public void stop() throws Exception{
		super.stop();
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception{
		primaryStage.setTitle("Simple Train Traffic Simulation");
		StackPane mainPane = new StackPane();
		VBox buttonMenu = new VBox();
		buttonMenu.setPadding(new Insets(10));
		Button startSimulation = new Button("Simulation");
		Button viewHistory = new Button("View History");
		startSimulation.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent event) {
				changeToSimulation(primaryStage);
			}
		});
		viewHistory.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				viewHistory();
			}
		});
		buttonMenu.getChildren().add(startSimulation);
		buttonMenu.getChildren().add(viewHistory);
		mainPane.getChildren().add(buttonMenu);
		Scene mainScene = new Scene(mainPane, 650, 500);
		primaryStage.setScene(mainScene);
		primaryStage.show();
		BackgroundImage myBI = new BackgroundImage(new Image(FilePaths.getConfigurationFolder().getPath() + File.separator + "train.jpg",650,500,false,true),
		        BackgroundRepeat.NO_REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
		          BackgroundSize.DEFAULT);
		mainPane.setBackground(new Background(myBI));
		primaryStage.setResizable(false);
	}
	
	private void changeToSimulation(Stage primaryStage) {
		BorderPane pane = new BorderPane();
		pane.setTop(getTopSimulation());
		getBottomSimulation();
		pane.setBottom(mapGrid);
		Scene simulationScene = new Scene(pane, 850, 850);
		primaryStage.setScene(simulationScene);	
	}
	
	private void viewHistory() {
		FlowPane pane = new FlowPane(100, 100);
		pane.setAlignment(Pos.CENTER);
		VBox box = new VBox();
		List<Button> buttons = SerializationMethods.getButtons();
		for(Button button : buttons)
			box.getChildren().add(button);
		ScrollPane scrollPane = new ScrollPane(box);		// 2
		scrollPane.setPrefViewportHeight(450);				// 3
		scrollPane.setPrefViewportWidth(450);				// 4
		pane.getChildren().add(scrollPane);
		Scene viewHistory = new Scene(pane, 500, 500);
		Stage newStage = new Stage();
		newStage.setScene(viewHistory);
		newStage.show();
	}
	
	private FlowPane getTopSimulation() {

        FlowPane pane = new FlowPane(Orientation.HORIZONTAL);
        pane.setAlignment(Pos.CENTER);
        Button startButton = new Button("Start");
        startButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if(started.equals(false)) {
				map.startAllStations();
				map.startCrossings();
				vehicleInitializer.start();
				trainInitializer.start();
				started = true;
				}
			}
		});
        Button pauseButton = new Button("Pause");
        pauseButton.setOnAction(new EventHandler<ActionEvent>() {
        	@Override
        	public void handle(ActionEvent event) {
        		if(paused.equals(false)) {
	        		trainInitializer.pause();
	        		vehicleInitializer.pause();
	        		map.pauseStations();
	        		map.pauseCrossingRamps();
	        		paused = true;
        		}
        	}
        });
        Button resumeButton = new Button("Resume");
        resumeButton.setOnAction(new EventHandler<ActionEvent>() {
        	@Override
        	public void handle(ActionEvent event) {
        		if(paused.equals(true)) {
	        		map.unpauseStations();
	        		map.unpauseCrossingRamps();
	        		vehicleInitializer.unpause();
	        		trainInitializer.unpause();	
	        		paused = false;
        		}
        	
        	}
        });
        pane.getChildren().add(startButton);   
        pane.getChildren().add(pauseButton);
        pane.getChildren().add(resumeButton);
        return pane;
    }
	
	private void getBottomSimulation() {
		mapGrid = new GridPane();
		mapGrid.setPadding(new Insets(10, 10, 10, 10));
		ImageView cell = null;
		for(int i = 0; i < Map.LENGTH; i++)
			for(int j = 0; j < Map.WIDTH; j++) {
				Field field = map.getField(new Coordinate(i, j));
				if(field instanceof Track)
					cell = new ImageView(images.get("Track"));
				else if(field instanceof Crossing)
					cell = new ImageView(images.get("Crossing"));
				else if(field instanceof Station)
					cell = new ImageView(images.get("Station"));
				else if(field instanceof Road)
					cell = new ImageView(images.get("Road"));
				else if(field == null)
					cell = new ImageView(images.get("White"));
				mapGrid.add(cell, j, i);
			}
		mapGrid.setGridLinesVisible(true);
	}
	
	private static void initializeImages() {
		images = new HashMap<>();
		String folderPath = FilePaths.getPicturesFolder().getPath() + File.separator;
		images.put("Vehicle", new Image(folderPath + "vehicle.png", 30, 30, false, false));
		images.put("Locomotive", new Image(folderPath + "locomotive.png", 30, 30, false, false));
		images.put("White", new Image(folderPath + "white.png", 30, 30, false, false));
		images.put("Road", new Image(folderPath + "road.png", 30, 30, false, false));
		images.put("Crossing", new Image(folderPath + "crossing.png", 30, 30, false, false));
		images.put("Station", new Image(folderPath + "station.png", 30, 30, false, false));
		images.put("Track", new Image(folderPath + "track.png", 30, 30, false, false));
		images.put("Wagon", new Image(folderPath + "wagon.png", 30, 30, false, false));
		images.put("Truck", new Image(folderPath + "truck.png", 30, 30, false, false));
	}
	
	public static void vehicleRemoveImageFromMap(Vehicle vehicle, Coordinate coordinate) {
		String overridenImage;
		if(vehicle instanceof RoadVehicle)
			overridenImage = vehicle.getOverridenImagePrevious();
		else
			overridenImage = vehicle.getOverridenImageInFront();
		TrainVehicle tempTrain = null;
		RoadVehicle tempRoad = null;
		if(vehicle instanceof RoadVehicle)
			tempRoad = (RoadVehicle)vehicle;
		else
			tempTrain = (TrainVehicle)vehicle;
		Field field = map.getField(coordinate);
		synchronized(field) {
			if(tempRoad != null)
				Simulation.map.removeRoadVehicle(tempRoad.getPreviousCoordinate());
			else if(tempTrain != null)
				Simulation.map.removeTrainVehicle(tempTrain.getPreviousCoordinate());
			ImageView view = (ImageView)mapGrid.getChildren().get(mapIntoVectorCoordinate(coordinate));
			view.setImage(images.get(overridenImage));
		}
	}
	
	private static int mapIntoVectorCoordinate(Coordinate coordinate) {
		return coordinate.row * Map.LENGTH + coordinate.column;
	}
	
	public static void vehicleInsertImageIntoMap(Vehicle vehicle, Coordinate coordinate) {
		if(vehicle instanceof TrainVehicle) {
			TrainVehicle trainVehicle = (TrainVehicle)vehicle;
			Field field = map.getField(coordinate);
			if(field instanceof Track) 
				vehicle.setOverridenImage("Track");
			else if(field instanceof Crossing) 
				vehicle.setOverridenImage("Crossing");
			synchronized(field) {
				Simulation.map.setVehicle(trainVehicle, trainVehicle.getCurrentCoordinate());
				ImageView view = (ImageView)mapGrid.getChildren().get(mapIntoVectorCoordinate(coordinate));
				if(vehicle instanceof Locomotive)
					view.setImage(images.get("Locomotive"));
				else if(vehicle instanceof Wagon)
					view.setImage(images.get("Wagon"));
			}
		}
		else if(vehicle instanceof RoadVehicle) {
			RoadVehicle roadVehicle = (RoadVehicle)vehicle;
			Field field = map.getField(coordinate);
			if(field instanceof Road) 
				vehicle.setOverridenImage("Road");
			else if(field instanceof Crossing) 
				vehicle.setOverridenImage("Crossing");
				synchronized(field) {
					Simulation.map.setVehicle(roadVehicle, roadVehicle.getCurrentCoordinate());
					ImageView view = (ImageView)mapGrid.getChildren().get(mapIntoVectorCoordinate(coordinate));
					if(vehicle instanceof Car)
						view.setImage(images.get("Vehicle"));
					else if(vehicle instanceof Truck)
						view.setImage(images.get("Truck"));
				}
		}
	}
}