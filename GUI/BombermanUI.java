package GUI;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.scene.Group;
import javafx.scene.text.*;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.io.*;

public class BombermanUI extends Application {
	public static final String BASE_ASSETS_PATH = "GUI/Sprites/";

	private static final double PLAYER_STATUS_IMAGE_SCALE = 1.0/40.0;
	// Size of Title's text
	public static double TITLE_FONT_SIZE_SCALE = 1.0/6.0;

	public static int PANE_SPACING = 1;
	public static final double TURN_TEXT_SCALE = 16;

	public static final double RIGHT_PANE_ACTION_TEXT_SCALE = 50;
	public static final double RIGHT_PANE_HEIGHT_SCALE = 4.5/6.0;
	public static final double RIGHT_PANE_WIDTH_SCALE = 1.0/4.0;

	public static final double LEFT_PANE_HEIGHT_SCALE = 4.5/6.0;
	public static final double LEFT_PANE_WIDTH_SCALE = 1.0/4.0;

	public static final double LEFT_PANE_TEXT_PLAYER_SCALE = 40;
	public static final double LEFT_PANE_TEXT_NORMAL_SCALE = 60;

	public static final double CENTER_PANE_HEIGHT_SCALE = 4.5/6.0;
	public static final double CENTER_PANE_WIDHT_SCALE = 2.0/4.0;

	public static ArrayList<BombermanState> listOfBombermanState;
	public static final String FONT_FAMILY = "Gentium";

	// Map buat file imagenya
	public static final HashMap<String, Image> imageMap = new HashMap<String,Image>();

	public static final int DELAY = 250;

	public static double screenWidth;
	public static double screenHeight;
	static int turnCounter = 0;

	/**
	 * Load all image's file
	 * @throws Exception
	 */
	public static void loadImage() throws Exception {
		for (int i = 1; i <= 8; i++) {
			imageMap.put("B" + i, new Image(new FileInputStream(BASE_ASSETS_PATH + "B" + i + ".png")));
		}

		// Power Up
		imageMap.put("+B",new Image(new FileInputStream(BASE_ASSETS_PATH + "+B.png")));
		imageMap.put("+P",new Image(new FileInputStream(BASE_ASSETS_PATH + "+P.png")));

		for (int i = 0; i < 4; i++) {
			imageMap.put(i + "D", new Image(new FileInputStream(BASE_ASSETS_PATH + "P" + i + "D.png")));
			imageMap.put(i + "L", new Image(new FileInputStream(BASE_ASSETS_PATH + "P" + i + "L.png")));
			imageMap.put(i + "U", new Image(new FileInputStream(BASE_ASSETS_PATH + "P" + i + "U.png")));
			imageMap.put(i + "R", new Image(new FileInputStream(BASE_ASSETS_PATH + "P" + i + "R.png")));
		}

		// Flare
		for (int i = 1; i <= 2; i++) {
			imageMap.put("F" + i,new Image(new FileInputStream(BASE_ASSETS_PATH + "F" + i + ".png")));
		}

		imageMap.put("#",new Image(new FileInputStream(BASE_ASSETS_PATH + "###.png")));
		imageMap.put("XB",new Image(new FileInputStream(BASE_ASSETS_PATH + "XBX.png")));
		imageMap.put("XP",new Image(new FileInputStream(BASE_ASSETS_PATH + "XPX.png")));
		imageMap.put("XX",new Image(new FileInputStream(BASE_ASSETS_PATH + "XXX.png")));

		imageMap.put("",new Image(new FileInputStream(BASE_ASSETS_PATH + "bg.png")));
	}


