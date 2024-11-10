# TrainSimulator

TrainSimulator simulates train and car traffic. It is a fun, advanced level, monolithic and desktop Java project that uses JavaFX for GUI. The project consists of a map that loads map, train, roadways and vehicles configuration from text files and initializes traffic. The current map configuration contains 5 train stations. Trains depart when there is no other trains inbound to the stations on that track, and the vehicles, when the train approaches, stop at the train stops and wait until the train passes. Trains consist a head locomotive and any number of wagons, each taking up one tile on the map. 
The project uses a multuthreaded approach for each vehicle and train.
