package Server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.ReentrantLock;
import javax.swing.plaf.basic.BasicInternalFrameTitlePane.MaximizeAction;
import javax.swing.plaf.synth.SynthSeparatorUI;

public class GameMachine
{
  public static final int TURN_LIMIT = 1000;
  
  public static final int POINTS_DESTROY_WALL = 10;
  public static final int POINTS_GET_POWERUP = 50;
  public static final int POINTS_KILL_PLAYER = 500;
  
  // Access locks
  public static ReentrantLock getTurnStateLock = null;
  public static ReentrantLock getTurnNumberLock = null;
  
  // Game state
  public static int turnNumber = 0;
  public static int numberOfPlayer = 0;
  public static boolean isGameRunning = false;
  public static String[] playerNames = null;
  public static String[] playerMoves = null;
  public static HashMap<String,Integer> playerIndexMap = null;
  
  // Board/map contents
  public static int boardHeight = 0;
  public static int boardWidth = 0;
  public static ArrayList<ArrayList<ArrayList<String>>> board = null;
  public static String boardStateString = null;
  
  // Player status
  public static boolean[] isPlayerConnected = null;
  public static boolean[] isPlayerAlive = null;
  public static int[] playerScores = null;
  public static int[] playerBombsRemaining = null;
  public static int[] playerBombsCount = null;
  public static int[] playerBombsPower = null;
  public static int[] playerPositionsRow = null;
  public static int[] playerPositionsCol = null;
  
  public GameMachine() {}  
  
  //=======================================================================================
  // INITIATION / RUNTIME
  //=======================================================================================
  
  /**
   * Initiate the game machine to start a new game.
   * @param numberOfPlayer - number of players competing
   */
  public static void initiate(int numberOfPlayer, String[] playerNames, String boardMap) {
    // Prepare player data
    GameMachine.playerNames = playerNames.clone();
    GameMachine.numberOfPlayer = numberOfPlayer;
    
    // Prepare locks
    GameMachine.getTurnStateLock = new ReentrantLock();
    GameMachine.getTurnNumberLock = new ReentrantLock();
    
    playerMoves = new String[numberOfPlayer];
    playerIndexMap = new HashMap<String,Integer>();
    playerBombsRemaining = new int[numberOfPlayer];
    playerBombsPower = new int[numberOfPlayer];
    playerBombsCount = new int[numberOfPlayer];
    isPlayerConnected = new boolean[numberOfPlayer];
    isPlayerAlive = new boolean[numberOfPlayer];
    playerScores = new int[numberOfPlayer];
    playerPositionsRow = new int[numberOfPlayer];
    playerPositionsCol = new int[numberOfPlayer];
    for (int i = 0; i < numberOfPlayer; i++) {
      playerMoves[i] = null;
      playerBombsRemaining[i] = 1;
      playerBombsPower[i] = 1;
      playerBombsCount[i] = 1;
      playerScores[i] = 0;
      isPlayerAlive[i] = true;
      isPlayerConnected[i] = false;
      playerIndexMap.put(playerNames[i], i);
    }
    
    // Initialize board
    String[] boardMapLines = boardMap.split("\n");
    boardHeight = boardMapLines.length;
    boardWidth = boardMapLines[0].length();
    board = new ArrayList<ArrayList<ArrayList<String>>>();
    
    // Fill board data and try to spawn player
    for (int row = 0; row < boardHeight; row++) {
      board.add(new ArrayList<ArrayList<String>>());
      for (int col = 0; col < boardWidth; col++) {
        board.get(row).add(new ArrayList<String>());
        ArrayList<String> iterBoardCell = getBoardCell(row, col);
        char boardMapCharacter = boardMapLines[row].charAt(col);
        if (boardMapCharacter == '.') {
          // Spawn nothing
        }
        else if (Character.isDigit(boardMapCharacter)) {
          // Spawn player at location, if the player index is valid
          int playerIndex = (boardMapCharacter - '0');
          if ((playerIndex < numberOfPlayer) && !isPlayerConnected[playerIndex]) {
            iterBoardCell.add("P" + (playerIndex));
            playerPositionsRow[playerIndex] = row;
            playerPositionsCol[playerIndex] = col;
            isPlayerConnected[playerIndex] = true;
            isPlayerAlive[playerIndex] = true;
          }          
        }
        else if (boardMapCharacter == '#') {
          // Spawn indestructible wall
          iterBoardCell.add("###");
        }
        else if (boardMapCharacter == 'X') {
          // Spawn destructible wall
          iterBoardCell.add("XXX");
        }
        else if (boardMapCharacter == 'B') {
          // Spawn destructible wall with Bomb+ powerup
          iterBoardCell.add("XBX");
        }
        else if (boardMapCharacter == 'P') {
          // Spawn destructible wall with Power+ powerup
          iterBoardCell.add("XPX");
        }
        else if (boardMapCharacter == '&') {
          // Spawn a bomb (for debug purposes)
          // Treated as: spawned by P0, power:1, count:5
          // (This will explode on turn 1)
          iterBoardCell.add("B:0:1:5");
        }
        else if (boardMapCharacter == '*') {
          // Spawn a bomb (for debug purposes)
          // Treated as: spawned by P1, power:1, count:5
          // (This will explode on turn 1)
          iterBoardCell.add("B:1:1:5");
        }
        else if (boardMapCharacter == '(') {
          // Spawn a bomb (for debug purposes)
          // Treated as: spawned by P0, power:1, count:1
          // (This will explode on turn 1)
          iterBoardCell.add("B:0:1:1");
        }
        else if (boardMapCharacter == ')') {
          // Spawn a bomb (for debug purposes)
          // Treated as: spawned by P1, power:1, count:1
          // (This will explode on turn 1)
          iterBoardCell.add("B:1:1:1");
        }
      }
    }
    
    // Generate initial boardStateString
    reloadBoardStateString(0);
    System.out.println("=======================");
    System.out.println(boardStateString);
  }
  
