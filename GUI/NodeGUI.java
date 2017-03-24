package GUI;

import java.util.Arrays;

/**
 * This class represents a tile in a map
 */
class NodeGUI {
    private String[] itemList;

    public NodeGUI(String[] itemList) {
        this.itemList = itemList;
    }

    public String[] getItemList() {
        return itemList;
    }

    public void setItemList(String[] itemList) {
        this.itemList = itemList;
    }

    @Override
    public String toString() {
        return "NodeGUI{" +
                "itemList=" + Arrays.toString(itemList) +
                '}';
    }
}