	/**
	 *
	 * Main method for creating the User Interface.
	 * @param stage
	 * @throws Exception
	 */
	@Override
	public void start(Stage stage) throws Exception {
		Screen screen = Screen.getPrimary();
		Rectangle2D bounds = screen.getVisualBounds();

		screenWidth = bounds.getWidth();
		screenHeight = bounds.getHeight();
		/*
		 * Turn's counter
		 */

		// The root of our panel
		final BorderPane bPane = new BorderPane();
		//bPane.setStyle("-fx-background-color: DAE6F3;");

		// Add text to the top of border pane
		Text text = new Text();
		text.setFont(new Font(screenHeight/10));
		text.setWrappingWidth(screenWidth);
		text.setTextAlignment(TextAlignment.CENTER);
		text.setText("BomberMan");
		text.setFill(Color.DARKKHAKI);
		// Set the top pane with the title
		bPane.setTop(text);


		// Get first turn's state
		setBombermanStateonBorderPane(bPane, 0);

		/*
		 * BOTTOM
		 * Set the button for playing, pause, back, front
		 */

		HBox bottomPane = new HBox();
		bottomPane.setPrefWidth(screenWidth);
		bottomPane.setPrefHeight(screenHeight/10);
		final Button backwardButton = new Button("<<");
		final Button forwardButton = new Button(">>");
		final Button playButton = new Button("Play");
		final Button resetButton = new Button("Reset");

		// Make them same size
		backwardButton.setMinWidth(bottomPane.getPrefWidth()/4.0);
		backwardButton.setMinHeight(bottomPane.getPrefHeight());

		forwardButton.setMinWidth(bottomPane.getPrefWidth()/4.0);
		forwardButton.setMinHeight(bottomPane.getPrefHeight());

		playButton.setMinWidth(bottomPane.getPrefWidth()/4.0);
		playButton.setMinHeight(bottomPane.getPrefHeight());

		resetButton.setMinWidth(bottomPane.getPrefWidth()/4.0);
		resetButton.setMinHeight(bottomPane.getPrefHeight());


		bottomPane.getChildren().addAll(backwardButton, playButton, forwardButton , resetButton);
		bPane.setBottom(bottomPane);

		// Add Forward Button event that reload the map on next turn
		forwardButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {

				// To prevent out of list
				if(turnCounter + 1 < listOfBombermanState.size()) {
					turnCounter++;
					setBombermanStateonBorderPane(bPane,turnCounter);
				}
			}
		});


		// Event on backward Button
		backwardButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {

				// To prevent out of list
				if(turnCounter > 0) {
					turnCounter--;
					setBombermanStateonBorderPane(bPane,turnCounter);
					playButton.setDisable(false);
				}
			}
		});


		// Event on reset Button
		resetButton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				playButton.setDisable(false);
				turnCounter = 0;
				setBombermanStateonBorderPane(bPane,turnCounter);
			}
		});

		// Event on Play Button
		playButton.setOnAction(new EventHandler<ActionEvent>() {
			boolean isPlaying = false; // Flag whether the button is playing or not
			Timeline fiveSecondsWonder = new Timeline(new KeyFrame(Duration.millis(DELAY), new EventHandler<ActionEvent>() {

				@Override
				public void handle(ActionEvent event) {
					//System.out.println(isPlaying);
					if(turnCounter + 1 < listOfBombermanState.size()) {
						turnCounter++;
						setBombermanStateonBorderPane(bPane,turnCounter);
					}

					else {
						fiveSecondsWonder.stop();
						playButton.setText("Play");
						playButton.setDisable(true);
						isPlaying = false;
						// reload
					}
				}
			}));

			/**
			 * Start the event periodically
			 * @param event
			 */
			@Override
			public void handle(ActionEvent event) {
				if (!isPlaying) {
					playButton.setText("Stop");
					isPlaying = true;
					fiveSecondsWonder.setCycleCount(Timeline.INDEFINITE);
					fiveSecondsWonder.play();
				}

				else {
					playButton.setText("Play");
					isPlaying = false;
					fiveSecondsWonder.stop();
				}
			}
		});


		/*
		 * MAIN
		 */
		Group group = new Group(bPane);
		Scene scene = new Scene(group);



		stage.setTitle("Bomberman SC");
		stage.setScene(scene);
		stage.show();
	}

	/**
	 * Create the map on GUI
	 * @param bPane border pane used to visualize the game
	 * @param turnCounter turn to show
	 */
	public static void setBombermanStateonBorderPane(BorderPane bPane, int turnCounter) {
		BombermanState bombermanStateNext = listOfBombermanState.get(turnCounter);
		int playerCount = bombermanStateNext.getPlayer_count();
		ArrayList<PlayerGUI> listOfPlayerGUIS = bombermanStateNext.getPlayerGUIS();
		ArrayList<PlayerGUI> previousListOfPlayerGUIS = null;


		if (turnCounter > 0) {
			previousListOfPlayerGUIS = listOfBombermanState.get(turnCounter - 1).getPlayerGUIS();
		}


		BombermanBoard board = bombermanStateNext.getBoard();
		ArrayList<String> listOfPlayerAction = bombermanStateNext.getPlayerActionList();
		NodeGUI[][] boardNodeGUI = board.getMap();
		int heightBoard = board.getHeight();
		int widthBoard = board.getWidth();

		// Set map on center pane
		GridPane newMap = generateMap(boardNodeGUI, heightBoard, widthBoard, listOfPlayerGUIS, previousListOfPlayerGUIS);
		bPane.setCenter(newMap);

		// Set player's status on left pane
		VBox leftPane = generatePlayerStatusPane(playerCount, listOfPlayerGUIS);
		bPane.setLeft(leftPane);

		// Set player's action and turn. turnCounter - 1 is used to set previous turn.
		VBox rightPane = generateTurnAndPlayerActionPane(listOfPlayerAction,turnCounter, listOfPlayerGUIS);
		bPane.setRight(rightPane);
	}

	/**
	 * Generate player's action text and turn on right pane.
	 * @param listOfPlayerAction list of player action on x turn
	 * @param turnCounter x turn
	 * @param listOfPlayerGUIS list of player in the game
	 * @return Vertical Box
	 */
	public static VBox generateTurnAndPlayerActionPane(ArrayList<String> listOfPlayerAction, int turnCounter, ArrayList<PlayerGUI> listOfPlayerGUIS) {
		VBox rightPane = new VBox();
		rightPane.setPadding(new Insets(10,0,10,10));
		Text turnText = new Text("TURN " + turnCounter );
		turnText.setFont(Font.font (FONT_FAMILY, TURN_TEXT_SCALE));
		rightPane.getChildren().add(turnText);
		rightPane.setPrefHeight(RIGHT_PANE_HEIGHT_SCALE * screenHeight);
		rightPane.setPrefWidth(RIGHT_PANE_WIDTH_SCALE * screenWidth);
		if (listOfPlayerAction.size() != 0) {

			for (int i = 0; i < listOfPlayerAction.size(); i++) {
				Text playerActionText = new Text(listOfPlayerGUIS.get(i).getPlayerName() + "'s Action = " + listOfPlayerAction.get(i));
				playerActionText.setFont(Font.font(FONT_FAMILY, screenHeight/RIGHT_PANE_ACTION_TEXT_SCALE));
				rightPane.getChildren().add(playerActionText);
			}
		}

		// Condition of end game
		else {
			// Count player that's alive. If it has more than one OR no one alive, check the point
			int highestAlivePoint = -1;
			int highestPointOfAll = -1;
			int countAlive = 0;

			// In case of draw
			ArrayList<Integer> setOfHighest = new ArrayList<Integer>();
			ArrayList<Integer> setOfHighestAlive = new ArrayList<Integer>();

			for (int i = 0; i < listOfPlayerGUIS.size(); i++) {
				PlayerGUI currentPlayerGUI = listOfPlayerGUIS.get(i);

				// Check if alive
				if (currentPlayerGUI.getStatus().equals("Alive")) {
					if (currentPlayerGUI.getPoint() > highestAlivePoint) {
						setOfHighestAlive.clear();
						highestAlivePoint = currentPlayerGUI.getPoint();
						setOfHighestAlive.add(i);
					}

					else if (currentPlayerGUI.getPoint() == highestAlivePoint) {
						setOfHighestAlive.add(i);
					}

					countAlive++;
				}

				if (currentPlayerGUI.getPoint() > highestPointOfAll) {
					setOfHighest.clear();
					highestPointOfAll = currentPlayerGUI.getPoint();
					setOfHighest.add(i);
				}

				else if (currentPlayerGUI.getPoint() == highestPointOfAll) {
					setOfHighest.add(i);
				}
			}

			if (countAlive == 1) {
				Text playerActionText = new Text("Congratulations\n" + listOfPlayerGUIS.get(setOfHighestAlive.get(0)).getPlayerName() + "\nWINS THE GAME" );
				playerActionText.setFont(Font.font(FONT_FAMILY, screenHeight/RIGHT_PANE_ACTION_TEXT_SCALE));
				rightPane.getChildren().add(playerActionText);
			}

			else {
				String winnerPlayer = "";
				for (int j = 0; j < setOfHighest.size(); j++) {
					winnerPlayer += " " + listOfPlayerGUIS.get(setOfHighest.get(j)).getPlayerName();
				}

				Text playerActionText = new Text("Congratulations\n" + winnerPlayer + "\nWIN THE GAME" );
				playerActionText.setFont(Font.font(FONT_FAMILY, screenHeight/RIGHT_PANE_ACTION_TEXT_SCALE));
				rightPane.getChildren().add(playerActionText);
			}
		}
		return rightPane;
	}

	/**
	 * Generate player's status on left's border pane
	 * @param playerCount How many players are there?
	 * @param listOfPlayerGUIS list of players
	 * @return Vertical Box
	 */
	public static VBox generatePlayerStatusPane(int playerCount, ArrayList<PlayerGUI> listOfPlayerGUIS) {
		VBox leftPane = new VBox();
		leftPane.setPadding(new Insets(10,0,10,10));
		leftPane.setPrefHeight(LEFT_PANE_HEIGHT_SCALE* screenHeight);
		leftPane.setPrefWidth(LEFT_PANE_WIDTH_SCALE* screenWidth);

		// Load the first turn into left pane
		for (int i = 0; i < playerCount; i++) {
			//Text text2 = new Text("Halo");
			//StackPane pane = new StackPane();
			HBox hbox = new HBox();
			hbox.setPrefWidth(leftPane.getPrefWidth());
			hbox.setPrefHeight(leftPane.getPrefHeight()/playerCount);
			hbox.setStyle(
					"-fx-border-color: black;");
			//Background
			StackPane pane = new StackPane();
			ImageView playerImage = new ImageView(imageMap.get(i + "D"));
			playerImage.setFitWidth(hbox.getPrefWidth()/2);
			playerImage.setFitHeight(hbox.getPrefHeight()-20); // magic number hehe
			playerImage.relocate(0, 0);
			pane.getChildren().add(playerImage);


			PlayerGUI playerGUI = listOfPlayerGUIS.get(i);
			VBox vbox = generatePlayerStatus(playerGUI);
			vbox.setPrefSize(hbox.getPrefWidth()/2,leftPane.getPrefHeight()/playerCount);
			hbox.getChildren().addAll(vbox,pane);
			leftPane.getChildren().add(hbox);
		}

		// Set spacing
		leftPane.setSpacing(PANE_SPACING);
		return leftPane;
	}

	/**
	 * Generate a playerGUI's status that help generatePlayerStatusPane method
	 * @param playerGUI
	 * @return
	 */
	public static VBox generatePlayerStatus(PlayerGUI playerGUI) {


		VBox vbox = new VBox();
		Text playerText = new Text(playerGUI.getPlayerName());
		playerText.setStyle("-fx-font-weight: bold;");
		playerText.setFill(Color.ORCHID);
		playerText.setFont(Font.font (FONT_FAMILY, screenHeight/LEFT_PANE_TEXT_PLAYER_SCALE));
		Text bombText = new Text("Bomb : " + playerGUI.getBombCount() + "/" + playerGUI.getMaxBomb());
		bombText.setFont(Font.font (FONT_FAMILY, screenHeight/LEFT_PANE_TEXT_NORMAL_SCALE));
		Text rangeText = new Text("Range : " + playerGUI.getRange());
		rangeText.setFont(Font.font (FONT_FAMILY, screenHeight/LEFT_PANE_TEXT_NORMAL_SCALE));
		Text statusText = new Text("Status : " + playerGUI.getStatus());

		if (playerGUI.getStatus().equals("Alive")) {
			statusText.setFill(Color.BLACK);
		}

		else {
			statusText.setFill(Color.RED);
		}
		statusText.setFont(Font.font (FONT_FAMILY, screenHeight/LEFT_PANE_TEXT_NORMAL_SCALE));
		Text pointText = new Text("Point : " + playerGUI.getPoint());
		pointText.setFont(Font.font(FONT_FAMILY,screenHeight/LEFT_PANE_TEXT_NORMAL_SCALE));



		vbox.setPadding(new Insets(7,0,0,0));
		vbox.getChildren().addAll(playerText,bombText,rangeText,statusText,pointText);

		return vbox;
	}
	/**
	 * Generate gridpane based on map that is stored in variable boardNodeGUI.
	 * @param boardNodeGUI bomberman map's state
	 * @param heightBoard the height of the map
	 * @param widthBoard the width of the map
	 * @return
	 */
	public static GridPane generateMap(NodeGUI[][] boardNodeGUI, int heightBoard, int widthBoard, ArrayList<PlayerGUI> listOfPlayerGUIS, ArrayList<PlayerGUI> previousListOfPlayerGUIS) {
		// Use Grid Pane
		GridPane bomberMap = new GridPane();
		bomberMap.setPrefSize(screenWidth * CENTER_PANE_WIDHT_SCALE,screenHeight * CENTER_PANE_HEIGHT_SCALE);

		bomberMap.setPadding(new Insets(10,0,10,10));

		for (int i = 0; i < heightBoard; i++) {
			for (int j = 0; j < widthBoard; j++) {

				// Get i and j node and get its item
				String[] nodeItemList = boardNodeGUI[i][j].getItemList();
				StackPane pane = generateStackPane(nodeItemList, listOfPlayerGUIS, previousListOfPlayerGUIS, i, j, bomberMap.getPrefHeight(), bomberMap.getPrefWidth(), heightBoard, widthBoard);

				bomberMap.add(pane,j,i);
			}
		}
		return bomberMap;
	}


	/**
	 * Generate Stack Pane to stack some images in a node
	 * @param nodeItemList item in the node
	 * @return stackpane
	 */
	public static StackPane generateStackPane(String[] nodeItemList, ArrayList<PlayerGUI> listOfPlayerGUIS, ArrayList<PlayerGUI> previousListOfPlayerGUIS, int y, int x, double pref_height_size, double pref_width_size, double mapTotalHeight, double mapTotalWidth) {
		StackPane pane = new StackPane();
		pane.setStyle("-fx-background-image: url("  + imageMap.get("") + "); " + "-fx-background-size: cover; -fx-padding: -4");
		double heightImageSize = pref_height_size/mapTotalHeight;
		double widthImageSize = pref_width_size/mapTotalWidth;
		pane.setPrefSize(widthImageSize,  heightImageSize);

		//Background
		ImageView backgroundImage = new ImageView(imageMap.get(""));
		backgroundImage.setFitWidth(widthImageSize);
		backgroundImage.setFitHeight(heightImageSize);
		backgroundImage.relocate(0, 0);
		pane.getChildren().add(backgroundImage);

		// Read image per node
		for (int itemIterate = 0; itemIterate < nodeItemList.length; itemIterate++) {
			String itemString = nodeItemList[itemIterate];

			String stringBuilder = "";
			if (!itemString.isEmpty()) {
				char itemFirstCharacter = itemString.charAt(0);
				stringBuilder = "" + itemFirstCharacter;

				//System.out.println(stringBuilder);
				if (itemFirstCharacter == '+') {
					stringBuilder += itemString.charAt(1);
				}
			}
			ImageView imageInNode = new ImageView(imageMap.get(""));

			if (stringBuilder.equals("0") ||stringBuilder.equals("1") || stringBuilder.equals("2") || stringBuilder.equals("3")) {
				// Give side animation
				int playerNumbers = Integer.parseInt(stringBuilder);

				PlayerGUI playerGUITargeted = listOfPlayerGUIS.get(playerNumbers);
				//System.out.println(playerGUITargeted.checkIfPositionSet());
				//System.out.println(playerGUITargeted);
				if (previousListOfPlayerGUIS != null) {
					PlayerGUI previousPlayerGUITargeted = previousListOfPlayerGUIS.get(playerNumbers);

					// Check current position from previous position
					int previousXPlayer = previousPlayerGUITargeted.getxLocation();
					int previousYPlayer = previousPlayerGUITargeted.getyLocation();

					// check if go up
					if ((previousYPlayer - y) == 1) {
						imageInNode = new ImageView(imageMap.get(stringBuilder + "U"));
						playerGUITargeted.setLatestMove("U");
					}

					//check if go down
					else if ((previousYPlayer - y) == -1) {
						imageInNode = new ImageView(imageMap.get(stringBuilder + "D"));
						playerGUITargeted.setLatestMove("D");
					}

					// check if go right
					else if ((previousXPlayer - x) == 1) {
						imageInNode = new ImageView(imageMap.get(stringBuilder + "L"));
						playerGUITargeted.setLatestMove("L");
					}

					//check if go left
					else if ((previousXPlayer - x) == -1) {
						imageInNode = new ImageView(imageMap.get(stringBuilder + "R"));
						playerGUITargeted.setLatestMove("R");
					}

					// Stay
					else {
						playerGUITargeted.setLatestMove(previousPlayerGUITargeted.getLatestMove());
						imageInNode = new ImageView(imageMap.get(stringBuilder + previousPlayerGUITargeted.getLatestMove()));
					}
					playerGUITargeted.setxLocation(x);
					playerGUITargeted.setyLocation(y);
				}

				else {
					//System.out.println("masuk Sini");
					playerGUITargeted.setxLocation(x);
					playerGUITargeted.setyLocation(y);
					playerGUITargeted.setLatestMove("D");

					imageInNode = new ImageView(imageMap.get(stringBuilder + "D"));
				}

			}

			else if (stringBuilder.equals("X")) {
				stringBuilder += itemString.charAt(1);
				//System.out.println(stringBuilder);
				imageInNode = new ImageView(imageMap.get(stringBuilder));
			}

			else if (stringBuilder.equals("F")) {
				stringBuilder += itemString.charAt(1);
				imageInNode = new ImageView(imageMap.get(stringBuilder));
			}

			else {
				imageInNode = new ImageView(imageMap.get(stringBuilder));
			}


			imageInNode.setFitWidth(widthImageSize);
			imageInNode.setFitHeight(heightImageSize);
			imageInNode.relocate(0,0);
			pane.getChildren().add(imageInNode);
		}

		// FEREDIT: Force draw bomb on top
		for (int itemIterate = 0; itemIterate < nodeItemList.length; itemIterate++) {
			String itemString = nodeItemList[itemIterate];

			String stringBuilder = "";
			if (!itemString.isEmpty()) {
				char itemFirstCharacter = itemString.charAt(0);
				stringBuilder = "" + itemFirstCharacter;

				//System.out.println(stringBuilder);
				if (itemFirstCharacter == '+') {
					stringBuilder += itemString.charAt(1);
				}
			}
			ImageView imageInNode = new ImageView(imageMap.get(""));

			//System.out.println(stringBuilder);
			// If the tile has bomb:
			if (stringBuilder.equals("B")) {
				int countBomb = Integer.parseInt(""+itemString.charAt(itemString.length() - 1));
				stringBuilder += countBomb;
				imageInNode = new ImageView(imageMap.get(stringBuilder));

				imageInNode.setFitWidth(widthImageSize);
				imageInNode.setFitHeight(heightImageSize);
				imageInNode.relocate(0,0);
				//imageInNode.setPreserveRatio(true);
				pane.getChildren().add(imageInNode);
			}
		}
		//pane.setStyle("-fx-border-color: black;");

		return pane;
	}

	/**
	 * Main method
	 * @param args
	 * @throws Exception
	 */
	public static void main (String[] args) throws Exception {
		//System.out.println("Test");
		loadImage();

		// set screen default


		listOfBombermanState = getAllBombermanState(args[0], args[1],args[2],args[3], args[4], args[5]);
		launch(args);


	}

	/**
	 * Fungsi untuk mengembalikan semua langkah game bomberman tersebut
	 * @return List semua state yang dijalankan
	 * @throws IOException
	 */
	public static ArrayList<BombermanState> getAllBombermanState(String map, String classPath, String AI1, String AI2, String AI3, String AI4) throws IOException {
		Process p = Runtime.getRuntime().exec("java Server/Main " + map + " " + classPath + " " + AI1 + " " + AI2 + " " + AI3 + " " + AI4);

		//Untuk input
		//FileReader fr = new FileReader(path_file);
		BufferedReader bf = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String input;

		ArrayList<BombermanState> listOfState = new ArrayList<>(); //Semua state

		while((input = bf.readLine()) != null) {
			// Read the TURN string and get the turn

			// DEBUG
			//String hehe = bf.readLine();

			String turnString[] = bf.readLine().split(" ");
			int turn = Integer.parseInt(turnString[1]);

			// Read the player string and get the number of players
			String playersCountString[] = bf.readLine().split(" ");

			int player = Integer.parseInt(playersCountString[1]);

			ArrayList<PlayerGUI> listPlayerGUIS = new ArrayList<>();

			// Generate player's state
			for (int i = 0; i < player; i++) {
				String playerString[] = bf.readLine().split(" ");
				String name = playerString[1];

				String bombCountAndMaxString = playerString[2].substring(5);
				String[] bombSplitString = bombCountAndMaxString.split("\\/");
				int bombCount = Integer.parseInt(bombSplitString[0]);
				int bombMax = Integer.parseInt(bombSplitString[1]);

				String rangeString = playerString[3].substring(6);
				int range = Integer.parseInt(rangeString);

				String status = playerString[4];

				int playerPoint = Integer.parseInt(playerString[5]);

				PlayerGUI aPlayerGUI = new PlayerGUI(name,bombCount,bombMax,range,status,playerPoint);
				listPlayerGUIS.add(aPlayerGUI);

			}

			// Generate Board
			// Board Size
			String boardSize = bf.readLine();
			String[] boardSizeSplit = boardSize.split(" ");
			int boardRow = Integer.parseInt(boardSizeSplit[1]);
			int boardColumn = Integer.parseInt(boardSizeSplit[2]);


			// Board
			BombermanBoard bombermanMap = new BombermanBoard(boardRow,boardColumn);

			// Split per row
			NodeGUI[][] allNodeGUI = new NodeGUI[boardRow][boardColumn];
			for (int rowIndex = 0; rowIndex < boardRow; rowIndex++) {
				String boardRowString = bf.readLine();

				String[] rowString = boardRowString.split("\\[");
//                for (int momo = 0; momo < rowString.length; momo++) {
//                    System.out.println(rowString[momo]);
//                }

				//System.out.print(rowIndex + " : ");
				for (int columnIndex = 1; columnIndex <= boardColumn; columnIndex++) {
					String nodeString = rowString[columnIndex];
					nodeString = nodeString.replaceAll("]", "");
					nodeString = nodeString.trim();

					// Split with ;
					String[] listOfItemInNodeSplit = nodeString.split(";");

					NodeGUI nodeGUI = new NodeGUI(listOfItemInNodeSplit);
					allNodeGUI[rowIndex][columnIndex-1] = nodeGUI;
				}
			}

			bombermanMap.setMap(allNodeGUI);


			// Set of action

			//Skip a line
			bf.readLine();

			boolean isEnd = bf.readLine().equals("GAME ENDS!");



			ArrayList<String> playerActionList = new ArrayList<>();
			BombermanState bombermanState = new BombermanState(turn, listPlayerGUIS.size(), listPlayerGUIS, bombermanMap, playerActionList);

			if (isEnd) {
				listOfState.add(bombermanState);
				break;
			}

			else {
				for (int actionIndex = 0; actionIndex < player; actionIndex++) {
					String playersActionLine = bf.readLine();
					String playerAction = playersActionLine.substring(4);
					playerActionList.add(playerAction);
				}

				//System.out.println(playerActionList);

				// Input into state


				//System.out.println(bombermanState);
				listOfState.add(bombermanState);
			}
			// Check if game has ended

		}
		return listOfState;
	}
}