  /**
   * Run the game.
   */
  public static void run() {
    GameMachine.isGameRunning = true;
    
    while (true) {
      int turnNumber = getTurnNumber();
      System.out.println("-----------------------");
      System.out.println("ACTION " + turnNumber);
      
      boolean allPlayerHasMoved = false;
      while (!allPlayerHasMoved) {
        // Wait until all player has moved
        allPlayerHasMoved = true;
        for (int i = 0; i < numberOfPlayer; i++) {
          if (playerMoves[i] == null && isPlayerConnected[i]) {
            allPlayerHasMoved = false;
          }
        }
      }
      
      // Process/resolve the board
      for (int i = 0; i < numberOfPlayer; i++) {
        System.out.println("P" + i + ": " + playerMoves[i]);
      }
      weakenFlares();
      movePlayers();
      grabPowerups();
      tickBombs();
      clearRubbles();
      placeBombs();
      killTimeouts();
            
      // Check for stopping condition
      // Stopping condition: exceeds turn limit or at most 1 player remaining
      boolean isGameToBeStopped = false;
      int numberOfPlayersAlive = 0;
      for (int i = 0; i < numberOfPlayer; i++) {
        numberOfPlayersAlive += (isPlayerConnected[i])? 1 : 0;
      }
      if (((turnNumber + 1) >= TURN_LIMIT) || numberOfPlayersAlive <= 1) {
        isGameToBeStopped = true;
      }
      
      // Generate new board state string
      reloadBoardStateString(turnNumber + 1);
      System.out.println("=======================");
      System.out.println(boardStateString);
      
      // Increment turn
      setTurnNumber(turnNumber + 1);
      for (int i = 0; i < numberOfPlayer; i++) {
        playerMoves[i] = null;
      }
            
      if (isGameToBeStopped) {
        System.out.println("-----------------------");
        System.out.println("GAME ENDS!");
        GameMachine.isGameRunning = false;
        break;
      }
    }
    
    System.exit(0);
  }
  
  /**
   * Register a player's move.
   * @param playerName - playerName
   * @param move - String of move made
   * @return true if the move is successfully registered.
   */
  public static boolean reportMove(String playerName, String move) {
    Integer playerNameIndex = playerIndexMap.get(playerName);
    // Register player's move
    if (playerNameIndex != null && isPlayerConnected[playerNameIndex] && isGameRunning) {
      playerMoves[playerNameIndex] = move;
      return true;
    }
    else {
      return false;
    }
  }
  
  public static void acquireTurnSliceLock() {
    while (GameMachine.getTurnStateLock == null) {}
    GameMachine.getTurnStateLock.lock();
  }
  
  public static void releaseTurnSliceLock() {
    GameMachine.getTurnStateLock.unlock();
  }
  
  public static void setTurnNumber(int newTurnNumber) {
    try {
      GameMachine.getTurnNumberLock.lock();
      GameMachine.turnNumber = newTurnNumber;
    }
    finally {
      GameMachine.getTurnNumberLock.unlock();
    }
  }
  
