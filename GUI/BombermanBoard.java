package GUI;

import java.util.Arrays;

/**
 * This class represent the board of the bomberman
 * Created by haryoaw on 21/02/17.
 */
public class BombermanBoard {
    private int height;
    private int width;
    private NodeGUI[][] map;

    public BombermanBoard(int height, int width, NodeGUI[][] map) {
        this.height = height;
        this.width = width;
        this.map = map;
    }

    public BombermanBoard(int height, int width) {
        this.height = height;
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public NodeGUI[][] getMap() {
        return map;
    }

    public void setMap(NodeGUI[][] map) {
        this.map = map;
    }

    @Override
    public String toString() {
        return "BombermanBoard{" +
                "height=" + height +
                ", width=" + width +
                ", map=" + Arrays.toString(map) +
                '}';
    }
}

