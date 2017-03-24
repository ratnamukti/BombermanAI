package Server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class Main
{
  public static final boolean DEBUG_OUTPUT_ENABLED = false;
  public static final int TURN_TIME_LIMIT_MS = 1200;
  public static final int DELAY_DURATION_MS = 10;
  
  // Default values, for my testing in Eclipse project directory.
  // These could be filled with empty strings, nulls, or zeroes.
  public static int numberOfPlayer = 4;
  public static String mapFile = "defaultmap.txt";
  public static String classPath = "bin/";
  public static String[] classNames = {"TegarAI1", "MuhAI1", "MuhAI2", "TegarAI2"};
  public static String boardMap = "" 
    + "0.X.XXBXX.X.1\n"
    + ".#X#.#X#.#X#.\n"
    + "XX.PX.X.XB.XX\n"
    + "B#B#.#P#.#P#B\n"
    + "X.X.XBPBX.X.X\n"
    + ".#P#P#B#P#P#.\n"
    + "X.X.XBPBX.X.X\n"
    + "B#P#.#P#.#B#B\n"
    + "XX.BX.X.XP.XX\n"
    + ".#X#.#X#.#X#.\n"
    + "2.X.XXBXX.X.3\n";
  
  /**
   * Pause program/thread execution for some duration
   * @param msDuration - length of sleep in ms.
   */
  public void sleep(int msDuration) {
    try {
      Thread.sleep(msDuration);
    }
    catch (InterruptedException e) {}     
  }
  
  public static void main(String[] args) {    
    try {
      // NOTE: COMMENT EVERYTHING INSIDE THIS TRY-BLOCK IF YOU ARE USING ECLIPSE.
      // `args` will be in form of: {<map>, <classPath>, <playerClassName> ...}
      // (minimum length of `args[]` should be at least 3)
      if (args.length < 3) {
        throw new Exception("Missing required parameters.");
      }
      // Read map-file-source and class-path
      mapFile = args[0];
      classPath = args[1];
      // Read map file and build `boardMap`
      BufferedReader bufferedReader = new BufferedReader(new FileReader(mapFile));
      String line = bufferedReader.readLine();
      boardMap = "";
      while (line != null) {
        boardMap += line + "\n";
        line = bufferedReader.readLine();
      }
      // Get player class names
      numberOfPlayer = args.length - 2;
      classNames = new String[numberOfPlayer];
      for (int argsIndex = 2; argsIndex < args.length; argsIndex++) {
        int playerIndex = argsIndex - 2;
        classNames[playerIndex] = args[argsIndex];
        // Check if file exists
        String pathToFile = classPath + args[argsIndex] + ".class";
        File file = new File(pathToFile);
        if (!file.exists()) {
          throw new Exception(pathToFile + " does not exists.");
        }
      }
    }
    catch (Exception e) {
      System.out.println(e.getMessage());
      System.out.println();
      System.out.println("Usage:");
      System.out.println("  java Server/Main <map> <classPath> <player1> <player2> ...");
      System.out.println();
      System.out.println("Example:");
      System.out.println("  java Server/Main defaultmap.txt AI/ ContohAI1 ContohAI2");
      System.out.println("  (Compile `ContohAI1.java` and `ContohAI2.java` first!)");
      System.out.println();
      System.out.println("Where:");
      System.out.println("  * <map> is a text file that contains the map.");
      System.out.println("  * <classPath> is the location of AI .class files relative to this directory.");
      System.out.println("  * <playerN> is the AI-program class-name for player-N.");
      System.exit(0);
    }
    
    // Initiate and run game machine
    GameMachine.initiate(numberOfPlayer, classNames, boardMap);
    Thread machineThread = new Thread() {
      public void run() {
        GameMachine.run();
      }
    };
    machineThread.start();

    // Prepare player processes (also run in the background)
    PlayerProcess[] processes = new PlayerProcess[numberOfPlayer];
    for (int i = 0; i < numberOfPlayer; i++) {
      processes[i] = new PlayerProcess(classPath, classNames[i]);
    }
    
    // Run the game
    int lastHandledTurnNumber = -1;
    while (GameMachine.isGameRunning) {
      int turnNumber = GameMachine.getTurnNumber();
      String boardStateString = GameMachine.boardStateString;
      
      // Make sure the state has been processed.
      if (turnNumber <= lastHandledTurnNumber) {
        continue;
      }
      else {
        lastHandledTurnNumber = turnNumber;
      }
      
      for (int playerIndex = 0; playerIndex < numberOfPlayer; playerIndex++) {
        // Skip the player if he's offline or dead.
        if (!GameMachine.isPlayerConnected[playerIndex]) {
          continue;
        }
        
        PlayerProcess playerProcess = processes[playerIndex];
        String playerName = classNames[playerIndex];
        
        // Declare thread that fetches player's output.
        Thread fetchOutputThread = new Thread(new Runnable() {
          public void run() {
            try {
              // Send board state representation to player
              playerProcess.sendLine(boardStateString);
              playerProcess.sendLine("END");
              
              // Try to fetch player move (starts with ">> ")
              // If a move is detected, report it to GameMachine.
              boolean isplayerMoveObtained = false;
              while (!isplayerMoveObtained) {
                String playerMove = "";

                playerMove = playerProcess.getNextLine();
                if (DEBUG_OUTPUT_ENABLED) {
                  System.out.print("[" + playerName + "]: ");
                  System.out.println(playerMove);
                }
                if (playerMove.startsWith(">> ")) {
                  // Report player move
                  String parsedPlayerMove = playerMove.substring(3);
                 
                  GameMachine.reportMove(playerName, parsedPlayerMove);
                  isplayerMoveObtained = true;
                }
              }
            }
            catch (Exception e) {
              if (DEBUG_OUTPUT_ENABLED) {
                System.out.println("[" + playerName + "]: ");
                e.printStackTrace();
              }
              GameMachine.reportMove(playerName, "TIMEOUT");
            }
            finally {
              // Close this thread
              Thread.currentThread().interrupt();
            }        
          }
        });
        
        // Ensure the thread finishes before the deadline.
        long ouputDeadlineMS = System.currentTimeMillis() + TURN_TIME_LIMIT_MS;
        fetchOutputThread.start();
        while (fetchOutputThread.isAlive()) {
          if (System.currentTimeMillis() > ouputDeadlineMS) {
            // Report player timeout
            fetchOutputThread.interrupt();
            GameMachine.reportMove(playerName, "TIMEOUT");
            break;
          }
          try {
            Thread.sleep(DELAY_DURATION_MS);
          }
          catch (InterruptedException t) {}
        }
      }
        
    }
  }
}