  public static int getTurnNumber() {
    try {
      GameMachine.getTurnNumberLock.lock();
      return GameMachine.turnNumber;
    }
    finally {
      GameMachine.getTurnNumberLock.unlock();
    }
  }
  
  /**
   * Reload board-state string representayion for current turn
   * (After all actions have been resolved)
   * @param displayedTurnNumber - turn number to be displayed
   */
  public static void reloadBoardStateString(int displayedTurnNumber) {
    boardStateString = "";
    
    // Append the next turn number
    boardStateString += "TURN " + displayedTurnNumber + "\n";
    
    // Append player data
    boardStateString += "PLAYER " + numberOfPlayer + "\n";
    for (int playerIndex = 0; playerIndex < numberOfPlayer; playerIndex++) {
      String playerState = "";
      // Append player's index and name
      playerState += "P" + playerIndex + " ";
      playerState += playerNames[playerIndex] + " ";
      // append player bombs state
      playerState += "Bomb:" + playerBombsRemaining[playerIndex] + "/" + playerBombsCount[playerIndex] + " ";
      playerState += "Range:" + playerBombsPower[playerIndex] + " ";
      // Append player's status (dead/offline/alive)
      if (!isPlayerAlive[playerIndex]) {
        playerState += "Dead";
      }
      else if (!isPlayerConnected[playerIndex]) {
        playerState += "Offline";
      }
      else {
        playerState += "Alive";
      }
      // Append player score
      playerState += " " + playerScores[playerIndex];
      boardStateString += playerState + "\n";
    }
    
    // Append board state
    boardStateString += "BOARD " + boardHeight + " " + boardWidth;
    for (int row = 0; row < boardHeight; row++) {
      String boardStringLine = "\n";
      for (int col = 0; col < boardWidth; col++) {
        ArrayList<String> cellContents = getBoardCell(row, col);
        int cellContentsLength = cellContents.size();
        String cellContentString = "";
        for (int i = 0; i < cellContentsLength; i++) {
          String cellContent = cellContents.get(i);
          // Add speparator?
          if (i > 0) {
            cellContentString += ";"; 
          }
          // Bombs and players must be parsed.
          // Otherwise, add to `cellContentString` as-is
          if (cellContent.startsWith("B")) {
            // Bomb strings are in the form of "B:<owners>:<power>:<count>"
            // Just display the power and the countdown.
            String[] cellContentSplit = cellContent.split(":");
            int bombPower = Integer.parseInt(cellContentSplit[2]);
            int bombCountdown = Integer.parseInt(cellContentSplit[3]);
            cellContentString += "B" + bombPower + "" + bombCountdown;
          }
          else if (cellContent.startsWith("P")) {
            cellContentString += cellContent.substring(1);
          }
          else {
            cellContentString += cellContent;
          }          
        }
        // Applying padding:
        int cellContentStringLength = cellContentString.length();
        switch (cellContentStringLength) {
          case 0:
            cellContentString = "     ";
            break;
          case 1:
            cellContentString = "  " + cellContentString + "  ";
            break;
          case 2:
            cellContentString = " " + cellContentString + "  ";
            break;
          case 3:
            cellContentString = " " + cellContentString + " ";
            break;
          case 4:
            cellContentString = cellContentString + " ";
            break;
        }
        boardStringLine += "[" + cellContentString + "]";
      }
      boardStateString += boardStringLine;
    }
  }
  
  /**
   * Get string representation of the board at current turn.
   * @return String representation of the board at current turn.
   */
  public static String getBoardStateString() {
    return boardStateString;
  }
    
  //=======================================================================================
  // BOARD-RELATED HELPERS METHODS
  //=======================================================================================
  
  /**
   * To obtain a cell in board, given row and column index.
   * @param row - row index (0..boardHeight)
   * @param col - column index (0..boardWidth)
   * @return a cell in board.
   */
  public static ArrayList<String> getBoardCell(int row, int col) {
    if (board == null) return null;
    if (board.get(row) == null) return null;
    return board.get(row).get(col);
  }
  
  /**
   * Remove a player from this cell (e.g. because of death/movement).
   * @param cellContents - cellContents to modify.
   * @param playerIndex - index of player (0..numberOfPlayer).
   */
  public static void removePlayerFromCell(ArrayList<String> cellContents, int playerIndex) {
    String targetString = "P" + playerIndex;
    cellContents.remove(targetString);
  }
  
