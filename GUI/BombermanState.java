package GUI;

import java.util.ArrayList;

/**
 * Created by haryoaw on 21/02/17.
 */
public class BombermanState {
    private int turn;
    private int player_count;
    private ArrayList<PlayerGUI> playerGUIS;
    private BombermanBoard board;
    private ArrayList<String> playerActionList;

    public BombermanState(int turn, int player_count, ArrayList<PlayerGUI> playerGUIS, BombermanBoard board) {
        this.turn = turn;
        this.player_count = player_count;
        this.playerGUIS = playerGUIS;
        this.board = board;
    }

    public BombermanState(int turn, int player_count, ArrayList<PlayerGUI> playerGUIS, BombermanBoard board, ArrayList<String> playerActionList) {
        this.turn = turn;
        this.player_count = player_count;
        this.playerGUIS = playerGUIS;
        this.board = board;
        this.playerActionList = playerActionList;
    }

    public int getTurn() {
        return turn;
    }

    public void setTurn(int turn) {
        this.turn = turn;
    }

    public int getPlayer_count() {
        return player_count;
    }

    public void setPlayer_count(int player_count) {
        this.player_count = player_count;
    }

    public ArrayList<PlayerGUI> getPlayerGUIS() {
        return playerGUIS;
    }

    public void setPlayerGUIS(ArrayList<PlayerGUI> playerGUIS) {
        this.playerGUIS = playerGUIS;
    }

    public BombermanBoard getBoard() {
        return board;
    }

    public void setBoard(BombermanBoard board) {
        this.board = board;
    }

    public ArrayList<String> getPlayerActionList() {
        return playerActionList;
    }

    public void setPlayerActionList(ArrayList<String> playerActionList) {
        this.playerActionList = playerActionList;
    }

    @Override
    public String toString() {
        return "BombermanState{" +
                "turn=" + turn +
                ", player_count=" + player_count +
                ", playerGUIS=" + playerGUIS +
                ", board=" + board +
                ", playerActionList=" + playerActionList +
                '}';
    }
}


