package GUI;

/**
 * Class to represent the player
 * Created by haryoaw on 21/02/17.
 */
public class PlayerGUI {
    private String playerName;
    private int bombCount;
    private int maxBomb;
    private int range;
    private String status;
    private int point;
    private int xLocation;
    private int yLocation;
    private String latestMove;
    public PlayerGUI(String playerName, int bombCount, int maxBomb, int range, String status, int point) {
        this.playerName = playerName;
        this.bombCount = bombCount;
        this.maxBomb = maxBomb;
        this.range = range;
        this.status = status;
        this.point = point;
        this.xLocation = -1;
        this.yLocation = -1;
    }

    public boolean checkIfPositionSet(){
        return !(this.xLocation == -1 && this.yLocation == -1);
    }

    public int getxLocation() {
        return xLocation;
    }

    public void setxLocation(int xLocation) {
        this.xLocation = xLocation;
    }

    public int getyLocation() {
        return yLocation;
    }

    public void setyLocation(int yLocation) {
        this.yLocation = yLocation;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public int getBombCount() {
        return bombCount;
    }

    public void setBombCount(int bombCount) {
        this.bombCount = bombCount;
    }

    public int getRange() {
        return range;
    }

    public void setRange(int range) {
        this.range = range;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getPoint() {
        return point;
    }

    public void setPoint(int point) {
        this.point = point;
    }

    public int getMaxBomb() {
        return maxBomb;
    }

    public void setMaxBomb(int maxBomb) {
        this.maxBomb = maxBomb;
    }


    @Override
    public String toString() {
        return "PlayerGUI{" +
                "playerName='" + playerName + '\'' +
                ", bombCount=" + bombCount +
                ", maxBomb=" + maxBomb +
                ", range=" + range +
                ", status='" + status + '\'' +
                ", point=" + point +
                ", xLocation=" + xLocation +
                ", yLocation=" + yLocation +
                '}';
    }

    public String getLatestMove() {
        return latestMove;
    }

    public void setLatestMove(String latestMove) {
        this.latestMove = latestMove;
    }
}