  /**
   * Adds a player to a cell if the player is not already there
   * (e.g. because of movement).
   * @param cellContents - cellContents to modify.
   * @param playerIndex - index of player (0..numberOfPlayer).
   */
  public static void addPlayerToCell(ArrayList<String> cellContents, int playerIndex) {
    String targetString = "P" + playerIndex;
    if (!cellContents.contains(targetString)) {
      cellContents.add(targetString);
    }
  }
    
  /**
   * Reduce flare counts (and remove those that have expired).
   * @param cellContents - cellContents to modify.
   */
  public static void decreaseFlaresInCell(ArrayList<String> cellContents) {
    int cellContentsLength = cellContents.size();
    for (int i = 0; i < cellContentsLength; i++) {
      String cellContent = cellContents.get(i);
      // If it's "F2", weaken to "F1"
      // If it's "F1", remove it from the array
      if (cellContent.startsWith("F")) {
        int flareCountdown = cellContent.charAt(1) - '0';
        flareCountdown = flareCountdown - 1;
        if (flareCountdown == 0) {
          cellContents.remove(i);
          cellContentsLength--;
          i--;
        }
        else {
          cellContents.set(i, "F" + flareCountdown);
        }
        // Only one flare may exist in a cell
        break;
      }
    }
  }
    
  /**
   * Create a new rubble string, adding players from `blastCause` to rubble destruction-cause list.
   * @param blastCause - the owner of the bomb that hits the wall/rubble.
   * @param rubbleString - old rubble string, null means create new rubble string.
   */
  public static String mergeRubbleString(ArrayList<Integer> blastCause, String oldRubbleString) {
    Set<Integer> destructionCause = new TreeSet<Integer>();
    String newRubbleString = "R:";
    if (oldRubbleString != null) {
      // Adding old destruction cause to `destructionCause`.
      // Rubble string is in the form of "R:<cause>"
      String[] oldRubbleStringSplit = oldRubbleString.split(":");
      String[] oldDestructionCause =  oldRubbleStringSplit[1].split("\\.");
      for (String playerIndex : oldDestructionCause) {
        destructionCause.add(Integer.parseInt(playerIndex));
      }
    }
    // Merge `blastCause` to `destructionCause`
    for (Integer playerIndex : blastCause) {
      destructionCause.add(playerIndex);
    }
    // Parse back to newRubbleString
    boolean firstAddedCause = true;
    for (Integer playerIndex : destructionCause) {
      if (firstAddedCause) {
        newRubbleString += playerIndex;
        firstAddedCause = false;
      }
      else {
        newRubbleString += "." + playerIndex;
      }
    }
    return newRubbleString;
  }
  
  /**
   * Add a flare in cell and destroy destructible wall in the cell.
   * Also, if there's a powerup in the wall, it will be spawned.
   * @param cellContents - ArrayList to modify.
   * @param blastCause - The owner of the bomb that explodes this cell.
   * @param isWillBeUpdated - SUPER DIRTY FIX. True if this cell will `bombTick()` update from
   *   its subsequent iteration, false otherwise. (`bombTick()` iterates to the right, then down.)
   * @return true if the cell does not stop the explosion from spreading.
   */
  public static boolean addFlareInCell(ArrayList<String> cellContents, ArrayList<Integer> blastCause, boolean isWillBeUpdated) {
    int cellContentsLength = cellContents.size();
    boolean isBlastCanContinue = false;
    boolean isFlareHasBeenAdded = false;
    // A cell may only contain at most one of the following: {###, XXX, XBX, XPX, R:<cause>}
    // If a rubble is hit, merge its destruction-cause with blast-cause
    // If a wall with popup is broken, the powerup must be spawned.
    for (int i = 0; i < cellContentsLength; i++) {
      String cellContent = cellContents.get(i);
      if (cellContent.equals("F2")) {
        isFlareHasBeenAdded = true;
        isBlastCanContinue = true;
      }
      else if (cellContent.equals("F1")) {
        isFlareHasBeenAdded = true;
        isBlastCanContinue = true;
        cellContents.set(i, "F2");
      }
      else if (cellContent.equals("###")) {
        isFlareHasBeenAdded = true;
        isBlastCanContinue = false;
        break;
      }
      else if (cellContent.equals("XXX")) {
        isFlareHasBeenAdded = true;
        isBlastCanContinue = false;
        cellContents.set(i, "F2");
        // Add Rubble
        String newRubbleString = mergeRubbleString(blastCause, null);
        cellContents.add(newRubbleString);
        cellContentsLength++;
        break;
      }
      else if (cellContent.equals("XBX")) {
        isFlareHasBeenAdded = true;
        isBlastCanContinue = false;
        cellContents.set(i, "F2");
        // Spawn powerup
        cellContents.add("+B");
        cellContentsLength++;
        // Add Rubble
        String newRubbleString = mergeRubbleString(blastCause, null);
        cellContents.add(newRubbleString);
        cellContentsLength++;
        break;
      }
      else if (cellContent.equals("XPX")) {
        isFlareHasBeenAdded = true;
        isBlastCanContinue = false;
        cellContents.set(i, "F2");
        // Spawn powerup
        cellContents.add("+P");
        cellContentsLength++;;
        // Add Rubble
        String newRubbleString = mergeRubbleString(blastCause, null);
        cellContents.add(newRubbleString);
        cellContentsLength++;
        break;
      }
      else if (cellContent.startsWith("R")) {
        isFlareHasBeenAdded = true;
        isBlastCanContinue = false;
        // Merge rubble
        String newRubbleString = mergeRubbleString(blastCause, cellContent);
        cellContents.set(i, newRubbleString);
        break;
      }
    }
    // If rubble processing should be made: create 
    // If any of above elements was not found, create new flare
    if (!isFlareHasBeenAdded) {
      isFlareHasBeenAdded = true;
      isBlastCanContinue = true;
      cellContents.add("F2");
    }
    // If this cell contains a bomb (max 1), decrease its countdown to 1
    cellContentsLength = cellContents.size();
    for (int i = 0; i < cellContentsLength; i++) {
      String cellContent = cellContents.get(i);
      if (cellContent.startsWith("B")) {
        // Bomb strings are in the form of "B:<owners>:<power>:<count>"
        // The owners (split[1]) and power (split[2]) stays the same
        String[] cellContentSplit = cellContent.split(":");
        // XXX: SUPER DIRTY FIX. If this cell will receive bombTick() update,
        // Then another ONE countdown will be substracted from that update (causing explosion if it becomes 0).
        // If the bomb should not explode by that time, change the newCountdown to 2 instead.
        int oldCountdown = Integer.parseInt(cellContentSplit[3]);
        int newCountdown = (isWillBeUpdated && oldCountdown > 1)? 2 : 1;
        String newBombState = "B:" + cellContentSplit[1] + ":" + cellContentSplit[2] + ":" + newCountdown;
        cellContents.set(i, newBombState);
      }
    }
    return isBlastCanContinue;
  }
  
  /**
   * Check if a cell contains flare.
   * @param cellContents - cellContents to scan.
   * @return true if cellContents contains flare.
   */
  public static boolean isCellContainsFlare(ArrayList<String> cellContents) {
    for (String cellContent : cellContents) {
      if (cellContent.startsWith("F")) {
        return true;
      }
    }
    return false;
  }
  
  /**
   * Drop a bomb in a cell.
   * @param cellContents - cellContents to modify.
   * @param playerIndex - player who drops the bomb.
   * @return true if a bomb is dropped there.
   */
  public static boolean tryDropBombInCell(ArrayList<String> cellContents, int playerIndex) {
    // Cannot drop bomb if the player can't drop another bomb.
    if (playerBombsRemaining[playerIndex] == 0) {
      return false;
    }
    // Cannot drop bomb if the cell already has a bomb not recently placed (count!=8)
    // If there's a bomb with count==8 (recently placed), merge it with the bomb.
    int cellContentsLength = cellContents.size();
    boolean isBombDropped = false;
    boolean isBombFound = false;
    for (int i = 0; i < cellContentsLength; i++) {
      String cellContent = cellContents.get(i);
      if (cellContent.startsWith("B")) {
        isBombFound = true;
        String[] cellContentSplit = cellContent.split(":");
        String bombOwnersString = cellContentSplit[1];
        int bombPower = Integer.parseInt(cellContentSplit[2]);
        int bombCountdown = Integer.parseInt(cellContentSplit[3]);
        if (bombCountdown == 8) {
          // Can place bomb here, merging with existing bomb
          bombOwnersString = bombOwnersString + "." + playerIndex;
          bombPower = Math.max(bombPower, playerBombsPower[playerIndex]);
          String newBombString = "B:" + bombOwnersString + ":" + bombPower + ":" + 8;
          cellContents.set(i, newBombString);
          playerBombsRemaining[playerIndex]--;
          isBombDropped = true;
          return true;
        }
        else {
          // Cannot place bomb here
          isBombDropped = false;
          return false;
        }
      }
    }
    // If there's no bomb, drop one here
    // Bomb strings are in the form of "B:<owners>:<power>:<count>"
    if (!isBombFound) {
      int bombPower = playerBombsPower[playerIndex];
      String newBombString = "B:" + playerIndex + ":" + bombPower + ":" + 8;
      cellContents.add(newBombString);
      playerBombsRemaining[playerIndex]--;
      isBombDropped = true;
    }
    return isBombDropped;
  }
  
  /**
   * Get all player indices in a cell.
   * @param cellContents - cellContents to scan.
   * @return ArrayList containing indices of player in a cell.
   */
  public static ArrayList<Integer> getPlayerIndicesInCell(ArrayList<String> cellContents) {
    ArrayList<Integer> returnArray = new ArrayList<Integer>();
    int cellContentsLength = cellContents.size();
    for (int i = 0; i < cellContentsLength; i++) {
      String cellContent = cellContents.get(i);
      if (cellContent.startsWith("P")) {
        int playerIndex = cellContent.charAt(1) - '0';
        returnArray.add(playerIndex);
      }
    }
    return returnArray;
  }
  
  /**
   * Clear all rubbles in this call and award players involved in its destruction
   * @param cellContents - cellContents to modify.
   */
  public static void clearRubbleInCell(ArrayList<String> cellContents) {
    int cellContentsLength = cellContents.size();
    for (int i = 0; i < cellContentsLength; i++) {
      String cellContent = cellContents.get(i);
      if (cellContent.startsWith("R")) {
        // Rubble string is in the form of "R:<cause>"
        String[] cellContentSplit = cellContent.split(":");
        String[] destructionCause =  cellContentSplit[1].split("\\.");
        for (String playerIndex : destructionCause) {
          int playerIndexInt = Integer.parseInt(playerIndex);
          playerScores[playerIndexInt] += POINTS_DESTROY_WALL;
        }
        // Remove rubble from this cell
        cellContents.remove(i);
        cellContentsLength--;
        break;
      }
    }
  }
  
  /**
   * Disconnect all player who gets a timeout
   * (The sustem will no longer wait for their moves).
   */
  public static void killTimeouts() {
    for (int i = 0; i < numberOfPlayer; i++) {
      if (isPlayerConnected[i] && playerMoves[i].equals("TIMEOUT")) {
        isPlayerConnected[i] = false;
      }
    }
  }
  
  //=======================================================================================
  // MECHANIC-RELATED FUNCTIONS
  //=======================================================================================
  
  public static void weakenFlares() {
    // If there's a flare, decrease its count.
    for (int row = 0; row < boardHeight; row++) {
      for (int col = 0; col < boardWidth; col++) {
        ArrayList<String> iterBoardCell = getBoardCell(row, col);
        decreaseFlaresInCell(iterBoardCell);
      }
    }
  }
  
  public static void movePlayers() {
    for (int i = 0; i < numberOfPlayer; i++) {
      if (isPlayerConnected[i]) {
        String move = playerMoves[i];
        if (move.startsWith("MOVE ")) {
          String direction = move.substring(5);
          int playerOldPositionRow = playerPositionsRow[i];
          int playerOldPositionCol = playerPositionsCol[i];
          int playerNewPositionRow = playerOldPositionRow;
          int playerNewPositionCol = playerOldPositionCol;
          boolean isPlayerMoved = true;
          
          // Calculate possible player's new location
          if (direction.equals("UP")) {
            playerNewPositionRow = playerOldPositionRow - 1;
          }
          else if (direction.equals("DOWN")) {
            playerNewPositionRow = playerOldPositionRow + 1;
          }
          else if (direction.equals("LEFT")) {
            playerNewPositionCol = playerOldPositionCol - 1;
          }
          else if (direction.equals("RIGHT")) {
            playerNewPositionCol = playerOldPositionCol + 1;
          }
          
          
          // If the player can move there, move there.
          // the player cannot move if there's a wall/bomb there.
          if ((playerNewPositionRow < 0)
            || (playerNewPositionRow >= boardHeight)
            || (playerNewPositionCol < 0)
            || (playerNewPositionCol >= boardWidth)
          ) {
            playerNewPositionRow = playerOldPositionRow;
            playerNewPositionCol = playerOldPositionCol;
            isPlayerMoved = false;
          }
          
          ArrayList<String> targetBoardCell = getBoardCell(playerNewPositionRow, playerNewPositionCol);
          int targetBoardCellLength = targetBoardCell.size();
          for (int j = 0; j < targetBoardCellLength; j++) {
            String cellContent = targetBoardCell.get(j);
            char firstCharacter = cellContent.charAt(0);
            if ((firstCharacter == '#')
              || (firstCharacter == 'X')
              || (firstCharacter == 'B')
            ) {
              playerNewPositionRow = playerOldPositionRow;
              playerNewPositionCol = playerOldPositionCol;
              isPlayerMoved = false;
              break;
            }
          }
          
          // Move player position in the board
          if (isPlayerMoved) {
            ArrayList<String> sourceBoardCell = getBoardCell(playerOldPositionRow, playerOldPositionCol);
            removePlayerFromCell(sourceBoardCell, i);
            addPlayerToCell(targetBoardCell, i);
            playerPositionsRow[i] = playerNewPositionRow;
            playerPositionsCol[i] = playerNewPositionCol;
          }
        }
      }
    }
  }
  
  public static void grabPowerups() {
    // Iterate only from players: If a player steps on a powerup,
    // all players in that cell gains the buff and the points.
    for (int i = 0; i < numberOfPlayer; i++) {
      if (isPlayerConnected[i]) {
        int playerPositionRow = playerPositionsRow[i];
        int playerPositionCol = playerPositionsCol[i];
        ArrayList<String> cellContents = getBoardCell(playerPositionRow, playerPositionCol);
        ArrayList<Integer> playerIndicesInCell = getPlayerIndicesInCell(cellContents);
        int cellContentsLength = cellContents.size();
        // Check if powerup exists
        for (int j = 0; j < cellContentsLength; j++) {
          String cellContent = cellContents.get(j);
          if (cellContent.startsWith("+")) {
            // What type of powerup?
            switch (cellContent.charAt(1)) {
              case 'B':
                for (Integer playerIndex : playerIndicesInCell) {
                  playerBombsRemaining[playerIndex]++;
                  playerBombsCount[playerIndex]++;
                  playerScores[playerIndex] += POINTS_GET_POWERUP;
                }
                break;
              case 'P':
                for (Integer playerIndex : playerIndicesInCell) {
                  playerBombsPower[playerIndex]++;
                  playerScores[playerIndex] += POINTS_GET_POWERUP;
                }
                break;
            }
            // Remove powerup occurence in that cell
            cellContents.remove(j);
            cellContentsLength--;
            j--;
          }
        }
      }
    }
  }
  
  public static void tickBombs() {
    // For tallying player's death because of bombs
    @SuppressWarnings("unchecked")
    TreeSet<Integer>[] playerDeathCause = new TreeSet[numberOfPlayer];
    boolean isBombKillHappened = false;
    for (int i = 0; i < numberOfPlayer; i++) {
      playerDeathCause[i] = new TreeSet<Integer>();
    }
    
    // Decrease bomb counts or fill with flare
    for (int row = 0; row < boardHeight; row++) {
      for (int col = 0; col < boardWidth; col++) {
        ArrayList<String> iterCellContents = getBoardCell(row, col);
        int cellContentsLength = iterCellContents.size();
        for (int i = 0; i < cellContentsLength; i++) {
          String cellContent = iterCellContents.get(i);
          if (cellContent.startsWith("B")) {
            // Bomb strings are in the form of "B:<owners>:<power>:<count>"
            String[] cellContentSplit = cellContent.split(":");
            String[] bombOwnersString = cellContentSplit[1].split("\\.");
            int bombPower = Integer.parseInt(cellContentSplit[2]);
            int bombCountdown = Integer.parseInt(cellContentSplit[3]);
            ArrayList<Integer> bombOwners = new ArrayList<Integer>();
            for (String ownerIndex : bombOwnersString) {
              bombOwners.add(Integer.parseInt(ownerIndex));
            }
            bombCountdown--;
            
            if (bombCountdown == 0) {
              // The bomb is exploding
              ArrayList<String> toCheckCellContents = null;
              ArrayList<Integer> deadPlayerIndices = null;    
              // Remove the bomb from the cellContent
              iterCellContents.remove(i);
              cellContentsLength--;
              i--;
              // Recover remaining bomb-count of bomb owners
              for (Integer bombOwnerIndex : bombOwners) {
                playerBombsRemaining[bombOwnerIndex]++;
              }
              // Explode the cell
              toCheckCellContents = iterCellContents;
              deadPlayerIndices = getPlayerIndicesInCell(toCheckCellContents);
              addFlareInCell(toCheckCellContents, bombOwners, false);
              for (Integer deadPlayerIndex : deadPlayerIndices) {
                for (Integer deathCause : bombOwners) {
                  playerDeathCause[deadPlayerIndex].add(deathCause);
                }
              }
              // Scan up, down, left, right
              int[] rowIncrementer = {-1, 1, 0, 0};
              int[] colIncrementer = {0, 0, -1, 1};
              for (int directionIndex = 0; directionIndex < 4 ; directionIndex++) {
                int CheckRow = row;
                int CheckCol = col;
                int distance = 0;
                while (true) {
                  boolean isBlastCanContinue = true;
                  CheckRow += rowIncrementer[directionIndex];
                  CheckCol += colIncrementer[directionIndex];
                  distance++;
                  if ((CheckRow < 0) || (CheckRow >= boardHeight) || (CheckCol < 0) || (CheckCol >= boardWidth)) {
                    // Stop spreading if already out of bounds
                    break;
                  }
                  else if (distance > bombPower) {
                    // Stop spreading if the bomb can't reach this cell
                    break;
                  }
                  else {
                    // Explode the cell
                    // XXX: `isWillBeUpdated` is for SUPER DIRTY FIX. Should be true if the target cell
                    // Would receive `tickBomb()` update (reducing its bomb count by 1). If the scanning direction
                    // is down (directionIndex=1) or right (directionIndex=3), target cell has not received `tickBomb()`
                    // update and will later receive an `tickBomb()` update later.
                    boolean isWillBeUpdated = (directionIndex == 1) || (directionIndex == 3); 
                    toCheckCellContents = getBoardCell(CheckRow, CheckCol);
                    deadPlayerIndices = getPlayerIndicesInCell(toCheckCellContents);
                    isBlastCanContinue = addFlareInCell(toCheckCellContents, bombOwners, isWillBeUpdated);
                    for (Integer deadPlayerIndex : deadPlayerIndices) {
                      for (Integer deathCause : bombOwners) {
                        playerDeathCause[deadPlayerIndex].add(deathCause);
                      }
                    }
                  }
                  // Stop if the blast can't continue in this direction
                  if (!isBlastCanContinue) {
                    break;
                  }
                }
              }
            }
            else {
              // The bomb does not explode yet
              String newBombState = "B:" + cellContentSplit[1] + ":" + bombPower + ":" + bombCountdown;
              iterCellContents.set(i, newBombState);
            }
          }
        }  
      }
    }
    
    // Kill players on flare
    for (int i = 0; i < numberOfPlayer; i++) {
      if (isPlayerAlive[i]) {
        int playerRow = playerPositionsRow[i];
        int playerCol = playerPositionsCol[i];
        boolean isCellContainsFlare = false;
        ArrayList<String> cellContents = getBoardCell(playerRow, playerCol);
        if (isCellContainsFlare(cellContents)) {
          isPlayerAlive[i] = false;
          isPlayerConnected[i] = false;
          removePlayerFromCell(cellContents, i);
        }
      }
    }
    
    // Award kill points if somebody died because of bombs.
    for (int deadPlayerIndex = 0; deadPlayerIndex < numberOfPlayer; deadPlayerIndex++) {
      for (Integer causePlayerIndex : playerDeathCause[deadPlayerIndex]) {
        if (causePlayerIndex != deadPlayerIndex) {
          playerScores[causePlayerIndex] += POINTS_KILL_PLAYER;
        }
      }
    }
  }
      
  public static void clearRubbles() {
    // If there's a rubble, grant points to players that cause its destruction 
    for (int row = 0; row < boardHeight; row++) {
      for (int col = 0; col < boardWidth; col++) {
        ArrayList<String> iterBoardCell = getBoardCell(row, col);
        clearRubbleInCell(iterBoardCell);
      }
    }
  }
  
  public static void placeBombs() {
    for (int i = 0; i < numberOfPlayer; i++) {
      if (isPlayerConnected[i]) {
        String move = playerMoves[i];
        if (move.equals("DROP BOMB")) {
          int playerRow = playerPositionsRow[i];
          int playerCol = playerPositionsCol[i];
          ArrayList<String> cellContents = getBoardCell(playerRow, playerCol);
          tryDropBombInCell(cellContents, i);
        }
      }
    }
  }
  
}
